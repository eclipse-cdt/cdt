/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
