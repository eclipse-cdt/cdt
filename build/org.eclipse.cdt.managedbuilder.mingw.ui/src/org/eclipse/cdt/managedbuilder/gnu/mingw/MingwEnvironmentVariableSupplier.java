/*******************************************************************************
 * Copyright (c) 2006, 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer, QNX Software Systems - Initial API and implementation
 *     Andrew Gvozdev                      - Ability to use different MinGW versions in different cfg
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.gnu.mingw;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MingwEnvironmentVariableSupplier implements IConfigurationEnvironmentVariableSupplier {
	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$
	private static final String BACKSLASH = java.io.File.separator;
	private static final String PATH_DELIMITER = CCorePlugin.getDefault().getBuildEnvironmentManager().getDefaultDelimiter();

	/**
	 * @return location of $MINGW_HOME/bin folder on the file-system.
	 * @deprecated. Deprecated as of CDT 8.2. Note that MinGW root path in general may depend on configuration.
	 *
	 * If you use this do not cache results to ensure user preferences are accounted for.
	 * Please rely on internal caching.
	 */
	@Deprecated
	public static IPath getBinDir() {
		IPath binDir = null;
		String minGWHome = MinGW.getMinGWHome();
		if (minGWHome != null) {
			binDir = new Path(minGWHome).append("bin"); //$NON-NLS-1$
		}
		return binDir;
	}

	/**
	 * @return location of $MINGW_HOME/msys/bin folder on the file-system.
	 * @deprecated. Deprecated as of CDT 8.2. Note that MinGW root path in general may depend on configuration.
	 *
	 * If you use this do not cache results to ensure user preferences are accounted for.
	 * Please rely on internal caching.
	 */
	@Deprecated
	public static IPath getMsysBinDir() {
		IPath msysBinDir = null;
		String msysHome = MinGW.getMSysHome();
		if (msysHome != null) {
			msysBinDir = new Path(msysHome).append("bin"); //$NON-NLS-1$
		}
		return msysBinDir;
	}

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration, IEnvironmentVariableProvider provider) {
		if (variableName.equals(MinGW.ENV_MINGW_HOME)) {
			IEnvironmentVariable varMinGWHome = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(MinGW.ENV_MINGW_HOME, null, false);
			if (varMinGWHome == null) {
				// Contribute if the variable does not already come from workspace environment
				String path = MinGW.getMinGWHome();
				if (path == null) {
					// If the variable is not defined still show it in the environment variables list as a hint to user
					path = ""; //$NON-NLS-1$
				}
				final String minGWHome = path; 
				return new IBuildEnvironmentVariable() {
					@Override
					public String getName() {
						return MinGW.ENV_MINGW_HOME;
					}
					@Override
					public String getValue() {
						return new Path(minGWHome).toOSString();
					}
					@Override
					public int getOperation() {
						return IBuildEnvironmentVariable.ENVVAR_REPLACE;
					}
					@Override
					public String getDelimiter() {
						return PATH_DELIMITER;
					}
				};
			}
			return null;
		} else if (variableName.equals(MinGW.ENV_MSYS_HOME)) {
			IEnvironmentVariable varMsysHome = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(MinGW.ENV_MSYS_HOME, null, false);
			if (varMsysHome == null) {
				// Contribute if the variable does not already come from workspace environment
				String path = MinGW.getMSysHome();
				if (path == null) {
					// If the variable is not defined still show it in the environment variables list as a hint to user
					path = ""; //$NON-NLS-1$
				}
				final String msysHome = path;
				return new IBuildEnvironmentVariable() {
					@Override
					public String getName() {
						return MinGW.ENV_MSYS_HOME;
					}
					@Override
					public String getValue() {
						return new Path(msysHome).toOSString();
					}
					@Override
					public int getOperation() {
						return IBuildEnvironmentVariable.ENVVAR_REPLACE;
					}
					@Override
					public String getDelimiter() {
						return PATH_DELIMITER;
					}
				};
			}
			return null;

		} else if (variableName.equals(ENV_PATH)) {
			@SuppressWarnings("nls")
			final String path = "${" + MinGW.ENV_MINGW_HOME + "}" + BACKSLASH + "bin" + PATH_DELIMITER
					+ "${" + MinGW.ENV_MSYS_HOME + "}" + BACKSLASH + "bin";
			
			return new IBuildEnvironmentVariable() {
				@Override
				public String getName() {
					return ENV_PATH;
				}
				@Override
				public String getValue() {
					return path;
				}
				@Override
				public int getOperation() {
					return IBuildEnvironmentVariable.ENVVAR_PREPEND;
				}
				@Override
				public String getDelimiter() {
					return PATH_DELIMITER;
				}
			};
		}

		return null;
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration, IEnvironmentVariableProvider provider) {
		return new IBuildEnvironmentVariable[] {
				getVariable(MinGW.ENV_MINGW_HOME, configuration, provider),
				getVariable(MinGW.ENV_MSYS_HOME, configuration, provider),
				getVariable(ENV_PATH, configuration, provider),
			};
	}

}
