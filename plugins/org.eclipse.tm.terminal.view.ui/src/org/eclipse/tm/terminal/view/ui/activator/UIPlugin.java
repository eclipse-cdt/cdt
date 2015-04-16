/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Max Weninger (Wind River) - [361363] [TERMINALS] Implement "Pin&Clone" for the "Terminals" view
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.activator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.view.core.preferences.ScopedEclipsePreferences;
import org.eclipse.tm.terminal.view.core.tracing.TraceHandler;
import org.eclipse.tm.terminal.view.ui.interfaces.ImageConsts;
import org.eclipse.tm.terminal.view.ui.listeners.WorkbenchWindowListener;
import org.eclipse.tm.terminal.view.ui.view.TerminalsView;
import org.eclipse.tm.terminal.view.ui.view.TerminalsViewMementoHandler;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class UIPlugin extends AbstractUIPlugin {
	// The shared instance
	private static UIPlugin plugin;
	// The scoped preferences instance
	private static volatile ScopedEclipsePreferences scopedPreferences;
	// The trace handler instance
	private static volatile TraceHandler traceHandler;
	// The workbench listener instance
	private IWorkbenchListener listener;
	// The global window listener instance
	private IWindowListener windowListener;

	/**
	 * The constructor
	 */
	public UIPlugin() {
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static UIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Convenience method which returns the unique identifier of this plug-in.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() != null && getDefault().getBundle() != null) {
			return getDefault().getBundle().getSymbolicName();
		}
		return "org.eclipse.tm.terminal.view.ui"; //$NON-NLS-1$
	}

	/**
	 * Return the scoped preferences for this plug-in.
	 */
	public static ScopedEclipsePreferences getScopedPreferences() {
		if (scopedPreferences == null) {
			scopedPreferences = new ScopedEclipsePreferences(getUniqueIdentifier());
		}
		return scopedPreferences;
	}

	/**
	 * Returns the bundles trace handler.
	 *
	 * @return The bundles trace handler.
	 */
	public static TraceHandler getTraceHandler() {
		if (traceHandler == null) {
			traceHandler = new TraceHandler(getUniqueIdentifier());
		}
		return traceHandler;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// Create and register the workbench listener instance
		listener = new IWorkbenchListener() {

			@Override
			public boolean preShutdown(IWorkbench workbench, boolean forced) {
				if (workbench != null && workbench.getActiveWorkbenchWindow() != null && workbench.getActiveWorkbenchWindow().getActivePage() != null) {
					// Find all "Terminals" views
					IViewReference[] refs = workbench.getActiveWorkbenchWindow().getActivePage().getViewReferences();
					for (IViewReference ref : refs) {
						IViewPart part = ref.getView(false);
						if (part instanceof TerminalsView) {
							/*
							 * The terminal tabs to save to the views memento on shutdown can
							 * be determined only _before_ the saveState(memento) method of the
							 * view is called. Within saveState, it is already to late and the
							 * terminals might be in CLOSED state already. This depends on the
							 * terminal type and the corresponding connector implementation.
							 *
							 * To be safe, we determine the still opened terminals on shutdown
							 * separately here in the preShutdown.
							 */
							final List<CTabItem> saveables = new ArrayList<CTabItem>();

							// Get the tab folder
							CTabFolder tabFolder = (CTabFolder)((TerminalsView)part).getAdapter(CTabFolder.class);
							if (tabFolder != null && !tabFolder.isDisposed()) {
								// Get the list of tab items
								CTabItem[] items = tabFolder.getItems();
								// Loop the tab items and find the still connected ones
								for (CTabItem item : items) {
									// Ignore disposed items
									if (item.isDisposed()) continue;
									// Get the terminal view control
									ITerminalViewControl terminal = (ITerminalViewControl)item.getData();
									if (terminal == null || terminal.getState() != TerminalState.CONNECTED) {
										continue;
									}
									// Still connected -> Add to the list
									saveables.add(item);
								}
							}

							// Push the determined saveable items to the memento handler
							TerminalsViewMementoHandler mementoHandler = (TerminalsViewMementoHandler)((TerminalsView)part).getAdapter(TerminalsViewMementoHandler.class);
							if (mementoHandler != null) mementoHandler.setSaveables(saveables);
						}
					}
				}

				return true;
			}

			@Override
			public void postShutdown(IWorkbench workbench) {
			}
		};
		PlatformUI.getWorkbench().addWorkbenchListener(listener);

		if (windowListener == null && PlatformUI.getWorkbench() != null) {
			windowListener = new WorkbenchWindowListener();
			PlatformUI.getWorkbench().addWindowListener(windowListener);
			activateContexts();
		}
	}

	void activateContexts() {
		if (Display.getCurrent() != null) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null && windowListener != null) windowListener.windowOpened(window);
		}
		else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){
				@Override
				public void run() {
					activateContexts();
				}});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		if (windowListener != null && PlatformUI.getWorkbench() != null) {
			PlatformUI.getWorkbench().removeWindowListener(windowListener);
			windowListener = null;
		}

		plugin = null;
		scopedPreferences = null;
		traceHandler = null;
		if (listener != null) { PlatformUI.getWorkbench().removeWorkbenchListener(listener); listener = null; }
		super.stop(context);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		Bundle bundle = Platform.getBundle("org.eclipse.ui.console"); //$NON-NLS-1$
		if (bundle != null) {
			URL url = bundle.getEntry(ImageConsts.IMAGE_DIR_ROOT + "full/" + ImageConsts.IMAGE_DIR_EVIEW + "console_view.gif"); //$NON-NLS-1$ //$NON-NLS-2$
			registry.put(ImageConsts.VIEW_Terminals, ImageDescriptor.createFromURL(url));

			url = bundle.getEntry(ImageConsts.IMAGE_DIR_ROOT + "full/" + ImageConsts.IMAGE_DIR_CLCL + "lock_co.gif"); //$NON-NLS-1$ //$NON-NLS-2$
			registry.put(ImageConsts.ACTION_ScrollLock_Hover, ImageDescriptor.createFromURL(url));
			url = bundle.getEntry(ImageConsts.IMAGE_DIR_ROOT + "full/" + ImageConsts.IMAGE_DIR_ELCL + "lock_co.gif"); //$NON-NLS-1$ //$NON-NLS-2$
			registry.put(ImageConsts.ACTION_ScrollLock_Enabled, ImageDescriptor.createFromURL(url));
			url = bundle.getEntry(ImageConsts.IMAGE_DIR_ROOT + "full/" + ImageConsts.IMAGE_DIR_DLCL + "lock_co.gif"); //$NON-NLS-1$ //$NON-NLS-2$
			registry.put(ImageConsts.ACTION_ScrollLock_Disabled, ImageDescriptor.createFromURL(url));

			url = bundle.getEntry(ImageConsts.IMAGE_DIR_ROOT + "full/" + ImageConsts.IMAGE_DIR_CLCL + "pin.gif"); //$NON-NLS-1$ //$NON-NLS-2$
			registry.put(ImageConsts.ACTION_PinTerminal_Hover, ImageDescriptor.createFromURL(url));
			url = bundle.getEntry(ImageConsts.IMAGE_DIR_ROOT + "full/" + ImageConsts.IMAGE_DIR_ELCL + "pin.gif"); //$NON-NLS-1$ //$NON-NLS-2$
			registry.put(ImageConsts.ACTION_PinTerminal_Enabled, ImageDescriptor.createFromURL(url));
			url = bundle.getEntry(ImageConsts.IMAGE_DIR_ROOT + "full/" + ImageConsts.IMAGE_DIR_DLCL + "pin.gif"); //$NON-NLS-1$ //$NON-NLS-2$
			registry.put(ImageConsts.ACTION_PinTerminal_Disabled, ImageDescriptor.createFromURL(url));
		}

		bundle = getBundle();
		URL url = bundle.getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_CLCL + "command_input_field.gif"); //$NON-NLS-1$
		registry.put(ImageConsts.ACTION_ToggleCommandField_Hover, ImageDescriptor.createFromURL(url));
		url = bundle.getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_ELCL + "command_input_field.gif"); //$NON-NLS-1$
		registry.put(ImageConsts.ACTION_ToggleCommandField_Enabled, ImageDescriptor.createFromURL(url));
		url = bundle.getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_DLCL + "command_input_field.gif"); //$NON-NLS-1$
		registry.put(ImageConsts.ACTION_ToggleCommandField_Disabled, ImageDescriptor.createFromURL(url));
	}

	/**
	 * Loads the image registered under the specified key from the image
	 * registry and returns the <code>Image</code> object instance.
	 *
	 * @param key The key the image is registered with.
	 * @return The <code>Image</code> object instance or <code>null</code>.
	 */
	public static Image getImage(String key) {
		return getDefault().getImageRegistry().get(key);
	}

	/**
	 * Loads the image registered under the specified key from the image
	 * registry and returns the <code>ImageDescriptor</code> object instance.
	 *
	 * @param key The key the image is registered with.
	 * @return The <code>ImageDescriptor</code> object instance or <code>null</code>.
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		return getDefault().getImageRegistry().getDescriptor(key);
	}
}
