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

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;

/**
 * Useful functions for doing code analysis on c/c++ AST
 */
public final class CxxAstUtils {
	private static CxxAstUtils instance;

	private CxxAstUtils() {
		// private constructor
	}

	public synchronized static CxxAstUtils getInstance() {
		if (instance == null)
			instance = new CxxAstUtils();
		return instance;
	}

	public IType unwindTypedef(IType type) {
		if (!(type instanceof IBinding))
			return type;
		IBinding typeName = (IBinding) type;
		// unwind typedef chain
		try {
			while (typeName instanceof ITypedef) {
				IType t = ((ITypedef) typeName).getType();
				if (t instanceof IBinding)
					typeName = (IBinding) t;
				else
					return t;
			}
		} catch (Exception e) { // in CDT 6.0 getType throws DOMException
			Activator.log(e);
		}
		return (IType) typeName;
	}

	public boolean isInMacro(IASTNode node) {
		IASTNodeSelector nodeSelector = node.getTranslationUnit()
				.getNodeSelector(node.getTranslationUnit().getFilePath());
		IASTFileLocation fileLocation = node.getFileLocation();
	
		IASTPreprocessorMacroExpansion macro = nodeSelector
				.findEnclosingMacroExpansion(fileLocation.getNodeOffset(),
						fileLocation.getNodeLength());
		return macro != null;
	}
}
