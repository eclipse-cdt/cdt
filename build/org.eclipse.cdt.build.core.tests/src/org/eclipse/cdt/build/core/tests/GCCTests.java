/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.core.tests;

import java.io.IOException;

import org.junit.Test;

public class GCCTests {

	@Test
	public void tryGCCDiscovery() throws IOException {
		long start = System.currentTimeMillis();
		System.out.println("Time: " + (System.currentTimeMillis() - start));
	}

}
