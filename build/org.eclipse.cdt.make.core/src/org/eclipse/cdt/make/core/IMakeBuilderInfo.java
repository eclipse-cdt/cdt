/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public interface IMakeBuilderInfo {
	IPath getBuildLocation();
	void setBuildLocation(IPath location) throws CoreException;

	boolean isStopOnError();
	void setStopOnError(boolean on) throws CoreException;

	boolean isDefaultBuildCmd();
	void setUseDefaultBuildCmd(boolean on) throws CoreException;

	IPath getBuildCommand();
	void setBuildCommand(IPath command) throws CoreException;

	String getBuildArguments();
	void setBuildArguments(String args) throws CoreException;

	boolean isAutoBuildEnable();
	void setAutoBuildEnable(boolean enabled) throws CoreException;

	String getAutoBuildTarget();
	void setAutoBuildTarget(String target) throws CoreException;

	boolean isIncrementalBuildEnabled();
	void setIncrementalBuildEnable(boolean enabled) throws CoreException;

	String getIncrementalBuildTarget();
	void setIncrementalBuildTarget(String target) throws CoreException;

	boolean isFullBuildEnabled();
	void setFullBuildEnable(boolean enabled) throws CoreException;

	String getFullBuildTarget();
	void setFullBuildTarget(String target) throws CoreException;

	String getCleanBuildTarget();
	void setCleanBuildTarget(String target) throws CoreException;

	boolean isCleanBuildEnabled();
	void setCleanBuildEnable(boolean enabled) throws CoreException;
	
	String[] getErrorParsers();
	void setErrorParsers(String[] parsers) throws CoreException;

	Map getEnvironment();
	void setEnvironment(Map env) throws CoreException;
}
