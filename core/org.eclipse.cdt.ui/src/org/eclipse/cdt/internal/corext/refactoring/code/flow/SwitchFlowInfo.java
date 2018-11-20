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

class SwitchFlowInfo extends FlowInfo {
	private GenericConditionalFlowInfo fCases;
	private boolean fHasNullCaseInfo;

	public SwitchFlowInfo() {
		fCases = new GenericConditionalFlowInfo();
	}

	public void mergeTest(FlowInfo info, FlowContext context) {
		if (info == null)
			return;
		mergeSequential(info, context);
	}

	public void mergeCase(FlowInfo info, FlowContext context) {
		if (info == null) {
			fHasNullCaseInfo = true;
			return;
		}
		fCases.mergeConditional(info, context);
	}

	public void mergeDefault(boolean defaultCaseExists, FlowContext context) {
		if (!defaultCaseExists || fHasNullCaseInfo)
			fCases.mergeEmptyCondition(context);
		mergeSequential(fCases, context);
	}
}
