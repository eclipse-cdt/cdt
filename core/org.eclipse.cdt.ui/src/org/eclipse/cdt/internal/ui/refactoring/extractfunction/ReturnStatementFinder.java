/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

/**
 * @author Emanuel Graf IFS
 */
class ReturnStatementFinder extends ASTVisitor{
	private boolean containsReturnStmt;

	{
		shouldVisitStatements = true;
	}

	@Override
	public int visit(IASTStatement statement) {
		if (statement instanceof IASTReturnStatement) {
			containsReturnStmt = true;
			return ASTVisitor.PROCESS_SKIP;
		}
		return ASTVisitor.PROCESS_CONTINUE;
	}
	
	public boolean containsReturn() {
		return containsReturnStmt;
	}
}
