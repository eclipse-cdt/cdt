/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.build.managed;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.build.managed.Target;
import org.eclipse.cdt.internal.core.build.managed.Tool;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;

/**
 * This is the main entry point for getting at the build information
 * for the managed build system. 
 */
public class ManagedBuildManager {

	/**
	 * Returns the list of platforms that are available to be used in
	 * conjunction with the given resource.  Generally this will include
	 * platforms defined by extensions as well as platforms defined by
	 * the project and all projects this project reference.
	 * 
	 * @param project
	 * @return
	 */
	public static ITarget[] getAvailableTargets(IProject project) {
		// Make sure the extensions are loaded
		loadExtensions();

		// Get the platforms for this project and all referenced projects
		
		// Create the array and copy the elements over
		ITarget[] targets = new ITarget[extensionTargets.size()];
		
		for (int i = 0; i < extensionTargets.size(); ++i)
			targets[i] = (ITarget)extensionTargets.get(i);
			
		return targets;
	}

	/**
	 * Returns the list of configurations belonging to the given platform
	 * that can be applied to the given project.  This does not include
	 * the configurations already applied to the project.
	 * 
	 * @param resource
	 * @param platform
	 * @return
	 */
	public static IConfiguration [] getAvailableConfigurations(IProject project, ITarget platform) {
		return null;
	}
	
	/**
	 * Returns the list of configurations associated with the given project.
	 * 
	 * @param project
	 * @return
	 */
	public static IConfiguration [] getConfigurations(IProject project) {
		return null;
	}

	/**
	 * Returns the list of configurations associated with a given file.
	 * 
	 * @param file
	 * @return
	 */
	public static IConfiguration[] getConfigurations(IFile file) {
		return null;
	}
	
	/**
	 * Creates a configuration containing the tools defined by the target.
	 * 
	 * @param target
	 * @param project
	 * @return
	 */
	public static IConfiguration createConfiguration(IProject project, ITarget target) {
		return null;
	}
	
	/**
	 * Creates a configuration that inherits from the parent configuration.
	 *  
	 * @param origConfig
	 * @param resource
	 * @return
	 */
	public static IConfiguration createConfiguration(IProject project, IConfiguration parentConfig) {
		return null;
	}

	/**
	 * Sets the String value for an option.
	 * 
	 * @param project
	 * @param config
	 * @param option
	 * @param value
	 */
	public static void setOptionValue(IProject project, IConfiguration config, IOption option, String value) {
	}
	
	/**
	 * Sets the String List value for an option.
	 * 
	 * @param project
	 * @param config
	 * @param option
	 * @param value
	 */
	public static void setOptionValue(IProject project, IConfiguration config, IOption option, String[] value) {
	}
	
	// Private stuff
	
	private static List extensionTargets;
	
	private static void loadExtensions() {
		if (extensionTargets != null)
			return;
			
		extensionTargets = new ArrayList();
		
		IExtensionPoint extensionPoint
			= CCorePlugin.getDefault().getDescriptor().getExtensionPoint("ManagedBuildInfo");
		IExtension[] extensions = extensionPoint.getExtensions();
		for (int i = 0; i < extensions.length; ++i) {
			IExtension extension = extensions[i];
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (int j = 0; j < elements.length; ++j) {
				IConfigurationElement element = elements[j];
				if (element.getName().equals("target")) {
					Target target = new Target(element.getAttribute("name"));
					extensionTargets.add(target);
					
					IConfigurationElement[] targetElements = element.getChildren();
					for (int k = 0; k < targetElements.length; ++k) {
						IConfigurationElement platformElement = targetElements[k];
						if (platformElement.getName().equals("tool")) {
							Tool tool = new Tool(platformElement.getAttribute("name"), target);
						}
					}
				}
			}
		}
	}
}
