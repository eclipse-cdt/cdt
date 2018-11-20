/*******************************************************************************
 * Copyright (c) 2012, 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.testplugin.util;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.pdom.tests.PDOMPrettyPrinter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * Base class for tests that use AST. The files in the test project are created from the comments
 * preceding the test case. The test project will contain a single source file called source.cpp or
 * source.c, depending on whether the project is for C++ or C, and zero or more header files called
 * header1.h, header2.h, etc. The AST is created for the source file only and can be obtained
 * by calling getAst().
 */
public class OneSourceMultipleHeadersTestCase extends BaseTestCase {
	private static final boolean DEBUG = false;

	private final TestSourceReader testSourceReader;
	private final boolean cpp;
	private IIndex index;
	private ICProject cproject;
	private StringBuilder[] testData;
	private IASTTranslationUnit ast;

	public OneSourceMultipleHeadersTestCase(TestSourceReader testSourceReader, boolean cpp) {
		this(null, testSourceReader, cpp);
	}

	public OneSourceMultipleHeadersTestCase(String name, TestSourceReader testSourceReader, boolean cpp) {
		super(name);
		this.testSourceReader = testSourceReader;
		this.cpp = cpp;
	}

	protected ICProject getCProject() {
		return cproject;
	}

	protected IIndex getIndex() {
		return index;
	}

	protected StringBuilder[] getTestData() {
		return testData;
	}

	protected IASTTranslationUnit getAst() {
		return ast;
	}

	protected String getAstSource() {
		return testData[testData.length - 1].toString();
	}

	@Override
	protected void setUp() throws Exception {
		setUp(false);
	}

	protected void setUp(boolean generateIncludeStatements) throws Exception {
		cproject = cpp
				? CProjectHelper.createCCProject(getName() + System.currentTimeMillis(), "bin",
						IPDOMManager.ID_NO_INDEXER)
				: CProjectHelper.createCProject(getName() + System.currentTimeMillis(), "bin",
						IPDOMManager.ID_NO_INDEXER);
		testData = testSourceReader.getContentsForTest(getName());

		if (testData.length > 0) {
			for (int i = 0; i < testData.length - 1; i++) {
				String filename = String.format("header%d.h", i + 1);
				IFile file = TestSourceReader.createFile(cproject.getProject(), new Path(filename),
						testData[i].toString());
				CCorePlugin.getIndexManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
			}
		}

		if (generateIncludeStatements) {
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < getTestData().length - 1; i++) {
				String filename = String.format("header%d.h", i + 1);
				buf.append(String.format("#include \"header%d.h\"\n", i + 1));
			}
			testData[testData.length - 1].insert(0, buf);
		}

		IFile cppfile = TestSourceReader.createFile(cproject.getProject(), new Path("source.c" + (cpp ? "pp" : "")),
				getAstSource());
		waitForIndexer(cproject);

		if (DEBUG) {
			System.out.println("Project PDOM: " + getName());
			((PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject)).accept(new PDOMPrettyPrinter());
		}

		index = CCorePlugin.getIndexManager().getIndex(cproject);

		index.acquireReadLock();
		ast = TestSourceReader.createIndexBasedAST(index, cproject, cppfile);
	}

	@Override
	protected void tearDown() throws Exception {
		if (index != null) {
			index.releaseReadLock();
		}
		if (cproject != null) {
			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT,
					new NullProgressMonitor());
		}
	}
}
