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
import org.eclipse.cdt.internal.core.build.managed.Configuration;
import org.eclipse.cdt.internal.core.build.managed.Target;
import org.eclipse.cdt.internal.core.build.managed.Tool;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.QualifiedName;

/**
 * This is the main entry point for getting at the build information
 * for the managed build system. 
 */
public class ManagedBuildManager {

	private static final QualifiedName configProperty
		= new QualifiedName(CCorePlugin.PLUGIN_ID, "config");
		
	/**
	 * Returns the list of targets that are available to be used in
	 * conjunction with the given project.  Generally this will include
	 * targets defined by extensions as well as targets defined by
	 * the project and all projects this project reference.
	 * 
	 * @param project
	 * @return
	 */
	public static ITarget[] getTargets(IProject project) {
		// Make sure the extensions are loaded
		loadExtensions();

		// Get the targets for this project and all referenced projects
		
		// Create the array and copy the elements over
		ITarget[] targets = new ITarget[extensionTargets.size()];
		
		for (int i = 0; i < extensionTargets.size(); ++i)
			targets[i] = (ITarget)extensionTargets.get(i);
			
		return targets;
	}

	/**
	 * Returns the list of configurations associated with the given project.
	 * 
	 * @param project
	 * @return
	 */
	public static IConfiguration[] getConfigurations(IProject project) {
		return getResourceConfigs(project);
	}

	/**
	 * Returns the list of configurations associated with a given file.
	 * 
	 * @param file
	 * @return
	 */
	public static IConfiguration[] getConfigurations(IFile file) {
		// TODO not ready for prime time...
		return getResourceConfigs(file);
	}

	/**
	 * Adds a configuration containing the tools defined by the target to
	 * the given project.
	 * 
	 * @param target
	 * @param project
	 * @return
	 */
	public static IConfiguration addConfiguration(IProject project, ITarget target) {
		Configuration config = new Configuration(project, target);
		return null;
	}

	/**
	 * Adds a configuration inheriting from the given configuration.
	 * 
	 * @param origConfig
	 * @param resource
	 * @return
	 */
	public static IConfiguration addConfiguration(IProject project, IConfiguration parentConfig) {
		if (parentConfig.getProject() != null)
			// Can only inherit from target configs
			return null;
		
		Configuration config = new Configuration(project, parentConfig);
		addResourceConfig(project, config);
		return config;
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
					
					List configs = null;
					IConfigurationElement[] targetElements = element.getChildren();
					for (int k = 0; k < targetElements.length; ++k) {
						IConfigurationElement targetElement = targetElements[k];
						if (targetElement.getName().equals("tool")) {
							Tool tool = new Tool(targetElement.getAttribute("name"), target);
						} else if (targetElement.getName().equals("configuration")) {
							if (configs == null)
								configs = new ArrayList();
							configs.add(new Configuration(target));
						}
					}
					
					if (configs != null) {
						IConfiguration[] configArray = new IConfiguration[configs.size()];
						configArray = (IConfiguration[])configs.toArray(configArray);
						target.setConfigurations(configArray);
					}
				}
			}
		}
	}
	
	private static final IConfiguration[] emptyConfigs = new IConfiguration[0];
	
	private static IConfiguration[] getResourceConfigs(IResource resource) {
		IConfiguration[] configs = null;
		
		try {
			configs = (IConfiguration[])resource.getSessionProperty(configProperty);
		} catch (CoreException e) {
		}
		
		return (configs != null) ? configs : emptyConfigs;
	}

	private static void addResourceConfig(IResource resource, IConfiguration config) {
		IConfiguration[] configs = getResourceConfigs(resource);
		
		IConfiguration[] newConfigs = new IConfiguration[configs.length + 1];
		for (int i = 0; i < configs.length; ++i)
			newConfigs[i] = configs[i];
		newConfigs[configs.length] = config;
		
		try {
			resource.setSessionProperty(configProperty, newConfigs);
		} catch (CoreException e) {
		}

		// TODO save the config info to the project build file
	}

}
