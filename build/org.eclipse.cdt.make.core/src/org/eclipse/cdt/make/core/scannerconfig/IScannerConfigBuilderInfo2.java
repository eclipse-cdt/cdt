/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IScannerConfigBuilderInfo2 {
	// general SCD options
	boolean isAutoDiscoveryEnabled();

	void setAutoDiscoveryEnabled(boolean enable);

	boolean isProblemReportingEnabled();

	void setProblemReportingEnabled(boolean enable);

	String getSelectedProfileId();

	void setSelectedProfileId(String profileId);

	List<String> getProfileIdList();

	// SCD profile - buildOutputProvider options
	boolean isBuildOutputFileActionEnabled();

	void setBuildOutputFileActionEnabled(boolean enable);

	String getBuildOutputFilePath();

	void setBuildOutputFilePath(String path);

	boolean isBuildOutputParserEnabled();

	void setBuildOutputParserEnabled(boolean enable);

	// SCD profile - scanner info provider options
	List<String> getProviderIdList();
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

	InfoContext getContext();

	/**
	 * Persist the buildInfo.
	 */
	void save() throws CoreException;
}
