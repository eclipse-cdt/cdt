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
package org.eclipse.cdt.internal.core.build.managed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.build.managed.BuildException;
import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.cdt.core.build.managed.ITool;
import org.eclipse.cdt.core.build.managed.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 */
public class Target extends BuildObject implements ITarget {

	private ITarget parent;
	private IResource owner;
	private List tools;
	private Map toolMap;
	private List configurations;
	private boolean isAbstract = false;

	private static final IConfiguration[] emptyConfigs = new IConfiguration[0];
	
	public Target(IResource owner) {
		this.owner = owner;
	}
	
	/**
	 * Create a target owned by a resource based on a parent target 
	 * @param parent
	 */
	public Target(IResource owner, ITarget parent) {
		this(owner);
		this.parent = parent;

		inheritConfigs();

		// Copy the parent's identity
		setId(parent.getId());		
		setName(parent.getName());

		// Hook me up
		ResourceBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(owner, true);
		buildInfo.addTarget(this);
	}

	/**
	 * This constructor is called to create a target defined by an extension.
	 * 
	 * @param element
	 */
	public Target(IConfigurationElement element) {
		// id
		setId(element.getAttribute("id"));
		
		// hook me up
		ManagedBuildManager.addExtensionTarget(this);
		
		// name
		setName(element.getAttribute("name"));

		// parent
		String parentId = element.getAttribute("parent");
		if (parentId != null) {
			parent = ManagedBuildManager.getTarget(null, parentId);

			// Inherit the configs from the parent
			inheritConfigs();
		}

		// isAbstract
		if ("true".equals(element.getAttribute("isAbstract")))
			isAbstract = true;

		IConfigurationElement[] targetElements = element.getChildren();
		for (int k = 0; k < targetElements.length; ++k) {
			IConfigurationElement targetElement = targetElements[k];
			if (targetElement.getName().equals("tool")) {
				new Tool(this, targetElement);
			} else if (targetElement.getName().equals("configuration")) {
				new Configuration(this, targetElement);
			}
		}

	}

	public Target(ResourceBuildInfo buildInfo, Element element) {
		this(buildInfo.getOwner());
		
		// id
		setId(element.getAttribute("id"));
		
		// hook me up
		buildInfo.addTarget(this);
		
		// name
		setName(element.getAttribute("name"));

		// parent
		String parentId = element.getAttribute("parent");
		if (parentId != null) {
			parent = ManagedBuildManager.getTarget(null, parentId);

			// Inherit the configs from the parent
			inheritConfigs();
		}

		// isAbstract
		if ("true".equals(element.getAttribute("isAbstract")))
			isAbstract = true;

	}
	private void inheritConfigs() {
		IConfiguration[] parentConfigs = parent.getConfigurations();
		if (parentConfigs.length > 0)
			configurations = new ArrayList(parentConfigs.length);
		for (int i = 0; i < parentConfigs.length; ++i)
			configurations.add(new Configuration(parentConfigs[i]));
		
	}
	
	public String getName() {
		return (name == null && parent != null) ? parent.getName() : name;
	}

	public ITarget getParent() {
		return parent;
	}
	
	public IResource getOwner() {
		return owner;
	}

	private int getNumTools() {
		int n = (tools == null) ? 0 : tools.size();
		if (parent != null)
			n += ((Target)parent).getNumTools();
		return n;
	}
	
	private int addToolsToArray(ITool[] toolArray, int start) {
		int n = start;
		if (parent != null)
			n = ((Target)parent).addToolsToArray(toolArray, start);

		if (tools != null) {
			for (int i = 0; i < tools.size(); ++i)
				toolArray[n++] = (ITool)tools.get(i); 
		}
		
		return n;
	}
	
	public ITool[] getTools() {
		ITool[] toolArray = new ITool[getNumTools()];
		addToolsToArray(toolArray, 0);
		return toolArray;
	}

	public ITool getTool(String id) {
		return (ITool)toolMap.get(id);
	}

	public void addTool(ITool tool) {
		if (tools == null) {
			tools = new ArrayList();
			toolMap = new HashMap();
		}
		
		tools.add(tool);
		toolMap.put(tool.getId(), tool);
	}
	
	public IConfiguration[] getConfigurations() {
		if (configurations != null)
			return (IConfiguration[])configurations.toArray(new IConfiguration[configurations.size()]);
		else
			return emptyConfigs;
	}

	public void addConfiguration(IConfiguration configuration) {
		if (configurations == null)
			configurations = new ArrayList();
		configurations.add(configuration);
	}
	
	private void addLocalConfiguration(IConfiguration configuration) {
		if (configurations == null)
			configurations = new ArrayList();
		configurations.add(configuration);
	}
	
	public IConfiguration createConfiguration()
		throws BuildException
	{
		IConfiguration config = new Configuration(this);
		addLocalConfiguration(config);
		return config;
	}

	public IConfiguration createConfiguration(IConfiguration parentConfig)
		throws BuildException
	{
		IResource parentOwner = parentConfig.getOwner();
		
		if (owner instanceof IProject) {
			// parent must be owned by the same project
			if (!owner.equals(parentOwner))
				throw new BuildException("addConfiguration: parent must be in same project");
		} else {
			// parent must be owned by the project
			if (!owner.getProject().equals(parentOwner))
				throw new BuildException("addConfiguration: parent must be in owning project");
		}

		// Validation passed
		IConfiguration config = new Configuration(parentConfig);
		addLocalConfiguration(config);
		return config;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#isAbstract()
	 */
	public boolean isAbstract() {
		return isAbstract;
	}
	
	public void serialize(Document doc, Element element) {
		element.setAttribute("id", getId());
		element.setAttribute("name", getName());
		if (parent != null)
			element.setAttribute("parent", parent.getId());
	}

}
