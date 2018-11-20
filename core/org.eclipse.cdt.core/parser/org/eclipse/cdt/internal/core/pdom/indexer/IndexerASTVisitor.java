/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.internal.core.pdom.indexer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

abstract public class IndexerASTVisitor extends ASTVisitor {
	/**
	 * Represents a definition of a class or function.
	 * IndexerASTVisitor builds a tree of these definitions, used for tracking enclosing
	 * definitions of names.
	 */
	public static class Definition {
		Definition(IASTName name, IASTNode node) {
			fName = name;
			fNode = node;
		}

		IASTName fName; // The name of the entity being defined.
		IASTNode fNode; // The AST node for the entire definition.
		List<Definition> fChildren; // Definitions contained within this one.

		/**
		 * Search the subtree of definitions rooted at this one for the nearest
		 * definition that encloses the range defined by 'offset' and 'length'.
		 * The name of the resulting definition is returned.
		 * This function assumes that 'this.matches(offset, length)' is true.
		 */
		public IASTName search(int offset, int length) {
			if (fChildren != null) {
				for (Definition child : fChildren) {
					if (child.matches(offset, length)) {
						return child.search(offset, length);
					}
				}
			}
			return fName;
		}

		/**
		 * Check whether this definition encloses the range defined by 'offset' and 'length'.
		 */
		boolean matches(int offset, int length) {
			if (!(fNode instanceof ASTNode)) {
				return false;
			}
			ASTNode node = (ASTNode) fNode;
			int nodeOffset = node.getOffset();
			int nodeLength = node.getLength();
			return nodeOffset <= offset && (nodeOffset + nodeLength) >= (offset + length);
		}
	}

	private IASTName fDefinitionName;
	private IASTNode fDefinitionNode;
	private ArrayList<Definition> fStack = new ArrayList<>();
	private ArrayList<IASTProblem> fProblems = new ArrayList<>();

	public IndexerASTVisitor(boolean visitImplicitNames) {
		shouldVisitNames = true;
		shouldVisitImplicitNames = visitImplicitNames;
		shouldVisitDeclarations = true;
		shouldVisitInitializers = true;
		shouldVisitDeclSpecifiers = true;
		shouldVisitProblems = true;
		shouldVisitExpressions = true;

		// Root node representing the entire file
		fStack.add(new Definition(null, null));
	}

	public List<IASTProblem> getProblems() {
		return fProblems;
	}

	abstract public void visit(IASTName name, IASTName definitionName);

	@Override
	final public int visit(IASTName name) {
		if (!(name instanceof ICPPASTQualifiedName)) {
			if (name != fDefinitionName) {
				visit(name, fDefinitionName);
			}
		}
		return PROCESS_CONTINUE;
	}

	private void push(IASTName name, IASTNode node) {
		assert !fStack.isEmpty();
		Definition def = new Definition(name, node);
		Definition parent = fStack.get(fStack.size() - 1);
		if (parent.fChildren == null) {
			parent.fChildren = new ArrayList<>();
		}
		parent.fChildren.add(def);
		fStack.add(def);
		name = getLastInQualified(name);
		fDefinitionName = name;
		fDefinitionNode = node;
	}

	private IASTName getLastInQualified(IASTName name) {
		return name.getLastName();
	}

	private void pop(IASTNode node) {
		if (node == fDefinitionNode) {
			assert !fStack.isEmpty();
			fStack.remove(fStack.size() - 1);
			if (fStack.isEmpty()) {
				fDefinitionName = null;
				fDefinitionNode = null;
			} else {
				Definition old = fStack.get(fStack.size() - 1);
				fDefinitionName = old.fName;
				fDefinitionNode = old.fNode;
			}
		}
	}

	// functions and methods
	@Override
	public int visit(IASTDeclaration decl) {
		if (decl instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition fdef = (IASTFunctionDefinition) decl;
			final IASTFunctionDeclarator declarator = fdef.getDeclarator();
			IASTDeclarator nestedDeclarator = declarator;
			while (nestedDeclarator.getNestedDeclarator() != null) {
				nestedDeclarator = nestedDeclarator.getNestedDeclarator();
			}
			IASTName name = getLastInQualified(nestedDeclarator.getName());
			visit(name, fDefinitionName);
			push(name, decl);
		} else if (decl instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) decl;
			if (sdecl.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef) {
				IASTDeclarator[] declarators = sdecl.getDeclarators();
				for (IASTDeclarator declarator : declarators) {
					if (declarator.getPointerOperators().length == 0 && declarator.getNestedDeclarator() == null) {
						IASTName name = getLastInQualified(declarator.getName());
						visit(name, fDefinitionName);
						push(name, decl);
					}
				}
			}
		}
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTDeclaration decl) {
		pop(decl);
		return PROCESS_CONTINUE;
	}

	// class definitions, typedefs
	@Override
	public int visit(IASTDeclSpecifier declspec) {
		if (declspec instanceof ICPPASTCompositeTypeSpecifier) {
			ICPPASTCompositeTypeSpecifier cts = (ICPPASTCompositeTypeSpecifier) declspec;
			IASTName name = getLastInQualified(cts.getName());
			visit(name, fDefinitionName);
			push(name, declspec);
		}
		if (declspec instanceof ICASTCompositeTypeSpecifier) {
			ICASTCompositeTypeSpecifier cts = (ICASTCompositeTypeSpecifier) declspec;
			IASTName name = cts.getName();
			visit(name, fDefinitionName);
			push(name, declspec);
		}
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTDeclSpecifier declspec) {
		pop(declspec);
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTProblem problem) {
		fProblems.add(problem);
		return PROCESS_SKIP;
	}

	// variable and field initializers
	@Override
	public int visit(IASTInitializer initializer) {
		if (!(fDefinitionNode instanceof IASTFunctionDefinition)) {
			IASTNode cand = initializer.getParent();
			if (cand instanceof IASTDeclarator) {
				cand = ASTQueries.findInnermostDeclarator((IASTDeclarator) cand);
				push(((IASTDeclarator) cand).getName(), initializer);
			}
		}
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTInitializer initializer) {
		pop(initializer);
		return PROCESS_CONTINUE;
	}

	// Lambda expressions
	@Override
	public int visit(IASTExpression expr) {
		if (expr instanceof ICPPASTLambdaExpression) {
			return visit((ICPPASTLambdaExpression) expr);
		}
		return PROCESS_CONTINUE;
	}

	public Definition getDefinitionTree() {
		assert !fStack.isEmpty();
		return fStack.get(0);
	}

	private int visit(final ICPPASTLambdaExpression lambdaExpr) {
		// Captures
		for (ICPPASTCapture cap : lambdaExpr.getCaptures()) {
			if (!cap.accept(this))
				return PROCESS_ABORT;
		}
		// Definition of closure type
		final IASTName closureName = lambdaExpr.getClosureTypeName();
		visit(closureName, fDefinitionName);

		// Definition of call operator and conversion operator (if applicable)
		IASTName[] ops = lambdaExpr.getImplicitNames();
		for (IASTName op : ops) {
			visit(op, closureName);

		}

		IBinding owner = CPPVisitor.findDeclarationOwner(lambdaExpr, true);
		boolean localToFunction = owner instanceof IFunction;
		if (!localToFunction)
			// Local closures don't appear in the index, so don't refer to them.
			push(lambdaExpr.getFunctionCallOperatorName(), lambdaExpr);

		ICPPASTFunctionDeclarator dtor = lambdaExpr.getDeclarator();
		if (dtor != null && !dtor.accept(this))
			return PROCESS_ABORT;

		IASTCompoundStatement body = lambdaExpr.getBody();
		if (body != null && !body.accept(this))
			return PROCESS_ABORT;

		if (!localToFunction)
			pop(lambdaExpr);
		return PROCESS_SKIP;
	}
}