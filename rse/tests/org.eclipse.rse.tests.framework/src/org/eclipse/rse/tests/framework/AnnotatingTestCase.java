/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.framework;

import java.io.PrintWriter;
import java.io.StringWriter;
import junit.framework.TestCase;

/**
 * AnnotatingTestCase extends TestCase by adding the capability to store remarks on the
 * test during its execution.  These remarks can be gathered by the environment that 
 * runs the test and presented to a user by whatever means is interesting in that environment.
 * For example, a view may choose to chose these in a pane immediately after running the 
 * testcase.
 */
public class AnnotatingTestCase extends TestCase {
	
	private PrintWriter out;
	private StringWriter base;
	
	public AnnotatingTestCase() {
		reset();
	}
	
	/**
	 * @param methodName The name of the method to run for this test.
	 * Usually "testSomething".
	 */
	public AnnotatingTestCase(String methodName) {
		super(methodName);
		reset();
	}
	
	protected void remark(String remark) {
		out.println(remark);
	}
	
	public String getAnnotations() {
		return base.toString();
	}
	
	public void reset() {
		base = new StringWriter(1000);
		out = new PrintWriter(base);
	}
	
}


