/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.tests;

import static org.junit.Assert.assertNotEquals;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.junit.Test;

public class BoardManagerTests {

	@Test
	public void loadPackagesTest() throws Exception {
		assertNotEquals(0, Activator.getService(ArduinoManager.class).getInstalledPlatforms().size());
	}

}
