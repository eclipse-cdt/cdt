/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mohamed Azab (Mentor Graphics) - Initial implementation.
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import junit.framework.Test;

public class CParameterGuessingTests extends AbstractContentAssistTest {
	private static final String HEADER_FILE_NAME = "PGTest_C.h";
	private static final String SOURCE_FILE_NAME = "PGTest_C.c";

	//	{PGTest_C.h}
	//	typedef struct aStruct {
	//		int a;
	//		int b;
	//	} aStruct;
	//
	//	void ov1(int x, aStruct a);
	//	void ov2(int x, aStruct* aPtr);
	//	int funWith2ATypeObjectParams(aStruct a, aStruct b);

	public CParameterGuessingTests(String name) {
		super(name, false);
	}

	public static Test suite() {
		return BaseTestCase.suite(CParameterGuessingTests.class, "_");
	}

	@Override
	protected IFile setUpProjectContent(IProject project) throws Exception {
		String headerContent = readTaggedComment(HEADER_FILE_NAME);
		StringBuilder sourceContent = getContentsForTest(1)[0];
		sourceContent.insert(0, "#include \"" + HEADER_FILE_NAME + "\"\n");
		assertNotNull(createFile(project, HEADER_FILE_NAME, headerContent));
		return createFile(project, SOURCE_FILE_NAME, sourceContent.toString());
	}

	protected static final int DEFAULT_FLAGS = IS_COMPLETION;

	protected void assertParametersGuesses(Map<String, String[][]> expected) throws Exception {
		assertContentAssistResults(getBuffer().length() - 1, 0, expected, DEFAULT_FLAGS, CompareType.REPLACEMENT);
	}

	//	void foo(){
	//		aStruct* axPtr;
	//		aStruct ax;
	//		funWith2ATypeObjectParams(
	public void testIndirectTypes() throws Exception {
		Map<String, String[][]> resultsMap = new HashMap<>();
		resultsMap.put("funWith2ATypeObjectParams(a, b)", new String[][] { { "ax", "*axPtr" }, { "ax", "*axPtr" } });
		assertParametersGuesses(resultsMap);
	}

	//	void foo(){
	//		aStruct ax;
	//		int x;
	//		ov
	public void testMultipleFunctions() throws Exception {
		Map<String, String[][]> resultsMap = new HashMap<>();
		resultsMap.put("ov1(x, a)", new String[][] { { "x" }, { "ax" } });
		resultsMap.put("ov2(x, aPtr)", new String[][] { { "x" }, { "&ax" } });
		assertParametersGuesses(resultsMap);
	}
}
