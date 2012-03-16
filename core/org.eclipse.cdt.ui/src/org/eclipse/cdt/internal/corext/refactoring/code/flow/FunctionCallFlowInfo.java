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

class FunctionCallFlowInfo extends FlowInfo {

	public FunctionCallFlowInfo() {
		super(NO_RETURN);
	}

	public void mergeArgument(FlowInfo info, FlowContext context) {
		if (info == null)
			return;
		mergeSequential(info, context);
	}

	public void mergeReceiver(FlowInfo info, FlowContext context) {
		if (info == null)
			return;
		mergeSequential(info, context);
	}
}
