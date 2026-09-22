package it.nexera.ris.common.security.api;

public interface SecurityInfoFactMethod {

    SecurityInfo getSecurityInfoByLogin(String userLogin) throws Exception;

}
