/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import java.util.List;

import org.eclipse.core.runtime.CoreException;

/**
 * New interface to replace IScannerConfigBuildInfo
 * Persisted in .cdtproject file
 * 
 * @author vhirsl
 */
public interface IScannerConfigBuilderInfo2 {
	// general SCD options
	boolean isAutoDiscoveryEnabled();
	void setAutoDiscoveryEnabled(boolean enable);

	boolean isProblemReportingEnabled();
	void setProblemReportingEnabled(boolean enable);
	
	String getSelectedProfileId();
	void setSelectedProfileId(String profileId);

    List getProfileIdList();
    
	// SCD profile - buildOutputProvider options 
	boolean isBuildOutputFileActionEnabled();
	void setBuildOutputFileActionEnabled(boolean enable);
	
	String getBuildOutputFilePath();
	void setBuildOutputFilePath(String path);
	
	boolean isBuildOutputParserEnabled();
	void setBuildOutputParserEnabled(boolean enable);
	
	// SCD profile - scanner info provider options
	List getProviderIdList();
//	void addSIProvider(String providerId);
//	void removeSIProvider(String providerId);
	
	boolean isProviderOutputParserEnabled(String providerId);
	void setProviderOutputParserEnabled(String providerId, boolean enable);
	
	boolean isUseDefaultProviderCommand(String providerId);
	void setUseDefaultProviderCommand(String providerId, boolean enable);
	
	String getProviderRunCommand(String providerId);
	void setProviderRunCommand(String providerId, String command);
	
	String getProviderRunArguments(String providerId);
	void setProviderRunArguments(String providerId, String arguments);
	
	String getProviderOpenFilePath(String providerId);
	void setProviderOpenFilePath(String providerId, String filePath);
	
    /**
     * Persist the buildInfo.
     * @throws CoreException
     */
    void save() throws CoreException;
}
