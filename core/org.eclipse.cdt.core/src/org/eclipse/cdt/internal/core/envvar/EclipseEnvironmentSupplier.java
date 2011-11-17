/*******************************************************************************
 * Copyright (c) 2005, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.envvar;

import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;

/**
 * This is the Environment Variable Supplier used to supply variables
 * defined in eclipse environment
 *
 * @since 3.0
 */
public class EclipseEnvironmentSupplier implements ICoreEnvironmentVariableSupplier {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariable()
	 */
	@Override
	public IEnvironmentVariable getVariable(String name, Object context) {
		if(context == null){
			String value = EnvironmentReader.getEnvVar(name);
			if(value == null)
				return null;
			return new EnvironmentVariable(name,value,IEnvironmentVariable.ENVVAR_REPLACE,null);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariables()
	 */
	@Override
	public IEnvironmentVariable[] getVariables(Object context) {
		if(context == null){
			Properties values = EnvironmentReader.getEnvVars();
			if(values == null)
				return null;

			IEnvironmentVariable variables[] = new IEnvironmentVariable[values.size()];
			Enumeration<?> en = values.propertyNames();
			for( int i = 0; i < variables.length ; i++){
				String name = (String)en.nextElement();
				String value = values.getProperty(name);
				variables[i] = new EnvironmentVariable(name,value,IEnvironmentVariable.ENVVAR_REPLACE,null);
			}
			return variables;
		}
		return null;
	}

	@Override
	public boolean appendEnvironment(Object context) {
		return true;
	}

}
