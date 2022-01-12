/*******************************************************************************
 * Copyright (c) 2005, 2013 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *    James Blackburn (Broadcom Corp.)
 *    IBM Corporation
 *    Andrew Gvozdev     - Notification mechanism for changes to environment variables
 *******************************************************************************/
package org.eclipse.cdt.internal.core.envvar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationSpecSettings;
import org.eclipse.cdt.internal.core.settings.model.IInternalCCfgInfo;
import org.eclipse.cdt.utils.envvar.EnvVarOperationProcessor;
import org.eclipse.cdt.utils.envvar.IEnvironmentChangeListener;
import org.eclipse.cdt.utils.envvar.PrefsStorableEnvironment;
import org.eclipse.cdt.utils.envvar.StorableEnvironment;
import org.eclipse.cdt.utils.envvar.StorableEnvironmentLoader;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * This is the Environment Variable Supplier used to supply and persist user
 * defined variables.  Variables are stored in the context of a CDT {@link ICConfigurationDescription},
 * or, globally at the {@link IWorkspace} level.
 *
 * <p>
 * This class is Singleton held by {@link EnvironmentVariableManager}.
 *
 * <p>
 * It also allows temporary 'overriding' of variables. These are not persisted, but override
 * the values of any existing user-defined variable. This functionality is used by HeadlessBuilder
 * to temporarily override environment variables on the command line.
 *
 * @since 3.0
 */
public class UserDefinedEnvironmentSupplier extends StorableEnvironmentLoader
		implements ICoreEnvironmentVariableSupplier {
	public static final String NODENAME = "environment"; //$NON-NLS-1$
	public static final String PREFNAME_WORKSPACE = "workspace"; //$NON-NLS-1$
	public static final String PREFNAME_PROJECT = "project"; //$NON-NLS-1$
	public static final String NODENAME_CFG = "project"; //$NON-NLS-1$

	private PrefsStorableEnvironment fWorkspaceVariables;
	private StorableEnvironment fOverrideVariables = new StorableEnvironment(false);

	public StorableEnvironment getEnvironment(Object context) {
		return getEnvironment(context, false);
	}

	protected StorableEnvironment getEnvironment(Object context, boolean forceLoad) {
		StorableEnvironment env = null;
		if (context instanceof IInternalCCfgInfo) {
			try {
				CConfigurationSpecSettings settings = ((IInternalCCfgInfo) context).getSpecSettings();
				env = settings.getEnvironment();
				if (env == null || forceLoad) {
					env = loadEnvironment(context, settings.isReadOnly());
					settings.setEnvironment(env);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		} else if (context == null || context instanceof IBuildConfiguration || context instanceof IWorkspace) {
			if (fWorkspaceVariables == null || forceLoad)
				fWorkspaceVariables = (PrefsStorableEnvironment) loadEnvironment(context, false);
			env = fWorkspaceVariables;
		}

		return env;
	}

	@Override
	protected ISerializeInfo getSerializeInfo(Object context) {
		ISerializeInfo serializeInfo = null;

		if (context instanceof ICConfigurationDescription) {
			final ICConfigurationDescription cfg = (ICConfigurationDescription) context;
			final String name = cfg.getId();
			if (name != null)
				serializeInfo = new ISerializeInfo() {
					@Override
					public Preferences getNode() {
						return getConfigurationNode(cfg.getProjectDescription());
					}

					@Override
					public String getPrefName() {
						return name;
					}
				};
		} else if (context == null || context instanceof IWorkspace) {
			final Preferences prefs = getWorkspaceNode();
			final String name = PREFNAME_WORKSPACE;
			if (prefs != null)
				serializeInfo = new ISerializeInfo() {
					@Override
					public Preferences getNode() {
						return prefs;
					}

					@Override
					public String getPrefName() {
						return name;
					}
				};
		}
		return serializeInfo;
	}

	private Preferences getConfigurationNode(ICProjectDescription projDes) {
		Preferences prefNode = getProjectNode(projDes);
		if (prefNode == null)
			return null;

		return prefNode.node(NODENAME_CFG);
	}

	private Preferences getProjectNode(ICProjectDescription projDes) {
		if (projDes == null)
			return null;
		IProject project = projDes.getProject();
		if (!project.exists())
			return null;

		Preferences prefNode = new ProjectScope(project).getNode(CCorePlugin.PLUGIN_ID);
		if (prefNode == null)
			return null;

		return prefNode.node(NODENAME);
	}

	private Preferences getWorkspaceNode() {
		Preferences prefNode = InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID);
		if (prefNode == null)
			return null;

		return prefNode.node(NODENAME);
	}

	public void checkInexistentConfigurations(ICProjectDescription projDes) {
		Preferences prefNode = getConfigurationNode(projDes);
		if (prefNode == null)
			return;

		try {
			String ids[] = prefNode.keys();
			boolean found = false;
			for (String id : ids) {
				if (projDes.getConfigurationById(id) == null) {
					prefNode.remove(id);
					found = true;
				}
			}

			if (found)
				prefNode.flush();
		} catch (BackingStoreException e) {
		}
	}

	public void storeWorkspaceEnvironment(boolean force) {
		if (fWorkspaceVariables != null) {
			try {
				storeEnvironment(fWorkspaceVariables, ResourcesPlugin.getWorkspace(), force, true);
			} catch (CoreException e) {

			}
		}
	}

	public StorableEnvironment getWorkspaceEnvironmentCopy() {
		StorableEnvironment envVar = getEnvironment(null);
		return new StorableEnvironment(envVar, false);
	}

	public boolean setWorkspaceEnvironment(StorableEnvironment env) {
		IEnvironmentVariable[] oldVariables = fWorkspaceVariables.getVariables();
		IEnvironmentVariable[] newVariables = env.getVariables();

		fWorkspaceVariables.deleteAll();
		fWorkspaceVariables.setVariales(newVariables);
		fWorkspaceVariables.setAppendEnvironment(env.appendEnvironment());
		fWorkspaceVariables.setAppendContributedEnvironment(env.appendContributedEnvironment());

		storeWorkspaceEnvironment(true);

		return !Arrays.equals(oldVariables, newVariables);
	}

	public void storeProjectEnvironment(ICProjectDescription des, boolean force) {
		ICConfigurationDescription cfgs[] = des.getConfigurations();
		for (ICConfigurationDescription cfg : cfgs) {
			storeEnvironment(cfg, force, false);
		}

		Preferences node = getProjectNode(des);
		try {
			node.flush();
		} catch (BackingStoreException e) {
		}
	}

	private void storeEnvironment(Object context, boolean force, boolean flush) {
		StorableEnvironment env = getEnvironment(context, false);
		if (env != null) {
			try {
				storeEnvironment(env, context, force, flush);
			} catch (CoreException e) {
			}
		}
	}

	@Override
	public IEnvironmentVariable getVariable(String name, Object context) {
		if (getValidName(name) == null)
			return null;
		IEnvironmentVariable var = fOverrideVariables.getVariable(name);
		StorableEnvironment env = getEnvironment(context);
		if (env == null)
			return var;
		return EnvVarOperationProcessor.performOperation(env.getVariable(name), var);
	}

	@Override
	public IEnvironmentVariable[] getVariables(Object context) {
		StorableEnvironment env = getEnvironment(context);
		if (env == null)
			return null;
		IEnvironmentVariable[] override = filterVariables(fOverrideVariables.getVariables());
		IEnvironmentVariable[] normal = filterVariables(env.getVariables());
		return combineVariables(normal, override);
	}

	private IEnvironmentVariable[] combineVariables(IEnvironmentVariable[] oldVariables,
			IEnvironmentVariable[] newVariables) {
		Map<String, IEnvironmentVariable> vars = new HashMap<>(oldVariables.length + newVariables.length);
		for (IEnvironmentVariable variable : oldVariables)
			vars.put(variable.getName(), variable);
		for (IEnvironmentVariable variable : newVariables) {
			if (!vars.containsKey(variable.getName()))
				vars.put(variable.getName(), variable);
			else
				vars.put(variable.getName(),
						EnvVarOperationProcessor.performOperation(vars.get(variable.getName()), variable));
		}
		return vars.values().toArray(new IEnvironmentVariable[vars.size()]);
	}

	/**
	 * Add an environment variable 'override'. This variable won't be persisted but will instead
	 * replace / remove / prepend / append any existing environment variable with the same name.
	 * This change is not persisted and remains for the current eclipse session.
	 *
	 * @param name Environment variable name
	 * @param value Environment variable value
	 * @param op one of the IBuildEnvironmentVariable.ENVVAR_* operation types
	 * @param delimiter delimiter to use or null for default
	 * @return Overriding IEnvironmentVariable or null if name is not valid
	 */
	public IEnvironmentVariable createOverrideVariable(String name, String value, int op, String delimiter) {
		if (getValidName(name) == null)
			return null;
		return fOverrideVariables.createVariable(name, value, op, delimiter);
	}

	public IEnvironmentVariable createVariable(String name, String value, int op, String delimiter, Object context) {
		if (getValidName(name) == null)
			return null;
		StorableEnvironment env = getEnvironment(context);
		if (env == null)
			return null;
		IEnvironmentVariable var = env.createVariable(name, value, op, delimiter);
		if (env.isChanged()) {
			env.setChanged(false);
		}
		return var;
	}

	public IEnvironmentVariable deleteVariable(String name, Object context) {
		StorableEnvironment env = getEnvironment(context);
		if (env == null)
			return null;
		IEnvironmentVariable var = env.deleteVariable(name);
		return var;
	}

	public void deleteAll(Object context) {
		StorableEnvironment env = getEnvironment(context);
		if (env == null)
			return;

		env.deleteAll();
	}

	public void setVariables(IEnvironmentVariable vars[], Object context) {
		StorableEnvironment env = getEnvironment(context);
		if (env == null)
			return;

		env.setVariales(vars);
		if (env.isChanged()) {
			env.setChanged(false);
		}
	}

	protected String getValidName(String name) {
		if (name == null || (name = name.trim()).length() == 0)
			return null;

		return name;
	}

	protected IEnvironmentVariable[] filterVariables(IEnvironmentVariable variables[]) {
		return EnvVarOperationProcessor.filterVariables(variables, null);
	}

	@Override
	public boolean appendEnvironment(Object context) {
		StorableEnvironment env = getEnvironment(context);
		if (env == null)
			return true;
		return env.appendEnvironment();
	}

	public boolean appendContributedEnvironment(Object context) {
		StorableEnvironment env = getEnvironment(context);
		if (env == null)
			return true;
		return env.appendContributedEnvironment();
	}

	public void setAppendEnvironment(boolean append, Object context) {
		StorableEnvironment env = getEnvironment(context);
		if (env != null) {
			env.setAppendEnvironment(append);
		}
	}

	public void setAppendContributedEnvironment(boolean append, Object context) {
		StorableEnvironment env = getEnvironment(context);
		if (env != null) {
			env.setAppendContributedEnvironment(append);
		}
	}

	public void restoreDefaults(Object context) {
		StorableEnvironment env = getEnvironment(context);
		if (env != null) {
			env.restoreDefaults();
		}
	}

	/**
	 * Adds a listener that will be notified of changes in environment variables.
	 *
	 * @param listener - the listener to add
	 * @since 5.5
	 */
	public void registerEnvironmentChangeListener(IEnvironmentChangeListener listener) {
		if (fWorkspaceVariables == null) {
			getEnvironment(null, false);
		}
		if (fWorkspaceVariables != null) {
			fWorkspaceVariables.registerEnvironmentChangeListener(listener);
		}
	}

	/**
	 * Removes an environment variables change listener.
	 *
	 * @param listener - the listener to remove.
	 * @since 5.5
	 */
	public void unregisterEnvironmentChangeListener(IEnvironmentChangeListener listener) {
		if (fWorkspaceVariables != null) {
			fWorkspaceVariables.unregisterEnvironmentChangeListener(listener);
		}
	}
}
