/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;

/**
 * Helps with IASTNames.
 * 
 * @author Mirko Stocker
 */
public class NameHelper {
	private static final Pattern localVariableRegexp = Pattern.compile("[a-z_A-Z]\\w*"); //$NON-NLS-1$

	public static boolean isValidLocalVariableName(String name) {
		return localVariableRegexp.matcher(name).matches();
	}
	
	public static boolean isKeyword(String name) {
		CharArrayIntMap keywords = new CharArrayIntMap(0, -1);
		Keywords.addKeywordsC(keywords);
		Keywords.addKeywordsCpp(keywords);
		Keywords.addKeywordsPreprocessor(keywords);
		return keywords.containsKey(name.toCharArray());
	}
	
	/**
	 * Constructs the fully qualified name from the given parameters. The file and offset parameters
	 * are used to determine the namespace at the declaration position and the target namespace at
	 * the target position.
	 * 
	 * @param declaratorName of the method or function
	 * @param declarationTu translation unit of the method or function declaration
	 * @param insertFileTu translation unit of the file where the implementation is being inserted
	 * @param selectionOffset the offset in the declarationFile, usually the position or selection
	 * 		of the declaration
	 * @param insertLocation 
	 * @return the correct name for the target
	 * @throws CoreException 
	 */
	public static ICPPASTQualifiedName createQualifiedNameFor(IASTName declaratorName,
			ITranslationUnit declarationTu, int selectionOffset, ITranslationUnit insertFileTu,
			int insertLocation, CRefactoringContext astCache) throws CoreException {
		ICPPASTQualifiedName qname = new CPPASTQualifiedName();
		
		IASTName[] declarationNames = NamespaceHelper.getSurroundingNamespace(declarationTu,
				selectionOffset, astCache).getNames();
		IASTName[] implementationNames = NamespaceHelper.getSurroundingNamespace(insertFileTu,
				insertLocation, astCache).getNames();
		
		for (int i = 0; i < declarationNames.length; i++) {
			if (i >= implementationNames.length) {
				qname.addName(declarationNames[i]);
			} else if (!Arrays.equals(declarationNames[i].toCharArray(), implementationNames[i].toCharArray())) {
				qname.addName(declarationNames[i]);
			}
		}

		qname.addName(declaratorName.copy(CopyStyle.withLocations));
		return qname;
	}
	
	public static String getTypeName(IASTParameterDeclaration parameter) {
		IASTName name = parameter.getDeclarator().getName();
		IBinding binding = name.resolveBinding();
		if (binding instanceof IVariable) {
			IType type = ((IVariable) binding).getType();
			if (type != null) {
				return ASTTypeUtil.getType(type);
			}
		}
		return ""; //$NON-NLS-1$
	}
}
