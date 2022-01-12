/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.make.core.makefile.gnu.IVPath;

public class VPath extends Directive implements IVPath {

	String pattern;
	String[] directories;

	public VPath(Directive parent, String pat, String[] dirs) {
		super(parent);
		pattern = pat;
		directories = dirs.clone();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(GNUMakefileConstants.DIRECTIVE_VPATH);
		if (pattern != null && pattern.length() > 0) {
			sb.append(' ').append(pattern);
		}
		for (int i = 0; i < directories.length; i++) {
			sb.append(' ').append(directories[i]);
		}
		return sb.toString();
	}

	@Override
	public String[] getDirectories() {
		return directories.clone();
	}

	@Override
	public String getPattern() {
		return pattern;
	}
}
