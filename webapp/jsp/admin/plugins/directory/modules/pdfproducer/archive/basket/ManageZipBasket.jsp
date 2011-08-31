<%@ page errorPage="../../../../../../ErrorPage.jsp" %>
<jsp:include page="../../../../../../AdminHeader.jsp" />
<jsp:useBean id="zipBasket" scope="session" class="fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.web.ZipBasketJspBean" />
<% 
	zipBasket.init( request, fr.paris.lutece.plugins.directory.web.ManageDirectoryJspBean.RIGHT_MANAGE_DIRECTORY);
%>
<%= zipBasket.getManageZipToBasket( request ) %>
<%@ include file="../../../../../../AdminFooter.jsp" %>