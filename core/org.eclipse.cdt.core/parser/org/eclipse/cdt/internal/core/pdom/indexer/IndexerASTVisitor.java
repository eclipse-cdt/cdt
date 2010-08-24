/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
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
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;

abstract public class IndexerASTVisitor extends ASTVisitor {
	private static class Definition {
		Definition(IASTName name, IASTNode node) {
			fName= name;
			fNode= node;
		}
		IASTName fName;
		IASTNode fNode;
	}
	
	private IASTName fDefinitionName;
	private IASTNode fDefinitionNode;
	private ArrayList<Definition> fStack= new ArrayList<Definition>();
	private ArrayList<IASTProblem> fProblems= new ArrayList<IASTProblem>();

	public IndexerASTVisitor(boolean visitImplicitNames) {
		shouldVisitNames= true;
		shouldVisitImplicitNames = visitImplicitNames;
		shouldVisitDeclarations= true;
		shouldVisitInitializers= true;
		shouldVisitDeclSpecifiers= true;
		shouldVisitProblems= true;
		shouldVisitExpressions= true;
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
		if (fDefinitionName != null) {
			fStack.add(new Definition(fDefinitionName, fDefinitionNode));
		}
		name = getLastInQualified(name);
		fDefinitionName= name;
		fDefinitionNode= node;
	}

	private IASTName getLastInQualified(IASTName name) {
		if (name instanceof ICPPASTQualifiedName) {
			name= ((ICPPASTQualifiedName) name).getLastName();
		}
		return name;
	}

	private void pop(IASTNode node) {
		if (node == fDefinitionNode) {
			if (fStack.isEmpty()) {
				fDefinitionName= null;
				fDefinitionNode= null;
			}
			else {
				Definition old= fStack.remove(fStack.size()-1);
				fDefinitionName= old.fName;
				fDefinitionNode= old.fNode;
			}
		}
	}

	// functions and methods
	@Override
	public int visit(IASTDeclaration decl) {
		if (decl instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition fdef= (IASTFunctionDefinition) decl;
			final IASTFunctionDeclarator declarator= fdef.getDeclarator();
			IASTDeclarator nestedDeclarator= declarator;
			while (nestedDeclarator.getNestedDeclarator() != null) {
				nestedDeclarator= nestedDeclarator.getNestedDeclarator();
			}
			IASTName name= getLastInQualified(nestedDeclarator.getName());
			visit(name, fDefinitionName);
			push(name, decl);
		} else if (decl instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration sdecl= (IASTSimpleDeclaration) decl;
			if (sdecl.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef) {
				IASTDeclarator[] declarators= sdecl.getDeclarators();
				for (IASTDeclarator declarator : declarators) {
					if (declarator.getPointerOperators().length == 0 &&
							declarator.getNestedDeclarator() == null) {
						IASTName name= getLastInQualified(declarator.getName());
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
			ICPPASTCompositeTypeSpecifier cts= (ICPPASTCompositeTypeSpecifier) declspec;
			IASTName name = getLastInQualified(cts.getName());
			visit(name, fDefinitionName);
			push(name, declspec);
		}
		if (declspec instanceof ICASTCompositeTypeSpecifier) {
			ICASTCompositeTypeSpecifier cts= (ICASTCompositeTypeSpecifier) declspec;
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
			IASTNode cand= initializer.getParent();
			if (cand instanceof IASTDeclarator) {
				cand= ASTQueries.findInnermostDeclarator((IASTDeclarator) cand);
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

	private int visit(final ICPPASTLambdaExpression lambdaExpr) {
		// Captures 
		for (ICPPASTCapture cap : lambdaExpr.getCaptures()) {
			if (!cap.accept(this))
				return PROCESS_ABORT;
		}
		// Definition of closure type
		final IASTName closureName = lambdaExpr.getClosureTypeName();
		visit(closureName, fDefinitionName);

		// Definition of call operator
		IASTName callOp= lambdaExpr.getFunctionCallOperatorName();
		visit(callOp, closureName);
		push(callOp, lambdaExpr);

		ICPPASTFunctionDeclarator dtor = lambdaExpr.getDeclarator();
		if (dtor != null && !dtor.accept(this))
			return PROCESS_ABORT;
		
		IASTCompoundStatement body = lambdaExpr.getBody();
		if (body != null && !body.accept(this))
			return PROCESS_ABORT;
		
		pop(lambdaExpr);
		return PROCESS_SKIP;
	}
}