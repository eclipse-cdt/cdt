/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents an occurrence of 'auto' or 'decltype(auto)' that has
 * not been resolved (replaced with a real type) because the information
 * necessary to resolve it (the function's body) is not available yet.
 */
public class CPPPlaceholderType implements ISerializableType, IType {
	public enum PlaceholderKind {
		Auto, DecltypeAuto
	}

	private final PlaceholderKind fPlaceholderKind;

	public CPPPlaceholderType(PlaceholderKind placeholderKind) {
		fPlaceholderKind = placeholderKind;
	}

	public PlaceholderKind getPlaceholderKind() {
		return fPlaceholderKind;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.PLACEHOLDER_TYPE;
		if (fPlaceholderKind == PlaceholderKind.DecltypeAuto) {
			firstBytes |= ITypeMarshalBuffer.FLAG1;
		}
		buffer.putShort(firstBytes);
	}

	@Override
	public boolean isSameType(IType type) {
		if (type instanceof CPPPlaceholderType) {
			return fPlaceholderKind == ((CPPPlaceholderType) type).fPlaceholderKind;
		}
		return false;
	}

	@Override
	public Object clone() {
		IType t = null;
		try {
			t = (IType) super.clone();
		} catch (CloneNotSupportedException e) {
			// not going to happen
		}
		return t;
	}

	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) {
		PlaceholderKind kind = (firstBytes & ITypeMarshalBuffer.FLAG1) != 0 ? PlaceholderKind.DecltypeAuto
				: PlaceholderKind.Auto;
		return new CPPPlaceholderType(kind);
	}
}
