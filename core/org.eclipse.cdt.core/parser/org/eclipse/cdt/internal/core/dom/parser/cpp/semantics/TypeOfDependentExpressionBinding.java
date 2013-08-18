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
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.core.runtime.CoreException;

/**
 * Represent a type of a dependent expression used in a context where it's
 * expected to be a binding.
 */
public class TypeOfDependentExpressionBinding extends CPPUnknownBinding 
		implements ICPPUnknownType, ISerializableType {
	private TypeOfDependentExpression fType;
	
	public TypeOfDependentExpressionBinding(TypeOfDependentExpression type) {
		super(type.getSignature());
		fType = type;
	}
	
	public TypeOfDependentExpression getType() {
		return fType;
	}

	@Override
	public boolean isSameType(IType type) {
		return fType.isSameType(type);
	}

	@Override
	public IBinding getOwner() {
		return null;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		fType.marshal(buffer);
	}
}
