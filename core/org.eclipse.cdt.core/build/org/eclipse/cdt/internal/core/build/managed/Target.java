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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * 
 */
public class Target extends BuildObject implements ITarget {

	private ITarget parent;
	private IResource owner;
	private List tools;
	private List configurations;

	private static final IConfiguration[] emptyConfigs = new IConfiguration[0];
	
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

	public ITool createTool() {
		ITool tool = new Tool(this);
		
		if (tools == null)
			tools = new ArrayList();
		tools.add(tool);
		
		return tool;
	}
	
	public IConfiguration[] getConfigurations() {
		if (configurations != null)
			return (IConfiguration[])configurations.toArray(new IConfiguration[configurations.size()]);
		else
			return emptyConfigs;
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

}
