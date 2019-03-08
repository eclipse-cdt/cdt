/*******************************************************************************
 * Copyright (c) 2019
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionSpecialization;

public class FunctionHelpers {
	public static ICPPASTFunctionDeclarator findDeclarator(ICPPFunction fun) {
		ICPPASTFunctionDeclarator funDecl = null;
		if (fun instanceof CPPFunction) {
			CPPFunction f = (CPPFunction) fun;
			if (f.getDeclarations() != null && f.getDeclarations()[0] instanceof ICPPASTFunctionDeclarator) {
				funDecl = (ICPPASTFunctionDeclarator) f.getDeclarations()[0];
			} else {
				funDecl = f.getDefinition();
			}
		} else if (fun instanceof ICPPFunctionSpecialization) {
			CPPFunctionTemplate funTemplate = (CPPFunctionTemplate) ((ICPPFunctionSpecialization) fun)
					.getSpecializedBinding();
			funDecl = funTemplate.getFirstFunctionDtor();
		}
		//		IASTNode node = name.getParent();
		//		//		if (node instanceof ICPPASTQualifiedName)
		//		//			node = node.getParent();
		//
		//		if (!(node instanceof IASTFunctionDeclarator))
		//			return null;
		//
		//		IASTFunctionDeclarator dtor = (IASTFunctionDeclarator) node;
		//		//		while (dtor.getParent() instanceof IASTDeclarator) {
		//		//			dtor = (IASTDeclarator) dtor.getParent();
		//		//		}

		return funDecl;
	}
}
