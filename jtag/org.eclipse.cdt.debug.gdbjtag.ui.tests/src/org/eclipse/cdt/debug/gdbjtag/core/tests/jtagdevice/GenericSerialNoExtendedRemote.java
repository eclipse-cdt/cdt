/*******************************************************************************
 * Copyright (c) 2018, 2022 Kichwa Coders Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core.tests.jtagdevice;

import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConnection2;

public class GenericSerialNoExtendedRemote extends GenericSerialNoExtendedRemoteInfo implements IGDBJtagConnection2 {
	@Override
	public boolean getSupportsExtendedRemote() {
		return false;
	}
}
