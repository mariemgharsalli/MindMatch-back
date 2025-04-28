package esprit.mindmatch.Service;

import esprit.mindmatch.Entities.Statistics;
import esprit.mindmatch.Repository.SessionRepository;
import esprit.mindmatch.Repository.SubmissionRepository;
import esprit.mindmatch.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private SessionRepository sessionRepository;

    public Statistics getStatistics() {
        long totalUsers = userRepository.count();
        long totalSessions = sessionRepository.count();
        long totalSubmissions = submissionRepository.count();
        long totalDocumentsUploaded = submissionRepository.countDocumentsUploaded();

        // Ajoutez des logs pour déboguer
        System.out.println("Statistics:");
        System.out.println("Total Users: " + totalUsers);
        System.out.println("Total Sessions: " + totalSessions);
        System.out.println("Total Submissions: " + totalSubmissions);
        System.out.println("Total Documents Uploaded: " + totalDocumentsUploaded);

        Statistics statistics = new Statistics();
        statistics.setTotalUsers(totalUsers);
        statistics.setTotalSessions(totalSessions);
        statistics.setTotalSubmissions(totalSubmissions);
        statistics.setTotalDocumentsUploaded(totalDocumentsUploaded);

        return statistics;
    }
}
