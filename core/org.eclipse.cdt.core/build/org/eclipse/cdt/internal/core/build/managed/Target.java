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
import java.util.List;

import org.eclipse.cdt.core.build.managed.BuildException;
import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.cdt.core.build.managed.ITool;
import org.eclipse.cdt.core.build.managed.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * 
 */
public class Target implements ITarget {

	private String name;
	private ITarget parent;
	private IResource owner;
	private List tools;
	private List configurations;

	public Target(IResource owner) {
		this.owner = owner;
	}
	
	/**
	 * Resource is allowed to be null to represent a ISV target def.
	 * 
	 * @param parent
	 */
	public Target(IResource owner, ITarget parent) {
		this.owner = owner;
		this.parent = parent;

		// Inherit the configs from the parent
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
	
	public void setName(String name) {
		this.name = name;
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

	public void addTool(ITool tool){
		if (tools == null)
			tools = new ArrayList();
		tools.add(tool);
	}
	
	public IConfiguration[] getConfigurations() {
		return (IConfiguration[])configurations.toArray(new IConfiguration[configurations.size()]);
	}

	public void addConfiguration(IConfiguration configuration) {
		if (configurations == null)
			configurations = new ArrayList();
		configurations.add(configuration);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#addConfiguration(org.eclipse.core.resources.IResource)
	 */
	public IConfiguration addConfiguration(IResource resource)
		throws BuildException
	{
		Target target = (Target)ManagedBuildManager.addTarget(resource, this);
		IConfiguration config = new Configuration(target);
		target.addConfiguration(config);
		return null;
	}

	public IConfiguration addConfiguration(IResource resource, IConfiguration parentConfig)
		throws BuildException
	{
		IResource parentOwner = parentConfig.getOwner();
		
		if (resource instanceof IProject) {
			// parent must be owned by the same project
			if (!resource.equals(parentOwner))
				throw new BuildException("addConfiguration: parent must be in same project");
		} else {
			// parent must be owned by the project
			if (!resource.getProject().equals(parentOwner))
				throw new BuildException("addConfiguration: parent must be in owning project");
		}

		// Validation passed
		Target target = (Target)ManagedBuildManager.addTarget(resource, this);
		IConfiguration config = new Configuration(parentConfig);
		target.addConfiguration(config);
		return config;
	}

}
