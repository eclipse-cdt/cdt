/*******************************************************************************
 * Copyright (c) 2023 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeductionGuide;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

public class PDOMCPPDeductionGuide extends PDOMCPPFunction implements ICPPDeductionGuide {
	public PDOMCPPDeductionGuide(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	public PDOMCPPDeductionGuide(PDOMCPPLinkage linkage, PDOMNode parent, ICPPDeductionGuide guide)
			throws CoreException, DOMException {
		super(linkage, parent, guide.getFunctionBinding(), true);
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_DEDUCTION_GUIDE;
	}

	@Override
	public ICPPFunction getFunctionBinding() {
		return this;
	}
}
