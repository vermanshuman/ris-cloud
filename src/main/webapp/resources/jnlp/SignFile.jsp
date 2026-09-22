<%  response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
    response.setHeader("content-disposition", "inline; filename=\"SignFile.jnlp\"");
    response.setContentType("application/x-java-jnlp-file");

    String customPath = request.getParameter("customPath").substring(1);
%>
<?xml version="1.0" encoding="utf-8"?>
<jnlp spec="1.0+" codebase='<%=request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort()%>'
      href="/<%=customPath%>/resources/jnlp/SignFile.jsp?fileId=<%=request.getParameter("fileId")%>&amp;viewId=<%=request.getParameter("viewId")%>&amp;radiologyExamRequestId=<%=request.getParameter("radiologyExamRequestId")%>&amp;userId=<%=request.getParameter("userId")%>&amp;customPath=<%=request.getParameter("customPath")%>&amp;token=<%=request.getParameter("token")%>&amp;userFiscalCode=<%=request.getParameter("userFiscalCode")%>">
    <information>
        <title>RIS Signer GUI</title>
        <vendor>Artensys</vendor>
        <homepage href="" />
        <description>RIS Signer GUI service</description>
        <offline-allowed/>
    </information>

    <update check="always" policy="always"/>

    <security>
        <all-permissions/>
    </security>
    <resources>
        <j2se version="1.7+" />
        <jar href="/signer/welodge-signer-service-1.4.jar" main="true" />
        <jar href="/signer/icepdf-core-6.1.3.jar" />
        <jar href="/signer/icepdf-viewer-6.1.3.jar" />
        <jar href="/signer/jdigitsign.jar" />
        <jar href="/signer/jcryptoki.jar" />
        <jar href="/signer/jackson-annotations-2.11.1.jar" />
        <jar href="/signer/jackson-core-2.11.1.jar" />
        <jar href="/signer/jackson-databind-2.11.1.jar" />
        <jar href="/signer/jersey-client-1.19.4.jar" />
        <jar href="/signer/jersey-core-1.19.4.jar" />
        <jar href="/signer/jsr311-api-1.1.1.jar" />
        <jar href="/signer/jqdigitsign-1.0.3.2.jar" />
        <jar href="/signer/axis2-adb-1.6.3.jar" />
        <jar href="/signer/axis2-jaxws-1.6.3.jar" />
        <jar href="/signer/axis2-kernel-1.6.3.jar" />
        <jar href="/signer/axis2-metadata-1.6.3.jar" />
        <jar href="/signer/axis2-saaj-1.6.3.jar" />
        <jar href="/signer/axis2-transport-http-1.6.3.jar" />
        <jar href="/signer/axis2-transport-local-1.6.3.jar" />
        <jar href="/signer/commons-httpclient-3.1.jar" />
        <jar href="/signer/axiom-api-1.2.14.jar" />
        <jar href="/signer/axiom-compat-1.2.14.jar" />
        <jar href="/signer/axiom-dom-1.2.14.jar" />
        <jar href="/signer/axiom-impl-1.2.14.jar" />
        <jar href="/signer/XmlSchema-1.4.7.jar" />
        <jar href="/signer/wsdl4j-1.6.2.jar" />
        <jar href="/signer/neethi-3.0.2.jar" />
        <jar href="/signer/geronimo-javamail_1.4_spec-1.7.1.jar" />
        <jar href="/signer/httpcore-4.0.jar" />
        <jar href="/signer/commons-codec-1.2.jar" />
        <jar href="/signer/license-1.0.jar" />
    </resources>
    <application-desc main-class="it.artensys.signer.Signer">
        <argument><%="fileId"%>=<%=request.getParameter("fileId")%></argument>
        <argument><%="viewId"%>=<%=request.getParameter("viewId")%></argument>
        <argument><%="radiologyExamRequestId"%>=<%=request.getParameter("radiologyExamRequestId")%></argument>
        <argument><%="userId"%>=<%=request.getParameter("userId")%></argument>
        <argument><%="token"%>=<%=request.getParameter("token")%></argument>
        <argument><%="userFiscalCode"%>=<%=request.getParameter("userFiscalCode")%></argument>
        <argument><%="passThrough"%>=<%="Document"%></argument>
        <argument><%="serviceGetFileURL"%>=<%=request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + "/" + customPath + "/rest/get_unsigned_file"%></argument>
        <argument><%="serviceSendResultURL"%>=<%=request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + "/" + customPath + "/rest/update_signed_file"%></argument>
    </application-desc>
</jnlp>
