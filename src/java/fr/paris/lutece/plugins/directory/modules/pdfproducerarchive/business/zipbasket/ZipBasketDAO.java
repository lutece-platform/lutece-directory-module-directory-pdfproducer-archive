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
package fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket;

import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.utils.ConstantsStatusZip;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Allow to access ZipBasket data
 *
 */
public class ZipBasketDAO implements IZipBasketDAO
{
    private static final String SQL_QUERY_INSERT = "INSERT INTO directory_zip_basket (id_zip_basket,name,zip_state,id_user,id_directory,id_record,archive_item_key) VALUES ( ? , ? , ? , ? , ? , ? , ? );";
    private static final String SQL_QUERY_SELECT_MAX_ID = "SELECT max(id_zip_basket) FROM directory_zip_basket";
    private static final String SQL_QUERY_CHECK = "SELECT id_zip_basket, name, url, zip_state, id_user, id_directory, id_record, archive_item_key, date_creation FROM directory_zip_basket WHERE id_user = ? AND id_directory = ? AND id_record = ? ;";
    private static final String SQL_QUERY_SELECT_ALL = "SELECT id_zip_basket, name, url, zip_state, id_user, id_directory, id_record, archive_item_key, date_creation FROM directory_zip_basket;";
    private static final String SQL_QUERY_SELECT_ALL_BY_ADMIN_USER = "SELECT id_zip_basket, name, url, zip_state, id_user, id_directory, id_record, archive_item_key, date_creation FROM directory_zip_basket WHERE id_user = ? AND id_directory = ? ;";
    private static final String SQL_QUERY_DELETE_ELEMENT = "DELETE FROM directory_zip_basket WHERE id_zip_basket = ? ";
    private static final String SQL_QUERY_UPDATE_STATUS = "UPDATE directory_zip_basket SET zip_state = ? , date_creation = ? WHERE id_zip_basket = ? ";
    private static final String SQL_QUERY_UPDATE_URL = "UPDATE directory_zip_basket SET url = ? , date_creation = ? WHERE id_zip_basket = ? ";
    private static final String SQL_QUERY_SELECT = "SELECT id_zip_basket, name, url, zip_state, id_user, id_directory, id_record, archive_item_key, date_creation FROM directory_zip_basket WHERE id_zip_basket = ? ";
    private static final String SQL_QUERY_SELECT_BY_DATE = " SELECT id_zip_basket, name, url, zip_state, id_user, id_directory, id_record, archive_item_key, date_creation FROM directory_zip_basket WHERE date_creation <= ? ";

    /**
     * Gets a new primary key
     * @param plugin the plugin
     * @return the key
     */
    private int newPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_MAX_ID, plugin );
        daoUtil.executeQuery(  );

        int nIdZipBasket = 1;

        if ( daoUtil.next(  ) )
        {
            nIdZipBasket = daoUtil.getInt( 1 ) + 1;
        }

        daoUtil.free(  );

        return nIdZipBasket;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void addZipBasket( String strName, int nIdAdminUser, Plugin plugin, int nIdDirectory,
        int nIdRecord, int nArchiveItemKey )
    {
        int nIdZipBasket = newPrimaryKey( plugin );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        daoUtil.setInt( 1, nIdZipBasket );
        daoUtil.setString( 2, strName );
        daoUtil.setString( 3, ConstantsStatusZip.PARAMATER_STATUS_PENDING );
        daoUtil.setInt( 4, nIdAdminUser );
        daoUtil.setInt( 5, nIdDirectory );
        daoUtil.setInt( 6, nIdRecord );
        daoUtil.setInt( 7, nArchiveItemKey );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    public boolean existsZipBasket( int nIdAdminUser, Plugin plugin, int nIdDirectory, int nIdRecord )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_CHECK, plugin );
        daoUtil.setInt( 1, nIdAdminUser );
        daoUtil.setInt( 2, nIdDirectory );
        daoUtil.setInt( 3, nIdRecord );
        daoUtil.executeQuery(  );

        boolean bExistsZipBasket = daoUtil.next(  );
        daoUtil.free(  );

        return bExistsZipBasket;
    }

    /**
     * {@inheritDoc}
     */
    public List<ZipBasket> loadAllZipBasket( Plugin plugin )
    {
        List<ZipBasket> lisZipBasket = new ArrayList<ZipBasket>(  );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL, plugin );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            ZipBasket zipBasket = new ZipBasket(  );
            zipBasket.setIdZip( daoUtil.getInt( 1 ) );
            zipBasket.setZipName( daoUtil.getString( 2 ) );
            zipBasket.setZipUrl( daoUtil.getString( 3 ) );
            zipBasket.setZipStatus( daoUtil.getString( 4 ) );
            zipBasket.setIdAdminUser( daoUtil.getInt( 5 ) );
            zipBasket.setIdDirectory( daoUtil.getInt( 6 ) );
            zipBasket.setIdRecord( daoUtil.getInt( 7 ) );
            zipBasket.setArchiveItemKey( daoUtil.getInt( 8 ) );
            zipBasket.setDateZipAdded( daoUtil.getTimestamp( 9 ) );

            if ( zipBasket != null )
            {
                lisZipBasket.add( zipBasket );
            }
        }

        daoUtil.free(  );

        return lisZipBasket;
    }

    /**
     * {@inheritDoc}
     */
    public List<ZipBasket> loadAllZipBasketByAdminUser( Plugin plugin, int nIdAdminUser, int nIdDirectory )
    {
        List<ZipBasket> lisZipBasket = new ArrayList<ZipBasket>(  );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_BY_ADMIN_USER, plugin );
        daoUtil.setInt( 1, nIdAdminUser );
        daoUtil.setInt( 2, nIdDirectory );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            ZipBasket zipBasket = new ZipBasket(  );
            zipBasket.setIdZip( daoUtil.getInt( 1 ) );
            zipBasket.setZipName( daoUtil.getString( 2 ) );
            zipBasket.setZipUrl( daoUtil.getString( 3 ) );
            zipBasket.setZipStatus( daoUtil.getString( 4 ) );
            zipBasket.setIdAdminUser( daoUtil.getInt( 5 ) );
            zipBasket.setIdDirectory( daoUtil.getInt( 6 ) );
            zipBasket.setIdRecord( daoUtil.getInt( 7 ) );
            zipBasket.setArchiveItemKey( daoUtil.getInt( 8 ) );
            zipBasket.setDateZipAdded( daoUtil.getTimestamp( 9 ) );

            if ( zipBasket != null )
            {
                lisZipBasket.add( zipBasket );
            }
        }

        daoUtil.free(  );

        return lisZipBasket;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteZipBasket( Plugin plugin, int nIdZipBasket )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_ELEMENT, plugin );
        daoUtil.setInt( 1, nIdZipBasket );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    public void deleteMultiZipBasket( Plugin plugin, List<Integer> listIdZipBasket )
    {
        for ( Integer idZipBasket : listIdZipBasket )
        {
            DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_ELEMENT, plugin );
            daoUtil.setInt( 1, idZipBasket.intValue(  ) );
            daoUtil.executeUpdate(  );
            daoUtil.free(  );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void changeZipBasketStatus( Plugin plugin, int nIdZipBasket, String strStatus )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_STATUS, plugin );
        daoUtil.setString( 1, strStatus );
        daoUtil.setTimestamp( 2, new Timestamp( GregorianCalendar.getInstance(  ).getTimeInMillis(  ) ) );
        daoUtil.setInt( 3, nIdZipBasket );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    public void changeZipBasketUrl( Plugin plugin, int nIdZipBasket, String strUrl )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_URL, plugin );
        daoUtil.setString( 1, strUrl );
        daoUtil.setTimestamp( 2, new Timestamp( GregorianCalendar.getInstance(  ).getTimeInMillis(  ) ) );
        daoUtil.setInt( 3, nIdZipBasket );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    public ZipBasket loadZipBasket( Plugin plugin, int nIdZipBasket )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );
        daoUtil.setInt( 1, nIdZipBasket );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            ZipBasket zipBasket = new ZipBasket(  );
            zipBasket.setIdZip( daoUtil.getInt( 1 ) );
            zipBasket.setZipName( daoUtil.getString( 2 ) );
            zipBasket.setZipUrl( daoUtil.getString( 3 ) );
            zipBasket.setZipStatus( daoUtil.getString( 4 ) );
            zipBasket.setIdAdminUser( daoUtil.getInt( 5 ) );
            zipBasket.setIdDirectory( daoUtil.getInt( 6 ) );
            zipBasket.setIdRecord( daoUtil.getInt( 7 ) );
            zipBasket.setArchiveItemKey( daoUtil.getInt( 8 ) );
            zipBasket.setDateZipAdded( daoUtil.getTimestamp( 9 ) );
            daoUtil.free(  );

            return zipBasket;
        }

        daoUtil.free(  );

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<ZipBasket> loadZipBasketByDate( Plugin plugin, Date dateExpiry )
    {
        List<ZipBasket> lisZipBasket = new ArrayList<ZipBasket>(  );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_DATE, plugin );
        daoUtil.setDate( 1, new java.sql.Date( dateExpiry.getTime(  ) ) );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            int nIndex = 1;
            ZipBasket zipBasket = new ZipBasket(  );
            zipBasket.setIdZip( daoUtil.getInt( nIndex++ ) );
            zipBasket.setZipName( daoUtil.getString( nIndex++ ) );
            zipBasket.setZipUrl( daoUtil.getString( nIndex++ ) );
            zipBasket.setZipStatus( daoUtil.getString( nIndex++ ) );
            zipBasket.setIdAdminUser( daoUtil.getInt( nIndex++ ) );
            zipBasket.setIdDirectory( daoUtil.getInt( nIndex++ ) );
            zipBasket.setIdRecord( daoUtil.getInt( nIndex++ ) );
            zipBasket.setArchiveItemKey( daoUtil.getInt( nIndex++ ) );
            zipBasket.setDateZipAdded( daoUtil.getTimestamp( nIndex++ ) );

            lisZipBasket.add( zipBasket );
        }

        daoUtil.free(  );

        return lisZipBasket;
    }
}
