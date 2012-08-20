/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;

/**
 * @author Emanuel Graf IFS
 */
class NonExtractableStatementFinder extends ASTVisitor {
	private boolean containsContinueStmt;
	private boolean containsBreakStmt;
	
	{
		shouldVisitStatements = true;
	}

	@Override
	public int visit(IASTStatement statement) {
		if (statement instanceof IASTContinueStatement) {
			containsContinueStmt = true;
			return ASTVisitor.PROCESS_SKIP;
		} else if (statement instanceof IASTBreakStatement) {
			containsBreakStmt = true;
			return ASTVisitor.PROCESS_SKIP;
		} else if (statement instanceof IASTForStatement ||
				statement instanceof ICPPASTRangeBasedForStatement ||
				statement instanceof IASTWhileStatement ||
				statement instanceof IASTDoStatement ||
				statement instanceof IASTSwitchStatement) {
			// Extracting a whole loop or switch statement is allowed.
			return ASTVisitor.PROCESS_SKIP;
		}
		return ASTVisitor.PROCESS_CONTINUE;
	}

	public boolean containsContinue() {
		return containsContinueStmt;
	}
	
	public boolean containsBreak() {
		return containsBreakStmt;
	}
}
