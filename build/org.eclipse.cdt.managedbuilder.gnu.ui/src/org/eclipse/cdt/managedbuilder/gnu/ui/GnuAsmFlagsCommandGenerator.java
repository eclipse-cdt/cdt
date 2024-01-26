/*******************************************************************************
 * Copyright (c) 2024 John Dallaway and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Dallaway - initial implementation (#666)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.gnu.ui;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCommandGenerator;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.FileMacroExplicitSubstitutor;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildfileMacroSubstitutor;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * Assembler flags command generator.
 * This command generator supports managed build projects that were
 * created using older versions of the GNU toolchain build description
 * where assembly files were built by invoking the GNU "as" tool directly.
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 8.7
 */
public class GnuAsmFlagsCommandGenerator implements IOptionCommandGenerator {

	private static final String DO_NOT_LINK_FLAG = "-c"; //$NON-NLS-1$
	private static final Pattern DO_NOT_LINK_PATTERN = Pattern.compile("(^|\\s)-c($|\\s)"); //$NON-NLS-1$
	private static final Pattern ASM_FLAG_PATTERN = Pattern.compile("(?<=^|\\s)-[aDKLR]\\S*"); //$NON-NLS-1$

	@Override
	public String generateCommand(IOption option, IVariableSubstitutor macroSubstitutor) {
		String toolCommand = getToolCommand(option, macroSubstitutor);
		try {
			if (null != toolCommand && IOption.STRING == option.getValueType() && option.getCommand().isEmpty()) {
				String optionValue = option.getStringValue();
				if (toolCommand.equals("gcc")) { //$NON-NLS-1$
					// if the default assembler tool command has not been overridden
					String command = CdtVariableResolver.resolveToString(optionValue, macroSubstitutor);
					if (!DO_NOT_LINK_PATTERN.matcher(command).find()) {
						// if the "-c" flag is not already present on the command line we
						// assume the flags target the GNU "as" command line rather than the
						// "gcc" command line so we add the "-c" flag and apply the "-Wa,"
						// prefix to those flags that are intended only for the assembler
						return DO_NOT_LINK_FLAG + " " + ASM_FLAG_PATTERN.matcher(command).replaceAll("-Wa,$0"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else if (toolCommand.endsWith("as") && optionValue.equals(DO_NOT_LINK_FLAG)) { //$NON-NLS-1$
					// if GNU "as" is called directly and the default assembler flags have
					// not been overridden we remove the "-c" flag
					return ""; //$NON-NLS-1$
				}
			}
		} catch (BuildException | CdtVariableException e) {
			Platform.getLog(getClass()).log(Status.error("Error generating GNU assembler command", e)); //$NON-NLS-1$
		}
		return null; // fallback to default command generator
	}

	private static String getToolCommand(IOption option, IVariableSubstitutor macroSubstitutor) {
		// the option holder may be a super class of the assembler tool so we must
		// locate the tool from the build configuration to obtain the correct tool command
		IConfiguration config = getConfiguration(macroSubstitutor);
		if (config != null) {
			String optionHolderId = option.getOptionHolder().getId();
			ITool[] tools = config.getToolsBySuperClassId(optionHolderId);
			if (1 == tools.length) {
				return tools[0].getToolCommand();
			}
		}
		return null;
	}

	private static IConfiguration getConfiguration(IVariableSubstitutor macroSubstitutor) {
		if (macroSubstitutor instanceof BuildfileMacroSubstitutor bms) { // case ToolSettingsPrefStore
			return bms.getConfiguration();
		} else if (macroSubstitutor instanceof FileMacroExplicitSubstitutor fmes) { // case BuildStep
			return fmes.getConfiguration();
		}
		return null;
	}

}
