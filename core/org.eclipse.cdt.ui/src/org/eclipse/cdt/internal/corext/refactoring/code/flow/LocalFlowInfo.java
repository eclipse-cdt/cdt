/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.code.flow;

import org.eclipse.cdt.core.dom.ast.IVariable;

class LocalFlowInfo extends FlowInfo {
	private final int fVariableIndex;

	public LocalFlowInfo(IVariable binding, int localAccessMode, FlowContext context) {
		super(NO_RETURN);
		fVariableIndex= context.getIndexFromLocal(binding);
		if (fVariableIndex < 0)
			throw new IllegalStateException("Invalid local variable \"" + binding.getName() + "\" for the context."); //$NON-NLS-1$ //$NON-NLS-2$
		if (context.considerAccessMode()) {
			createAccessModeArray(context);
			context.manageLocal(binding);
			fAccessModes[fVariableIndex]= localAccessMode;
		}
	}

	public LocalFlowInfo(LocalFlowInfo info, int localAccessMode, FlowContext context) {
		super(NO_RETURN);
		fVariableIndex= info.fVariableIndex;
		if (context.considerAccessMode()) {
			createAccessModeArray(context);
			fAccessModes[fVariableIndex]= localAccessMode;
		}
	}

	public void setWriteAccess(FlowContext context) {
		if (context.considerAccessMode()) {
			fAccessModes[fVariableIndex]= FlowInfo.WRITE;
		}
	}
}

