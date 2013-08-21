/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import java.io.File;
import java.io.IOException;

/**
 * Abstraction of the java.lang.Runtime exec() methods to allow different implementations to be supplied.
 */
public interface IProcessFactory {

	public Process exec(String cmd) throws IOException;

	public Process exec(String[] cmdarray) throws IOException;

	public Process exec(String[] cmdarray, String[] envp) throws IOException;

	public Process exec(String cmd, String[] envp) throws IOException;

	public Process exec(String cmd, String[] envp, File dir) throws IOException;

	public Process exec(String cmdarray[], String[] envp, File dir) throws IOException;
}
