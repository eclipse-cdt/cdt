/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * Style preferences for a category of include statements.
 */
public class IncludeGroupStyle implements Comparable<IncludeGroupStyle> {

	public enum IncludeKind {
		RELATED(PreferencesMessages.IncludeCategoriesBlock_related_headers_node,
				PreferencesMessages.IncludeCategoriesBlock_related_headers_node_description, null),
		PARTNER(PreferencesMessages.IncludeCategoriesBlock_partner_header_node,
				PreferencesMessages.IncludeCategoriesBlock_partner_header_node_description, RELATED),
		IN_SAME_FOLDER(PreferencesMessages.IncludeCategoriesBlock_same_folder_header_node,
				PreferencesMessages.IncludeCategoriesBlock_same_folder_header_node_description, RELATED),
		IN_SUBFOLDER(PreferencesMessages.IncludeCategoriesBlock_subfolder_header_node,
				PreferencesMessages.IncludeCategoriesBlock_subfolder_header_node_description, RELATED),
		SYSTEM(PreferencesMessages.IncludeCategoriesBlock_system_headers_node,
				PreferencesMessages.IncludeCategoriesBlock_system_headers_node_description, null),
		SYSTEM_WITH_EXTENSION(PreferencesMessages.IncludeCategoriesBlock_system_headers_with_extension_node,
				PreferencesMessages.IncludeCategoriesBlock_system_headers_with_extension_node_description, SYSTEM),
		SYSTEM_WITHOUT_EXTENSION(PreferencesMessages.IncludeCategoriesBlock_system_headers_without_extension_node,
				PreferencesMessages.IncludeCategoriesBlock_system_headers_without_extension_node_description, SYSTEM),
		OTHER(PreferencesMessages.IncludeCategoriesBlock_unrelated_headers_node,
				PreferencesMessages.IncludeCategoriesBlock_unrelated_headers_node_description, null),
		IN_SAME_PROJECT(PreferencesMessages.IncludeCategoriesBlock_same_project_headers_node,
				PreferencesMessages.IncludeCategoriesBlock_same_project_headers_node_description, OTHER),
		IN_OTHER_PROJECT(PreferencesMessages.IncludeCategoriesBlock_other_project_headers_node,
				PreferencesMessages.IncludeCategoriesBlock_other_project_headers_node_description, OTHER),
		EXTERNAL(PreferencesMessages.IncludeCategoriesBlock_external_headers_node,
				PreferencesMessages.IncludeCategoriesBlock_external_headers_node_description, OTHER),
		MATCHING_PATTERN(PreferencesMessages.IncludeCategoriesBlock_user_defined_categories_node,
				PreferencesMessages.IncludeCategoriesBlock_user_defined_categories_node_description, null);

		public final String name;
		public final String description;
		public final IncludeKind parent;
		public final List<IncludeKind> children = new ArrayList<>();

		private IncludeKind(String name, String description, IncludeKind parent) {
			this.name = name;
			this.description = description;
			this.parent = parent;
			if (parent != null)
				parent.children.add(this);
		}

		public boolean hasChildren() {
			return !children.isEmpty();
		}
	}

	private static final String TAG_STYLE = "style"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_PATTERN = "pattern"; //$NON-NLS-1$
	private static final String TAG_KEEP_TOGETHER = "keep_together"; //$NON-NLS-1$
	private static final String TAG_BLANK_LINE_BEFORE = "blank_line_before"; //$NON-NLS-1$
	private static final String TAG_RELATIVE_PATH = "relative_path"; //$NON-NLS-1$
	private static final String TAG_ANGLE_BRACKETS = "angle_brackets"; //$NON-NLS-1$
	private static final String TAG_ORDER = "order"; //$NON-NLS-1$

	private final IncludeKind includeKind;
	private boolean keepTogether;
	private boolean blankLineBefore;
	private boolean relativePath;
	private boolean angleBrackets;
	private Pattern headerNamePattern;
	private String name;
	private int order; // Relative position of the include group. Ignored if keepTogether is false.

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

	public boolean isKeepTogether() {
		return keepTogether;
	}

	public void setKeepTogether(boolean value) {
		this.keepTogether = value;
	}

	public boolean isBlankLineBefore() {
		return blankLineBefore;
	}

	public void setBlankLineBefore(boolean value) {
		this.blankLineBefore = value;
	}

	public boolean isRelativePath() {
		return relativePath;
	}

	public void setRelativePath(boolean value) {
		assert !includeKind.hasChildren();
		this.relativePath = value;
	}

	public boolean isAngleBrackets() {
		return angleBrackets;
	}

	public void setAngleBrackets(boolean value) {
		assert !includeKind.hasChildren();
		this.angleBrackets = value;
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

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
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
		style.setKeepTogether(nullToFalse(memento.getBoolean(TAG_KEEP_TOGETHER)));
		style.setBlankLineBefore(nullToFalse(memento.getBoolean(TAG_BLANK_LINE_BEFORE)));
		if (!includeKind.hasChildren()) {
			style.setRelativePath(nullToFalse(memento.getBoolean(TAG_RELATIVE_PATH)));
			style.setAngleBrackets(nullToFalse(memento.getBoolean(TAG_ANGLE_BRACKETS)));
		}
		Integer order = memento.getInteger(TAG_ORDER);
		if (order != null)
			style.setOrder(order.intValue());
		return style;
	}

	private static boolean nullToFalse(Boolean val) {
		return val != null && val.booleanValue();
	}

	private static String nullToEmpty(String val) {
		return val != null ? val : ""; //$NON-NLS-1$
	}

	public void saveToMemento(IMemento memento) {
		if (includeKind == IncludeKind.MATCHING_PATTERN) {
			memento.putString(TAG_NAME, name);
			memento.putString(TAG_PATTERN, headerNamePattern.toString());
		}
		memento.putBoolean(TAG_KEEP_TOGETHER, keepTogether);
		memento.putBoolean(TAG_BLANK_LINE_BEFORE, blankLineBefore);
		if (!includeKind.hasChildren()) {
			memento.putBoolean(TAG_RELATIVE_PATH, relativePath);
			memento.putBoolean(TAG_ANGLE_BRACKETS, angleBrackets);
		}
		if (keepTogether)
			memento.putInteger(TAG_ORDER, order);
	}

	public String toXmlString() {
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

	public static IncludeGroupStyle fromXmlString(String str, IncludeKind includeKind) {
		StringReader reader = new StringReader(str);
		XMLMemento memento;
		try {
			memento = XMLMemento.createReadRoot(reader);
		} catch (WorkbenchException e) {
			return null;
		}
		return fromMemento(memento, includeKind);
	}

	/** For debugging only */
	@Override
	public String toString() {
		return includeKind.toString();
	}

	/**
	 * Compares styles according to their sorting order.
	 */
	@Override
	public int compareTo(IncludeGroupStyle other) {
		if (keepTogether != other.keepTogether)
			return keepTogether ? -1 : 1;
		int c = order - other.order;
		if (c != 0)
			return c;
		return includeKind.ordinal() - other.includeKind.ordinal();
	}

	public IncludeGroupStyle getGroupingStyle(Map<IncludeKind, IncludeGroupStyle> stylesMap) {
		if (keepTogether)
			return this;
		IncludeGroupStyle parent = getParentStyle(stylesMap);
		if (parent != null && (parent.keepTogether || parent.includeKind == IncludeKind.OTHER))
			return parent;
		return stylesMap.get(IncludeKind.OTHER);
	}

	private IncludeGroupStyle getParentStyle(Map<IncludeKind, IncludeGroupStyle> stylesMap) {
		IncludeKind kind = includeKind.parent;
		if (kind == null)
			return null;
		return stylesMap.get(kind);
	}

	public boolean isBlankLineNeededAfter(IncludeGroupStyle previousIncludeStyle,
			Map<IncludeKind, IncludeGroupStyle> stylesMap) {
		if (previousIncludeStyle == null)
			return false;
		IncludeGroupStyle groupingStyle = getGroupingStyle(stylesMap);
		IncludeGroupStyle previousGroupingStyle = previousIncludeStyle.getGroupingStyle(stylesMap);
		if (groupingStyle != previousGroupingStyle && groupingStyle.isBlankLineBefore())
			return true;
		IncludeGroupStyle parentStyle = groupingStyle.getParentStyle(stylesMap);
		IncludeGroupStyle previousParentStyle = previousGroupingStyle.getParentStyle(stylesMap);
		return parentStyle != null && previousParentStyle != null && parentStyle != previousParentStyle
				&& parentStyle.isKeepTogether() && parentStyle.isBlankLineBefore();
	}
}
