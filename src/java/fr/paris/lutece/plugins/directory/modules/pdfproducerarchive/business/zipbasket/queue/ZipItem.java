package fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.business.zipbasket.queue;

import java.util.Locale;


/**
 * ZipItem
 */
public class ZipItem
{
    private int _nIdDirectory;
    private String[] _listIdRecord;
    private int _nIdAdminUser;
    private boolean _bIsLocked;
    private Locale _locale;
    
    /**
     * Get the Id of the directory
     * @return The id of the directory
     */
    public int getIdDirectory( )
    {
        return _nIdDirectory;
    }

    /**
     * Set the Id of the directory
     * @param nIdDirectory The id of the directory
     */
    public void setIdDirectory( int nIdDirectory )
    {
        this._nIdDirectory = nIdDirectory;
    }

    /**
     * Get the list of id record
     * @return The list of id record
     */
    public String[] getListIdRecord( )
    {
        return _listIdRecord;
    }

    /**
     * Set the list of id record
     * @param nIdRecord The list of id record
     */
    public void setListIdRecord( String[] listIdRecord )
    {
        this._listIdRecord = listIdRecord;
    }

    /**
     * Get the Id of the admin user
     * @return The id of the admin user
     */
    public int getIdAdminUser( )
    {
        return _nIdAdminUser;
    }

    /**
     * Set the Id of the admin user
     * @param nIdAdminUser The id of the admin user
     */
    public void setIdAdminUser( int nIdAdminUser )
    {
        this._nIdAdminUser = nIdAdminUser;
    }

    /**
     * Check if the item is locked or not
     * @return True if the item is locked, false otherwise
     */
    public boolean isIsLocked( )
    {
        return _bIsLocked;
    }

    /**
     * Set the lock attribut of the item
     * @param bIsLocked True if the item is locked, false otherwise
     */
    public void setIsLocked( boolean bIsLocked )
    {
        this._bIsLocked = bIsLocked;
    }

    /**
     * Get the locale
     * @return The locale
     */
    public Locale getLocale( )
    {
        return _locale;
    }

    /**
     * Set the locale
     * @param locale The locale
     */
    public void setLocale( Locale locale )
    {
        this._locale = locale;
    }

}
