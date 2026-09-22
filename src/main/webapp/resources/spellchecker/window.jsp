<title>Spell Checker</title>
<%@ include file="globals.inc" %>
<!-- frames -->
<% String jsvar=request.getParameter("jsvar");%>

<frameset  rows="212,*">
    <frame name="topframe" src="top.jsp" marginwidth="5" marginheight="5" scrolling="no" frameborder="0">
    <frame name="bottom" src="bottom.jsp?jsvar=<%=jsvar%>" marginwidth="2" marginheight="2" scrolling="no" frameborder="0">
</frameset>
