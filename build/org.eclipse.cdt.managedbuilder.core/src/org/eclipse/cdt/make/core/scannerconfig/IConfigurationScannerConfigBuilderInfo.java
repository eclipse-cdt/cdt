/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;


public interface IConfigurationScannerConfigBuilderInfo {
	boolean isPerRcTypeDiscovery();
	
	void setPerRcTypeDiscovery(boolean on);
	
	Map getInfoMap();
	
	InfoContext[] getContexts();
	
	IScannerConfigBuilderInfo2 getInfo(InfoContext context);
	
	IScannerConfigBuilderInfo2 applyInfo(InfoContext context, IScannerConfigBuilderInfo2 base) throws CoreException;
	
	IScannerConfigBuilderInfo2 getDefaultInfo();
	
//	boolean isAutoDiscoveryEnabled();
//	void setAutoDiscoveryEnabled(boolean enable);
//
//	boolean isProblemReportingEnabled();
//	void setProblemReportingEnabled(boolean enable);
//	
//	String getSelectedProfileId();
//	void setSelectedProfileId(String profileId);
//
//    List getProfileIdList();
//    
//	// SCD profile - buildOutputProvider options 
//	boolean isBuildOutputFileActionEnabled();
//	void setBuildOutputFileActionEnabled(boolean enable);
//	
//	String getBuildOutputFilePath();
//	void setBuildOutputFilePath(String path);
//	
//	boolean isBuildOutputParserEnabled();
//	void setBuildOutputParserEnabled(boolean enable);
//	
//	// SCD profile - scanner info provider options
//	List getProviderIdList();
////	void addSIProvider(String providerId);
////	void removeSIProvider(String providerId);
//	
//	boolean isProviderOutputParserEnabled(String providerId);
//	void setProviderOutputParserEnabled(String providerId, boolean enable);
//	
//	boolean isUseDefaultProviderCommand(String providerId);
//	void setUseDefaultProviderCommand(String providerId, boolean enable);
//	
//	String getProviderRunCommand(String providerId);
//	void setProviderRunCommand(String providerId, String command);
//	
//	String getProviderRunArguments(String providerId);
//	void setProviderRunArguments(String providerId, String arguments);
//	
//	String getProviderOpenFilePath(String providerId);
//	void setProviderOpenFilePath(String providerId, String filePath);
}
