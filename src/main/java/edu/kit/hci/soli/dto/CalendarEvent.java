package edu.kit.hci.soli.dto;

import edu.kit.hci.soli.domain.ShareRoomType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Record representing an event as specified by FullCalendar.
 *
 * @param url           the URL associated with the event
 * @param title         the title of the event
 * @param start         the start date and time of the event
 * @param end           the end date and time of the event
 * @param classNames    the list of CSS classes to use for the generated HTML element of this event
 * @param shareRoomType the type of room sharing
 */
public record CalendarEvent(String url, String title, LocalDateTime start, LocalDateTime end, List<String> classNames, ShareRoomType shareRoomType) {
}
