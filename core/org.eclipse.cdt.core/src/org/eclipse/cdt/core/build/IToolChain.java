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

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
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

	IToolChainType getType();

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

	IEnvironmentVariable getVariable(String name);

	IEnvironmentVariable[] getVariables();

	IExtendedScannerInfo getScannerInfo(IBuildConfiguration buildConfig, Path command, String[] args,
			IExtendedScannerInfo baseScannerInfo, IResource resource, URI buildDirectoryURI);

	String[] getErrorParserIds();

	Path getCommandPath(String command);

	IResource[] getResourcesFromCommand(String[] command, URI buildDirectoryURI);
	
}
