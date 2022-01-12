/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTCompoundStatement extends ASTAttributeOwner implements IASTCompoundStatement, IASTAmbiguityParent {
	private IASTStatement[] statements;
	private IScope scope;

	@Override
	public CASTCompoundStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTCompoundStatement copy(CopyStyle style) {
		CASTCompoundStatement copy = new CASTCompoundStatement();
		for (IASTStatement statement : getStatements())
			copy.addStatement(statement == null ? null : statement.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTStatement[] getStatements() {
		if (statements == null)
			return IASTStatement.EMPTY_STATEMENT_ARRAY;
		return ArrayUtil.trim(IASTStatement.class, statements);
	}

	@Override
	public void addStatement(IASTStatement statement) {
		assertNotFrozen();
		statements = ArrayUtil.append(IASTStatement.class, statements, statement);
		if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(NESTED_STATEMENT);
		}
	}

	@Override
	public IScope getScope() {
		if (scope == null)
			scope = new CScope(this, EScopeKind.eLocal);
		return scope;
	}

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
		IASTStatement[] s = getStatements();
		for (int i = 0; i < s.length; i++) {
			if (!s[i].accept(action))
				return false;
		}

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
	public void replace(IASTNode child, IASTNode other) {
		if (statements == null)
			return;
		for (int i = 0; i < statements.length; ++i) {
			if (statements[i] == child) {
				other.setParent(statements[i].getParent());
				other.setPropertyInParent(statements[i].getPropertyInParent());
				statements[i] = (IASTStatement) other;
				break;
			}
		}
	}
}