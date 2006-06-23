/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model.tests;

import java.io.FileNotFoundException;
import java.util.Stack;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.ExpectedStrings;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Peter Graves
 * 
 * This file contains a set of generic tests for the core C model's
 * TranslationUnit class. There is nothing exotic here, mostly just sanity type
 * tests
 * 
 */
public class TranslationUnitTests extends TranslationUnitBaseTest {
	/*
	 * This is a list of elements in the test .c file. It will be used in a
	 * number of places in the tests
	 */
	String[] expectedStringList = { "stdio.h", "unistd.h", "func2p",
			"globalvar", "myenum", "mystruct", "mystruct_t", "myunion",
			"mytype", "func1", "func2", "main", "func3" };

	int[] expectedLines = { 12, 14, 17, 20, 23, 28, 32, 35, 42, 47, 53, 58, 65,
			70 };

	/*
	 * This is a list of that the types of the above list of elements is
	 * expected to be.
	 */
	int[] expectedTypes = { ICElement.C_INCLUDE, ICElement.C_INCLUDE,
			ICElement.C_FUNCTION_DECLARATION, ICElement.C_VARIABLE,
			ICElement.C_ENUMERATION, ICElement.C_STRUCT, ICElement.C_TYPEDEF,
			ICElement.C_UNION, ICElement.C_TYPEDEF, ICElement.C_FUNCTION,
			ICElement.C_FUNCTION, ICElement.C_FUNCTION, ICElement.C_FUNCTION,
			ICElement.C_FUNCTION };

	/**
	 * Constructor for TranslationUnitTests
	 * 
	 * @param name
	 */
	public TranslationUnitTests(String name) {
		super(name);
	}

	public static TestSuite suite() {
		TestSuite suite = new TestSuite(TranslationUnitTests.class.getName());
		suite.addTest(new TranslationUnitTests("testIsTranslationUnit"));
		suite.addTest(new TranslationUnitTests("testGetChildren"));
		suite.addTest(new TranslationUnitTests("testGetElement"));
		suite.addTest(new TranslationUnitTests("testBug23478A"));
		suite.addTest(new TranslationUnitTests("testBug23478B"));
		suite.addTest(new TranslationUnitTests("testIsValidSourceUnitName"));
		// TODO: suite.addTest(new
		// TranslationUnitTests("testGetElementAtLine"));
		return suite;
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	/***************************************************************************
	 * Simple sanity test to make sure TranslationUnit.isTranslationUnit returns
	 * true
	 * 
	 */
	public void testIsTranslationUnit() throws CoreException,
			FileNotFoundException {
		ITranslationUnit myTranslationUnit;

		myTranslationUnit = CProjectHelper.findTranslationUnit(testProject,
				"exetest.c");
		assertTrue("A TranslationUnit", myTranslationUnit != null);

	}

	/***************************************************************************
	 * Simple sanity tests to make sure TranslationUnit.getChildren seems to
	 * basicly work
	 */
	public void testGetChildren() throws CModelException {
		ITranslationUnit myTranslationUnit;
		ICElement[] elements;
		int x;

		ExpectedStrings expectedString = new ExpectedStrings(expectedStringList);

		myTranslationUnit = CProjectHelper.findTranslationUnit(testProject,
				"exetest.c");

		if (myTranslationUnit.hasChildren()) {
			elements = myTranslationUnit.getChildren();
			for (x = 0; x < elements.length; x++) {
				expectedString.foundString(elements[x].getElementName());
			}
		}
		assertTrue("PR:23603 " + expectedString.getMissingString(),
				expectedString.gotAll());
		assertTrue(expectedString.getExtraString(), !expectedString.gotExtra());

	}

	/***************************************************************************
	 * Simple sanity tests for the getElement() call
	 */
	public void testGetElement() throws CModelException {
		ITranslationUnit myTranslationUnit;
		ICElement myElement;
		Stack missing = new Stack();
		int x;
		myTranslationUnit = CProjectHelper.findTranslationUnit(testProject,
				"exetest.c");

		for (x = 0; x < expectedStringList.length; x++) {
			myElement = myTranslationUnit.getElement(expectedStringList[x]);
			if (myElement == null)
				missing.push(expectedStringList[x]);
			else {
				assertTrue("Expected:" + expectedStringList[x] + " Got:"
						+ myElement.getElementName(), expectedStringList[x]
						.equals(myElement.getElementName()));
			}

		}
		if (!missing.empty()) {
			String output = new String("PR:23603 Could not get elements: ");
			while (!missing.empty())
				output += missing.pop() + " ";
			assertTrue(output, false);
		}

	}

	/***************************************************************************
	 * Simple sanity tests for the getInclude call
	 */
	public void testBug23478A() throws CModelException {
		IInclude myInclude;
		int x;
		String includes[] = { "stdio.h", "unistd.h" };
		ITranslationUnit myTranslationUnit = CProjectHelper
				.findTranslationUnit(testProject, "exetest.c");

		for (x = 0; x < includes.length; x++) {
			myInclude = myTranslationUnit.getInclude(includes[x]);
			if (myInclude == null)
				fail("Unable to get include: " + includes[x]);
			else {
				// Failed test: Include.getIncludeName() always returns "";
				// assertTrue
				assertTrue("PR:23478 Expected:" + new String("") + " Got:"
						+ myInclude.getIncludeName(), includes[x]
						.equals(myInclude.getIncludeName()));
			}
		}

	}

	/***************************************************************************
	 * Simple sanity tests for the getIncludes call
	 */
	public void testBug23478B() throws CModelException {
		IInclude myIncludes[];
		String includes[] = { "stdio.h", "unistd.h" };
		ExpectedStrings myExp = new ExpectedStrings(includes);
		int x;
		ITranslationUnit myTranslationUnit = CProjectHelper
				.findTranslationUnit(testProject, "exetest.c");

		myIncludes = myTranslationUnit.getIncludes();
		for (x = 0; x < myIncludes.length; x++) {
			myExp.foundString(myIncludes[x].getIncludeName());
		}
		// Failed test: Include.getIncludeName() always returns "";
		// assertTrue
		assertTrue(myExp.getMissingString(), myExp.gotAll());
		assertTrue(myExp.getExtraString(), !myExp.gotExtra());
	}

	/***************************************************************************
	 * Simple sanity tests for the getElementAtLine() call
	 */
	public void testGetElementAtLine() throws CoreException {
		ITranslationUnit myTranslationUnit;
		ICElement myElement;
		Stack missing = new Stack();
		int x;
		myTranslationUnit = CProjectHelper.findTranslationUnit(testProject,
				"exetest.c");

		for (x = 0; x < expectedStringList.length; x++) {
			myElement = myTranslationUnit.getElementAtLine(expectedLines[x]);
			if (myElement == null)
				missing.push(expectedStringList[x]);
			else {
				if (expectedStringList[x].equals("mystruct_t")) {
					assertTrue("PR:23603 expected:" + expectedStringList[x]
							+ " Got:" + myElement.getElementName(),
							expectedStringList[x].equals(myElement
									.getElementName()));
				} else {
					assertTrue("Expected:" + expectedStringList[x] + " Got:"
							+ myElement.getElementName(), expectedStringList[x]
							.equals(myElement.getElementName()));
				}

			}

		}
		if (!missing.empty()) {
			String output = new String("PR: 23603 Could not get elements: ");
			while (!missing.empty())
				output += missing.pop() + " ";
			assertTrue(output, false);
		}
	}

	public void testIsValidSourceUnitName() {
		assertTrue(CoreModel.isValidSourceUnitName(testProject.getProject(), "test.c"));
		assertFalse(CoreModel.isValidSourceUnitName(testProject.getProject(), "test.h"));
		assertTrue(CoreModel.isValidSourceUnitName(testProject.getProject(), "test.cc"));
		assertFalse(CoreModel.isValidSourceUnitName(testProject.getProject(), "test.hh"));
	}
}
