/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.ExpectedStrings;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Peter Graves
 *
 * This file contains a set of generic tests for the core C model's Binary
 * class. There is nothing exotic here, mostly just sanity type tests
 *
 */
public class BinaryTests {
	IWorkspace workspace;
	IWorkspaceRoot root;
	ICProject testProject;
	IFile cfile, exefile, libfile, archfile, objfile, bigexe, ppcexefile, ndexe;
	Path cpath, exepath, libpath, archpath, objpath;
	NullProgressMonitor monitor;

	@BeforeEach
	protected void setUp() throws Exception {
		/***
		 * The tests assume that they have a working workspace
		 * and workspace root object to use to create projects/files in,
		 * so we need to get them setup first.
		 */
		workspace = ResourcesPlugin.getWorkspace();
		root = workspace.getRoot();
		monitor = new NullProgressMonitor();
		if (workspace == null)
			fail("Workspace was not setup");
		if (root == null)
			fail("Workspace root was not setup");

		/***
		 * Setup the various files, paths and projects that are needed by the
		 * tests
		 */

		testProject = CProjectHelper.createCProject("filetest", "none", IPDOMManager.ID_NO_INDEXER);

		// since our test require that we can read the debug info from the exe we must set the GNU elf
		// binary parser since the default (generic elf binary parser) does not do this.
		ICProjectDescription projDesc = CoreModel.getDefault().getProjectDescription(testProject.getProject(), true);
		ICConfigurationDescription defaultConfig = projDesc.getDefaultSettingConfiguration();
		defaultConfig.remove(CCorePlugin.BINARY_PARSER_UNIQ_ID);
		defaultConfig.create(CCorePlugin.BINARY_PARSER_UNIQ_ID, "org.eclipse.cdt.core.GNU_ELF");
		CoreModel.getDefault().setProjectDescription(testProject.getProject(), projDesc);

		if (testProject == null)
			fail("Unable to create project");

		cfile = testProject.getProject().getFile("exetest.c");
		if (!cfile.exists()) {
			cfile.create(
					new FileInputStream(CTestPlugin.getDefault().getFileInPlugin(new Path("resources/exe/main.c"))),
					false, monitor);
		}
		cpath = new Path(workspace.getRoot().getLocation() + "/filetest/main.c");

		objfile = testProject.getProject().getFile("exetest.o");
		if (!objfile.exists()) {
			objfile.create(
					new FileInputStream(
							CTestPlugin.getDefault().getFileInPlugin(new Path("resources/exe/x86/o.g/main.o"))),
					false, monitor);
		}
		objpath = new Path(workspace.getRoot().getLocation() + "/filetest/exetest.o");

		exefile = testProject.getProject().getFile("test_g");
		if (!exefile.exists()) {
			exefile.create(
					new FileInputStream(
							CTestPlugin.getDefault().getFileInPlugin(new Path("resources/exe/x86/o.g/exe_g"))),
					false, monitor);
		}
		exepath = new Path(workspace.getRoot().getLocation() + "/filetest/exe_g");
		ppcexefile = testProject.getProject().getFile("ppctest_g");
		if (!ppcexefile.exists()) {
			ppcexefile.create(
					new FileInputStream(
							CTestPlugin.getDefault().getFileInPlugin(new Path("resources/exe/ppc/be.g/exe_g"))),
					false, monitor);
		}
		ndexe = testProject.getProject().getFile("exetest");
		if (!ndexe.exists()) {
			ndexe.create(
					new FileInputStream(CTestPlugin.getDefault().getFileInPlugin(new Path("resources/exe/x86/o/exe"))),
					false, monitor);
		}

		bigexe = testProject.getProject().getFile("exebig_g");
		if (!bigexe.exists()) {
			bigexe.create(
					new FileInputStream(
							CTestPlugin.getDefault().getFileInPlugin(new Path("resources/exebig/x86/o.g/exebig_g"))),
					false, monitor);
		}

		archfile = testProject.getProject().getFile("libtestlib_g.a");
		if (!archfile.exists()) {
			archfile.create(new FileInputStream(
					CTestPlugin.getDefault().getFileInPlugin(new Path("resources/testlib/x86/a.g/libtestlib_g.a"))),
					false, monitor);
		}
		libpath = new Path(workspace.getRoot().getLocation() + "/filetest/libtestlib_g.so");

		libfile = testProject.getProject().getFile("libtestlib_g.so");
		if (!libfile.exists()) {
			libfile.create(new FileInputStream(
					CTestPlugin.getDefault().getFileInPlugin(new Path("resources/testlib/x86/so.g/libtestlib_g.so"))),
					false, monitor);
		}
		archpath = new Path(workspace.getRoot().getLocation() + "/filetest/libtestlib_g.a");

	}

	/**
	* Tears down the test fixture.
	*
	* Called after every test case method.
	*/
	@AfterEach
	protected void tearDown() throws CoreException, InterruptedException {
		System.gc();
		System.runFinalization();
		CProjectHelper.delete(testProject);
	}

	/****
	 * Simple tests to make sure we can get all of a binarys children
	 */
	@Test
	public void testGetChildren() throws CoreException, FileNotFoundException {
		IBinary myBinary;
		ICElement[] elements;
		ExpectedStrings expSyms;
		//        String[] myStrings = {"test.c", "_init","main.c", "_start", "test2.c", "_btext"};
		// On Windows at least, it appears the .c files aren't included in the binary
		String[] myStrings = { "_init", "_start", "_btext" };

		expSyms = new ExpectedStrings(myStrings);

		/***
		 * Grab the IBinary we want to test, and find all the elements in all
		 * the binarie and make sure we get everything we expect.
		 */
		myBinary = CProjectHelper.findBinary(testProject, "test_g");
		elements = myBinary.getChildren();
		for (int i = 0; i < elements.length; i++) {
			expSyms.foundString(elements[i].getElementName());
		}

		assertTrue(expSyms.gotAll(), expSyms.getMissingString());
		//        assertTrue(expSyms.getExtraString(), !expSyms.gotExtra());
	}

	/***
	 * A quick check to make sure the getBSS function works as expected.
	 */
	@Test
	public void testGetBss() throws CModelException {
		IBinary bigBinary, littleBinary;
		bigBinary = CProjectHelper.findBinary(testProject, "exebig_g");
		littleBinary = CProjectHelper.findBinary(testProject, "test_g");

		assertTrue(bigBinary.getBSS() == 432, "Expected 432, Got: " + bigBinary.getBSS());
		assertTrue(littleBinary.getBSS() == 4, "Expected 4, Got: " + littleBinary.getBSS());
	}

	/***
	 * A quick check to make sure the getBSS function works as expected.
	 */
	@Test
	public void testGetData() throws CModelException {
		IBinary bigBinary, littleBinary;
		bigBinary = CProjectHelper.findBinary(testProject, "exebig_g");
		littleBinary = CProjectHelper.findBinary(testProject, "test_g");
		/* These two test used to fail due to pr 23602 */
		assertTrue(bigBinary.getData() == 256, "Expected 256 Got: " + bigBinary.getData());
		assertTrue(littleBinary.getData() == 196, "Expected 196, Got: " + littleBinary.getData());
	}

	/***
	 * A very small set of tests to make usre Binary.getCPU() seems to return
	 * something sane for the most common exe type (x86) and one other (ppc)
	 * This is not a in depth test at all.
	 */
	@Test
	public void testGetCpu() throws CModelException {
		IBinary myBinary;
		myBinary = CProjectHelper.findBinary(testProject, "exebig_g");

		assertTrue(myBinary.getCPU().equals("x86"), "Expected: x86  Got: " + myBinary.getCPU());
		myBinary = CProjectHelper.findBinary(testProject, ppcexefile.getLocation().lastSegment());
		assertTrue(myBinary.getCPU().equals("ppc"), "Expected: ppc  Got: " + myBinary.getCPU());

	}

	/****
	 * A set of simple tests to make sute getNeededSharedLibs seems to be sane
	 */
	@Test
	public void testGetNeededSharedLibs() throws CModelException {
		IBinary myBinary;
		String[] exelibs = { "libsocket.so.2", "libc.so.2" };
		String[] bigexelibs = { "libc.so.2" };
		String[] gotlibs;
		ExpectedStrings exp;
		int x;

		exp = new ExpectedStrings(exelibs);
		myBinary = CProjectHelper.findBinary(testProject, "test_g");
		gotlibs = myBinary.getNeededSharedLibs();
		for (x = 0; x < gotlibs.length; x++) {
			exp.foundString(gotlibs[x]);
		}
		assertTrue(exp.gotAll(), exp.getMissingString());
		assertTrue(!exp.gotExtra(), exp.getExtraString());

		exp = new ExpectedStrings(bigexelibs);
		myBinary = CProjectHelper.findBinary(testProject, "exebig_g");
		gotlibs = myBinary.getNeededSharedLibs();
		for (x = 0; x < gotlibs.length; x++) {
			exp.foundString(gotlibs[x]);
		}
		assertTrue(exp.gotAll(), exp.getMissingString());
		assertTrue(!exp.gotExtra(), exp.getExtraString());

		exp = new ExpectedStrings(bigexelibs);
		myBinary = CProjectHelper.findBinary(testProject, "libtestlib_g.so");
		gotlibs = myBinary.getNeededSharedLibs();
		for (x = 0; x < gotlibs.length; x++) {
			exp.foundString(gotlibs[x]);
		}
		assertTrue(exp.gotAll(), exp.getMissingString());
		assertTrue(!exp.gotExtra(), exp.getExtraString());

	}

	/****
	 * Simple tests for the getSoname method;
	 */
	@Test
	public void testGetSoname() throws CModelException {
		IBinary myBinary;
		String name;
		myBinary = CProjectHelper.findBinary(testProject, "test_g");
		assertTrue(myBinary.getSoname().isEmpty());

		myBinary = CProjectHelper.findBinary(testProject, "libtestlib_g.so");
		name = myBinary.getSoname();
		assertNotNull(name);
		assertTrue(name.equals("libtestlib_g.so.1"), "Expected: libtestlib_g.so.1  Got: " + name);

	}

	/***
	 * Simple tests for getText
	 */
	@Test
	public void testGetText() throws CModelException {
		IBinary bigBinary, littleBinary;
		bigBinary = CProjectHelper.findBinary(testProject, bigexe.getLocation().lastSegment());
		littleBinary = CProjectHelper.findBinary(testProject, exefile.getLocation().lastSegment());
		/* These two asserts used to fail due to pr 23602 */
		assertTrue(bigBinary.getText() == 886, "Expected  886, Got: " + bigBinary.getText());
		assertTrue(littleBinary.getText() == 1223, "Expected 1223, Got: " + littleBinary.getText());
	}

	/***
	 * Simple tests for the hadDebug call
	 */
	@Test
	public void testHasDebug() throws CModelException {
		IBinary myBinary;
		myBinary = CProjectHelper.findBinary(testProject, "test_g");
		assertTrue(myBinary.hasDebug());
		myBinary = CProjectHelper.findBinary(testProject, "libtestlib_g.so");
		assertTrue(myBinary.hasDebug());
		myBinary = CProjectHelper.findBinary(testProject, "exetest");
		assertTrue(!myBinary.hasDebug());
	}

	/***
	 * Sanity - isBinary and isReadonly should always return true;
	 */
	@Test
	public void testisBinRead() throws CModelException {
		IBinary myBinary;
		myBinary = CProjectHelper.findBinary(testProject, "test_g");
		assertTrue(myBinary != null);
		assertTrue(myBinary.isReadOnly());

	}

	/***
	 * Quick tests to make sure isObject works as expected.
	 */
	@Test
	public void testIsObject() throws CModelException {
		IBinary myBinary;
		myBinary = CProjectHelper.findObject(testProject, "exetest.o");
		assertTrue(myBinary.isObject());

		myBinary = CProjectHelper.findBinary(testProject, "test_g");
		assertTrue(!myBinary.isObject());

		myBinary = CProjectHelper.findBinary(testProject, "libtestlib_g.so");
		assertTrue(!myBinary.isObject());

		myBinary = CProjectHelper.findBinary(testProject, "exetest");
		assertTrue(!myBinary.isObject());

	}

	/***
	 * Quick tests to make sure isSharedLib works as expected.
	 */
	@Test
	public void testIsSharedLib() throws CModelException {
		IBinary myBinary;

		myBinary = CProjectHelper.findObject(testProject, "exetest.o");
		assertTrue(!myBinary.isSharedLib());

		myBinary = CProjectHelper.findBinary(testProject, "libtestlib_g.so");
		assertTrue(myBinary.isSharedLib());

		myBinary = CProjectHelper.findBinary(testProject, "test_g");
		assertTrue(!myBinary.isSharedLib());

		myBinary = CProjectHelper.findBinary(testProject, "exetest");
		assertTrue(!myBinary.isSharedLib());

	}

	/***
	 * Quick tests to make sure isExecutable works as expected.
	 */
	@Test
	public void testIsExecutable() throws InterruptedException, CModelException {
		IBinary myBinary;
		myBinary = CProjectHelper.findObject(testProject, "exetest.o");
		assertTrue(!myBinary.isExecutable());

		myBinary = CProjectHelper.findBinary(testProject, "test_g");
		assertTrue(myBinary.isExecutable());

		myBinary = CProjectHelper.findBinary(testProject, "libtestlib_g.so");
		assertTrue(!myBinary.isExecutable());

		myBinary = CProjectHelper.findBinary(testProject, "exetest");
		assertTrue(myBinary.isExecutable());

	}

	/***
	 *  Simple sanity test to make sure Binary.isBinary returns true
	 *
	 */
	@Test
	public void testIsBinary() throws CoreException, FileNotFoundException, Exception {
		IBinary myBinary;

		myBinary = CProjectHelper.findBinary(testProject, "exebig_g");
		assertTrue(myBinary != null, "A Binary");
	}

}
