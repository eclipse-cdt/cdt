package org.eclipse.cdt.make.core;
/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public interface IMakeBuilderInfo {
	IPath getBuildLocation();
	boolean isStopOnError();
	boolean isDefaultBuildCmd();
	IPath getBuildCommand();
	String getBuildArguments();
	
	boolean isAutoBuildEnable();
	String getAutoBuildTarget();
	boolean isIncrementalBuildEnabled();
	String getIncrementalBuildTarget();
	boolean isFullBuildEnabled();
	String getFullBuildTarget();
    
	void setBuildLocation(IPath location) throws CoreException;
    void setStopOnError(boolean on) throws CoreException;
	void setUseDefaultBuildCmd(boolean on) throws CoreException;
    void setBuildCommand(IPath command) throws CoreException;
	void setBuildArguments(String args) throws CoreException;
    
    void setAutoBuildEnable(boolean enabled) throws CoreException;
	void setAutoBuildTarget(String target) throws CoreException;
	void setIncrementalBuildEnable(boolean enabled) throws CoreException;
	void setIncrementalBuildTarget(String target) throws CoreException;
	void setFullBuildEnable(boolean enabled) throws CoreException;
	void setFullBuildTarget(String target) throws CoreException;
}

