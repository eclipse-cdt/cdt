/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.util;

import java.util.Map;

import org.eclipse.text.edits.TextEdit;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ToolFactory;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICProject;

public class CodeFormatterUtil {

	/**
	 * Creates a string that represents the given number of indentation units.
	 * The returned string can contain tabs and/or spaces depending on the core
	 * formatter preferences.
	 * 
	 * @param indentationUnits the number of indentation units to generate
	 * @param project the project from which to get the formatter settings,
	 *        <code>null</code> if the workspace default should be used
	 * @return the indent string
	 */
	public static String createIndentString(int indentationUnits, ICProject project) {
		Map<String, String> options= project != null ? project.getOptions(true) : CCorePlugin.getOptions();		
		return ToolFactory.createDefaultCodeFormatter(options).createIndentationString(indentationUnits);
	} 
		
	/**
	 * Gets the current tab width.
	 * 
	 * @param project The project where the source is used, used for project
	 *        specific options or <code>null</code> if the project is unknown
	 *        and the workspace default should be used
	 * @return The tab width
	 */
	public static int getTabWidth(ICProject project) {
		/*
		 * If the tab-char is SPACE, FORMATTER_INDENTATION_SIZE is not used
		 * by the core formatter.
		 * We piggy back the visual tab length setting in that preference in
		 * that case.
		 */
		String key;
		if (CCorePlugin.SPACE.equals(getCoreOption(project, DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR)))
			key= DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE;
		else
			key= DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE;
		
		return getCoreOption(project, key, 4);
	}

	/**
	 * Returns the current indent width.
	 * 
	 * @param project the project where the source is used or <code>null</code>
	 *        if the project is unknown and the workspace default should be used
	 * @return the indent width
	 */
	public static int getIndentWidth(ICProject project) {
		String key;
		if (DefaultCodeFormatterConstants.MIXED.equals(getCoreOption(project, DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR)))
			key= DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE;
		else
			key= DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE;
		
		return getCoreOption(project, key, 4);
	}

	/**
	 * Returns the possibly <code>project</code>-specific core preference
	 * defined under <code>key</code>.
	 * 
	 * @param project the project to get the preference from, or
	 *        <code>null</code> to get the global preference
	 * @param key the key of the preference
	 * @return the value of the preference
	 */
	private static String getCoreOption(ICProject project, String key) {
		if (project == null)
			return CCorePlugin.getOption(key);
		return project.getOption(key, true);
	}

	/**
	 * Returns the possibly <code>project</code>-specific core preference
	 * defined under <code>key</code>, or <code>def</code> if the value is
	 * not a integer.
	 * 
	 * @param project the project to get the preference from, or
	 *        <code>null</code> to get the global preference
	 * @param key the key of the preference
	 * @param def the default value
	 * @return the value of the preference
	 */
	private static int getCoreOption(ICProject project, String key, int def) {
		try {
			return Integer.parseInt(getCoreOption(project, key));
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Creates edits that describe how to format the given string. Returns <code>null</code> if the code could not be formatted for the given kind.
	 * @throws IllegalArgumentException If the offset and length are not inside the string, a
	 *  IllegalArgumentException is thrown.
	 */
	public static TextEdit format(int kind, String source, int offset, int length, int indentationLevel, String lineSeparator, Map<String, ?> options) {
		if (offset < 0 || length < 0 || offset + length > source.length()) {
			throw new IllegalArgumentException("offset or length outside of string. offset: " + offset + ", length: " + length + ", string size: " + source.length());   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		}
		CodeFormatter formatter = ToolFactory.createCodeFormatter(options);
		if (formatter != null) {
			return formatter.format(kind, source, offset, length, indentationLevel, lineSeparator);
		}
		return null;
	}
	
	public static TextEdit format(int kind, String source, int indentationLevel, String lineSeparator, Map<String, ?> options) {
		String prefix= ""; //$NON-NLS-1$
		String suffix= ""; //$NON-NLS-1$
		switch (kind) {
		case CodeFormatter.K_EXPRESSION:
			prefix= "int __dummy__="; //$NON-NLS-1$
			suffix= ";"; //$NON-NLS-1$
			break;
		case CodeFormatter.K_STATEMENTS:
			prefix= "void __dummy__() {"; //$NON-NLS-1$
			suffix= "}"; //$NON-NLS-1$
			--indentationLevel;
			break;
		}
		String tuSource= prefix + source + suffix;
		TextEdit edit= format(CodeFormatter.K_TRANSLATION_UNIT, tuSource, prefix.length(), source.length(), indentationLevel, lineSeparator, options);
		if (edit != null && prefix.length() > 0) {
			edit.moveTree(-prefix.length());
		}
		return edit;
	}
	
	/**
	 * @return The formatter tab width on workspace level.
	 */
	public static int getTabWidth() {
		return getTabWidth(null);
	}
}
