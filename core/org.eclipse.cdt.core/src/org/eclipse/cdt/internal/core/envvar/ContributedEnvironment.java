/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.envvar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;

public class ContributedEnvironment implements IContributedEnvironment{
	private EnvironmentVariableManager fMngr;
	public ContributedEnvironment(EnvironmentVariableManager mngr){
		fMngr = mngr;
	}
	private class ContributedEnvContextInfo extends DefaultEnvironmentContextInfo {
		private IEnvironmentContextInfo fBaseInfo;
		private ICoreEnvironmentVariableSupplier fSuppliers[];

		public ContributedEnvContextInfo(IEnvironmentContextInfo info) {
			super(info.getContext());
			fBaseInfo = info;
		}

		@Override
		public ICoreEnvironmentVariableSupplier[] getSuppliers() {
			if(fSuppliers == null){
				ICoreEnvironmentVariableSupplier[] suppliers = fBaseInfo.getSuppliers();
				int i = 0;
				for(; i < suppliers.length; i++){
					if(suppliers[i] == EnvironmentVariableManager.fEclipseSupplier){
						break;
					}
				}

				if(i != suppliers.length){
					ICoreEnvironmentVariableSupplier tmp[] = new ICoreEnvironmentVariableSupplier[suppliers.length - 1];
					if(i != 0)
						System.arraycopy(suppliers, 0, tmp, 0, i);
					if(i != tmp.length)
						System.arraycopy(suppliers, i+1, tmp, i, tmp.length - i);
					suppliers = tmp;
				}

				fSuppliers = suppliers;
			}
			return fSuppliers;
		}

		@Override
		public IEnvironmentContextInfo getNext() {
			IEnvironmentContextInfo baseNext = fBaseInfo.getNext();
			if(baseNext != null)
				return new ContributedEnvContextInfo(baseNext);
			return null;
		}
	}

	public IEnvironmentContextInfo getContextInfo(Object context){
		return new ContributedEnvContextInfo(fMngr.getDefaultContextInfo(context));
	}

	@Override
	public IEnvironmentVariable[] getVariables(ICConfigurationDescription des){
		EnvVarCollector cr = EnvironmentVariableManager.getVariables(getContextInfo(des), true);
		if(cr != null){
			EnvVarDescriptor collected[] = cr.toArray(true);
			List<IEnvironmentVariable> vars = new ArrayList<IEnvironmentVariable>(collected.length);
			IEnvironmentVariable var;
			IEnvironmentContextInfo info = new DefaultEnvironmentContextInfo(des);//getContextInfo(des);
			for(int i = 0; i < collected.length; i++){
				var = collected[i];
				var = EnvironmentVariableManager.getVariable(var.getName(), info, true);
				if(var != null)
					vars.add(var);
			}
			return vars.toArray(new EnvVarDescriptor[vars.size()]);
		}
		return new EnvVarDescriptor[0];
	}

	@Override
	public IEnvironmentVariable getVariable(String name, ICConfigurationDescription des){
		EnvVarDescriptor varDes = EnvironmentVariableManager.getVariable(name, getContextInfo(des), true);
		if(varDes != null)
			return EnvironmentVariableManager.getVariable(name, new DefaultEnvironmentContextInfo(des), true);
		return null;
	}

	@Override
	public boolean appendEnvironment(ICConfigurationDescription des){
		return EnvironmentVariableManager.fUserSupplier.appendContributedEnvironment(des);
	}

	@Override
	public void setAppendEnvironment(boolean append, ICConfigurationDescription des){
		EnvironmentVariableManager.fUserSupplier.setAppendContributedEnvironment(append, des);
	}

	@Override
	public IEnvironmentVariable addVariable(String name,
			String value,
			int op,
			String delimiter,
			ICConfigurationDescription des){
		return new EnvVarDescriptor(
				EnvironmentVariableManager.fUserSupplier.createVariable(name, value, op, delimiter, des),
				null,
				-1,
				EnvironmentVariableManager.fUserSupplier);
	}

	@Override
	public void addVariables(IEnvironmentVariable[] vars,
			ICConfigurationDescription des) {
		for (IEnvironmentVariable v : vars)
			addVariable(v, des);
	}

	@Override
	public IEnvironmentVariable addVariable(IEnvironmentVariable var,
			ICConfigurationDescription des) {
		return addVariable(var.getName(),
						   var.getValue(),
						   var.getOperation(),
						   var.getDelimiter(),
						   des);
	}

	@Override
	public IEnvironmentVariable removeVariable(String name, ICConfigurationDescription des){
		return EnvironmentVariableManager.fUserSupplier.deleteVariable(name, des);
	}

	@Override
	public void restoreDefaults(ICConfigurationDescription des){
		EnvironmentVariableManager.fUserSupplier.restoreDefaults(des);
	}

	@Override
	public boolean isUserVariable(ICConfigurationDescription des, IEnvironmentVariable var){
		if(var instanceof EnvVarDescriptor)
			return ((EnvVarDescriptor)var).getSupplier() == EnvironmentVariableManager.fUserSupplier;
		return false;
	}

	public String getOrigin(IEnvironmentVariable var) {
		if(var instanceof EnvVarDescriptor) {
			ICoreEnvironmentVariableSupplier sup = ((EnvVarDescriptor)var).getSupplier();
			if (sup instanceof BuildSystemEnvironmentSupplier)
				return Messages.getString("ContributedEnvironment.0"); //$NON-NLS-1$
			if (sup instanceof EclipseEnvironmentSupplier)
				return Messages.getString("ContributedEnvironment.1"); //$NON-NLS-1$
			if (sup instanceof UserDefinedEnvironmentSupplier) {
				if (((EnvVarDescriptor)var).getContextInfo().getContext() != null)
					return Messages.getString("ContributedEnvironment.4"); //$NON-NLS-1$
				return Messages.getString("ContributedEnvironment.2"); //$NON-NLS-1$
			}
		}
		return Messages.getString("ContributedEnvironment.3"); //$NON-NLS-1$
	}

	public void serialize(ICProjectDescription des){
		EnvironmentVariableManager.fUserSupplier.storeProjectEnvironment(des, false);
	}


}
