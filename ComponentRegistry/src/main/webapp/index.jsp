<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html
    xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

        <!--  BEGIN Browser History required section -->
        <link rel="stylesheet" type="text/css" href="history/history.css" />
        <!--  END Browser History required section -->

        <title>${title}</title>
    </head>

    <body>
        <form action="rest/authentication" method="GET">
            <input type="submit" value="Authentication status"/>
        </form>
        <form action="rest/authentication?redirect=${pageContext.request.requestURL}" method="POST">
            <input type="submit" value="Log in"/>
        </form>
    </body>
</html>
