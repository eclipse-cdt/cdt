/*******************************************************************************
 * Copyright (c) 2010, 2013 Nokia Siemens Networks Oyj, Finland.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.tests;
import junit.framework.TestCase;

/**
 * Automated testing with JUnit.
 * 
 * @author Petri Tuononen
 *
 */
public class JUnit_tests extends TestCase {

	public JUnit_tests(String name) {
		super(name);
	}

	@Override
	protected void setUp() { }

	@Override
	protected void tearDown() { }

	public static void testBooleanTrue() {
		assertTrue(true);
	}

	public static void testBooleanFalse() {
		assertTrue(false);
	}

}