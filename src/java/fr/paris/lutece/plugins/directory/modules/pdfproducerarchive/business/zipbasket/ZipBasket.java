/*
 * Copyright (c) 2002-2014, Mairie de Paris
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

import java.sql.Timestamp;

import java.util.List;


/**
 * Panier to stock url zip
 */
public class ZipBasket
{
    private int _nIdZip;
    private String _strZipName;
    private String _strZipUrl;
    private String _strZipStatus;
    private int _nIdAdminUser;
    private int _nIdDirectory;
    private int _nIdRecord;
    private int _nArchiveItemKey;
    private Timestamp _dateZipAdded;
    private List<ZipBasketAction> _listZipBasketAction;

    /**
     * @return id zip
     */
    public int getIdZip(  )
    {
        return _nIdZip;
    }

    /**
     * @param nIdZip id zip
     */
    public void setIdZip( int nIdZip )
    {
        _nIdZip = nIdZip;
    }

    /**
     * @return zip name
     */
    public String getZipName(  )
    {
        return _strZipName;
    }

    /**
     * @param strZipName zip name
     */
    public void setZipName( String strZipName )
    {
        _strZipName = strZipName;
    }

    /**
     * @return zip url
     */
    public String getZipUrl(  )
    {
        return _strZipUrl;
    }

    /**
     * @param strZipUrl zip url
     */
    public void setZipUrl( String strZipUrl )
    {
        _strZipUrl = strZipUrl;
    }

    /**
     * @return zip status
     */
    public String getZipStatus(  )
    {
        return _strZipStatus;
    }

    /**
     * @param strZipStatus zip status
     */
    public void setZipStatus( String strZipStatus )
    {
        _strZipStatus = strZipStatus;
    }

    /**
     * @return id admin user
     */
    public int getIdAdminUser(  )
    {
        return _nIdAdminUser;
    }

    /**
     * @param nIdAdminUser  id admin user
     */
    public void setIdAdminUser( int nIdAdminUser )
    {
        _nIdAdminUser = nIdAdminUser;
    }

    /**
     * @return id directory
     */
    public int getIdDirectory(  )
    {
        return _nIdDirectory;
    }

    /**
     * @param nIdDirectory id directory
     */
    public void setIdDirectory( int nIdDirectory )
    {
        _nIdDirectory = nIdDirectory;
    }

    /**
     * @return id record
     */
    public int getIdRecord(  )
    {
        return _nIdRecord;
    }

    /**
     * @param nIdRecord id record
     */
    public void setIdRecord( int nIdRecord )
    {
        _nIdRecord = nIdRecord;
    }

    /**
     * @return archive item key
     */
    public int getArchiveItemKey(  )
    {
        return _nArchiveItemKey;
    }

    /**
     * @param nArchiveItemKey archive item key
     */
    public void setArchiveItemKey( int nArchiveItemKey )
    {
        _nArchiveItemKey = nArchiveItemKey;
    }

    /**
     * @return date zip added
     */
    public Timestamp getDateZipAdded(  )
    {
        return _dateZipAdded;
    }

    /**
     * @param dateZipAdded  date zip added
     */
    public void setDateZipAdded( Timestamp dateZipAdded )
    {
        _dateZipAdded = dateZipAdded;
    }

    /**
     * @return list zip basket action
     */
    public List<ZipBasketAction> getListZipBasketAction(  )
    {
        return _listZipBasketAction;
    }

    /**
     * @param listZipBasketAction list zip basket action
     */
    public void setListZipBasketAction( List<ZipBasketAction> listZipBasketAction )
    {
        _listZipBasketAction = listZipBasketAction;
    }
}
