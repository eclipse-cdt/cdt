/*******************************************************************************
 * Copyright (c) 2005 Texas Instruments Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Texas Instruments Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;

/**
 * 
 */
public class AlwaysOffApplicabilityCalculator implements IOptionApplicability {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionApplicability#isOptionUsedInCommandLine()
	 */
	public boolean isOptionUsedInCommandLine(
			IBuildObject config,
			IHoldsOptions holder,
			IOption option) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionApplicability#isOptionVisible()
	 */
	public boolean isOptionVisible(
			IBuildObject config,
			IHoldsOptions holder,
			IOption option) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionApplicability#isOptionEnabled()
	 */
	public boolean isOptionEnabled(
			IBuildObject config,
			IHoldsOptions holder,
			IOption option) {
		return false;
	}

}
