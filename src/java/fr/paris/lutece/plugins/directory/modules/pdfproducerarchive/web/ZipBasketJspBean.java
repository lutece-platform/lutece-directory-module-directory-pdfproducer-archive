/*
 * Copyright (c) 2002-2011, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.web;

import fr.paris.lutece.plugins.directory.business.Directory;
import fr.paris.lutece.plugins.directory.business.DirectoryHome;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordHome;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.service.DirectoryPDFProducerPlugin;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket.ZipBasket;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket.ZipBasketAction;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.service.DirectoryManageZipBasketService;
import fr.paris.lutece.plugins.directory.service.DirectoryResourceIdService;
import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.web.admin.PluginAdminPageJspBean;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.string.StringUtil;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 * ZipBasketJspBean
 *
 */
public class ZipBasketJspBean extends PluginAdminPageJspBean
{
    // Templates
    private static final String TEMPLATE_MANAGE_ZIP_BASKET = "admin/plugins/directory/modules/pdfproducer/archive/basket/manage_zip_basket.html";

    // Messages (I18n keys)
    private static final String MESSAGE_CONFIRM_ADD_ZIP_TO_BASKET = "module.directory.pdfproducerarchive.message.confirm_add_zip_to_basket";
    private static final String MESSAGE_ADD_ZIP_TO_BASKET = "module.directory.pdfproducerarchive.message.add_zip_to_basket";
    private static final String MESSAGE_ERROR_ADD_ZIP_TO_BASKET = "module.directory.pdfproducerarchive.message.error_add_zip_to_basket";
    private static final String MESSAGE_CONFIRM_REMOVE_ZIP_TO_BASKET = "module.directory.pdfproducerarchive.message.confirm_remove_zip_to_basket";
    private static final String MESSAGE_REMOVE_ZIP_TO_BASKET = "module.directory.pdfproducerarchive.message.remove_zip_to_basket";
    private static final String MESSAGE_ERROR_REMOVE_ZIP_TO_BASKET = "module.directory.pdfproducerarchive.message.error_remove_zip_to_basket";
    private static final String MESSAGE_CONFIRM_EXPORT_ALL_ZIP = "module.directory.pdfproducerarchive.message.confirm_export_all_zip";
    private static final String MESSAGE_EXPORT_ALL_ZIP = "module.directory.pdfproducerarchive.message.export_all_zip";
    private static final String MESSAGE_ERROR_EXPORT_ALL_ZIP = "module.directory.pdfproducerarchive.message.error_export_all_zip";

    //Markers
    private static final String MARK_ID_DIRECTORY = "idDirectory";

    //marker for zipbasket
    private static final String MARK_LIST_ZIPBASKET = "list_zipbasket";

    // JSP URL
    private static final String JSP_MANAGE_DIRECTORY_RECORD = DirectoryUtils.JSP_MANAGE_DIRECTORY_RECORD;
    private static final String JSP_MANAGE_ZIPBASKET = "jsp/admin/plugins/directory/modules/pdfproducer/archive/basket/ManageZipBasket.jsp";
    private static final String JSP_DO_ADD_ZIP_TO_BASKET = "jsp/admin/plugins/directory/modules/pdfproducer/archive/basket/AddZipToBasket.jsp";
    private static final String JSP_DO_REMOVE_ZIP_TO_BASKET = "jsp/admin/plugins/directory/modules/pdfproducer/archive/basket/DoRemoveZipBasket.jsp";
    private static final String JSP_DO_EXPORT_ALL_ZIP = "jsp/admin/plugins/directory/modules/pdfproducer/archive/basket/ExportAllZip.jsp";

    //Parameters
    private static final String PARAMETER_ID_DIRECTORY = DirectoryUtils.PARAMETER_ID_DIRECTORY;
    private static final String PARAMETER_ID_DIRECTORY_RECORD = "id_directory_record";
    private static final String PARAMETER_SESSION = DirectoryUtils.PARAMETER_SESSION;
    private static final String PARAMETER_ID_ZIPBASKET = "id_zipbasket";

    /**
     * return the service to manage zipbasket
     * @return manageZipBasketService
     */
    private static DirectoryManageZipBasketService getDirectoryManageZipBasketService(  )
    {
        DirectoryManageZipBasketService manageZipBasketService = (DirectoryManageZipBasketService) SpringContextService.getPluginBean( DirectoryPDFProducerPlugin.PLUGIN_NAME,
                "directory-pdfproducer-archive.directoryManageZipBasketService" );

        return manageZipBasketService;
    }

    /**
     * display the basket basket
     * @param request request
     * @return page to manage zip
     */
    public String getManageZipToBasket( HttpServletRequest request )
    {
        Map<String, Object> model = new HashMap<String, Object>(  );

        String strIdDirectory = request.getParameter( PARAMETER_ID_DIRECTORY );

        if ( StringUtils.isNotBlank( strIdDirectory ) )
        {
            model.put( MARK_ID_DIRECTORY, strIdDirectory );
        }

        DirectoryManageZipBasketService manageZipBasketService = getDirectoryManageZipBasketService(  );
        List<ZipBasket> listZipBasket = manageZipBasketService.loadAllZipBasketByAdminUser( getPlugin(  ),
                getUser(  ).getUserId(  ), Integer.valueOf( strIdDirectory ).intValue(  ) );
        model.put( MARK_LIST_ZIPBASKET, listZipBasket );

        List<ZipBasketAction> listZipBasketActionPending = manageZipBasketService.selectActionsByZipBasketState( 0,
                getPlugin(  ) );
        List<ZipBasketAction> listZipBasketActionInProgress = manageZipBasketService.selectActionsByZipBasketState( 1,
                getPlugin(  ) );
        List<ZipBasketAction> listZipBasketActionFinished = manageZipBasketService.selectActionsByZipBasketState( 2,
                getPlugin(  ) );
        List<ZipBasketAction> listZipBasketActionFailed = manageZipBasketService.selectActionsByZipBasketState( 3,
                getPlugin(  ) );

        if ( !listZipBasket.isEmpty(  ) )
        {
            for ( ZipBasket zipBasket : listZipBasket )
            {
                if ( zipBasket.getZipStatus(  ).equals( DirectoryUtils.PARAMATER_STATUS_PENDING ) )
                {
                    zipBasket.setListZipBasketAction( listZipBasketActionPending );
                }

                if ( zipBasket.getZipStatus(  ).equals( DirectoryUtils.PARAMATER_STATUS_IN_PROGRESS ) )
                {
                    zipBasket.setListZipBasketAction( listZipBasketActionInProgress );
                }

                if ( zipBasket.getZipStatus(  ).equals( DirectoryUtils.PARAMATER_STATUS_FINISHED ) )
                {
                    zipBasket.setListZipBasketAction( listZipBasketActionFinished );
                }

                if ( zipBasket.getZipStatus(  ).equals( DirectoryUtils.PARAMATER_STATUS_FAILED ) )
                {
                    zipBasket.setListZipBasketAction( listZipBasketActionFailed );
                }
            }
        }

        HtmlTemplate templateList = AppTemplateService.getTemplate( TEMPLATE_MANAGE_ZIP_BASKET, getLocale(  ), model );

        return getAdminPage( templateList.getHtml(  ) );
    }

    /**
     * Gets the confirmation to create zip
     * @param request request
     * @return message of confirmation
     * @throws AccessDeniedException AccessDeniedException
     */
    public String getConfirmAddZipBasket( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdDirectoryRecord = request.getParameter( PARAMETER_ID_DIRECTORY_RECORD );
        int nIdDirectoryRecord = DirectoryUtils.convertStringToInt( strIdDirectoryRecord );
        Record record = RecordHome.findByPrimaryKey( nIdDirectoryRecord, getPlugin(  ) );

        if ( ( record == null ) ||
                !RBACService.isAuthorized( Directory.RESOURCE_TYPE,
                    Integer.toString( record.getDirectory(  ).getIdDirectory(  ) ),
                    DirectoryResourceIdService.PERMISSION_GENERATE_ZIP, getUser(  ) ) )
        {
            throw new AccessDeniedException(  );
        }

        UrlItem url = new UrlItem( JSP_DO_ADD_ZIP_TO_BASKET );
        url.addParameter( PARAMETER_ID_DIRECTORY_RECORD, nIdDirectoryRecord );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_ADD_ZIP_TO_BASKET , url.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Add zip to basket or return error message
     * @param request request
     * @return message of confirmation or error
     */
    public String addZipToBasket( HttpServletRequest request )
    {
        String strIdRecord = request.getParameter( PARAMETER_ID_DIRECTORY_RECORD );

        DirectoryManageZipBasketService manageZipBasketService = getDirectoryManageZipBasketService(  );
        Record record = RecordHome.findByPrimaryKey( DirectoryUtils.convertStringToInt( strIdRecord ), getPlugin(  ) );
        Directory directory = DirectoryHome.findByPrimaryKey( record.getDirectory(  ).getIdDirectory(  ), getPlugin(  ) );

        String strName = strIdRecord + "_" + StringUtil.replaceAccent( directory.getTitle(  ) ).replace( " ", "_" );
        UrlItem url = new UrlItem( getJspManageDirectoryRecord( request, record.getDirectory(  ).getIdDirectory(  ) ) );
        boolean bZipAdded = manageZipBasketService.addZipBasket( request, strName, getUser(  ).getUserId(  ),
                getPlugin(  ), record.getDirectory(  ).getIdDirectory(  ),
                DirectoryUtils.convertStringToInt( strIdRecord ) );

        if ( bZipAdded )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_ADD_ZIP_TO_BASKET, url.getUrl(  ),
                AdminMessage.TYPE_INFO );
        }
        else
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_ADD_ZIP_TO_BASKET, url.getUrl(  ),
                AdminMessage.TYPE_STOP );
        }
    }

    /**
     * Gets the confirmation to remove zip
     * @param request request
     * @return message of confirmation
     */
    public String getConfirmRemoveZipBasket( HttpServletRequest request )
    {
        String strIdZipBasket = request.getParameter( PARAMETER_ID_ZIPBASKET );
        String strIdDirectory = request.getParameter( PARAMETER_ID_DIRECTORY );
        String strIdRecord = request.getParameter( PARAMETER_ID_DIRECTORY_RECORD );

        UrlItem url = new UrlItem( JSP_DO_REMOVE_ZIP_TO_BASKET );
        url.addParameter( PARAMETER_ID_ZIPBASKET, strIdZipBasket );
        url.addParameter( PARAMETER_ID_DIRECTORY, strIdDirectory );
        url.addParameter( PARAMETER_ID_DIRECTORY_RECORD, strIdRecord );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_ZIP_TO_BASKET, url.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Remove zip to basket
     * @param request request
     * @return message
     */
    public String removeZipToBasket( HttpServletRequest request )
    {
        String strIdZipBasket = request.getParameter( PARAMETER_ID_ZIPBASKET );
        String strIdDirectory = request.getParameter( PARAMETER_ID_DIRECTORY );
        String strIdRecord = request.getParameter( PARAMETER_ID_DIRECTORY_RECORD );

        UrlItem url = new UrlItem( JSP_MANAGE_ZIPBASKET );
        url.addParameter( PARAMETER_ID_DIRECTORY, Integer.valueOf( strIdDirectory ).intValue(  ) );

        DirectoryManageZipBasketService manageZipBasketService = getDirectoryManageZipBasketService(  );

        if ( manageZipBasketService.deleteZipBasket( getPlugin(  ), Integer.valueOf( strIdZipBasket ).intValue(  ),
                    strIdRecord ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_REMOVE_ZIP_TO_BASKET, url.getUrl(  ),
                AdminMessage.TYPE_INFO );
        }
        else
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_REMOVE_ZIP_TO_BASKET, url.getUrl(  ),
                AdminMessage.TYPE_STOP );
        }
    }

    /**
     * Gets the confirmation to export all zip
     * @param request request
     * @return message
     */
    public String getConfirmExportAllZipBasket( HttpServletRequest request )
    {
        String strIdDirectory = request.getParameter( PARAMETER_ID_DIRECTORY );

        UrlItem url = new UrlItem( JSP_DO_EXPORT_ALL_ZIP );
        url.addParameter( PARAMETER_ID_DIRECTORY, Integer.valueOf( strIdDirectory ).intValue(  ) );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_EXPORT_ALL_ZIP, url.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * export all zip
     * @param request request
     * @return information message or error message
     */
    public String exportAllZipBasket( HttpServletRequest request )
    {
        String strIdDirectory = request.getParameter( PARAMETER_ID_DIRECTORY );

        DirectoryManageZipBasketService manageZipBasketService = getDirectoryManageZipBasketService(  );
        List<ZipBasket> listZipBasket = manageZipBasketService.loadAllZipBasketByAdminUser( getPlugin(  ),
                getUser(  ).getUserId(  ), Integer.valueOf( strIdDirectory ).intValue(  ) );

        boolean bCheckAllFileZipped = true;

        for ( ZipBasket zipBasket : listZipBasket )
        {
            if ( !zipBasket.getZipStatus(  ).equals( DirectoryUtils.PARAMATER_STATUS_FINISHED ) )
            {
                bCheckAllFileZipped = false;
            }
        }

        UrlItem url = new UrlItem( JSP_MANAGE_ZIPBASKET );
        url.addParameter( PARAMETER_ID_DIRECTORY, Integer.valueOf( strIdDirectory ).intValue(  ) );

        if ( bCheckAllFileZipped )
        {
            int nIdDirectory = Integer.valueOf( strIdDirectory ).intValue(  );
            Directory directory = new Directory(  );

            if ( nIdDirectory != -1 )
            {
                directory = DirectoryHome.findByPrimaryKey( nIdDirectory, getPlugin(  ) );
            }

            String strName = StringUtil.replaceAccent( directory.getTitle(  ) ).replace( " ", "_" );
            int nIdAdminUser = getUser(  ).getUserId(  );

            manageZipBasketService.exportAllZipFile( strName, nIdAdminUser, getPlugin(  ), nIdDirectory );

            return AdminMessageService.getMessageUrl( request, MESSAGE_EXPORT_ALL_ZIP, url.getUrl(  ),
                AdminMessage.TYPE_INFO );
        }
        else
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_EXPORT_ALL_ZIP, url.getUrl(  ),
                AdminMessage.TYPE_STOP );
        }
    }

    /**
     * return url of the jsp manage directory record
     * @param request The HTTP request
     * @param nIdDirectory the directory id
     * @return url of the jsp manage directory record
     */
    private String getJspManageDirectoryRecord( HttpServletRequest request, int nIdDirectory )
    {
        return AppPathService.getBaseUrl( request ) + JSP_MANAGE_DIRECTORY_RECORD + "?" + PARAMETER_ID_DIRECTORY + "=" +
        nIdDirectory + "&" + PARAMETER_SESSION + "=" + PARAMETER_SESSION;
    }
}
