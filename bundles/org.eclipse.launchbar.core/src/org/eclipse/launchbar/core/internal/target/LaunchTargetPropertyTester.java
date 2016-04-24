/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.core.internal.target;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.launchbar.core.target.ILaunchTarget;

public class LaunchTargetPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof ILaunchTarget) {
			if (property.equals("launchTargetType")) { //$NON-NLS-1$
				return ((ILaunchTarget) receiver).getTypeId().equals(expectedValue);
			}
		}
		return false;
	}

}
