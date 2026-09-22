/**
 *
 */
package it.nexera.ris.persistence;

import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.ModulePage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModulesHolder {

    private transient final Logger log = LogManager.getLogger(ModulesHolder.class);

    private static ModulesHolder instance;

    private Map<String, List<Long>> modules = new HashMap<String, List<Long>>();

    private Map<Long, List<String>> modulePages = new HashMap<Long, List<String>>();

    public static synchronized ModulesHolder getInstance() {
        if (instance == null) {
            instance = new ModulesHolder();
        }

        return instance;
    }

    public Map<String, List<Long>> getModules() {
        if (modules != null && !modules.isEmpty()) {
            return modules;
        }

        if (modules == null) {
            modules = new HashMap<String, List<Long>>();
        }

        try {
            for (ModulePage mp : DaoManager.load(ModulePage.class)) {
                List<Long> ids = modules.get(mp.getPage_type());
                if (ids == null) {
                    ids = new ArrayList<Long>();
                }
                ids.add(mp.getModule().getId());
                modules.put(mp.getPage_type(), ids);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return modules;
    }

    public Map<Long, List<String>> getModulePages() {
        if (modulePages != null && !modulePages.isEmpty()) {
            return modulePages;
        }

        if (modulePages == null) {
            modulePages = new HashMap<Long, List<String>>();
        }

        try {
            for (ModulePage mp : DaoManager.load(ModulePage.class)) {
                List<String> pages = modulePages.get(mp.getModule().getId());
                if (pages == null) {
                    pages = new ArrayList<String>();
                }
                pages.add(mp.getPage_type());
                modulePages.put(mp.getModule().getId(), pages);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return modulePages;
    }
}
