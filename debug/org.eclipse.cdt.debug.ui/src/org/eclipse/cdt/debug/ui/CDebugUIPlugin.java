package org.eclipse.cdt.debug.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.debug.core.IDisassemblyStorage;
import org.eclipse.cdt.debug.core.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.core.IFormattedMemoryRetrieval;
import org.eclipse.cdt.debug.core.ISwitchToFrame;
import org.eclipse.cdt.debug.core.ISwitchToThread;
import org.eclipse.cdt.debug.internal.ui.CDTDebugModelPresentation;
import org.eclipse.cdt.debug.internal.ui.CDebugImageDescriptorRegistry;
import org.eclipse.cdt.debug.internal.ui.ColorManager;
import org.eclipse.cdt.debug.internal.ui.editors.DisassemblyEditorInput;
import org.eclipse.cdt.debug.internal.ui.preferences.CDebugPreferencePage;
import org.eclipse.cdt.debug.internal.ui.preferences.MemoryViewPreferencePage;
import org.eclipse.cdt.debug.internal.ui.preferences.RegistersViewPreferencePage;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class CDebugUIPlugin extends AbstractUIPlugin implements ISelectionListener, IDebugEventSetListener
{
	//The shared instance.
	private static CDebugUIPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

	protected Map fDebuggerPageMap;

	private CDebugImageDescriptorRegistry fImageDescriptorRegistry;

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
		RegistersViewPreferencePage.initDefaults( pstore );
		CDebugPreferencePage.initDefaults( pstore );
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

	/**
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e the exception to be logged
	 */	
	public static void log( Throwable e )
	{
		log( new Status( IStatus.ERROR, getUniqueIdentifier(), ICDebugUIConstants.INTERNAL_ERROR, "Internal Error", e ) );
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

	public static void errorDialog( String message, IStatus status )
	{
		log( status );
		Shell shell = getActiveWorkbenchShell();
		if ( shell != null )
		{
			ErrorDialog.openError( shell, "Error", message, status );
		}
	}

	public static void errorDialog( String message, Throwable t )
	{
		log( t );
		Shell shell = getActiveWorkbenchShell();
		if ( shell != null )
		{
			IStatus status = new Status( IStatus.ERROR, getUniqueIdentifier(), ICDebugUIConstants.INTERNAL_ERROR, t.getMessage(), null ); //$NON-NLS-1$	
			ErrorDialog.openError( shell, "Error", message, status );
		}
	}

	/**
	 * Returns the active workbench window
	 * 
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow()
	{
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static IWorkbenchPage getActivePage()
	{
		IWorkbenchWindow w = getActiveWorkbenchWindow();
		if ( w != null )
		{
			return w.getActivePage();
		}
		return null;
	}

	/**
	 * Returns the active workbench shell or <code>null</code> if none
	 * 
	 * @return the active workbench shell or <code>null</code> if none
	 */
	public static Shell getActiveWorkbenchShell()
	{
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if ( window != null )
		{
			return window.getShell();
		}
		return null;
	}

	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated display. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay()
	{
		Display display;
		display = Display.getCurrent();
		if ( display == null )
			display = Display.getDefault();
		return display;
	}

	/**
	 * Returns the image descriptor registry used for this plugin.
	 */
	public static CDebugImageDescriptorRegistry getImageDescriptorRegistry()
	{
		if ( getDefault().fImageDescriptorRegistry == null )
		{
			getDefault().fImageDescriptorRegistry = new CDebugImageDescriptorRegistry();
		}
		return getDefault().fImageDescriptorRegistry;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#shutdown()
	 */
	public void shutdown() throws CoreException
	{
		DebugPlugin.getDefault().removeDebugEventListener( this );
		IWorkbenchWindow ww = getActiveWorkbenchWindow();
		if ( ww != null )
		{
			ww.getSelectionService().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		}
		if ( fImageDescriptorRegistry != null )
		{
			fImageDescriptorRegistry.dispose();
		}
		super.shutdown();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#startup()
	 */
	public void startup() throws CoreException
	{
		super.startup();
		IWorkbenchWindow ww = getActiveWorkbenchWindow();
		if ( ww != null )
		{
			ww.getSelectionService().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		}
		DebugPlugin.getDefault().addDebugEventListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection )
	{
		if ( selection != null && selection instanceof IStructuredSelection )
		{
			if ( ((IStructuredSelection)selection).size() == 1 )
			{
				Object element = ((IStructuredSelection)selection).getFirstElement();
				if ( element != null && element instanceof IThread )
				{
					if ( ((IThread)element).getDebugTarget() instanceof ISwitchToThread )
					{
						try
						{
							((ISwitchToThread)((IThread)element).getDebugTarget()).setCurrentThread( (IThread)element );
						}
						catch( DebugException e )
						{
							errorDialog( e.getMessage(), e );
						}
					}
				}
				else if ( element != null && element instanceof IStackFrame )
				{
					if ( ((IStackFrame)element).getThread() instanceof ISwitchToFrame )
					{
						try
						{
							((ISwitchToFrame)((IStackFrame)element).getThread()).switchToFrame( (IStackFrame)element );
						}
						catch( DebugException e )
						{
							errorDialog( "Switch to stack frame failed.", e );
						}
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents( DebugEvent[] events )
	{
		for ( int i = 0; i < events.length; i++ )
		{
			DebugEvent event = events[i];
			if ( event.getKind() == DebugEvent.TERMINATE )
			{
				Object element = event.getSource();
				if ( element != null && element instanceof IDebugTarget )
				{
					closeDisassemblyEditors( (IDebugTarget)element );
				}
			}
		}
	}
	
	private void closeDisassemblyEditors( final IDebugTarget target )
	{
		IWorkbenchWindow[] windows = getWorkbench().getWorkbenchWindows();
		for ( int i = 0; i < windows.length; ++i )
		{
			IWorkbenchPage[] pages = windows[i].getPages();
			for ( int j = 0; j < pages.length; ++j )
			{
				IEditorReference[] refs = pages[j].getEditorReferences();
				for ( int k = 0; k < refs.length; ++k )
				{
					IEditorPart editor = refs[k].getEditor( false );
					if ( editor != null )
					{
						IEditorInput input = editor.getEditorInput();
						if ( input != null && input instanceof DisassemblyEditorInput )
						{
							try
							{
								IStorage storage = ((DisassemblyEditorInput)input).getStorage();
								if ( storage != null && storage instanceof IDisassemblyStorage && 
									 target.equals( ((IDisassemblyStorage)storage).getDebugTarget() ) )
								{
									Shell shell = windows[i].getShell();
									if ( shell != null )
									{
										Display display = shell.getDisplay();
										if ( display != null )
										{
											final IWorkbenchPage page = pages[j];
											final IEditorPart editor0 = editor;
											display.asyncExec( new Runnable()
																	{
																		public void run()
																		{
																			page.closeEditor( editor0, false );
																		}
																	} );
										}
									}
								}
							}
							catch( CoreException e )
							{
								// ignore
							}
						}
					}
				}
			}
		}
	}
}
