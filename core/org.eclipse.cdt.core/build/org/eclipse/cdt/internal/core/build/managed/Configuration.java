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

import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.cdt.core.build.managed.ITool;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * 
 */
public class Configuration extends BuildObject implements IConfiguration {

	private ITarget target;
	private IConfiguration parent;
	private List toolReferences;
	
	public Configuration(Target target) {
		this.target = target;
	}

	public Configuration(IConfiguration parent) {
		this.parent = parent;
	}

	public Configuration(Target target, IConfigurationElement element) {
		this(target);
		
		// id
		setId(element.getAttribute("id"));
		
		// hook me up
		target.addConfiguration(this);
		
		// name
		setName(element.getAttribute("name"));

		IConfigurationElement[] configElements = element.getChildren();
		for (int l = 0; l < configElements.length; ++l) {
			IConfigurationElement configElement = configElements[l];
			if (configElement.getName().equals("toolRef")) {
				new ToolReference(this, configElement);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getName()
	 */
	public String getName() {
		return (name == null && parent != null) ? parent.getName() : name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getTools()
	 */
	public ITool[] getTools() {
		ITool[] tools = parent != null
			? parent.getTools()
			: target.getTools();
		
		// Replace tools with overrides
		for (int i = 0; i < tools.length; ++i) {
			ToolReference ref = getToolReference(tools[i]);
			if (ref != null)
				tools[i] = ref;
		}
		
		return tools;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getParent()
	 */
	public IConfiguration getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getTarget()
	 */
	public ITarget getTarget() {
		return (target == null && parent != null) ? parent.getTarget() : target;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getOwner()
	 */
	public IResource getOwner() {
		return getTarget().getOwner();
	}

	/**
	 * Returns the reference for a given tool.
	 * 
	 * @param tool
	 * @return
	 */
	private ToolReference getToolReference(ITool tool) {
		if (toolReferences != null)
			for (int i = 0; i < toolReferences.size(); ++i) {
				ToolReference toolRef = (ToolReference)toolReferences.get(i);
				if (toolRef.references(tool))
					return toolRef;
			}
		return null;
	}
	
	public void addToolReference(ToolReference toolRef) {
		if (toolReferences == null)
			toolReferences = new ArrayList();
		toolReferences.add(toolRef);
	}
}
