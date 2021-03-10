/*******************************************************************************
 * Copyright (c) 2009, 2020 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 559707
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.language.settings.providers;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableManagerToolChain;
import org.eclipse.osgi.util.NLS;

/**
 * Abstract parser capable to execute compiler command printing built-in compiler
 * specs and parse built-in language settings out of it. The compiler to be used
 * is taken from MBS tool-chain definition.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class interface is not stable yet as
 * it is not currently (CDT 8.1, Juno) clear how it may need to be used in future.
 * There is no guarantee that this API will work or that it will remain the same.
 * Please do not use this API without consulting with the CDT team.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 *
 * @since 8.1
 */
public abstract class ToolchainBuiltinSpecsDetector extends AbstractBuiltinSpecsDetector {
	private static final String EMPTY_QUOTED_STRING = "\"\""; //$NON-NLS-1$
	private Map<String, ITool> toolMap = new HashMap<>();

	/**
	 * Concrete compiler specs detectors need to supply tool-chain ID.
	 *
	 * Tool-chain id must be supplied for global providers where we don't
	 * have configuration description to figure that out programmatically.
	 * @since 8.2
	 */
	public abstract String getToolchainId();

	/**
	 * Finds a tool handling given language in the tool-chain of the provider.
	 * This returns the first tool found or empty {@link Optional}.
	 * @since 8.8
	 */
	protected Optional<ITool> languageTool(String languageId) {
		if (languageId == null) {
			return Optional.empty();
		}

		if (currentCfgDescription == null) {
			ITool tool = toolMap.get(languageId);
			if (tool != null) {
				return Optional.of(tool);
			}
		}

		String toolchainId = null;
		IToolChain toolchain = null;
		ITool tool = null;
		if (currentCfgDescription != null) {
			IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(currentCfgDescription);
			toolchain = cfg != null ? cfg.getToolChain() : null;
			toolchainId = toolchain != null ? toolchain.getId() : null;
		}
		if (toolchain == null) {
			toolchainId = getToolchainId();
			toolchain = ManagedBuildManager.getExtensionToolChain(toolchainId);
		}
		for (; toolchain != null; toolchain = toolchain.getSuperClass()) {
			tool = getTool(languageId, toolchain);
			if (tool != null) {
				break;
			}
		}
		if (currentCfgDescription == null && tool != null) {
			// cache only for global providers which use extension tool-chains that can not change
			toolMap.put(languageId, tool);
		}

		if (tool == null) {
			ManagedBuilderCorePlugin
					.error("Unable to find tool in toolchain=" + toolchainId + " for language=" + languageId); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return Optional.ofNullable(tool);
	}

	/**
	 * Finds a tool handling given language in the tool-chain.
	 * This returns the first tool found.
	 */
	private ITool getTool(String languageId, IToolChain toolchain) {
		ITool[] tools = toolchain.getTools();
		for (ITool tool : tools) {
			IInputType[] inputTypes = tool.getInputTypes();
			for (IInputType inType : inputTypes) {
				String lang = inType.getLanguageId(tool);
				if (languageId.equals(lang)) {
					return tool;
				}
			}
		}
		return null;
	}

	@Override
	protected String getCompilerCommand(String languageId) {
		Optional<String> found = languageTool(languageId)//
				.flatMap(t -> Optional.of(t.getToolCommand()));
		if (!found.isPresent()) {
			ManagedBuilderCorePlugin
					.error(NLS.bind("Unable to find compiler command in toolchain={0}", getToolchainId())); //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
		return found.get();
	}

	@Override
	protected String getSpecFileExtension(String languageId) {
		Optional<String[]> optionalExtensions = languageTool(languageId)
				.flatMap(t -> Optional.of(t.getAllInputExtensions()));
		List<String> extensions = optionalExtensions.map(Arrays::asList).orElseGet(() -> Collections.emptyList());
		Optional<String> extension = selectBestSpecFileExtension(extensions);

		if (!extension.isPresent()) {
			//this looks like either invalid configuration settings or API issue
			ManagedBuilderCorePlugin.error(NLS.bind("Unable to find file extension for language {0}", languageId)); //$NON-NLS-1$
			return null;
		}
		return extension.get();
	}

	/**
	 * @since 9.2
	 */
	protected String getToolOptions(String languageId, Predicate<IOption> predicate) {
		Optional<IOption[]> found = languageTool(languageId).flatMap(t -> Optional.of(t.getOptions()));
		if (!found.isPresent()) {
			return ""; //$NON-NLS-1$
		}
		StringBuilder flags = new StringBuilder();
		IOption[] options = found.get();
		for (IOption option : options) {
			if (predicate.test(option)) {
				try {
					String optionValue = null;
					switch (option.getBasicValueType()) {
					case IOption.BOOLEAN:
						if (option.getBooleanValue()) {
							optionValue = option.getCommand();
						} else {
							optionValue = option.getCommandFalse();
						}
						break;
					case IOption.ENUMERATED:
						optionValue = option.getEnumCommand(option.getSelectedEnum());
						break;
					case IOption.STRING:
						optionValue = option.getCommand() + option.getStringValue();
						break;
					case IOption.STRING_LIST:
						String[] values = option.getBasicStringListValue();
						if (values != null) {
							optionValue = ""; //$NON-NLS-1$
							String cmd = option.getCommand();
							for (String value : values) {
								if (!value.isEmpty() && !value.equals(EMPTY_QUOTED_STRING)) {
									optionValue = optionValue + cmd + value + ' ';
								}
							}
						}
						break;
					case IOption.TREE:
						optionValue = option.getCommand(option.getStringValue());
						break;
					default:
					}
					if (optionValue != null) {
						flags.append(' ').append(optionValue.trim());
					}
				} catch (BuildException e) {
					ManagedBuilderCorePlugin.log(e);
				}
			}
		}
		return flags.toString().trim();
	}

	@Override
	protected String getToolOptions(String languageId) {
		return getToolOptions(languageId, IOption::isForScannerDiscovery);
	}

	@Override
	protected String getAllToolOptions(String languageId) {
		return getToolOptions(languageId, option -> true);
	}

	@Override
	protected List<IEnvironmentVariable> getEnvironmentVariables() {
		if (envMngr == null && currentCfgDescription == null) {
			// For global provider need to include toolchain in the equation
			IToolChain toolchain = ManagedBuildManager.getExtensionToolChain(getToolchainId());
			envMngr = new EnvironmentVariableManagerToolChain(toolchain);
		}
		List<IEnvironmentVariable> vars = super.getEnvironmentVariables();

		return vars;
	}
}
