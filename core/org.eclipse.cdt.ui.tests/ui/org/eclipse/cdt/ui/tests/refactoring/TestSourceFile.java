/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring;

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
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		TestSourceFile other = (TestSourceFile) obj;
		return name.equals(other.name);
	}
}
