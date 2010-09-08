/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;

/**
 * Wrapper for initializer lists to allow for participation in the overload resolution.
 */
class InitializerListType implements IType {

	private final ICPPASTInitializerList fInitializerList;
	private IType[] fExpressionTypes;
	private ValueCategory[] fLValues;

	public InitializerListType(ICPPASTInitializerList list) {
		fInitializerList= list;
	}

	public boolean isSameType(IType type) {
		return false;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// Will not happen, we IType extends Clonable.
			return null;
		}
	}

	public ICPPASTInitializerList getInitializerList() {
		return fInitializerList;
	}

	public IType[] getExpressionTypes() {
		if (fExpressionTypes == null) {
			final IASTInitializerClause[] clauses = fInitializerList.getClauses();
			fExpressionTypes= new IType[clauses.length];
			for (int i = 0; i < clauses.length; i++) {
				IASTInitializerClause clause = clauses[i];
				if (clause instanceof IASTExpression) {
					fExpressionTypes[i]= ((IASTExpression) clause).getExpressionType();
				} else if (clause instanceof ICPPASTInitializerList) {
					fExpressionTypes[i]= new InitializerListType((ICPPASTInitializerList) clause);
				} else {
					assert false;
				}
			}
		}
		return fExpressionTypes;
	}

	public ValueCategory[] getValueCategories() {
		if (fLValues == null) {
			final IASTInitializerClause[] clauses = fInitializerList.getClauses();
			fLValues= new ValueCategory[clauses.length];
			for (int i = 0; i < clauses.length; i++) {
				IASTInitializerClause clause = clauses[i];
				if (clause instanceof IASTExpression) {
					fLValues[i]= ((IASTExpression) clause).getValueCategory();
				} 
			}
		}
		return fLValues;
	}
}
