/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeTransformationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUnaryTypeTransformation.Operator;

/**
 * Implementation of ICPPASTTypeTransformationSpecifier.
 */
public class CPPASTTypeTransformationSpecifier extends CPPASTBaseDeclSpecifier
		implements ICPPASTTypeTransformationSpecifier {
	private Operator fOperator;
	private ICPPASTTypeId fOperand;

	public CPPASTTypeTransformationSpecifier(Operator operator, ICPPASTTypeId operand) {
		fOperator = operator;
		fOperand = operand;
		fOperand.setParent(this);
		fOperand.setPropertyInParent(OPERAND);
	}

	@Override
	public ICPPASTDeclSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTTypeTransformationSpecifier copy(CopyStyle style) {
		CPPASTTypeTransformationSpecifier copy = new CPPASTTypeTransformationSpecifier(fOperator, fOperand.copy(style));
		return super.copy(copy, style);
	}

	@Override
	public Operator getOperator() {
		return fOperator;
	}

	@Override
	public ICPPASTTypeId getOperand() {
		return fOperand;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclSpecifiers) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (!fOperand.accept(action))
			return false;

		if (action.shouldVisitDeclSpecifiers) {
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
}
