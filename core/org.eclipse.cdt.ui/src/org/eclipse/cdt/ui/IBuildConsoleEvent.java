package org.eclipse.cdt.ui;
/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;

public interface IBuildConsoleEvent {
	final static int CONSOLE_START = 1;
	final static int CONSOLE_CLOSE = 2;
	
	IProject getProject();
	int getType();
}
