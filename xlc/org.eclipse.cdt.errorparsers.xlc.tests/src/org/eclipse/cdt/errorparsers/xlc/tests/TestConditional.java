/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.errorparsers.xlc.tests;

import org.eclipse.cdt.errorparsers.xlc.XlcErrorParser;

import junit.framework.TestCase;


public class TestConditional extends TestCase {
	String err_msg;
	/**
	 * This function tests parseLine function of the
	 * XlcErrorParser class. Informative message generated 
	 * by the xlc compiler is given as input for testing.  
	 */
	public void testparseLine()
	{
		XlcErrorParser aix = new XlcErrorParser();
		aix.parseLine(err_msg);
		assertEquals("temp8.c", aix.getFileName());
		assertEquals(12, aix.getLineNumber());
		assertEquals("I", aix.getSeverity());
		assertEquals(" The then branch of conditional is an empty statement.",aix.getMessage());
	}
	public TestConditional( String name)
	{
		super(name);
		err_msg = "\"temp8.c\", line 12.9: 1506-478 (I) " + 
				"The then branch of conditional is an empty statement.";
	}
}