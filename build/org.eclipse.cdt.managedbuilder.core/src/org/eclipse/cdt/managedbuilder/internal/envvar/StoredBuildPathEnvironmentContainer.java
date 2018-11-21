/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.envvar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.internal.core.envvar.EnvVarCollector;
import org.eclipse.cdt.internal.core.envvar.EnvVarDescriptor;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IEnvVarBuildPath;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.utils.envvar.StorableEnvironment;
import org.eclipse.cdt.utils.envvar.StorableEnvironmentLoader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.osgi.service.prefs.Preferences;

/**
 * This class holds the build path variable values and allows
 * checking the stored variable values with the values of the current environment environment
 *
 * @since 3.0
 *
 */
public class StoredBuildPathEnvironmentContainer extends StorableEnvironmentLoader {
	public static final String NODENAME = "environment"; //$NON-NLS-1$
	public static final String NODENAME_PREFIX_CFG = "buildEnvironment"; //$NON-NLS-1$
	public static final String NODENAME_CFG_INCLUDE = NODENAME_PREFIX_CFG + "Include"; //$NON-NLS-1$
	public static final String NODENAME_CFG_LIBRARY = NODENAME_PREFIX_CFG + "Library"; //$NON-NLS-1$

	private IConfiguration fConfiguration;
	private StorableEnvironment fEnvironment;
	private int fPathType;
	private boolean fIsVariableCaseSensitive = ManagedBuildManager.getEnvironmentVariableProvider()
			.isVariableCaseSensitive();

	public StoredBuildPathEnvironmentContainer(int pathType) {
		fPathType = pathType == IEnvVarBuildPath.BUILDPATH_LIBRARY ? IEnvVarBuildPath.BUILDPATH_LIBRARY
				: IEnvVarBuildPath.BUILDPATH_INCLUDE;
	}

	protected StorableEnvironment getEnvironment(Object context) {
		StorableEnvironment env = null;
		if (context instanceof IConfiguration) {
			if (fConfiguration != null && context == fConfiguration && fEnvironment != null)
				env = fEnvironment;
			else {
				env = loadEnvironment(context, false);
				if (env != null) {
					if (fConfiguration != null && fEnvironment != null) {
						try {
							storeEnvironment(fEnvironment, fConfiguration, false, false);
						} catch (CoreException e) {
						}
					}

					checkLoadedVarNames(env, context);

					fConfiguration = (IConfiguration) context;
					fEnvironment = env;
				}
			}
		}
		return env;
	}

	private boolean haveIdenticalValues(IEnvironmentVariable var1, IEnvironmentVariable var2) {
		if (var1 == null)
			return var2 == null || var2.getOperation() == IBuildEnvironmentVariable.ENVVAR_REMOVE;
		if (var2 == null)
			return var1 == null || var1.getOperation() == IBuildEnvironmentVariable.ENVVAR_REMOVE;
		int op1 = var1.getOperation();
		int op2 = var2.getOperation();
		if (op1 == IBuildEnvironmentVariable.ENVVAR_REMOVE || op2 == IBuildEnvironmentVariable.ENVVAR_REMOVE)
			return op1 == op2;

		return maskNull(var1.getValue()).equals(maskNull(var2.getValue()));
	}

	private String maskNull(String val) {
		return val == null ? "" : val; //$NON-NLS-1$
	}

	public boolean checkBuildPathChange(EnvVarCollector existingVariables, IConfiguration configuration) {
		StorableEnvironment env = getEnvironment(configuration);
		if (env == null)
			return false;
		IEnvironmentVariable vars[] = env.getVariables();
		for (int i = 0; i < vars.length; i++) {
			IEnvironmentVariable var = vars[i];
			String name = var.getName();
			EnvVarDescriptor des = existingVariables != null ? existingVariables.getVariable(name) : null;
			//			EnvironmentVariableProvider provider = ((EnvironmentVariableProvider)ManagedBuildManager.getEnvironmentVariableProvider());
			IEnvironmentVariable curVar = des/* != null ?
												provider.getVariable(des.getName(), configuration, true, false) : null*/;
			if (!haveIdenticalValues(var, curVar)) {
				if (curVar == null) {
					env.createVariable(name, null, IBuildEnvironmentVariable.ENVVAR_REMOVE, null);
				} else {
					env.createVariable(curVar.getName(), curVar.getValue(), curVar.getOperation(),
							curVar.getDelimiter());
				}
			}
		}
		boolean changed = env.isChanged();
		env.setChanged(false);
		if (changed && !configuration.isTemporary())
			try {
				storeEnvironment(env, configuration, false, false);
			} catch (CoreException e) {
			}
		return changed;
	}

	/*
	 * checks whether the variable of a given name is the build path variable
	 * for the given configuration
	 * If it is, than performs a check and returns true if the variable was changed
	 * If it is not the build path variable, no check is performed and this method always
	 * returns false in this case
	 */
	public boolean isVariableChanged(String name, IEnvironmentVariable variable, IConfiguration configuration) {
		StorableEnvironment env = getEnvironment(configuration);
		if (env == null)
			return false;
		IEnvironmentVariable var = env.getVariable(name);
		if (var == null)
			return false;

		//		EnvironmentVariableProvider provider = (EnvironmentVariableProvider)ManagedBuildManager.getEnvironmentVariableProvider();
		//		variable = provider.getVariable(name, configuration, true, false);

		if (haveIdenticalValues(var, variable))
			return false;

		return true;
	}

	/*
	 * synchronizes the stored variables with the ones passed to this method
	 */
	public void synchronize(EnvVarCollector existingVariables, IConfiguration configuration) {
		checkBuildPathChange(existingVariables, configuration);
	}

	private void checkLoadedVarNames(StorableEnvironment env, Object context) {
		String varNames[] = getBuildPathVarNames((IConfiguration) context, fPathType);
		for (int i = 0; i < varNames.length; i++) {
			String name = varNames[i];
			if (env.getVariable(name) == null)
				env.createVariable(name, null, IBuildEnvironmentVariable.ENVVAR_REMOVE, null);
		}

		IEnvironmentVariable vars[] = env.getVariables();
		for (int i = 0; i < vars.length; i++) {
			IEnvironmentVariable var = vars[i];
			boolean validVar = false;
			for (int j = 0; j < varNames.length; j++) {
				String varName = varNames[j];
				if (varNamesEqual(var.getName(), varName)) {
					validVar = true;
					break;
				}
			}
			if (!validVar) {
				env.deleteVariable(var.getName());
			}
		}
	}

	/*
	 * returns true if the variable names are equal and false otherwise
	 */
	private boolean varNamesEqual(String name1, String name2) {
		return fIsVariableCaseSensitive ? name1.equals(name2) : name1.equalsIgnoreCase(name2);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.StorableEnvironmentLoader#getSerializeInfo(java.lang.Object)
	 */
	@Override
	protected ISerializeInfo getSerializeInfo(Object context) {
		ISerializeInfo serializeInfo = null;
		if (context instanceof IConfiguration) {
			IConfiguration cfg = (IConfiguration) context;
			final Preferences prefs = getConfigurationNode(cfg);
			final String name = cfg.getId();
			if (prefs != null && name != null)
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

	public void serialize(boolean force) {
		if (fEnvironment != null) {
			try {
				storeEnvironment(fEnvironment, fConfiguration, force, true);
			} catch (CoreException e) {
			}
		}
	}

	private Preferences getConfigurationNode(IConfiguration cfg) {
		IProject project = (IProject) cfg.getOwner();
		if (project == null || !project.exists())
			return null;

		Preferences prefNode = new ProjectScope(project).getNode(ManagedBuilderCorePlugin.getUniqueIdentifier());
		if (prefNode == null)
			return null;

		prefNode = prefNode.node(NODENAME);
		if (prefNode == null)
			return null;

		if (fPathType == IEnvVarBuildPath.BUILDPATH_LIBRARY)
			return prefNode.node(NODENAME_CFG_LIBRARY);
		return prefNode.node(NODENAME_CFG_INCLUDE);
	}

	private String[] getBuildPathVarNames(IConfiguration configuration, int buildPathType) {
		ITool tools[] = configuration.getFilteredTools();
		List<String> list = new ArrayList<>();

		for (int i = 0; i < tools.length; i++) {
			IEnvVarBuildPath pathDescriptors[] = tools[i].getEnvVarBuildPaths();

			if (pathDescriptors == null || pathDescriptors.length == 0)
				continue;

			for (int j = 0; j < pathDescriptors.length; j++) {
				IEnvVarBuildPath curPathDes = pathDescriptors[j];
				if (curPathDes.getType() != buildPathType)
					continue;

				String vars[] = curPathDes.getVariableNames();
				if (vars == null || vars.length == 0)
					continue;

				list.addAll(Arrays.asList(vars));
			}
		}

		return list.toArray(new String[list.size()]);
	}
}
