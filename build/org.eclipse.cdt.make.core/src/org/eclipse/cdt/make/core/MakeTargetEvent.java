/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import java.util.EventObject;

import org.eclipse.core.resources.IProject;

public class MakeTargetEvent extends EventObject {
	public static final int TARGET_ADD = 1;
	public static final int TARGET_CHANGED = 2;
	public static final int TARGET_REMOVED = 3;
	public static final int PROJECT_ADDED = 4;
	public static final int PROJECT_REMOVED = 5;

	IMakeTarget target;
	IProject project;
	int type;

	/**
	 * @param source
	 */
	public MakeTargetEvent(Object source, int type, IMakeTarget target) {
		super(source);
		this.type = type;
		this.target = target;
	}

	public MakeTargetEvent(Object source, int type, IProject project) {
		super(source);
		this.type = type;
		this.project = project;
	}
	
	public int getType() {
		return type;
	}

	public IMakeTarget getTarget() {
		return target;
	}
}
