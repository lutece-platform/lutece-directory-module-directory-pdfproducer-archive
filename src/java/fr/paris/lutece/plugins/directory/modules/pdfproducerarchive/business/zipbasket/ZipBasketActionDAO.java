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
package fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Allow to access ZipBasketAction data
 *
 */
public class ZipBasketActionDAO implements IZipBasketActionDAO
{
    private static final String SQL_QUERY_SELECT_ACTIONS = "SELECT id_action, name_key, description_key, action_url, icon_url, action_permission, directory_state FROM directory_zip_basket_action WHERE directory_state = ? ;";

    /**
     * {@inheritDoc}
     */
    public List<ZipBasketAction> selectActionsByZipBasketState( int nState, Plugin plugin )
    {
        List<ZipBasketAction> listActions = new ArrayList<ZipBasketAction>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ACTIONS, plugin );
        daoUtil.setInt( 1, nState );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            ZipBasketAction action = new ZipBasketAction(  );
            action.setNameKey( daoUtil.getString( 2 ) );
            action.setDescriptionKey( daoUtil.getString( 3 ) );
            action.setUrl( daoUtil.getString( 4 ) );
            action.setIconUrl( daoUtil.getString( 5 ) );
            action.setPermission( daoUtil.getString( 6 ) );
            action.setFormState( daoUtil.getInt( 7 ) );
            listActions.add( action );
        }

        daoUtil.free(  );

        return listActions;
    }
}
