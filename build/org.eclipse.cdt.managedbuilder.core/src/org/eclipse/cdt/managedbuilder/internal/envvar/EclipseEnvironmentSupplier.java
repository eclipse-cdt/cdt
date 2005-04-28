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

import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;

/**
 * This is the Environment Variable Supplier used to supply variables
 * defined in eclipse environment
 * 
 * @since 3.0
 */
public class EclipseEnvironmentSupplier implements IEnvironmentVariableSupplier {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariable()
	 */
	public IBuildEnvironmentVariable getVariable(String name, Object context) {
		if(context == null){
			String value = EnvironmentReader.getEnvVar(name);
			if(value == null)
				return null;
			return new BuildEnvVar(name,value,IBuildEnvironmentVariable.ENVVAR_REPLACE,null);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariables()
	 */
	public IBuildEnvironmentVariable[] getVariables(Object context) {
		if(context == null){
			Properties values = EnvironmentReader.getEnvVars();
			if(values == null)
				return null;

			IBuildEnvironmentVariable variables[] = new IBuildEnvironmentVariable[values.size()];
			Enumeration en = values.propertyNames();
			for( int i = 0; i < variables.length ; i++){
				String name = (String)en.nextElement();
				String value = values.getProperty(name);
				variables[i] = new BuildEnvVar(name,value,IBuildEnvironmentVariable.ENVVAR_REPLACE,null);
			}
			return variables;
		}
		return null;
	}

}
