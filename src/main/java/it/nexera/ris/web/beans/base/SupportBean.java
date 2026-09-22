package it.nexera.ris.web.beans.base;

import it.nexera.ris.common.helpers.MigrateHistoricalReportsHelper;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.*;

@Named("supportBean")
@ViewScoped
public class SupportBean implements Serializable {

    private static final long serialVersionUID = -5215017908911007651L;

    private static final String ERROR_READING_SITE_VERSION = "Error reading site version";

    private static final String MIGRATION_PASSWORD = "RISmigrate";

    private String password;

    private Boolean disableMigration;

    private String migrateDBStatusFromOld;

    private String migrateDBStatusFromCurrent;

    public String getSiteVersion() {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model;
            if ((new File("effective-pom.xml")).exists()) {
                model = reader.read(new FileReader("effective-pom.xml"));
            } else {
                model = reader.read(new InputStreamReader(this.getClass().getResourceAsStream("/effective-pom.xml")));
            }
            return model.getVersion();
        } catch (Exception ignored) {
        }
        return ERROR_READING_SITE_VERSION;
    }

    public void migrateDBfromOld() {
        synchronized (SupportBean.class) {
            if (!this.getDisableMigration().booleanValue()) {
                this.setDisableMigration(Boolean.TRUE);
                MigrateHistoricalReportsHelper migrateHistoricalReportsHelper = new MigrateHistoricalReportsHelper();

                if (migrateHistoricalReportsHelper.migrateFromOldDB()) {
                    setMigrateDBStatusFromOld("Success");
                } else {
                    setMigrateDBStatusFromOld("Fail");
                }
            }
        }
    }

    public void login() {
        if (MIGRATION_PASSWORD.equals(getPassword())) {
            setDisableMigration(Boolean.FALSE);
            setPassword("");
        }
    }

    public String getMigrateDBStatusFromOld() {
        return migrateDBStatusFromOld;
    }

    public void setMigrateDBStatusFromOld(String migrateDBStatusFromOld) {
        this.migrateDBStatusFromOld = migrateDBStatusFromOld;
    }

    public String getMigrateDBStatusFromCurrent() {
        return migrateDBStatusFromCurrent;
    }

    public void setMigrateDBStatusFromCurrent(String migrateDBStatusFromCurrent) {
        this.migrateDBStatusFromCurrent = migrateDBStatusFromCurrent;
    }

    public Boolean getDisableMigration() {
        return disableMigration == null ? Boolean.TRUE : disableMigration;
    }

    public void setDisableMigration(Boolean disableMigration) {
        this.disableMigration = disableMigration;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
