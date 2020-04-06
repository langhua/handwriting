<%@ page pageEncoding="utf-8" %>
<%
    String swaggerUI = "v/swagger-ui-3.25.0";
%>
<html xmlns:lxslt="http://xml.apache.org/xslt">
<head>
    <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="/handwriting/openapi/<%= swaggerUI %>/dist/swagger-ui.css">
</head>
<body>
<div class="swagger-ui" style="margin: -8px;">
    <div class="topbar">
        <a href="#" class="link"><span>开放接口</span></a>
    </div>
</div>
<br>
<p>
    <a href="overview-frame.html" target="fileFrame">概述</a>
</p>
<p>
<table width="100%">
    <tr>
        <td nowrap><a target="fileFrame"
                      href="/handwriting/openapi/<%= swaggerUI %>/dist/index.html?url=/handwriting/openapi/yaml/Handwriting.yaml">中文手写开放接口</a>
        </td>
    </tr>
</table>
</p>
</body>
</html>
