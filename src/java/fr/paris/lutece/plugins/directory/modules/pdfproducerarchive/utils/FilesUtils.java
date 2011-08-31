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
package fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.utils;

import fr.paris.lutece.plugins.directory.business.Directory;
import fr.paris.lutece.plugins.directory.business.EntryFilter;
import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.business.PhysicalFile;
import fr.paris.lutece.plugins.directory.business.PhysicalFileHome;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.directory.business.RecordHome;
import fr.paris.lutece.plugins.directory.service.DirectoryPlugin;
import fr.paris.lutece.plugins.directory.service.DirectoryResourceIdService;
import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.List;

import javax.servlet.http.HttpServletRequest;


/**
 * FilesUtils
 *
 */
public final class FilesUtils
{
    private static final String PARAMETER_ID_DIRECTORY_RECORD = "id_directory_record";
    private static final String PROPERTY_PATH_FILES_GENERATED = "directory.zipbasket.root.path.repository.filesgenerated";

    /**
     * Constructor
     */
    private FilesUtils(  )
    {
    }

    /**
     * method to clean a specific repository
     * @param strTempDirectory name of repository
     */
    public static void cleanTemporyZipDirectory( String strTempDirectory )
    {
        File file = new File( strTempDirectory );

        if ( file.isDirectory(  ) )
        {
            File[] entries = file.listFiles(  );
            int sz = entries.length;

            for ( int j = 0; j < sz; j++ )
            {
                cleanTemporyZipDirectory( entries[j].getPath(  ) );
            }

            file.delete(  );
            file.deleteOnExit(  );
        }

        if ( file.isFile(  ) )
        {
            file.delete(  );
        }
    }

    /**
     * method to create a specific repository
     * @param strPath repository path
     */
    public static void createTemporyZipDirectory( String strPath )
    {
        File file = new File( strPath );

        if ( !file.isDirectory(  ) )
        {
            file.mkdirs(  );
        }
    }

    /**
     * Thie method get all recorded files
     * @param request request
     * @param strTempDirectoryExtract the temporary directory for extraction
     */
    public static void getAllFilesRecorded( HttpServletRequest request, String strTempDirectoryExtract )
    {
        Plugin plugin = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
        AdminUser adminUser = AdminUserService.getAdminUser( request );
        String strIdRecord = request.getParameter( PARAMETER_ID_DIRECTORY_RECORD );
        int nIdRecord = DirectoryUtils.convertStringToInt( strIdRecord );
        EntryFilter filter;

        Record record = RecordHome.findByPrimaryKey( nIdRecord, plugin );

        if ( ( record == null ) ||
                !RBACService.isAuthorized( Directory.RESOURCE_TYPE,
                    Integer.toString( record.getDirectory(  ).getIdDirectory(  ) ),
                    DirectoryResourceIdService.PERMISSION_VISUALISATION_RECORD, adminUser ) )
        {
            try
            {
                throw new AccessDeniedException(  );
            }
            catch ( AccessDeniedException e )
            {
                AppLogService.error( e );
            }
        }

        filter = new EntryFilter(  );
        filter.setIdDirectory( record.getDirectory(  ).getIdDirectory(  ) );
        filter.setIsGroup( EntryFilter.FILTER_TRUE );

        List<IEntry> listEntry = DirectoryUtils.getFormEntries( record.getDirectory(  ).getIdDirectory(  ), plugin,
                adminUser );

        for ( IEntry entry : listEntry )
        {
            if ( entry.getEntryType(  ).getGroup(  ) && ( entry.getChildren(  ) != null ) )
            {
                for ( IEntry child : entry.getChildren(  ) )
                {
                    doExtractFiles( strTempDirectoryExtract, plugin, nIdRecord, listEntry, child );
                }
            }
            else
            {
                doExtractFiles( strTempDirectoryExtract, plugin, nIdRecord, listEntry, entry );
            }
        }
    }

    /**
     * Thie method extracts the file to temp directory
     * @param strTempDirectoryExtract temp directory
     * @param plugin plugin
     * @param nIdRecord id record
     * @param listEntry list of entry
     * @param entry entry
     */
    private static void doExtractFiles( String strTempDirectoryExtract, Plugin plugin, int nIdRecord,
        List<IEntry> listEntry, IEntry entry )
    {
        if ( entry.isShownInExport(  ) )
        {
            for ( RecordField recordField : DirectoryUtils.getMapIdEntryListRecordField( listEntry, nIdRecord, plugin )
                                                          .get( String.valueOf( entry.getIdEntry(  ) ) ) )
            {
                String strTempPathExtract = strTempDirectoryExtract.concat( File.separator + entry.getTitle(  ) );

                if ( recordField.getFile(  ) != null )
                {
                    createTemporyZipDirectory( strTempPathExtract );
                    doExtracFile( plugin, recordField, strTempPathExtract );
                }

                if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeDownloadUrl &&
                        StringUtils.isNotBlank( recordField.getValue(  ) ) )
                {
                    createTemporyZipDirectory( strTempPathExtract );
                    doDownloadUrl( recordField.getValue(  ), strTempPathExtract );
                }
            }
        }
    }

    /**
     * This method extract a specific file
     * @param plugin plugin
     * @param recordField recordField
     * @param strTempDirectoryExtract the temporary directory for extraction
     */
    private static void doExtracFile( Plugin plugin, RecordField recordField, String strTempDirectoryExtract )
    {
        try
        {
            FileOutputStream out = new FileOutputStream( strTempDirectoryExtract + File.separator +
                    recordField.getFile(  ).getTitle(  ) );

            if ( recordField.getFile(  ) != null )
            {
                PhysicalFile physicalFile = PhysicalFileHome.findByPrimaryKey( recordField.getFile(  ).getPhysicalFile(  )
                                                                                          .getIdPhysicalFile(  ), plugin );

                if ( ( physicalFile != null ) && ( physicalFile.getValue(  ) != null ) )
                {
                    out.write( physicalFile.getValue(  ) );
                }
            }

            out.close(  );
        }
        catch ( FileNotFoundException e )
        {
            AppLogService.error( e );
        }
        catch ( IOException e )
        {
            AppLogService.error( e );
        }
    }

    /**
     * Download the file
     * @param strUrl url to download
     * @param strTempDirectoryExtract the file path
     */
    private static void doDownloadUrl( String strUrl, String strTempDirectoryExtract )
    {
        HttpAccess httpAccess = new HttpAccess(  );

        try
        {
            //String[] tab = strUrl.split("/");
            //String strFileName = tab[tab.length-1];
            String strFileName = httpAccess.getFileName( strUrl );
            httpAccess.downloadFile( strUrl, strTempDirectoryExtract + File.separator + strFileName );
        }
        catch ( HttpAccessException e )
        {
            AppLogService.error( e );
        }
    }

    /**
     * build path name to generate files
     * @param nIdAdminUser id admin user
     * @param nIdDirectory id directory
     * @return strPathBasket
     */
    public static String builNamePathBasket( int nIdAdminUser, int nIdDirectory )
    {
        String strRootPathFilesGenerate = AppPropertiesService.getProperty( PROPERTY_PATH_FILES_GENERATED );
        String strPathBasket = strRootPathFilesGenerate + "_" + String.valueOf( nIdAdminUser ) + "_" +
            String.valueOf( nIdDirectory );

        return strPathBasket;
    }
}
