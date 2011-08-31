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

import fr.paris.lutece.plugins.archiveclient.service.archive.IArchiveClientService;
import fr.paris.lutece.plugins.archiveclient.service.util.ArchiveClientConstants;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.utils.PDFUtils;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket.ZipBasket;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket.ZipBasketAction;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket.ZipBasketActionHome;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket.ZipBasketHome;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.utils.FilesUtils;
import fr.paris.lutece.plugins.directory.service.DirectoryPlugin;
import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.util.List;

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
    private IArchiveClientService _archiveClientService;

    /**
     * this method builds different repository to stock files and generate a PDF and a zip file of this repository
     * @param request request
     * @param strName the name of the futur zip
     * @param nIdAdminUser id of admin user
     * @param plugin plugin
     * @param nIdDirectory id of directory
     * @param nIdRecord id of record
     * @return true if the zip is correctly added in the database, false if the zip already exists
     */
    public boolean addZipBasket( HttpServletRequest request, String strName, int nIdAdminUser, Plugin plugin,
        int nIdDirectory, int nIdRecord )
    {
        if ( !ZipBasketHome.existsZipBasket( nIdAdminUser, plugin, nIdDirectory, nIdRecord ) )
        {
            String strPathFilesGenerate = FilesUtils.builNamePathBasket( nIdAdminUser, nIdDirectory ) + File.separator +
                strName;
            String strPathZipGenerate = FilesUtils.builNamePathBasket( nIdAdminUser, nIdDirectory ) + File.separator +
                AppPropertiesService.getProperty( PROPERTY_ZIP_NAME_REPOSITORY );

            FilesUtils.createTemporyZipDirectory( strPathFilesGenerate );
            FilesUtils.createTemporyZipDirectory( strPathZipGenerate );

            FileOutputStream os;

            try
            {
                os = new FileOutputStream( new File( strPathFilesGenerate + "/" + strName + EXTENSION_FILE_PDF ) );
                PDFUtils.doCreateDocumentPDF( request, strName, os );
            }
            catch ( FileNotFoundException e )
            {
                AppLogService.error( e );
            }

            FilesUtils.getAllFilesRecorded( request, strPathFilesGenerate );

            try
            {
                int nARchiveItemKey = _archiveClientService.generateArchive( strPathFilesGenerate, strPathZipGenerate,
                        strName + EXTENSION_FILE_ZIP, ArchiveClientConstants.ARCHIVE_TYPE_ZIP );
                ZipBasketHome.addZipBasket( strName, nIdAdminUser, plugin, nIdDirectory, nIdRecord, nARchiveItemKey );

                return true;
            }
            catch ( Exception e )
            {
                AppLogService.error( e );

                return false;
            }
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
     * This method delete a ZipBasket, its PDF and attachments after update status
     * @param plugin plugin
     * @param nIdZipBasket id of the zipbasket
     * @param strIdRecord id of record
     * @return true if the zipbasket can be deleted otherwise false
     */
    public boolean deleteZipBasket( Plugin plugin, int nIdZipBasket, String strIdRecord )
    {
        ZipBasket zipbasket = ZipBasketHome.loadZipBasket( plugin, nIdZipBasket );

        String strArchiveStatus = _archiveClientService.informationArchive( zipbasket.getArchiveItemKey(  ) );

        if ( !strArchiveStatus.equals( ARCHIVE_STATE_USED ) )
        {
            String strPathFilesGenerate = FilesUtils.builNamePathBasket( zipbasket.getIdAdminUser(  ),
                    zipbasket.getIdDirectory(  ) ) + File.separator + zipbasket.getZipName(  );
            String strPathZipGenerate = FilesUtils.builNamePathBasket( zipbasket.getIdAdminUser(  ),
                    zipbasket.getIdDirectory(  ) ) + File.separator +
                AppPropertiesService.getProperty( PROPERTY_ZIP_NAME_REPOSITORY );

            if ( !strIdRecord.equals( "-1" ) )
            {
                FilesUtils.cleanTemporyZipDirectory( strPathFilesGenerate );
                FilesUtils.cleanTemporyZipDirectory( strPathZipGenerate + File.separator + zipbasket.getZipName(  ) +
                    EXTENSION_FILE_ZIP );
            }
            else
            {
                FilesUtils.cleanTemporyZipDirectory( strPathFilesGenerate + File.separator + zipbasket.getZipName(  ) +
                    EXTENSION_FILE_ZIP );
            }

            _archiveClientService.removeArchive( zipbasket.getArchiveItemKey(  ) );
            ZipBasketHome.deleteZipBasket( plugin, nIdZipBasket );

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * This method delete multi ZipBasket
     * @param plugin plugin
     * @param listIdZipBasket list of id zipbasket
     */

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
     * this method change zip file status to in progress and modify the date
     * @param plugin plugin
     * @param nIdZipBasket id of the zipbasket
     */
    private static void changeZipBasketStatusToInProgress( Plugin plugin, int nIdZipBasket )
    {
        ZipBasketHome.changeZipBasketStatus( plugin, nIdZipBasket, DirectoryUtils.PARAMATER_STATUS_IN_PROGRESS );
    }

    /**
     * this method change zip file status to finished and modify the date
     * @param plugin plugin
     * @param nIdZipBasket id of the zipbasket
     * @param strUrl url
     */
    private static void changeZipBasketStatusToFinished( Plugin plugin, int nIdZipBasket, String strUrl )
    {
        ZipBasketHome.changeZipBasketStatus( plugin, nIdZipBasket, DirectoryUtils.PARAMATER_STATUS_FINISHED );
        ZipBasketHome.changeZipBasketUrl( plugin, nIdZipBasket, strUrl );
    }

    /**
     * this method change zip file status to failed and modify the date
     * @param plugin plugin
     * @param nIdZipBasket id of the zipbasket
     */
    private static void changeZipBasketStatusToFailed( Plugin plugin, int nIdZipBasket )
    {
        ZipBasketHome.changeZipBasketStatus( plugin, nIdZipBasket, DirectoryUtils.PARAMATER_STATUS_FAILED );
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
            if ( zipBasket.getZipStatus(  ).equals( DirectoryUtils.PARAMATER_STATUS_IN_PROGRESS ) ||
                    zipBasket.getZipStatus(  ).equals( DirectoryUtils.PARAMATER_STATUS_PENDING ) )
            {
                String strArchiveStatus = _archiveClientService.informationArchive( zipBasket.getArchiveItemKey(  ) );

                if ( strArchiveStatus.equals( ARCHIVE_STATE_USED ) )
                {
                    changeZipBasketStatusToInProgress( plugin, zipBasket.getIdZip(  ) );
                }

                if ( strArchiveStatus.equals( ARCHIVE_STATE_ERROR ) )
                {
                    changeZipBasketStatusToFailed( plugin, zipBasket.getIdZip(  ) );

                    String strPathFilesGenerate = FilesUtils.builNamePathBasket( zipBasket.getIdAdminUser(  ),
                            zipBasket.getIdDirectory(  ) ) + File.separator + zipBasket.getZipName(  );
                    FilesUtils.cleanTemporyZipDirectory( strPathFilesGenerate );
                }

                if ( strArchiveStatus.equals( ARCHIVE_STATE_FINAL ) )
                {
                    String strUrl = _archiveClientService.getDownloadUrl( zipBasket.getArchiveItemKey(  ) );

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

        int nARchiveItemKey = _archiveClientService.generateArchive( strPathZipGenerate + File.separator +
                AppPropertiesService.getProperty( PROPERTY_ZIP_NAME_REPOSITORY ), strPathZipGenerate,
                strName + EXTENSION_FILE_ZIP, ArchiveClientConstants.ARCHIVE_TYPE_ZIP );
        ZipBasketHome.addZipBasket( strName, nIdAdminUser, plugin, nIdDirectory, -1, nARchiveItemKey );
    }

    /**
     * get all actions for zipbasket by its status
     * @param nState status of zipbasket
     * @param plugin plugin
     * @return list of actions
     */
    public List<ZipBasketAction> selectActionsByZipBasketState( int nState, Plugin plugin )
    {
        return ZipBasketActionHome.selectActionsByZipBasketState( nState, plugin );
    }

    /**
     * Method to set _archiveClientService
     * @param archiveClientService archiveClientService
     */
    public void setArchiveClientService( IArchiveClientService archiveClientService )
    {
        this._archiveClientService = archiveClientService;
    }
}
