package edu.kit.hci.soli.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.validation.Valid; // This import should work with the latest version of the Jakarta EE API
import lombok.ToString;

import java.util.List;

/**
 * Entity class representing the system configuration.
 * This table is designed to contain only a single row.
 */
@Entity
@Table(name = "soli_system_configuration")
@ToString
public class SystemConfiguration {
    /**
     * The unique identifier for the system configuration.
     * This is always set to 1 to enforce a single row.
     */
    @Id
    private Integer id = 1;

    /**
     * Flag indicating whether guest login is enabled.
     */
    private boolean guestLoginEnabled;

    @Column(name = "room_id", nullable = false)
    private Integer roomId;

    @ElementCollection
    @Valid // This annotation should work with the latest version of the Jakarta EE API
    private List<OpeningHours> openingHours;

    /**
     * Gets the value of the guestLoginEnabled flag.
     *
     * @return true if guest login is enabled, false otherwise.
     */
    public boolean isGuestLoginEnabled() {
        return guestLoginEnabled;
    }

    /**
     * Sets the value of the guestLoginEnabled flag.
     *
     * @param guestLoginEnabled true to enable guest login, false to disable it.
     */
    public void setGuestLoginEnabled(boolean guestLoginEnabled) {
        this.guestLoginEnabled = guestLoginEnabled;
    }

    /**
     * Gets the room ID.
     *
     * @return the room ID.
     */
    public Integer getRoomId() {
        return roomId;
    }

    /**
     * Sets the room ID.
     *
     * @param roomId the room ID.
     */
    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    /**
     * Gets the list of opening hours.
     *
     * @return the list of opening hours.
     */
    public List<OpeningHours> getOpeningHours() {
        return openingHours;
    }

    /**
     * Sets the list of opening hours.
     *
     * @param openingHours the list of opening hours.
     */
    public void setOpeningHours(List<OpeningHours> openingHours) {
        this.openingHours = openingHours;
    }
}