/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;

/**
 * Default RSE System View property tester to support org.eclipse.ui.menu extension
 * point contributions.
 */
public class SystemViewPropertyTester extends PropertyTester {

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		// The receiver must be the real object and not the adapter itself. Otherwise
		// most of the property testings will fail and will evaluate to false.
		//
		// Therefor, do not use the property tester within an <adapt> expression
		// where the target adapter type is ISystemViewElementAdapter.
		//
		//    Example where the testing will fail:
		//       ...
		//          <adapt type="org.eclipse.rse.ui.view.ISystemViewElementAdapter">
		//             <test property=org.eclipse.rse.ui.systemTypeId" value="org.eclipse.rse.systemtype.local"/>
		//          </adapt>
		//       ...
		//
		assert !(receiver instanceof AbstractSystemViewAdapter);
		
		ISystemViewElementAdapter adapter = null;
		// Try to adapt the receiver to an ISystemViewElement adapter.
		if (receiver instanceof IAdaptable) {
			// Use IAdaptable#getAdapter(...) instead of Platform.getAdapterManager().getAdapter(...) to
			// give element contributors the chance to provide custom adapter implementations even if there
			// is an adapter factory registered providing element adaptation to ISystemViewElementAdapter.
			// This way we can take away a lot of pain from contributors otherwise struggeling with adapter factories.
			adapter = (ISystemViewElementAdapter)((IAdaptable)receiver).getAdapter(ISystemViewElementAdapter.class);
		} else {
			// Fallback to the adapter manager
			adapter = (ISystemViewElementAdapter)Platform.getAdapterManager().getAdapter(receiver, ISystemViewElementAdapter.class);
		}

		// If we succeeded to adapt to ISystemViewElementAdapter and the expected value is
		// of string type (IActionFilter#test(...) supports string testing only, we can
		// forward the property test to the original IActionFilter test implementation.
		if (adapter != null && expectedValue instanceof String) {
			return adapter.testAttribute(receiver, property, (String)expectedValue);
		}

		// Return false in any case the adaptation fails or if the expected
		// value is not of String type.
		return false;
	}
}