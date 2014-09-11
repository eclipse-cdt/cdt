/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Mohamed Azab (Mentor Graphics) - Initial implementation.
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase;

public class ParameterGuessingTests_PlainC extends AbstractContentAssistTest {
	private static final String HEADER_FILE_NAME = "PGTest_C.h";
	private static final String SOURCE_FILE_NAME = "PGTest_C.c";

	//	{PGTest_C.h}
	//	typedef struct aStruct {
	//		int a;
	//		int b;
	//	}aStruct;
	//	void ov1(int x, aStruct a);
	//	void ov2(int x, aStruct* aPtr);
	//	int piab(aStruct a, aStruct b);

	public ParameterGuessingTests_PlainC(String name) {
		super(name, false);
	}

	public static Test suite() {
		return BaseTestCase.suite(ParameterGuessingTests_PlainC.class, "_");
	}

	@Override
	protected IFile setUpProjectContent(IProject project) throws Exception {
		String headerContent= readTaggedComment(HEADER_FILE_NAME);
		StringBuilder sourceContent= getContentsForTest(1)[0];
		sourceContent.insert(0, "#include \"" + HEADER_FILE_NAME + "\"\n");
		assertNotNull(createFile(project, HEADER_FILE_NAME, headerContent));
		return createFile(project, SOURCE_FILE_NAME, sourceContent.toString());
	}

	protected void assertParametersGuesses(Map<String, String[][]> expected) throws Exception {
		assertContentAssistResults(getBuffer().length() - 1, 0, expected, true, false, false, CompareType.REPLACEMENT);
	}

	//	void foo(){
	//		aStruct* axPtr;
	//		aStruct ax;
	//		piab(
	public void testIndirectTypes() throws Exception {
		Map<String, String[][]> resultsMap = new HashMap<String, String[][]>();
		resultsMap.put("piab(ax, *axPtr)", new String[][]{
				{"*axPtr", "ax"}, {"*axPtr", "ax"}
		});
		assertParametersGuesses(resultsMap);
	}

	//	void foo(){
	//		aStruct ax;
	//		ov
	public void testMultipleFunctions() throws Exception {
		Map<String, String[][]> resultsMap = new HashMap<String, String[][]>();
		resultsMap.put("ov1(x, ax)", new String[][]{
				{"x"}, {"ax"}
		});
		resultsMap.put("ov2(x, &ax)", new String[][]{
				{"x"}, {"&ax"}
		});
		assertParametersGuesses(resultsMap);
	}
}
