/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.ui;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.model.ISwitchToFrame;
import org.eclipse.cdt.debug.core.model.ISwitchToThread;
import org.eclipse.cdt.debug.internal.ui.CBreakpointUpdater;
import org.eclipse.cdt.debug.internal.ui.CDTDebugModelPresentation;
import org.eclipse.cdt.debug.internal.ui.CDebugImageDescriptorRegistry;
import org.eclipse.cdt.debug.internal.ui.ColorManager;
import org.eclipse.cdt.debug.ui.sourcelookup.DefaultSourceLocator;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class CDebugUIPlugin extends AbstractUIPlugin implements ISelectionListener {

	/**
	 * The plug-in identifier (value <code>"org.eclipse.cdt.debug.ui"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.cdt.debug.ui"; //$NON-NLS-1$

	//The shared instance.
	private static CDebugUIPlugin plugin;

	protected Map fDebuggerPageMap;

	private CDebugImageDescriptorRegistry fImageDescriptorRegistry;

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
		if ( getDefault() == null ) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * Returns the a color based on the type of output. Valid types:
	 * <li>CHANGED_REGISTER_RGB</li>
	 */
	public static Color getPreferenceColor( String type ) {
		return ColorManager.getDefault().getColor( PreferenceConverter.getColor( getDefault().getPreferenceStore(), type ) );
	}

	public static CDTDebugModelPresentation getDebugModelPresentation() {
		return CDTDebugModelPresentation.getDefault();
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
		log( new Status( IStatus.ERROR, getUniqueIdentifier(), ICDebugUIConstants.INTERNAL_ERROR, "Internal Error", e ) ); //$NON-NLS-1$
	}

	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message
	 *            the error message to log
	 */
	public static void logErrorMessage( String message ) {
		log( new Status( IStatus.ERROR, getUniqueIdentifier(), ICDebugUIConstants.INTERNAL_ERROR, message, null ) );
	}

	public ILaunchConfigurationTab getDebuggerPage( String debuggerID ) {
		if ( fDebuggerPageMap == null ) {
			initializeDebuggerPageMap();
		}
		IConfigurationElement configElement = (IConfigurationElement)fDebuggerPageMap.get( debuggerID );
		ILaunchConfigurationTab tab = null;
		if ( configElement != null ) {
			try {
				tab = (ILaunchConfigurationTab)configElement.createExecutableExtension( "class" ); //$NON-NLS-1$
			}
			catch( CoreException ce ) {
				log( new Status( IStatus.ERROR, getUniqueIdentifier(), 100, "An error occurred retrieving a C Debugger page", ce ) ); //$NON-NLS-1$
			}
		}
		return tab;
	}

	protected void initializeDebuggerPageMap() {
		fDebuggerPageMap = new HashMap( 10 );
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint( PLUGIN_ID, "CDebuggerPage" ); //$NON-NLS-1$
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		for( int i = 0; i < infos.length; i++ ) {
			String id = infos[i].getAttribute( "debuggerID" ); //$NON-NLS-1$
			fDebuggerPageMap.put( id, infos[i] );
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
			IStatus status = new Status( IStatus.ERROR, getUniqueIdentifier(), ICDebugUIConstants.INTERNAL_ERROR, t.getMessage(), null ); //$NON-NLS-1$	
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
	 * Returns the active workbench shell or <code>null</code> if none
	 * 
	 * @return the active workbench shell or <code>null</code> if none
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if ( window != null ) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
		if ( selection != null && selection instanceof IStructuredSelection ) {
			if ( ((IStructuredSelection)selection).size() == 1 ) {
				Object element = ((IStructuredSelection)selection).getFirstElement();
				if ( element != null && element instanceof IThread ) {
					if ( ((IThread)element).getDebugTarget() instanceof ISwitchToThread ) {
						try {
							if ( !sameThread( (IDebugElement)element ) ) {
								((ISwitchToThread)((IThread)element).getDebugTarget()).setCurrentThread( (IThread)element );
							}
						}
						catch( DebugException e ) {
							errorDialog( e.getMessage(), e );
						}
					}
				}
				else if ( element != null && element instanceof IStackFrame ) {
					if ( ((IStackFrame)element).getThread() instanceof ISwitchToFrame ) {
						try {
							if ( !sameThread( (IDebugElement)element ) ) {
								((ISwitchToThread)((IStackFrame)element).getDebugTarget()).setCurrentThread( ((IStackFrame)element).getThread() );
							}
							((ISwitchToFrame)((IStackFrame)element).getThread()).switchToFrame( (IStackFrame)element );
						}
						catch( DebugException e ) {
							//							errorDialog( "Switch to stack frame failed.", e );
						}
					}
				}
			}
		}
	}

	private boolean sameThread( IDebugElement element ) throws DebugException {
		if ( element.getDebugTarget() instanceof ISwitchToThread ) {
			if ( element instanceof IThread ) {
				return ((IThread)element).equals( ((ISwitchToThread)element.getDebugTarget()).getCurrentThread() );
			}
			if ( element instanceof IStackFrame ) {
				return ((IStackFrame)element).getThread().equals( ((ISwitchToThread)element.getDebugTarget()).getCurrentThread() );
			}
		}
		return false;
	}

	public static IPersistableSourceLocator createDefaultSourceLocator() {
		return new DefaultSourceLocator();
	}

	public static String getDefaultSourceLocatorID() {
		return DefaultSourceLocator.ID_DEFAULT_SOURCE_LOCATOR;
	}

	/*
	 * to support old launch configurations
	 */
	public static String getDefaultSourceLocatorOldID() {
		return DefaultSourceLocator.ID_OLD_DEFAULT_SOURCE_LOCATOR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start( BundleContext context ) throws Exception {
		super.start( context );
		listenSelection( true, this );
		CDebugCorePlugin.getDefault().addCBreakpointListener( CBreakpointUpdater.getInstance() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop( BundleContext context ) throws Exception {
		CDebugCorePlugin.getDefault().removeCBreakpointListener( CBreakpointUpdater.getInstance() );
		try {
			listenSelection( false, this );
		} 
		catch (SWTException e) {
		}
		if ( fImageDescriptorRegistry != null ) {
			fImageDescriptorRegistry.dispose();
		}
		super.stop( context );
	}

	void listenSelection( final boolean enable, final ISelectionListener listener ) {
		Display display = getWorkbench().getDisplay();
		if ( display == null || display.isDisposed() )
			return;
		Runnable r = new Runnable() {
			
			public void run() {
				IWorkbenchWindow ww = getActiveWorkbenchWindow();
				if ( ww != null ) {
					if ( enable )
						ww.getSelectionService().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, listener );
					else
						ww.getSelectionService().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, listener );
				}
			}
		};
		display.asyncExec( r );
	}
}