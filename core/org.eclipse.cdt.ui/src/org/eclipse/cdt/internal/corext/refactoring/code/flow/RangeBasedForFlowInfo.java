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

class RangeBasedForFlowInfo extends FlowInfo {

	public void mergeDeclaration(FlowInfo info, FlowContext context) {
		if (info == null)
			return;
		mergeAccessModeSequential(info, context);
	}

	public void mergeInitializerClause(FlowInfo info, FlowContext context) {
		if (info == null)
			return;
		mergeAccessModeSequential(info, context);
	}

	public void mergeAction(FlowInfo info, FlowContext context) {
		if (info == null)
			return;

		info.mergeEmptyCondition(context);
		mergeSequential(info, context);
	}
}

