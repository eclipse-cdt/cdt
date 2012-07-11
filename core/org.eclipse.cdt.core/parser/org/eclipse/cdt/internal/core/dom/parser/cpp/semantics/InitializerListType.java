/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.IType;

/**
 * Wrapper for initializer lists to allow for participation in the overload resolution.
 */
class InitializerListType implements IType {
	private final EvalInitList fInitializerList;

	public InitializerListType(EvalInitList exprEvalInitList) {
		fInitializerList= exprEvalInitList;
	}

	public EvalInitList getEvaluation() {
		return fInitializerList;
	}
	
	@Override
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
}
