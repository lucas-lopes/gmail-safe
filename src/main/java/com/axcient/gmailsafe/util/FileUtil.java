package com.axcient.gmailsafe.util;

import com.axcient.gmailsafe.service.exception.FileException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Slf4j
@Component
public class FileUtil {

    public static void generateFolderStructure(String folderName, String filename, String emailContent) throws IOException {
        String folder = Constants.FOLDER_DEFAULT + folderName + "/"+ filename + ".txt";
        File file = new File(folder);
        file.getParentFile().mkdir();

        if (file.createNewFile()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(emailContent);
            } catch (IOException e){
                throw new FileException(e.getMessage());
            }
        }
    }

    public static StreamingResponseBody generateZipFile(String backupId,
                                                        List<String> emails,
                                                        ServletOutputStream servletOutputStream) {
        log.info("[generateZipFile] start generating ZIP of all emails received from Gmail");
        final int BUFFER_SIZE = 1024;

        return out -> {
            final ZipOutputStream zos = new ZipOutputStream(servletOutputStream);
            InputStream inputStream = null;

            try {
                for (String email : emails) {
                    File file = new File(Constants.FOLDER_DEFAULT + backupId + "/" + email);
                    ZipEntry zipEntry = new ZipEntry(file.getName());

                    inputStream = new FileInputStream(file);

                    zos.putNextEntry(zipEntry);
                    byte[] bytes = new byte[BUFFER_SIZE];
                    int length;
                    while ((length = inputStream.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                }
            } catch (IOException e) {
                log.error("Exception while reading and streaming data {} ", e.getMessage());
                throw new FileException(e.getMessage());
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                zos.close();
            }
            log.info("[generateZipFile] generated ZIP file with success");
        };
    }

    private static void deleteFolder(String path) {
        if (Optional.ofNullable(path).isPresent()) {
            try {
                FileUtils.deleteDirectory(new File(path));
            } catch (IOException e) {
                throw new FileException(e.getMessage());
            }
        }
    }

    public static void createOutputFolder(String folder) {
        File file = new File(folder);
        if (!file.mkdir()) {
            deleteFolder(folder);
        }
        file.mkdir();
    }

}
