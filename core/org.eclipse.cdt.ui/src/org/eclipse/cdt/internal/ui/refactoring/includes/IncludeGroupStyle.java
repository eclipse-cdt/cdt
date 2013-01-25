/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Pattern;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Style preferences for a category of include statements.
 */
public class IncludeGroupStyle {
	public enum IncludeKind {
		PARTNER,
		IN_SAME_FOLDER,
		IN_SUBFOLDERS,
		SYSTEM_WITH_EXTENSION,
		SYSTEM_WITHOUT_EXTENSION,
		IN_WORKSPACE,
		EXTERNAL,
		MATCHING_PATTERN,
	}

	private static final String TAG_STYLE = "style"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_PATTERN = "pattern"; //$NON-NLS-1$
	private static final String TAG_DISABLED = "disabled"; //$NON-NLS-1$
	private static final String TAG_RELATIVE_PATH = "relative_path"; //$NON-NLS-1$
	private static final String TAG_ANGLE_BRACKETS = "angle_brackets"; //$NON-NLS-1$

	private final IncludeKind includeKind;
	private boolean disabled;
	private boolean relativePath;
	private boolean angleBrackets;
	private Pattern headerNamePattern;
	private String name;

	public IncludeGroupStyle(IncludeKind includeKind) {
		if (includeKind == null || includeKind == IncludeKind.MATCHING_PATTERN)
			throw new IllegalArgumentException();
		this.includeKind = includeKind;
	}

	public IncludeGroupStyle(String name, Pattern headerNamePattern) {
		if (name == null)
			throw new IllegalArgumentException();
		if (headerNamePattern == null)
			throw new IllegalArgumentException();
		this.includeKind = IncludeKind.MATCHING_PATTERN;
		this.name = name;
		this.headerNamePattern = headerNamePattern;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isRelativePath() {
		return relativePath;
	}

	public void setRelativePath(boolean relativePath) {
		this.relativePath = relativePath;
	}

	public boolean isAngleBrackets() {
		return angleBrackets;
	}

	public void setAngleBrackets(boolean angleBrackets) {
		this.angleBrackets = angleBrackets;
	}

	public Pattern getHeaderNamePattern() {
		return headerNamePattern;
	}

	public void setHeaderNamePattern(Pattern headerNamePattern) {
		this.headerNamePattern = headerNamePattern;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IncludeKind getIncludeKind() {
		return includeKind;
	}

	public static IncludeGroupStyle fromMemento(IMemento memento, IncludeKind includeKind) {
		IncludeGroupStyle style;
		if (includeKind == IncludeKind.MATCHING_PATTERN) {
			String name = nullToEmpty(memento.getString(TAG_NAME));
			String pattern = nullToEmpty(memento.getString(TAG_PATTERN));
			style = new IncludeGroupStyle(name, Pattern.compile(pattern));
		} else {
			style = new IncludeGroupStyle(includeKind);
		}		
		style.setDisabled(memento.getBoolean(TAG_DISABLED));
		style.setRelativePath(memento.getBoolean(TAG_RELATIVE_PATH));
		style.setAngleBrackets(memento.getBoolean(TAG_ANGLE_BRACKETS));
		return style;
	}

	private static String nullToEmpty(String string) {
		return string != null ? string : ""; //$NON-NLS-1$
	}

	public void saveToMemento(IMemento memento) {
		if (includeKind == IncludeKind.MATCHING_PATTERN) {
			memento.putString(TAG_NAME, name);
			memento.putString(TAG_PATTERN, headerNamePattern.toString());
		}
		memento.putBoolean(TAG_DISABLED, disabled);
		memento.putBoolean(TAG_RELATIVE_PATH, relativePath);
		memento.putBoolean(TAG_ANGLE_BRACKETS, angleBrackets);
	}

	@Override
	public String toString() {
		XMLMemento memento = XMLMemento.createWriteRoot(TAG_STYLE);
		saveToMemento(memento);
		StringWriter writer = new StringWriter();
		try {
			memento.save(writer);
		} catch (IOException e) {
			CUIPlugin.log(e);
		}
		return writer.toString();
	}

	public static IncludeGroupStyle fromString(String str, IncludeKind includeKind) {
		StringReader reader = new StringReader(str);
		XMLMemento memento;
		try {
			memento = XMLMemento.createReadRoot(reader);
		} catch (WorkbenchException e) {
			return null;
		}
		return fromMemento(memento, includeKind);
	}
}
