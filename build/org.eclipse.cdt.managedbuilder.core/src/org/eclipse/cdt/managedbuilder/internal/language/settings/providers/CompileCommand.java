/*******************************************************************************
 * Copyright (c) 2016, 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-André Laperle - Moved to managed builder
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.language.settings.providers;

public class CompileCommand {

	public String directory;
	public String command;
	public String file;

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
