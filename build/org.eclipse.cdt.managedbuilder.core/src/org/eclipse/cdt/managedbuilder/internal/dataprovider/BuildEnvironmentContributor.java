/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import org.eclipse.cdt.core.envvar.IEnvironmentContributor;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.internal.envvar.ExternalExtensionEnvironmentSupplier;
import org.eclipse.cdt.managedbuilder.internal.envvar.MbsEnvironmentSupplier;
import org.eclipse.cdt.utils.envvar.EnvironmentCollector;

public class BuildEnvironmentContributor implements IEnvironmentContributor {
	private BuildBuildData fBuildData;
	private IConfiguration fCfg;
	
	private MbsEnvironmentSupplier fMbsSupplier = new MbsEnvironmentSupplier();
	
	public BuildEnvironmentContributor(BuildBuildData buildData){
		fBuildData = buildData;
		fCfg = fBuildData.getBuilder().getParent().getParent();
	}

	public IEnvironmentVariable getVariable(String name,
			IEnvironmentVariableManager provider) {
		
		EnvironmentCollector collector = new EnvironmentCollector();
		IEnvironmentVariable result = null;
		ExternalExtensionEnvironmentSupplier extSupplier = new ExternalExtensionEnvironmentSupplier(provider);
		
		IEnvironmentVariable var = extSupplier.getVariable(name, fCfg.getManagedProject());
		if(var != null)
			result = collector.addVariable(var);

		var = fMbsSupplier.getVariable(name, fCfg);
		if(var != null)
			result = collector.addVariable(var);
		
		var = extSupplier.getVariable(name, fCfg);
		if(var != null)
			result = collector.addVariable(var);
		return result;
	}

	public IEnvironmentVariable[] getVariables(
			IEnvironmentVariableManager provider) {
		EnvironmentCollector collector = new EnvironmentCollector();
		ExternalExtensionEnvironmentSupplier extSupplier = new ExternalExtensionEnvironmentSupplier(provider);
		
		IEnvironmentVariable vars[] = extSupplier.getVariables(fCfg.getManagedProject());
		if(vars != null && vars.length != 0)
			collector.addVariables(vars);

		vars = fMbsSupplier.getVariables(fCfg);
		if(vars != null && vars.length != 0)
			collector.addVariables(vars);
		
		vars = extSupplier.getVariables(fCfg);
		if(vars != null && vars.length != 0)
			collector.addVariables(vars);
		return collector.getVariables();
	}

}
