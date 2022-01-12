/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.TextSelection;

/**
 * @author Emanuel Graf
 */
public class TestSourceFile {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String LINE_SEPARATOR = "\n"; //$NON-NLS-1$
	private static final Pattern SELECTION_START = Pattern.compile("/\\*\\$\\*/"); //$NON-NLS-1$
	private static final Pattern SELECTION_END = Pattern.compile("/\\*\\$\\$\\*/"); //$NON-NLS-1$

	private final String name;
	private String expectedName;
	private final StringBuilder source = new StringBuilder();
	private final StringBuilder expectedSource = new StringBuilder();
	private int selectionStart = -1;
	private int selectionEnd = -1;

	public TestSourceFile(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getSource() {
		return source.toString();
	}

	/**
	 * Returns the expected name after refactoring.
	 */
	public String getExpectedName() {
		return expectedName == null ? name : expectedName;
	}

	public void setExpectedName(String name) {
		expectedName = name;
	}

	/**
	 * Returns the expected contents after refactoring.
	 */
	public String getExpectedSource() {
		if (expectedSource.length() == 0) {
			return getSource();
		}
		return expectedSource.toString();
	}

	public void addLineToSource(String code) {
		Matcher start = SELECTION_START.matcher(code);
		if (start.find()) {
			selectionStart = start.start() + source.length();
			code = start.replaceAll(EMPTY_STRING);
		}
		Matcher end = SELECTION_END.matcher(code);
		if (end.find()) {
			selectionEnd = end.start() + source.length();
			code = end.replaceAll(EMPTY_STRING);
		}
		source.append(code);
		source.append(LINE_SEPARATOR);
	}

	public void addLineToExpectedSource(String code) {
		expectedSource.append(code);
		expectedSource.append(LINE_SEPARATOR);
	}

	public TextSelection getSelection() {
		if (selectionStart < 0 || selectionEnd < selectionStart)
			return null;
		return new TextSelection(selectionStart, selectionEnd - selectionStart);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, expectedName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		TestSourceFile other = (TestSourceFile) obj;
		return Objects.equals(name, other.name) && Objects.equals(expectedName, other.expectedName);
	}
}
