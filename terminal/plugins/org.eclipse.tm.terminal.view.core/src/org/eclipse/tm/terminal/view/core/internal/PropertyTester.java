/*******************************************************************************
 * Copyright (c) 2011 - 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.core.internal;

import org.eclipse.tm.terminal.view.core.TerminalContextPropertiesProviderFactory;

/**
 * Property tester implementation.
 */
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

		// "hasContextPropertiesProvider": Checks if a context properties provider is available for the given receiver.
		if ("hasContextPropertiesProvider".equals(property)) { //$NON-NLS-1$
			boolean hasProvider = TerminalContextPropertiesProviderFactory.getProvider(receiver) != null;
			return expectedValue instanceof Boolean ? ((Boolean) expectedValue).equals(Boolean.valueOf(hasProvider))
					: hasProvider;
		}

		return false;
	}

}
