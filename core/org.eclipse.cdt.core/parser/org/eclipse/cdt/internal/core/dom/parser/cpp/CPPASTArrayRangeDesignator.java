/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sergey Prigogin (Google) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTArrayRangeDesignator;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Implementation of array range designator.
 */
public class CPPASTArrayRangeDesignator extends ASTNode implements IGPPASTArrayRangeDesignator, IASTAmbiguityParent {
	private ICPPASTExpression floor;
	private ICPPASTExpression ceiling;

	public CPPASTArrayRangeDesignator() {
	}

	public CPPASTArrayRangeDesignator(ICPPASTExpression floor, ICPPASTExpression ceiling) {
		setRangeFloor(floor);
		setRangeCeiling(ceiling);
	}

	@Override
	public CPPASTArrayRangeDesignator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTArrayRangeDesignator copy(CopyStyle style) {
		CPPASTArrayRangeDesignator copy = new CPPASTArrayRangeDesignator();
		copy.setRangeFloor(floor == null ? null : (ICPPASTExpression) floor.copy(style));
		copy.setRangeCeiling(ceiling == null ? null : (ICPPASTExpression) ceiling.copy(style));
		return copy(copy, style);
	}

	@Override
	public ICPPASTExpression getRangeFloor() {
		return this.floor;
	}

	@Override
	public void setRangeFloor(ICPPASTExpression expression) {
		assertNotFrozen();
		floor = expression;
		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(SUBSCRIPT_FLOOR_EXPRESSION);
		}
	}

	@Override
	public ICPPASTExpression getRangeCeiling() {
		return ceiling;
	}

	@Override
	public void setRangeCeiling(ICPPASTExpression expression) {
		assertNotFrozen();
		ceiling = expression;
		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(SUBSCRIPT_CEILING_EXPRESSION);
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDesignators) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		if (floor != null && !floor.accept(action))
			return false;
		if (ceiling != null && !ceiling.accept(action))
			return false;

		if (action.shouldVisitDesignators && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == floor) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			floor = (ICPPASTExpression) other;
		}
		if (child == ceiling) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			ceiling = (ICPPASTExpression) other;
		}
	}
}
