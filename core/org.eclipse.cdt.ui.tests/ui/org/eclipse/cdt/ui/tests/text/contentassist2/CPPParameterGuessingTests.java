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

public class CPPParameterGuessingTests extends AbstractContentAssistTest {
	private static final String HEADER_FILE_NAME = "PGTest.h";
	private static final String SOURCE_FILE_NAME = "PGTest.cpp";

	//	{PGTest.h}
	//	class aClass {
	//	public:
	//		int aField;
	//		void aMethod(char c);
	//		void aMethod(char c, int x);
	//	};
	//
	//	class bClass : aClass {
	//	};
	//
	//	void overload(int x, aClass a);
	//	void overload(int x, aClass* aPtr);
	//	int piab(aClass a, bClass b);
	//	template<class T>void tFunc(T x, T y);

	public CPPParameterGuessingTests(String name) {
		super(name, true);
	}

	public static Test suite() {
		return BaseTestCase.suite(CPPParameterGuessingTests.class, "_");
	}

	@Override
	protected IFile setUpProjectContent(IProject project) throws Exception {
		String headerContent = readTaggedComment(HEADER_FILE_NAME);
		StringBuilder sourceContent = getContentsForTest(1)[0];
		sourceContent.insert(0, "#include \"" + HEADER_FILE_NAME + "\"\n");
		assertNotNull(createFile(project, HEADER_FILE_NAME, headerContent));
		return createFile(project, SOURCE_FILE_NAME, sourceContent.toString());
	}

	protected void assertParametersGuesses(Map<String, String[][]> expected)
			throws Exception {
		assertContentAssistResults(getBuffer().length() - 1, 0, expected, true,
				false, false, CompareType.REPLACEMENT);
	}

	//	void foo(){
	//		aClass* aTypePtr;
	//		bClass bTypeObj;
	//		piab(
	public void testIndirectTypes() throws Exception {
		Map<String, String[][]> resultsMap = new HashMap<>();
		resultsMap.put("piab(a, b)", new String[][] { { "*aTypePtr", "bTypeObj" },
				{ "bTypeObj" } });
		assertParametersGuesses(resultsMap);
	}

	//	void foo(){
	//		int intVal;
	//		aClass aTypeObj;
	//		overload(
	public void testOverloadedFunction() throws Exception {
		Map<String, String[][]> resultsMap = new HashMap<>();
		resultsMap.put("overload(x, a)", new String[][] { { "intVal" }, { "aTypeObj" } });
		resultsMap.put("overload(x, aPtr)",
				new String[][] { { "intVal" }, { "&aTypeObj" } });
		assertParametersGuesses(resultsMap);
	}

	//	void foo(){
	//		aClass aTypeObj;
	//		tFunc<aClass> (
	public void testTemplateFunction() throws Exception {
		Map<String, String[][]> resultsMap = new HashMap<>();
		resultsMap.put("tFunc<aClass> (x, y)", new String[][] { { "x" },
				{ "y" } });
		assertParametersGuesses(resultsMap);
	}

	//	struct container {
	//		aClass* aTypePtr;
	//	};
	//
	//	void foo(){
	//		char charX, charY, charZ;
	//		container containerObj;
	//		containerObj.aTypePtr = new aClass();
	//		containerObj.aTypePtr->
	public void testOverloadedMethod() throws Exception {
		Map<String, String[][]> resultsMap = new HashMap<>();
		resultsMap.put("aMethod(c)", new String[][] { { "charX", "charY", "charZ" } });
		resultsMap.put("aMethod(c, x)", new String[][] { { "charX", "charY", "charZ" },
				{ "charX", "charY", "charZ" } });
		assertParametersGuesses(resultsMap);
	}

	//	void testParameterNameMatching(int lngName, int shrt);
	//
	//	void foo() {
	//		int lng;
	//		int shrtNameMatch;
	//		testParameter
	public void testParameterNameMatching() throws Exception {
		Map<String, String[][]> resultsMap = new HashMap<>();
		resultsMap.put("testParameterNameMatching(lngName, shrt)", new String[][] {
				{ "lng", "shrtNameMatch" }, { "lng", "shrtNameMatch" } });
		assertParametersGuesses(resultsMap);
	}

	//	class cClass : bClass {
	//	public:
	//		cClass(int inCall) {
	//			char charX, charY;
	//			aClass::
	public void testInsideConstructor() throws Exception {
		Map<String, String[][]> resultsMap = new HashMap<>();
		resultsMap.put("aMethod(c)", new String[][] { { "charX", "charY", "inCall" } });
		resultsMap.put("aMethod(c, x)", new String[][] {
				{ "charX", "charY", "inCall" }, { "charX", "charY", "inCall" } });
		assertParametersGuesses(resultsMap);
	}
}
