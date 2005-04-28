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
			
			if(noCheck)
				fMap.put(name,var);
			else {
				IBuildEnvironmentVariable prevVar = (IBuildEnvironmentVariable)fMap.remove(name);
				fMap.put(name,EnvVarOperationProcessor.performOperation(prevVar,var));
			}
		}
	}
	
	/**
	 * Returns an array of variables held by this collector
	 * 
	 * @param includeRemoved true if removed variables should be included in the resulting array
	 * @return IBuildEnvironmentVariable[]
	 */
	public IBuildEnvironmentVariable[] toArray(boolean includeRemoved){
		if(fMap == null)
			return new IBuildEnvironmentVariable[0];
		Collection values = fMap.values();
		List list = new ArrayList();
		Iterator iter = values.iterator();
		while(iter.hasNext()){
			IBuildEnvironmentVariable var = (IBuildEnvironmentVariable)iter.next();
			if(var != null && 
					(includeRemoved || var.getOperation() != IBuildEnvironmentVariable.ENVVAR_REMOVE))
				list.add(var);
		}
		return (IBuildEnvironmentVariable[])list.toArray(new IBuildEnvironmentVariable[list.size()]);
	}
	
	/**
	 * Returns a variable of a given name held by this collector
	 * 
	 * @param name a variable name
	 * @return IBuildEnvironmentVariable
	 */
	public IBuildEnvironmentVariable getVariable(String name){
		if(fMap == null)
			return null;
		
		if(!EnvironmentVariableProvider.getDefault().isVariableCaseSensitive())
			name = name.toUpperCase();

		return (IBuildEnvironmentVariable)fMap.get(name);
	}
	
	/**
	 * Returns an array of variables held by this collector
	 * The call to this method is equivalent of calling toArray(true)
	 * 
	 * @return IBuildEnvironmentVariable[]
	 */
	public IBuildEnvironmentVariable[] getVariables(){
		return toArray(true);
	}

}
