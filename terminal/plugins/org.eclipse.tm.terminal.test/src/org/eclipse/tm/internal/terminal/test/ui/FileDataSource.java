/*******************************************************************************
 * Copyright (c) 2007, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.test.ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.tm.terminal.model.TerminalStyle;

/**
 * Reads the file in an infinite loop.
 * Makes lines containing 'x' bold.
 *
 */
final class FileDataSource extends AbstractLineOrientedDataSource {
	private final String fFile;

	BufferedReader reader;

	String line;

	TerminalStyle style;

	TerminalStyle styleNormal = TerminalStyle.getDefaultStyle();

	TerminalStyle styleBold = styleNormal.setBold(true);

	FileDataSource(String file) {
		fFile = file;
	}

	@Override
	public char[] dataSource() {
		return line.toCharArray();
	}

	@Override
	public TerminalStyle getStyle() {
		return style;
	}

	@Override
	public void next() {
		try {
			if (reader == null)
				reader = new BufferedReader(new FileReader(fFile));
			line = reader.readLine();
			if (line == null) {
				reader.close();
				reader = null;
				// reopen the file
				next();
				return;
			}
			if (line.lastIndexOf('x') > 0)
				style = styleBold;
			else
				style = styleNormal;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}