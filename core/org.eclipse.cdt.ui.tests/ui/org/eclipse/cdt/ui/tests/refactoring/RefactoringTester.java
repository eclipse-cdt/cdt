/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.TextSelection;
import org.osgi.framework.Bundle;

import org.eclipse.cdt.ui.testplugin.CTestPlugin;

/**
 * @author Emanuel Graf
 */
public class RefactoringTester extends TestSuite{
	enum MatcherState{skip, inTest, inSource, inExpectedResult}
	
	private static final String classRegexp = "//#(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String testRegexp = "//!(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String fileRegexp = "//@(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String resultRegexp = "//=.*$"; //$NON-NLS-1$
	
	public static Test suite(String name, String file)throws Exception {
		BufferedReader in = createReader(file);

		ArrayList<RefactoringBaseTest> testCases = createTests(in);
		in.close();
		return createSuite(testCases, name);
	}
	
	protected static BufferedReader createReader(String file) throws IOException {
		Bundle bundle = CTestPlugin.getDefault().getBundle();
		Path path = new Path(file);
		String file2 = FileLocator.toFileURL(FileLocator.find(bundle, path, null)).getFile();
		return new BufferedReader(new FileReader(file2));
	}
	
	private static ArrayList<RefactoringBaseTest> createTests(BufferedReader inputReader) throws Exception {
		String line;
		Collection<TestSourceFile> files = new ArrayList<TestSourceFile>();
		TestSourceFile actFile = null;
		MatcherState matcherState = MatcherState.skip;
		ArrayList<RefactoringBaseTest> testCases = new ArrayList<RefactoringBaseTest>();
		String testName = null;
		String className = null;
		boolean bevorFirstTest = true;
		
		while ((line = inputReader.readLine()) != null) {
			if (lineMatchesBeginOfTest(line)) {
				if (!bevorFirstTest) {
					RefactoringBaseTest test = createTestClass(className, testName, files);
					testCases.add(test);
					files = new ArrayList<TestSourceFile>();
					className = null;
					testName = null;
				}
				matcherState = MatcherState.inTest;
				testName = getNameOfTest(line);
				bevorFirstTest = false;
				continue;
			} else if (lineMatchesBeginOfResult(line)) {
				matcherState = MatcherState.inExpectedResult;
				continue;
			} else if (lineMatchesFileName(line)) {
				matcherState = MatcherState.inSource;
				actFile = new TestSourceFile(getFileName(line));
				files.add(actFile);
				continue;
			} else if (lineMatchesClassName(line)) {
				className = getNameOfClass(line);
				continue;
			}
			
			switch (matcherState) {
			case inSource:
				if (actFile != null) {
					actFile.addLineToSource(line);
				}
				break;
			case inExpectedResult:
				if (actFile != null) {
					actFile.addLineToExpectedSource(line);
				}
				break;
			default:
				break;
			}
		}
		RefactoringBaseTest test = createTestClass(className, testName, files);
		testCases.add(test);
		return testCases;
	}
	
	private static RefactoringBaseTest createTestClass(String className, String testName, Collection<TestSourceFile> files) throws Exception {
		try {
			Class<?> refClass = Class.forName(className);
			if (testName == null) {
				testName = refClass.getSimpleName();
			}
			Constructor<?>[] constructors = refClass.getConstructors();
			for (Constructor<?> ctor : constructors) {
				Object arglist[] = new Object[] { testName, files };
				if (ctor.getParameterTypes().length == arglist.length) {
					RefactoringBaseTest test = null;
					try {
						test = (RefactoringBaseTest) ctor.newInstance(arglist);
					} catch (IllegalArgumentException e) {
						continue;
					}
					for (TestSourceFile file : files) {
						TextSelection sel = file.getSelection();
						if (sel != null) {
							test.setFileWithSelection(file.getName());
							test.setSelection(sel);
							break;
						}
					}
					return test;
				}
			}
			throw new Exception(className + " class does not provide required constructor"); //$NON-NLS-1$
		} catch (ClassNotFoundException e) {
			throw new Exception("Unknown test class " + className); //$NON-NLS-1$
		} catch (Exception e) {
			throw new Exception(e.getClass().getSimpleName()
					+ " during creation of test " + className, e); //$NON-NLS-1$
		}
	}

	private static String getFileName(String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(fileRegexp, line);
		if (matcherBeginOfTest.find())
			return matcherBeginOfTest.group(1);
		else
			return null;
	}

	private static String getNameOfClass(String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(classRegexp, line);
		if (matcherBeginOfTest.find())
			return matcherBeginOfTest.group(1);
		else
			return null;
	}

	private static boolean lineMatchesBeginOfTest(String line) {
		return createMatcherFromString(testRegexp, line).find();
	}
	
	private static boolean lineMatchesClassName(String line) {
		return createMatcherFromString(classRegexp, line).find();
	}
	
	private static boolean lineMatchesFileName(String line) {
		return createMatcherFromString(fileRegexp, line).find();
	}

	protected static Matcher createMatcherFromString(String pattern, String line) {
		return Pattern.compile(pattern).matcher(line);
	}
	
	private static String getNameOfTest(String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(testRegexp, line);
		if (matcherBeginOfTest.find())
			return matcherBeginOfTest.group(1);
		return null;
	}
	
	private static boolean lineMatchesBeginOfResult(String line) {
		return createMatcherFromString(resultRegexp, line).find();
	}
	
	private static TestSuite createSuite(ArrayList<RefactoringBaseTest> testCases, String name) {
		TestSuite suite = new TestSuite(name);
		Iterator<RefactoringBaseTest> it = testCases.iterator();
		while(it.hasNext()) {
			RefactoringBaseTest subject =it.next();
			suite.addTest(subject);
		}
		return suite;
	}
}

