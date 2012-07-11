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
import org.eclipse.core.runtime.Path;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GnuCygwinConfigurationEnvironmentSupplier implements IConfigurationEnvironmentVariableSupplier {
	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$
	private static final String ENV_CYGWIN_HOME = "CYGWIN_HOME"; //$NON-NLS-1$
	private static final String ENV_LANG = "LANG"; //$NON-NLS-1$
	private static final String ENV_LC_ALL = "LC_ALL"; //$NON-NLS-1$
	private static final String ENV_LC_MESSAGES = "LC_MESSAGES"; //$NON-NLS-1$

	private static final String DELIMITER_UNIX = ":"; //$NON-NLS-1$
	private static final String PROPERTY_DELIMITER = "path.separator"; //$NON-NLS-1$
	private static final String PROPERTY_OSNAME = "os.name"; //$NON-NLS-1$

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration, IEnvironmentVariableProvider provider) {
		if (variableName == null) {
			return null;
		}

		if (!System.getProperty(PROPERTY_OSNAME).toLowerCase().startsWith("windows ")) { //$NON-NLS-1$
			return null;
		}

		if (variableName.equalsIgnoreCase(ENV_PATH)) {
			String path = CygwinPathResolver.getBinPath();
			if (path != null) {
				path = new Path(path).toOSString();
				return new BuildEnvVar(ENV_PATH, path, IBuildEnvironmentVariable.ENVVAR_PREPEND, System.getProperty(PROPERTY_DELIMITER, DELIMITER_UNIX));
			}
		} else if (variableName.equals(ENV_CYGWIN_HOME)) {
			String home = CygwinPathResolver.getRootPath();
			if (home == null) {
				home = ""; //$NON-NLS-1$
			} else {
				home = new Path(home).toOSString();
			}
			return new BuildEnvVar(ENV_CYGWIN_HOME, home);
		} else if (variableName.equalsIgnoreCase(ENV_LANG)) {
			// Workaround for not being able to select encoding for CDT console -> change codeset to Latin1
			String langValue = System.getenv(ENV_LANG);
			if (langValue == null || langValue.length() == 0) {
				langValue = System.getenv(ENV_LC_ALL);
			}
			if (langValue == null || langValue.length() == 0) {
				langValue = System.getenv(ENV_LC_MESSAGES);
			}
			if (langValue != null && langValue.length() > 0) {
				// langValue is [language[_territory][.codeset][@modifier]], i.e. "en_US.UTF-8@dict"
				// we replace codeset with Latin1 as CDT console garbles UTF
				// and ignore modifier which is not used by LANG
				langValue = langValue.replaceFirst("([^.@]*)(\\..*)?(@.*)?", "$1.ISO-8859-1"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				langValue = "C.ISO-8859-1"; //$NON-NLS-1$
			}

			return new BuildEnvVar(ENV_LANG, langValue);
		}
		return null;
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration, IEnvironmentVariableProvider provider) {
		IBuildEnvironmentVariable varHome = getVariable(ENV_CYGWIN_HOME, configuration, provider);
		IBuildEnvironmentVariable varLang = getVariable(ENV_LANG, configuration, provider);
		IBuildEnvironmentVariable varPath = getVariable(ENV_PATH, configuration, provider);

		if (varPath != null) {
			return new IBuildEnvironmentVariable[] {varHome, varLang, varPath};
		} else {
			return new IBuildEnvironmentVariable[] {varHome, varLang};
		}
	}
}
