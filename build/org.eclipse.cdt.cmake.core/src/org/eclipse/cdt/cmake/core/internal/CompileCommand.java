/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

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
