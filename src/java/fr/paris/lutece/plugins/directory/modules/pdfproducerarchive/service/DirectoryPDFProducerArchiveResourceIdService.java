package fr.paris.lutece.plugins.directory.modules.pdfproducerarchive.service;

import java.util.Locale;

import fr.paris.lutece.portal.service.rbac.Permission;
import fr.paris.lutece.portal.service.rbac.ResourceIdService;
import fr.paris.lutece.portal.service.rbac.ResourceType;
import fr.paris.lutece.portal.service.rbac.ResourceTypeManager;
import fr.paris.lutece.util.ReferenceList;

/**
 * DirectoryPDFProducerArchiveResourceIdService
 *
 */
public class DirectoryPDFProducerArchiveResourceIdService extends ResourceIdService
{

	/** Permission for generate zip */
    public static final String PERMISSION_GENERATE_ZIP = "ADDZIPBASKET";
    
    /** Permission for mylutece user visualisation */
    private static final String RESOURCE_TYPE = "DIRECTORY_PDFPRODUCER_ARCHIVE_TYPE";
    private static final String PROPERTY_LABEL_GENERATE_ZIP = "module.directory.pdfproducerarchive.permission.label.generate_zip";
	
	@Override
	public ReferenceList getResourceIdList(Locale locale) 
	{
		return null;
	}

	@Override
	public String getTitle(String strId, Locale locale) 
	{
		return null;
	}

	@Override
	public void register() 
	{
		ResourceType rt = new ResourceType(  );
        rt.setResourceIdServiceClass( DirectoryPDFProducerArchiveResourceIdService.class.getName(  ) );
        rt.setPluginName( DirectoryPDFProducerArchivePlugin.PLUGIN_NAME );
        rt.setResourceTypeKey( RESOURCE_TYPE );
        rt.setResourceTypeLabelKey( PROPERTY_LABEL_GENERATE_ZIP );
        
		Permission p = new Permission(  );
		p.setPermissionKey( PERMISSION_GENERATE_ZIP );
        p.setPermissionTitleKey( PROPERTY_LABEL_GENERATE_ZIP );
        rt.registerPermission( p );	
        
        ResourceTypeManager.registerResourceType( rt );
	}

}
