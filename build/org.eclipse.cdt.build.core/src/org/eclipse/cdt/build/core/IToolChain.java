/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.core.resources.IResource;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.osgi.service.prefs.Preferences;

/**
 * Represents a toolchain used to build and deploy systems.
 */
public interface IToolChain {

	IToolChainType getType();

	String getName();

	String getCommand();

	boolean supports(ILaunchTarget target);

	IExtendedScannerInfo getScannerInfo(String command, List<String> args, List<String> includePaths,
			IResource resource, Path buildDirectory) throws IOException;

	Collection<CConsoleParser> getConsoleParsers();

	void setEnvironment(Map<String, String> env);

	/**
	 * Called by the tool chain manager to save settings for this toolchain into
	 * the user's preferences.
	 * 
	 * @param properties
	 *            settings for the toolchain to be persisted
	 */
	void save(Preferences properties);

}
