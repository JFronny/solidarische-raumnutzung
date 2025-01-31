package edu.kit.hci.soli.test.controller;

import edu.kit.hci.soli.config.SoliConfiguration;
import edu.kit.hci.soli.config.security.SoliUserDetails;
import edu.kit.hci.soli.controller.EventFeedController;
import edu.kit.hci.soli.domain.Room;
import edu.kit.hci.soli.dto.CalendarEvent;
import edu.kit.hci.soli.service.BookingsService;
import edu.kit.hci.soli.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventFeedControllerTest {

    @Mock
    private BookingsService bookingsService;

    @Mock
    private RoomService roomService;

    @Mock
    private SoliConfiguration soliConfiguration;

    @InjectMocks
    private EventFeedController eventFeedController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getEvents_returnsEventsWithinTimeRange() {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2023, 1, 2, 0, 0);
        long roomId = 1L;
        SoliUserDetails principal = mock(SoliUserDetails.class);
        Room room = new Room();
        List<CalendarEvent> events = List.of(new CalendarEvent(null, null, null, null, null));

        when(roomService.getOptional(roomId)).thenReturn(Optional.of(room));
        when(bookingsService.getCalendarEvents(room, start, end, principal.getUser())).thenReturn(events);

        List<CalendarEvent> result = eventFeedController.getEvents(start, end, roomId, principal);

        assertEquals(events, result);
    }

    @Test
    void getEvents_throwsExceptionWhenEndBeforeStart() {
        LocalDateTime start = LocalDateTime.of(2023, 1, 2, 0, 0);
        LocalDateTime end = LocalDateTime.of(2023, 1, 1, 0, 0);
        Long roomId = 1L;
        SoliUserDetails principal = mock(SoliUserDetails.class);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                eventFeedController.getEvents(start, end, roomId, principal));

        assertEquals("End date must be after start date", exception.getMessage());
    }

    @Test
    void getEvents_throwsExceptionWhenTimeRangeExceedsThreeMonths() {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2023, 5, 1, 0, 0);
        Long roomId = 1L;
        SoliUserDetails principal = mock(SoliUserDetails.class);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                eventFeedController.getEvents(start, end, roomId, principal));

        assertEquals("Time range must be less than 3 months", exception.getMessage());
    }

    @Test
    void getEvents_throwsExceptionWhenRoomNotFound() {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2023, 1, 2, 0, 0);
        Long roomId = 1L;
        SoliUserDetails principal = mock(SoliUserDetails.class);

        when(roomService.getOptional(roomId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                eventFeedController.getEvents(start, end, roomId, principal));

        assertEquals("Room not found", exception.getMessage());
    }

    @Test
    void handleIllegalArgumentException_returnsBadRequest() {
        IllegalArgumentException exception = new IllegalArgumentException("Test exception");

        ResponseEntity<String> response = eventFeedController.handleIllegalArgumentException(exception);

        assertEquals(ResponseEntity.badRequest().body("Test exception"), response);
    }
}