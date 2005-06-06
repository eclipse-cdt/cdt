/**********************************************************************
 * Copyright (c) 2005 Symbian Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Symbian Ltd - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ManagedOptionValueHandler;

public class TestValueHandler extends ManagedOptionValueHandler implements
		IManagedOptionValueHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler#handleValue(IConfiguration,IToolChain,IOption,String,int)
	 */
	public boolean handleValue(IBuildObject configuration, 
                   IHoldsOptions holder, 
                   IOption option,
                   String extraArgument, int event)
	{
		// The event was not handled, thus return false
		return false;
	}	
}
