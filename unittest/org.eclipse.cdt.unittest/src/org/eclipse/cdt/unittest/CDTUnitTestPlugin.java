/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.unittest;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * The plug-in runtime class for the JUnit core plug-in.
 */
public class CDTUnitTestPlugin extends Plugin {

	/**
	 * The single instance of this plug-in runtime class.
	 */
	private static CDTUnitTestPlugin fgPlugin = null;

	public static final String UNITTEST_PLUGIN_ID = "org.eclipse.unittest.ui"; //$NON-NLS-1$
	public static final String PLUGIN_ID = "org.eclipse.cdt.unittest"; //$NON-NLS-1$

	public static final String CDT_TEST_VIEW_SUPPORT_ID = "org.eclipse.cdt.unittest.loader"; //$NON-NLS-1$
	public static final String CDT_DSF_DBG_TEST_VIEW_SUPPORT_ID = "org.eclipse.cdt.unittest.loader"; //$NON-NLS-1$

	private BundleContext fBundleContext;

	public CDTUnitTestPlugin() {
		fgPlugin = this;
	}

	public static CDTUnitTestPlugin getDefault() {
		return fgPlugin;
	}

	public static String getPluginId() {
		return PLUGIN_ID;
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, "Error", e)); //$NON-NLS-1$
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		fBundleContext = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			InstanceScope.INSTANCE.getNode(CDTUnitTestPlugin.UNITTEST_PLUGIN_ID).flush();
		} finally {
			super.stop(context);
		}
		fBundleContext = null;
	}

	/**
	 * Returns a service with the specified name or <code>null</code> if none.
	 *
	 * @param serviceName name of service
	 * @return service object or <code>null</code> if none
	 */
	public Object getService(String serviceName) {
		ServiceReference<?> reference = fBundleContext.getServiceReference(serviceName);
		if (reference == null)
			return null;
		return fBundleContext.getService(reference);
	}

	/**
	 * Activates UnitTest Bundle. Eclipse uses lazy bundle loading by default, which
	 * means a bundle will not be loaded in many cases until some of its class is
	 * used. This method allows the clients to instantiate the Unit Test bundle in
	 * order to make it setup its launch listeners that are used to create and
	 * activate Unit Test View. The Unit Test client bundles must call this method
	 * before a Unit Test launch is created (preferably right before creation of the
	 * launch in order to not make Eclipse to load the Unit Test bundle when it is
	 * not really required), To load the Unit Test bundle the clients, for
	 * example, might call this method inside their
	 * 'ILaunchConfigurationDelegate2.getLaunch(ILaunchConfiguration, String)'
	 * method of their launch configuration implementation.
	 */
	public static void activateTestResultView() {
		Assert.isNotNull(Platform.getBundle("org.eclipse.unittest.ui")); //$NON-NLS-1$
		Display.getDefault().syncExec(() -> {
			try {
				IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IViewPart view = activeWindow.getActivePage().showView("org.eclipse.unittest.ui.ResultView"); //$NON-NLS-1$
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(view);
			} catch (PartInitException e) {
				log(e);
			}
		});
	}
}