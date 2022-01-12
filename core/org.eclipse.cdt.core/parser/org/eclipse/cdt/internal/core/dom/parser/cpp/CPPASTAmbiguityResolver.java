/*******************************************************************************
 * Copyright (c) 2009, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Stack;

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
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Visitor to resolve AST ambiguities in the right order
 */
final class CPPASTAmbiguityResolver extends ASTVisitor {
	private int fSkipInitializers = 0;
	/*
	 * The current nesting level of class definitions.
	 * Used to handle processing of method bodies, which are deferred
	 * until the end of the outermost class definition.
	 */
	private int fClassNestingLevel = 0;
	private HashSet<IASTDeclaration> fRepopulate = new HashSet<>();
	/*
	 * Nodes that have been deferred for later processing.
	 * Currently used only for method bodies.
	 */
	private Deque<IASTNode> fDeferredNodes = new ArrayDeque<>();

	/*
	 * Used by visit(IASTDeclaration) to determine whether it should
	 * process a function declaration now instead of deferring it
	 * to later. There is a stack of them because, thanks to local
	 * classes, function definitions can be nested inside each other.
	 */
	private Stack<IASTFunctionDefinition> fProcessNow = new Stack<>();

	public CPPASTAmbiguityResolver() {
		super(false);
		includeInactiveNodes = true;
		shouldVisitAmbiguousNodes = true;
		shouldVisitDeclarations = true;
		shouldVisitDeclSpecifiers = true;
		shouldVisitInitializers = true;
		shouldVisitTranslationUnit = true;
	}

	@Override
	public int visit(ASTAmbiguousNode astAmbiguousNode) {
		IASTNode node = astAmbiguousNode.resolveAmbiguity(this);
		if (node instanceof IASTDeclarator) {
			while (node != null) {
				if (node instanceof IASTDeclaration) {
					fRepopulate.add((IASTDeclaration) node);
					break;
				}
				if (node instanceof IASTParameterDeclaration) {
					// If the parameter declaration belongs to a function declaration or
					// function definition we need to update the scope.
					IASTNode parent = node.getParent();
					if (parent instanceof IASTDeclarator) {
						IASTDeclarator dtor = (IASTDeclarator) parent;
						if (dtor == ASTQueries.findTypeRelevantDeclarator(dtor)
								&& ASTQueries.findOutermostDeclarator(dtor).getParent() instanceof IASTDeclaration) {
							repopulateScope((IASTParameterDeclaration) node);
						}
					}
					break;
				}
				if (node instanceof IASTExpression) {
					break;
				}
				node = node.getParent();
			}
		} else if (node instanceof IASTDeclaration) {
			repopulateScope((IASTDeclaration) node);
		}
		return PROCESS_SKIP;
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
			fClassNestingLevel++;
		}
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTDeclSpecifier declSpec) {
		if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
			fClassNestingLevel--;

			// Resolve class type definitions, such that the scope is available
			// during ambiguity resolution.
			((ICPPASTCompositeTypeSpecifier) declSpec).getName().resolveBinding();

			// Trigger computation of implicit members.
			if (declSpec instanceof CPPASTCompositeTypeSpecifier)
				((CPPASTCompositeTypeSpecifier) declSpec).setAmbiguitiesResolved();

			// If we are leaving the outermost class, process the bodies of
			// methods of the class and its nested classes.
			if (fClassNestingLevel == 0) {
				while (!fDeferredNodes.isEmpty()) {
					fDeferredNodes.removeFirst().accept(this);
				}
			}
		}
		return PROCESS_CONTINUE;
	}

	private boolean shouldProcessNow(IASTFunctionDefinition func) {
		return !fProcessNow.isEmpty() && fProcessNow.peek() == func;
	}

	@Override
	public int visit(IASTDeclaration decl) {
		if (decl instanceof IASTFunctionDefinition && !shouldProcessNow((IASTFunctionDefinition) decl)) {
			final IASTFunctionDefinition fdef = (IASTFunctionDefinition) decl;

			// Visit the declarator first, it may contain ambiguous template arguments needed
			// for associating the template declarations.
			ICPPASTFunctionDeclarator fdecl = (ICPPASTFunctionDeclarator) fdef.getDeclarator();
			fSkipInitializers++; // Initializers may refer to class members declared later.
			fdecl.accept(this);
			fSkipInitializers--;
			fdef.getDeclSpecifier().accept(this);
			IASTTypeId trailingReturnType = fdecl.getTrailingReturnType();
			if (trailingReturnType != null) {
				// Visit initializers inside the trailing return type that were skipped earlier.
				trailingReturnType.accept(this);
			}
			if (fClassNestingLevel > 0) {
				// If this is a method defined inline inside a class declaration, defer visiting
				// the remaining parts of the method (notably the body) until the end of the
				// class declaration has been reached.
				fDeferredNodes.add(decl);
			} else {
				// Otherwise, visit the remaining parts of the method now. To avoid duplicating
				// code in CPPASTFunctionDefinition.accept(), call accept() on the entire
				// definition, but push the function definition onto fProcessNow to avoid recursion.
				fProcessNow.push(fdef);
				decl.accept(this);
				fProcessNow.pop();
			}
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
			IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) declaration;
			IASTName name = null;
			IASTDeclSpecifier declspec = sdecl.getDeclSpecifier();
			if (declspec instanceof IASTCompositeTypeSpecifier) {
				// Definition of a class[template[specialization]]
				name = ((IASTCompositeTypeSpecifier) declspec).getName().getLastName();
			} else if (declspec instanceof ICPPASTElaboratedTypeSpecifier && sdecl.getDeclarators().length == 0) {
				ASTNodeProperty prop = declaration.getPropertyInParent();
				if (prop == ICPPASTTemplateDeclaration.OWNED_DECLARATION
						|| prop == ICPPASTTemplateSpecialization.OWNED_DECLARATION) {
					ICPPASTElaboratedTypeSpecifier elab = (ICPPASTElaboratedTypeSpecifier) declspec;
					if (!elab.isFriend()) {
						// Declaration of a class template specialization.
						name = elab.getName().getLastName();
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
		// As deferred method bodies are processed at the end of outermost
		// class definitions, there should be none left when the end of
		// the translation unit is reached.
		assert fDeferredNodes.isEmpty();
		assert fProcessNow.isEmpty();
		return PROCESS_CONTINUE;
	}

	private void repopulateScope(IASTDeclaration declaration) {
		IScope scope = CPPVisitor.getContainingNonTemplateScope(declaration);
		if (scope instanceof ICPPASTInternalScope) {
			CPPSemantics.populateCache((ICPPASTInternalScope) scope, declaration);
		}
	}

	private void repopulateScope(IASTParameterDeclaration declaration) {
		IScope scope = CPPVisitor.getContainingNonTemplateScope(declaration);
		if (scope instanceof ICPPASTInternalScope) {
			CPPSemantics.populateCache((ICPPASTInternalScope) scope, declaration);
		}
	}

	/**
	 * If 'node' has been deferred for later processing, process it now.
	 */
	public void resolvePendingAmbiguities(IASTNode node) {
		for (IASTNode deferredNode : fDeferredNodes) {
			if (deferredNode == node) {
				// Temporarily set the class nesting level to 0,
				// to prevent the node just being deferred again.
				int classNestingLevel = fClassNestingLevel;
				fClassNestingLevel = 0;
				try {
					deferredNode.accept(this);
				} finally {
					fClassNestingLevel = classNestingLevel;
				}
				fDeferredNodes.remove(deferredNode);
				break;
			}
		}
	}
}
