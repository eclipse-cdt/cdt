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
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTLabelStatement extends ASTAttributeOwner implements IASTLabelStatement, IASTAmbiguityParent {
	private IASTName name;
	private IASTStatement nestedStatement;

	public CASTLabelStatement() {
	}

	public CASTLabelStatement(IASTName name, IASTStatement nestedStatement) {
		setName(name);
		setNestedStatement(nestedStatement);
	}

	@Override
	public CASTLabelStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTLabelStatement copy(CopyStyle style) {
		CASTLabelStatement copy = new CASTLabelStatement();
		copy.setName(name == null ? null : name.copy(style));
		copy.setNestedStatement(nestedStatement == null ? null : nestedStatement.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTName getName() {
		return name;
	}

	@Override
	public void setName(IASTName name) {
		assertNotFrozen();
		this.name = name;
		if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(NAME);
		}
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
		if (name != null && !name.accept(action))
			return false;
		if (nestedStatement != null && !nestedStatement.accept(action))
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
	public int getRoleForName(IASTName n) {
		if (n == name)
			return r_declaration;
		return r_unclear;
	}

	@Override
	public IASTStatement getNestedStatement() {
		return nestedStatement;
	}

	@Override
	public void setNestedStatement(IASTStatement s) {
		assertNotFrozen();
		nestedStatement = s;
		if (s != null) {
			s.setParent(this);
			s.setPropertyInParent(NESTED_STATEMENT);
		}
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == nestedStatement) {
			other.setParent(this);
			other.setPropertyInParent(child.getPropertyInParent());
			setNestedStatement((IASTStatement) other);
		}
	}
}
