/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.eclipse.cdt.make.core.makefile.IStatement;
import org.eclipse.cdt.make.internal.core.makefile.MakefileReader;
import org.eclipse.cdt.make.internal.core.makefile.posix.PosixMakefile;

/**
 * GNUMakefile
 */
public class GNUMakefile extends PosixMakefile {

	public static String PATH_SEPARATOR = System.getProperty("path.separator", ":");

	public GNUMakefile(String name) throws IOException {
		this(new FileReader(name));
	}

	public GNUMakefile(Reader reader) throws IOException {
		this(new MakefileReader(reader));
	}

	public GNUMakefile(MakefileReader reader) throws IOException {
		super(reader);
	}

	protected IStatement processLine(String line, int startLine, int endLine) {
		return null;
	}
}
