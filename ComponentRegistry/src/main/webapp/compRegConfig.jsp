<%-- Configuration hosted next to the front end index page, from which it is loaded --%>
<%@ page import="java.net.URI" %>
{
  "cors": false,
  "REST": {
    "url": "<%= application.getInitParameter("eu.clarin.cmdi.componentregistry.serviceRootUrl") %>"
  },
  "deploy": {
    "path": "<%= 
                URI.create(application.getInitParameter("eu.clarin.cmdi.componentregistry.serviceRootUrl"))
                        .getPath() //at same URL as app, so simply take path
            %>"
  }
}
