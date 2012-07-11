/*******************************************************************************
 * Copyright (c) 2012, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.envvar;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.cdtvariables.DefaultVariableContextInfo;
import org.eclipse.cdt.internal.core.cdtvariables.EnvironmentVariableSupplier;
import org.eclipse.cdt.internal.core.cdtvariables.ICoreVariableContextInfo;
import org.eclipse.cdt.internal.core.envvar.DefaultEnvironmentContextInfo;
import org.eclipse.cdt.internal.core.envvar.EnvVarDescriptor;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.internal.core.envvar.ICoreEnvironmentVariableSupplier;
import org.eclipse.cdt.internal.core.envvar.IEnvironmentContextInfo;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.utils.cdtvariables.ICdtVariableSupplier;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;


/**
 * Helper class to resolve environment variables directly from toolchain. The intention is
 * to use that in New Project Wizard scenarios when no configuration is available yet.
 */
public class EnvironmentVariableManagerToolChain extends EnvironmentVariableManager {
	private static EnvironmentVariableManagerToolChain fInstance = null;

	/**
	 * Basically, converter from IEnvironmentVariable to ICdtVariable (build macros) which
	 * is used by EnvironmentVariableManager implementation to resolve variables/macros.
	 */
	private final class CoreVariableContextInfoToolChain implements ICoreVariableContextInfo {
		public final static int CONTEXT_TOOLCHAIN = 1009; // arbitrary value different from ICoreVariableContextInfo.CONTEXT_XXX
		
		private final IToolChain toolChain;
		private final IConfigurationEnvironmentVariableSupplier mbsSupplier;

		private CoreVariableContextInfoToolChain(IToolChain toolChain) {
			this.toolChain = toolChain;
			this.mbsSupplier = toolChain.getEnvironmentVariableSupplier();
		}

		@Override
		public ICdtVariableSupplier[] getSuppliers() {
			ICdtVariableSupplier sup = new ICdtVariableSupplier() {
				@Override
				public ICdtVariable getVariable(String macroName, IVariableContextInfo context) {
					IEnvironmentVariable var = mbsSupplier.getVariable(macroName, null, ManagedBuildManager.getEnvironmentVariableProvider());
					return EnvironmentVariableSupplier.getInstance().createBuildMacro(var);
				}
				@Override
				public ICdtVariable[] getVariables(IVariableContextInfo context) {
					IEnvironmentVariable[] vars = mbsSupplier.getVariables(null, ManagedBuildManager.getEnvironmentVariableProvider());
					if (vars != null) {
						ICdtVariable[] cdtVars = new ICdtVariable[vars.length];
						for (int i = 0; i < vars.length; i++) {
							cdtVars[i] = EnvironmentVariableSupplier.getInstance().createBuildMacro(vars[i]);
						}
					}
					return null;
				}
				
			};
			return new ICdtVariableSupplier[] { sup };
		}

		@Override
		public IVariableContextInfo getNext() {
			return new DefaultVariableContextInfo(ICoreVariableContextInfo.CONTEXT_WORKSPACE, null);
		}

		@Override
		public int getContextType() {
			return CONTEXT_TOOLCHAIN;
		}

		@Override
		public Object getContextData() {
			return toolChain;
		}
	}

	private final class EnvironmentContextInfoToolChain implements IEnvironmentContextInfo {
		private final IToolChain toolChain;

		private EnvironmentContextInfoToolChain(IToolChain toolChain) {
			this.toolChain = toolChain;
		}

		@Override
		public IEnvironmentContextInfo getNext() {
			return new DefaultEnvironmentContextInfo(null);
		}

		@Override
		public ICoreEnvironmentVariableSupplier[] getSuppliers() {
			final IConfigurationEnvironmentVariableSupplier cevSupplier = toolChain.getEnvironmentVariableSupplier();
			
			ICoreEnvironmentVariableSupplier toolchainSupplier = new ICoreEnvironmentVariableSupplier() {
				@Override
				public IEnvironmentVariable getVariable(String name, Object context) {
					IEnvironmentVariableProvider provider = ManagedBuildManager.getEnvironmentVariableProvider();
					return cevSupplier.getVariable(name, null, provider);
				}
				@Override
				public IEnvironmentVariable[] getVariables(Object context) {
					return cevSupplier.getVariables(null, ManagedBuildManager.getEnvironmentVariableProvider());
				}
				@Override
				public boolean appendEnvironment(Object context) {
					// Arbitrary value, it did not appear being used in tested scenarios
					return false;
				}
			};
			return new ICoreEnvironmentVariableSupplier[] { EnvironmentVariableManagerToolChain.fUserSupplier, toolchainSupplier };
		}

		@Override
		public Object getContext() {
			return toolChain;
		}
	}

	public static EnvironmentVariableManagerToolChain getDefault() {
		if (fInstance == null)
			fInstance = new EnvironmentVariableManagerToolChain();
		return fInstance;
	}

	@Override
	public IEnvironmentContextInfo getContextInfo(Object level) {
		if (level instanceof IToolChain) {
			return new EnvironmentContextInfoToolChain((IToolChain) level);
		}

		return super.getContextInfo(level);
	}

	@Override
	protected int getMacroContextTypeFromContext(Object context) {
		if (context instanceof IToolChain) {
			return CoreVariableContextInfoToolChain.CONTEXT_TOOLCHAIN;
		}
		
		return super.getMacroContextTypeFromContext(context);
	}

	@Override
	public ICoreVariableContextInfo getMacroContextInfoForContext(Object context) {
		if (context instanceof IToolChain) {
			return new CoreVariableContextInfoToolChain((IToolChain) context);
		}

		return super.getMacroContextInfoForContext(context);
	}

	/**
	 * Get environment variable value from toolchain definition.
	 *
	 * @param name - name of the variable.
	 * @param toolChain - toolchain.
	 * @param resolveMacros - {@code true} to expand macros, {@code false} otherwise.
	 *
	 * @return value of the variable.
	 */
	public IEnvironmentVariable getVariable(String name, IToolChain toolChain, boolean resolveMacros) {
		if (name == null || name.isEmpty())
			return null;

		IEnvironmentContextInfo info = getContextInfo(toolChain);
		EnvVarDescriptor var = EnvironmentVariableManagerToolChain.getVariable(name,info,true);

		if (var != null && var.getOperation() != IEnvironmentVariable.ENVVAR_REMOVE) {
			return resolveMacros ? calculateResolvedVariable(var,info) : var;
		}
		return null;
	}

	/**
	 * Get environment variable value resolved in context of configuration.
	 * If no configuration available use toolchain definition.
	 *
	 * @param name - name of the variable.
	 * @param toolChain - toolchain.
	 * @param resolveMacros - {@code true} to expand macros, {@code false} otherwise.
	 *
	 * @return value of the variable.
	 */
	public String getVariableInConfigurationContext(String name, IToolChain toolChain, boolean resolveMacros) {
		if (toolChain == null) {
			return null;
		}

		IConfiguration cfg = toolChain.getParent();
		ICConfigurationDescription cfgDescription = cfg != null ? ManagedBuildManager.getDescriptionForConfiguration(cfg) : null;

		IEnvironmentVariable var = null;
		if (cfgDescription != null) {
			var = getVariable(name, cfgDescription, resolveMacros);
		} else {
			var = getVariable(name, toolChain, resolveMacros);
		}

		String value = var != null ? var.getValue() : null;
		return value;
	}

}
