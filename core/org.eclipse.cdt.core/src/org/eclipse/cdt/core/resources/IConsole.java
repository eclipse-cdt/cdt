package org.eclipse.cdt.core.resources;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.core.resources.IProject;


public interface IConsole {
	void start(IProject project);
    ConsoleOutputStream getOutputStream();
    ConsoleOutputStream getInfoStream();
    ConsoleOutputStream getErrorStream();
}

