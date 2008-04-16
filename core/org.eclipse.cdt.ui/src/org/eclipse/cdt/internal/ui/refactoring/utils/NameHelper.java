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

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

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
		return valid; /* && Keyword.getKeyword(stringToValidate, stringToValidate.length()) == null*/ //TODO Check for keywords?;
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
	 */
	public static ICPPASTQualifiedName createQualifiedNameFor(IASTName declaratorName, IFile declarationFile, int selectionOffset, IFile insertFile, int insertLocation) {
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

		qname.addName(declaratorName);
		return qname;
	}
}
