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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Element;

public interface IDiscoveredPathManager {

	interface IDiscoveredPathInfo {

		IProject getProject();

        /**
         * Get include paths for the whole project 
         * @return
         */
        IPath[] getIncludePaths();
        /**
         * Get defined symbols for the whole project 
         * @return
         */
		Map 	getSymbols();
		
        /**
         * Get include paths for the specific path (file) 
         * @return
         */
        IPath[] getIncludePaths(IPath path);
        /**
         * Get defined symbols for the specific path (file) 
         * @return
         */
        Map     getSymbols(IPath path);
        
        IDiscoveredScannerInfoSerializable getSerializable();
        ScannerConfigScope getScope();
        
        void setIncludeMap(LinkedHashMap map);
		void setSymbolMap(LinkedHashMap map);

		LinkedHashMap getIncludeMap();
		LinkedHashMap getSymbolMap();
	}

    interface IDiscoveredScannerInfoSerializable {
        /**
         * Serialize discovered scanner info to an XML element
         * 
         * @param root
         */
        public void serialize(Element root);
        
        /**
         * Deserialize discovered scanner info from an XML element
         * 
         * @param root
         */
        public void deserialize(Element root);
        
        /**
         * @return an id of the collector
         */
        public String getCollectorId();
    }
    
    interface IDiscoveredInfoListener {

		void infoChanged(IDiscoveredPathInfo info);
		void infoRemoved(IDiscoveredPathInfo info);
	}

	IDiscoveredPathInfo getDiscoveredInfo(IProject project) throws CoreException;
	void removeDiscoveredInfo(IProject project);
	void updateDiscoveredInfo(IDiscoveredPathInfo info) throws CoreException;
    /**
     * @param project
     * @param profileScope
     * @throws CModelException 
     * @throws CoreException 
     */
    void changeDiscoveredContainer(IProject project, ScannerConfigScope profileScope);

	void addDiscoveredInfoListener(IDiscoveredInfoListener listener);
	void removeDiscoveredInfoListener(IDiscoveredInfoListener listener);
}
