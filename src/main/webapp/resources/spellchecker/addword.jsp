<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ include file="globals.inc" %>

<html>
<head>
	<title>addword</title>
</head>
<%
    String word = request.getParameter("word");

    if (word == null || !java.util.regex.Pattern
            .matches("^[a-zA-Z0-9_.\\[\\] ']+$", word))
    {
        out.println("Invalid Word.");
        return;
    }
%>

<%
    com.foundeo.spellchecker.Checker sc = new com.foundeo.spellchecker.Checker();
    sc.addWord(userdict, word);
%>


<body onLoad="parent.topframe.increment()">
<font face="verdana" size="-2">
<span name="viewer" id="viewer">&nbsp;&nbsp;<%=word%> Added To Dictionary....</span>
</font>

</body>
</html>
