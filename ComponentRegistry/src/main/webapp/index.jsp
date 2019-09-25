<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html
    xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>${title}</title>
    </head>

    <body>
        <p><a href="..">Click here to go to the Component Registry</a></p>
        
        <hr />
        
        <form action="rest/authentication" method="GET">
            <input type="submit" value="Authentication status"/>
        </form>
        <form action="rest/authentication?redirect=${pageContext.request.requestURL}" method="POST">
            <input type="submit" value="Log in"/>
        </form>
    </body>
</html>
