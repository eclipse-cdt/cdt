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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeductionGuide;

/**
 * Implementation for deduction guide.
 */
public class CPPASTDeductionGuide extends CPPASTFunctionDeclarator implements ICPPASTDeductionGuide {
	public CPPASTDeductionGuide() {
	}

	@Override
	public CPPASTDeductionGuide copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTDeductionGuide copy(CopyStyle style) {
		CPPASTDeductionGuide copy = new CPPASTDeductionGuide();
		return super.copy(copy, style);
	}
}
