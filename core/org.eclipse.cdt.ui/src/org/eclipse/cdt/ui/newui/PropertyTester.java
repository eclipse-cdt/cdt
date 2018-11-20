/*******************************************************************************
 * Copyright (c) 2007, 2018 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

/**
 * @deprecated Doesn't do anything useful.
 * @noextend This class is not intended to be subclassed by clients.
 */
@Deprecated
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		return false;
	}
}
