/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;


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
	void setBuildLocation(IPath location) throws CoreException;
	
	boolean isStopOnError();
	void setStopOnError(boolean on) throws CoreException;

	boolean isDefaultBuildCmd();
	void setUseDefaultBuildCmd(boolean on) throws CoreException;

	IPath getBuildCommand();

	/**
	 * @deprecated - use setBuildString(BUILD_COMMAND...)
	 */
	void setBuildCommand(IPath command) throws CoreException;

	String getBuildArguments();

	/**
	 * @deprecated - use setBuildString(BUILD_ARGUMENTS...)
	 */
	void setBuildArguments(String args) throws CoreException;

	String[] getErrorParsers();
	void setErrorParsers(String[] parsers) throws CoreException;

	Map getExpandedEnvironment();

	Map getEnvironment();
	void setEnvironment(Map env) throws CoreException;
	
	boolean appendEnvironment();
	void setAppendEnvironment(boolean append) throws CoreException;
}
