/*******************************************************************************
 * Copyright (c) 2010, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

/**
 * Wrapper for initializer lists to allow for participation in the overload resolution.
 */
public class InitializerListType implements IType, ISerializableType {
	private final EvalInitList fInitializerList;

	public InitializerListType(EvalInitList exprEvalInitList) {
		fInitializerList = exprEvalInitList;
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

	@Override
	public String toString() {
		return "InitializerListType"; //$NON-NLS-1$
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.INITIALIZER_LIST_TYPE);
		buffer.marshalEvaluation(fInitializerList, true);
	}

	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		EvalInitList evalInitList = (EvalInitList) buffer.unmarshalEvaluation();
		return new InitializerListType(evalInitList);
	}
}
