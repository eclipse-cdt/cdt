/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */package org.eclipse.cdt.internal.ui;

import java.util.EventObject;

import org.eclipse.cdt.ui.IBuildConsoleEvent;
import org.eclipse.core.resources.IProject;

public class ConsoleEvent extends EventObject implements IBuildConsoleEvent {
	private IProject fProject;
	private int fType;
		
	public ConsoleEvent(Object source, IProject project, int type) {
		super(source);
		fProject = project;
		fType = type;
	}

	public IProject getProject() {
		return fProject;
	}

	public int getType() {
		return fType;
	}

}
