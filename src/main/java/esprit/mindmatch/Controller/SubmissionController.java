package esprit.mindmatch.Controller;

import esprit.mindmatch.Entities.Submission;
import esprit.mindmatch.Entities.submissionStatus;
import esprit.mindmatch.Service.SubmissionService;
import esprit.mindmatch.file.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping(value = "/submit", consumes = "multipart/form-data")
    public Submission submitCV(
            @RequestParam("userId") Long userId,
            @RequestParam("sessionId") Long sessionId,
            @RequestParam("document") MultipartFile document
    ) {
        System.out.println("Utilisateur ID : " + userId);
        System.out.println("Session ID : " + sessionId);
        System.out.println("Nom du fichier : " + document.getOriginalFilename());
        // Traiter la soumission ici
        return submissionService.createSubmissionWithDocument(userId, sessionId, document);
    }





    @GetMapping
    public List<Submission> getAllSubmissions() {
        return submissionService.getAllSubmissions();
    }

    @GetMapping("/bySession/{sessionId}")
    public List<Submission> getBySession(@PathVariable Long sessionId) {
        return submissionService.getSubmissionsBySession(sessionId);
    }

    @GetMapping("/byUser/{userEmail}")
    public List<Submission> getByUser(@PathVariable String userEmail) {
        return submissionService.getSubmissionsByUser(userEmail);
    }

    @PutMapping("/{submissionId}/status")
    public Submission updateStatus(
            @PathVariable Long submissionId,
            @RequestParam submissionStatus status
    ) {
        return submissionService.updateSubmissionStatus(submissionId, status);
    }

    @GetMapping("/document")
    public ResponseEntity<ByteArrayResource> getDocument(@RequestParam String filePath) {
        try {
            byte[] fileContent = FileUtils.readFileFromLocation(filePath);
            if (fileContent == null) {
                return ResponseEntity.notFound().build();
            }

            String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
            String contentType = Files.probeContentType(Paths.get(filePath));

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                    .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                    .body(new ByteArrayResource(fileContent));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}