/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMakeCommonBuildInfo {
	public final static String ARGS_PREFIX = MakeCorePlugin.getUniqueIdentifier();

	public final static String BUILD_LOCATION = ARGS_PREFIX + ".build.location"; //$NON-NLS-1$
	public final static String BUILD_COMMAND = ARGS_PREFIX + ".build.command"; //$NON-NLS-1$
	public final static String BUILD_ARGUMENTS = ARGS_PREFIX + ".build.arguments"; //$NON-NLS-1$

	void setBuildAttribute(String name, String value) throws CoreException;

	String getBuildAttribute(String name, String defaultValue);

	IPath getBuildLocation();

	/**
	 * @deprecated - use setBuildString(BUILD_LOCATION...)
	 */
	@Deprecated
	void setBuildLocation(IPath location) throws CoreException;

	boolean isStopOnError();

	void setStopOnError(boolean on) throws CoreException;

	boolean isDefaultBuildCmd();

	void setUseDefaultBuildCmd(boolean on) throws CoreException;

	IPath getBuildCommand();

	/**
	 * @deprecated - use setBuildString(BUILD_COMMAND...)
	 */
	@Deprecated
	void setBuildCommand(IPath command) throws CoreException;

	String getBuildArguments();

	/**
	 * @deprecated - use setBuildString(BUILD_ARGUMENTS...)
	 */
	@Deprecated
	void setBuildArguments(String args) throws CoreException;

	String[] getErrorParsers();

	void setErrorParsers(String[] parsers) throws CoreException;

	Map<String, String> getExpandedEnvironment() throws CoreException;

	Map<String, String> getEnvironment();

	void setEnvironment(Map<String, String> env) throws CoreException;

	boolean appendEnvironment();

	void setAppendEnvironment(boolean append) throws CoreException;
}
