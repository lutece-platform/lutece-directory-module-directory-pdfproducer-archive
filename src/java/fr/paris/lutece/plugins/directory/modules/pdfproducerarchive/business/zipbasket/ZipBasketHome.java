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

import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.service.DirectoryPDFProducerArchivePlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.Date;
import java.util.List;


/**
 *        ZipBasketHome
 */
public final class ZipBasketHome
{
    private static IZipBasketDAO _dao = (IZipBasketDAO) SpringContextService.getPluginBean( DirectoryPDFProducerArchivePlugin.PLUGIN_NAME,
            "directoryZipBasketDAO" );

    /**
     * Constructor
     */
    private ZipBasketHome(  )
    {
    }

    /**
     * This method add a new zip in the basket
     * @param strName the name of the futur zip
     * @param nIdAdminUser id of admin user
     * @param plugin plugin
     * @param nIdDirectory id of directory
     * @param nIdRecord id of record
     * @param nArchiveItemKey id of the archive item
     */
    public static void addZipBasket( String strName, int nIdAdminUser, Plugin plugin, int nIdDirectory, int nIdRecord,
        int nArchiveItemKey )
    {
        _dao.addZipBasket( strName, nIdAdminUser, plugin, nIdDirectory, nIdRecord, nArchiveItemKey );
    }

    /**
     * This SQL method check if the zip is already exists
     * @param nIdAdminUser id of admin user
     * @param plugin plugin
     * @param nIdDirectory id directory
     * @param nIdRecord id ercord
     * @return true if the zip already exists
     */
    public static boolean existsZipBasket( int nIdAdminUser, Plugin plugin, int nIdDirectory, int nIdRecord )
    {
        return _dao.existsZipBasket( nIdAdminUser, plugin, nIdDirectory, nIdRecord );
    }

    /**
     * This method load all zipbasket
     * @param plugin plugin
     * @return list of ZipBasket
     */
    public static List<ZipBasket> loadAllZipBasket( Plugin plugin )
    {
        return _dao.loadAllZipBasket( plugin );
    }

    /**
     * This method load all element in basket by id admin user for a specific directory
     * @param plugin plugin
     * @param nIdAdminUser id of admin user
     * @param nIdDirectory id of directory
     * @return list of ZipBasket
     */
    public static List<ZipBasket> loadAllZipBasketByAdminUser( Plugin plugin, int nIdAdminUser, int nIdDirectory )
    {
        return _dao.loadAllZipBasketByAdminUser( plugin, nIdAdminUser, nIdDirectory );
    }

    /**
     * This method delete a ZipBasket
     * @param plugin plugin
     * @param nIdZipBasket id of the zipbasket
     */
    public static void deleteZipBasket( Plugin plugin, int nIdZipBasket )
    {
        _dao.deleteZipBasket( plugin, nIdZipBasket );
    }

    /**
     * This method delete multi ZipBasket
     * @param plugin plugin
     * @param listIdZipBasket list of id zipbasket
     */
    public static void deleteMultiZipBasket( Plugin plugin, List<Integer> listIdZipBasket )
    {
        _dao.deleteMultiZipBasket( plugin, listIdZipBasket );
    }

    /**
     * this method change de zip file status and modify the date
     * @param plugin plugin
     * @param nIdZipBasket id of the zipbasket
     * @param strStatus the new status
     */
    public static void changeZipBasketStatus( Plugin plugin, int nIdZipBasket, String strStatus )
    {
        _dao.changeZipBasketStatus( plugin, nIdZipBasket, strStatus );
    }

    /**
     * this method change de zip file url and modify the date
     * @param plugin plugin
     * @param nIdZipBasket id of the zipbasket
     * @param strUrl the new url
     */
    public static void changeZipBasketUrl( Plugin plugin, int nIdZipBasket, String strUrl )
    {
        _dao.changeZipBasketUrl( plugin, nIdZipBasket, strUrl );
    }

    /**
     * this method return a specific zipbasket by id
     * @param plugin plugin
     * @param nIdZipBasket id of the zipbasket
     * @return the zipbasket by the id
     */
    public static ZipBasket loadZipBasket( Plugin plugin, int nIdZipBasket )
    {
        return _dao.loadZipBasket( plugin, nIdZipBasket );
    }

    /**
     * Find all zip baskets by date
     * @param plugin the plugin
     * @param dateExpiry the date expiry
     * @return a list of {@link ZipBasket}
     */
    public static List<ZipBasket> loadZipBasketByDate( Plugin plugin, Date dateExpiry )
    {
        return _dao.loadZipBasketByDate( plugin, dateExpiry );
    }
}
