/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;

/**
 * Useful functions for doing code analysis on c/c++ AST
 */
public final class CxxAstUtils {
	private static CxxAstUtils instance;
	private CxxAstUtils(){
		// private constructor
	}
	public synchronized static CxxAstUtils getInstance(){
		if (instance==null) instance = new CxxAstUtils();
		return instance;
	}
	public IType unwindTypedef(IType type) {
		if (!(type instanceof IBinding)) return type;
		IBinding typeName = (IBinding) type;
		// unwind typedef chain
		while (typeName instanceof ITypedef) {
			IType t = ((ITypedef) typeName).getType();
			if (t instanceof IBinding)
				typeName = (IBinding) t;
			else
				return t;
		}
		return (IType)typeName;
	}
}
