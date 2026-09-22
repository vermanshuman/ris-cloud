package it.nexera.ris.web.services;

import it.nexera.ris.web.services.base.BaseService;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.Date;

public class TempRemoverService extends BaseService implements Serializable {
    private static final long serialVersionUID = 5762805573705111471L;

    // Remove files older than hour
    private static final long MIN_LAST_CHANGE_TIME_MS = 1 * 1 * 60 * 60 * 1000; // One
    // hour

    private String[] tempFilesDirs;

    public TempRemoverService(String[] tempDirs) {
        super("TempRemoverService");

        tempFilesDirs = tempDirs;
    }

    @Override
    protected void routineFuncInternal() {
        if (tempFilesDirs == null || tempFilesDirs.length == 0) {
            return;
        }

        for (String dir : tempFilesDirs) {
            File tempDir = new File(dir);
            if (tempDir != null && tempDir.isDirectory() && tempDir.exists()) {
                handleDirectory(tempDir);
            }
        }
    }

    private void handleDirectory(File f) {
        if (f != null && f.isDirectory() && f.exists()) {
            File[] tempFiles = f.listFiles(new TempFilesFilter(new Date()));

            if (tempFiles != null) {
                for (File file : tempFiles) {
                    if (file.isFile()) {
                        file.delete();
                    } else if (file.isDirectory()) {
                        handleDirectory(file);
                    }
                }
            }
        }
    }

    private class TempFilesFilter implements FileFilter {
        private Date currentDate;

        public TempFilesFilter(Date currentDate) {
            this.currentDate = currentDate;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.FileFilter#accept(java.io.File)
         */
        @Override
        public boolean accept(File pathname) {
            if (pathname != null && pathname.exists() && pathname.isDirectory()) {
                return true;
            }

            if (pathname != null && pathname.exists() && pathname.isFile()) {
                long diff = currentDate.getTime() - pathname.lastModified();
                if (diff > MIN_LAST_CHANGE_TIME_MS) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    protected int getPollTimeKey() {
        return 30;
    }
}
