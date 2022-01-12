/*******************************************************************************
 * Copyright (c) 2013, 2014 Nathan Ridge
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDecltypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Implementation of ICPPASTDecltypeSpecifier.
 */
public class CPPASTDecltypeSpecifier extends ASTNode implements ICPPASTDecltypeSpecifier, IASTAmbiguityParent {
	private ICPPASTExpression fDecltypeExpression;
	private char[] fSignature;

	public CPPASTDecltypeSpecifier(ICPPASTExpression decltypeExpression) {
		fDecltypeExpression = decltypeExpression;
		fDecltypeExpression.setParent(this);
	}

	@Override
	public ICPPASTExpression getDecltypeExpression() {
		return fDecltypeExpression;
	}

	@Override
	public CPPASTDecltypeSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTDecltypeSpecifier copy(CopyStyle style) {
		CPPASTDecltypeSpecifier copy = new CPPASTDecltypeSpecifier((ICPPASTExpression) fDecltypeExpression.copy(style));
		return copy(copy, style);
	}

	@Override
	public char[] toCharArray() {
		if (fSignature == null) {
			StringBuilder buffer = new StringBuilder();
			buffer.append(Keywords.cDECLTYPE);
			buffer.append(Keywords.cpLPAREN);
			buffer.append(fDecltypeExpression.getEvaluation().getSignature());
			buffer.append(Keywords.cpRPAREN);
			final int len = buffer.length();
			fSignature = new char[len];
			buffer.getChars(0, len, fSignature, 0);
		}
		return fSignature;
	}

	@Override
	public boolean accept(ASTVisitor visitor) {
		if (visitor.shouldVisitDecltypeSpecifiers) {
			switch (visitor.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (!fDecltypeExpression.accept(visitor))
			return false;

		if (visitor.shouldVisitDecltypeSpecifiers) {
			switch (visitor.leave(this)) {
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
	public IBinding resolveBinding() {
		IType type = fDecltypeExpression.getExpressionType();
		type = SemanticUtil.getNestedType(type, SemanticUtil.CVTYPE);
		if (type instanceof IBinding)
			return (IBinding) type;
		return null;
	}

	@Override
	public IBinding resolvePreBinding() {
		return resolveBinding();
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == fDecltypeExpression) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fDecltypeExpression = (ICPPASTExpression) other;
			fSignature = null;
		}
	}
}
