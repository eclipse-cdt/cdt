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

import org.eclipse.cdt.core.build.managed.IOption;
import org.eclipse.cdt.core.build.managed.IOptionCategory;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.cdt.core.build.managed.ITool;

/**
 * 
 */
public class Tool implements ITool {

	private String name;
	private ITarget target;
	
	public Tool(String name) {
		this.name = name;
	}
	
	public Tool(String name, Target target) {
		this(name);
		this.target = target;
	}
	
	public String getName() {
		return name;
	}

	public ITarget getTarget() {
		return target;	
	}
	
	public IOption[] getOptions() {
		return null;
	}

	public IOptionCategory getTopOptionCategory() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getParent()
	 */
	public ITool getParent() {
		// TODO Auto-generated method stub
		return null;
	}

}
