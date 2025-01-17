package edu.kit.hci.soli.service.scheduled.impl;

import edu.kit.hci.soli.repository.BookingsRepository;
import edu.kit.hci.soli.repository.UserRepository;
import edu.kit.hci.soli.service.UserService;
import edu.kit.hci.soli.service.scheduled.AutoAnonymizationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AutoAnonymizationServiceImpl implements AutoAnonymizationService {
    private final UserRepository userRepository;
    private final BookingsRepository bookingsRepository;
    private final UserService userService;

    /**
     * Constructor for the account deletion service implementation.
     *
     * @param userRepository the userRepository to use
     */
    public AutoAnonymizationServiceImpl(UserRepository userRepository, BookingsRepository bookingsRepository, UserService userService) {
        this.userRepository = userRepository;
        this.bookingsRepository = bookingsRepository;
        this.userService = userService;
    }

    @Override
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void scheduledAnonymize() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(3);
        bookingsRepository.anonymizeOlderThan(cutoff, userService.resolveAnonUser());
        userRepository.deleteUnusedOlderThan(cutoff);
    }
}
