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

import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.cdt.core.build.managed.ITool;
import org.eclipse.core.resources.IProject;

/**
 * 
 */
public class Target implements ITarget {

	private String name;
	private Target parent;
	private ITool[] tools;
	private IConfiguration[] configurations;

	public Target(String name) {
		this.name = name;
	}
	
	public Target(String name, Target parent) {
		this(name);
		this.parent = parent;
	}
	
	public String getName() {
		return name;
	}

	public ITarget getParent() {
		return parent;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	private int getNumTools() {
		int n = (tools == null) ? 0 : tools.length;
		if (parent != null)
			n += parent.getNumTools();
		return n;
	}
	
	private int addToolsToArray(ITool[] toolArray, int start) {
		int n = start;
		if (parent != null)
			n = parent.addToolsToArray(toolArray, start);

		if (tools != null) {
			for (int i = 0; i < tools.length; ++i)
				toolArray[n++] = (ITool)tools[i]; 
		}
		
		return n;
	}
	
	public ITool[] getTools() {
		ITool[] toolArray = new ITool[getNumTools()];
		addToolsToArray(toolArray, 0);
		return toolArray;
	}

	public void setTools(ITool[] tools) {
		this.tools = tools;
	}
	
	public void setConfigurations(IConfiguration [] configurations) {
		this.configurations = configurations;
	}
	
	public IConfiguration[] getAvailableConfigurations(IProject project) {
		// TODO Auto-generated method stub
		return null;
	}

}
