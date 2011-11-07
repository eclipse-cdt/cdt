/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.TextSelection;
import org.osgi.framework.Bundle;

/**
 * @author Emanuel Graf
 */
public class RewriteTester extends TestSuite {
	enum MatcherState{skip, inTest, inSource, inExpectedResult}
	
	private static final String classRegexp = "//#(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String testRegexp = "//!(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String fileRegexp = "//@(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String resultRegexp = "//=.*$"; //$NON-NLS-1$
	
	public static Test suite(String name, String file) throws Exception {
		BufferedReader in = createReader(file);

		ArrayList<RewriteBaseTest> testCases = createTests(in);
		in.close();
		return createSuite(testCases, name);
	}
	
	protected static BufferedReader createReader(String file) throws IOException {
		Bundle bundle = CTestPlugin.getDefault().getBundle();
		Path path = new Path(file);
		String file2 = FileLocator.toFileURL(FileLocator.find(bundle, path, null)).getFile();
		return new BufferedReader(new FileReader(file2));
	}
	
	private static ArrayList<RewriteBaseTest> createTests(BufferedReader inputReader) throws Exception {
		String line;
		Vector<TestSourceFile> files = new Vector<TestSourceFile>();
		TestSourceFile actFile = null;
		MatcherState matcherState = MatcherState.skip;
		ArrayList<RewriteBaseTest> testCases = new ArrayList<RewriteBaseTest>();
		String testName = null;
		String className = null;
		boolean bevorFirstTest = true;
		
		while ((line = inputReader.readLine()) != null) {
			if (lineMatchesBeginOfTest(line)) {
				if (!bevorFirstTest) {
					RewriteBaseTest test = createTestClass(className, testName, files);
					testCases.add(test);
					files = new Vector<TestSourceFile>();
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
		RewriteBaseTest test = createTestClass(className, testName, files);
		testCases.add(test);
		return testCases;
	}
	
	private static RewriteBaseTest createTestClass(String className, String testName,
			Vector<TestSourceFile> files) throws Exception {
		try {
			Class<?> refClass = Class.forName(className);
			Class<?> paratypes[] = new Class[2];
			paratypes[0] = testName.getClass();
			paratypes[1] = files.getClass();
			Constructor<?> ct = refClass.getConstructor(paratypes);
			Object arglist[] = new Object[2];
			arglist[0] = testName;
			arglist[1] = files;
			RewriteBaseTest test = (RewriteBaseTest) ct.newInstance(arglist);
			for (TestSourceFile file : files) {
				TextSelection sel = file.getSelection();
				if (sel != null) {
					test.setFileWithSelection(file.getName());
					test.setSelection(sel);
					break;
				}
			}
			return test;
		} catch (ClassNotFoundException e) {
			throw new Exception("Unknown TestClass: " + e.getMessage() +
					". Make sure the test's sourcefile specifies a valid test class."); 
		} catch (SecurityException e) {
			throw new Exception("Security Exception during Test creation", e); 
		} catch (NoSuchMethodException e) {
			throw new Exception("Test class does not provied required constructor."); 
		} catch (IllegalArgumentException e) {
			throw new Exception("IllegalArgumentException during Test creation", e); 
		} catch (InstantiationException e) {
			throw new Exception("InstantiationException during Test creation", e); 
		} catch (IllegalAccessException e) {
			throw new Exception("IllegalAccessException during Test creation", e); 
		} catch (InvocationTargetException e) {
			throw new Exception("InvocationTargetException during Test creation", e); 
		}
	}

	private static String getFileName(String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(fileRegexp, line);
		if (matcherBeginOfTest.find())
			return matcherBeginOfTest.group(1);
		return null;
	}

	private static String getNameOfClass(String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(classRegexp, line);
		if (matcherBeginOfTest.find())
			return matcherBeginOfTest.group(1);
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
		return "Not Named";
	}
	
	private static boolean lineMatchesBeginOfResult(String line) {
		return createMatcherFromString(resultRegexp, line).find();
	}
	
	private static TestSuite createSuite(ArrayList<RewriteBaseTest> testCases, String name) {
		TestSuite suite = new TestSuite(name);
		Iterator<RewriteBaseTest> it = testCases.iterator();
		while (it.hasNext()) {
			RewriteBaseTest subject =it.next();
			suite.addTest(subject);
		}
		return suite;
	}
}

