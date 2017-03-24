/*******************************************************************************
 * Copyright (c) 2017 IAR Systems AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jesper Eskilson (IAR Systems AB) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.server;

import java.util.List;

/**
 * Thrown to indicate a build failure
 */
@SuppressWarnings("serial")
public class CMakeBuildException extends CMakeServerException {

	private int exitcode;
	private List<String> buildCommand;

	public CMakeBuildException(String s, List<String> buildCommand, int exitcode) {
		super(s);
		this.exitcode = exitcode;
		this.buildCommand = buildCommand;
	}

	public int getExitcode() {
		return exitcode;
	}

	public List<String> getBuildCommand() {
		return buildCommand;
	}
}
