package fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.service.daemon;

import fr.paris.lutece.plugins.directory.business.Directory;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.directory.business.RecordFieldFilter;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.business.producerconfig.DefaultConfigProducer;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.business.producerconfig.IConfigProducer;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.service.ConfigProducerService;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.utils.PDFUtils;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.service.DirectoryManageZipBasketService;
import fr.paris.lutece.portal.business.user.AdminUserHome;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.string.StringUtil;

import java.util.List;
import java.util.Locale;


public class AsyncAddZipBasketService implements Runnable
{
    private static final String DEFAULT_TYPE_FILE_NAME = "default";
    private static final String DIRECTORY_ENTRY_FILE_NAME = "directory_entry";

    private static final DirectoryManageZipBasketService _manageZipBasketService = SpringContextService
            .getBean( "directory-pdfproducer-archive.directoryManageZipBasketService" );
    private static final ConfigProducerService _manageConfigProducerService = SpringContextService
            .getBean( "directory-pdfproducer.manageConfigProducer" );

    private int _nIdAdminUser;
    private Plugin _plugin;
    private int _nIdDirectory;
    private String[] _listIdRecord;
    private Locale _locale;

    /**
     * Priovate constructor.
     */
    @SuppressWarnings( "unused" )
    private AsyncAddZipBasketService( )
    {

    }

    /**
     * Create a new initialized AsynchronousAddZipBasketService
     * @param request The request
     * @param nIdAdminUser The ID of the admin user
     * @param plugin The plugin
     * @param nIdDirectory The ID of the directory
     * @param nIdRecord The id of the record of the directory to add to the
     *            basket
     */
    public AsyncAddZipBasketService( Locale locale, int nIdAdminUser, Plugin plugin, int nIdDirectory,
            String[] listIdRecord )
    {
        _locale = locale;
        _nIdAdminUser = nIdAdminUser;
        _plugin = plugin;
        _nIdDirectory = nIdDirectory;
        _listIdRecord = listIdRecord;
    }

    /**
     * Add asynchronously a list of records to the basket of an admin user.
     * Parameters are passed throw the constructor of the class.
     */
    @Override
    public void run( )
    {
        if ( _listIdRecord == null || _listIdRecord.length == 0 || _nIdDirectory <= 0 || _nIdAdminUser <= 0 )
        {
            return;
        }

        // Fetch directory
        Directory directory = _manageZipBasketService.getDirectory( _nIdDirectory );

        // Fetch config
        int nIdConfig = _manageConfigProducerService.loadDefaultConfig( _plugin, _nIdDirectory );
        IConfigProducer configProducer = null;

        if ( ( nIdConfig == -1 ) || ( nIdConfig == 0 ) )
        {
            configProducer = new DefaultConfigProducer( );
        }
        else
        {
            configProducer = _manageConfigProducerService.loadConfig( _plugin, nIdConfig );
        }

        String strTypeConfigFileName = configProducer.getTypeConfigFileName( );

        for ( String strIdRecord : _listIdRecord )
        {
            int nIdRecord = Integer.parseInt( strIdRecord );
            String strName = null;

            // Build file name
            if ( DEFAULT_TYPE_FILE_NAME.equals( strTypeConfigFileName ) )
            {
                strName = PDFUtils.doPurgeNameFile( StringUtil.replaceAccent( directory.getTitle( ) )
                        .replace( " ", "_" ) + "_" + nIdRecord );
            }
            else if ( DIRECTORY_ENTRY_FILE_NAME.equals( strTypeConfigFileName ) )
            {
                RecordFieldFilter filter = new RecordFieldFilter( );
                filter.setIdRecord( nIdRecord );
                filter.setIdEntry( configProducer.getIdEntryFileName( ) );

                List<RecordField> listRecordField = _manageZipBasketService.getRecordFields( filter );

                for ( RecordField recordField : listRecordField )
                {
                    strName = PDFUtils.doPurgeNameFile( recordField.getEntry( ).convertRecordFieldValueToString(
                            recordField, _locale, false, false ) );
                }
            }
            else
            {
                strName = PDFUtils.doPurgeNameFile( configProducer.getTextFileName( ) + "_" + nIdRecord );
            }

            boolean bAllExportAlreadyExists = _manageZipBasketService.existsZipBasket( _nIdAdminUser, _plugin,
                    _nIdDirectory, -1 );

            if ( !bAllExportAlreadyExists )
            {
                boolean bZipAdded = _manageZipBasketService
                        .addZipBasket( AdminUserHome.findByPrimaryKey( _nIdAdminUser ), _locale, strName,
                                _nIdAdminUser, _plugin, _nIdDirectory, nIdRecord,
                                _manageConfigProducerService.loadListConfigEntry( _plugin, nIdConfig ) );

                if ( !bZipAdded )
                {
                    return;
                }
            }
            else
            {
                return;
            }
        }
    }
}
