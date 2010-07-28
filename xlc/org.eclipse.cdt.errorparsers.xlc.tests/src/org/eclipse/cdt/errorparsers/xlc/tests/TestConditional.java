/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.errorparsers.xlc.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.core.IMarkerGenerator;


public class TestConditional extends TestCase {
	String err_msg;
	/**
	 * This function tests parseLine function of the
	 * XlcErrorParser class. Informative message generated 
	 * by the xlc compiler is given as input for testing.  
	 */
	public void testparseLine()
	{
		XlcErrorParserTester aix = new XlcErrorParserTester();
		aix.parseLine(err_msg);
		assertEquals("temp8.c", aix.getFileName(0));
		assertEquals(12, aix.getLineNumber(0));
		assertEquals(IMarkerGenerator.SEVERITY_INFO, aix.getSeverity(0));
		assertEquals("The then branch of conditional is an empty statement.",aix.getMessage(0));
	}
	public TestConditional( String name)
	{
		super(name);
		err_msg = "\"temp8.c\", line 12.9: 1506-478 (I) " + 
				"The then branch of conditional is an empty statement.";
	}
}