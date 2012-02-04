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

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;

public class GetterSetterNameGenerator {
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

	// Do not instantiate.
	private GetterSetterNameGenerator() {
	}
	
	public static Set<String> getGenerateGetterSettersPreferenceKeys() {
		return generateGetterSettersPreferenceKeys;
	}

	/**
	 * Generates getter name for a given field name.
	 * 
	 * @param fieldName the name of the field
	 * @param namesToAvoid the set of names to avoid
	 * @return the generated getter name, or <code>null</code> if a valid name could not be
	 *     generated.
	 */
	public static String generateGetterName(IASTName fieldName, Set<String> namesToAvoid) {
		ITranslationUnit tu = getTranslationUnit(fieldName);
		return StubUtility.suggestGetterName(StubUtility.trimFieldName(fieldName.toString()),
				isBooleanDeclaratorName(fieldName), namesToAvoid, tu);
	}
	
	private static boolean isBooleanDeclaratorName(IASTName name) {
		if (IASTDeclarator.DECLARATOR_NAME.equals(name.getPropertyInParent())) {
			IASTDeclarator declarator = (IASTDeclarator) name.getParent();
			IType type = CPPVisitor.createType(declarator);
			type = SemanticUtil.getNestedType(type, SemanticUtil.CVTYPE | SemanticUtil.TDEF);
			if (type instanceof IBasicType && ((IBasicType) type).getKind() == IBasicType.Kind.eBoolean) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Generates setter name for a given field name.
	 * 
	 * @param fieldName the name of the field
	 * @param namesToAvoid the set of names to avoid
	 * @return the generated setter name, or <code>null</code> if a valid name could not be
	 *     generated.
	 */
	public static String generateSetterName(IASTName fieldName, Set<String> namesToAvoid) {
		ITranslationUnit tu = getTranslationUnit(fieldName);
		return StubUtility.suggestSetterName(StubUtility.trimFieldName(fieldName.toString()), namesToAvoid, tu);
	}

	public static String generateSetterParameterName(IASTName fieldName) {
		ITranslationUnit tu = getTranslationUnit(fieldName);
		return StubUtility.suggestParameterName(StubUtility.trimFieldName(fieldName.toString()), null, tu);
	}

	private static ITranslationUnit getTranslationUnit(IASTNode node) {
		return node.getTranslationUnit().getOriginatingTranslationUnit();
	}
}
