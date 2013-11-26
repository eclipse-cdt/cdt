/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDecltypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Implementation of ICPPASTDecltypeSpecifier.
 */
public class CPPASTDecltypeSpecifier extends ASTNode implements ICPPASTDecltypeSpecifier {
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
		return fDecltypeExpression.accept(visitor);
	}

	@Override
	public IBinding resolveBinding() {
		IType type = fDecltypeExpression.getExpressionType();
		if (type instanceof IBinding)
			return (IBinding) type;
		return null;
	}

	@Override
	public IBinding resolvePreBinding() {
		return resolveBinding();
	}
}
