package org.eclipse.cdt.core.resources;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;


public interface IConsole {
	void start(IProject project);
    ConsoleOutputStream getOutputStream() throws CoreException;
    ConsoleOutputStream getInfoStream() throws CoreException;
    ConsoleOutputStream getErrorStream() throws CoreException;
}

