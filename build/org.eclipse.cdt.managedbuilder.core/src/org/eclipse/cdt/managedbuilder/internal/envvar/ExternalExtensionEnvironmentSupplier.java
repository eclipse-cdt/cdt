/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.envvar;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IProjectEnvironmentVariableSupplier;
import org.eclipse.cdt.utils.envvar.EnvVarOperationProcessor;

/**
 * This is the Environment Variable Supplier used to supply variables
 * defined by the tool-integrator
 *
 * @since 3.0
 */
public class ExternalExtensionEnvironmentSupplier implements
		IEnvironmentVariableSupplier {
	private EnvironmentVariableProvider fProvider;
	private static final String fNonOverloadableVariables[] = new String[]{
			//tool-integrators not allowed currently to override the "CWD" and "PWD" variables
			EnvVarOperationProcessor.normalizeName("CWD"),   //$NON-NLS-1$
			EnvVarOperationProcessor.normalizeName("PWD")	  //$NON-NLS-1$
		};

	/**
	 * EnvironmentVariableProvider passed to the tool-integrator provided
	 * suppliers.
	 * Accepts only contexts lower than the one passed to a suppler
	 *
	 * @since 3.0
	 */

	public ExternalExtensionEnvironmentSupplier(IEnvironmentVariableManager mngr){
		fProvider = new EnvironmentVariableProvider(mngr);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariable()
	 */
	@Override
	public IEnvironmentVariable getVariable(String name, Object context) {
		if(context == null)
			return null;
		if((name = getValidName(name)) == null)
			return null;

		else if(context instanceof IConfiguration){
			IConfiguration cfg = (IConfiguration)context;
			IConfigurationEnvironmentVariableSupplier supplier = cfg.getEnvironmentVariableSupplier();
			if(supplier == null)
				return null;
			return supplier.getVariable(name,cfg,fProvider);
		}
		else if (context instanceof IManagedProject) {
			IManagedProject project = (IManagedProject)context;
			IProjectType pType = project.getProjectType();
			IProjectEnvironmentVariableSupplier supplier = pType != null ?
					pType.getEnvironmentVariableSupplier() : null;
			if(supplier == null)
				return null;
			return supplier.getVariable(name,project,fProvider);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariables()
	 */
	@Override
	public IEnvironmentVariable[] getVariables(Object context) {
		if(context == null)
			return null;
		IBuildEnvironmentVariable variables[] = null;
		if(context instanceof IConfiguration){
			IConfiguration cfg = (IConfiguration)context;
			IConfigurationEnvironmentVariableSupplier supplier = cfg.getEnvironmentVariableSupplier();
			if(supplier == null)
				return null;
			variables = supplier.getVariables(cfg,fProvider);
		}
		else if (context instanceof IManagedProject) {
			IManagedProject project = (IManagedProject)context;
			IProjectEnvironmentVariableSupplier supplier = project.getProjectType() != null ? project.getProjectType().getEnvironmentVariableSupplier() : null;
			if(supplier == null)
				return null;
			variables = supplier.getVariables(project,fProvider);
		}

		return filterVariables(variables);
	}

	protected IEnvironmentVariableSupplier[] filterValidSuppliers(IEnvironmentVariableSupplier suppliers[]){
		if(suppliers == null)
			return null;

		int i = 0, j = 0;
		for(i = 0; i < suppliers.length; i++){
			if(suppliers[i] == this)
				break;
		}


		if(i >= suppliers.length)
			return null;

		int startNum = i + 1;


		IEnvironmentVariableSupplier validSuppliers[] =
			new IEnvironmentVariableSupplier[suppliers.length - startNum];

		for(i = startNum, j = 0; i < suppliers.length; i++, j++)
			validSuppliers[j] = suppliers[i];

		return validSuppliers;
	}

	protected String getValidName(String name){
		name = EnvVarOperationProcessor.normalizeName(name);
		if(name == null)
			return null;
		if(fNonOverloadableVariables != null){
			for(int i = 0; i < fNonOverloadableVariables.length; i++){
				if(name.equals(fNonOverloadableVariables[i]))
					return null;
			}
		}
		return name;
	}

	protected IEnvironmentVariable[] filterVariables(IBuildEnvironmentVariable variables[]){
		return EnvVarOperationProcessor.filterVariables(variables,fNonOverloadableVariables);
	}
}
