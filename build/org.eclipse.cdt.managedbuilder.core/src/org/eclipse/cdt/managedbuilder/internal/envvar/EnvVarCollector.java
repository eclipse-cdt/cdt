/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.envvar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;

/**
 * This class implements the "merging" functionality of environment variables
 * Used by the EnvironmentVariableProvider to "merge" the sets of macros returned
 * by different suppliers into one set returned to the user
 *  
 * @since 3.0
 *
 */
public class EnvVarCollector {
	private Map fMap = null;
	public EnvVarCollector(){
		
	}
	
	/**
	 * adds an array of environment variables to the set of variables held by this collector
	 * performing environment variable operations
	 * @param vars
	 */
	public void add(IBuildEnvironmentVariable vars[]){
		add(vars,null,-1);
	}
	
	public void add(IBuildEnvironmentVariable vars[], IContextInfo info, int num){
		if(vars == null)
			return;
		boolean isCaseInsensitive = !EnvironmentVariableProvider.getDefault().isVariableCaseSensitive();
		for(int i = 0; i < vars.length; i ++) {
			IBuildEnvironmentVariable var = vars[i];
			String name = var.getName();
			if(isCaseInsensitive)
				name = name.toUpperCase();
			
			boolean noCheck = false;
			
			if(fMap == null){
				noCheck = true;
				fMap = new HashMap();
			}
			
			EnvVarDescriptor des = null;
			if(noCheck || (des = (EnvVarDescriptor)fMap.get(name)) == null){
				des = new EnvVarDescriptor(var,info,num);
				fMap.put(name,des);
			}
			else {
				des.setContextInfo(info);
				des.setSupplierNum(num);
				des.setVariable(EnvVarOperationProcessor.performOperation(des.getOriginalVariable(),var));
			}
		}
	}
	
	/**
	 * Returns an array of variables held by this collector
	 * 
	 * @param includeRemoved true if removed variables should be included in the resulting array
	 * @return IBuildEnvironmentVariable[]
	 */
	public EnvVarDescriptor[] toArray(boolean includeRemoved){
		if(fMap == null)
			return new EnvVarDescriptor[0];
		Collection values = fMap.values();
		List list = new ArrayList();
		Iterator iter = values.iterator();
		while(iter.hasNext()){
			EnvVarDescriptor des = (EnvVarDescriptor)iter.next();
			if(des != null && 
					(includeRemoved || des.getOperation() != IBuildEnvironmentVariable.ENVVAR_REMOVE))
				list.add(des);
		}
		return (EnvVarDescriptor[])list.toArray(new EnvVarDescriptor[list.size()]);
	}
	
	/**
	 * Returns a variable of a given name held by this collector
	 * 
	 * @param name a variable name
	 * @return IBuildEnvironmentVariable
	 */
	public EnvVarDescriptor getVariable(String name){
		if(fMap == null)
			return null;
		
		if(!EnvironmentVariableProvider.getDefault().isVariableCaseSensitive())
			name = name.toUpperCase();

		return (EnvVarDescriptor)fMap.get(name);
	}
	
	/**
	 * Returns an array of variables held by this collector
	 * The call to this method is equivalent of calling toArray(true)
	 * 
	 * @return IBuildEnvironmentVariable[]
	 */
	public EnvVarDescriptor[] getVariables(){
		return toArray(true);
	}

}
