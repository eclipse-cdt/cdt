/*******************************************************************************
 * Copyright (c) 2012,2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
 package org.eclipse.cdt.managedbuilder.tests.suite;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

public class Preconditions extends TestCase {
	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ResourceHelper.cleanUp(getName());
	}

	/**
	 * Many MBS tests run make and gcc and will inspect resulting artifacts of the build.
	 * Make sure GNU tool-chain is available for the tests.
	 */
	public void testGnu() {
		IPath make = PathUtil.findProgramLocation("make");
		assertNotNull("Precodition FAILED - program 'make' is not found in path.", make);

		IPath gcc = PathUtil.findProgramLocation("gcc");
		assertNotNull("Precodition FAILED - program 'gcc' is not found in path.", gcc);
	}

	/**
	 * Generated makefiles will often contain dependency lines for all file extension
	 * corresponding content types set in preferences. Make sure that this set has not been
	 * changed when the tests are run.
	 */
	public void testContentTypes() {
		Set<String> fileExts = new TreeSet<String>();
		IContentTypeManager manager = Platform.getContentTypeManager();

		IContentType contentTypeCpp = manager.getContentType(CCorePlugin.CONTENT_TYPE_CXXSOURCE);
		fileExts.addAll(Arrays.asList(contentTypeCpp.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)));

		IContentType contentTypeC = manager.getContentType(CCorePlugin.CONTENT_TYPE_CSOURCE);
		fileExts.addAll(Arrays.asList(contentTypeC.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)));

		Set<String> expectedExts = new TreeSet<String>(Arrays.asList(new String[] {"C", "c", "c++", "cc", "cpp", "cxx"}));
		assertEquals("Precodition FAILED - Content Types do not match expected defaults.", expectedExts.toString(), fileExts.toString());
	}

}
