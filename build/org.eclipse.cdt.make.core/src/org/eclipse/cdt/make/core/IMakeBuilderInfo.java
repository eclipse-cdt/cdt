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

	public String[] getPreprocessorSymbols();
    public String[] getIncludePaths();
    
	void setBuildLocation(IPath location);
    void setStopOnError(boolean on);
	void setUseDefaultBuildCmd(boolean on);
    void setBuildCommand(IPath command);
	void setBuildArguments(String args);
    
    void setAutoBuildEnable(boolean enabled);
	void setAutoBuildTarget(String target);
	void setIncrementalBuildEnable(boolean enabled);
	void setIncrementalBuildTarget(String target);
	void setFullBuildEnable(boolean enabled);
	void setFullBuildTarget(String target);
	
	public void setPreprocessorSymbols(String[] symbols);
	public void setIncludePaths(String[] paths);

}

