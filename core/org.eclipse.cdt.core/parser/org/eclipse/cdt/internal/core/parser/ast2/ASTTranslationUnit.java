/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast2;

import org.eclipse.cdt.core.parser.ast2.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * This represents the global scope.
 * 
 * @author Doug Schaefer
 */
public class ASTTranslationUnit extends ASTScope implements IASTTranslationUnit {

	private IASTDeclaration builtins;
	
	// builtin types
	public static final char[] b_int = "int".toCharArray();
	
	public IASTDeclaration findDeclaration(char[] name) {
		IASTDeclaration decl = getDeclaration(name);
		if (decl != null)
			return decl;
		
		// See if it is a built in
		for (decl = builtins; decl != null; decl = decl.getNextDeclaration())
			if (CharArrayUtils.equals(name, decl.getName().getName().toCharArray()))
				return decl;
		
		// See if we need to populate it
		if (CharArrayUtils.equals(name, b_int))
			return addBuiltinType(b_int);

		// not found
		return null;
	}

	private IASTDeclaration addBuiltinType(char[] name) {
		ASTBuiltinType type = new ASTBuiltinType();
		type.setName(name);
		
		ASTIdentifier id = new ASTIdentifier();
		id.setName(name);
		
		ASTTypeDeclaration decl = new ASTTypeDeclaration();
		decl.setName(id);
		decl.setType(type);
		
		// insert to head of list
		decl.setNextDeclaration(builtins);
		builtins = decl;
		
		return decl;
	}
}
