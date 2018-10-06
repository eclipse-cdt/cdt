/*******************************************************************************
 * Copyright (c) 2018 STMicroelectronics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		return Arrays.asList(optHolder.getOptions())
			.stream()
			.map((opt) -> opt.getCategory())
			.anyMatch((cat) -> cat != null && cat.equals(category));
	}
}
