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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Settings for ScannerConfigBuilder
 *
 * @author vhirsl
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
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
