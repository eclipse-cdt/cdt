/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.code.flow;

import org.eclipse.cdt.core.dom.ast.IVariable;

class LocalFlowInfo extends FlowInfo {
	private final int fVariableIndex;

	public LocalFlowInfo(IVariable binding, int variableIndex, int localAccessMode, FlowContext context) {
		super(NO_RETURN);
		if (variableIndex < 0)
			throw new IllegalArgumentException("Invalid index for local variable \"" + binding.getName()); //$NON-NLS-1$
		fVariableIndex = variableIndex;
		if (context.considerAccessMode()) {
			createAccessModeArray(context);
			context.manageLocal(binding);
			fAccessModes[fVariableIndex] = localAccessMode;
		}
	}

	public LocalFlowInfo(LocalFlowInfo info, int localAccessMode, FlowContext context) {
		super(NO_RETURN);
		fVariableIndex = info.fVariableIndex;
		if (context.considerAccessMode()) {
			createAccessModeArray(context);
			fAccessModes[fVariableIndex] = localAccessMode;
		}
	}

	public void setWriteAccess(FlowContext context) {
		if (context.considerAccessMode()) {
			fAccessModes[fVariableIndex] = FlowInfo.WRITE;
		}
	}
}
