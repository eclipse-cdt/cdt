/*******************************************************************************
 * Copyright (c) 2011, 2014 Jens Elmenthaler and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.InstanceScope;

import junit.framework.Test;

/**
 * Test the correct response to the value of {@link ContentAssistPreference#SHOW_CAMEL_CASE_MATCHES}.
 */
public class ShowCamelCasePreferenceTest extends AbstractContentAssistTest {

	private static final String SOURCE_FILE_NAME = "ContentAssistPreferenceTest.cpp";
	private static final String CURSOR_LOCATION_TAG = "/*cursor*/";

	protected int fCursorOffset;
	private IProject fProject;

	public ShowCamelCasePreferenceTest(String name) {
		super(name, true);
	}

	public static Test suite() {
		return BaseTestCase.suite(ShowCamelCasePreferenceTest.class, "_");
	}

	@Override
	protected IFile setUpProjectContent(IProject project) throws Exception {
		fProject = project;
		StringBuilder sourceContent = getContentsForTest(1)[0];
		fCursorOffset = sourceContent.indexOf(CURSOR_LOCATION_TAG);
		assertTrue("No cursor location specified", fCursorOffset >= 0);
		sourceContent.delete(fCursorOffset, fCursorOffset + CURSOR_LOCATION_TAG.length());
		return createFile(project, SOURCE_FILE_NAME, sourceContent.toString());
	}

	@Override
	protected void setUp() throws Exception {
		InstanceScope.INSTANCE.getNode(CUIPlugin.PLUGIN_ID).remove(ContentAssistPreference.SHOW_CAMEL_CASE_MATCHES);
		super.setUp();
	}

	@Override

	protected void tearDown() throws Exception {
		InstanceScope.INSTANCE.getNode(CUIPlugin.PLUGIN_ID).remove(ContentAssistPreference.SHOW_CAMEL_CASE_MATCHES);
		super.tearDown();
	}

	protected static final int DEFAULT_FLAGS = AbstractContentAssistTest.DEFAULT_FLAGS | IS_COMPLETION;

	protected void assertCompletionResults(int offset, String[] expected, CompareType compareType) throws Exception {
		assertContentAssistResults(offset, expected, DEFAULT_FLAGS, compareType);
	}

	protected void assertCompletionResults(String[] expected) throws Exception {
		assertCompletionResults(fCursorOffset, expected, CompareType.REPLACEMENT);
	}

	private void setShowCamelCaseMatches(boolean enabled) {
		InstanceScope.INSTANCE.getNode(CUIPlugin.PLUGIN_ID).putBoolean(ContentAssistPreference.SHOW_CAMEL_CASE_MATCHES,
				enabled);
	}

	// int fbar;
	// int fooBar;
	// void something() {
	//   fB/*cursor*/
	// }
	public void testDefault() throws Exception {
		final String[] expected = { "fbar", "fooBar" };
		assertCompletionResults(expected);
	}

	// int fbar;
	// int fooBar;
	// void something() {
	//   fB/*cursor*/
	// }
	public void testCamelCaseOff() throws Exception {
		setShowCamelCaseMatches(false);
		final String[] expected = { "fbar" };
		assertCompletionResults(expected);
	}

	// int fbar;
	// int fooBar;
	// void something() {
	//   fB/*cursor*/
	// }
	public void testCamelCaseOn() throws Exception {
		setShowCamelCaseMatches(true);
		final String[] expected = { "fbar", "fooBar" };
		assertCompletionResults(expected);
	}
}
