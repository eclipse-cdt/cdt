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
 * CMake server listener which attempts to mimic the behaviour of command line
 * cmake with respect to output logging.
 */
public class StdoutCMakeServerListener implements ICMakeServerListener {

	@Override
	public void onFileChange(String path, List<String> properties) {
	}

	@Override
	public void onMessage(String title, String message) {
		System.out.println("-- " + message);
	}

	@Override
	public void onProgress(CMakeProgress progress) {
	}

	@Override
	public void onSignal(String name) {
	}
}
