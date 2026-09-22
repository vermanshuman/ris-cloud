package it.nexera.ris.web.common;

import it.nexera.ris.common.helpers.ResourcesHelper;

public class Constants {

    public static final String API_SUCCESS_CODE = "00";
    public static final String API_FAILURE_CODE = "01";
    public static final String API_FAILURE_MISSING_PARAMETER = "02";

    public static final String ENTITY_ID_NOT_FOUND = "03";

    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";

    public static final String API_LOGIN_SUCCESS = ResourcesHelper.getString("loginApiSuccess");
    public static final String API_CF_NOT_VALID = ResourcesHelper.getString("apiCFNotValid");
    public static final String API_ID_TOKEN_NOT_VALID = ResourcesHelper.getString("idTokenNotValid");
    public static final String API_GET_METADATA_SUCCESS = ResourcesHelper.getString("getMetaDataApiSuccess");
    public static final String API_GET_METADATA_INVALID_DATE = ResourcesHelper.getString("getMetaDataInvalidDate");
    public static final String API_TOKEN_NOT_VALID = ResourcesHelper.getString("apiTokenNotValid");
    public static final String API_DOCID_TOKEN_NOT_VALID = ResourcesHelper.getString("docIdTokenNotValid");
    public static final String API_MASSIVE_SIGNED_FILE_FAILURE = ResourcesHelper.getString("apiMassiveSignedFileFailure");

    public static final String ENTITY_ID_NOT_FOUND_MESSAGE = ResourcesHelper.getString("apiEntityIdNotFound");

    public static final String MIME_TYPE_NOT_VALID = ResourcesHelper.getString("mimeTypeNotValid");

}
