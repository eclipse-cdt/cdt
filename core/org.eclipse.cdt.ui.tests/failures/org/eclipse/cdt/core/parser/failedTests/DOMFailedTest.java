/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.failedTests;

import org.eclipse.cdt.core.parser.tests.BaseDOMTest;

/**
 * @author jcamelon
 */
public class DOMFailedTest extends BaseDOMTest  {

	public DOMFailedTest(String name) {
		super(name);
	}
	
	public void testBug36730(){
		failTest("FUNCTION_MACRO( 1, a );\n	int i;");
	}

	public void testBug37019(){
		failTest("static const A a( 1, 0 );");
	}
	
	public void testBug36932() {
		failTest("A::A( ) : var( new char [ (unsigned)bufSize ] ) {}");
	}
}
