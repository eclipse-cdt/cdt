/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

abstract public class IndexerASTVisitor extends ASTVisitor {
	private IASTName fDefinitionName;
	private IASTNode fDefinitionNode;
	private ArrayList fStack= new ArrayList();

	public IndexerASTVisitor() {
		shouldVisitNames= true;
		shouldVisitDeclarations= true;
		shouldVisitInitializers= true;
		shouldVisitDeclSpecifiers= true;
	}

	abstract public void visit(IASTName name, IASTName definitionName);

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
			fStack.add(new Object[] {fDefinitionName, fDefinitionNode});
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
				Object[] old= (Object[]) fStack.remove(fStack.size()-1);
				fDefinitionName= (IASTName) old[0];
				fDefinitionNode= (IASTNode) old[1];
			}
		}
	}

	// functions and methods
	public int visit(IASTDeclaration decl) {
		if (decl instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition fdef= (IASTFunctionDefinition) decl;
			IASTName name = getLastInQualified(fdef.getDeclarator().getName());
			visit(name, fDefinitionName);
			push(name, decl);
		}
		else if (decl instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration sdecl= (IASTSimpleDeclaration) decl;
			if (sdecl.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef) {
				IASTDeclarator[] declarators= sdecl.getDeclarators();
				for (int i = 0; i < declarators.length; i++) {
					IASTDeclarator declarator = declarators[i];
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

	public int leave(IASTDeclaration decl) {
		pop(decl);
		return PROCESS_CONTINUE;
	}

	// class definitions, typedefs
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

	public int leave(IASTDeclSpecifier declspec) {
		pop(declspec);
		return PROCESS_CONTINUE;
	}

	// variable and field initializers
	public int visit(IASTInitializer initializer) {
		if (!(fDefinitionNode instanceof IASTFunctionDefinition)) {
			IASTNode cand= initializer.getParent();
			if (cand instanceof IASTDeclarator) {
				push(((IASTDeclarator) cand).getName(), initializer);
			}
		}
		return PROCESS_CONTINUE;
	}
	
	public int leave(IASTInitializer initializer) {
		pop(initializer);
		return PROCESS_CONTINUE;
	}
}