/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.internal.core.scannerconfig.util.SymbolEntry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Element;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
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
        Map<String, String> getSymbols();
		
        IDiscoveredScannerInfoSerializable getSerializable();
	}
    
    interface IPerProjectDiscoveredPathInfo extends IDiscoveredPathInfo {
        void setIncludeMap(LinkedHashMap<String, Boolean> map);
        void setSymbolMap(LinkedHashMap<String, SymbolEntry> map);

        LinkedHashMap<String, Boolean> getIncludeMap();
        LinkedHashMap<String, SymbolEntry> getSymbolMap();
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
        Map<String, String> getSymbols(IPath path);
        
        /**
         * Get include files (gcc option -include) for the specific path (file)
         */
        IPath[] getIncludeFiles(IPath path);
        /**
         * Get macro files (gcc option -imacros) for the specific path (file)
         */
        IPath[] getMacroFiles(IPath path);
		/**
		 * Returns if there is any discovered scanner info for the path
		 */
		boolean isEmpty(IPath path);
    }
    
    interface IPerFileDiscoveredPathInfo2 extends IPerFileDiscoveredPathInfo {
    	/**
    	 * returns the map containing {@link IResource} - to - {@link PathInfo} pairs representing 
    	 * complete set of discovered information for the whole project
    	 * 
    	 * @return Map
    	 */
    	Map<IResource, PathInfo> getPathInfoMap();
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

	IDiscoveredPathInfo getDiscoveredInfo(IProject project, InfoContext context) throws CoreException;

	IDiscoveredPathInfo getDiscoveredInfo(IProject project, InfoContext context, boolean defaultToProjectSettings) throws CoreException;

	IDiscoveredPathInfo getDiscoveredInfo(IProject project) throws CoreException;
	void removeDiscoveredInfo(IProject project);
	void removeDiscoveredInfo(IProject project, InfoContext context);
	void updateDiscoveredInfo(InfoContext context, IDiscoveredPathInfo info, boolean updateContainer, List<IResource> changedResources) throws CoreException;
	void updateDiscoveredInfo(IDiscoveredPathInfo info, List<IResource> changedResources) throws CoreException;
    void changeDiscoveredContainer(IProject project, ScannerConfigScope profileScope, List<IResource> changedResources);

	void addDiscoveredInfoListener(IDiscoveredInfoListener listener);
	void removeDiscoveredInfoListener(IDiscoveredInfoListener listener);
}
