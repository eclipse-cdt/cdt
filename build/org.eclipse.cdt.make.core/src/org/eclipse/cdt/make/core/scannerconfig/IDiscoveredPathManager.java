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
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Element;

public interface IDiscoveredPathManager {

	interface IDiscoveredPathInfo {

		IProject getProject();

        /**
         * Get include paths for the whole project 
         */
        IPath[] getIncludePaths();
        /**
         * Get defined symbols for the whole project 
         */
		Map 	getSymbols();
		
        IDiscoveredScannerInfoSerializable getSerializable();
	}
    
    interface IPerProjectDiscoveredPathInfo extends IDiscoveredPathInfo {
        void setIncludeMap(LinkedHashMap map);
        void setSymbolMap(LinkedHashMap map);

        LinkedHashMap getIncludeMap();
        LinkedHashMap getSymbolMap();
    }

    interface IPerFileDiscoveredPathInfo extends IDiscoveredPathInfo {
        /**
         * Get include paths for the specific path (file) 
         */
        IPath[] getIncludePaths(IPath path);
        /**
         * Get quote include paths (for #include "...") for the specific path (file)
         */
        IPath[] getQuoteIncludePaths(IPath path);
        /**
         * Get defined symbols for the specific path (file) 
         */
        Map     getSymbols(IPath path);
        
        /**
         * Get include files (gcc option -include) for the specific path (file)
         */
        IPath[] getIncludeFiles(IPath path);
        /**
         * Get macro files (gcc option -imacros) for the specific path (file)
         */
        IPath[] getMacroFiles(IPath path);
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
	void updateDiscoveredInfo(IDiscoveredPathInfo info, List changedResources) throws CoreException;
    void changeDiscoveredContainer(IProject project, ScannerConfigScope profileScope, List changedResources);

	void addDiscoveredInfoListener(IDiscoveredInfoListener listener);
	void removeDiscoveredInfoListener(IDiscoveredInfoListener listener);
}
