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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

abstract public class IndexerASTVisitor extends ASTVisitor {
	private IASTName fDefinitionName;
	private IASTNode fDefinitionNode;

	public IndexerASTVisitor() {
		shouldVisitNames= true;
		shouldVisitDeclarations= true;
		shouldVisitInitializers= true;
		shouldVisitDeclSpecifiers= true;
	}

	abstract public void visit(IASTName name, IASTName definitionName);

	final public int visit(IASTName name) {
		if (!(name instanceof ICPPASTQualifiedName)) {
			if (fDefinitionNode != null) {
				if (!fDefinitionNode.contains(name)) {
					fDefinitionNode= null;
					fDefinitionName= null;
				}
			}
			if (name != fDefinitionName) {
				visit(name, fDefinitionName);
			}
		}
		return PROCESS_CONTINUE;
	}

	public int visit(IASTDeclaration decl) {
		if (decl instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition fdef= (IASTFunctionDefinition) decl;
			fDefinitionNode= decl;
			fDefinitionName= fdef.getDeclarator().getName();
			if (fDefinitionName instanceof ICPPASTQualifiedName) {
				fDefinitionName= ((ICPPASTQualifiedName) fDefinitionName).getLastName();
			}
			visit(fDefinitionName, null);
		}
		return PROCESS_CONTINUE;
	}

// leave methods don't get called correctly: bug 152846
//	public int leave(IASTDeclaration decl) {
//		if (decl == fDefinitionNode) {
//			fDefinitionNode= null;
//			fDefinitionName= null;
//		}
//		return PROCESS_CONTINUE;
//	}

	public int visit(IASTDeclSpecifier declspec) {
		if (declspec instanceof ICPPASTCompositeTypeSpecifier) {
			if (fDefinitionNode == null || !fDefinitionNode.contains(declspec)) {
				ICPPASTCompositeTypeSpecifier cts= (ICPPASTCompositeTypeSpecifier) declspec;
				fDefinitionNode= declspec;
				fDefinitionName= cts.getName();
				if (fDefinitionName instanceof ICPPASTQualifiedName) {
					fDefinitionName= ((ICPPASTQualifiedName) fDefinitionName).getLastName();
				}
				visit(fDefinitionName, null);
			}
		}
		return PROCESS_CONTINUE;
	}

// leave methods don't get called correctly: bug 152846
//	public int leave(IASTDeclSpecifier declspec) {
//		if (declspec == fDefinitionNode) {
//			fDefinitionNode= null;
//			fDefinitionName= null;
//		}
//		return PROCESS_CONTINUE;
//	}

	public int visit(IASTInitializer initializer) {
		if (fDefinitionNode == null) {
			IASTNode cand= initializer.getParent();
			if (cand instanceof IASTDeclarator) {
				fDefinitionNode= cand;
				fDefinitionName= ((IASTDeclarator) cand).getName();
			}
		}
		return PROCESS_CONTINUE;
	}
	
	// leave methods don't get called correctly: bug 152846
//	public int leave(IASTInitializer initializer) {
//		if (fDefinitionNode == initializer) {
//			fDefinitionNode= null;
//			fDefinitionName= null;
//		}
//		return PROCESS_CONTINUE;
//	}
}