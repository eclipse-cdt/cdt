/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;

/**
 * Helps with IASTNames.
 * 
 * @author Mirko Stocker
 * 
 */
public class NameHelper {

	
	private static final String localVariableRegexp = "[a-z_A-Z]\\w*"; //$NON-NLS-1$

	public static boolean isValidLocalVariableName(String name) {
		boolean valid = Pattern.compile(localVariableRegexp).matcher(name).matches();
		return valid;
	}
	
	public static boolean isKeyword(String name) {
		CharArrayIntMap keywords = new CharArrayIntMap(0, -1);
		Keywords.addKeywordsC(keywords);
		Keywords.addKeywordsCpp(keywords);
		Keywords.addKeywordsPreprocessor(keywords);
		return keywords.containsKey(name.toCharArray());
	}
	
	/**
	 * Constructs the fully qualified name from the given parameters. The file and offset parameters are used to determine
	 * the namespace at the declaration position and the target namespace at the target position.
	 * 
	 * @param declaratorName of the method or function
	 * @param declarationFile
	 * @param selectionOffset the offset in the declarationFile, usually the position or selection of the declaration
	 * @param insertFile the target file in which the definition is inserted
	 * @param insertLocation 
	 * @return the correct name for the target
	 * @throws CoreException 
	 */
	public static ICPPASTQualifiedName createQualifiedNameFor(IASTName declaratorName, IFile declarationFile, int selectionOffset, IFile insertFile, int insertLocation) 
		throws CoreException {
		ICPPASTQualifiedName qname = new CPPASTQualifiedName();
		
		IASTName[] declarationNames = NamespaceHelper.getSurroundingNamespace(declarationFile, selectionOffset).getNames();
		IASTName[] implementationNames = NamespaceHelper.getSurroundingNamespace(insertFile, insertLocation).getNames();
		
		for(int i = 0; i < declarationNames.length; i++) {
			if(i >= implementationNames.length) {
				qname.addName(declarationNames[i]);
			} else if (!String.valueOf(declarationNames[i].toCharArray()).equals(String.valueOf(implementationNames[i].toCharArray()))) {
				qname.addName(declarationNames[i]);
			}
		}

		qname.addName(declaratorName.copy());
		return qname;
	}
	
	public static String trimFieldName(String fieldName){
		char[] letters = fieldName.toCharArray();
		int start = 0;
		int end = letters.length - 1;
		try{
		while(!Character.isLetter(letters[start]) && start < end) {
			++start;
		}
		
		if(Character.isLowerCase(letters[start])){
			if(!Character.isLetter(letters[start + 1])){
				start+= 2;
			}
			else if (Character.isUpperCase(letters[start + 1])){
				start += 1;
			}
		}
		
		while((!Character.isLetter(letters[end]) && !Character.isDigit(letters[end])) && start < end) {
			--end;
		}
		}catch(IndexOutOfBoundsException e){}	
		
		return new String(letters, start, end - start + 1);

	}
	
	public static String makeFirstCharUpper(String name) {
		if(Character.isLowerCase(name.charAt(0))){
			name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		}
		return name;
	} 

	public static String getTypeName(IASTParameterDeclaration parameter) {
		IASTDeclSpecifier parameterDeclSpecifier = parameter.getDeclSpecifier();
		if (parameterDeclSpecifier instanceof ICPPASTNamedTypeSpecifier) {
			return ((ICPPASTNamedTypeSpecifier) parameterDeclSpecifier).getName().getRawSignature();
		} else {
			return parameterDeclSpecifier.getRawSignature();
		}
	}
}
