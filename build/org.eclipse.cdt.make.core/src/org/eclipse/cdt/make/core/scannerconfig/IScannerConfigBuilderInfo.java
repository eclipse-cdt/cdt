/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.core.scannerconfig;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Settings for ScannerConfigBuilder
 * 
 * @author vhirsl
 */
public interface IScannerConfigBuilderInfo {
	boolean isAutoDiscoveryEnabled();
	void setAutoDiscoveryEnabled(boolean enabled) throws CoreException;

	String getMakeBuilderConsoleParserId();
	void setMakeBuilderConsoleParserId(String parserId) throws CoreException;

	boolean isESIProviderCommandEnabled();
	void setESIProviderCommandEnabled(boolean enabled) throws CoreException;

	boolean isDefaultESIProviderCmd();
	void setUseDefaultESIProviderCmd(boolean on) throws CoreException;

	IPath getESIProviderCommand();
	void setESIProviderCommand(IPath command) throws CoreException;

	String getESIProviderArguments();
	void setESIProviderArguments(String args) throws CoreException;

	String getESIProviderConsoleParserId();
	void setESIProviderConsoleParserId(String parserId) throws CoreException;

	boolean isMakeBuilderConsoleParserEnabled();
	void setMakeBuilderConsoleParserEnabled(boolean enabled) throws CoreException;

	boolean isSIProblemGenerationEnabled();
	void setSIProblemGenerationEnabled(boolean enabled) throws CoreException;
}
