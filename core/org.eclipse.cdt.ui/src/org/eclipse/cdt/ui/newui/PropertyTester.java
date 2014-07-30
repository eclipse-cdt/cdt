/*******************************************************************************
 * Copyright (c) 2007, 2014 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

/**
 * @deprecated Doesn't do anything useful.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		return false;
	}
}
