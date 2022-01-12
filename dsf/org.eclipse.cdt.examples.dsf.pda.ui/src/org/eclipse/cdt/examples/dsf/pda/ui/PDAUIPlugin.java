/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     Wind River Systems - adopted to use with DSF
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.ui;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.pda.launch.PDALaunch;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PDAUIPlugin extends AbstractUIPlugin implements ILaunchesListener2 {

	public static String PLUGIN_ID = "org.eclipse.cdt.examples.dsf.pda.ui ";

	//The shared instance.
	private static PDAUIPlugin plugin;

	private static BundleContext fContext;

	private final static String ICONS_PATH = "icons/full/";//$NON-NLS-1$
	private final static String PATH_OBJECT = ICONS_PATH + "obj16/"; //Model object icons //$NON-NLS-1$

	/**
	 * PDA program image
	 */
	public final static String IMG_OBJ_PDA = "IMB_OBJ_PDA";

	/**
	 * Keyword color
	 */
	public final static RGB KEYWORD = new RGB(0, 0, 255);
	public final static RGB LABEL = new RGB(128, 128, 0);

	/**
	 * Managed colors
	 */
	private Map<RGB, Color> fColors = new HashMap<>();

	/**
	 * Active adapter sets.  They are accessed using the DSF session ID
	 * which owns the debug services.
	 */
	private Map<String, SessionAdapterSet> fSessionAdapterSets = Collections
			.synchronizedMap(new HashMap<String, SessionAdapterSet>());

	/**
	 * Map of launches for which adapter sets have already been disposed.
	 * This map (used as a set) is maintained in order to avoid re-creating an
	 * adapter set after the launch was removed from the launch manager, but
	 * while the launch is still being held by other classes which may
	 * request its adapters.  A weak map is used to avoid leaking
	 * memory once the launches are no longer referenced.
	 * <p>
	 * Access to this map is synchronized using the fSessionAdapterSets
	 * instance.
	 * </p>
	 */
	private Map<ILaunch, Object> fDisposedSessionAdapterSets = new WeakHashMap<>();

	private void disposeAdapterSet(PDALaunch launch) {
		String sessionId = launch.getSession().getId();
		synchronized (fSessionAdapterSets) {
			if (fSessionAdapterSets.containsKey(sessionId)) {
				fSessionAdapterSets.remove(sessionId).dispose();
				fDisposedSessionAdapterSets.put(launch, null);
			}
		}
	}

	/**
	 * The constructor.
	 */
	public PDAUIPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		fContext = context;
		super.start(context);
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		disposeAdapterSets();
		super.stop(context);
		plugin = null;
		fContext = null;
		for (Map.Entry<RGB, Color> entry : fColors.entrySet()) {
			entry.getValue().dispose();
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static PDAUIPlugin getDefault() {
		return plugin;
	}

	public static BundleContext getBundleContext() {
		return fContext;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		declareImage(IMG_OBJ_PDA, PATH_OBJECT + "pda.gif");
	}

	/**
	 * Declares a workbench image given the path of the image file (relative to
	 * the workbench plug-in). This is a helper method that creates the image
	 * descriptor and passes it to the main <code>declareImage</code> method.
	 *
	 * @param symbolicName the symbolic name of the image
	 * @param path the path of the image file relative to the base of the workbench
	 * plug-ins install directory
	 * <code>false</code> if this is not a shared image
	 */
	private void declareImage(String key, String path) {
		URL url = BundleUtility.find("org.eclipse.cdt.examples.dsf.pda.ui", path);
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		getImageRegistry().put(key, desc);
	}

	/**
	 * Returns the color described by the given RGB.
	 *
	 * @param rgb
	 * @return color
	 */
	public Color getColor(RGB rgb) {
		Color color = fColors.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColors.put(rgb, color);
		}
		return color;
	}

	SessionAdapterSet getAdapterSet(PDALaunch launch) {
		// Find the correct set of adapters based on the launch.  If not found
		// it means that we have a new launch, and we have to create a
		// new set of adapters.
		SessionAdapterSet adapterSet;
		synchronized (fSessionAdapterSets) {
			// The adapter set for the given launch was already disposed.
			// Return a null adapter.
			if (fDisposedSessionAdapterSets.containsKey(launch)) {
				return null;
			}
			String sessionId = launch.getSession().getId();
			adapterSet = fSessionAdapterSets.get(sessionId);
			if (adapterSet == null) {
				adapterSet = new SessionAdapterSet(launch);
				fSessionAdapterSets.put(sessionId, adapterSet);
			}
		}
		return adapterSet;
	}

	SessionAdapterSet getAdapterSet(String sessionId) {
		DsfSession session = DsfSession.getSession(sessionId);
		ILaunch launch = (ILaunch) session.getModelAdapter(ILaunch.class);
		if (launch instanceof PDALaunch) {
			return getAdapterSet((PDALaunch) launch);
		}
		return null;
	}

	/**
	 * Dispose adapter sets for all launches.
	 */
	private void disposeAdapterSets() {
		for (ILaunch launch : DebugPlugin.getDefault().getLaunchManager().getLaunches()) {
			if (launch instanceof PDALaunch) {
				disposeAdapterSet((PDALaunch) launch);
			}
		}
	}

	@Override
	public void launchesRemoved(ILaunch[] launches) {
		// Dispose the set of adapters for a launch only after the launch is
		// removed from the view.  If the launch is terminated, the adapters
		// are still needed to populate the contents of the view.
		for (ILaunch launch : launches) {
			if (launch instanceof PDALaunch) {
				disposeAdapterSet((PDALaunch) launch);
			}
		}
	}

	@Override
	public void launchesTerminated(ILaunch[] launches) {
	}

	@Override
	public void launchesAdded(ILaunch[] launches) {
	}

	@Override
	public void launchesChanged(ILaunch[] launches) {
	}

}
