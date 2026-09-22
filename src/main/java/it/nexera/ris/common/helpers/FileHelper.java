package it.nexera.ris.common.helpers;

import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.FileEntity;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

/**
 * FileHelper class Used for working with filesystem
 */
public class FileHelper extends BaseHelper {

    public static String CONTEXT_NAME = "ris";

    public static String realPath;

    public static String getLocalFilePath(String path) throws IOException {
        if (FileHelper.exists(FileHelper.getLocalFileDir(), path)) {

        } else if (FileHelper.exists(FileHelper.getFileDir(), path)) {
            FileHelper.copy(new File(FileHelper.getFileDir(), path), new File(
                    FileHelper.getLocalFileDir(), path));

        } else if (FileHelper.exists(FileHelper.getTempDir(), path)) {
            FileHelper.copy(new File(FileHelper.getTempDir(), path), new File(
                    FileHelper.getLocalFileDir(), path));
        } else {
            return getPathWithDefaultSeparator(path);
        }

        return getPathWithDefaultSeparator(path);
    }

    public static String getFileBase64(String path) {
        String base64String = null;
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                byte[] fileContent = FileEntityHelper.loadContentByPath(path);
                base64String = new String(Base64.encodeBase64(fileContent));
            }
        }
        return base64String;
    }

    public static String getPathWithDefaultSeparator(String path) {
        return path.replace("\\", "/");
    }

    public static boolean exists(String path) throws IOException {
        if (FileHelper.exists(FileHelper.getLocalFileDir(), path)) {
            return true;
        } else if (FileHelper.exists(FileHelper.getFileDir(), path)) {
            return true;

        } else if (FileHelper.exists(FileHelper.getTempDir(), path)) {
            return true;
        }

        return false;
    }

    public static boolean existInTemp(String path) {
        if (FileHelper.exists(FileHelper.getTempDir(), path)) {
            return true;
        }
        return false;
    }

    public static boolean existInLocalTemp(String path) {
        if (FileHelper.exists(FileHelper.getLocalTempDir(), path)) {
            return true;
        }
        return false;
    }

    public static boolean exists(String parent, String filename) {
        return new File(parent, filename).exists();
    }

    public static String getFileName(String path) {
        String name = EncodingHelper.ConvertToUTF8String(path);
        return (new File(name)).getName();
    }

    public static String getFileExtension(String path) {
        if (path.lastIndexOf('.') == -1) {
            return "";
        }

        return path.substring(path.lastIndexOf('.'), path.length());
    }

    public static String getFileNameWOExtension(String path) {
        if (path.lastIndexOf('.') == -1) {
            return path;
        }

        return path.substring(0, path.lastIndexOf('.'));
    }

    public static String newFileName(String name) {
        File file = new File(getBaseDir(), "File" + File.separator
                + CONTEXT_NAME);
        file.setWritable(true, false);
        file.mkdir();

        StringBuilder sb = new StringBuilder();
        sb.append(getBaseDir() + File.separator + "File" + File.separator
                + CONTEXT_NAME + File.separator);
        sb.append(UUID.randomUUID().toString());
        sb.append(FileHelper.getFileExtension(name));

        return sb.toString();
    }

    public static String newFileName(String parent, String dest) {
        File file = new File(getBaseDir(), "File" + File.separator
                + CONTEXT_NAME);
        file.setWritable(true, false);
        file.mkdir();

        StringBuilder sb = new StringBuilder();
        sb.append(getBaseDir() + File.separator + "File" + File.separator
                + CONTEXT_NAME + File.separator + parent + File.separator);
        sb.append(UUID.randomUUID().toString());
        sb.append(FileHelper.getFileExtension(dest));

        return sb.toString();
    }

    public static String getUserDirPath(Long userId) {
        StringBuilder sb = new StringBuilder();

        sb.append("user_" + userId + File.separatorChar);

        File file = new File(getFileDir() + File.separatorChar + sb.toString());
        if (!file.exists()) {
            file.setWritable(true, false);
            file.mkdirs();
        }
        return sb.toString();
    }

    public static String getUserLocalPath(Long userId) {
        StringBuilder sb = new StringBuilder();

        sb.append("user_" + userId + File.separatorChar);

        File file = new File(getLocalFileDir() + File.separatorChar
                + sb.toString());
        if (!file.exists()) {
            file.setWritable(true, false);
            file.mkdirs();
        }
        return sb.toString();
    }

    public static String getBaseDir() {
        return new File(getLocalDir()).getParent();
    }

    public static String getImageDir() {
        return new File(getLocalDir() + "resources" + File.separator + "images")
                .getAbsolutePath();
    }

    public static String getCustomFolderDir(String folder) {
        if (folder != null) {
            return new File(FileHelper.getRealPath(), folder).getAbsolutePath();
        }
        return null;
    }

    public static String getLogsDir() {
        return new File(
                new File(new File(getBaseDir()).getParent()).getParent(),
                "logs").getPath();
    }

    public static String getLocalDir() {
        String s2 = getRealPath();

        // String s2 = FacesContext.getCurrentInstance().getExternalContext()
        // .getRealPath("/");

        if (s2 != null) {
            return s2.substring(0, s2.length() - 1);
        } else {
            return "";
        }
    }

    public static String getRandomFileName(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(UUID.randomUUID().toString());
        sb.append(FileHelper.getFileExtension(name));

        return sb.toString();
    }

    public static String getFileEntityName(RadiologyExamRequestItem radiologyExamRequest, Date reportDate) {
        StringBuffer sb = new StringBuffer();

        sb.append(radiologyExamRequest.getAccessNumberCode());
        sb.append(radiologyExamRequest.getAccessNumberYear());
        sb.append(radiologyExamRequest.getAccessNumberId());

        if (radiologyExamRequest.getReportDate() != null) {
            sb.append(DateTimeHelper
                    .toFileEntityDate(radiologyExamRequest.getReportDate()));
        } else if (reportDate != null) {
            sb.append(DateTimeHelper.toFileEntityDate(reportDate));
        } else {
            sb.append(DateTimeHelper.toFileEntityDate(new Date()));
        }

        sb.append("_");
        sb.append(radiologyExamRequest.getRadiologyExamRequestId());
        sb.append("_");

        sb.append("V");

        if (ValidationHelper.isNullOrEmpty(radiologyExamRequest.getFileEntityId())) {
            sb.append("1");
        } else {
            long version = 0L;
            try {
                String versionStr = DaoManager.getField(FileEntity.class, "versionOfSave", new Criterion[]{
                        Restrictions.eq("id", radiologyExamRequest.getFileEntityId())
                }, null);
                if (!ValidationHelper.isNullOrEmpty(versionStr)) {
                    version = Long.parseLong(versionStr);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }

            sb.append(version + 1);
        }

        sb.append(".pdf");

        return sb.toString().toUpperCase();
    }

    public static String newLocalFileName(String name) {
        File file = new File(getLocalDir(), "File");
        file.setWritable(true, false);
        file.mkdir();

        StringBuilder sb = new StringBuilder();
        sb.append(getLocalDir() + File.separator + "File" + File.separator);
        sb.append(UUID.randomUUID().toString());
        sb.append(FileHelper.getFileExtension(name));

        return sb.toString();
    }

    public static String newTempFileName(String name) {
        File file = new File(getLocalDir(), "Temp");
        file.setWritable(true, false);
        file.mkdir();

        StringBuilder sb = new StringBuilder();
        sb.append(getLocalDir() + File.separator + "Temp" + File.separator);
        sb.append(UUID.randomUUID().toString());
        sb.append(FileHelper.getFileExtension(name));

        return sb.toString();
    }

    public static String getTempDir() {
        File file = new File(getBaseDir(), "Temp");
        file.setWritable(true, false);
        file.mkdir();

        return file.getPath();
    }

    public static String getLocalTempDir() {
        File file = new File(getLocalDir(), "Temp");
        file.setWritable(true, false);
        file.mkdir();

        return file.getPath();
    }

    public static String getFileDir() {
        File file = new File(getBaseDir(), "File" + File.separator
                + CONTEXT_NAME);
        file.setWritable(true, false);
        file.mkdirs();

        return file.getPath();
    }

    public static String getLocalFileDir() {
        File file = new File(getLocalDir(), "File");
        file.setWritable(true, false);
        file.mkdir();

        return file.getPath();
    }

    public static File getProfilePicturePath(Long userId,
                                             String profilePictureName) {
        return new File(FileHelper.getFileDir() + File.separatorChar
                + FileHelper.getUserDirPath(userId), profilePictureName);
    }

    public static String getNewCroppedThumbnailName() {
        return FileHelper.getRandomFileName(".jpg");
    }

    public static boolean delete(File resource) {
        if (resource.isDirectory()) {
            File[] childFiles = resource.listFiles();

            for (File child : childFiles) {
                delete(child);
            }
        }

        return resource.delete();
    }

    public static boolean delete(String resource) {
        if (resource == null || resource.isEmpty()) {
            return false;
        }
        File file = new File(resource);
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();

            for (File child : childFiles) {
                delete(child);
            }
        }

        return file.delete();
    }

    public static boolean delete(String parent, String resource) {
        if (resource == null || resource.isEmpty()) {
            return false;
        }
        File file = new File(parent, resource);
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();

            for (File child : childFiles) {
                delete(child);
            }
        }

        return file.delete();
    }

    public static void moveFile(File source, File dest) throws IOException {
        Boolean bRet = source.renameTo(dest);
        log.info(String.format("Move file to %s was %s", dest, bRet.toString()));
    }

    public static void copy(File fromFile, File toFile) throws IOException {
        if (toFile.isDirectory()) {
            toFile = new File(toFile, fromFile.getName());
            toFile.setWritable(true, false);
            toFile.mkdirs();
        } else {
            File parent = toFile.getParentFile();
            parent.setWritable(true, false);
            parent.mkdirs();
        }

        if (toFile.exists()) {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    System.in));
            String response = in.readLine();

            if (response == null) {
                throw new IOException("FileCopy: empty string.");
            } else if (!response.equals("Y") && !response.equals("y")) {
                throw new IOException("FileCopy: "
                        + "existing file was not overwritten.");
            }
        } else {
            String parent = toFile.getParent();
            if (parent == null) {
                parent = System.getProperty("user.dir");
            }
            File dir = new File(parent);
            if (!dir.exists()) {
                throw new IOException("FileCopy: "
                        + "destination directory doesn't exist: " + parent);
            }
            if (dir.isFile()) {
                throw new IOException("FileCopy: "
                        + "destination is not a directory: " + parent);
            }
            if (!dir.canWrite()) {
                throw new IOException("FileCopy: "
                        + "destination directory is unwriteable: " + parent);
            }
        }

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead); // write
            }
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException e) {
                    ;
                }
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException e) {
                    ;
                }
            }
        }
    }

    public static void moveFileFromTemp(String source, String dest)
            throws IOException {
        moveFile(new File(getTempDir(), source), new File(getFileDir(), dest));
    }

    public static void copyFileFromTemp(String source, String dest)
            throws IOException {
        copy(new File(getTempDir(), source), new File(getFileDir(), dest));
    }

    public static void copyFileFromLocalTemp(String source, String dest)
            throws IOException {
        copy(new File(getLocalTempDir(), source), new File(getFileDir(), dest));
    }

    public static void copyFileFromTempToLocalDir(String source, String dest)
            throws IOException {
        copy(new File(getTempDir(), source), new File(getLocalFileDir(), dest));
    }

    public static void copyFileToTemp(String source, String dest)
            throws IOException {
        copy(new File(getFileDir(), source), new File(getTempDir(), dest));
    }

    public static void copyFileToLocalTemp(String source, String dest)
            throws IOException {
        copy(new File(getFileDir(), source), new File(getLocalTempDir(), dest));
    }

    public static void writeFileToTemp(String name, byte[] data)
            throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(getTempDir(), name));
            out.write(data);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static void writeFileToLocalTemp(String name, byte[] data)
            throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(getLocalTempDir(), name));
            out.write(data);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static void sendFile(String fileName, byte[] data) {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context
                .getExternalContext().getResponse();
        ServletOutputStream output = null;
        try {
            int length = data.length;

            response.reset();
            response.setHeader("Content-Type",
                    FileHelper.getFileExtension(fileName));
            response.setHeader("Content-Length", String.valueOf(length));
            response.setHeader("Content-Disposition", "attachment; filename=\""
                    + fileName + "\"");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            // Streams we will use to read, write the file bytes to our response

            output = response.getOutputStream();
            output.write(data);
            output.flush();
        } catch (Exception e) {
            LogHelper.log(log, e);
        } finally {
            try {
                if (output != null) {
                    context.responseComplete();
                    output.close();
                }
            } catch (IOException e) {
                LogHelper.log(log, e);
            }
        }
    }

    /**
     * Send file throu response
     *
     * @param fileName
     * @param inputFile
     * @param dataLength
     * @param dataLength
     */
    public static void sendFile(String fileName, InputStream inputFile,
                                int dataLength) {
        byte[] data = new byte[dataLength];
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context
                .getExternalContext().getResponse();
        ServletOutputStream output = null;
        try {
            int length = inputFile.read(data);

            response.reset();
            response.setHeader("Content-Type",
                    FileHelper.getFileExtension(fileName));
            response.setHeader("Content-Length", String.valueOf(length));
            response.setHeader("Content-Disposition", "attachment; filename=\""
                    + fileName + "\"");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            // Streams we will use to read, write the file bytes to our response

            output = response.getOutputStream();
            output.write(data);
            output.flush();

        } catch (Exception e) {
            LogHelper.log(log, e);
        } finally {

            try {
                if (output != null) {
                    context.responseComplete();
                    output.close();
                }
            } catch (IOException e) {
                LogHelper.log(log, e);
            }
            try {
                if (inputFile != null) {
                    inputFile.close();
                }
            } catch (IOException e) {
                LogHelper.log(log, e);
            }
        }
    }

    public static String writeFileToFolder(String name, File folder, byte[] data)
            throws IOException {

        File f = new File(folder, name);
        if (!f.exists()) {

            new File(f.getParent()).mkdirs();
            f.createNewFile();
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(folder, name));
            out.write(data);
        } finally {
            if (out != null) {
                out.close();
            }
        }

        return f.getAbsolutePath();
    }

    public static FileEntity getFileEntityFromPathFromCatalina(
            String oldFilePath) {
        FileEntity fileEntity = null;

        if (!ValidationHelper.isNullOrEmpty(oldFilePath)) {
            try {
                String filePathWithNewSeparator = oldFilePath.replace('/',
                        File.separatorChar);
                String filePath = System.getProperty("catalina.home")
                        + filePathWithNewSeparator;

                File file = new File(filePath);

                if (file.exists() && !file.isDirectory()) {
                    byte[] content = null;
                    try (FileInputStream fStream = new FileInputStream(file)) {
                        content = IOUtils.toByteArray(fStream);
                    }

                    fileEntity = new FileEntity();
                    String randomFileName = getRandomFileName("1.pdf");

                    if (Boolean.TRUE.equals(Boolean.valueOf(ResourcesHelper
                            .getString("saveOnHard")))) {

                        File path = FileEntityHelper.locateOrCreateSavingDir();

                        fileEntity.setPath(path.getAbsolutePath()
                                + File.separator + randomFileName);

                        FileHelper.writeFileToFolder(randomFileName, path,
                                content);

                    } else {
                        fileEntity.setContent(content);
                    }

                    fileEntity.setName(getRandomFileName("1.pdf"));
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return fileEntity;
    }

    public static String readLayoutFile(String filename, String folderName) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        try {
            String currentLine = null;

            filename = (new File(FileHelper.getRealPath(), "resources" + File.separator
                    + "layouts" + File.separator + folderName
                    + File.separator + filename).getAbsolutePath());

            //FileReader fr = new FileReader(filename);

            br =  Files.newBufferedReader(Paths.get(filename), StandardCharsets.ISO_8859_1);

            while ((currentLine = br.readLine()) != null) {
                sb.append(currentLine);
                sb.append("\r\n");
            }
        } catch (IOException e) {
            LogHelper.log(log, e);
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                LogHelper.log(log, ex);
            }
        }

        return sb.toString();
    }

    public static String getRealPath() {
        return realPath;
    }

    public static void setRealPath(String realPath) {
        FileHelper.realPath = realPath;
    }

    public static void saveToFile(final byte[] content, final Path filePath) {
        try (BufferedOutputStream bs = new BufferedOutputStream(Files.newOutputStream(filePath))){
            bs.write(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
