/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.gnu.cygwin;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.envvar.BuildEnvVar;


/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GnuCygwinConfigurationEnvironmentSupplier implements
		IConfigurationEnvironmentVariableSupplier {
	
	private static final String PATH = "PATH"; //$NON-NLS-1$
	private static final String DELIMITER_UNIX = ":"; //$NON-NLS-1$
	private static final String PROPERTY_DELIMITER = "path.separator"; //$NON-NLS-1$
	private static final String PROPERTY_OSNAME = "os.name"; //$NON-NLS-1$

	private static final String LANG = "LANG"; //$NON-NLS-1$
	private static final String LC_ALL = "LC_ALL"; //$NON-NLS-1$
	private static final String LC_MESSAGES = "LC_MESSAGES"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier#getVariable(java.lang.String, org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider)
	 */
	@Override
	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		
		if (variableName == null)
			return null;

		if (!System.getProperty(PROPERTY_OSNAME).toLowerCase().startsWith("windows ")) //$NON-NLS-1$
			return null;
		
		if (variableName.equalsIgnoreCase(PATH)) {
			String p = CygwinPathResolver.getBinPath();
			if (p != null)
				return new BuildEnvVar(PATH, p.replace('/','\\'), IBuildEnvironmentVariable.ENVVAR_PREPEND, System.getProperty(PROPERTY_DELIMITER, DELIMITER_UNIX));
		} else if (variableName.equalsIgnoreCase(LANG)) {
			// Workaround for not being able to select encoding for CDT console -> change codeset to Latin1
			String langValue = System.getenv(LANG); 
			if (langValue == null || langValue.length() == 0)
				langValue = System.getenv(LC_ALL);
			if (langValue == null || langValue.length() == 0)
				langValue = System.getenv(LC_MESSAGES);
			if (langValue != null && langValue.length() > 0)
				// langValue is [language[_territory][.codeset][@modifier]], i.e. "en_US.UTF-8@dict"
				// we replace codeset with Latin1 as CDT console garbles UTF
				// and ignore modifier which is not used by LANG
				langValue = langValue.replaceFirst("([^.@]*)(\\..*)?(@.*)?", "$1.ISO-8859-1"); //$NON-NLS-1$ //$NON-NLS-2$
			else
				langValue = "C.ISO-8859-1"; //$NON-NLS-1$

			return new BuildEnvVar(LANG, langValue); 
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier#getVariables(org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider)
	 */
	@Override
	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {

		IBuildEnvironmentVariable varLang = getVariable(LANG, configuration, provider);
		IBuildEnvironmentVariable varPath = getVariable(PATH, configuration, provider);
		
		if (varPath != null) {
			return new IBuildEnvironmentVariable[] {varLang, varPath};
		} else {
			return new IBuildEnvironmentVariable[] {varLang};
		}
	}
}
