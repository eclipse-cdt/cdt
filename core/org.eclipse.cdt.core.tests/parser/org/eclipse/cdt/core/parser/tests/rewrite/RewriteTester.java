/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.parser.tests.rewrite.comenthandler.CommentHandlingTest;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.params.provider.Arguments;
import org.osgi.framework.Bundle;

/**
 * This is not actually a test, but a test provider. See loadTests and its uses
 *
 * The possibly unusual structure here is a result of migrating this JUnit 3 test
 * suite creator to JUnit 5
 *
 * @author Emanuel Graf
 */
public class RewriteTester {
	enum MatcherState {
		skip, inTest, inSource, inExpectedResult
	}

	private static final String classRegexp = "//#(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String testRegexp = "//!(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String fileRegexp = "//@(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String resultRegexp = "//=.*$"; //$NON-NLS-1$

	public static List<Arguments> loadTests(Class<? extends CommentHandlingTest> clazz, String file) throws Exception {
		BufferedReader in = createReader(file);

		List<Arguments> testCases = createTests(clazz, in);
		in.close();
		return testCases;
	}

	protected static BufferedReader createReader(String file) throws IOException {
		Bundle bundle = CTestPlugin.getDefault().getBundle();
		Path path = new Path(file);
		String file2 = FileLocator.toFileURL(FileLocator.find(bundle, path, null)).getFile();
		return new BufferedReader(new FileReader(file2));
	}

	private static List<Arguments> createTests(Class<? extends CommentHandlingTest> clazz, BufferedReader inputReader)
			throws Exception {
		String line;
		List<TestSourceFile> files = new ArrayList<>();
		TestSourceFile actFile = null;
		MatcherState matcherState = MatcherState.skip;
		List<Arguments> testCases = new ArrayList<>();
		String testName = null;
		String className = null;
		boolean bevorFirstTest = true;

		while ((line = inputReader.readLine()) != null) {
			if (lineMatchesBeginOfTest(line)) {
				if (!bevorFirstTest) {
					Arguments test = createTestClass(clazz, className, testName, files);
					testCases.add(test);
					files = new ArrayList<>();
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
		Arguments test = createTestClass(clazz, className, testName, files);
		testCases.add(test);
		return testCases;
	}

	private static Arguments createTestClass(Class<? extends CommentHandlingTest> clazz, String className,
			String testName, List<TestSourceFile> files) throws Exception {
		// For historical reasons, the Java classname of the test exists in the rts files.
		// This is a check that the rts file matches the test currently being loaded.
		assertEquals(clazz.getName(), className);
		return Arguments.argumentSet(testName, files);
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
}
