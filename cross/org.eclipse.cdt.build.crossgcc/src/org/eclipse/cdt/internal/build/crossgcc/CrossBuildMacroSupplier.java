/*******************************************************************************
 * Copyright (c) 2023 John Dallaway and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Dallaway - initial API and implementation (#361)
 *******************************************************************************/
package org.eclipse.cdt.internal.build.crossgcc;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.macros.AbstractGnuToolPrefixMacro;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier;
import org.eclipse.core.runtime.Status;

public class CrossBuildMacroSupplier implements IConfigurationBuildMacroSupplier {

	private static class GnuToolPrefixMacro extends AbstractGnuToolPrefixMacro {

		private static final String GNU_TOOL_PREFIX_OPTION = "cdt.managedbuild.option.gnu.cross.prefix"; //$NON-NLS-1$
		private final IConfiguration configuration;

		public GnuToolPrefixMacro(IConfiguration configuration) {
			this.configuration = configuration;
		}

		@Override
		public String getStringValue() throws BuildMacroException {
			final IOption option = configuration.getToolChain().getOptionBySuperClassId(GNU_TOOL_PREFIX_OPTION);
			if (null == option) {
				throw new BuildMacroException(Status.error("Toolchain option not found: " + GNU_TOOL_PREFIX_OPTION)); //$NON-NLS-1$
			}
			return getStringValue(option);
		}

	}

	@Override
	public IBuildMacro getMacro(String macroName, IConfiguration configuration, IBuildMacroProvider provider) {
		if (GnuToolPrefixMacro.MACRO_NAME.equals(macroName)) {
			return new GnuToolPrefixMacro(configuration);
		}
		return null;
	}

	@Override
	public IBuildMacro[] getMacros(IConfiguration configuration, IBuildMacroProvider provider) {
		final IBuildMacro macro = getMacro(GnuToolPrefixMacro.MACRO_NAME, configuration, provider);
		return (null == macro) ? new IBuildMacro[0] : new IBuildMacro[] { macro };
	}

}
