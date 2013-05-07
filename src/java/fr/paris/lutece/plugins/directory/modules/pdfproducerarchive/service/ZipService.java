/*
 * Copyright (c) 2002-2013, Mairie de Paris
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
import fr.paris.lutece.plugins.archiveclient.service.util.ArchiveClientException;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.utils.PDFUtils;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.utils.FilesUtils;
import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.string.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;


/**
 * ZipUtils
 */
public final class ZipService
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
    * Constructor
    */
    private ZipService(  )
    {
    }

    /**
     * Method to generate zip
     * @param request request
     * @param strName directory name
     * @param nIdKeyUser id key user
     * @param nIdDirectory id directory
     * @param listIdEntryConfig config to build pdf
     * @param nIdRecord the id record
     * @return the archive id
     */
    public int doGeneratePDFAndZip( HttpServletRequest request, String strName, int nIdKeyUser, int nIdDirectory,
        List<Integer> listIdEntryConfig, int nIdRecord )
    {
        return doGeneratePDFAndZip( AdminUserService.getAdminUser( request ), AdminUserService.getLocale( request ),
                strName, nIdKeyUser, nIdDirectory, listIdEntryConfig, nIdRecord );
    }

    /**
     * Method to generate zip
     * @param adminUser The adminUser
     * @param locale The locale
     * @param strName directory name
     * @param nIdKeyUser id key user
     * @param nIdDirectory id directory
     * @param listIdEntryConfig config to build pdf
     * @param nIdRecord the id record
     * @return the archive id
     */
    public int doGeneratePDFAndZip( AdminUser adminUser, Locale locale, String strName, int nIdKeyUser,
            int nIdDirectory, List<Integer> listIdEntryConfig, int nIdRecord )
    {
        String strDirectoryName = strName;

        if ( StringUtils.isNotEmpty( strName ) )
        {
            strDirectoryName = StringUtil.replaceAccent( strName ).replaceAll( "\\W", "_" );
        }

        String strPathFilesGenerate = FilesUtils.builNamePathBasket( nIdKeyUser, nIdDirectory ) + File.separator +
            strDirectoryName;
        String strPathZipGenerate = FilesUtils.builNamePathBasket( nIdKeyUser, nIdDirectory ) + File.separator +
            AppPropertiesService.getProperty( PROPERTY_ZIP_NAME_REPOSITORY );

        FilesUtils.createTemporyZipDirectory( strPathFilesGenerate );
        FilesUtils.createTemporyZipDirectory( strPathZipGenerate );

        FileOutputStream os = null;

        try
        {
            os = new FileOutputStream( new File( strPathFilesGenerate + "/" + strDirectoryName + EXTENSION_FILE_PDF ) );
            PDFUtils.doCreateDocumentPDF( adminUser, locale, strDirectoryName, os, nIdRecord, listIdEntryConfig );
        }
        catch ( FileNotFoundException e )
        {
            AppLogService.error( e );
        }
        finally
        {
            IOUtils.closeQuietly( os );
        }

        FilesUtils.getAllFilesRecorded( adminUser, strPathFilesGenerate, listIdEntryConfig, nIdRecord );

        int nArchiveItemKey;

        try
        {
            nArchiveItemKey = _archiveClientService.generateArchive( strPathFilesGenerate, strPathZipGenerate,
                    strDirectoryName + EXTENSION_FILE_ZIP, ArchiveClientConstants.ARCHIVE_TYPE_ZIP );
        }
        catch ( Exception e )
        {
            AppLogService.error( e );

            return -1;
        }

        return nArchiveItemKey;
    }

    /**
     * Method to delete zip
     * @param strIdRecord id record
     * @param nArchiveItemKey id archive item
     * @param nIdKeyUser id user key
     * @param nIdDirectory id directory
     * @param zipName zip name
     * @return true if the zip is deleted otherwise false
     */
    public boolean doDeleteZip( String strIdRecord, int nArchiveItemKey, int nIdKeyUser, int nIdDirectory,
        String zipName )
    {
        String strArchiveStatus = _archiveClientService.informationArchive( nArchiveItemKey );

        if ( !ARCHIVE_STATE_USED.equals( strArchiveStatus ) )
        {
            String strPathFilesGenerate = FilesUtils.builNamePathBasket( nIdKeyUser, nIdDirectory ) + File.separator +
                zipName;

            if ( DirectoryUtils.convertStringToInt( strIdRecord ) != DirectoryUtils.CONSTANT_ID_NULL )
            {
                // Delete the temp folder in case it still exists
                FilesUtils.cleanTemporyZipDirectory( strPathFilesGenerate );
            }

            try
            {
                // Archive client must handle the deletion of the files, not directory-pdfproducer-archive
                _archiveClientService.removeArchive( nArchiveItemKey );
            }
            catch ( ArchiveClientException e )
            {
                AppLogService.error( e.getMessage(  ), e );

                return false;
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Method to do zip
     * @param strFolderToArchive path to the folder to archive
     * @param strArchiveDestination path to the destination folder which will store the archive
     * @param strArchiveName the name of the archive
     * @param strArchiveType the archive type(zip,..)
     * @return the archive id
     */
    public int doBasicZip( String strFolderToArchive, String strArchiveDestination, String strArchiveName,
        String strArchiveType )
    {
        return _archiveClientService.generateArchive( strFolderToArchive, strArchiveDestination, strArchiveName,
            strArchiveType );
    }

    /**
     * Method to get the zip status
     * @param nArchiveItemKey id of archive item
     * @return the zip status
     */
    public String getStatutZip( int nArchiveItemKey )
    {
        return _archiveClientService.informationArchive( nArchiveItemKey );
    }

    /**
     * Method to get the url zip
     * @param nArchiveItemKey id of archive item
     * @return the url zip
     */
    public String getUrlZip( int nArchiveItemKey )
    {
        return _archiveClientService.getDownloadUrl( nArchiveItemKey );
    }

    /**
    * Method to set _archiveClientService
    * @param archiveClientService archiveClientService
    */
    public void setArchiveClientService( IArchiveClientService archiveClientService )
    {
        _archiveClientService = archiveClientService;
    }
}
