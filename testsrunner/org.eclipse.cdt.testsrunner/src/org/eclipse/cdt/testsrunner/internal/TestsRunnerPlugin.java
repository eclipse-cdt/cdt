/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal;

import java.net.URL;
import java.util.HashSet;

import org.eclipse.cdt.testsrunner.internal.launcher.TestsRunnerProvidersManager;
import org.eclipse.cdt.testsrunner.internal.model.TestingSessionsManager;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TestsRunnerPlugin extends AbstractUIPlugin {

	/** The plug-in ID. */
	private static final String PLUGIN_ID = "org.eclipse.cdt.testsrunner"; //$NON-NLS-1$

	/** The path to the plugin icons */
	private static final IPath ICONS_PATH = new Path("$nl$/icons"); //$NON-NLS-1$

	/** Plug-in instance. */
	private static TestsRunnerPlugin plugin;

	private TestsRunnerProvidersManager testsRunnerProvidersManager = new TestsRunnerProvidersManager();
	private TestingSessionsManager testingSessionsManager = new TestingSessionsManager(testsRunnerProvidersManager);

	public TestsRunnerPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the Tests Runner Plug-in instance
	 *
	 * @return the plug-in instance
	 */
	public static TestsRunnerPlugin getDefault() {
		return plugin;
	}

	/** Convenience method which returns the unique identifier of this plug-in. */
	public static String getUniqueIdentifier() {
		return PLUGIN_ID;
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
	 * Logs an internal error with the specified message.
	 *
	 * @param message the error message to log
	 */
	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}

	/**
	 * Logs an internal error with the specified throwable
	 *
	 * @param e the exception to be logged
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		setDefaultLaunchDelegates();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Access the plugin's Testing Sessions Manager instance.
	 *
	 * @return sessions manager
	 */
	public TestingSessionsManager getTestingSessionsManager() {
		return testingSessionsManager;
	}

	/**
	 * Access the plugin's Test Runners manager instance.
	 *
	 * @return tests runners manager
	 */
	public TestsRunnerProvidersManager getTestsRunnerProvidersManager() {
		return testsRunnerProvidersManager;
	}

	/**
	 * Returns the descriptor for image with <code>relativePath</code> path.
	 *
	 * @param relativePath path relative to <code>ICONS_PATH</code>
	 * @return image descriptor
	 */
	static public ImageDescriptor getImageDescriptor(String relativePath) {
		return getDefault().getImageDescriptorImpl(relativePath);
	}

	/**
	 * Returns the descriptor for image with <code>relativePath</code> path.
	 *
	 * @param relativePath path relative to <code>ICONS_PATH</code>
	 * @return image descriptor
	 */
	private ImageDescriptor getImageDescriptorImpl(String relativePath) {
		IPath path = ICONS_PATH.append(relativePath);
		return createImageDescriptor(getDefault().getBundle(), path, true);
	}

	/**
	 * Returns the image with the specified path.
	 *
	 * @param path path
	 * @return image image
	 */
	public static Image createAutoImage(String path) {
		return getDefault().createAutoImageImpl(path);
	}

	/**
	 * Returns the image with the specified path.
	 *
	 * @param path path
	 * @return image image
	 */
	private Image createAutoImageImpl(String path) {
		Image image = getImageRegistry().get(path);
		if (image == null) {
			image = getImageDescriptor(path).createImage();
			getImageRegistry().put(path, image);
		}
		return image;
	}

	/**
	 * Creates an image descriptor for the given path in a bundle. The path can
	 * contain variables like $NL$. If no image could be found,
	 * <code>useMissingImageDescriptor</code> decides if either the 'missing
	 * image descriptor' is returned or <code>null</code>.
	 *
	 * @param bundle a bundle
	 * @param path path in the bundle
	 * @param useMissingImageDescriptor if <code>true</code>, returns the shared
	 * image descriptor for a missing image. Otherwise, returns
	 * <code>null</code> if the image could not be found
	 * @return an {@link ImageDescriptor}, or <code>null</code> iff there's no
	 * image at the given location and <code>useMissingImageDescriptor</code> is
	 * <code>true</code>
	 */
	private ImageDescriptor createImageDescriptor(Bundle bundle, IPath path, boolean useMissingImageDescriptor) {
		URL url = FileLocator.find(bundle, path, null);
		if (url != null) {
			return ImageDescriptor.createFromURL(url);
		}
		if (useMissingImageDescriptor) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
		return null;
	}

	/**
	 * Setup the launch delegate with id <code>delegateId</code> as default for
	 * launch configuration type <code>cfgType</code> for <code>mode</code>
	 * launch mode.
	 *
	 * @param cfgType launch configuration type
	 * @param delegateId unique identifier of the launch delegate
	 * @param mode launch mode
	 */
	private void setDefaultLaunchDelegate(ILaunchConfigurationType cfgType, String delegateId, String mode) {
		HashSet<String> modes = new HashSet<>();
		modes.add(mode);
		try {
			if (cfgType.getPreferredDelegate(modes) == null) {
				ILaunchDelegate[] delegates = cfgType.getDelegates(modes);
				for (ILaunchDelegate delegate : delegates) {
					if (delegateId.equals(delegate.getId())) {
						cfgType.setPreferredDelegate(modes, delegate);
						break;
					}
				}
			}
		} catch (CoreException e) {
			log(e);
		}
	}

	/**
	 * Sets up the default launch delegates for the Tests Runner's launch configuration type.
	 */
	private void setDefaultLaunchDelegates() {
		ILaunchManager launchMgr = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType configurationType = launchMgr
				.getLaunchConfigurationType(ITestsRunnerConstants.LAUNCH_CONFIGURATION_TYPE_ID);

		setDefaultLaunchDelegate(configurationType, ITestsRunnerConstants.PREFERRED_DEBUG_TESTS_LAUNCH_DELEGATE,
				ILaunchManager.DEBUG_MODE);
		setDefaultLaunchDelegate(configurationType, ITestsRunnerConstants.PREFERRED_RUN_TESTS_LAUNCH_DELEGATE,
				ILaunchManager.RUN_MODE);
	}

}
