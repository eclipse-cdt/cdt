/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.compilationdatabase.internal.core;

public class CompileCommand {

	private String directory;
	private String command;
	private String file;

	public String getDirectory() {
		return directory;
	}

	public String getCommand() {
		return command;
	}

	public String getFile() {
		return file;
	}

}
