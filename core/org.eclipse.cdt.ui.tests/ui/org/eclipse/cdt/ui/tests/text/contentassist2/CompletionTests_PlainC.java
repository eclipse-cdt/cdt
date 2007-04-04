/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.text.contentassist2;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase;

/**
 * Completion tests for plain C.
 * 
 * @since 4.0
 */
public class CompletionTests_PlainC extends AbstractContentAssistTest {

	private static final String HEADER_FILE_NAME = "CompletionTest.h";
	private static final String SOURCE_FILE_NAME = "CompletionTest.c";
	private static final String CURSOR_LOCATION_TAG = "/*cursor*/";
	
	protected int fCursorOffset;

	//{CompletionTest.h}
	//int gGlobalInt;

	public static Test suite() {
		return BaseTestCase.suite(CompletionTests_PlainC.class, "_");
	}
	
	/**
	 * @param name
	 */
	public CompletionTests_PlainC(String name) {
		super(name);
	}

	/*
	 * @see org.eclipse.cdt.ui.tests.text.contentassist2.AbstractContentAssistTest#setUpProjectContent(org.eclipse.core.resources.IProject)
	 */
	protected IFile setUpProjectContent(IProject project) throws Exception {
		String headerContent= readTaggedComment(HEADER_FILE_NAME);
		StringBuffer sourceContent= getContentsForTest(1)[0];
		sourceContent.insert(0, "#include \""+HEADER_FILE_NAME+"\"\n");
		fCursorOffset= sourceContent.indexOf(CURSOR_LOCATION_TAG);
		assertTrue("No cursor location specified", fCursorOffset >= 0);
		sourceContent.delete(fCursorOffset, fCursorOffset+CURSOR_LOCATION_TAG.length());
		assertNotNull(createFile(project, HEADER_FILE_NAME, headerContent));
		return createFile(project, SOURCE_FILE_NAME, sourceContent.toString());
	}

	protected void assertCompletionResults(String[] expected) throws Exception {
		assertContentAssistResults(fCursorOffset, expected, true, AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//void test() {
    //  int myvar;
    //  (my/*cursor*/
	public void testLocalVariableAfterOpeningParen_Bug180885() throws Exception {
		final String[] expected= {
				"myvar"
		};
		assertCompletionResults(expected);
	}

	//void test() {
    //  int myvar;
    //  int x = my/*cursor*/
	public void testLocalVariableInAssignment() throws Exception {
		final String[] expected= {
				"myvar"
		};
		assertCompletionResults(expected);
	}

	//void test() {
    //  int myvar;
    //  my/*cursor*/
	public void testLocalVariableOnLHS() throws Exception {
		final String[] expected= {
				"myvar"
		};
		assertCompletionResults(expected);
	}

}
