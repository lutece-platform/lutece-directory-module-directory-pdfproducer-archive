package fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.archiveclient.service.archive.IArchiveClientService;
import fr.paris.lutece.plugins.archiveclient.service.util.ArchiveClientConstants;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.utils.PDFUtils;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.utils.FilesUtils;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

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
    private static IArchiveClientService _archiveClientService;
    
	/**
     * Constructor
     */
    private ZipService(  )
    {
    }
    
    /** 
     * Method to generate zip without config
	 * @param request request
	 * @param strName directory name
	 * @param nIdKeyUser id key user
	 * @param nIdDirectory id directory
	 * @return the archive id
	 */
	public int doGeneratePDFAndZipWithoutConfig(HttpServletRequest request, String strName,
			int nIdKeyUser, int nIdDirectory ) 
	{
		List<Integer> listIdEntryConfig = new ArrayList<Integer>();
		return doGeneratePDFAndZip( request, strName, nIdKeyUser,  nIdDirectory , listIdEntryConfig ) ;
	}
	
    /** 
     * Method to generate zip
	 * @param request request
	 * @param strName directory name
	 * @param nIdKeyUser id key user
	 * @param nIdDirectory id directory
	 * @param listIdEntryConfig config to build pdf
	 * @return the archive id
	 */
	public static int doGeneratePDFAndZip(HttpServletRequest request, String strName,
			int nIdKeyUser, int nIdDirectory , List<Integer> listIdEntryConfig  ) 
	{
		String strPathFilesGenerate = FilesUtils.builNamePathBasket( nIdKeyUser, nIdDirectory ) + File.separator +
		    strName;
		String strPathZipGenerate = FilesUtils.builNamePathBasket( nIdKeyUser, nIdDirectory ) + File.separator +
		    AppPropertiesService.getProperty( PROPERTY_ZIP_NAME_REPOSITORY );

		FilesUtils.createTemporyZipDirectory( strPathFilesGenerate );
		FilesUtils.createTemporyZipDirectory( strPathZipGenerate );

		FileOutputStream os;

		try
		{
		    os = new FileOutputStream( new File( strPathFilesGenerate + "/" + strName + EXTENSION_FILE_PDF ) );
		    PDFUtils.doCreateDocumentPDF( request, strName, os , listIdEntryConfig );
		}
		catch ( FileNotFoundException e )
		{
		    AppLogService.error( e );
		}

		FilesUtils.getAllFilesRecorded( request, strPathFilesGenerate , listIdEntryConfig );

		int nARchiveItemKey;
		try
		{
		    nARchiveItemKey = _archiveClientService.generateArchive( strPathFilesGenerate, strPathZipGenerate,
		            strName + EXTENSION_FILE_ZIP, ArchiveClientConstants.ARCHIVE_TYPE_ZIP );
		}
		catch ( Exception e )
		{
		    AppLogService.error( e );

		    return -1;
		}
		return nARchiveItemKey;
	}
	
	/**
	 * Method to delete zip
	 * @param strIdRecord id record
	 * @param archiveItemKey id archive item
	 * @param nIdKeyUser id user key 
	 * @param idDirectory id directory
	 * @param zipName zip name
	 * @return true if the zip is deleted otherwise false
	 */
	public boolean doDeleteZip(String strIdRecord, int nArchiveItemKey,
			int nIdKeyUser, int nIdDirectory, String zipName) {
		String strArchiveStatus = _archiveClientService.informationArchive( nArchiveItemKey );

        if ( !strArchiveStatus.equals( ARCHIVE_STATE_USED ) )
        {
            String strPathFilesGenerate = FilesUtils.builNamePathBasket( nIdKeyUser ,
            		nIdDirectory ) + File.separator + zipName;
            String strPathZipGenerate = FilesUtils.builNamePathBasket( nIdKeyUser ,
            		nIdDirectory ) + File.separator +
                AppPropertiesService.getProperty( PROPERTY_ZIP_NAME_REPOSITORY );

            if ( !strIdRecord.equals( "-1" ) )
            {
                FilesUtils.cleanTemporyZipDirectory( strPathFilesGenerate );
                FilesUtils.cleanTemporyZipDirectory( strPathZipGenerate + File.separator + zipName +
                    EXTENSION_FILE_ZIP );
            }
            else
            {
                FilesUtils.cleanTemporyZipDirectory( strPathFilesGenerate + File.separator + zipName +
                    EXTENSION_FILE_ZIP );
            }

            _archiveClientService.removeArchive( nArchiveItemKey );

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
	 * @paramstrArchiveDestination path to the destination folder which will store the archive
	 * @paramstrArchiveName the name of the archive
	 * @paramstrArchiveType the archive type(zip,..)
	 * @return the archive id
	 */
	public int doBasicZip( String strFolderToArchive, String strArchiveDestination, String strArchiveName, String strArchiveType )
	{
		return _archiveClientService.generateArchive(strFolderToArchive, strArchiveDestination, strArchiveName, strArchiveType) ;
	}	
	
	/**
	 * Method to get the zip status 
	 * @param nArchiveItemKey id of archive item
	 * @return the zip status
	 */
	public String getStatutZip( int nArchiveItemKey )
	{
		return _archiveClientService.informationArchive( nArchiveItemKey ) ;
	}
	
	/**
	 * Method to get the url zip
	 * @param nArchiveItemKey id of archive item
	 * @return the url zip
	 */
	public String getUrlZip( int nArchiveItemKey )
	{
		return _archiveClientService.getDownloadUrl( nArchiveItemKey ) ;
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
