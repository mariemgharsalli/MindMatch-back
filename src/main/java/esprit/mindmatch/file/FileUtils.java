package esprit.mindmatch.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class FileUtils {


    /***
     * @param fileUrl  : chemin absolu du fichier sur le serveur.
     * @return
    But : Lire un fichier depuis son chemin sur le serveur et le renvoyer sous forme de tableau de bytes.
     *
     */
    public static byte[] readFileFromLocation(String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            return null;
        }
        try {
            Path filePath = new File(fileUrl).toPath();
            // Utilise Files.readAllBytes(filePath) pour lire le fichier et le convertir en byte[].
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.warn("Nou file found in the path {}", fileUrl);
        }
        return null;
    }
}
