package it.nexera.ris.persistence.integration;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.exceptions.IntegrationConnectionException;
import it.nexera.ris.common.factories.CustomLibLoggerFactory;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.AsapSector;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Hospital;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.logic.ApplicationSettingsValueWrapper;
import org.apache.logging.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ADTIntegrationHelper {

    protected static final Logger hl7ErrorLog = CustomLibLoggerFactory.getHl7ErrorLogger();

    private static ADTIntegrationHelper instance;

    private ADTIntegrationHelper() {
        super();
    }

    public static ADTIntegrationHelper getInstance() throws IntegrationConnectionException {
        if (instance == null) {
            synchronized (ADTIntegrationHelper.class) {
                if (instance == null) {
                    instance = new ADTIntegrationHelper();
                }
            }
        }
        return instance;
    }

    public boolean checkConnection(String url, String username, String password) {
        Connection connection = null;
        boolean result = false;
        try {
            int timeout = DriverManager.getLoginTimeout();
            DriverManager.setLoginTimeout(5);

            if (!ValidationHelper.isNullOrEmpty(url)) {
                Locale.setDefault(Locale.ENGLISH);
                connection = DriverManager.getConnection(url, username,
                        password);
                DriverManager.setLoginTimeout(timeout);
            }
        } catch (Exception e) {
            result = false;
        }

        try {
            if (connection == null || !connection.isValid(5)) {
                result = false;
            } else {
                result = true;
            }
            closeConnection(connection);
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }

        return result;
    }

    private static Connection getConnection(String url, String username, String password)
            throws IntegrationConnectionException, SQLException {
        Connection connection = null;
        try {
            int timeout = DriverManager.getLoginTimeout();
            DriverManager.setLoginTimeout(2);

            if (!ValidationHelper.isNullOrEmpty(url)) {
                Locale.setDefault(Locale.ENGLISH);
                connection = DriverManager.getConnection(url, username, password);
                DriverManager.setLoginTimeout(timeout);
            }
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
            throw new IntegrationConnectionException();
        }

        if (connection == null || !connection.isValid(2)) {
            throw new IntegrationConnectionException(ResourcesHelper.getValidation("cantConnectToDB"));
        }

        return connection;
    }

    private static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {

            }
            connection = null;
        }
    }

    public List<AsapSector> getASAPSIOSectors(String sqlCondition) {
        return getASAPSIOSectors(sqlCondition, null);
    }

    public List<AsapSector> getASAPSIOSectors(String sqlCondition, String hospitalCode) {
        List<AsapSector> result = new ArrayList<>();

        ApplicationSettingsValueWrapper asapSioIntUrl = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_URL);

        ApplicationSettingsValueWrapper asapSioIntUser = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_USERNAME);

        ApplicationSettingsValueWrapper asapSioIntPwd = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_PASSWORD);

        if (asapSioIntUrl != null && asapSioIntUser != null && asapSioIntPwd != null) {
            Connection connection = null;
            PreparedStatement sqlStatement = null;
            ResultSet set = null;
            try {
                connection = getConnection(asapSioIntUrl.getValue(), asapSioIntUser.getValue(), asapSioIntPwd.getValue());

                String selectPart = "SELECT * ";
                String fromPart = "FROM DIC_SECTOR s ";
                String joinPart = "";
                String wherePart = "";
                String orderPart = "ORDER BY s.DESCRIPTION asc";
                if (!ValidationHelper.isNullOrEmpty(sqlCondition)) {
                    wherePart = "WHERE " + sqlCondition;
                }
                if(!ValidationHelper.isNullOrEmpty(hospitalCode)){
                    if(!wherePart.isEmpty()){
                        wherePart += " AND ";
                    } else{
                        wherePart = "WHERE ";
                    }
                    joinPart = "LEFT OUTER JOIN DIC_HOSPITAL h on s.hospital_id = h.id ";
                    wherePart += "lower(h.code) = lower('" + hospitalCode + "') ";
                }
                String sqlQuery = selectPart + fromPart + joinPart + wherePart + orderPart;
                sqlStatement = connection.prepareStatement(sqlQuery);
                set = sqlStatement.executeQuery();
                AsapSector item;
                while (set.next()) {
                    item = new AsapSector(set.getLong("ID"), set.getString("CODE"), set.getString("DESCRIPTION"));
                    result.add(item);
                }
            } catch (Exception e) {
                LogHelper.log(hl7ErrorLog, e);
            } finally {
                if (set != null) {
                    try {
                        set.close();
                    } catch (SQLException e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }
                    set = null;
                }
                if (sqlStatement != null) {
                    try {
                        sqlStatement.close();
                    } catch (SQLException e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }
                    sqlStatement = null;
                }

                closeConnection(connection);
            }
        }
        return result;
    }

    public Long getASAPSIOSectorIdFromCode(String sectorCode) {
        Long result = null;

        ApplicationSettingsValueWrapper asapSioIntUrl = ApplicationSettingsHolder
                .getInstance()
                .getByKey(
                        ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_URL);

        ApplicationSettingsValueWrapper asapSioIntUser = ApplicationSettingsHolder
                .getInstance()
                .getByKey(
                        ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_USERNAME);

        ApplicationSettingsValueWrapper asapSioIntPwd = ApplicationSettingsHolder
                .getInstance()
                .getByKey(
                        ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_PASSWORD);

        if (asapSioIntUrl != null && asapSioIntUser != null
                && asapSioIntPwd != null) {
            Connection connection = null;
            PreparedStatement sqlStatement = null;
            ResultSet set = null;
            try {
                connection = getConnection(asapSioIntUrl.getValue(),
                        asapSioIntUser.getValue(), asapSioIntPwd.getValue());
                String sqlQuery = "SELECT * FROM DIC_SECTOR where code = '"
                        + sectorCode + "'";
                sqlStatement = connection.prepareStatement(sqlQuery);
                set = sqlStatement.executeQuery();
                if (set.next()) {
                    result = Long.valueOf(set.getString("ID"));
                }
            } catch (Exception e) {
                LogHelper.log(hl7ErrorLog, e);
            } finally {
                if (set != null) {
                    try {
                        set.close();
                    } catch (SQLException e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }
                    set = null;
                }
                if (sqlStatement != null) {
                    try {
                        sqlStatement.close();
                    } catch (SQLException e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }
                    sqlStatement = null;
                }

                closeConnection(connection);
            }
        }
        return result;
    }

    // Process situation when the returned sector contains data that exist in
    // ASAP_SIO but does not exist in RIS
    public Sector getASAPSIOSectorById(Long id) {

        ApplicationSettingsValueWrapper asapSioIntUrl = ApplicationSettingsHolder
                .getInstance()
                .getByKey(
                        ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_URL);

        ApplicationSettingsValueWrapper asapSioIntUser = ApplicationSettingsHolder
                .getInstance()
                .getByKey(
                        ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_USERNAME);

        ApplicationSettingsValueWrapper asapSioIntPwd = ApplicationSettingsHolder
                .getInstance()
                .getByKey(
                        ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_PASSWORD);

        if (asapSioIntUrl != null && asapSioIntUser != null
                && asapSioIntPwd != null) {
            Connection connection = null;
            PreparedStatement sqlStatement = null;
            PreparedStatement hospQuery = null;
            ResultSet res = null;

            try {
                connection = getConnection(asapSioIntUrl.getValue(),
                        asapSioIntUser.getValue(), asapSioIntPwd.getValue());

                String sqlQuery = "SELECT * FROM DIC_SECTOR s WHERE ID=" + id;

                sqlStatement = connection.prepareStatement(sqlQuery);
                res = sqlStatement.executeQuery();
                res.next();

                String sectorCode = res.getString("code");

                if (!ValidationHelper.isNullOrEmpty(sectorCode)) {
                    Sector temp = DaoManager.get(Sector.class, new Criterion[]
                            {Restrictions.eq("code", sectorCode)});
                    if (temp != null) {
                        return temp;
                    } else {
                        Sector resultSector = new Sector();

                        Long hospitalId = Long.parseLong(res
                                .getString("HOSPITAL_ID"));

                        String query = "SELECT * FROM DIC_HOSPITAL WHERE ID="
                                + hospitalId;

                        hospQuery = connection.prepareStatement(query);
                        ResultSet tempHosp = hospQuery.executeQuery();
                        tempHosp.next();

                        Hospital hospitalFromRis = DaoManager.get(
                                Hospital.class,
                                new Criterion[]
                                        {Restrictions.eq("code",
                                                tempHosp.getString("code"))});

                        if (hospitalFromRis != null) {
                            resultSector.setHospital(hospitalFromRis);
                        } else {
                            return null;
                        }

                        resultSector.setCode(sectorCode);
                        resultSector.setCreateDate(new SimpleDateFormat(
                                "yyyy-MM-dd hh:mm:ss.S").parse(res
                                .getString("CREATE_DATE")));
                        resultSector.setCreateUserId(Long.parseLong(res
                                .getString("CREATE_USER_ID")));
                        resultSector.setUpdateDate(new SimpleDateFormat(
                                "yyyy-MM-dd hh:mm:ss.S").parse(res
                                .getString("UPDATE_DATE")));
                        resultSector.setUpdateUserId(Long.parseLong(res
                                .getString("UPDATE_USER_ID")));
                        resultSector.setCode(res.getString("CODE"));
                        resultSector.setDescription(res
                                .getString("DESCRIPTION"));
                        resultSector.setVersion(Long.parseLong(res
                                .getString("VERSION")));
                        resultSector.setCdr(res.getString("CDR"));
                        resultSector.setIstat(res.getString("ISTAT"));
                        DaoManager.save(resultSector);
                        return resultSector;
                    }
                }
            } catch (Exception e) {
                LogHelper.log(hl7ErrorLog, e);
            } finally {
                if (res != null) {
                    try {
                        res.close();
                    } catch (SQLException e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }
                    res = null;
                }
                if (sqlStatement != null) {
                    try {
                        sqlStatement.close();
                    } catch (SQLException e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }
                    sqlStatement = null;
                }
                if (hospQuery != null) {
                    try {
                        hospQuery.close();
                    } catch (SQLException e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }
                    sqlStatement = null;
                }

                closeConnection(connection);
            }
        }
        return null;
    }

    public RadiologyExamRequest getRequestFieldsFromSectorId(Long sectorId,
                                                             RadiologyExamRequest request) {
        ApplicationSettingsValueWrapper asapSioIntUrl = ApplicationSettingsHolder
                .getInstance()
                .getByKey(
                        ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_URL);

        ApplicationSettingsValueWrapper asapSioIntUser = ApplicationSettingsHolder
                .getInstance()
                .getByKey(
                        ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_USERNAME);

        ApplicationSettingsValueWrapper asapSioIntPwd = ApplicationSettingsHolder
                .getInstance()
                .getByKey(
                        ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_PASSWORD);

        if (request != null && sectorId != null && asapSioIntUrl != null
                && asapSioIntUser != null && asapSioIntPwd != null) {
            Connection connection = null;
            PreparedStatement sqlStatement = null;
            PreparedStatement hospQuery = null;
            ResultSet res = null;

            try {
                connection = getConnection(asapSioIntUrl.getValue(),
                        asapSioIntUser.getValue(), asapSioIntPwd.getValue());

                String sqlQuery = "SELECT * FROM DIC_SECTOR s WHERE ID="
                        + sectorId;

                sqlStatement = connection.prepareStatement(sqlQuery);
                res = sqlStatement.executeQuery();
                res.next();

                request.setAsapSectorCode(res.getString("CODE"));
                request.setAsapSectorDescription(res.getString("DESCRIPTION"));
                request.setAsapSectorId(sectorId);
            } catch (Exception e) {
                LogHelper.log(hl7ErrorLog, e);
            } finally {
                if (res != null) {
                    try {
                        res.close();
                    } catch (SQLException e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }
                    res = null;
                }
                if (sqlStatement != null) {
                    try {
                        sqlStatement.close();
                    } catch (SQLException e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }
                    sqlStatement = null;
                }
                if (hospQuery != null) {
                    try {
                        hospQuery.close();
                    } catch (SQLException e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }
                    sqlStatement = null;
                }

                closeConnection(connection);
            }
        }

        return request;
    }

    // Process situation when the returned sector contains data that exist in
    // ASAP_SIO but does not exist in RIS
    public AsapSector getASAPSIOSectorWrapperById(Long id) {

        ApplicationSettingsValueWrapper asapSioIntUrl = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_URL);

        ApplicationSettingsValueWrapper asapSioIntUser = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_USERNAME);

        ApplicationSettingsValueWrapper asapSioIntPwd = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_PASSWORD);

        if (asapSioIntUrl != null && asapSioIntUser != null && asapSioIntPwd != null) {
            Connection connection = null;
            PreparedStatement sqlStatement = null;
            PreparedStatement hospQuery = null;
            ResultSet res = null;

            try {
                connection = getConnection(asapSioIntUrl.getValue(), asapSioIntUser.getValue(), asapSioIntPwd.getValue());

                String sqlQuery = "SELECT * FROM DIC_SECTOR s WHERE ID=" + id;

                sqlStatement = connection.prepareStatement(sqlQuery);
                res = sqlStatement.executeQuery();
                res.next();
                String code = res.getString("code");
                String description = res.getString("description");
                if (description != null) {
                    return new AsapSector(id, code, description);
                }
            } catch (Exception e) {
                LogHelper.log(hl7ErrorLog, e);
            } finally {
                if (res != null) {
                    try {
                        res.close();
                    } catch (SQLException e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }
                    res = null;
                }
                if (sqlStatement != null) {
                    try {
                        sqlStatement.close();
                    } catch (SQLException e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }
                    sqlStatement = null;
                }
                if (hospQuery != null) {
                    try {
                        hospQuery.close();
                    } catch (SQLException e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }
                    sqlStatement = null;
                }

                closeConnection(connection);
            }
        }
        return null;
    }

}
