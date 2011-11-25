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
package fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.service.daemon;

import fr.paris.lutece.plugins.directory.modules.pdfproducer.service.DirectoryPDFProducerPlugin;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket.ZipBasket;
import fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.service.DirectoryManageZipBasketService;
import fr.paris.lutece.portal.service.daemon.Daemon;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import org.apache.commons.lang.StringUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 *
 * Daemon ZipCleanerDaemon
 *
 */
public class ZipCleanerDaemon extends Daemon
{
    // BEANS
    private static final String BEAN_DIRECTOR_MANAGE_ZIP_BASKET_SERVICE = "directory-pdfproducer-archive.directoryManageZipBasketService";

    // PROPERTIES
    private static final String PROPERTY_DAEMON_NB_EXPIRATION_DAYS = "daemon.zipCleaner.nbExpirationDays";

    // VARIABLES
    private DirectoryManageZipBasketService _manageZipBasketService = (DirectoryManageZipBasketService) SpringContextService.getPluginBean( DirectoryPDFProducerPlugin.PLUGIN_NAME,
            BEAN_DIRECTOR_MANAGE_ZIP_BASKET_SERVICE );

    /**
     * Daemon's treatment method
     */
    public void run(  )
    {
        StringBuilder sbLog = new StringBuilder(  );
        Plugin plugin = PluginService.getPlugin( DirectoryPDFProducerPlugin.PLUGIN_NAME );
        int nExpirationDays = AppPropertiesService.getPropertyInt( PROPERTY_DAEMON_NB_EXPIRATION_DAYS, 7 );
        Calendar calendar = new GregorianCalendar(  );
        calendar.add( Calendar.DATE, -nExpirationDays );

        for ( ZipBasket zipBasket : _manageZipBasketService.loadZipBasketByDate( plugin, calendar.getTime(  ) ) )
        {
            if ( zipBasket != null )
            {
                sbLog.append( "\n- Cleaning archive '" + zipBasket.getZipName(  ) + " (ID : " + zipBasket.getIdZip(  ) +
                    ")'" );
                _manageZipBasketService.deleteZipBasket( plugin, zipBasket.getIdZip(  ),
                    Integer.toString( zipBasket.getIdRecord(  ) ), sbLog );
            }
        }

        if ( StringUtils.isBlank( sbLog.toString(  ) ) )
        {
            sbLog.append( "\nNo archive to clean" );
        }

        setLastRunLogs( sbLog.toString(  ) );
    }
}
