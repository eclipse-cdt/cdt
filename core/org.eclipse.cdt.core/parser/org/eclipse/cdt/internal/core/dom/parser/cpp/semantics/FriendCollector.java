/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPASTInternalScope;

/**
 * Utility class to populate scope with friend declarations hidden in nested classes
 */
class FriendCollector extends ASTVisitor {

	private final ICPPASTInternalScope fScope;

	public FriendCollector(ICPPASTInternalScope scope) {
		fScope= scope;
		shouldVisitDeclarations= true;
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		if (declaration instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
			ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) simpleDeclaration.getDeclSpecifier();
			if (declSpec.isFriend()) {
				IASTDeclarator[] declarators= simpleDeclaration.getDeclarators();
				for (IASTDeclarator declarator : declarators) {
					IASTDeclarator innermost= null;
					while (declarator != null) {
						if (declarator instanceof IASTAmbiguousDeclarator) {
							innermost= null;
							break;
						}
						innermost= declarator;
						declarator= declarator.getNestedDeclarator();
					}
					if (innermost != null) {
						IASTName declaratorName = innermost.getName();
						ASTInternal.addName(fScope,  declaratorName);
					}
				}
				return PROCESS_SKIP;
			}
	
			if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
				return PROCESS_CONTINUE;
			}
		}
		return PROCESS_SKIP;
	}
}
