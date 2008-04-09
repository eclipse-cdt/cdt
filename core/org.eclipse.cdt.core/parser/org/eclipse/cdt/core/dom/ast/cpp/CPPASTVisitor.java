/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;

/**
 * C++ specific visitor class.
 * <br>The visit() methods implement a top-down traversal of the AST,
 * and the leave() methods implement a bottom-up traversal.
 */
public abstract class CPPASTVisitor extends ASTVisitor implements ICPPASTVisitor {
 
	public int visit(ICPPASTBaseSpecifier baseSpecifier) {
		return PROCESS_CONTINUE;
	}

	public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
		return PROCESS_CONTINUE;
	}

	public int visit(ICPPASTTemplateParameter templateParameter) {
		return PROCESS_CONTINUE;
	}

	public int leave(ICPPASTBaseSpecifier baseSpecifier) {
		return PROCESS_CONTINUE;
	}

	public int leave(ICPPASTNamespaceDefinition namespaceDefinition) {
		return PROCESS_CONTINUE;
	}

	public int leave(ICPPASTTemplateParameter templateParameter) {
		return PROCESS_CONTINUE;
	}
}
