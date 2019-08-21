/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Toolchains are a collection of tools that take the source code and converts
 * it into an executable system.
 *
 * @since 6.0
 */
public interface IToolChain extends IAdaptable {

	/**
	 * Property: The OS the toolchain builds for.
	 */
	static final String ATTR_OS = "os"; //$NON-NLS-1$

	/**
	 * Property: The CPU architecture the toolchain supports.
	 */
	static final String ATTR_ARCH = "arch"; //$NON-NLS-1$

	/**
	 * Property: A package ID to reflect different version/package line up of
	 * the platform this toolchain supports.
	 */
	static final String ATTR_PACKAGE = "package"; //$NON-NLS-1$

	/**
	 * The provider of the toolchain.
	 *
	 * @return toolchain provider
	 */
	IToolChainProvider getProvider();

	/**
	 * The ID of the toolchain
	 *
	 * @return toolchain ID
	 */
	String getId();

	/**
	 * The version of the toolchain
	 *
	 * @deprecated the version doesn't matter. id's for a given type must be unique.
	 * @return toolchain version
	 */
	@Deprecated
	String getVersion();

	/**
	 * The user friendly name for the toolchain
	 *
	 * @return toolchain name
	 */
	String getName();

	/**
	 * Return a toolchain specific part of the build configuration name. This should be enough
	 * to ensure the build config generated proper code for the selected target.
	 *
	 * As a default implementation, we do what the CMakeBuildConfigationProvider did which has
	 * been copied to a number of other providers, i.e. use the os and arch.
	 *
	 * @return fragment to be used in the build config name
	 * @since 6.9
	 */
	default String getBuildConfigNameFragment() {
		String os = getProperty(ATTR_OS);
		String arch = getProperty(ATTR_ARCH);

		if (os != null) {
			return os + '.' + arch;
		} else if (arch != null) {
			return arch;
		} else {
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * The type id for the toolchain. The combination of type id and toolchain id
	 * uniquely identify the toolchain in the system.
	 *
	 * @since 6.4
	 */
	default String getTypeId() {
		// Subclasses really need to override this. There can be multiple providers for
		// a given toolchain type.
		return getProvider().getId();
	}

	/**
	 * Returns an property of the toolchain. Used to determine applicability of
	 * a toolchain for a given situation.
	 *
	 * @param key
	 *            key of the property
	 * @return value of the property or null if the toolchain does not have that
	 *         property
	 */
	String getProperty(String key);

	/**
	 * Set a property on the toolchain.
	 *
	 * @param key
	 *            key of the property
	 * @param value
	 *            value of the property
	 */
	void setProperty(String key, String value);

	/**
	 * Return the environment variables to be set when invoking the tools in the
	 * toolchain.
	 *
	 * @return environment variables
	 */
	IEnvironmentVariable[] getVariables();

	/**
	 * Return the environment variable of the given name used when invoking the
	 * toolchain.
	 *
	 * @param name
	 *            environment variable name
	 * @return environment variable value
	 */
	IEnvironmentVariable getVariable(String name);

	/**
	 * Returns the error parser IDs use to create error markers for builds with
	 * this toolchain.
	 *
	 * @return error parser IDs
	 */
	String[] getErrorParserIds();

	/**
	 * Returns the IDs for the binary parsers that can parse the build output of
	 * the toolchain.
	 *
	 * @return binary parser IDs for this toolchain
	 */
	String getBinaryParserId();

	/**
	 * Get the scanner info for a given build config, command, base scanner
	 * info, resource and build directory.
	 *
	 * @param buildConfig
	 *            the build configuration this scanner info applies to
	 * @param command
	 *            the compile command that is used to build the resource
	 * @param baseScannerInfo
	 *            base scanner info that this scanner info extends/replaces
	 * @param resource
	 *            the resource this scanner info applies to, usually a source
	 *            file
	 * @param buildDirectoryURI
	 *            where the build command is run to build this resource
	 * @return scanner info for this resource
	 * @since 6.1
	 */
	default IExtendedScannerInfo getScannerInfo(IBuildConfiguration buildConfig, List<String> command,
			IExtendedScannerInfo baseScannerInfo, IResource resource, URI buildDirectoryURI) {
		return null;
	}

	@Deprecated
	default IExtendedScannerInfo getScannerInfo(IBuildConfiguration buildConfig, Path command, String[] args,
			IExtendedScannerInfo baseScannerInfo, IResource resource, URI buildDirectoryURI) {
		List<String> commandStrings = new ArrayList<>(args.length + 1);
		commandStrings.add(command.toString());
		commandStrings.addAll(Arrays.asList(args));
		return getScannerInfo(buildConfig, commandStrings, baseScannerInfo, resource, buildDirectoryURI);
	}

	/**
	 * Return the default scanner info for this toolchain. This is used before
	 * any build information is available to provide at least a minimal scanner
	 * info based on the compiler built-ins.
	 *
	 * @param buildConfig
	 *            the build configuration this scanner info applies to
	 * @param baseScannerInfo
	 *            base scanner info that this scanner info extends/replaces
	 * @param language
	 *            the source language that selects the tool to provide scanner
	 *            info for
	 * @param buildDirectoryURI
	 *            the build directory that would be used to run commands
	 * @returns default scanner info for this language
	 * @since 6.1
	 */
	default IExtendedScannerInfo getDefaultScannerInfo(IBuildConfiguration buildConfig,
			IExtendedScannerInfo baseScannerInfo, ILanguage language, URI buildDirectoryURI) {
		return null;
	}

	/**
	 * Returns the absolute path of the tool represented by the command
	 *
	 * @param command
	 *            the command as it usually appears on the command line
	 * @return the absolute path to the tool for the command
	 */
	Path getCommandPath(Path command);

	/**
	 * Returns the list of compiler tools.
	 *
	 * @return list of compiler tools
	 */
	String[] getCompileCommands();

	/**
	 * Returns the list of compiler tools for a given language.
	 *
	 * @param language
	 *            the language for the commands
	 * @return the compile commands for the language
	 * @since 6.1
	 */
	default String[] getCompileCommands(ILanguage language) {
		return new String[0];
	}

	/**
	 * Returns the list of resources referenced in a compile command.
	 *
	 * @param command
	 *            the compile command
	 * @param buildDirectoryURI
	 *            the directory the compile command runs in
	 * @return the list of resources referenced in the compile command
	 * @since 6.1
	 */
	default IResource[] getResourcesFromCommand(List<String> command, URI buildDirectoryURI) {
		return new IResource[0];
	}

	@Deprecated
	default IResource[] getResourcesFromCommand(String[] command, URI buildDirectoryURI) {
		return getResourcesFromCommand(Arrays.asList(command), buildDirectoryURI);
	}

	/**
	 * Strips the resources from the compile command. Use to produce the common
	 * parts of the command shared by a number of resources.
	 *
	 * @param command
	 *            the original compile command
	 * @param resources
	 *            the resources this command compiles for usually returned by
	 *            getResourcesFromCommand()
	 * @return the stripped command
	 * @since 6.1
	 */
	default List<String> stripCommand(List<String> command, IResource[] resources) {
		return command;
	}

	/**
	 * Determine if this toolchain supports targets with the given set of properties.
	 *
	 * @param properties the set of properties to test against
	 * @return does this toolchain support these properties
	 *
	 * @since 6.1
	 */
	default boolean matches(Map<String, String> properties) {
		for (Map.Entry<String, String> property : properties.entrySet()) {
			String tcValue = getProperty(property.getKey());
			// If toolchain doesn't have this property, it doesn't care
			if (tcValue != null && !property.getValue().equals(tcValue)) {
				return false;
			}
		}
		return true;
	}

}
