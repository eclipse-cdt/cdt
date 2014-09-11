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

public class ParameterGuessingTests extends AbstractContentAssistTest {
	private static final String HEADER_FILE_NAME = "PGTest.h";
	private static final String SOURCE_FILE_NAME = "PGTest.cpp";

	//	{PGTest.h}
	//	class aClass {
	//  public:
	//	    int aField;
	//	    void aMethod(char c);
	//	    void aMethod(char c, int x);
	//	};
	//	class bClass : aClass {
	//	};
	//	void overload(int x, aClass a);
	//	void overload(int x, aClass* aPtr);
	//	int piab(aClass a, bClass b);
	//	template<class T>void tFunc(T x, T y);

	public ParameterGuessingTests(String name) {
		super(name, true);
	}

	public static Test suite() {
		return BaseTestCase.suite(ParameterGuessingTests.class, "_");
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
	//		aClass* axPtr;
	//		bClass bx;
	//		piab(
	public void testIndirectTypes() throws Exception {
		Map<String, String[][]> resultsMap = new HashMap<String, String[][]>();
		resultsMap.put("piab(a, b)", new String[][]{
				{"a", "*axPtr", "bx"}, {"b", "bx"}
		});
		assertParametersGuesses(resultsMap);
	}

	//	void foo(){
	//		int w;
	//		aClass ax;
	//		overload(
	public void testOverloadedFunction() throws Exception {
		Map<String, String[][]> resultsMap = new HashMap<String, String[][]>();
		resultsMap.put("overload(x, a)", new String[][]{
				{"x", "w"}, {"a", "ax"}
		});
		resultsMap.put("overload(x, aPtr)", new String[][]{
				{"x", "w"}, {"aPtr", "&ax"}
		});
		assertParametersGuesses(resultsMap);
	}

	//	void foo(){
	//		aClass ax;
	//		tFunc<aClass> (
	public void testTemplateFunction() throws Exception {
		Map<String, String[][]> resultsMap = new HashMap<String, String[][]>();
		resultsMap.put("tFunc<aClass> (x, y)", new String[][]{
				{"x"}, {"y"}
		});
		assertParametersGuesses(resultsMap);
	}

	//	struct container {
	//		aClass* axPtr;
	//	};
	//	void foo(){
	//		char x, y, z;
	//		container c;
	//		c.axPtr = new aClass();
	//		c.axPtr->
	public void testOverloadedMethod() throws Exception {
		Map<String, String[][]> resultsMap = new HashMap<String, String[][]>();
		resultsMap.put("aMethod(c)", new String[][]{
				{"c", "x", "y", "z"}
		});
		resultsMap.put("aMethod(c, x)", new String[][]{
				{"c", "x", "y", "z"}, {"x", "x", "y", "z"}
		});
		assertParametersGuesses(resultsMap);
	}
	
	//	class cClass : bClass {
	//	public:
	//		cClass(int inCall) {
	//			char x, y;
	//			aClass::
	public void testInsideConstructor() throws Exception {
		Map<String, String[][]> resultsMap = new HashMap<String, String[][]>();
		resultsMap.put("aMethod(c)", new String[][]{
				{"c", "x", "y", "inCall"}
		});
		resultsMap.put("aMethod(c, x)", new String[][]{
				{"c", "x", "y", "inCall"}, {"x", "x", "y", "inCall"}
		});
		assertParametersGuesses(resultsMap);
	}
}
