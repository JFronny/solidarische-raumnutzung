package edu.kit.hci.soli.controller;

import edu.kit.hci.soli.domain.Room;
import edu.kit.hci.soli.dto.KnownError;
import edu.kit.hci.soli.dto.LayoutParams;
import edu.kit.hci.soli.service.RoomService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller class for viewing and managing rooms.
 */
@Controller("/")
@Slf4j
public class RoomsController {
    private final RoomService roomService;

    /**
     * Constructs a new RoomsController instance.
     *
     * @param roomService the room service to be used by this controller
     */
    public RoomsController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Handles GET requests to the root endpoint.
     * If there is only one room, redirects to that room.
     *
     * @param model the model to which attributes are added
     * @param layout the layout parameters
     * @return the name of the view to be rendered
     */
    @GetMapping("/")
    public String roomList(Model model,
                           @ModelAttribute("layout") LayoutParams layout) {
        List<Room> rooms = roomService.getAll();
        if (rooms.size() == 1) {
            return "redirect:/" + rooms.get(0).getId();
        }
        layout.setRoom(null);
        model.addAttribute("rooms", rooms);
        return "rooms";
    }

    /**
     * Shows the room list in edit mode.
     *
     * @param model the model to which attributes are added
     * @param layout the layout parameters
     * @return the name of the view to be rendered
     */
    @GetMapping("/admin/rooms")
    public String adminRoomList(Model model, @ModelAttribute("layout") LayoutParams layout) {
        List<Room> rooms = roomService.getAll();
        model.addAttribute("rooms", rooms);
        model.addAttribute("edit", true);
        return "rooms";
    }

    /**
     * Shows the form for creating a new room.
     *
     * @param model the model to which attributes are added
     * @param layout the layout parameters
     * @return the name of the view to be rendered
     */
    @GetMapping("/admin/rooms/new")
    public String newRoom(Model model, @ModelAttribute("layout") LayoutParams layout) {
        model.addAttribute("id", null);
        model.addAttribute("name", "");
        model.addAttribute("description", "");
        model.addAttribute("location", "");
        return "admin/edit_room_page";
    }

    /**
     * Shows the form for editing a room.
     *
     * @param model the model to which attributes are added
     * @param response the HTTP response
     * @param roomId the ID of the room to be edited
     * @return the name of the view to be rendered
     */
    @GetMapping("/admin/rooms/{roomId:\\d+}")
    public String editRoom(Model model, HttpServletResponse response,
                           @PathVariable long roomId) {
        Optional<Room> room = roomService.getOptional(roomId);
        if (room.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("error", KnownError.NOT_FOUND);
            return "error/known";
        }
        model.addAttribute("id", roomId);
        model.addAttribute("name", room.get().getName());
        model.addAttribute("description", room.get().getDescription());
        model.addAttribute("location", room.get().getLocation());
        return "admin/edit_room_page";
    }

    /**
     * Shows the deletion dialog for a room.
     *
     * @param model the model to which attributes are added
     * @param response the HTTP response
     * @param roomId the ID of the room to be deleted
     * @return the name of the view to be rendered
     */
    @GetMapping("/admin/rooms/{roomId:\\d+}/delete")
    public String showDeletionDialog(Model model, HttpServletResponse response,
                                     @PathVariable long roomId) {
        Optional<Room> room = roomService.getOptional(roomId);
        if (room.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("error", KnownError.NOT_FOUND);
            return "error/known";
        }
        model.addAttribute("room", room.get());
        return "admin/confirmation/delete_room_page";
    }

    /**
     * Deletes a room.
     *
     * @param model the model to which attributes are added
     * @param response the HTTP response
     * @param roomId the ID of the room to be deleted
     * @return the name of the view to be rendered
     */
    @DeleteMapping("/admin/rooms/{roomId:\\d+}")
    public String deleteRoom(Model model, HttpServletResponse response,
                             @PathVariable long roomId) {
        Optional<Room> room = roomService.getOptional(roomId);
        if (room.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("error", KnownError.NOT_FOUND);
            return "error/known";
        }
        roomService.delete(room.get());
        return "redirect:/admin/rooms";
    }

    /**
     * Handles applying changes to a room from modals created by this controller.
     *
     * @param model the model to which attributes are added
     * @param response the HTTP response
     * @param formData the form data for the room
     * @return the name of the view to be rendered
     */
    @PostMapping(value = "/admin/rooms", consumes = "application/x-www-form-urlencoded")
    public String editOrCreateRoom(Model model, HttpServletResponse response, @ModelAttribute FormData formData) {
        Objects.requireNonNull(formData.name);
        Objects.requireNonNull(formData.description);
        Objects.requireNonNull(formData.location);
        if (formData.target != null) {
            // Some target was picked, edit it
            Optional<Room> room = roomService.getOptional(formData.target);
            if (room.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                model.addAttribute("error", KnownError.NOT_FOUND);
                return "error/known";
            }
            room.get().setName(formData.name);
            room.get().setDescription(formData.description);
            room.get().setLocation(formData.location);
            roomService.save(room.get());
            return "redirect:/admin/rooms";
        } else {
            // No target was picked, create a new room
            roomService.save(new Room(null, formData.name, formData.description, formData.location));
            return "redirect:/admin/rooms";
        }
    }

    /**
     * Data class for room creation form data.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormData {
        /**
         * The target room ID for editing.
         */
        public Long target;
        /**
         * The name of the room.
         */
        public String name;
        /**
         * The description of the room.
         */
        public String description;
        /**
         * The location of the room.
         */
        public String location;
    }
}