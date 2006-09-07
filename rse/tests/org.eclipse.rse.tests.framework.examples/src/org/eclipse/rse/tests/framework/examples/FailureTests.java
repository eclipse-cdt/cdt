/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.framework.examples;

public class FailureTests extends AbstractTest {

	public void testFailure01() {
		sleep(50);
		fail("generic failure 01");
	}
	
	public void testFailure02() {
		sleep(50);
		fail("generic failure 02");
	}
	
	public void testFailure03() {
		sleep(50);
		fail("generic failure 03");
	}
	
}


