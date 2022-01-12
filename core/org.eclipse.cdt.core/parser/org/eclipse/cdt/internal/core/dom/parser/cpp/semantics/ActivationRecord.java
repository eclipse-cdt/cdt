/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;

/**
 * ActivationRecord keeps track of the values of parameters and local variables
 * during the evaluation of a function call.
 * */
public class ActivationRecord {
	private final Map<IBinding, ICPPEvaluation> vars = new HashMap<>();
	private final ICPPParameter[] params;
	private final ICPPEvaluation[] args;
	private final ICPPEvaluation implicitThis;

	public ActivationRecord(ICPPParameter[] params, ICPPEvaluation implicitThis) {
		this.params = params;
		this.args = new ICPPEvaluation[params.length];
		this.implicitThis = implicitThis;
	}

	public ActivationRecord() {
		this(new ICPPParameter[] {}, null);
	}

	public void update(IBinding binding, ICPPEvaluation value) {
		int paramPos = getParameterPosition(binding);
		if (paramPos == -1) {
			vars.put(binding, value);
		} else {
			args[paramPos] = value;
		}
	}

	public ICPPEvaluation getVariable(IBinding binding) {
		int paramPos = getParameterPosition(binding);
		if (paramPos == -1) {
			return vars.get(binding);
		} else {
			return args[paramPos];
		}
	}

	private int getParameterPosition(IBinding binding) {
		if (binding instanceof ICPPParameter) {
			for (int i = 0; i < params.length; i++) {
				ICPPParameter param = params[i];
				if (param.getName().equals(binding.getName())) {
					return i;
				}
			}
		}
		return -1;
	}

	public ICPPEvaluation getImplicitThis() {
		return implicitThis;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Local variables: [\n"); //$NON-NLS-1$
		int i = 0;
		for (Entry<IBinding, ICPPEvaluation> entry : vars.entrySet()) {
			builder.append("\t\t"); //$NON-NLS-1$
			builder.append(entry.getKey().toString());
			builder.append("="); //$NON-NLS-1$
			builder.append(entry.getValue().toString());
			if (i < vars.entrySet().size() - 1) {
				builder.append(", "); //$NON-NLS-1$
			}
			i++;
			builder.append("\n"); //$NON-NLS-1$
		}
		builder.append("]\n"); //$NON-NLS-1$

		builder.append("Implicit this: "); //$NON-NLS-1$
		if (implicitThis != null) {
			builder.append(implicitThis.toString());
		} else {
			builder.append("<null>"); //$NON-NLS-1$
		}
		builder.append("\n"); //$NON-NLS-1$
		return builder.toString();
	}
}