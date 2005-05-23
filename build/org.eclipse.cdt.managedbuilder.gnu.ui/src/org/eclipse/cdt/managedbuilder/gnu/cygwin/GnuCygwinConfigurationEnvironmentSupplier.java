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
package org.eclipse.cdt.managedbuilder.gnu.cygwin;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.internal.envvar.BuildEnvVar;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;

public class GnuCygwinConfigurationEnvironmentSupplier implements
		IConfigurationEnvironmentVariableSupplier {

	static final String VARNAME = "PATH";        //$NON-NLS-1$
	static final String DELIMITER_UNIX = ":";    //$NON-NLS-1$
	static final String PROPERTY_DELIMITER = "path.separator"; //$NON-NLS-1$
	static final String PROPERTY_OSNAME    = "os.name"; //$NON-NLS-1$
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier#getVariable(java.lang.String, org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider)
	 */
	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {

		if (!System.getProperty(PROPERTY_OSNAME).toLowerCase().startsWith("windows ")) //$NON-NLS-1$ 
			return null;
		
		if (variableName == null) return null;
		if (!VARNAME.equalsIgnoreCase(variableName)) return null;
		
		String p = CygwinPathResolver.getBinPath();
		if (p != null) 
			return new BuildEnvVar(VARNAME, p.replace('/','\\'), IBuildEnvironmentVariable.ENVVAR_PREPEND, System.getProperty(PROPERTY_DELIMITER, DELIMITER_UNIX)); //$NON-NLS-1$ //$NON-NLS-2$
		return null;	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier#getVariables(org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider)
	 */
	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		
		IBuildEnvironmentVariable[] tmp = new IBuildEnvironmentVariable[1];   
		tmp[0] = getVariable(VARNAME, configuration, provider);
		if (tmp[0] != null) return tmp; 
		return null;
	}
}
