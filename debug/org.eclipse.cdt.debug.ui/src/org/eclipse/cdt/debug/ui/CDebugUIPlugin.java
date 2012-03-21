/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * ARM Limited - https://bugs.eclipse.org/bugs/show_bug.cgi?id=186981
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.debug.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.internal.ui.CDebugImageDescriptorRegistry;
import org.eclipse.cdt.debug.internal.ui.CDebugModelPresentation;
import org.eclipse.cdt.debug.internal.ui.CDebuggerPageAdapter;
import org.eclipse.cdt.debug.internal.ui.CRegisterManagerProxies;
import org.eclipse.cdt.debug.internal.ui.ColorManager;
import org.eclipse.cdt.debug.internal.ui.EvaluationContextManager;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.internal.ui.breakpoints.CBreakpointUpdater;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyBackendCdiFactory;
import org.eclipse.cdt.debug.internal.ui.disassembly.editor.DisassemblyEditorManager;
import org.eclipse.cdt.debug.internal.ui.pinclone.ViewIDCounterManager;
import org.eclipse.cdt.debug.ui.sourcelookup.DefaultSourceLocator;
import org.eclipse.cdt.debug.ui.sourcelookup.OldDefaultSourceLocator;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.WorkbenchJob;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class CDebugUIPlugin extends AbstractUIPlugin {

	/**
	 * The plug-in identifier (value <code>"org.eclipse.cdt.debug.ui"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.cdt.debug.ui"; //$NON-NLS-1$

	public static final String CDEBUGGER_PAGE_EXTENSION_POINT_ID = "CDebuggerPage"; //$NON-NLS-1$
	public static final String DEBUGGER_PAGE_ELEMENT = "debuggerPage"; //$NON-NLS-1$
	
	//The shared instance.
	private static CDebugUIPlugin plugin;

	protected Map<String, IConfigurationElement> fDebuggerPageMap;

	private CDebugImageDescriptorRegistry fImageDescriptorRegistry;

    private DisassemblyEditorManager fDisassemblyEditorManager;

	/**
	 * The constructor.
	 */
	public CDebugUIPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static CDebugUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		return PLUGIN_ID;
	}

	/**
	 * Returns the a color based on the type of output. Valid types:
	 * <li>CHANGED_REGISTER_RGB</li>
	 */
	public static Color getPreferenceColor( String type ) {
		return ColorManager.getDefault().getColor( PreferenceConverter.getColor( getDefault().getPreferenceStore(), type ) );
	}

	public static CDebugModelPresentation getDebugModelPresentation() {
		return CDebugModelPresentation.getDefault();
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status
	 *            status to log
	 */
	public static void log( IStatus status ) {
		getDefault().getLog().log( status );
	}

	/**
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e
	 *            the exception to be logged
	 */
	public static void log( Throwable e ) {
		log( new Status( IStatus.ERROR, getUniqueIdentifier(), IInternalCDebugUIConstants.INTERNAL_ERROR, "Internal Error", e ) ); //$NON-NLS-1$
	}

	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message
	 *            the error message to log
	 */
	public static void logErrorMessage( String message ) {
		log( new Status( IStatus.ERROR, getUniqueIdentifier(), IInternalCDebugUIConstants.INTERNAL_ERROR, message, null ) );
	}

	public ICDebuggerPage getDebuggerPage( String debuggerID ) throws CoreException {
		if ( fDebuggerPageMap == null ) {
			initializeDebuggerPageMap();
		}
		IConfigurationElement configElement = fDebuggerPageMap.get( debuggerID );
		ICDebuggerPage tab = null;
		if ( configElement != null ) {
			Object o = configElement.createExecutableExtension( "class" ); //$NON-NLS-1$
			if ( o instanceof ICDebuggerPage ) {
				tab = (ICDebuggerPage)o;
				tab.init( debuggerID );
			}
			else if ( o instanceof ILaunchConfigurationTab ) {
				tab = new CDebuggerPageAdapter( (ILaunchConfigurationTab)o );
				tab.init( debuggerID );
			}
		}
		return tab;
	}

	protected void initializeDebuggerPageMap() {
		fDebuggerPageMap = new HashMap<String, IConfigurationElement>( 10 );
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint( PLUGIN_ID, CDEBUGGER_PAGE_EXTENSION_POINT_ID );
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		for( int i = 0; i < infos.length; i++ ) {
			IConfigurationElement info = infos[i];
			if (info.getName().equals(DEBUGGER_PAGE_ELEMENT)) {
				String id = info.getAttribute( "debuggerID" ); //$NON-NLS-1$
				if (id != null) {
					fDebuggerPageMap.put( id, info );
				}
			}
		}
	}

	public static void errorDialog( String message, IStatus status ) {
		log( status );
		Shell shell = getActiveWorkbenchShell();
		if ( shell != null ) {
			ErrorDialog.openError( shell, UIMessages.getString( "CDebugUIPlugin.0" ), message, status ); //$NON-NLS-1$
		}
	}

	public static void errorDialog( String message, Throwable t ) {
		log( t );
		Shell shell = getActiveWorkbenchShell();
		if ( shell != null ) {
			IStatus status = new Status( IStatus.ERROR, getUniqueIdentifier(), IInternalCDebugUIConstants.INTERNAL_ERROR, t.getMessage(), null );	
			ErrorDialog.openError( shell, UIMessages.getString( "CDebugUIPlugin.0" ), message, status ); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the active workbench window
	 * 
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow w = getActiveWorkbenchWindow();
		if ( w != null ) {
			return w.getActivePage();
		}
		return null;
	}

	/**
	 * Returns the active workbench shell, or the shell from the first available
	 * workbench window, or <code>null</code> if neither is available.
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window == null) {
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			if (windows.length > 0) {
				return windows[0].getShell();
			}
		}
		else {
			return window.getShell();
		}
		return null;
	}

	/**
	 * Returns the standard display to be used. The method first checks, if the thread calling this method has an associated display. If so, this display is
	 * returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display;
		display = Display.getCurrent();
		if ( display == null )
			display = Display.getDefault();
		return display;
	}

	/**
	 * Returns the image descriptor registry used for this plugin.
	 */
	public static CDebugImageDescriptorRegistry getImageDescriptorRegistry() {
		if ( getDefault().fImageDescriptorRegistry == null ) {
			getDefault().fImageDescriptorRegistry = new CDebugImageDescriptorRegistry();
		}
		return getDefault().fImageDescriptorRegistry;
	}

	public static IPersistableSourceLocator createDefaultSourceLocator() {
		return new DefaultSourceLocator();
	}

	public static String getDefaultSourceLocatorID() {
		return OldDefaultSourceLocator.ID_DEFAULT_SOURCE_LOCATOR;
	}

	/*
	 * to support old launch configurations
	 */
	public static String getDefaultSourceLocatorOldID() {
		return OldDefaultSourceLocator.ID_OLD_DEFAULT_SOURCE_LOCATOR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start( BundleContext context ) throws Exception {
		super.start( context );
        fDisassemblyEditorManager = new DisassemblyEditorManager();
		CDebugCorePlugin.getDefault().addCBreakpointListener( CBreakpointUpdater.getInstance() );
		
		// Register the CDI backend for DSF's disassembly view
		Platform.getAdapterManager().registerAdapters(new DisassemblyBackendCdiFactory(), ICDebugElement.class);
		
		WorkbenchJob wjob = new WorkbenchJob("Initializing CDT Debug UI") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				startupInUIThread();
				return Status.OK_STATUS;
			}
		};
		wjob.schedule();
	}

	private void startupInUIThread() {
		EvaluationContextManager.startup();
		ViewIDCounterManager.getInstance().init();
		
		// We contribute actions to the platform's Variables view with a
		// criteria to enable only when this plugin is loaded. This can lead to
		// some edge cases with broken behavior (273306). The solution is to
		// force a selection change notification after we get loaded.
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
		   IWorkbenchPage[] pages = window.getPages();
		   for (IWorkbenchPage page : pages) {
			   IViewReference viewRef = page.findViewReference(IDebugUIConstants.ID_VARIABLE_VIEW);
			   if (viewRef != null) {
				   IViewPart part = viewRef.getView(false);
				   if (part instanceof IDebugView) {
					   Viewer viewer = ((IDebugView)part).getViewer();
					   if (viewer != null) {
						   viewer.setSelection(viewer.getSelection());
					   }
				   }
			   }
		   }
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop( BundleContext context ) throws Exception {
		CDebugCorePlugin.getDefault().removeCBreakpointListener( CBreakpointUpdater.getInstance() );
		CRegisterManagerProxies.getInstance().dispose();
        fDisassemblyEditorManager.dispose();
		if ( fImageDescriptorRegistry != null ) {
			fImageDescriptorRegistry.dispose();
		}
		super.stop( context );
	}

	/**
	 * Returns the shared text colors of this plug-in.
	 *
	 * @return the shared text colors
	 * @since 3.1
	 */
	public ISharedTextColors getSharedTextColors() {
		return EditorsUI.getSharedTextColors();
	}

    public DisassemblyEditorManager getDisassemblyEditorManager() {
        return fDisassemblyEditorManager;
    }
    
	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * 
	 * @param key - key is usually plug-in relative path to image like icons/xxx.gif
	 * @return Image loaded from key location or from registry cache, it will be stored in plug-in registry and disposed when plug-in unloads
	 */
	public Image getImage(String key) {
		ImageRegistry registry = getImageRegistry();
		Image image = registry.get(key);
		if (image == null) {
			ImageDescriptor descriptor = imageDescriptorFromPlugin(PLUGIN_ID, key);
			if (descriptor==null) {
				ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
				return sharedImages.getImage(key);
			}
			registry.put(key, descriptor);
			image = registry.get(key);
		}
		return image;
	}

	/**
	 * @param key - image key to associate with descriptor, if not set yet
	 * @param desc - image descriptor
	 * @return
	 */
	public Image getImage(String key, ImageDescriptor desc) {
		ImageRegistry registry = getImageRegistry();
		Image image = registry.get(key);
		if (image == null) {
			registry.put(key, desc);
			image = registry.get(key);
		}
		return image;
	}
}
