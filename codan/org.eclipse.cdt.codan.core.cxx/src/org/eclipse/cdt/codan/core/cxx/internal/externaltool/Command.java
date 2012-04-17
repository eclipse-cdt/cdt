/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.externaltool;

import org.eclipse.core.runtime.IPath;

/**
 * The command to execute to invoke an external tool.
 */
class Command {
	private final IPath path;
	private final String[] args;

	Command(IPath path, String[] args) {
		this.path = path;
		this.args = args;
	}

	IPath getPath() {
		return path;
	}

	String[] getArgs() {
		return args;
	}
}
