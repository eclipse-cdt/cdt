/*******************************************************************************
 * Copyright (c) 2003, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.util.EventObject;

import org.eclipse.core.resources.IProject;

import org.eclipse.cdt.ui.IBuildConsoleEvent;

public class ConsoleEvent extends EventObject implements IBuildConsoleEvent {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	private IProject fProject;
	private int fType;

	public ConsoleEvent(Object source, IProject project, int type) {
		super(source);
		fProject = project;
		fType = type;
	}

	@Override
	public IProject getProject() {
		return fProject;
	}

	@Override
	public int getType() {
		return fType;
	}

}
