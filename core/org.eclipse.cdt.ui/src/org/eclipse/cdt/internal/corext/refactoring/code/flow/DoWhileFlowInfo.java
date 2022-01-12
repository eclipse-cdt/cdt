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

class DoWhileFlowInfo extends FlowInfo {
	private boolean fActionBranches;

	public void mergeAction(FlowInfo info, FlowContext context) {
		if (info == null)
			return;

		fActionBranches = info.branches();

		assign(info);

		if (fActionBranches && fReturnKind == VALUE_RETURN) {
			fReturnKind = PARTIAL_RETURN;
		}
	}

	public void mergeCondition(FlowInfo info, FlowContext context) {
		if (fActionBranches || fReturnKind == VALUE_RETURN || fReturnKind == VOID_RETURN || info == null)
			return;
		mergeAccessModeSequential(info, context);
	}
}
