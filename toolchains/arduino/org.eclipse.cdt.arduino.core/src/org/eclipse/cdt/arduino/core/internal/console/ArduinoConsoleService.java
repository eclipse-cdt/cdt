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

import org.eclipse.core.resources.IFolder;

public interface ArduinoConsoleService {

	void monitor(Process process, ArduinoConsoleParser[] consoleParsers, IFolder buildDirectory) throws IOException;

	void writeOutput(String msg) throws IOException;

	void writeError(String msg) throws IOException;

}
