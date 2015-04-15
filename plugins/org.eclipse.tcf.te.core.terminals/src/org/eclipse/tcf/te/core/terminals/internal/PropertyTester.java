/*******************************************************************************
 * Copyright (c) 2011, 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.core.terminals.internal;

import org.eclipse.tcf.te.core.terminals.TerminalContextPropertiesProviderFactory;



/**
 * Property tester implementation.
 */
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

		// "hasContextPropertiesProvider": Checks if a context properties provider is available for the given receiver.
		if ("hasContextPropertiesProvider".equals(property)) { //$NON-NLS-1$
			boolean hasProvider = TerminalContextPropertiesProviderFactory.getProvider(receiver) != null;
			return expectedValue instanceof Boolean ? ((Boolean)expectedValue).equals(Boolean.valueOf(hasProvider)) : hasProvider;
		}

		return false;
	}

}
