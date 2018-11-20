/*******************************************************************************
 * Copyright (c) 2005, 2011 Symbian Ltd and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Symbian Ltd - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ManagedOptionValueHandler;

public class TestValueHandler extends ManagedOptionValueHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler#handleValue(IConfiguration,IToolChain,IOption,String,int)
	 */
	@Override
	public boolean handleValue(IBuildObject configuration, IHoldsOptions holder, IOption option, String extraArgument,
			int event) {
		// The event was not handled, thus return false
		return false;
	}
}
