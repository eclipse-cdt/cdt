/*******************************************************************************
 * Copyright (c) 2013, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.errorparsers.xlc.tests;

import org.eclipse.cdt.core.IMarkerGenerator;

import junit.framework.TestCase;

public class TestUnrecoverableError_2 extends TestCase {
	String err_msg;

	/**
	 * This function tests parseLine function of the
	 * XlcErrorParser class. A variant of error message generated by
	 * xlc compiler with unrecoverable severity (U) is given as
	 * input for testing.
	 */
	public void testparseLine() {
		XlcErrorParserTester aix = new XlcErrorParserTester();
		aix.parseLine(err_msg);
		assertEquals("", aix.getFileName(0));
		assertEquals(0, aix.getLineNumber(0));
		assertEquals(IMarkerGenerator.SEVERITY_ERROR_RESOURCE, aix.getSeverity(0));
		assertEquals("An error occurred during code generation.  The code generation return code was 1.",
				aix.getMessage(0));
	}

	public TestUnrecoverableError_2(String name) {
		super(name);
		err_msg = "1586-346 (U) An error occurred during code generation.  The code generation return code was 1.";
	}
}
