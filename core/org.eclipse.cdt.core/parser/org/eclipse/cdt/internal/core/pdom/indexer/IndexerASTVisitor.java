/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

abstract public class IndexerASTVisitor extends ASTVisitor {
	private IASTName fDefinitionName;
	private IASTNode fDefinition;

	public IndexerASTVisitor() {
		shouldVisitNames = true;
		shouldVisitDeclarations = true;
	}

	abstract public void visit(IASTName name, IASTName definitionName);

	final public int visit(IASTName name) {
		if (!(name instanceof ICPPASTQualifiedName)) {
			if (fDefinition != null) {
				if (!fDefinition.contains(name)) {
					fDefinition= null;
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
			fDefinition= decl;
			fDefinitionName= fdef.getDeclarator().getName();
			if (fDefinitionName instanceof ICPPASTQualifiedName) {
				fDefinitionName= ((ICPPASTQualifiedName) fDefinitionName).getLastName();
			}
			visit(fDefinitionName, null);
		}
		return PROCESS_CONTINUE;
	}
}