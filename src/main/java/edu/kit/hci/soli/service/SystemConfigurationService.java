package edu.kit.hci.soli.service;

import edu.kit.hci.soli.domain.OpeningHours;
import edu.kit.hci.soli.domain.SystemConfiguration;

import java.util.List;

/**
 * Service interface for managing the shared system configuration.
 */
public interface SystemConfigurationService {
    /**
     * Checks if guest login is enabled.
     *
     * @return true if guest login is enabled, false otherwise.
     */
    boolean isGuestLoginEnabled();

    /**
     * Sets the guest login status.
     *
     * @param enabled true to enable guest login, false to disable it.
     */
    void setGuestLoginEnabled(boolean enabled);

    /**
     * Gets the opening hours.
     *
     * @return a list of OpeningHours objects representing the opening times.
     */
    List<OpeningHours> getOpeningHours();

    /**
     * Sets the opening hours.
     *
     * @param openingHours a list of OpeningHours objects representing the opening times.
     */
    void setOpeningHours(List<OpeningHours> openingHours);

    /**
     * Gets the system configuration for the specified room.
     *
     * @param roomId the unique identifier of the room.
     * @return the system configuration for the specified room.
     */
    SystemConfiguration getConfigurationByRoomId(int roomId);
}
