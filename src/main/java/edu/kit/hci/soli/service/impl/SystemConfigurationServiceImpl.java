package edu.kit.hci.soli.service.impl;

import edu.kit.hci.soli.domain.OpeningHours;
import edu.kit.hci.soli.domain.SystemConfiguration;
import edu.kit.hci.soli.repository.BookingsRepository;
import edu.kit.hci.soli.repository.SystemConfigurationRepository;
import edu.kit.hci.soli.service.SystemConfigurationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemConfigurationServiceImpl implements SystemConfigurationService {
    private final SystemConfigurationRepository systemConfigurationRepository;
    private final BookingsRepository bookingsRepository;

    /**
     * Constructs a SystemConfigurationService with the specified {@link SystemConfigurationRepository}.
     *
     * @param systemConfigurationRepository the repository for managing the configuration
     */
    public SystemConfigurationServiceImpl(SystemConfigurationRepository systemConfigurationRepository, BookingsRepository bookingsRepository) {
        this.systemConfigurationRepository = systemConfigurationRepository;
        this.bookingsRepository = bookingsRepository;
    }

    @Override
    public boolean isGuestLoginEnabled() {
        return systemConfigurationRepository.getInstance().isGuestLoginEnabled();
    }

    @Override
    @Transactional
    public void setGuestLoginEnabled(boolean enabled) {
        systemConfigurationRepository.getInstance().setGuestLoginEnabled(enabled);
        if (!enabled) {
            bookingsRepository.deleteAllBookingsByGuests();
        }
    }

    @Override
    public List<OpeningHours> getOpeningHours() {
        return systemConfigurationRepository.getInstance().getOpeningHours();
    }

    @Override
    @Transactional
    public void setOpeningHours(List<OpeningHours> openingHours) {
        systemConfigurationRepository.getInstance().setOpeningHours(openingHours);
    }

    @Override
    public SystemConfiguration getConfigurationByRoomId(int roomId) {
        return systemConfigurationRepository.findByRoomId(roomId);
    }
}