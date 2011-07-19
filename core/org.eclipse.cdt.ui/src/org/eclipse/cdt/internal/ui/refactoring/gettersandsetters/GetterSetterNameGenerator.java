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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;


import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.util.NameComposer;

public class GetterSetterNameGenerator {

	// Do not instantiate.
	private GetterSetterNameGenerator() {
	}
	
	private static Set<String> generateGetterSettersPreferenceKeys = new HashSet<String>();
	static {
		generateGetterSettersPreferenceKeys.add(PreferenceConstants.NAME_STYLE_GETTER_CAPITALIZATION);
		generateGetterSettersPreferenceKeys.add(PreferenceConstants.NAME_STYLE_GETTER_WORD_DELIMITER);
		generateGetterSettersPreferenceKeys.add(PreferenceConstants.NAME_STYLE_GETTER_PREFIX_FOR_BOOLEAN);
		generateGetterSettersPreferenceKeys.add(PreferenceConstants.NAME_STYLE_GETTER_PREFIX);
		generateGetterSettersPreferenceKeys.add(PreferenceConstants.NAME_STYLE_GETTER_SUFFIX);
		generateGetterSettersPreferenceKeys.add(PreferenceConstants.NAME_STYLE_SETTER_CAPITALIZATION);
		generateGetterSettersPreferenceKeys.add(PreferenceConstants.NAME_STYLE_SETTER_WORD_DELIMITER);
		generateGetterSettersPreferenceKeys.add(PreferenceConstants.NAME_STYLE_SETTER_PREFIX);
		generateGetterSettersPreferenceKeys.add(PreferenceConstants.NAME_STYLE_SETTER_SUFFIX);
		generateGetterSettersPreferenceKeys.add(PreferenceConstants.NAME_STYLE_VARIABLE_CAPITALIZATION);
		generateGetterSettersPreferenceKeys.add(PreferenceConstants.NAME_STYLE_VARIABLE_WORD_DELIMITER);
		generateGetterSettersPreferenceKeys.add(PreferenceConstants.NAME_STYLE_VARIABLE_PREFIX);
		generateGetterSettersPreferenceKeys.add(PreferenceConstants.NAME_STYLE_VARIABLE_SUFFIX);
	}

	public static Set<String> getGenerateGetterSettersPreferenceKeys() {
		return generateGetterSettersPreferenceKeys;
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
    	String name = NameComposer.trimFieldName(fieldName.toString());
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
		String name = NameComposer.trimFieldName(fieldName.toString());
		return composer.compose(name);
	}

	public static String generateSetterParameterName(IASTName fieldName) {
		IPreferencesService preferences = Platform.getPreferencesService();
		int capitalization = preferences.getInt(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_VARIABLE_CAPITALIZATION,
				PreferenceConstants.NAME_STYLE_CAPITALIZATION_ORIGINAL, null);
		String wordDelimiter = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_VARIABLE_WORD_DELIMITER, "", null); //$NON-NLS-1$
		String prefix = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_VARIABLE_PREFIX, "", null); //$NON-NLS-1$
		String suffix = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_VARIABLE_SUFFIX, "", null); //$NON-NLS-1$
		NameComposer composer = new NameComposer(capitalization, wordDelimiter, prefix, suffix);
		String name = NameComposer.trimFieldName(fieldName.toString());
		return composer.compose(name);
	}
}
