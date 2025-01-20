import edu.kit.hci.soli.domain.OpeningHours;
import edu.kit.hci.soli.domain.SystemConfiguration;
import edu.kit.hci.soli.service.SystemConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing opening hours.
 */
@RestController
@RequestMapping("/api")
public class OpeningHoursController {
    @Autowired
    private SystemConfigurationService systemConfigurationService;

    /**
     * Retrieves the opening hours for a specific room.
     *
     * @param roomId the ID of the room
     * @return a list of opening hours for the specified room
     */
    @GetMapping("/{roomId}/opening-hours")
    public List<OpeningHours> getOpeningHoursByRoomId(@PathVariable Integer roomId) {
        SystemConfiguration config = systemConfigurationService.getConfigurationByRoomId(roomId);
        return config.getOpeningHours();
    }
}