package edu.kit.hci.soli.domain;

import jakarta.persistence.Embeddable;
// import jakarta.validation.constraints.NotNull; <- This import should work with the latest version of the Jakarta EE API
import org.jetbrains.annotations.NotNull; // This is a replacement for the above import

import java.time.LocalTime;

/**
 * Represents the opening hours for a specific day of the week.
 * This class is embeddable and can be used as part of an entity.
 */
@Embeddable
@ValidOpeningHours
public class OpeningHours {
    @NotNull
    private String dayOfWeek;

    @NotNull
    private LocalTime openingTime;

    @NotNull
    private LocalTime closingTime;

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }
}