/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Symbian - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;

/**
 * Tests which are too expensive to run as part of normal testing, but
 * should be run after B-tree related development.
 * <p>
 * The 'Full Checking' tests perform a full validation of the B-tree
 * invariants after each B-tree operation, and so are especially
 * expensive and cpu hungry.
 */
public class BTreeExpensiveTests extends BTreeTests {
	
	public static Test suite() {
		return suite(BTreeExpensiveTests.class);
	}
	
	public void testBySortedSetMirror() throws Exception {
		sortedMirrorTest(100);
	}
	
	// @Override
	@Override
	public void testInsertion() throws Exception {
		super.testInsertion();
	}
	
	/*
	 * N.B. Each of the following tests are quite expensive (i.e. > 10mins each on a 2Ghz machine)
	 */
	
	public void testBySortedSetMirror1682762087() throws Exception {
		System.out.println("1682762087 Full Checking");
		trial(1682762087, true); // exposed bugs in 2a,b
	}

	public void testBySortedSetMirror322922974() throws Exception {
		System.out.println("322922974 Full Checking");
		trial(322922974, true); // exposed bugs in 3b(ii)
	}

	public void testBySortedSetMirror_588448152() throws Exception {
		System.out.println("-588448152 Full Checking");
		trial(-588448152, true); // exposed root-delete-on-merge problems
	}
}
