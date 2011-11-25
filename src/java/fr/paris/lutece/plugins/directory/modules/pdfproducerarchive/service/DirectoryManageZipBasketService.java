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
package fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.service;

import fr.paris.lutece.plugins.archiveclient.service.util.ArchiveClientConstants;
import fr.paris.lutece.plugins.directory.business.Directory;
import fr.paris.lutece.plugins.directory.business.DirectoryHome;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.directory.business.RecordFieldFilter;
import fr.paris.lutece.plugins.directory.business.RecordFieldHome;
import fr.paris.lutece.plugins.directory.business.RecordHome;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.service.DirectoryPDFProducerPlugin;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket.ZipBasket;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket.ZipBasketAction;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket.ZipBasketActionHome;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket.ZipBasketHome;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.utils.ConstantsStatusZip;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.utils.FilesUtils;
import fr.paris.lutece.plugins.directory.service.DirectoryPlugin;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import org.apache.commons.lang.StringUtils;

import java.io.File;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;


/**
 * Service to manage zipbasket *
 */
public class DirectoryManageZipBasketService
{
    public static final String ARCHIVE_STATE_INITIAL = "INIT";
    public static final String ARCHIVE_STATE_USED = "USED";
    public static final String ARCHIVE_STATE_ERROR = "ERROR";
    public static final String ARCHIVE_STATE_FINAL = "FINAL";
    public static final String PROPERTY_ZIP_NAME_REPOSITORY = "directory.zipbasket.name_zip_repository";
    public static final String EXTENSION_FILE_ZIP = ".zip";
    public static final String EXTENSION_FILE_PDF = ".pdf";

    /**
     * this method builds different repository to stock files and generate a PDF and a zip file of this repository
     * @param request request
     * @param strName the name of the futur zip
     * @param nIdAdminUser id of admin user
     * @param plugin plugin
     * @param nIdDirectory id of directory
     * @param nIdRecord id of record
     * @param listIdConfig list of id config
     * @return true if the zip is correctly added in the database, false if the zip already exists
     */
    public boolean addZipBasket( HttpServletRequest request, String strName, int nIdAdminUser, Plugin plugin,
        int nIdDirectory, int nIdRecord, List<Integer> listIdConfig )
    {
        if ( !ZipBasketHome.existsZipBasket( nIdAdminUser, plugin, nIdDirectory, nIdRecord ) )
        {
            int nArchiveItemKey = getZipService(  )
                                      .doGeneratePDFAndZip( request, strName, nIdAdminUser, nIdDirectory, listIdConfig,
                    nIdRecord );

            if ( nArchiveItemKey == -1 )
            {
                return false;
            }

            ZipBasketHome.addZipBasket( strName, nIdAdminUser, plugin, nIdDirectory, nIdRecord, nArchiveItemKey );

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
    * This method load all element in basket by id admin user for a specific directory
    * @param plugin plugin
    * @param nIdAdminUser id of admin user
    * @param nIdDirectory id of directory
    * @return list of ZipBasket
    */
    public List<ZipBasket> loadAllZipBasketByAdminUser( Plugin plugin, int nIdAdminUser, int nIdDirectory )
    {
        return ZipBasketHome.loadAllZipBasketByAdminUser( plugin, nIdAdminUser, nIdDirectory );
    }

    /**
     * Find zip basket by date
     * @param plugin the plugin
     * @param dateExpiry the date expiry
     * @return a list of {@link ZipBasket}
     */
    public List<ZipBasket> loadZipBasketByDate( Plugin plugin, Date dateExpiry )
    {
        return ZipBasketHome.loadZipBasketByDate( plugin, dateExpiry );
    }

    /**
     * This method delete a ZipBasket, its PDF and attachments after update status
     * @param plugin plugin
     * @param nIdZipBasket id of the zipbasket
     * @param strIdRecord id of record
     * @return true if the zipbasket can be deleted otherwise false
     */
    public boolean deleteZipBasket( Plugin plugin, int nIdZipBasket, String strIdRecord )
    {
        updateZipBasketStatus(  );

        ZipBasket zipbasket = ZipBasketHome.loadZipBasket( plugin, nIdZipBasket );
        int nArchiveItemKey = zipbasket.getArchiveItemKey(  );
        int nIdAdminUser = zipbasket.getIdAdminUser(  );
        int nIdDirectory = zipbasket.getIdDirectory(  );
        String zipName = zipbasket.getZipName(  );

        if ( getZipService(  ).doDeleteZip( strIdRecord, nArchiveItemKey, nIdAdminUser, nIdDirectory, zipName ) )
        {
            ZipBasketHome.deleteZipBasket( plugin, nIdZipBasket );

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Delete all zip
     * @param plugin the plugin
     * @param nIdDirectory the id directory
     * @param nIdAdminUser the id admin user
     * @return true if the zip basket can be deleted, false otherwise
     */
    public boolean deleteAllZipBasket( Plugin plugin, int nIdDirectory, int nIdAdminUser )
    {
        updateZipBasketStatus(  );

        for ( ZipBasket zipBasket : loadAllZipBasketByAdminUser( plugin, nIdAdminUser, nIdDirectory ) )
        {
            if ( getZipService(  )
                         .doDeleteZip( Integer.toString( zipBasket.getIdRecord(  ) ), zipBasket.getArchiveItemKey(  ),
                        nIdAdminUser, nIdDirectory, zipBasket.getZipName(  ) ) )
            {
                ZipBasketHome.deleteZipBasket( plugin, zipBasket.getIdZip(  ) );
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    /**
     * this method delete several zip basket
     * @param plugin plugin
     * @param listIdZipBasket list of id zip basket
     */
    public void deleteMultiZipBasket( Plugin plugin, List<Integer> listIdZipBasket )
    {
        ZipBasketHome.deleteMultiZipBasket( plugin, listIdZipBasket );
    }

    /**
     * Get the directory from a given id directory
     * @param nIdDirectory the id directory
     * @return the directory
     */
    public Directory getDirectory( int nIdDirectory )
    {
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

        return DirectoryHome.findByPrimaryKey( nIdDirectory, pluginDirectory );
    }

    /**
     * Get the record from a given id record
     * @param nIdRecord the id record
     * @return the record
     */
    public Record getRecord( int nIdRecord )
    {
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

        return RecordHome.findByPrimaryKey( nIdRecord, pluginDirectory );
    }

    /**
     * Get the list of record fields by a filter
     * @param filter the filter
     * @return a list of record fields
     */
    public List<RecordField> getRecordFields( RecordFieldFilter filter )
    {
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

        return RecordFieldHome.getRecordFieldList( filter, pluginDirectory );
    }

    /**
     * this method change zip file status to in progress and modify the date
     * @param plugin plugin
     * @param nIdZipBasket id of the zipbasket
     */
    private static void changeZipBasketStatusToInProgress( Plugin plugin, int nIdZipBasket )
    {
        ZipBasketHome.changeZipBasketStatus( plugin, nIdZipBasket, ConstantsStatusZip.PARAMATER_STATUS_IN_PROGRESS );
    }

    /**
     * this method change zip file status to finished and modify the date
     * @param plugin plugin
     * @param nIdZipBasket id of the zipbasket
     * @param strUrl url
     */
    private static void changeZipBasketStatusToFinished( Plugin plugin, int nIdZipBasket, String strUrl )
    {
        ZipBasketHome.changeZipBasketStatus( plugin, nIdZipBasket, ConstantsStatusZip.PARAMATER_STATUS_FINISHED );
        ZipBasketHome.changeZipBasketUrl( plugin, nIdZipBasket, strUrl );
    }

    /**
     * this method change zip file status to failed and modify the date
     * @param plugin plugin
     * @param nIdZipBasket id of the zipbasket
     */
    private static void changeZipBasketStatusToFailed( Plugin plugin, int nIdZipBasket )
    {
        ZipBasketHome.changeZipBasketStatus( plugin, nIdZipBasket, ConstantsStatusZip.PARAMATER_STATUS_FAILED );
    }

    /**
     * modify the status of zipbasket according the information given by archiveClientService
     */
    public void updateZipBasketStatus(  )
    {
        Plugin plugin = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
        List<ZipBasket> listZipBasket = ZipBasketHome.loadAllZipBasket( plugin );

        for ( ZipBasket zipBasket : listZipBasket )
        {
            if ( ConstantsStatusZip.PARAMATER_STATUS_IN_PROGRESS.equals( zipBasket.getZipStatus(  ) ) ||
                    ConstantsStatusZip.PARAMATER_STATUS_PENDING.equals( zipBasket.getZipStatus(  ) ) )
            {
                String strArchiveStatus = getZipService(  ).getStatutZip( zipBasket.getArchiveItemKey(  ) );

                if ( ARCHIVE_STATE_USED.equals( strArchiveStatus ) )
                {
                    changeZipBasketStatusToInProgress( plugin, zipBasket.getIdZip(  ) );
                }

                if ( ARCHIVE_STATE_ERROR.equals( strArchiveStatus ) )
                {
                    changeZipBasketStatusToFailed( plugin, zipBasket.getIdZip(  ) );

                    String strPathFilesGenerate = FilesUtils.builNamePathBasket( zipBasket.getIdAdminUser(  ),
                            zipBasket.getIdDirectory(  ) ) + File.separator + zipBasket.getZipName(  );
                    FilesUtils.cleanTemporyZipDirectory( strPathFilesGenerate );
                }

                if ( ARCHIVE_STATE_FINAL.equals( strArchiveStatus ) )
                {
                    String strUrl = getZipService(  ).getUrlZip( zipBasket.getArchiveItemKey(  ) );

                    if ( StringUtils.isNotBlank( strUrl ) )
                    {
                        changeZipBasketStatusToFinished( plugin, zipBasket.getIdZip(  ), strUrl );

                        String strPathFilesGenerate = FilesUtils.builNamePathBasket( zipBasket.getIdAdminUser(  ),
                                zipBasket.getIdDirectory(  ) ) + File.separator + zipBasket.getZipName(  );
                        FilesUtils.cleanTemporyZipDirectory( strPathFilesGenerate );
                    }
                    else
                    {
                        changeZipBasketStatusToFailed( plugin, zipBasket.getIdZip(  ) );
                    }
                }
            }
        }
    }

    /**
     * generate a zip file of all of zipbasket
     * @param strName name of zip
     * @param nIdAdminUser id of admin user
     * @param plugin plugin
     * @param nIdDirectory if of directory
     */
    public void exportAllZipFile( String strName, int nIdAdminUser, Plugin plugin, int nIdDirectory )
    {
        String strPathZipGenerate = FilesUtils.builNamePathBasket( nIdAdminUser, nIdDirectory );

        int nARchiveItemKey = getZipService(  )
                                  .doBasicZip( strPathZipGenerate + File.separator +
                AppPropertiesService.getProperty( PROPERTY_ZIP_NAME_REPOSITORY ), strPathZipGenerate,
                strName + EXTENSION_FILE_ZIP, ArchiveClientConstants.ARCHIVE_TYPE_ZIP );
        ZipBasketHome.addZipBasket( strName, nIdAdminUser, plugin, nIdDirectory, -1, nARchiveItemKey );
    }

    /**
     * get all actions for zipbasket by its status
     * @param nState status of zipbasket
     * @param locale the locale
     * @param directory the directory
     * @param user the user
     * @param plugin plugin
     * @return list of actions
     */
    public List<ZipBasketAction> selectActionsByZipBasketState( int nState, Locale locale, Directory directory,
        AdminUser user, Plugin plugin )
    {
        List<ZipBasketAction> listActions = ZipBasketActionHome.selectActionsByZipBasketState( nState, locale, plugin );

        return (List<ZipBasketAction>) RBACService.getAuthorizedActionsCollection( listActions, directory, user );
    }

    /**
     * This SQL method check if the zip is already exists
     * @param nIdAdminUser id of admin user
     * @param plugin plugin
     * @param nIdDirectory id directory
     * @param nIdRecord id ercord
     * @return true if the zip already exists
     */
    public boolean existsZipBasket( int nIdAdminUser, Plugin plugin, int nIdDirectory, int nIdRecord )
    {
        return ZipBasketHome.existsZipBasket( nIdAdminUser, plugin, nIdDirectory, nIdRecord );
    }

    /**
     * Method to get ZipService
     * @return ZipService
     */
    private static ZipService getZipService(  )
    {
        return (ZipService) SpringContextService.getPluginBean( DirectoryPDFProducerPlugin.PLUGIN_NAME,
            "directory-pdfproducer-archive.zipUtils" );
    }
}
