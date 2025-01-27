package edu.kit.hci.soli.controller;

import edu.kit.hci.soli.config.NIHCache;
import edu.kit.hci.soli.config.SoliConfiguration;
import edu.kit.hci.soli.config.security.SoliUserDetails;
import edu.kit.hci.soli.domain.Room;
import edu.kit.hci.soli.dto.CalendarEvent;
import edu.kit.hci.soli.service.BookingsService;
import edu.kit.hci.soli.service.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * REST controller for generating the FullCalendar event feed.
 */
@RestController("/api/events")
public class EventFeedController {
    private final BookingsService bookingsRepository;
    private final RoomService roomService;
    private final SoliConfiguration soliConfiguration;

    /**
     * Constructs an EventFeedController with the specified {@link BookingsService}.
     *
     * @param bookingsRepository the service for managing bookings
     * @param roomService        the service for managing rooms
     * @param soliConfiguration  the system configuration
     */
    public EventFeedController(BookingsService bookingsRepository, RoomService roomService, SoliConfiguration soliConfiguration) {
        this.bookingsRepository = bookingsRepository;
        this.roomService = roomService;
        this.soliConfiguration = soliConfiguration;
    }

    /**
     * Retrieves calendar events within the specified time range.
     *
     * @param start     the start date and time
     * @param end       the end date and time
     * @param roomId    the id of the room to show
     * @param principal the authenticated user details
     * @return the list of calendar events
     */
    @GetMapping("/api/{roomId}/events")
    public List<CalendarEvent> getEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PathVariable Long roomId,
            @AuthenticationPrincipal SoliUserDetails principal
    ) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        if (end.minusMonths(3).isAfter(start)) {
            throw new IllegalArgumentException("Time range must be less than 3 months");
        }

        Optional<Room> room = roomService.getOptional(roomId);
        if (room.isEmpty()) {
            throw new IllegalArgumentException("Room not found");
        }

        return bookingsRepository.getCalendarEvents(room.get(), start, end, principal == null ? null : principal.getUser());
    }

    private static final Pattern dtStart = Pattern.compile("DTSTART;VALUE=DATE:(\\d+)");
    private static final Pattern dtEnd = Pattern.compile("DTEND;VALUE=DATE:(\\d+)");
    private final NIHCache<String> holidays = new NIHCache<>(1000 * 60 * 60 * 12, this::fetchHolidays);

    /**
     * Fetches the holidays from an iCalendar source and modifies it to avoid all-day events.
     * This is necessary since FullCalendar displays those kinds of events in a separate row,
     * which isn't desirable here.
     *
     * @return the modified iCalendar
     * @throws IOException if the HTTP request fails
     * @throws InterruptedException if the thread is interrupted during the request
     */
    private String fetchHolidays() throws IOException, InterruptedException {
        try (var hc = HttpClient.newBuilder().build()) {
            String response = hc.send(
                    HttpRequest.newBuilder().uri(soliConfiguration.getHolidayCalendarURL()).build(),
                    HttpResponse.BodyHandlers.ofString()
            ).body();
            response = dtStart.matcher(response).replaceAll("DTSTART:$1T000000Z");
            return dtEnd.matcher(response).replaceAll("DTEND:$1T234500Z");
        }
    }

    /**
     * Retrieves the holidays.
     *
     * @return the holidays in iCalendar format to be used by FullCalendar
     * @throws Exception if it could not be retrieved
     */
    @GetMapping("/api/holidays.ics")
    @ResponseBody
    public String getHolidays() throws Exception {
        return holidays.getWithException();
    }

    /**
     * Handles {@link IllegalArgumentException} and returns a bad request response with the exception message.
     *
     * @param e the IllegalArgumentException
     * @return the response entity with the exception message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
