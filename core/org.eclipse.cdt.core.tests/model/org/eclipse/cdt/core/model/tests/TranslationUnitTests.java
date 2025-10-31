/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.ExpectedStrings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Peter Graves
 *
 * This file contains a set of generic tests for the core C model's TranslationUnit class.
 * There is nothing exotic here, mostly just sanity type tests.
 */
public class TranslationUnitTests extends TranslationUnitBaseTest {

	/*
	 * This is a list of elements in the test .c file. It will be used in a
	 * number of places in the tests
	 */
	private static final String[] expectedStringList = { "stdio.h", "unistd.h", "func2p", "globalvar", "myenum",
			"mystruct_t", "mystruct", "myunion", "mytype", "func1", "func2", "main", "func3" };

	/***************************************************************************
	 * Simple sanity test to make sure TranslationUnit.isTranslationUnit returns true
	 */
	@Test
	public void testIsTranslationUnit() throws Exception, FileNotFoundException {
		ITranslationUnit tu = CProjectHelper.findTranslationUnit(testProject, "exetest.c");
		Assertions.assertNotNull(tu);
	}

	/***************************************************************************
	 * Simple sanity tests to make sure TranslationUnit.getChildren seems to basically work.
	 */
	@Test
	public void testGetChildren() throws Exception {
		ExpectedStrings expectedString = new ExpectedStrings(expectedStringList);

		ITranslationUnit tu = CProjectHelper.findTranslationUnit(testProject, "exetest.c");

		if (tu.hasChildren()) {
			ICElement[] elements = tu.getChildren();
			for (int x = 0; x < elements.length; x++) {
				expectedString.foundString(elements[x].getElementName());
			}
		}
		Assertions.assertTrue(expectedString.gotAll(), "PR:23603 " + expectedString.getMissingString());
		Assertions.assertTrue(!expectedString.gotExtra(), expectedString.getExtraString());
	}

	/***************************************************************************
	 * Simple sanity tests for the getElement() call
	 */
	@Test
	public void testGetElement() throws Exception {
		Deque<String> missing = new ArrayDeque<>();
		ITranslationUnit tu = CProjectHelper.findTranslationUnit(testProject, "exetest.c");

		for (int x = 0; x < expectedStringList.length; x++) {
			ICElement myElement = tu.getElement(expectedStringList[x]);
			if (myElement == null) {
				missing.push(expectedStringList[x]);
			} else {
				Assertions.assertTrue(expectedStringList[x].equals(myElement.getElementName()),
						"Expected: \"" + expectedStringList[x] + "\". Got:" + myElement.getElementName());
			}
		}
		if (!missing.isEmpty()) {
			StringBuilder output = new StringBuilder("PR:23603 Could not get elements:");
			while (!missing.isEmpty()) {
				output.append(" ").append(missing.pop());
			}
			Assertions.assertTrue(false, output.toString());
		}
	}

	/***************************************************************************
	 * Simple sanity tests for the getInclude call
	 */
	@Test
	public void testBug23478A() throws Exception {
		String includes[] = { "stdio.h", "unistd.h" };
		ITranslationUnit tu = CProjectHelper.findTranslationUnit(testProject, "exetest.c");

		for (int x = 0; x < includes.length; x++) {
			IInclude include = tu.getInclude(includes[x]);
			if (include == null) {
				Assertions.fail("Unable to get include: " + includes[x]);
			} else {
				// Failed test: Include.getIncludeName() always returns "";
				// assertTrue
				Assertions.assertTrue(includes[x].equals(include.getIncludeName()),
						"PR:23478 Expected: an empty string. Got: " + include.getIncludeName());
			}
		}
	}

	/***************************************************************************
	 * Simple sanity tests for the getIncludes call
	 */
	@Test
	public void testBug23478B() throws Exception {
		String headers[] = { "stdio.h", "unistd.h" };
		ExpectedStrings myExp = new ExpectedStrings(headers);
		ITranslationUnit tu = CProjectHelper.findTranslationUnit(testProject, "exetest.c");

		IInclude[] includes = tu.getIncludes();
		for (int x = 0; x < includes.length; x++) {
			myExp.foundString(includes[x].getIncludeName());
		}
		// Failed test: Include.getIncludeName() always returns "";
		// assertTrue
		Assertions.assertTrue(myExp.gotAll(), myExp.getMissingString());
		Assertions.assertTrue(!myExp.gotExtra(), myExp.getExtraString());
	}

	/***************************************************************************
	 * Simple sanity tests for the getElementAtLine() call.
	 */
	// This test is disabled due to consistent failure.
	//	public void testGetElementAtLine() throws Exception {
	//		Deque<String> missing = new ArrayDeque<String>();
	//		ITranslationUnit tu = CProjectHelper.findTranslationUnit(testProject, "exetest.c");
	//
	//		for (int x = 0; x < expectedStringList.length; x++) {
	//			ICElement element = tu.getElementAtLine(expectedLines[x]);
	//			if (element == null) {
	//				missing.push(expectedStringList[x]);
	//			} else {
	//				if (expectedStringList[x].equals("mystruct_t")) {
	//					assertTrue("PR:23603 expected: " + expectedStringList[x]
	//							+ ". Got: " + element.getElementName(),
	//							expectedStringList[x].equals(element.getElementName()));
	//				} else {
	//					assertTrue("Expected: " + expectedStringList[x]
	//							+ ". Got: " + element.getElementName(),
	//							expectedStringList[x].equals(element.getElementName()));
	//				}
	//			}
	//		}
	//		if (!missing.isEmpty()) {
	//			StringBuilder output = new StringBuilder("PR:23603 Could not get elements:");
	//			while (!missing.isEmpty()) {
	//				output.append(" ").append(missing.pop());
	//			}
	//			assertTrue(output.toString(), false);
	//		}
	//	}

	@Test
	public void testIsValidSourceUnitName() {
		Assertions.assertTrue(CoreModel.isValidSourceUnitName(testProject.getProject(), "test.c"));
		Assertions.assertFalse(CoreModel.isValidSourceUnitName(testProject.getProject(), "test.h"));
		Assertions.assertTrue(CoreModel.isValidSourceUnitName(testProject.getProject(), "test.cc"));
		Assertions.assertFalse(CoreModel.isValidSourceUnitName(testProject.getProject(), "test.hh"));
	}

	// This test is disabled because it fails consistently due to a collision between content types
	// "asmSource" defined in org.eclipse.cdt.core and
	// "org.eclipse.cdt.managedbuilder.llvm.ui.llvmAssemblyCode" defined in
	// org.eclipse.cdt.managedbuilder.llvm.ui.
	//	public void testAssemblyContentType_Bug186774() {
	//		assertEquals(CCorePlugin.CONTENT_TYPE_ASMSOURCE, CoreModel.getRegistedContentTypeId(testProject.getProject(), "test.s"));
	//		assertEquals(CCorePlugin.CONTENT_TYPE_ASMSOURCE, CoreModel.getRegistedContentTypeId(testProject.getProject(), "test.S"));
	//	}
}
