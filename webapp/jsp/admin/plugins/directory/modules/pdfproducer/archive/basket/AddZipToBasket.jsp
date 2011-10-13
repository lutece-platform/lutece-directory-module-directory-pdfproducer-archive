<%@ page errorPage="../../../../../../ErrorPage.jsp" %>
<jsp:useBean id="zipBasket" scope="session" class="fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.web.ZipBasketJspBean" />
<jsp:useBean id="directoryDirectory" scope="session" class="fr.paris.lutece.plugins.directory.web.DirectoryJspBean" />
<%
	zipBasket.init( request, fr.paris.lutece.plugins.directory.web.ManageDirectoryJspBean.RIGHT_MANAGE_DIRECTORY);
	zipBasket.addZipToBasket( request );
	response.sendRedirect( directoryDirectory.getRedirectUrl( request ) );
%>