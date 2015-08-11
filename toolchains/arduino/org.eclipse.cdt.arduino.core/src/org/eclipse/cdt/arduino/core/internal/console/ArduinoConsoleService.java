/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.console;

import java.io.IOException;

public interface ArduinoConsoleService {

	/**
	 * Capture the output for the process and display on the console.
	 * 
	 * @param process
	 */
	void monitor(Process process, ConsoleParser[] consoleParsers) throws IOException;

	void writeOutput(String msg) throws IOException;

	void writeError(String msg) throws IOException;

}
