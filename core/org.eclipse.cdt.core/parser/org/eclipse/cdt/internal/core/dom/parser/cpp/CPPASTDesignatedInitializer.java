/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Implementation for designated initializers.
 */
public class CPPASTDesignatedInitializer extends ASTNode implements ICPPASTDesignatedInitializer, IASTAmbiguityParent {
	private ICPPASTInitializerClause rhs;
	private ICPPASTDesignator[] designators = ICPPASTDesignator.EMPTY_ARRAY;
	private int designatorsPos;

	public CPPASTDesignatedInitializer() {
	}

	public CPPASTDesignatedInitializer(ICPPASTInitializerClause init) {
		setOperand(init);
	}

	@Override
	public CPPASTDesignatedInitializer copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTDesignatedInitializer copy(CopyStyle style) {
		CPPASTDesignatedInitializer copy = new CPPASTDesignatedInitializer(
				rhs == null ? null : (ICPPASTInitializerClause) rhs.copy(style));
		for (ICPPASTDesignator designator : getDesignators()) {
			copy.addDesignator(designator == null ? null : designator.copy(style));
		}
		return copy(copy, style);
	}

	@Override
	public void addDesignator(ICPPASTDesignator designator) {
		assertNotFrozen();
		if (designator != null) {
			designator.setParent(this);
			designator.setPropertyInParent(DESIGNATOR);
			designators = ArrayUtil.appendAt(designators, designatorsPos++, designator);
		}
	}

	@Override
	public ICPPASTDesignator[] getDesignators() {
		designators = ArrayUtil.trim(designators, designatorsPos);
		return designators;
	}

	@Override
	public ICPPASTInitializerClause getOperand() {
		return rhs;
	}

	@Override
	public void setOperand(ICPPASTInitializerClause operand) {
		assertNotFrozen();
		this.rhs = operand;
		if (rhs != null) {
			rhs.setParent(this);
			rhs.setPropertyInParent(OPERAND);
		}
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		return rhs.getEvaluation();
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitInitializers) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		ICPPASTDesignator[] ds = getDesignators();
		for (int i = 0; i < ds.length; i++) {
			if (!ds[i].accept(action))
				return false;
		}
		if (rhs != null && !rhs.accept(action))
			return false;

		if (action.shouldVisitInitializers) {
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
		if (child == rhs) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			rhs = (ICPPASTInitializerClause) other;
		}
	}
}
