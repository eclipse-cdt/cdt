package org.eclipse.cdt.debug.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.debug.core.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.core.IFormattedMemoryRetrieval;
import org.eclipse.cdt.debug.internal.ui.CDTDebugModelPresentation;
import org.eclipse.cdt.debug.internal.ui.ColorManager;
import org.eclipse.cdt.debug.internal.ui.preferences.MemoryViewPreferencePage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
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

	protected Map fDebuggerPageMap;
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
	
	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public ILaunchConfigurationTab getDebuggerPage(String debuggerID) {
		if (fDebuggerPageMap == null) {	
			initializeDebuggerPageMap();
		}
		IConfigurationElement configElement = (IConfigurationElement) fDebuggerPageMap.get(debuggerID);
		ILaunchConfigurationTab tab = null;
		if (configElement != null) {
			try {
				tab = (ILaunchConfigurationTab) configElement.createExecutableExtension("class"); //$NON-NLS-1$
			} catch(CoreException ce) {			 
				log(new Status(Status.ERROR, getUniqueIdentifier(), 100, "An error occurred retrieving a C Debugger page", ce));
			}
		}
		return tab;
	}
	
	protected void initializeDebuggerPageMap() {
		fDebuggerPageMap = new HashMap(10);

		IPluginDescriptor descriptor= getDefault().getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint("CDebuggerPage");
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();
		for (int i = 0; i < infos.length; i++) {
			String id = infos[i].getAttribute("debuggerID"); //$NON-NLS-1$
			fDebuggerPageMap.put(id, infos[i]);
		}		
	}
}
