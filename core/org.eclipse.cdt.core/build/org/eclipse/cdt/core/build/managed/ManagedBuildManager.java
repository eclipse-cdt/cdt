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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.build.managed.Configuration;
import org.eclipse.cdt.internal.core.build.managed.Target;
import org.eclipse.cdt.internal.core.build.managed.Tool;
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

	private static final QualifiedName ownedTargetsProperty
		= new QualifiedName(CCorePlugin.PLUGIN_ID, "ownedTargets");
	private static final QualifiedName definedTargetsProperty
		= new QualifiedName(CCorePlugin.PLUGIN_ID, "definedTargets"); 

	private static final ITarget[] emptyTargets = new ITarget[0];
	
	/**
	 * Returns the list of targets that are defined by this project,
	 * projects referenced by this project, and by the extensions. 
	 * 
	 * @param project
	 * @return
	 */
	public static ITarget[] getDefinedTargets(IProject project) {
		// Make sure the extensions are loaded
		loadExtensions();

		// Get the targets for this project and all referenced projects
		List definedTargets = null;

		if (project != null) {
			try {
				definedTargets = (List)project.getSessionProperty(definedTargetsProperty);
			} catch (CoreException e) {
			}
		}

		// Create the array and copy the elements over
		int size = extensionTargets.size()
			+ (definedTargets != null ? definedTargets.size() : 0);

		ITarget[] targets = new ITarget[size];
		
		int n = 0;
		for (int i = 0; i < extensionTargets.size(); ++i)
			targets[n++] = (ITarget)extensionTargets.get(i);
		
		if (definedTargets != null)
			for (int i = 0; i < definedTargets.size(); ++i)
				targets[n++] = (ITarget)definedTargets.get(i);
				
		return targets;
	}

	/**
	 * Returns the targets owned by this project.  If none are owned,
	 * an empty array is returned.
	 * 
	 * @param project
	 * @return
	 */
	public static ITarget[] getTargets(IResource resource) {
		List targets = getOwnedTargetsProperty(resource);
		
		if (targets != null) {
			return (ITarget[])targets.toArray(new ITarget[targets.size()]);
		} else {
			return emptyTargets;
		}
	}

	/**
	 * Adds a new target to the resource based on the parentTarget.
	 * 
	 * @param resource
	 * @param parentTarget
	 * @return
	 * @throws BuildException
	 */
	public static ITarget addTarget(IResource resource, ITarget parentTarget)
		throws BuildException
	{
		IResource owner = parentTarget.getOwner();
		
		if (owner != null && owner.equals(resource))
			// Already added
			return parentTarget; 
			
		if (resource instanceof IProject) {
			// Owner must be null
			if (owner != null)
				throw new BuildException("addTarget: owner not null");
		} else {
			// Owner must be owned by the project containing this resource
			if (owner == null)
				throw new BuildException("addTarget: null owner");
			if (!owner.equals(resource.getProject()))
				throw new BuildException("addTarget: owner not project");
		}
		
		// Passed validation
		List targets = getOwnedTargetsProperty(resource);
		if (targets == null) {
			targets = new ArrayList();
			try {
				resource.setSessionProperty(ownedTargetsProperty, targets);
			} catch (CoreException e) {
				throw new BuildException("addTarget: could not add property");
			}
		}
		
		Target newTarget = new Target(resource, parentTarget);
		targets.add(newTarget);
		return newTarget;
	}
	
	/**
	 * Saves the build information associated with a project and all resources
	 * in the project to the build info file.
	 * 
	 * @param project
	 */
	public static void saveBuildInfo(IProject project) {
	}
	
	// Private stuff
	
	private static List extensionTargets;
	
	private static void loadExtensions() {
		if (extensionTargets != null)
			return;
			
		extensionTargets = new ArrayList();
		Map targetMap = new HashMap(); 
		
		IExtensionPoint extensionPoint
			= CCorePlugin.getDefault().getDescriptor().getExtensionPoint("ManagedBuildInfo");
		IExtension[] extensions = extensionPoint.getExtensions();
		for (int i = 0; i < extensions.length; ++i) {
			IExtension extension = extensions[i];
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (int j = 0; j < elements.length; ++j) {
				IConfigurationElement element = elements[j];
				if (element.getName().equals("target")) {
					String parentId = element.getAttribute("parent");
					Target target = null;
					if (parentId != null)
						target  = new Target(null, (Target)targetMap.get(parentId));
					else
						target = new Target(null);
					target.setName(element.getAttribute("name"));
					extensionTargets.add(target);
					targetMap.put(element.getAttribute("id"), target);
					
					IConfigurationElement[] targetElements = element.getChildren();
					for (int k = 0; k < targetElements.length; ++k) {
						IConfigurationElement targetElement = targetElements[k];
						if (targetElement.getName().equals("tool")) {
							ITool tool = target.createTool();
							tool.setName(targetElement.getAttribute("name"));
							
							Map categoryMap = new HashMap();
							categoryMap.put(targetElement.getAttribute("id"), tool.getTopOptionCategory());
							IConfigurationElement[] toolElements = targetElement.getChildren();
							for (int l = 0; l < toolElements.length; ++l) {
								IConfigurationElement toolElement = toolElements[l];
								if (toolElement.getName().equals("option")) {
									IOption option = tool.createOption();
									option.setName(toolElement.getAttribute("name"));
									
									String categoryId = toolElement.getAttribute("category");
									if (categoryId != null)
										option.setCategory((IOptionCategory)categoryMap.get(categoryId));
								} else if (toolElement.getName().equals("optionCategory")) {
									IOptionCategory owner = (IOptionCategory)categoryMap.get(toolElement.getAttribute("owner"));
									IOptionCategory category = owner.createChildCategory();
									category.setName(toolElement.getAttribute("name"));
									categoryMap.put(toolElement.getAttribute("id"), category);
								}
							}
						} else if (targetElement.getName().equals("configuration")) {
							try {
								IConfiguration config = target.createConfiguration();
								config.setName(targetElement.getAttribute("name"));
							} catch (BuildException e) {
								// Not sure what to do here.
							}
						}
					}
				}
			}
		}
	}
	
	private static List getOwnedTargetsProperty(IResource resource) {
		try {
			return (List)resource.getSessionProperty(ownedTargetsProperty);
		} catch (CoreException e) {
			return null;
		}
	}
}
