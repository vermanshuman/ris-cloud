package it.nexera.ris.common.xml.wrappers;


import it.nexera.ris.common.annotations.CdaTag;
import it.nexera.ris.common.enums.CdaTags;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class HistoricalReportXMLWrapper {

    public transient final Logger log = LogManager.getLogger(HistoricalReportXMLWrapper.class);

    private Long radiologyExamRequestId;
    private List<RadiologyExamRequestItem> radiologyExamRequestItems;

    private String examCode;

    private String examDescription;

    private RadiologyExamRequest radiologyExamRequest;

    private String patientFiscalCode;

    private String patientName;

    private String patientSurname;

    private Date patientBirthDate;

    private FileEntity fileEntity;

    private Long fileEntityId;

    private String reportResultWithoutTags;

    private String patientId;

    private String sexType;

    private String userFiscalCode;

    private String userFirstName;

    private String userLastName;

    private String accessNumberRequest;

    private String sectorDescriptionRequest;

    private String itemDescription;

    private String insertPDF;

    private Boolean isDocumentHidden;

    private Boolean isDocumentSecure;

    private String patientCounty;

    private String electronicRecipeNumber;

    private String patientCity;

    private String patientCap;

    private String patientAddress;

    private String patientBirthCity;

    private String patientBirthCityCode;
    private static final String[] FISCAL_CODE_PREFIXES = new String[]{"ENI", "STP"};

    private User closingUser;

    private Sector sector;

    private static final Base64 CODER = new Base64();

    @CdaTag(CdaTags.ITEM_LIST)
    public String getItemListTag() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())) {
            String template = FileHelper.readLayoutFile("item.xml", "ReportSearch");
            StringBuilder sb = new StringBuilder();
            for (int r=0; r < getRadiologyExamRequestItems().size() ; r++) {
                RadiologyExamRequestItem item = getRadiologyExamRequestItems().get(r);
                if(examCode == null){
                    setExamCode(item.getRadiologyExam().getCode());
                }
                if(examDescription == null){
                    setExamDescription(item.getRadiologyExam().getDescription());
                }
                sb.append(template
                        .replaceAll("%EXAM_CODE%", item.getRadiologyExam().getRegionalCode())
                        .replaceAll("%EXAM_DESCRIPTION%", item.getRadiologyExam().getDescription())
                        .replaceAll("%ENTRY_ROW_ID%", "Esame" + (r+1))
                        .replaceAll("%REQUEST_PERFORM_DATE%", getPerformDateRequest()));
            }

            return sb.toString();
        }

        return "";
    }

    public List<RadiologyExamRequestItem> getRadiologyExamRequestItems() {
        return radiologyExamRequestItems;
    }

    public void setRadiologyExamRequestItems(List<RadiologyExamRequestItem> radiologyExamRequestItems) {
        this.radiologyExamRequestItems = radiologyExamRequestItems;
    }

    @CdaTag(CdaTags.EXAM_CODE)
    public String getExamCode() {
        return examCode;
    }

    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }


    @CdaTag(CdaTags.EXAM_DESCRIPTION)
    public String getExamDescription() {
        return examDescription;
    }

    public void setExamDescription(String examDescription) {
        this.examDescription = examDescription;
    }

    @CdaTag(CdaTags.REQUEST_PERFORM_DATE)
    public String getPerformDateRequest() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequest())) {
            return DateTimeHelper.toFormatedString(getRadiologyExamRequest().getPerformDate(), DateTimeHelper.XML_DATE_PATTERN);
        }

        return "";
    }

    public RadiologyExamRequest getRadiologyExamRequest() {
        return radiologyExamRequest;
    }

    public void setRadiologyExamRequest(RadiologyExamRequest radiologyExamRequest) {
        this.radiologyExamRequest = radiologyExamRequest;
    }

    @CdaTag(CdaTags.FIRST_ROOT_CODE)
    public String getFirstRootCode() {
        if (!ValidationHelper.isNullOrEmpty(getPatientFiscalCode())) {
            if (fiscalCodeCheck(getPatientFiscalCode())) {
                return ResourcesHelper.getString("searchGenerateXmlRootCodeSE");
            } else {
                return ResourcesHelper.getString("searchGenerateXmlRootCodeNSE");
            }
        }

        return "";
    }

    @CdaTag(CdaTags.FIRST_HOSPITAL_NAME)
    public String getFirstHospitalName() {
        if (!ValidationHelper.isNullOrEmpty(getPatientFiscalCode())) {
            if (fiscalCodeCheck(getPatientFiscalCode())) {
                return ResourcesHelper.getString("searchGenerateXmlHospitalNameSE");
            } else {
                return ResourcesHelper.getString("searchGenerateXmlHospitalNameNSE");
            }
        }

        return "";
    }

    private boolean fiscalCodeCheck(String fiscalCode) {
        for (String prefix : FISCAL_CODE_PREFIXES) {
            if (fiscalCode.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    @CdaTag(CdaTags.PATIENT_FISCAL_CODE)
    public String getPatientFiscalCode() {
        return patientFiscalCode;
    }

    public void setPatientFiscalCode(String patientFiscalCode) {
        this.patientFiscalCode = patientFiscalCode;
    }


    @CdaTag(CdaTags.PATIENT_NAME)
    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    @CdaTag(CdaTags.PATIENT_SURNAME)
    public String getPatientSurname() {
        return patientSurname;
    }

    public void setPatientSurname(String patientSurname) {
        this.patientSurname = patientSurname;
    }

    public Date getPatientBirthDate() {
        return patientBirthDate;
    }

    @CdaTag(CdaTags.PATIENT_BIRTH_DATE)
    public String getPatientBirthDateTag() {
        return DateTimeHelper.toFormatedString(getPatientBirthDate(), DateTimeHelper.XML_SHORT_DATE_PATTERN);
    }

    public void setPatientBirthDate(Date patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

    @CdaTag(CdaTags.FILE_ENTITY_ID)
    public Long getFileEntityId() {
        return fileEntityId;
    }

    public void setFileEntityId(Long fileEntityId) {
        this.fileEntityId = fileEntityId;
    }

    @CdaTag(CdaTags.FILE_ENTITY_CREATE_DATE)
    public String getFileEntityCreateDate() {
        if(!ValidationHelper.isNullOrEmpty(getFileEntity()))
            return DateTimeHelper.toFormatedString(getFileEntity().getCreateDate(),
                    DateTimeHelper.XML_DATE_PATTERN);
        return "";
    }

    @CdaTag(CdaTags.FILE_ENTITY_VERSION)
    public Long getFileEntityVersion() {
        if(!ValidationHelper.isNullOrEmpty(getFileEntity()))
            return getFileEntity().getVersion();
        return null;
    }

    public FileEntity getFileEntity() {
        return fileEntity;
    }

    public void setFileEntity(FileEntity fileEntity) {
        this.fileEntity = fileEntity;
    }

    @CdaTag(CdaTags.HISTORICAL_REPORT_RESULT)
    public String getReportResultWithoutTags() {
        return reportResultWithoutTags;
    }

    public void setReportResultWithoutTags(String reportResultWithoutTags) {
        this.reportResultWithoutTags = reportResultWithoutTags;
    }

    @CdaTag(CdaTags.PATIENT_ID)
    public String getPatientId() {
        if (patientId == null) {
            try {
                patientId = DaoManager.getField(Patient.class, "id", new Criterion[]{
                        Restrictions.eq("fiscalCode", getPatientFiscalCode())
                }, null);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    @CdaTag(CdaTags.PATIENT_SEX_TYPE)
    public String getSexType() {
        if (sexType == null) {
            try {
                String value = DaoManager.getField(Patient.class, "sexType", new Criterion[]{
                        Restrictions.eq("fiscalCode", getPatientFiscalCode())
                }, null);
                if (!ValidationHelper.isNullOrEmpty(value)) {
                    sexType = value.substring(0, 1);
                } else {
                    sexType = "";
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return sexType;
    }

    public void setSexType(String sexType) {
        this.sexType = sexType;
    }

    @CdaTag(CdaTags.USER_FISCAL_CODE)
    public String getUserFiscalCode() {
        if (userFiscalCode == null) {
            if (getClosingUser() != null) {
                userFiscalCode = getClosingUser().getFiscalCode();
            } else {
                userFiscalCode = "";
            }
        }

        return userFiscalCode;
    }

    public void setUserFiscalCode(String userFiscalCode) {
        this.userFiscalCode = userFiscalCode;
    }

    @CdaTag(CdaTags.USER_FIRST_NAME)
    public String getUserFirstName() {
        if (userFirstName == null) {
            if (getClosingUser() != null) {
                userFirstName = getClosingUser().getFirstName();
            } else {
                userFirstName = "";
            }
        }

        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    @CdaTag(CdaTags.USER_LAST_NAME)
    public String getUserLastName() {
        if (userLastName == null) {
            if (getClosingUser() != null) {
                userLastName = getClosingUser().getLastName();
            } else {
                userLastName = "";
            }
        }

        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    @CdaTag(CdaTags.REQUEST_ACCESS_NUMBER)
    public String getAccessNumberRequest() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())) {
            List<String> accessNumbers = new ArrayList<>();

            for (RadiologyExamRequestItem item : getRadiologyExamRequestItems()) {
                if (!accessNumbers.contains(item.getAccessNumber())) {
                    accessNumbers.add(item.getAccessNumber());
                }
            }

            accessNumberRequest = generateFormatString(accessNumbers);
        } else {
            accessNumberRequest = "";
        }

        return accessNumberRequest;
    }

    public String generateFormatString(List<String> strs) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = strs.iterator();
        while (iterator.hasNext()) {
            String str = iterator.next();
            sb.append(str);
            if (iterator.hasNext()) {
                sb.append(" - ");
            }
        }

        return sb.toString();
    }

    public void setAccessNumberRequest(String accessNumberRequest) {
        this.accessNumberRequest = accessNumberRequest;
    }

    @CdaTag(CdaTags.REQUEST_SECTOR_DESCRIPTION)
    public String getSectorDescriptionRequest() {
        if (sectorDescriptionRequest == null) {
            if (getSector() != null) {
                sectorDescriptionRequest = getSector().getDescription();
            } else {
                sectorDescriptionRequest = "";
            }
        }

        return sectorDescriptionRequest;
    }

    public void setSectorDescriptionRequest(String sectorDescriptionRequest) {
        this.sectorDescriptionRequest = sectorDescriptionRequest;
    }

    @CdaTag(CdaTags.REQUEST_ITEMS_DESCR)
    public String getItemDescription() {
        if (itemDescription == null) {
            if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())) {
                String template = FileHelper.readLayoutFile("item_table.xml", "ReportSearch");
                StringBuilder sb = new StringBuilder();
                for (int r=0; r < getRadiologyExamRequestItems().size() ; r++) {
                    RadiologyExamRequestItem item = getRadiologyExamRequestItems().get(r);
                    sb.append(template
                            .replaceAll("%ITEM_ROW_ID%", "Esame" + (r+1))
                            .replaceAll("%ITEM_ROW_DESC%", "Esame_descrizione" + (r+1))
                            .replaceAll("%ITEM_ROW_DATE%", "Esame_data_esecuzione" + (r+1))
                            .replaceAll("%EXAM_ITEM_DESC%", item.getRadiologyExam().getDescription())
                            .replaceAll("%REQUEST_PERFORM_DATE%", getPerformDateRequest()));
                }
                itemDescription = sb.toString();
            }else
                itemDescription = "";
        }

        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    @CdaTag(CdaTags.INSERT_PDF)
    public String getInsertPDF() {
        try {
            if (!ValidationHelper.isNullOrEmpty(this.getFileEntityId())) {
                File file = new File(this.getFileEntity().getPath());
                byte[] bytes = Files.readAllBytes(Paths.get(file.getPath()));
                return CODER.encodeAsString(bytes);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return "";
    }


    public void setInsertPDF(String insertPDF) {
        this.insertPDF = insertPDF;
    }


    @CdaTag(CdaTags.PATIENT_COUNTY)
    public String getPatientCounty() {
        if (patientCounty == null) {
            try {
                RadiologyExamRequest request = DaoManager.get(RadiologyExamRequest.class, getRadiologyExamRequestId());

                if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getPatient())){
                    Patient patient = request.getPatient();
                    if(!ValidationHelper.isNullOrEmpty(patient) && !ValidationHelper.isNullOrEmpty(patient.getProvince())){
                        patientCounty = patient.getProvince().getCode();
                    }
                }
                if(patientCounty == null){
                    patientCounty = "";
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return patientCounty;
    }

    public void setPatientCounty(String patientCounty) {
        this.patientCounty = patientCounty;
    }

    @CdaTag(CdaTags.PATIENT_CITY)
    public String getPatientCity() {
        if (patientCity == null) {
            try {
                RadiologyExamRequest request = DaoManager.get(RadiologyExamRequest.class, getRadiologyExamRequestId());
                if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getPatient())){
                    Patient patient = request.getPatient();
                    if(!ValidationHelper.isNullOrEmpty(patient)){
                        patientCity = patient.getCityDescription();
                    }
                }
                if(patientCity == null){
                    patientCity = "";
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return patientCity;
    }

    public void setPatientCity(String patientCity) {
        this.patientCity = patientCity;
    }

    @CdaTag(CdaTags.PATIENT_CAP)
    public String getPatientCap() {
        if (patientCap == null) {
            try {
                RadiologyExamRequest request = DaoManager.get(RadiologyExamRequest.class, getRadiologyExamRequestId());
                if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getPatient())){
                    Patient patient = request.getPatient();
                    if(!ValidationHelper.isNullOrEmpty(patient)){
                        patientCap = patient.getCap();
                    }
                }
                if(patientCap == null){
                    patientCap = "";
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return patientCap;
    }

    public void setPatientCap(String patientCap) {
        this.patientCap = patientCap;
    }

    @CdaTag(CdaTags.PATIENT_ADDRESS)
    public String getPatientAddress() {
        if (patientAddress == null) {
            try {
                RadiologyExamRequest request = DaoManager.get(RadiologyExamRequest.class, getRadiologyExamRequestId());
                if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getPatient())){
                    Patient patient = request.getPatient();
                    if(!ValidationHelper.isNullOrEmpty(patient)){
                        patientAddress = patient.getAddress();
                    }
                }
                if(patientAddress == null){
                    patientAddress = "";
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return patientAddress;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }

    @CdaTag(CdaTags.PATIENT_BIRTH_CITY)
    public String getPatientBirthCity() {
        if (patientBirthCity == null) {
            try {
                RadiologyExamRequest request = DaoManager.get(RadiologyExamRequest.class, getRadiologyExamRequestId());
                if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getPatient())){
                    Patient patient = request.getPatient();
                    if(!ValidationHelper.isNullOrEmpty(patient)){
                        patientBirthCity = patient.getBirthCityDescription();
                    }
                }
                if(patientBirthCity == null){
                    patientBirthCity = "";
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return patientBirthCity;
    }

    public void setPatientBirthCity(String patientBirthCity) {
        this.patientBirthCity = patientBirthCity;
    }
    @CdaTag(CdaTags.PATIENT_BIRTH_CITY_CODE)
    public String getPatientBirthCityCode() {
        if (patientBirthCityCode == null) {
            try {
                RadiologyExamRequest request = DaoManager.get(RadiologyExamRequest.class, getRadiologyExamRequestId());
                if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getPatient())){
                    Patient patient = request.getPatient();
                    if(!ValidationHelper.isNullOrEmpty(patient)
                            && !ValidationHelper.isNullOrEmpty(patient.getBirthCity())){
                        patientBirthCityCode = patient.getBirthCity().getCode();
                    }
                }
                if(patientBirthCityCode == null){
                    patientBirthCityCode = "";
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return patientBirthCityCode;
    }

    public void setPatientBirthCityCode(String patientBirthCityCode) {
        this.patientBirthCityCode = patientBirthCityCode;
    }

    public void lazyLoadEntities() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestId())) {
            try {
                RadiologyExamRequest request = DaoManager.get(RadiologyExamRequest.class, getRadiologyExamRequestId());
                setRadiologyExamRequest(request);
                List<RadiologyExamRequestItem> items = request.getRadiologyExamRequestItems();
                Hibernate.initialize(items);
                setRadiologyExamRequestItems(items);
                setClosingUser(DaoManager.get(User.class, request.getUserClosingReportId()));
                setSector(request.getSector());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public Long getRadiologyExamRequestId() {
        return radiologyExamRequestId;
    }

    public void setRadiologyExamRequestId(Long radiologyExamRequestId) {
        this.radiologyExamRequestId = radiologyExamRequestId;
    }

    public User getClosingUser() {
        if (closingUser == null) {
            lazyLoadEntities();
        }

        return closingUser;
    }

    public void setClosingUser(User closingUser) {
        this.closingUser = closingUser;
    }

    public Sector getSector() {
        if (sector == null) {
            lazyLoadEntities();
        }

        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public Boolean getDocumentHidden() {
        return isDocumentHidden;
    }

    public void setDocumentHidden(Boolean documentHidden) {
        isDocumentHidden = documentHidden;
    }

    public Boolean getDocumentSecure() {
        return isDocumentSecure;
    }

    public void setDocumentSecure(Boolean documentSecure) {
        isDocumentSecure = documentSecure;
    }

    public String getElectronicRecipeNumber() {
        return electronicRecipeNumber;
    }

    public void setElectronicRecipeNumber(String electronicRecipeNumber) {
        this.electronicRecipeNumber = electronicRecipeNumber;
    }

    @CdaTag(CdaTags.SECOND_ROOT_CODE)
    public String getSecondRootCode() {
        return ResourcesHelper.getString("searchGenerateXmlSecondCodeSE");
    }

    @CdaTag(CdaTags.EXTERNAL_PATIENT_CODE)
    public String getExternalPatientCode() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        if(!ValidationHelper.isNullOrEmpty(getPatientId())){
            Patient patient = DaoManager.get(Patient.class, new Criterion[]{
                    Restrictions.eq("id", Long.valueOf(patientId))
            });
            if(!ValidationHelper.isNullOrEmpty(patient) && !ValidationHelper.isNullOrEmpty(patient.getExternalPatientId()))
                return patient.getExternalPatientId();
            else
                return getPatientFiscalCode();
        }
        return "";
    }

    @CdaTag(CdaTags.CONFIDENTIALITY_CODE)
    public String getConfidentialityCode() {
        return Boolean.TRUE.equals(getDocumentSecure()) ?
                ResourcesHelper.getString("searchGenerateXmlHiddenDocument") :
                ResourcesHelper.getString("searchGenerateXmlNotHiddenDocument");
    }

    @CdaTag(CdaTags.IN_FULFILLMENT_OF_ID_CONTENT)
    public String getInFulfillmentOf() {
        return ValidationHelper.isNullOrEmpty(getElectronicRecipeNumber()) ?
                String.format(ResourcesHelper.getString("searchGenerateXmlIdTagDefault"), getAccessNumberRequest()) :
                String.format(ResourcesHelper.getString("searchGenerateXmlIdTagForElectronicRecipe"), getElectronicRecipeNumber());
    }
}
