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

import java.util.List;

import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.cdt.core.build.managed.ITool;
import org.eclipse.core.resources.IProject;

/**
 * 
 */
public class Configuration implements IConfiguration {

	private String name;
	private ITarget target;
	private IProject project;
	private IConfiguration parent;
	private List toolReference;
	
	public Configuration(Target target) {
		this.target = target;
	}

	public Configuration(IProject project, ITarget target) {
		this.project = project;
		this.target = target;
	}
	
	public Configuration(IProject project, IConfiguration parent) {
		this.project = project;
		this.parent = parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getName()
	 */
	public String getName() {
		return (name == null && parent != null) ? parent.getName() : name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getTools()
	 */
	public ITool[] getTools() {
		// TODO Auto-generated method stub
		return null;
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
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getProject()
	 */
	public IProject getProject() {
		return (project == null && parent != null) ? parent.getProject() : project;
	}

}
