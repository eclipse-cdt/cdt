/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
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
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.envvar.IEnvironmentContributor;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.envvar.ExternalExtensionEnvironmentSupplier;
import org.eclipse.cdt.managedbuilder.internal.envvar.MbsEnvironmentSupplier;
import org.eclipse.cdt.utils.envvar.EnvironmentCollector;

public class BuildEnvironmentContributor implements IEnvironmentContributor {
	private BuildBuildData fBuildData;
	private IConfiguration fCfg;
	private ICConfigurationDescription fCfgDes;

	private MbsEnvironmentSupplier fMbsSupplier = new MbsEnvironmentSupplier();

	public BuildEnvironmentContributor(BuildBuildData buildData) {
		fBuildData = buildData;
		fCfg = fBuildData.getBuilder().getParent().getParent();
		fCfgDes = ManagedBuildManager.getDescriptionForConfiguration(fCfg);
	}

	@Override
	public IEnvironmentVariable getVariable(String name, IEnvironmentVariableManager provider) {

		EnvironmentCollector collector = new EnvironmentCollector();
		ExternalExtensionEnvironmentSupplier extSupplier = new ExternalExtensionEnvironmentSupplier(provider);

		boolean varFound = false;

		IEnvironmentVariable var = extSupplier.getVariable(name, fCfg.getManagedProject());
		varFound = processVariable(name, var, collector, provider, varFound);

		var = fMbsSupplier.getVariable(name, fCfg);
		varFound = processVariable(name, var, collector, provider, varFound);

		var = extSupplier.getVariable(name, fCfg);
		varFound = processVariable(name, var, collector, provider, varFound);

		return collector.getVariable(name);
	}

	@Override
	public IEnvironmentVariable[] getVariables(IEnvironmentVariableManager provider) {
		EnvironmentCollector collector = new EnvironmentCollector();
		ExternalExtensionEnvironmentSupplier extSupplier = new ExternalExtensionEnvironmentSupplier(provider);

		Set<String> set = null;

		IEnvironmentVariable vars[] = extSupplier.getVariables(fCfg.getManagedProject());
		set = processVariables(vars, collector, provider, set);

		vars = fMbsSupplier.getVariables(fCfg);
		set = processVariables(vars, collector, provider, set);

		vars = extSupplier.getVariables(fCfg);
		set = processVariables(vars, collector, provider, set);

		return collector.getVariables();
	}

	private boolean processVariable(String name, IEnvironmentVariable var, EnvironmentCollector collector,
			IEnvironmentVariableManager provider, boolean varFound) {
		if (var != null) {
			if (!varFound) {
				varFound = true;
				IEnvironmentVariable base = provider.getVariable(name, fCfgDes, false);
				if (base != null)
					collector.addVariable(base);
			}
			collector.addVariable(var);
		}

		return varFound;
	}

	private Set<String> processVariables(IEnvironmentVariable vars[], EnvironmentCollector collector,
			IEnvironmentVariableManager provider, Set<String> set) {
		boolean checkSet = true;
		if (vars != null && vars.length != 0) {
			if (set == null) {
				set = new HashSet<>();
				checkSet = false;
			}

			for (int i = 0; i < vars.length; i++) {
				if (vars[i] == null)
					continue;

				if (set.add(vars[i].getName()) || !checkSet) {
					IEnvironmentVariable base = provider.getVariable(vars[i].getName(), fCfgDes, false);
					if (base != null) {
						collector.addVariable(base);
					}
				}

				collector.addVariable(vars[i]);
			}
			//collector.addVariables(vars);
		}

		return set;
	}

}
