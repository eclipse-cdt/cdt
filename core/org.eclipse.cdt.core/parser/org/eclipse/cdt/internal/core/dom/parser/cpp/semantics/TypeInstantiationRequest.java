/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTypeSpecialization;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Used to track ongoing instantiations as a safeguard against infinite recursion.
 */
public class TypeInstantiationRequest {
	private final IType type;
	private final ICPPTemplateParameterMap parameterMap;
	private final int packOffset;
	private final ICPPTypeSpecialization contextTypeSpecialization;
	private int hashCode;

	public TypeInstantiationRequest(IType type, InstantiationContext context) {
		this.type = type;
		this.parameterMap = context.getParameterMap();
		this.packOffset = context.getPackOffset();
		this.contextTypeSpecialization = context.getContextTypeSpecialization();
	}

	@Override
	public int hashCode() {
		if (hashCode == 0) {
			SignatureBuilder builder = new SignatureBuilder();
			try {
				builder.marshalType(type);
				char[] signature = builder.getSignature();
				hashCode = CharArrayUtils.hash(signature);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				hashCode = Integer.MIN_VALUE;
			}
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!getClass().equals(obj.getClass()))
			return false;
		TypeInstantiationRequest other = (TypeInstantiationRequest) obj;
		if (!type.isSameType(other.type))
			return false;
		if (!equals(contextTypeSpecialization, other.contextTypeSpecialization))
			return false;
		if (!equals(parameterMap, other.parameterMap))
			return false;
		if (packOffset != other.packOffset)
			return false;
		return true;
	}

	private boolean equals(IType type1, IType type2) {
		if (type1 == type2)
			return true;
		if (type1 == null || type2 == null)
			return false;
		return type1.isSameType(type2);
	}

	private boolean equals(ICPPTemplateParameterMap map1, ICPPTemplateParameterMap map2) {
		if (map1 == map2)
			return true;
		if (map1 == null || map2 == null)
			return false;
		Integer[] p1 = map1.getAllParameterPositions();
		Integer[] p2 = map2.getAllParameterPositions();
		if (!Arrays.equals(p1, p2))
			return false;
		for (Integer paramId : p1) {
			ICPPTemplateArgument[] packExpansion1 = map1.getPackExpansion(paramId);
			ICPPTemplateArgument[] packExpansion2 = map2.getPackExpansion(paramId);
			if (packExpansion1 != null && packExpansion2 != null) {
				if (packExpansion1.length != packExpansion2.length)
					return false;
				for (int i = 0; i < packExpansion1.length; i++) {
					if (!equals(packExpansion1[i], packExpansion2[i]))
						return false;
				}
			} else if (packExpansion1 == null && packExpansion2 == null) {
				ICPPTemplateArgument arg1 = map1.getArgument(paramId);
				ICPPTemplateArgument arg2 = map2.getArgument(paramId);
				if (!equals(arg1, arg2))
					return false;
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean equals(ICPPTemplateArgument arg1, ICPPTemplateArgument arg2) {
		if (arg1 == arg2)
			return true;
		if (arg1 == null || arg2 == null)
			return false;
		return arg1.isSameValue(arg2);
	}
}
