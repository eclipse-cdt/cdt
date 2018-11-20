/*******************************************************************************
 * Copyright (c) 2018 STMicroelectronics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * STMicroelectronics
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.tests.properties;

import java.util.Arrays;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IOptionCategoryApplicability;

public class HideEmptyOptionCategoryApplicabilityCalculator implements IOptionCategoryApplicability {

	public HideEmptyOptionCategoryApplicabilityCalculator() {
	}

	@Override
	public boolean isOptionCategoryVisible(IBuildObject configuration, IHoldsOptions optHolder,
			IOptionCategory category) {
		// Check that the category contains at least one option
		return Arrays.asList(optHolder.getOptions()).stream().map((opt) -> opt.getCategory())
				.anyMatch((cat) -> cat != null && cat.equals(category));
	}
}
