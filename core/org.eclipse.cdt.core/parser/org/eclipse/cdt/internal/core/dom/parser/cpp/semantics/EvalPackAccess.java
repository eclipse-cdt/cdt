/*******************************************************************************
* Copyright (c) 2016,2022 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - factor out EvalPackAccess
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.core.runtime.CoreException;

public class EvalPackAccess extends EvalCompositeAccess {

	public EvalPackAccess(ICPPEvaluation parent, int elementId) {
		super(parent, elementId);
	}

	@Override
	public IType getType() {
		IType type = getParent().getType();
		type = SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);

		return type;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EVAL_PACK_ACCESS);
		buffer.marshalEvaluation(getParent(), includeValue);
		buffer.putInt(getElementId());
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPEvaluation parent = buffer.unmarshalEvaluation();
		int elementId = buffer.getInt();
		return new EvalPackAccess(parent, elementId);
	}
}
