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

import org.eclipse.cdt.core.parser.ast2.IASTBuiltinType;
import org.eclipse.cdt.core.parser.ast2.IASTIdentifier;
import org.eclipse.cdt.core.parser.ast2.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ast2.IASTType;

/**
 * This represents the global scope.
 * 
 * @author Doug Schaefer
 */
public class ASTTranslationUnit extends ASTScope implements IASTTranslationUnit {

	// builtin types
	public static final IASTBuiltinType b_int = new ASTBuiltinType("int");
	
	private Map builtins = new HashMap();
	{
		builtins.put(b_int.getName(), b_int);
	}
	
	public IASTType findType(IASTIdentifier name) {
		IASTType type = super.findType(name);
		if (type != null)
			return type;

		// See if it is a built in
		return (IASTBuiltinType)builtins.get(name);
	}

}
