<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>
	<div class="tabbable tabbable-custom tabbable-secondary">
		<ul class="nav nav-tabs" data-active="${param._tab_active}">
			<li class="tools pull-right"><a href="javascript:;" class="btn default reload"><i class="fa fa-refresh"></i></a></li>
			<li><a data-toggle="tab" href="${ctx}/admin/aqbx/user-share/edit?id=${param.id}&clone=${param.clone}">基本信息</a></li>
		</ul>
	</div>
</body>
</html>