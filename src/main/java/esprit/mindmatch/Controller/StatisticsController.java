package esprit.mindmatch.Controller;

import esprit.mindmatch.Entities.Statistics;
import esprit.mindmatch.Service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping
    public Statistics getStatistics() {
        return statisticsService.getStatistics();
    }
}
