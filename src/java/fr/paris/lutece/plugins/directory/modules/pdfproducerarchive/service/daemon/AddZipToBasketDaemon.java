package fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.service.daemon;

import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket.queue.ZipItem;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.service.DirectoryPDFProducerArchivePlugin;
import fr.paris.lutece.portal.service.daemon.Daemon;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;


public class AddZipToBasketDaemon extends Daemon
{
    private static final String PROPERTY_MAX_NUMBER_THREAD = "daemon.addZipToBacket.maxNumberOfThread";

    private static Deque<ZipItem> _stackItems = new ArrayDeque<ZipItem>( );
    private static Plugin _plugin = PluginService.getPlugin( DirectoryPDFProducerArchivePlugin.PLUGIN_NAME );
    private static Map<Integer, Thread> _mapThreadByUserId = new HashMap<Integer, Thread>( );

    /**
     * {@inheritDoc}
     */
    @Override
    public void run( )
    {
        int nMaxNumberThread = AppPropertiesService.getPropertyInt( PROPERTY_MAX_NUMBER_THREAD, 5 );
        ZipItem item = null;
        while ( ( item = popItemFromQueue( ) ) != null )
        {
            Thread thread = _mapThreadByUserId.get( item.getIdAdminUser( ) );
            AsyncAddZipBasketService asyncAddZipBasketService = new AsyncAddZipBasketService( item.getLocale( ),
                    item.getIdAdminUser( ), _plugin, item.getIdDirectory( ), item.getListIdRecord( ) );
            if ( thread != null )
            {
                if ( thread.isAlive( ) )
                {
                    // The thread is already running. We replace the item in the queue.
                    addItemToQueue( item );
                    
                    // If the item we just used is the only item in the queue
                    if ( countItemsInQueue( ) <= 1 )
                    {
                        setLastRunLogs( "No more zip to add to basket." );
                        return;
                    }
                    
                    // We now check if the daemon can start another thread
                    if ( _mapThreadByUserId.size( ) >= nMaxNumberThread )
                    {
                        // We check if every threads are running
                        boolean bStopedThreadFound = false;
                        for ( Thread otherThread : _mapThreadByUserId.values( ) )
                        {
                            if ( !otherThread.isAlive( ) )
                            {
                                _mapThreadByUserId.remove( otherThread );
                                bStopedThreadFound = true;
                            }
                        }
                        // If every thread is running, and the maximum number of threads has been reached, we stop the daemon
                        if ( !bStopedThreadFound )
                        {
                            setLastRunLogs( "Every threads are running. Daemon execution ending." );
                            return;
                        }
                    }
                }
                else
                {
                    _mapThreadByUserId.remove( thread );
                    thread = new Thread( asyncAddZipBasketService );
                    thread.start( );
                    _mapThreadByUserId.put( item.getIdAdminUser( ), thread );
                }
            }
            else
            {
                // We check if we can launch a new thread
                if ( _mapThreadByUserId.size( ) < nMaxNumberThread )
                {
                    thread = new Thread( asyncAddZipBasketService );
                    thread.start( );
                    _mapThreadByUserId.put( item.getIdAdminUser( ), thread );
                }
                else
                {
                    // We check if a thread has stoped
                    boolean bStopedThreadFound = false;
                    for ( Thread otherThread : _mapThreadByUserId.values( ) )
                    {
                        if ( !otherThread.isAlive( ) )
                        {
                            _mapThreadByUserId.remove( otherThread );
                            bStopedThreadFound = true;
                            break;
                        }
                    }
                    // If we found a dead thread and removed it from the map, we can create a new thread for this user
                    if ( bStopedThreadFound )
                    {
                        thread = new Thread( asyncAddZipBasketService );
                        thread.start( );
                        _mapThreadByUserId.put( item.getIdAdminUser( ), thread );
                    }
                    else
                    {
                        // We can not create a new thread. The item has to wait until a thread end.
                        addItemToQueue( item );
                        // We stop the execution of the Daemon because all threads are up, so no other one can be created. The Daemon can do nothing more until at last one thread stop.
                        setLastRunLogs( "Every threads are running. Daemon execution ending." );
                        return;
                    }
                }
            }
        }
        setLastRunLogs( "There is no more zip to add to basket." );
    }

    public synchronized static void addItemToQueue( ZipItem zipItem )
    {
        for ( ZipItem item : _stackItems )
        {
            if ( item.getIdAdminUser( ) == zipItem.getIdAdminUser( )
                    && item.getIdDirectory( ) == zipItem.getIdDirectory( ) )
            {
                String[] listIdRecord = item.getListIdRecord( );
                for ( String strIdRecord : zipItem.getListIdRecord( ) )
                {
                    boolean bContain = false;
                    for ( String strOtherIdRecord : listIdRecord )
                    {
                        if ( StringUtils.equals( strOtherIdRecord, strIdRecord ) )
                        {
                            bContain = true;
                            break;
                        }
                    }
                    if ( !bContain )
                    {
                        ArrayUtils.add( listIdRecord, strIdRecord );
                    }
                }
                item.setListIdRecord( listIdRecord );
                return;
            }
        }
        _stackItems.addLast( zipItem );
    }

    public synchronized static ZipItem popItemFromQueue( )
    {
        if ( _stackItems.size( ) == 0 )
        {
            return null;
        }
        else
        {
            ZipItem item = _stackItems.pop( );
            return item;
        }
    }

    public synchronized static Integer countItemsInQueue( )
    {
        return _stackItems.size( );
    }

}
