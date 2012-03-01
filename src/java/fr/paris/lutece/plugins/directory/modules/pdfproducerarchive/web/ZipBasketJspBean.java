/*
 * Copyright (c) 2002-2012, Mairie de Paris
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
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.directory.business.RecordFieldFilter;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.business.producerconfig.DefaultConfigProducer;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.business.producerconfig.IConfigProducer;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.service.ConfigProducerService;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.service.DirectoryPDFProducerPlugin;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.utils.PDFUtils;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket.ZipBasket;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.service.DirectoryManageZipBasketService;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.service.DirectoryPDFProducerArchiveResourceIdService;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.utils.StatusZipEnum;
import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.web.admin.PluginAdminPageJspBean;
import fr.paris.lutece.portal.web.constants.Messages;
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
    private static final String MESSAGE_EXPORT_ALL_ZIP_ALREADY_EXISTS = "module.directory.pdfproducerarchive.message.error_export_all_zip.already_exists";
    private static final String MESSAGE_ERROR_ADD_ZIP_TO_BASKET_ALLEXPORT = "module.directory.pdfproducerarchive.message.error_add_zip_to_basket.allexport";
    private static final String MESSAGE_ERROR_REMOVE_ZIP_TO_BASKET_ALLEXPORT = "module.directory.pdfproducerarchive.message.error_remove_zip_to_basket.allexport";
    private static final String MESSAGE_CONFIRM_REMOVE_ALL_ZIP = "module.directory.pdfproducerarchive.message.confirm_remove_all_zip";
    private static final String MESSAGE_REMOVE_ALL_ZIP = "module.directory.pdfproducerarchive.message.remove_all_zip";
    private static final String MESSAGE_ERROR_REMOVE_ALL_ZIP = "module.directory.pdfproducerarchive.message.error_remove_all_zip";

    //Markers
    private static final String MARK_ID_DIRECTORY = "idDirectory";

    //marker for zipbasket
    private static final String MARK_LIST_ZIPBASKET = "list_zipbasket";
    private static final String MARK_PERMISSION_DELETE_ZIP = "permission_delete_zip";

    // JSP URL
    private static final String JSP_MANAGE_DIRECTORY_RECORD = DirectoryUtils.JSP_MANAGE_DIRECTORY_RECORD;
    private static final String JSP_MANAGE_ZIPBASKET = "jsp/admin/plugins/directory/modules/pdfproducer/archive/basket/ManageZipBasket.jsp";
    private static final String JSP_DO_ADD_ZIP_TO_BASKET = "jsp/admin/plugins/directory/modules/pdfproducer/archive/basket/AddZipToBasket.jsp";
    private static final String JSP_DO_REMOVE_ZIP_TO_BASKET = "jsp/admin/plugins/directory/modules/pdfproducer/archive/basket/DoRemoveZipBasket.jsp";
    private static final String JSP_DO_EXPORT_ALL_ZIP = "jsp/admin/plugins/directory/modules/pdfproducer/archive/basket/ExportAllZip.jsp";
    private static final String JSP_DO_REMOVE_ALL_ZIP = "jsp/admin/plugins/directory/modules/pdfproducer/archive/basket/RemoveAllZip.jsp";

    //Parameters
    private static final String PARAMETER_ID_DIRECTORY = DirectoryUtils.PARAMETER_ID_DIRECTORY;
    private static final String PARAMETER_ID_DIRECTORY_RECORD = "id_directory_record";
    private static final String PARAMETER_SESSION = DirectoryUtils.PARAMETER_SESSION;
    private static final String PARAMETER_ID_ZIPBASKET = "id_zipbasket";

    //constant
    private static final String DEFAULT_TYPE_FILE_NAME = "default";
    private static final String DIRECTORY_ENTRY_FILE_NAME = "directory_entry";
    private static final DirectoryManageZipBasketService _manageZipBasketService = (DirectoryManageZipBasketService) SpringContextService.getPluginBean( DirectoryPDFProducerPlugin.PLUGIN_NAME,
            "directory-pdfproducer-archive.directoryManageZipBasketService" );
    private static final ConfigProducerService _manageConfigProducerService = (ConfigProducerService) SpringContextService.getPluginBean( DirectoryPDFProducerPlugin.PLUGIN_NAME,
            "directory-pdfproducer.manageConfigProducer" );

    /**
     * display the basket basket
     * @param request request
     * @return page to manage zip
     * @throws AccessDeniedException exception if the user does not have the right
     */
    public String getManageZipToBasket( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdDirectory = request.getParameter( PARAMETER_ID_DIRECTORY );

        if ( !RBACService.isAuthorized( Directory.RESOURCE_TYPE, strIdDirectory,
                    DirectoryPDFProducerArchiveResourceIdService.PERMISSION_GENERATE_ZIP, getUser(  ) ) ||
                StringUtils.isBlank( strIdDirectory ) || !StringUtils.isNumeric( strIdDirectory ) )
        {
            throw new AccessDeniedException(  );
        }

        Map<String, Object> model = new HashMap<String, Object>(  );
        List<ZipBasket> listZipBasket = _manageZipBasketService.loadAllZipBasketByAdminUser( getPlugin(  ),
                getUser(  ).getUserId(  ), DirectoryUtils.convertStringToInt( strIdDirectory ) );

        model.put( MARK_ID_DIRECTORY, strIdDirectory );
        model.put( MARK_LIST_ZIPBASKET, listZipBasket );
        model.put( MARK_PERMISSION_DELETE_ZIP,
            RBACService.isAuthorized( Directory.RESOURCE_TYPE, strIdDirectory,
                DirectoryPDFProducerArchiveResourceIdService.PERMISSION_DELETE_ZIP, getUser(  ) ) );

        Directory directory = _manageZipBasketService.getDirectory( DirectoryUtils.convertStringToInt( strIdDirectory ) );

        if ( !listZipBasket.isEmpty(  ) && ( directory != null ) )
        {
            for ( ZipBasket zipBasket : listZipBasket )
            {
                zipBasket.setListZipBasketAction( _manageZipBasketService.selectActionsByZipBasketState( 
                        DirectoryUtils.convertStringToInt( zipBasket.getZipStatus(  ) ), getLocale(  ), directory,
                        getUser(  ), getPlugin(  ) ) );
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
        String[] listIdsDirectoryRecord = request.getParameterValues( PARAMETER_ID_DIRECTORY_RECORD );

        if ( ( listIdsDirectoryRecord != null ) && ( listIdsDirectoryRecord.length > 0 ) )
        {
            String strIdDirectory = request.getParameter( PARAMETER_ID_DIRECTORY );

            // If the id directory is not in the parameter, then fetch it from the first record
            // assuming all records are from the same directory 
            if ( StringUtils.isBlank( strIdDirectory ) || !StringUtils.isNumeric( strIdDirectory ) )
            {
                String strIdDirectoryRecord = listIdsDirectoryRecord[0];
                int nIdDirectoryRecord = DirectoryUtils.convertStringToInt( strIdDirectoryRecord );
                Record record = _manageZipBasketService.getRecord( nIdDirectoryRecord );

                if ( record != null )
                {
                    strIdDirectory = Integer.toString( record.getDirectory(  ).getIdDirectory(  ) );
                }
            }

            if ( !RBACService.isAuthorized( Directory.RESOURCE_TYPE, strIdDirectory,
                        DirectoryPDFProducerArchiveResourceIdService.PERMISSION_GENERATE_ZIP, getUser(  ) ) ||
                    StringUtils.isBlank( strIdDirectory ) || !StringUtils.isNumeric( strIdDirectory ) )
            {
                return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED,
                    AdminMessage.TYPE_CONFIRMATION );
            }

            int nIdDirectory = DirectoryUtils.convertStringToInt( strIdDirectory );
            UrlItem url = new UrlItem( JSP_DO_ADD_ZIP_TO_BASKET );
            url.addParameter( PARAMETER_ID_DIRECTORY, nIdDirectory );

            for ( String strIdDirectoryRecord : listIdsDirectoryRecord )
            {
                int nIdDirectoryRecord = DirectoryUtils.convertStringToInt( strIdDirectoryRecord );
                Record record = _manageZipBasketService.getRecord( nIdDirectoryRecord );

                if ( ( record == null ) || ( record.getDirectory(  ).getIdDirectory(  ) != nIdDirectory ) )
                {
                    return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
                }

                url.addParameter( PARAMETER_ID_DIRECTORY_RECORD, nIdDirectoryRecord );
            }

            return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_ADD_ZIP_TO_BASKET, url.getUrl(  ),
                AdminMessage.TYPE_CONFIRMATION );
        }

        return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
    }

    /**
     * Add zip to basket or return error message
     * @param request request
     * @return message of confirmation or error
     * @throws AccessDeniedException exception if the user does not have the right
     */
    public String addZipToBasket( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdDirectory = request.getParameter( PARAMETER_ID_DIRECTORY );

        if ( !RBACService.isAuthorized( Directory.RESOURCE_TYPE, strIdDirectory,
                    DirectoryPDFProducerArchiveResourceIdService.PERMISSION_GENERATE_ZIP, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED,
                AdminMessage.TYPE_CONFIRMATION );
        }

        String[] listIdsRecord = request.getParameterValues( PARAMETER_ID_DIRECTORY_RECORD );

        // Check parameteres
        if ( ( listIdsRecord == null ) || ( listIdsRecord.length == 0 ) || StringUtils.isBlank( strIdDirectory ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        // Fetch directory
        int nIdDirectory = DirectoryUtils.convertStringToInt( strIdDirectory );
        Directory directory = _manageZipBasketService.getDirectory( nIdDirectory );

        // Fetch config
        int nIdConfig = _manageConfigProducerService.loadDefaultConfig( getPlugin(  ), nIdDirectory );
        IConfigProducer configProducer = null;

        if ( ( nIdConfig == -1 ) || ( nIdConfig == 0 ) )
        {
            configProducer = new DefaultConfigProducer(  );
        }
        else
        {
            configProducer = _manageConfigProducerService.loadConfig( getPlugin(  ), nIdConfig );
        }

        String strTypeConfigFileName = configProducer.getTypeConfigFileName(  );
        UrlItem url = new UrlItem( getJspManageDirectoryRecord( request, nIdDirectory ) );

        for ( String strIdRecord : listIdsRecord )
        {
            int nIdRecord = DirectoryUtils.convertStringToInt( strIdRecord );
            String strName = null;

            // Build file name
            if ( DEFAULT_TYPE_FILE_NAME.equals( strTypeConfigFileName ) )
            {
                strName = PDFUtils.doPurgeNameFile( StringUtil.replaceAccent( directory.getTitle(  ) ).replace( " ", "_" ) +
                        "_" + strIdRecord );
            }
            else if ( DIRECTORY_ENTRY_FILE_NAME.equals( strTypeConfigFileName ) )
            {
                RecordFieldFilter filter = new RecordFieldFilter(  );
                filter.setIdRecord( nIdRecord );
                filter.setIdEntry( configProducer.getIdEntryFileName(  ) );

                List<RecordField> listRecordField = _manageZipBasketService.getRecordFields( filter );

                for ( RecordField recordField : listRecordField )
                {
                    strName = PDFUtils.doPurgeNameFile( recordField.getEntry(  )
                                                                   .convertRecordFieldValueToString( recordField,
                                getLocale(  ), false, false ) );
                }
            }
            else
            {
                strName = PDFUtils.doPurgeNameFile( configProducer.getTextFileName(  ) + "_" + strIdRecord );
            }

            boolean bAllExportAlreadyExists = _manageZipBasketService.existsZipBasket( getUser(  ).getUserId(  ),
                    getPlugin(  ), nIdDirectory, -1 );

            if ( !bAllExportAlreadyExists )
            {
                boolean bZipAdded = _manageZipBasketService.addZipBasket( request, strName, getUser(  ).getUserId(  ),
                        getPlugin(  ), nIdDirectory, nIdRecord,
                        _manageConfigProducerService.loadListConfigEntry( getPlugin(  ), nIdConfig ) );

                if ( !bZipAdded )
                {
                    return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_ADD_ZIP_TO_BASKET, url.getUrl(  ),
                        AdminMessage.TYPE_STOP );
                }
            }
            else
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_ADD_ZIP_TO_BASKET_ALLEXPORT,
                    url.getUrl(  ), AdminMessage.TYPE_STOP );
            }
        }

        return AdminMessageService.getMessageUrl( request, MESSAGE_ADD_ZIP_TO_BASKET, url.getUrl(  ),
            AdminMessage.TYPE_INFO );
    }

    /**
     * Gets the confirmation to remove zip
     * @param request request
     * @return message of confirmation
     */
    public String getConfirmRemoveZipBasket( HttpServletRequest request )
    {
        String strIdDirectory = request.getParameter( PARAMETER_ID_DIRECTORY );

        if ( !RBACService.isAuthorized( Directory.RESOURCE_TYPE, strIdDirectory,
                    DirectoryPDFProducerArchiveResourceIdService.PERMISSION_DELETE_ZIP, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED,
                AdminMessage.TYPE_CONFIRMATION );
        }

        String strIdZipBasket = request.getParameter( PARAMETER_ID_ZIPBASKET );
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
        String strIdDirectory = request.getParameter( PARAMETER_ID_DIRECTORY );

        if ( !RBACService.isAuthorized( Directory.RESOURCE_TYPE, strIdDirectory,
                    DirectoryPDFProducerArchiveResourceIdService.PERMISSION_DELETE_ZIP, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED,
                AdminMessage.TYPE_CONFIRMATION );
        }

        String strIdZipBasket = request.getParameter( PARAMETER_ID_ZIPBASKET );
        String strIdRecord = request.getParameter( PARAMETER_ID_DIRECTORY_RECORD );

        int nIdDirectory = DirectoryUtils.convertStringToInt( strIdDirectory );

        UrlItem url = new UrlItem( JSP_MANAGE_ZIPBASKET );
        url.addParameter( PARAMETER_ID_DIRECTORY, nIdDirectory );

        int nIdAdminUser = getUser(  ).getUserId(  );
        boolean bAllExportAlreadyExists = _manageZipBasketService.existsZipBasket( nIdAdminUser, getPlugin(  ),
                nIdDirectory, -1 );

        if ( ( DirectoryUtils.convertStringToInt( strIdRecord ) == DirectoryUtils.CONSTANT_ID_NULL ) ||
                !bAllExportAlreadyExists )
        {
            if ( _manageZipBasketService.deleteZipBasket( getPlugin(  ),
                        DirectoryUtils.convertStringToInt( strIdZipBasket ), strIdRecord ) )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_REMOVE_ZIP_TO_BASKET, url.getUrl(  ),
                    AdminMessage.TYPE_INFO );
            }

            return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_REMOVE_ZIP_TO_BASKET, url.getUrl(  ),
                AdminMessage.TYPE_STOP );
        }

        return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_REMOVE_ZIP_TO_BASKET_ALLEXPORT,
            url.getUrl(  ), AdminMessage.TYPE_STOP );
    }

    /**
     * Gets the confirmation to export all zip
     * @param request request
     * @return message
     */
    public String getConfirmExportAllZipBasket( HttpServletRequest request )
    {
        String strIdDirectory = request.getParameter( PARAMETER_ID_DIRECTORY );

        if ( !RBACService.isAuthorized( Directory.RESOURCE_TYPE, strIdDirectory,
                    DirectoryPDFProducerArchiveResourceIdService.PERMISSION_GENERATE_ZIP, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED,
                AdminMessage.TYPE_CONFIRMATION );
        }

        UrlItem url = new UrlItem( JSP_DO_EXPORT_ALL_ZIP );
        url.addParameter( PARAMETER_ID_DIRECTORY, DirectoryUtils.convertStringToInt( strIdDirectory ) );

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

        if ( !RBACService.isAuthorized( Directory.RESOURCE_TYPE, strIdDirectory,
                    DirectoryPDFProducerArchiveResourceIdService.PERMISSION_GENERATE_ZIP, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED,
                AdminMessage.TYPE_CONFIRMATION );
        }

        List<ZipBasket> listZipBasket = _manageZipBasketService.loadAllZipBasketByAdminUser( getPlugin(  ),
                getUser(  ).getUserId(  ), DirectoryUtils.convertStringToInt( strIdDirectory ) );

        boolean bCheckAllFileZipped = true;

        for ( ZipBasket zipBasket : listZipBasket )
        {
            if ( !StatusZipEnum.FINISHED.getId(  ).equals( zipBasket.getZipStatus(  ) ) )
            {
                bCheckAllFileZipped = false;
            }
        }

        UrlItem url = new UrlItem( JSP_MANAGE_ZIPBASKET );
        url.addParameter( PARAMETER_ID_DIRECTORY, DirectoryUtils.convertStringToInt( strIdDirectory ) );

        int nIdDirectory = DirectoryUtils.convertStringToInt( strIdDirectory );
        int nIdAdminUser = getUser(  ).getUserId(  );
        boolean bAllExportAlreadyExists = _manageZipBasketService.existsZipBasket( nIdAdminUser, getPlugin(  ),
                nIdDirectory, -1 );

        if ( bCheckAllFileZipped && !bAllExportAlreadyExists && ( nIdDirectory != DirectoryUtils.CONSTANT_ID_NULL ) )
        {
            Directory directory = _manageZipBasketService.getDirectory( nIdDirectory );

            String strName = StringUtil.replaceAccent( directory.getTitle(  ) ).replace( " ", "_" );

            _manageZipBasketService.exportAllZipFile( strName, nIdAdminUser, getPlugin(  ), nIdDirectory );

            return AdminMessageService.getMessageUrl( request, MESSAGE_EXPORT_ALL_ZIP, url.getUrl(  ),
                AdminMessage.TYPE_INFO );
        }
        else if ( bAllExportAlreadyExists )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_EXPORT_ALL_ZIP_ALREADY_EXISTS, url.getUrl(  ),
                AdminMessage.TYPE_STOP );
        }
        else
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_EXPORT_ALL_ZIP, url.getUrl(  ),
                AdminMessage.TYPE_STOP );
        }
    }

    /**
     * Gets the confirmation to export all zip
     * @param request request
     * @return message
     */
    public String getConfirmRemoveAllZipBasket( HttpServletRequest request )
    {
        String strIdDirectory = request.getParameter( PARAMETER_ID_DIRECTORY );

        if ( !RBACService.isAuthorized( Directory.RESOURCE_TYPE, strIdDirectory,
                    DirectoryPDFProducerArchiveResourceIdService.PERMISSION_GENERATE_ZIP, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED,
                AdminMessage.TYPE_CONFIRMATION );
        }

        UrlItem url = new UrlItem( JSP_DO_REMOVE_ALL_ZIP );
        url.addParameter( PARAMETER_ID_DIRECTORY, DirectoryUtils.convertStringToInt( strIdDirectory ) );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_ALL_ZIP, url.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Remove all zip to basket
     * @param request request
     * @return message
     */
    public String removeAllZip( HttpServletRequest request )
    {
        String strIdDirectory = request.getParameter( PARAMETER_ID_DIRECTORY );

        if ( !RBACService.isAuthorized( Directory.RESOURCE_TYPE, strIdDirectory,
                    DirectoryPDFProducerArchiveResourceIdService.PERMISSION_DELETE_ZIP, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED,
                AdminMessage.TYPE_CONFIRMATION );
        }

        int nIdDirectory = DirectoryUtils.convertStringToInt( strIdDirectory );
        int nIdAdminUser = getUser(  ).getUserId(  );

        if ( nIdDirectory != DirectoryUtils.CONSTANT_ID_NULL )
        {
            UrlItem url = new UrlItem( JSP_MANAGE_ZIPBASKET );
            url.addParameter( PARAMETER_ID_DIRECTORY, nIdDirectory );

            if ( _manageZipBasketService.deleteAllZipBasket( getPlugin(  ), nIdDirectory, nIdAdminUser ) )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_REMOVE_ALL_ZIP, url.getUrl(  ),
                    AdminMessage.TYPE_INFO );
            }

            return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_REMOVE_ALL_ZIP, url.getUrl(  ),
                AdminMessage.TYPE_STOP );
        }

        return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
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
