/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.managedbuilder.core.IBuildPathResolver;
import org.eclipse.cdt.managedbuilder.core.IEnvVarBuildPath;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public class EnvVarBuildPath implements
		IEnvVarBuildPath {
	private ITool tool;
	
	private int type;
	private String variableNames[];
	private String pathDelimiter;
	private IBuildPathResolver buildPathResolver;
	private IConfigurationElement buildPathResolverElement;


	/**
	 * Constructor to create an EnvVarBuildPath based on an element from the plugin
	 * manifest. 
	 * 
	 * @param element The element containing the information about the tool.
	 */
	public EnvVarBuildPath(ITool tool, IManagedConfigElement element) {
		this.tool = tool;
		loadFromManifest(element);
	}
	
	/* (non-Javadoc)
	 * Load the EnvVarBuildPath information from the XML element specified in the 
	 * argument
	 * @param element An XML element containing the tool information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {
		
		setType(convertPathTypeToInt(element.getAttribute(TYPE)));
		
		setVariableNames(element.getAttribute(LIST));
		
		setPathDelimiter(element.getAttribute(PATH_DELIMITER));
		
		// Store the configuration element IFF there is a build path resolver defined 
		String buildPathResolver = element.getAttribute(BUILD_PATH_RESOLVER); 
		if (buildPathResolver != null && element instanceof DefaultManagedConfigElement) {
			buildPathResolverElement = ((DefaultManagedConfigElement)element).getConfigurationElement();			
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IEnvVarBuildPath#getType()
	 */
	public int getType() {
		return type;
	}
	
	public void setType(int type){
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IEnvVarBuildPath#getVariableNames()
	 */
	public String[] getVariableNames() {
		return variableNames;
	}
	
	public void setVariableNames(String names[]){
		this.variableNames = names;
	}

	public void setVariableNames(String names){
		setVariableNames(getNamesFromString(names));
	}
	
	public String[] getNamesFromString(String names){
		if(names == null)
			return null;
		return names.split(NAME_SEPARATOR);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IEnvVarBuildPath#getPathDelimiter()
	 */
	public String getPathDelimiter() {
		return pathDelimiter;
	}

	public void setPathDelimiter(String delimiter) {
		if(delimiter == null)
			delimiter = ManagedBuildManager.getEnvironmentVariableProvider().getDefaultDelimiter();
		this.pathDelimiter = delimiter;
	}

	private int convertPathTypeToInt(String pathType){
		if(pathType != null && TYPE_LIBRARY.equals(pathType))
			return BUILDPATH_LIBRARY;
		return BUILDPATH_INCLUDE;
	}

	private String convertPathTypeToString(int pathType){
		switch(pathType){
		case BUILDPATH_LIBRARY:
			return TYPE_LIBRARY;
		case BUILDPATH_INCLUDE:
		default:
			return TYPE_INCLUDE;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IEnvVarBuildPath#getBuildPathResolver()
	 */
	public IBuildPathResolver getBuildPathResolver() {
		if(buildPathResolver == null && buildPathResolverElement != null){
			try {
				if (buildPathResolverElement.getAttribute(BUILD_PATH_RESOLVER) != null) {
					buildPathResolver = (IBuildPathResolver) buildPathResolverElement.createExecutableExtension(BUILD_PATH_RESOLVER);
				}
			} catch (CoreException e) {}
		}
		return buildPathResolver;
	}

}
