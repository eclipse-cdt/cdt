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
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeductionGuide;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPDeductionGuideTemplate extends CompositeCPPFunctionTemplate implements ICPPDeductionGuide {
	public CompositeCPPDeductionGuideTemplate(ICompositesFactory cf, ICPPDeductionGuide rbinding) {
		super(cf, rbinding.getFunctionBinding());
	}

	@Override
	public ICPPFunction getFunctionBinding() {
		return this;
	}
}
