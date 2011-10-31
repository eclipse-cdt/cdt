/*******************************************************************************
 * Copyright (c) 2009, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.HashSet;
import java.util.LinkedList;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Visitor to resolve ast ambiguities in the right order
 */
final class CPPASTAmbiguityResolver extends ASTVisitor {
	private int fSkipInitializers= 0;
	private int fDeferFunctions= 1;
	private HashSet<IASTDeclaration> fRepopulate= new HashSet<IASTDeclaration>();
	private LinkedList<IASTNode> fDeferredNodes= new LinkedList<IASTNode>();
	
	public CPPASTAmbiguityResolver() {
		super(false);
		includeInactiveNodes= true;
		shouldVisitAmbiguousNodes= true;
		shouldVisitDeclarations= true;
		shouldVisitDeclSpecifiers= true;
		shouldVisitInitializers= true;
		shouldVisitTranslationUnit= true;
	}

	@Override
	public int visit(ASTAmbiguousNode astAmbiguousNode) {
		IASTNode node= astAmbiguousNode.resolveAmbiguity(this);
		if (node instanceof IASTDeclarator) {
			while (node != null) {
				if (node instanceof IASTDeclaration) {
					fRepopulate.add((IASTDeclaration) node);
					break;
				}
				if (node instanceof IASTParameterDeclaration) {
					// If the parameter declaration belongs to a function declaration or
					// function definition we need to update the scope.
					IASTNode parent= node.getParent();
					if (parent instanceof IASTDeclarator) {
						IASTDeclarator dtor= (IASTDeclarator) parent;
						if (dtor == ASTQueries.findTypeRelevantDeclarator(dtor) &&
								ASTQueries.findOutermostDeclarator(dtor).getParent() instanceof IASTDeclaration) {
							repopulateScope((IASTParameterDeclaration) node);
						}
					}
					break;
				}
				if (node instanceof IASTExpression) {
					break;
				} 
				node= node.getParent();
			}
		} else if (node instanceof IASTDeclaration) {
			repopulateScope((IASTDeclaration) node);
		}
		return PROCESS_SKIP;
	}
	
	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
			fDeferFunctions++;
		}
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTDeclSpecifier declSpec) {
		if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
			fDeferFunctions--;
			
			// Resolve class type definitions, such that the scope is available
			// during ambiguity resolution.
			((ICPPASTCompositeTypeSpecifier) declSpec).getName().resolveBinding();
			
			// Trigger computation of implicit members.
			if (declSpec instanceof CPPASTCompositeTypeSpecifier)
				((CPPASTCompositeTypeSpecifier) declSpec).setAmbiguitiesResolved();
		}
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTDeclaration decl) {
		if (fDeferFunctions > 0 && decl instanceof IASTFunctionDefinition) {
			final IASTFunctionDefinition fdef= (IASTFunctionDefinition) decl;

			// visit the declarator first, it may contain ambiguous template arguments needed 
			// for associating the template declarations.
			fSkipInitializers++;
			ASTQueries.findOutermostDeclarator(fdef.getDeclarator()).accept(this);
			fSkipInitializers--;
			fdef.getDeclSpecifier().accept(this);
			// defer visiting the body of the function until the class body has been visited.
			fDeferredNodes.add(decl);
			return PROCESS_SKIP;
		} 
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTDeclaration declaration) {
		if (fRepopulate.remove(declaration)) {
			repopulateScope(declaration);
		}
		// We need to create class bindings for all definitions and for the specializations.
		// Otherwise, name resolution cannot access members or correct specialization.
		if (declaration instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration sdecl= (IASTSimpleDeclaration) declaration;
			IASTName name= null;
			IASTDeclSpecifier declspec = sdecl.getDeclSpecifier();
			if (declspec instanceof IASTCompositeTypeSpecifier) {
				// Definition of a class[template[specialization]]
				name= ((IASTCompositeTypeSpecifier) declspec).getName().getLastName();
			} else if (declspec instanceof ICPPASTElaboratedTypeSpecifier
					&& sdecl.getDeclarators().length == 0) {
				ASTNodeProperty prop = declaration.getPropertyInParent();
				if (prop == ICPPASTTemplateDeclaration.OWNED_DECLARATION 
						|| prop == ICPPASTTemplateSpecialization.OWNED_DECLARATION) {
					ICPPASTElaboratedTypeSpecifier elab= (ICPPASTElaboratedTypeSpecifier) declspec;
					if (!elab.isFriend()) {
						// Declaration of a class template specialization.
						name= elab.getName().getLastName();
					}
				}
			}
			if (name instanceof ICPPASTTemplateId) {
				name.resolveBinding();
			}
		}
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTInitializer initializer) {
		if (fSkipInitializers > 0)
			return PROCESS_SKIP;

		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTTranslationUnit tu) {
		while (!fDeferredNodes.isEmpty()) {
			fDeferFunctions= 0;
			fDeferredNodes.removeFirst().accept(this);
		}
		return PROCESS_CONTINUE;
	}

	private void repopulateScope(IASTDeclaration declaration) {
		IScope scope= CPPVisitor.getContainingNonTemplateScope(declaration);
		if (scope instanceof ICPPASTInternalScope) {
			CPPSemantics.populateCache((ICPPASTInternalScope) scope, declaration);
		}
	}
	private void repopulateScope(IASTParameterDeclaration declaration) {
		IScope scope= CPPVisitor.getContainingNonTemplateScope(declaration);
		if (scope instanceof ICPPASTInternalScope) {
			CPPSemantics.populateCache((ICPPASTInternalScope) scope, declaration);
		}
	}
}
