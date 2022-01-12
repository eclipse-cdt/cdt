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

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

/**
 */
public class MakefileReader extends LineNumberReader {

	public MakefileReader(Reader reader) {
		super(reader);
	}

	public MakefileReader(Reader reader, int sz) {
		super(reader, sz);
	}

	@Override
	public String readLine() throws IOException {
		boolean done = false;
		StringBuilder buffer = new StringBuilder();
		boolean escapedLine = false;
		boolean escapedCommand = false;
		while (!done) {
			String line = super.readLine();
			if (line == null) {
				return null;
			}

			if (escapedLine && line.length() > 0) {
				// Eat the spaces at the beginning.
				int i = 0;
				while (i < line.length() && (Util.isSpace(line.charAt(i)))) {
					i++;
				}
				line = line.substring(i);
			} else if (escapedCommand && line.length() > 0) {
				// Only eat the first tab
				if (line.charAt(0) == '\t') {
					line = line.substring(1);
				}
			}

			// According to POSIX rules:
			// When an escaped <newline>(one preceded by a backslash) is found
			// anywhere in the makefile except in a command line, it shall be replaced,
			// along with any leading white space on the following line, with a single <space>
			//
			// When an escaped <newline> is found in a command line in a makefile,
			// the command line shall contain the backslash, the <newline>, and  the next line,
			// except that the first character of the next line shall not be included if it is a <tab>
			if (Util.isEscapedLine(line)) {
				int index = line.lastIndexOf('\\');
				if (index > 0) {
					if (!escapedLine && Util.isCommand(line)) {
						escapedCommand = true;
						buffer.append(line);
					} else {
						escapedLine = true;
						buffer.append(line.substring(0, index));
						buffer.append(' ');
					}
				}
			} else {
				done = true;
				escapedLine = false;
				escapedCommand = false;
				buffer.append(line);
			}
		}
		return buffer.toString();
	}

}
