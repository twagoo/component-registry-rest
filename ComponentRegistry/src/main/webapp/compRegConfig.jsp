<%-- Configuration hosted next to the front end index page, from which it is loaded --%>
<%@ page import="java.net.URI" %>
<%
    final String loglevelParam = request.getParameter("loglevel");
    final String loglevel;
    if(loglevelParam != null) {
        loglevel = loglevelParam;
    } else {
        loglevel = "info" ;
    }
%>
{
  "loglevel": "<%= loglevel %>",
  "cors": false,
  "REST": {
    "url": "<%= application.getInitParameter("eu.clarin.cmdi.componentregistry.serviceRootUrl") %>"
  },
  "backEndVersion": "${project.version}",
  "frontEndVersion": "${frontEndVersion}",
  "deploy": {
    "path": "<%= 
                URI.create(application.getInitParameter("eu.clarin.cmdi.componentregistry.serviceRootUrl"))
                        .getPath() //at same URL as app, so simply take path
            %>"
  }
}
