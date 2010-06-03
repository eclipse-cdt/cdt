/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPASTInternalScope;

/**
 * Utility class to populate scope with friend declarations hidden in nested classes
 */
class NamespaceTypeCollector extends ASTVisitor {

	private final ICPPASTInternalScope fScope;

	public NamespaceTypeCollector(ICPPASTInternalScope scope) {
		fScope= scope;
		shouldVisitDeclarations= true;
		shouldVisitStatements= true;
		shouldVisitParameterDeclarations= true;
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
			} else if (declSpec instanceof ICPPASTElaboratedTypeSpecifier) {
				// 3.3.1.5 Point of declaration
				if (simpleDeclaration.getDeclarators().length != 0) { 
					addNonSimpleElabSpec((ICPPASTElaboratedTypeSpecifier) declSpec);
				}
			} 
			// Visit nested class definitions and parameter declarations
			return PROCESS_CONTINUE;
		} else if (declaration instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition funcDefinition = (IASTFunctionDefinition) declaration;
			ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) funcDefinition.getDeclSpecifier();
			if (declSpec.isFriend()) {
				IASTFunctionDeclarator declarator = funcDefinition.getDeclarator();
				ASTInternal.addName(fScope,  declarator.getName());
			} else if (declSpec instanceof ICPPASTElaboratedTypeSpecifier) {
				addNonSimpleElabSpec((ICPPASTElaboratedTypeSpecifier) declSpec);
			} 
			// Visit parameter declarations
			return PROCESS_CONTINUE;
		} 
		return PROCESS_SKIP;
	}

	
	@Override
	public int visit(IASTParameterDeclaration declaration) {
		IASTDeclSpecifier declSpec = declaration.getDeclSpecifier();
		if (declSpec instanceof ICPPASTElaboratedTypeSpecifier) {
			addNonSimpleElabSpec((ICPPASTElaboratedTypeSpecifier) declSpec);
		} 
		return PROCESS_SKIP;
	}

	private void addNonSimpleElabSpec(final ICPPASTElaboratedTypeSpecifier elabSpec) {
		if (elabSpec.getKind() != IASTElaboratedTypeSpecifier.k_enum) {
			final IASTName name = elabSpec.getName();
			if (!(name instanceof ICPPASTQualifiedName)) {
				ASTInternal.addName(fScope,  name);
			}
		}
	}

	@Override
	public int visit(IASTStatement statement) {
		// Don't visit function bodies
		return PROCESS_SKIP;
	}
}
