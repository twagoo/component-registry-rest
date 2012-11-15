
<?xml version="1.0" encoding="UTF-8" ?>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
String redirectURL = application.getInitParameter("componentRegistryDocumentationUrl");
response.sendRedirect(redirectURL);
%>
