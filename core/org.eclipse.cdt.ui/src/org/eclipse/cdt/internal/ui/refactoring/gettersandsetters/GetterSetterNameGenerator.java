/*******************************************************************************
 * Copyright (c) 2010, 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

import com.ibm.icu.text.BreakIterator;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.text.CBreakIterator;
import org.eclipse.cdt.internal.ui.util.NameComposer;

public class GetterSetterNameGenerator {

	// Do not instantiate.
	private GetterSetterNameGenerator() {
	}

	public static String generateGetterName(IASTName fieldName) {
    	IPreferencesService preferences = Platform.getPreferencesService();
    	int capitalization = preferences.getInt(CUIPlugin.PLUGIN_ID,
    			PreferenceConstants.NAME_STYLE_GETTER_CAPITALIZATION,
    			PreferenceConstants.NAME_STYLE_CAPITALIZATION_CAMEL_CASE, null);
    	String wordDelimiter = preferences.getString(CUIPlugin.PLUGIN_ID,
    			PreferenceConstants.NAME_STYLE_GETTER_WORD_DELIMITER, "", null); //$NON-NLS-1$
    	String prefix = isBooleanDecaratorName(fieldName) ?
    			preferences.getString(CUIPlugin.PLUGIN_ID,
    					PreferenceConstants.NAME_STYLE_GETTER_PREFIX_FOR_BOOLEAN, "is", null) : //$NON-NLS-1$
				preferences.getString(CUIPlugin.PLUGIN_ID,
						PreferenceConstants.NAME_STYLE_GETTER_PREFIX, "get", null); //$NON-NLS-1$
    	String suffix = preferences.getString(CUIPlugin.PLUGIN_ID,
    			PreferenceConstants.NAME_STYLE_GETTER_SUFFIX, "", null); //$NON-NLS-1$
    	NameComposer composer = new NameComposer(capitalization, wordDelimiter, prefix, suffix);
    	String name = GetterSetterNameGenerator.trimFieldName(fieldName.toString());
    	return composer.compose(name);
	}
	
	private static boolean isBooleanDecaratorName(IASTName name) {
		if (IASTDeclarator.DECLARATOR_NAME.equals(name.getPropertyInParent())) {
			IASTDeclarator declarator = (IASTDeclarator) name.getParent();
			IType type = CPPVisitor.createType(declarator);
			if (type instanceof IBasicType && ((IBasicType) type).getKind() == IBasicType.Kind.eBoolean) {
				return true;
			}
		}
		return false;
	}
	
	public static String generateSetterName(IASTName fieldName) {
		IPreferencesService preferences = Platform.getPreferencesService();
		int capitalization = preferences.getInt(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_SETTER_CAPITALIZATION,
				PreferenceConstants.NAME_STYLE_CAPITALIZATION_CAMEL_CASE, null);
		String wordDelimiter = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_SETTER_WORD_DELIMITER, "", null); //$NON-NLS-1$
		String prefix = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_SETTER_PREFIX, "set", null); //$NON-NLS-1$
		String suffix = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_SETTER_SUFFIX, "", null); //$NON-NLS-1$
		NameComposer composer = new NameComposer(capitalization, wordDelimiter, prefix, suffix);
		String name = GetterSetterNameGenerator.trimFieldName(fieldName.toString());
		return composer.compose(name);
	}

	/**
	 * Returns the trimmed field name. Leading and trailing non-alphanumeric characters are trimmed.
	 * If the first word of the name consists of a single letter and the name contains more than
 	 * one word, the first word is removed.
	 * 
	 * @param fieldName a field name to trim
	 * @return the trimmed field name
	 */
	public static String trimFieldName(String fieldName){
		CBreakIterator iterator = new CBreakIterator();
		iterator.setText(fieldName);
		int firstWordStart = -1;
		int firstWordEnd = -1;
		int secondWordStart = -1;
		int lastWordEnd = -1;
		int end;
		for (int start = iterator.first(); (end = iterator.next()) != BreakIterator.DONE; start = end) {
			if (Character.isLetterOrDigit(fieldName.charAt(start))) {
				int pos = end;
				while (--pos >= start && !Character.isLetterOrDigit(fieldName.charAt(pos))) {
				}
				lastWordEnd = pos + 1;
				if (firstWordStart < 0) {
					firstWordStart = start;
					firstWordEnd = lastWordEnd;
				} else if (secondWordStart < 0) {
					secondWordStart = start;
				}
			}
		}
		// Skip the first word if it consists of a single letter and the name contains more than
		// one word.
		if (firstWordStart >= 0 && firstWordStart + 1 == firstWordEnd && secondWordStart >= 0) {
			firstWordStart = secondWordStart;
		}
		if (firstWordStart < 0) {
			return fieldName;
		} else {
			return fieldName.substring(firstWordStart, lastWordEnd);
		}
	}
}
