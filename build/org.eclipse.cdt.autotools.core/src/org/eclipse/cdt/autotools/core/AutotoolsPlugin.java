/*******************************************************************************
 * Copyright (c) 2006, 2015 Red Hat Inc..
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.core;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.internal.autotools.core.configure.AutotoolsConfigurationManager;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The main plugin class to be used in the desktop.
 */

public class AutotoolsPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static AutotoolsPlugin plugin;
	private ResourceBundle resourceBundle;

	public static final String PLUGIN_ID = "org.eclipse.cdt.autotools.core"; //$NON-NLS-1$
	public static final String AUTOTOOLS_PROJECT_TYPE_ID = "org.eclipse.linuxtools.cdt.autotools.core.projectType"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public AutotoolsPlugin() {
		Assert.isTrue(plugin == null);
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle(PLUGIN_ID + ".Resources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}

	}

	public static String getPluginId() {
		return PLUGIN_ID;
	}

	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static AutotoolsPlugin getDefault() {
		return plugin;
	}

	/**
	 * Return the OSGi service with the given service interface.
	 *
	 * @param service service interface
	 * @return the specified service or null if it's not registered
	 * @since 1.5
	 */
	public static <T> T getService(Class<T> service) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 *
	 * @param key the message key
	 * @return the resource bundle message
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = AutotoolsPlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 *
	 * @param key the message key
	 * @param args an array of substituition strings
	 * @return the resource bundle message
	 */
	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), (Object[]) args);
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public static boolean hasTargetBuilder(IProject project) {
		try {
			// When a project is converted to an Autotools project, we
			// replace the ManagedMake builder with a special one that
			// handles MakeTargets.  If a project is brought into Eclipse and
			// uses the New Project Wizard to create a ManagedMake project that
			// is of type: Autotools, this added step is not done.  If we know
			// we have an Autotools project from the configuration id, then
			// we should add the builder now.  We also should replace the
			// default ManagedMake scanner provider with the Autotools one,
			// then return true.
			if (project.getNature(ManagedCProjectNature.MNG_NATURE_ID) != null) {
				IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
				IManagedProject m = info.getManagedProject();
				if (m != null && m.getProjectType().getId().equals(AUTOTOOLS_PROJECT_TYPE_ID)) {
					AutotoolsNewProjectNature.addAutotoolsBuilder(project, new NullProgressMonitor());
					return true;
				}
			}
		} catch (Exception f) {
			// Don't care...fall through to not found.
		}
		// Otherwise not found.
		return false;
	}

	public static void log(IStatus status) {
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}

	public static void log(Throwable e) {
		if (e instanceof InvocationTargetException)
			e = ((InvocationTargetException) e).getTargetException();
		IStatus status = null;
		if (e instanceof CoreException)
			status = ((CoreException) e).getStatus();
		else
			status = new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.OK, e.getMessage(), e);
		log(status);
	}

	/**
	 * Return set of Autotool configuration options for a given build configuration id.
	 *
	 * @param project existing autotools project
	 * @param cfgId configuration id
	 * @return a copy of Autotools configurations for the given configuration id
	 * @throws CoreException if project is not valid Autotools project or cfgId does not exist
	 * @since 1.2
	 */
	public Map<String, IAutotoolsOption> getAutotoolCfgOptions(IProject project, String cfgId) throws CoreException {
		return AutotoolsConfigurationManager.getInstance().getAutotoolsCfgOptions(project, cfgId);
	}

	/**
	 * Update Autotool configuration options for a specified build configuration
	 *
	 * @param project existing autotools project
	 * @param cfgId configuation id
	 * @param options set of updated Autotool configuration options
	 * @throws CoreException if project is not valid Autotools project or cfgId does not exist
	 *
	 * @since 1.2
	 */
	public void updateAutotoolCfgOptions(IProject project, String cfgId, Map<String, IAutotoolsOption> options)
			throws CoreException {
		AutotoolsConfigurationManager.getInstance().updateAutotoolCfgOptions(project, cfgId, options);

	}
}
