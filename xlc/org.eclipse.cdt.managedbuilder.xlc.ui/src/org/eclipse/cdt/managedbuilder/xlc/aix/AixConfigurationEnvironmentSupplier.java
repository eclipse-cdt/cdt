/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.xlc.aix;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.envvar.BuildEnvVar;

public class AixConfigurationEnvironmentSupplier implements IConfigurationEnvironmentVariableSupplier {
	static final String VARNAME = "PATH";
	static final String BINPATH = "/usr/vac/bin";
	static final String DELIMITER_AIX = ":";
	static final String PROPERTY_DELIMITER = "path.separator";
	static final String PROPERTY_OSNAME = "os.name";

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier#getVariable(java.lang.String, org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider)
	 */
	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration,
			IEnvironmentVariableProvider provider) {

		if (variableName == null)
			return null;
		if (!VARNAME.equalsIgnoreCase(variableName))
			return null;
		return new BuildEnvVar(VARNAME, BINPATH, IBuildEnvironmentVariable.ENVVAR_PREPEND,
				System.getProperty(PROPERTY_DELIMITER, DELIMITER_AIX));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier#getVariables(org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider)
	 */
	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration,
			IEnvironmentVariableProvider provider) {

		IBuildEnvironmentVariable[] tmp = new IBuildEnvironmentVariable[1];
		tmp[0] = getVariable(VARNAME, configuration, provider);
		if (tmp[0] != null)
			return tmp;
		return null;
	}
}
