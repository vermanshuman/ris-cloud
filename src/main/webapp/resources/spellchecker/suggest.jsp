<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ include file="globals.inc" %>
<html>

<head>
	<title>suggest spelling</title>
	
<% 
	String jsoutput="";
	String word="";
	if(request.getParameter("word")!=null) {
		word=request.getParameter("word");
	}
	if (word == null || !java.util.regex.Pattern.matches("^[a-zA-Z0-9_.\\[\\] ']+$", word)) {
		out.println("Invalid Word.");
		return;
	}
%>

<%
	com.foundeo.spellchecker.Checker sc = new com.foundeo.spellchecker.Checker();
	sc.addDictionary(dictionaryPath);
	sc.setUserDictionary(userdict);
	sc.setSearchDepth(searchdepth);
	sc.setNumSuggestions(suggestions);
	jsoutput = sc.checkSpelling(word, format, striphtml);
%>

<script>
function spell() {
    //JSpellCheck Output
    <%=jsoutput%>
	if (typeof(suggestions) == "undefined") { 
		var suggestions = new Array();
		suggestions[0] = new Array();
	}
	
	parent.topframe.doSuggest("<%=word%>", numMispelled, suggestions);
	
}		
</script>
	
</head>

<body onload="spell()">

<font face="verdana" size="-2">
	<span name="viewer" id="viewer">&nbsp;&nbsp;Suggestions for <%=word%></span>
</font>


</body>
</html>
