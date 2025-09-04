/*******************************************************************************
 * Copyright (c) 2025 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;

public class DeductionGuideTemplate extends FunctionTemplateDeclaration {
	public DeductionGuideTemplate(ICElement parent, String name) {
		super(parent, name, ICElement.C_DEDUCTION_GUIDE_TEMPLATE);
	}
}
