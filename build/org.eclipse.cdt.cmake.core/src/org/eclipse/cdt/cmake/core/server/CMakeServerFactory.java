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

import java.io.File;

import org.eclipse.cdt.cmake.core.server.internal.CMakeServerBackendImpl;
import org.eclipse.cdt.cmake.core.server.internal.CMakeServerImpl;

public class CMakeServerFactory {
	public static ICMakeServer createServer() {
		return new CMakeServerImpl();
	}

	/**
	 * Creates a server backend using a CMake executable.
	 * 
	 * @param cmakeExe
	 * @return
	 */
	public static ICMakeServerBackend createBackend(File cmakeExe, boolean experimental) {
		ProcessBuilder pb = new ProcessBuilder(cmakeExe.toString(), "-E", "server", "--debug");

		if (experimental) {
			pb.command().add("--experimental");
		}

		return new CMakeServerBackendImpl(pb);
	}
}
