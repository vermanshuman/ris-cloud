<%
	  String path = application.getRealPath(request.getServletPath());
	  path = path.substring(0, path.lastIndexOf(java.io.File.separator));
      String dictionaryPath = path + java.io.File.separator + "dictionaries/it.dic";
      //it.nexera.ris.common.helpers.ResourcesHelper help = new it.nexera.ris.common.helpers.ResourcesHelper();
	  //String userdict = help.getDictionaryPath();
	  String userdict = path + java.io.File.separator + "dictionaries/user/user-dictionary.txt";

        int searchdepth = 50;

       boolean striphtml = true;

       String format = "javascript";
       int     suggestions = 14;
	if (request.getParameter("jsvar") != null) {
		if (!java.util.regex.Pattern.matches("^[a-zA-Z0-9_.\\[\\]]+$", request.getParameter("jsvar"))) {
			out.println("Invalid Jsvar");
			return;
		}
	}
%>
