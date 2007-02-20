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

/*		public ContributedEnvContextInfo(Object context,
				ICoreEnvironmentVariableSupplier[] suppliers) {
			super(context, suppliers);
		}
*/
		public ContributedEnvContextInfo(IEnvironmentContextInfo info) {
			super(info.getContext());
			fBaseInfo = info;
		}

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
	
	public IEnvironmentVariable[] getVariables(ICConfigurationDescription des){
		EnvVarCollector cr = EnvironmentVariableManager.getVariables(getContextInfo(des), true);
		if(cr != null){
			EnvVarDescriptor collected[] = cr.toArray(true);
			List vars = new ArrayList(collected.length);
			IEnvironmentVariable var;
			IEnvironmentContextInfo info = new DefaultEnvironmentContextInfo(des);//getContextInfo(des);
			for(int i = 0; i < collected.length; i++){
				var = collected[i];
				var = EnvironmentVariableManager.getVariable(var.getName(), info, true);
				if(var != null)
					vars.add(var);
			}
			return (EnvVarDescriptor[])vars.toArray(new EnvVarDescriptor[vars.size()]);
		}
		return new EnvVarDescriptor[0];
	}
	
	public IEnvironmentVariable getVariable(String name, ICConfigurationDescription des){
		EnvVarDescriptor varDes = EnvironmentVariableManager.getVariable(name, getContextInfo(des), true);
		if(varDes != null)
			return EnvironmentVariableManager.getVariable(name, new DefaultEnvironmentContextInfo(des), true);
		return null;
	}
	
	public boolean appendEnvironment(ICConfigurationDescription des){
		return EnvironmentVariableManager.fUserSupplier.appendContributedEnvironment(des);
	}

	public void setAppendEnvironment(boolean append, ICConfigurationDescription des){
		EnvironmentVariableManager.fUserSupplier.setAppendContributedEnvironment(append, des);
	}

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
	
	public IEnvironmentVariable removeVariable(String name, ICConfigurationDescription des){
		return EnvironmentVariableManager.fUserSupplier.deleteVariable(name, des);
	}
	
	public void restoreDefaults(ICConfigurationDescription des){
		EnvironmentVariableManager.fUserSupplier.restoreDefaults(des);
	}
	
	public boolean isUserVariable(ICConfigurationDescription des, IEnvironmentVariable var){
		if(var instanceof EnvVarDescriptor)
			return ((EnvVarDescriptor)var).getSupplier() == EnvironmentVariableManager.fUserSupplier;
		return false;
	}
	
	public void serialize(ICProjectDescription des){
		EnvironmentVariableManager.fUserSupplier.storeProjectEnvironment(des, false);
	}
	
	
}
