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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.parser.ast2.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTIdentifier;
import org.eclipse.cdt.core.parser.ast2.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ast2.IASTType;
import org.eclipse.cdt.core.parser.ast2.IASTTypeDeclaration;

/**
 * This represents the global scope.
 * 
 * @author Doug Schaefer
 */
public class ASTTranslationUnit extends ASTScope implements IASTTranslationUnit {

	// builtin types
	public static final IASTTypeDeclaration decl_int;
	public static final IASTTypeDeclaration decl_void;

	private static Map builtins = new HashMap();
	
	private static IASTTypeDeclaration createBuiltinType(String name_str) {
		IASTIdentifier name = new ASTIdentifier(name_str);
		IASTType type = new ASTType();
		IASTTypeDeclaration decl = new ASTTypeDeclaration();
		decl.setName(name);
		decl.setType(type);
		type.setDeclaration(decl);
		builtins.put(name, decl);
		return decl;
	}
	
	static {
		decl_int = createBuiltinType("int");
		decl_void = createBuiltinType("void");
	}
	
	public IASTDeclaration findDeclaration(IASTIdentifier name) {
		IASTDeclaration decl = super.findDeclaration(name);
		if (decl != null)
			return decl;
		
		return (IASTDeclaration)builtins.get(name);
	}

}
