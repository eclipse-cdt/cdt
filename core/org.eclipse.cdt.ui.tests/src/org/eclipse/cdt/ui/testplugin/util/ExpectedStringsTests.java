/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.testplugin.util;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Peter Graves
 *
 *Some simple tests to make sure our ExtraStrings class seems to work.
 */
public class ExpectedStringsTests extends TestCase {

	/**
	 * Constructor for ExpectedStringsTests.
	 * @param name
	 */
	public ExpectedStringsTests(String name) {
		super(name);
	}
	    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     * 
     * Example code test the packages in the project 
     *  "com.qnx.tools.ide.cdt.core"
     */
    protected void setUp() {


    }
    
     /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    protected void tearDown() {
       // release resources here and clean-up
    }
    
    public static TestSuite suite() {
        return new TestSuite(ExpectedStringsTests.class);
    }
    
    public static void main (String[] args){
        junit.textui.TestRunner.run(suite());
    }

	public void testGotAll() {
		ExpectedStrings myExp;
		String[] strings= {"stringOne", "stringTwo", "stringThree" };
		
		myExp=new ExpectedStrings(strings);
		assertTrue("No found strings", !myExp.gotAll());
		myExp.foundString("stringOne");
		assertTrue("1 found strings", !myExp.gotAll());
		myExp.foundString("stringTwo");
		assertTrue("2 found strings", !myExp.gotAll());
		myExp.foundString("stringThree");
		assertTrue("All found strings", myExp.gotAll());
		
		
	}
	public void testGotExtra () {
		ExpectedStrings myExp;
		String[] strings= {"stringOne", "stringTwo", "stringThree" };
		
		myExp=new ExpectedStrings(strings);
		assertTrue("No found strings", !myExp.gotExtra());
		myExp.foundString("stringOne");
		assertTrue("1 found strings", !myExp.gotExtra());
		myExp.foundString("stringTwo");
		assertTrue("2 found strings", !myExp.gotExtra());
		myExp.foundString("stringThree");
		assertTrue("All found strings", !myExp.gotExtra());
		myExp.foundString("Somerandomestring");
		assertTrue("Extra String", myExp.gotExtra());
		
	}
	
	public void testGetMissingString()
	{
		ExpectedStrings myExp;
		String[] strings= {"stringOne", "stringTwo", "stringThree" };
		
		myExp=new ExpectedStrings(strings);
		assertNotNull(myExp.getMissingString());
		myExp.foundString("stringOne");
		assertNotNull(myExp.getMissingString());
		myExp.foundString("stringTwo");
		assertNotNull(myExp.getMissingString());
		myExp.foundString("stringThree");
		assertNotNull(myExp.getMissingString());

	}
	
	public void testGetExtraString()
	{
		ExpectedStrings myExp;
		String[] strings= {"stringOne", "stringTwo", "stringThree" };
		
		myExp=new ExpectedStrings(strings);
		assertNotNull(myExp.getExtraString());
		myExp.foundString("stringOnenot");
		assertNotNull(myExp.getMissingString());
		myExp.foundString("stringTwonot");
		assertNotNull(myExp.getMissingString());

	}



}
