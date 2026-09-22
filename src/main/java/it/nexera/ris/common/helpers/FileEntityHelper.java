package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.settings.ApplicationSettingsHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Arrays;

public class FileEntityHelper extends BaseHelper {

    private static final String DEFAULT_FOLDER = "RIS_FILE_ENTITY";

    private static final String DEFAULT_IMAGE_FOLDER = "RIS_IMAGES";

    public static File locateOrCreateSavingDir() {
        return getFile(ApplicationSettingsKeys.FILE_ENTITY_PATH, DEFAULT_FOLDER);
    }

    public static File locateOrCreateSavingImageDir() {
        return getFile(ApplicationSettingsKeys.IMAGE_PATH, DEFAULT_IMAGE_FOLDER);
    }

    private static File getFile(ApplicationSettingsKeys keyPath, String defaultFolder) {
        LocalDate date = LocalDate.now();
        StringBuilder path = new StringBuilder();
        if (!ValidationHelper.isNullOrEmpty(ApplicationSettingsHolder
                .getInstance().getByKey(keyPath))) {
            path.append(ApplicationSettingsHolder.getInstance()
                    .getByKey(keyPath).getValue());
        } else {
            path.append(System.getProperty("catalina.home"))
                    .append(File.separator).append(defaultFolder);
        }

        path.append(File.separator);
        path.append(date.get(ChronoField.YEAR));
        path.append(File.separator);
        path.append(date.get(ChronoField.MONTH_OF_YEAR));
        path.append(File.separator);
        path.append(date.get(ChronoField.DAY_OF_MONTH));

        File dayFolder = new File(path.toString());
        File test = null;

        if (dayFolder.exists()) {

            int i = 1;
            while (true) {
                test = new File(dayFolder, String.valueOf(i));
                if (Arrays.asList(dayFolder.list()).contains(String.valueOf(i))) {
                    if (test.list().length < 500) {
                        break;
                    }
                    i++;
                } else {

                    break;
                }
            }
        } else {
            test = new File(dayFolder, "1");
            test.mkdirs();
        }

        return test;
    }

    public static byte[] loadContentByPath(String path) {
        if (!ValidationHelper.isNullOrEmpty(path)) {
            FileInputStream fileInputStream = null;

            File file = new File(path);
            if (file.exists()) {
                byte[] data = new byte[(int) file.length()];

                try {
                    fileInputStream = new FileInputStream(file);
                    fileInputStream.read(data);

                    return data;
                } catch (Exception e) {
                    LogHelper.log(log, e);
                } finally {
                    try {
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                    } catch (IOException e) {
                        LogHelper.log(log, e);
                    }
                }
            }
        }

        return null;
    }
}
