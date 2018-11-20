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

import java.util.HashSet;

import org.eclipse.cdt.core.dom.ast.IASTName;

class BranchFlowInfo extends FlowInfo {

	public BranchFlowInfo(IASTName label, FlowContext context) {
		super(NO_RETURN);
		fBranches = new HashSet<>(2);
		fBranches.add(makeString(label));
	}
}
