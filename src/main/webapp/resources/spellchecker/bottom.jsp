<%@ include file="globals.inc" %>
<% String jsvar=request.getParameter("jsvar");%>
<html>

<head>
	<title>it works</title>
	<script>
		function transpose() {
			document.SpellCheckForm.spellCheckContent.value = parent.opener.<%=jsvar%>;
			document.SpellCheckForm.submit();
		}
		
	</script>
</head>

<body onload="transpose()">

<font face="verdana" size="-2">
<span name="viewer" id="viewer">&nbsp;&nbsp;Checking Spelling...</span>
</font>

<form name="SpellCheckForm" style="visibility:hidden;" action="spell.jsp" method="post" target="topframe">
	<textarea name="spellCheckContent" style="visibility:hidden;">a textarea</textarea>
	<input type="hidden" name="jsvar" value="<%=jsvar%>">
</form>

</body>
</html>

