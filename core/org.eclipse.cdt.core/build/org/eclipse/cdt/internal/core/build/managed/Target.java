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

import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.IResourceBuildInfo;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.cdt.core.build.managed.ITool;
import org.eclipse.cdt.core.build.managed.ManagedBuildManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 */
public class Target extends BuildObject implements ITarget {

	private ITarget parent;
	private IResource owner;
	private List tools;
	private Map toolMap;
	private List configurations;
	private Map configMap;
	private boolean isAbstract = false;
	private boolean isTest = false;
	private String artifactName;
	private String defaultExtension;

	private static final IConfiguration[] emptyConfigs = new IConfiguration[0];
	private static final String EMPTY_STRING = new String();
	
	public Target(IResource owner) {
		this.owner = owner;
	}
	
	/**
	 * Create a target owned by a resource based on a parent target
	 * 
	 * @param owner 
	 * @param parent
	 */
	public Target(IResource owner, ITarget parent) {
		// Make the owner of the target the project resource
		this(owner);
		
		// Copy the parent's identity
		this.parent = parent;
		setId(parent.getId() + ".1");		
		setName(parent.getName());
		this.artifactName = parent.getArtifactName();
		this.defaultExtension = parent.getDefaultExtension();
		this.isTest = parent.isTestTarget();

		// Hook me up
		IResourceBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(owner, true);
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
		
		// Get the target name
		setName(element.getAttribute("name"));

		// Get the name of the build artifact associated with target (usually 
		// in the plugin specification).
		artifactName = element.getAttribute("artifactName");
		
		// Get the default extension
		defaultExtension = element.getAttribute("defaultExtension");

		// parent
		String parentId = element.getAttribute("parent");
		if (parentId != null) {
			parent = ManagedBuildManager.getTarget(null, parentId);
			// copy over the parents configs
			IConfiguration[] parentConfigs = parent.getConfigurations();
			for (int i = 0; i < parentConfigs.length; ++i)
				addConfiguration(parentConfigs[i]);
		}

		// isAbstract
		if ("true".equals(element.getAttribute("isAbstract")))
			isAbstract = true;

		// Is this a test target
		isTest = ("true".equals(element.getAttribute("isTest")));

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

	/**
	 * Create target from project file
	 * 
	 * @param buildInfo
	 * @param element
	 */
	public Target(ResourceBuildInfo buildInfo, Element element) {
		this(buildInfo.getOwner());
		
		// id
		setId(element.getAttribute("id"));
		
		// hook me up
		buildInfo.addTarget(this);
		
		// name
		setName(element.getAttribute("name"));

		// Get the name of the build artifact associated with target (should
		// contain what the user entered in the UI).
		artifactName = element.getAttribute("artifactName");

		// Get the default extension
		defaultExtension = element.getAttribute("defaultExtension");

		// parent
		String parentId = element.getAttribute("parent");
		if (parentId != null)
			parent = ManagedBuildManager.getTarget(null, parentId);

		// isAbstract
		if ("true".equals(element.getAttribute("isAbstract")))
			isAbstract = true;
			
		// Is this a test target
		isTest = ("true".equals(element.getAttribute("isTest")));
	
		Node child = element.getFirstChild();
		while (child != null) {
			if (child.getNodeName().equals("configuration")) {
				new Configuration(this, (Element)child);
			}
			child = child.getNextSibling();
		}
	}
	
	/**
	 * Persist receiver to project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {
		element.setAttribute("id", getId());
		element.setAttribute("name", getName());
		if (parent != null)
			element.setAttribute("parent", parent.getId());
		element.setAttribute("isAbstract", isAbstract ? "true" : "false");
		element.setAttribute("artifactName", getArtifactName());
		element.setAttribute("defaultExtension", getDefaultExtension());
		element.setAttribute("isTest", isTest ? "true" : "false");
				
		if (configurations != null)
			for (int i = 0; i < configurations.size(); ++i) {
				Configuration config = (Configuration)configurations.get(i);
				Element configElement = doc.createElement("configuration");
				element.appendChild(configElement);
				config.serialize(doc, configElement);
			}
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
		ITool result = null;
		// See if receiver has it in list
		result = (ITool)toolMap.get(id);
		// If not, check if parent has it
		if (result == null && parent != null) {
			result = ((Target)parent).getTool(id);
		}
		return result;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#getDefaultExtension()
	 */
	public String getDefaultExtension() {
		return defaultExtension == null ? EMPTY_STRING : defaultExtension;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#getArtifactName()
	 */
	public String getArtifactName() {
		// Return name or an empty string
		return artifactName == null ? EMPTY_STRING : artifactName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#getConfiguration()
	 */
	public IConfiguration getConfiguration(String id) {
		return (IConfiguration)configMap.get(id);
	}

	public void addConfiguration(IConfiguration configuration) {
		if (configurations == null) {
			configurations = new ArrayList();
			configMap = new HashMap();
		}
		configurations.add(configuration);
		configMap.put(configuration.getId(), configuration);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#isAbstract()
	 */
	public boolean isAbstract() {
		return isAbstract;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#isTestTarget()
	 */
	public boolean isTestTarget() {
		return isTest;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#createConfiguration()
	 */
	public IConfiguration createConfiguration(String id) {
		return new Configuration(this, id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#createConfiguration(org.eclipse.cdt.core.build.managed.IConfiguration)
	 */
	public IConfiguration createConfiguration(IConfiguration parent, String id) {
		return new Configuration(this, parent, id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#setBuildArtifact(java.lang.String)
	 */
	public void setBuildArtifact(String name) {
		artifactName = name;		
	}
}
