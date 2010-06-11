<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">


<%@page import="clarin.cmdi.componentregistry.Configuration"%><html
	xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body >
 <form action="Admin">  
     <label>Press button to migrate all to dates to ISO standard</label>   
     <input type="submit" name="submit" value="MigrateDates"/>  
 </form>
 <a href="<%Configuration.getInstance().getRegistryRoot();%>">rootDir</a>  
</body>
</html>
