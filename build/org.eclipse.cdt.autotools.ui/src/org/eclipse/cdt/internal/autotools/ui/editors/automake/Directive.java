/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMakefile;

public abstract class Directive implements IDirective {

	int endLine;
	int startLine;
	String filename;
	IMakefile makefile;
	Directive parent;

	public Directive(Directive owner) {
		parent = owner;
	}

	public Directive(int start, int end) {
		setLines(start, end);
	}

	@Override
	public abstract String toString();

	@Override
	public int getEndLine() {
		return endLine;
	}

	@Override
	public int getStartLine() {
		return startLine;
	}

	@Override
	public IDirective getParent() {
		return parent;
	}

	public String getFileName() {
		if (filename == null) {
			if (parent != null) {
				filename = parent.getFileName();
			}
		}
		return filename;
	}

	@Override
	public IMakefile getMakefile() {
		if (makefile == null) {
			if (parent != null) {
				makefile = parent.getMakefile();
			}
		}
		return makefile;
	}

	public void setParent(Directive owner) {
		parent = owner;
	}

	public void setStartLine(int lineno) {
		startLine = lineno;
	}

	public void setEndLine(int lineno) {
		endLine = lineno;
	}

	public void setLines(int start, int end) {
		setStartLine(start);
		setEndLine(end);
	}

	public void setFilename(String name) {
		filename = name;
	}

}
