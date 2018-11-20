/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.xlc.ui.properties;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;

/**
	This class decides whether the make shared object
	option is visible,enabled or used in command line or not */

public class HiddenOptionApplicabilityCalculator implements IOptionApplicability {

	/**
	 * This function decides if the option for which this class
	 * is option applicability calculator is enabled or not.
	 */
	@Override
	public boolean isOptionEnabled(IBuildObject configuration, IHoldsOptions holder, IOption option) {
		return false;
	}

	/**
	 * This function decides if the option for which this class
	 * is option applicability calculator is used in command line
	 * or not.
	 */

	@Override
	public boolean isOptionUsedInCommandLine(IBuildObject configuration, IHoldsOptions holder, IOption option) {
		return true;
	}

	/**
	 * This function decides if the option for which this class
	 * is option applicability calculator is visible or not.
	 */

	@Override
	public boolean isOptionVisible(IBuildObject configuration, IHoldsOptions holder, IOption option) {

		return false;
	}

}
