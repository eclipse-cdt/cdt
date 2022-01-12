/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecDefault;

/**
 * @author jcamelon
 */
public class CPPASTDefaultStatement extends CPPASTAttributeOwner implements IASTDefaultStatement, ICPPExecutionOwner {
	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitStatements) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (!acceptByAttributeSpecifiers(action))
			return false;

		if (action.shouldVisitStatements) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}

	@Override
	public CPPASTDefaultStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTDefaultStatement copy(CopyStyle style) {
		CPPASTDefaultStatement copy = new CPPASTDefaultStatement();
		return copy(copy, style);
	}

	@Override
	public ICPPExecution getExecution() {
		return new ExecDefault();
	}
}
