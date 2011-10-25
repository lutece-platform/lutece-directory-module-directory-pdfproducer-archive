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
package fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.web.action;

import fr.paris.lutece.plugins.directory.business.Directory;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.service.DirectoryPDFProducerArchiveResourceIdService;
import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.plugins.directory.web.action.DirectoryAdminSearchFields;
import fr.paris.lutece.plugins.directory.web.action.IDirectoryAction;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.web.pluginaction.AbstractPluginAction;
import fr.paris.lutece.portal.web.pluginaction.DefaultPluginActionResult;
import fr.paris.lutece.portal.web.pluginaction.IPluginActionResult;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * MassExportZipAction
 *
 */
public class MassExportZipAction extends AbstractPluginAction<DirectoryAdminSearchFields> implements IDirectoryAction
{
    // ACTION
    private static final String ACTION_NAME = "Mass export Zip";

    // TEMPLATES
    private static final String TEMPLATE_BUTTON = "../directory/modules/pdfproducer/archive/actions/mass_export_zip.html";

    // JSP
    private static final String JSP_CONFIRM_ADD_ZIP_BASKET = "jsp/admin/plugins/directory/modules/pdfproducer/archive/basket/ConfirmAddZipToBasket.jsp";

    // PARAMETERS
    private static final String PARAMETER_MASS_ZIP_BASKET_ACTION = "mass_export_zip.x";

    // MARKS
    private static final String MARK_PERMISSION_GENERATE_ZIP = "permission_generate_zip";

    /**
     * {@inheritDoc}
     */
    public void fillModel( HttpServletRequest request, AdminUser adminUser, Map<String, Object> model )
    {
        String strIdDirectory = request.getParameter( DirectoryUtils.PARAMETER_ID_DIRECTORY );
        String strIdResource = StringUtils.isNotBlank( strIdDirectory ) ? strIdDirectory : RBAC.WILDCARD_RESOURCES_ID;
        model.put( MARK_PERMISSION_GENERATE_ZIP,
            RBACService.isAuthorized( Directory.RESOURCE_TYPE, strIdResource,
                DirectoryPDFProducerArchiveResourceIdService.PERMISSION_GENERATE_ZIP, adminUser ) );
    }

    /**
     * {@inheritDoc}
     */
    public String getButtonTemplate(  )
    {
        return TEMPLATE_BUTTON;
    }

    /**
     * {@inheritDoc}
     */
    public String getName(  )
    {
        return ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInvoked( HttpServletRequest request )
    {
        return StringUtils.isNotBlank( request.getParameter( PARAMETER_MASS_ZIP_BASKET_ACTION ) );
    }

    /**
     * {@inheritDoc}
     */
    public IPluginActionResult process( HttpServletRequest request, HttpServletResponse response, AdminUser adminUser,
        DirectoryAdminSearchFields sessionFields ) throws AccessDeniedException
    {
        IPluginActionResult result = new DefaultPluginActionResult(  );

        String strRedirect = StringUtils.EMPTY;

        if ( ( sessionFields.getSelectedRecords(  ) != null ) && !sessionFields.getSelectedRecords(  ).isEmpty(  ) )
        {
            String strIdDirectory = request.getParameter( DirectoryUtils.PARAMETER_ID_DIRECTORY );
            UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + JSP_CONFIRM_ADD_ZIP_BASKET );
            url.addParameter( DirectoryUtils.PARAMETER_ID_DIRECTORY, strIdDirectory );

            for ( String strIdRecord : sessionFields.getSelectedRecords(  ) )
            {
                if ( StringUtils.isNotBlank( strIdRecord ) && StringUtils.isNumeric( strIdRecord ) )
                {
                    url.addParameter( DirectoryUtils.PARAMETER_ID_DIRECTORY_RECORD, strIdRecord );
                }
            }

            strRedirect = url.getUrl(  );
        }
        else
        {
            strRedirect = AdminMessageService.getMessageUrl( request, DirectoryUtils.MESSAGE_SELECT_RECORDS,
                    AdminMessage.TYPE_INFO );
        }

        result.setRedirect( strRedirect );

        return result;
    }
}
