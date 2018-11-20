/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.model;

import org.eclipse.cdt.testsrunner.model.ITestLocation;

/**
 * Represents the location of the test object.
 */
public class TestLocation implements ITestLocation {

	/** Stores the file name in which testing object is located. */
	private String file;

	/** Stores the line number on which testing object is located. */
	private int line;

	public TestLocation(String file, int line) {
		this.file = file;
		this.line = line;
	}

	@Override
	public String getFile() {
		return file;
	}

	@Override
	public int getLine() {
		return line;
	}
}
