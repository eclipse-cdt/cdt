/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Mar 8, 2005
 */
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;

public abstract class ASTVisitor {

	/**
	 * These values should be overriden in the implementation subclass.
	 */
	public boolean shouldVisitNames = false;

	public boolean shouldVisitDeclarations = false;

	public boolean shouldVisitInitializers = false;

	public boolean shouldVisitParameterDeclarations = false;

	public boolean shouldVisitDeclarators = false;

	public boolean shouldVisitDeclSpecifiers = false;

	public boolean shouldVisitExpressions = false;

	public boolean shouldVisitStatements = false;

	public boolean shouldVisitTypeIds = false;

	public boolean shouldVisitEnumerators = false;

	public boolean shouldVisitTranslationUnit = false;

	/**
	 * @return continue to continue visiting, abort to stop, skip to not descend
	 *         into this node.
	 */
	public final static int PROCESS_SKIP = 1;

	public final static int PROCESS_ABORT = 2;

	public final static int PROCESS_CONTINUE = 3;

	/**
	 * 
	 * visit methods
	 * 
	 */
	public int visit(IASTTranslationUnit tu) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTName name) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTDeclaration declaration) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTInitializer initializer) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTParameterDeclaration parameterDeclaration) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTDeclarator declarator) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTDeclSpecifier declSpec) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTExpression expression) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTStatement statement) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTTypeId typeId) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTEnumerator enumerator) {
		return PROCESS_CONTINUE;
	}
}