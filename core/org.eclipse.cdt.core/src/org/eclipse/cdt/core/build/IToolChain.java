/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	// Standard attributes
	static final String ATTR_OS = "os"; //$NON-NLS-1$
	static final String ATTR_ARCH = "arch"; //$NON-NLS-1$
	static final String ATTR_PACKAGE = "package"; //$NON-NLS-1$

	IToolChainProvider getProvider();

	String getId();

	String getVersion();

	String getName();

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

	void setProperty(String key, String value);

	IEnvironmentVariable getVariable(String name);

	IEnvironmentVariable[] getVariables();

	/**
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
	 * @since 6.1
	 */
	default IExtendedScannerInfo getDefaultScannerInfo(IBuildConfiguration buildConfig,
			IExtendedScannerInfo baseScannerInfo, ILanguage language, URI buildDirectoryURI) {
		return null;
	}

	String[] getErrorParserIds();

	Path getCommandPath(Path command);

	String[] getCompileCommands();

	/**
	 * @since 6.1
	 */
	default String[] getCompileCommands(ILanguage language) {
		return new String[0];
	}

	/**
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
	 * @since 6.1
	 */
	default List<String> stripCommand(List<String> command, IResource[] resources) {
		return command;
	}

	String getBinaryParserId();

}
