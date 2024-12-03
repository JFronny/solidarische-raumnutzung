package edu.kit.hci.soli.controller;

import edu.kit.hci.soli.domain.*;
import edu.kit.hci.soli.dto.KnownError;
import edu.kit.hci.soli.dto.LoginStateModel;
import edu.kit.hci.soli.service.BookingsService;
import edu.kit.hci.soli.service.RoomService;
import edu.kit.hci.soli.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Controller("/bookings")
public class BookingsController {

    private final BookingsService bookingsService;
    private final RoomService roomService;
    private final UserService userService;

    public BookingsController(BookingsService bookingsService, RoomService roomService, UserService userService) {
        this.bookingsService = bookingsService;
        this.roomService = roomService;
        this.userService = userService;
    }

    @GetMapping("/bookings")
    public String userBookings(Model model, HttpServletResponse response, Principal principal) {
        return roomBookings(model, response, principal, roomService.get().getId());
    }


    @DeleteMapping("/bookings/delete/{id}")
    public String deleteBookings(@PathVariable("id") Long id, Model model, HttpServletResponse response, Principal principal) {
        log.info("Received delete request for booking {}", id);
        User user = userService.resolveLoggedInUser(principal);
        Booking booking = bookingsService.getBookingById(id);


        if (booking == null) {
            log.info("Booking {} not found", id);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("error", KnownError.NOT_FOUND);
            return "error_known";
        }

        User admin = userService.resolveAdminUser();

        if (booking.getUser().equals(admin)) {
            bookingsService.delete(booking, BookingsService.BookingDeleteReason.ADMIN);
            log.info("Admin deleted booking {}", id);
            return "redirect:/bookings";
        }

        if (booking.getUser().equals(user)) {
            bookingsService.delete(booking, BookingsService.BookingDeleteReason.SELF);
            log.info("User deleted booking {}", id);
            return "redirect:/bookings";
        }

        log.info("User {} tried to delete booking {} of user {}", user, id, booking.getUser());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        model.addAttribute("error", KnownError.DELETE_NO_PERMISSION);
        return "error_known";
    }

    @GetMapping("/{id}/bookings")
    public String roomBookings(Model model, HttpServletResponse response, Principal principal, @PathVariable Long id) {
        User user = userService.resolveLoggedInUser(principal);
        Room room = roomService.get(id);
        model.addAttribute("bookings", bookingsService.getBookingsByUser(user, room));

        return "bookings";
    }

    @GetMapping("/{id}/bookings/new")
    public String newBooking(
            Model model, HttpServletResponse response, Principal principal, @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        if (!roomService.existsById(id)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("error", KnownError.NOT_FOUND);
            return "error_known";
        }
        if (start == null) {
            start = LocalDateTime.now();
        }
        if (end == null) {
            end = start.plusMinutes(30);
        }
        model.addAttribute("room", id);
        model.addAttribute("start", start);
        model.addAttribute("end", end);

        model.addAttribute("minimumTime", minimumTime());
        model.addAttribute("maximumTime", maximumTime());

        return "create_booking";
    }

    public LocalDateTime currentSlot() {
        return normalize(LocalDateTime.now());
    }

    private LocalDateTime minimumTime() {
        return normalize(LocalDateTime.now().plusMinutes(15));
    }

    private LocalDateTime maximumTime() {
        return normalize(LocalDateTime.now().plusDays(14));
    }

    private LocalDateTime normalize(LocalDateTime time) {
        return time.minusMinutes(time.getMinute() % 15).withSecond(0).withNano(0);
    }

    @PostMapping(value = "/{id}/bookings/new", consumes = "application/x-www-form-urlencoded")
    public String createBooking(
            Model model, HttpServletResponse response, HttpServletRequest request, @PathVariable Long id,
            @ModelAttribute("login") LoginStateModel loginStateModel,
            @ModelAttribute FormData formData
    ) {
        // Validate exists
        if (!roomService.existsById(id)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("error", KnownError.NOT_FOUND);
            return "error_known";
        }
        Room room = roomService.get();
        if (loginStateModel.user() == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            model.addAttribute("error", KnownError.NO_USER);
            return "error_known"; //TODO we should modify the LSM so this never happens
        }
        if (formData.start == null || formData.end == null || formData.priority == null || formData.cooperative == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("error", KnownError.MISSING_PARAMETER);
            return "error_known";
        }
        formData.description = formData.description == null ? "" : formData.description.trim();

        // Validate start and end times
        if (formData.start.isAfter(formData.end)
                || formData.start.isBefore(minimumTime())
                || formData.end.isAfter(formData.start.plusHours(4)) // Keep these in sync with index.jte!
                || formData.end.isAfter(maximumTime())
                || formData.start.getMinute() % 15 != 0
                || formData.end.getMinute() % 15 != 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("error", KnownError.INVALID_TIME);
            return "error_known";
        }

        Booking attemptedBooking = new Booking(
                null,
                formData.description,
                formData.start,
                formData.end,
                formData.cooperative,
                room,
                loginStateModel.user(),
                formData.priority,
                Set.of()
        );
        return handleBookingAttempt(attemptedBooking, bookingsService.attemptToBook(attemptedBooking), request, model);
    }

    @PostMapping(value = "/{id}/bookings/new/resolve", consumes = "application/x-www-form-urlencoded")
    public String resolveConflict(
            Model model, HttpServletRequest request, @PathVariable Long id,
            @ModelAttribute("login") LoginStateModel loginStateModel
    ) {
        Booking attemptedBooking = (Booking) request.getSession().getAttribute("attemptedBooking");
        BookingsService.BookingAttemptResult.PossibleCooperation bookingResult = (BookingsService.BookingAttemptResult.PossibleCooperation) request.getSession().getAttribute("bookingResult");
        return handleBookingAttempt(attemptedBooking, bookingsService.affirm(attemptedBooking, bookingResult), request, model);
    }

    private String handleBookingAttempt(Booking attemptedBooking, BookingsService.BookingAttemptResult bookingResult, HttpServletRequest request, Model model) {
        return switch (bookingResult) {
            case BookingsService.BookingAttemptResult.Failure result -> {
                model.addAttribute("error", KnownError.EVENT_CONFLICT);
                model.addAttribute("conflicts", result.conflict());
                yield "error_known";
            }
            case BookingsService.BookingAttemptResult.Success result -> "redirect:/" + attemptedBooking.getRoom().getId() + "/bookings"; //TODO redirect to the new booking
            case BookingsService.BookingAttemptResult.PossibleCooperation result -> {
                request.getSession().setAttribute("attemptedBooking", attemptedBooking);
                request.getSession().setAttribute("bookingResult", result);
                model.addAttribute("attemptedBooking", attemptedBooking);
                model.addAttribute("bookingResult", result);
                yield "create_booking_conflict";
            }
            case BookingsService.BookingAttemptResult.Staged(var staged) -> {
                model.addAttribute("stagedBooking", staged);
                yield "create_booking_staged";
            }
        };
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormData {
        public LocalDateTime start;
        public LocalDateTime end;
        public String description;
        public Priority priority;
        public ShareRoomType cooperative;
    }
}
