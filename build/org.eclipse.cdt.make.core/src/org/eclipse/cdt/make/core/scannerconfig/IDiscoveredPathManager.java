/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public interface IDiscoveredPathManager {

	interface IDiscoveredPathInfo {

		IProject getProject();

		IPath[] getIncludePaths();
		Map 	getSymbols();
		
		void setIncludeMap(LinkedHashMap map);
		void setSymbolMap(LinkedHashMap map);

		LinkedHashMap getIncludeMap();
		LinkedHashMap getSymbolMap();
	}

	interface IDiscoveredInfoListener {

		void infoChanged(IDiscoveredPathInfo info);
		void infoRemoved(IProject project);
	}

	IDiscoveredPathInfo getDiscoveredInfo(IProject project) throws CoreException;
	void removeDiscoveredInfo(IProject project);
	void updateDiscoveredInfo(IDiscoveredPathInfo info) throws CoreException;

	void addDiscoveredInfoListener(IDiscoveredInfoListener listener);
	void removeDiscoveredInfoListener(IDiscoveredInfoListener listener);
}
