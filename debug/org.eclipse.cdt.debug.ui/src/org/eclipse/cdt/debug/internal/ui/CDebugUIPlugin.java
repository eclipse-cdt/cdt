package org.eclipse.cdt.debug.internal.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.debug.core.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.core.IFormattedMemoryRetrieval;
import org.eclipse.cdt.debug.internal.ui.preferences.MemoryViewPreferencePage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class CDebugUIPlugin extends AbstractUIPlugin
{
	//The shared instance.
	private static CDebugUIPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

	/**
	 * The constructor.
	 */
	public CDebugUIPlugin( IPluginDescriptor descriptor )
	{
		super( descriptor );
		plugin = this;
		try
		{
			resourceBundle =
				ResourceBundle.getBundle( "org.eclipse.cdt.debug.ui.CDebugUIPluginResources" );
		}
		catch( MissingResourceException x )
		{
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static CDebugUIPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace()
	{
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key)
	{
		ResourceBundle bundle = CDebugUIPlugin.getDefault().getResourceBundle();
		try
		{
			return bundle.getString( key );
		}
		catch ( MissingResourceException e )
		{
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle()
	{
		return resourceBundle;
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier()
	{
		if ( getDefault() == null )
		{
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return "org.eclipse.cdt.debug.ui"; //$NON-NLS-1$
		}
		return getDefault().getDescriptor().getUniqueIdentifier();
	}

	/**
	 * Returns the a color based on the type of output.
	 * Valid types:
	 * <li>CHANGED_REGISTER_RGB</li>
	 */
	public static Color getPreferenceColor( String type )
	{
		return ColorManager.getDefault().getColor( 
				PreferenceConverter.getColor( getDefault().getPreferenceStore(), type ) );
	}

	/**
	 * @see AbstractUIPlugin#initializeDefaultPreferences
	 */
	protected void initializeDefaultPreferences( IPreferenceStore pstore ) 
	{
		MemoryViewPreferencePage.initDefaults( pstore );
	}
	
	public static CDTDebugModelPresentation getDebugModelPresentation()
	{
		return CDTDebugModelPresentation.getDefault();
	}

	public void addBlock( IFormattedMemoryRetrieval mbr, IFormattedMemoryBlock memoryBlock )
	{
	}
	
	public void removeBlock( IFormattedMemoryRetrieval mbr, IFormattedMemoryBlock memoryBlock )
	{
	}

	public void removeAllBlocks( IFormattedMemoryRetrieval mbr )
	{
	}

	public IFormattedMemoryBlock getBlock( IFormattedMemoryRetrieval mbr, int index )
	{
		return null;
	}

	public IFormattedMemoryBlock[] getBlocks( IFormattedMemoryRetrieval mbr )
	{
		return new IFormattedMemoryBlock[0];
	}
}
