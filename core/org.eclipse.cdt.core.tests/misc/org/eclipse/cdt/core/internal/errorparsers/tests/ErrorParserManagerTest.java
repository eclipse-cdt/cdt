/*******************************************************************************
 * Copyright (c) 2008, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.internal.errorparsers.tests;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IErrorParser2;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * @author Alena Laskavaia
 *
 * Tests for ErrorParser manager and different parsers 
 *
 */
public class ErrorParserManagerTest extends TestCase {
	IWorkspace workspace;
	IWorkspaceRoot root;

	NullProgressMonitor monitor;
	private ICProject cProject;
	private ErrorParserManager epManager;
	private ArrayList<ProblemMarkerInfo> errorList;
	private IMarkerGenerator markerGenerator;

	/**
	 * Constructor for CModelTests.
	 * @param name
	 */
	public ErrorParserManagerTest(String name) {
		super(name);
	}

	/**
	 * Sets up the test fixture.
	 *
	 * Called before every test case method.
	 * 
	 * Example code test the packages in the project 
	 *  "com.qnx.tools.ide.cdt.core"
	 */
	@Override
	protected void setUp() throws Exception {
		/***
		 * The test of the tests assume that they have a working workspace
		 * and workspace root object to use to create projects/files in, 
		 * so we need to get them setup first.
		 */
		IWorkspaceDescription desc;
		workspace = ResourcesPlugin.getWorkspace();
		root = workspace.getRoot();
		monitor = new NullProgressMonitor();
		if (workspace == null)
			fail("Workspace was not setup");
		if (root == null)
			fail("Workspace root was not setup");
		desc = workspace.getDescription();
		desc.setAutoBuilding(false);
		workspace.setDescription(desc);

		errorList = new ArrayList<ProblemMarkerInfo>();
		cProject = createProject("errorparsersanity");
		markerGenerator = new IMarkerGenerator() {

			@Override
			public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
				// Obsolete
			}

			@Override
			public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
				errorList.add(problemMarkerInfo);

			}

		};
		String[] errorParsersIds = {
			"org.eclipse.cdt.core.CWDLocator",
			"org.eclipse.cdt.core.GCCErrorParser",
			"org.eclipse.cdt.core.GASErrorParser",
			"org.eclipse.cdt.core.GLDErrorParser",
			"org.eclipse.cdt.core.VCErrorParser",
			"org.eclipse.cdt.core.GmakeErrorParser",
		};
		epManager = new ErrorParserManager(cProject.getProject(), markerGenerator, errorParsersIds);

	}

	/**
	* Tears down the test fixture.
	*
	* Called after every test case method.
	*/
	@Override
	protected void tearDown() {
		// release resources here and clean-up
	}

	public static TestSuite suite() {
		return new TestSuite(ErrorParserManagerTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	private ICProject createProject(String name) throws CoreException {
		ICProject testProject;
		testProject = CProjectHelper.createCProject(name, "none", IPDOMManager.ID_NO_INDEXER);
		if (testProject == null) {
			fail("Unable to create project");
		}
		return testProject;
	}

	private void output(String line) throws IOException {
		epManager.write(line.getBytes(), 0, line.length());
	}

	private void end() throws IOException {
		epManager.getOutputStream();
		epManager.close();
		epManager.getOutputStream().close();
	}

	public void testParsersSanity() throws CoreException, IOException {
		output("catchpoints.cpp:12: warning: no return statement in function returning non-void\n");
		end();
		assertEquals(1, errorList.size());
		
		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("no return statement in function returning non-void",problemMarkerInfo.description);
		assertEquals(new Path("catchpoints.cpp"),problemMarkerInfo.externalPath);
	}
	public void testParsersSanityTrimmed() throws CoreException, IOException {
		output("   catchpoints.cpp:12: warning: no return statement in function returning non-void   \n");
		end();
		assertEquals(1, errorList.size());
		
		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("no return statement in function returning non-void",problemMarkerInfo.description);
		assertEquals(new Path("catchpoints.cpp"),problemMarkerInfo.externalPath);
	}

	public void testOutput() throws IOException {
		FileInputStream fileInputStream = new FileInputStream(CTestPlugin.getDefault().getFileInPlugin(
				new Path("resources/errortests/output-1")));
		byte b[] = new byte[1024];
		while (true) {
			int k = fileInputStream.read(b);
			if (k < 0)
				break;
			epManager.write(b, 0, k);
		}
		end();
		assertEquals(22, errorList.size());

	}



	private String addErrorParserExtension(String shortId, Class cl) {
		String ext = "<plugin><extension id=\"" + shortId + "\" name=\"" + shortId
				+ "\" point=\"org.eclipse.cdt.core.ErrorParser\">" + "<errorparser class=\"" + cl.getName() + "\"/>"
				+ "</extension></plugin>";
		IContributor contributor = ContributorFactoryOSGi.createContributor(CTestPlugin.getDefault().getBundle());
		boolean added = Platform.getExtensionRegistry().addContribution(new ByteArrayInputStream(ext.getBytes()),
				contributor, false, shortId, null,
				((ExtensionRegistry) Platform.getExtensionRegistry()).getTemporaryUserToken());
		assertTrue("failed to add extension", added);
		String fullId = "org.eclipse.cdt.core.tests." + shortId;
		IErrorParser[] errorParser = CCorePlugin.getDefault().getErrorParser(fullId);
		assertTrue(errorParser.length > 0);
		return fullId;
	}

	public static class TestParser1 implements IErrorParser2 {
		String last = null;
		@Override
		public int getProcessLineBehaviour() {
			return KEEP_UNTRIMMED;
		}

		@Override
		public boolean processLine(String line, ErrorParserManager eoParser) {
			if (line.startsWith(" ") && last!=null) {
				eoParser.generateExternalMarker(null, 1, last+line, 1, "", null);
				return true;
			}
			if (line.startsWith("bug:")) {
				last = line;
				return true;
			} else {
				last = null;
			}

			return false;
		}
	}
	public void testNoTrimParser() throws IOException {
		String id = addErrorParserExtension("test1", TestParser1.class);
		epManager = new ErrorParserManager(cProject.getProject(), markerGenerator, new String[] { id });
		
		output("bug: start\n");
		output(" end");
		end();
		assertEquals(1, errorList.size());
		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("bug: start end",problemMarkerInfo.description);
	}
	
	public static class TestParser2 implements IErrorParser2 {
		@Override
		public int getProcessLineBehaviour() {
			return KEEP_LONGLINES;
		}

		@Override
		public boolean processLine(String line, ErrorParserManager eoParser) {
			if (line.startsWith("errorT: ")) {
				eoParser.generateExternalMarker(null, 1, line, 1, "", null);
				return true;
			}

			return false;
		}
	}
	public void testLongLinesParser() throws IOException {
		String id = addErrorParserExtension("test2", TestParser2.class);
		epManager = new ErrorParserManager(cProject.getProject(), markerGenerator, new String[] { id });
		
		StringBuffer buf = new StringBuffer("errorT: ");
		for (int i = 0; i < 100; i++) {
			buf.append("la la la la la "+i+" ");
		}
		output(buf.toString()+"\n");
		end();
		assertEquals(1, errorList.size());
		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		int l = problemMarkerInfo.description.length();
		assertTrue(l>1000);
		String end = problemMarkerInfo.description.substring(l-10,l);
		// check - line trimmed but long
		assertEquals("a la la 99",end);
	}
	public static class TestParser3 implements IErrorParser2 {
		@Override
		public int getProcessLineBehaviour() {
			return KEEP_LONGLINES | KEEP_UNTRIMMED;
		}

		@Override
		public boolean processLine(String line, ErrorParserManager eoParser) {
			if (line.startsWith("errorT: ")) {
				eoParser.generateExternalMarker(null, 1, line, 1, "", null);
				return true;
			}

			return false;
		}
	}
	public void testLongLinesUntrimmedParser() throws IOException {
		String id = addErrorParserExtension("test3", TestParser3.class);
		epManager = new ErrorParserManager(cProject.getProject(), markerGenerator, new String[] { id });
		
		StringBuffer buf = new StringBuffer("errorT: ");
		for (int i = 0; i < 100; i++) {
			buf.append("la la la la la "+i+" ");
		}
		output(buf.toString()+"\n");
		end();
		assertEquals(1, errorList.size());
		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		int l = problemMarkerInfo.description.length();
		assertTrue(l>1000);
		String end = problemMarkerInfo.description.substring(l-10,l);
		// check - line trimmed but long
		assertEquals(" la la 99 ",end);
	}

	public static class TestParser4 implements IErrorParser {
		@Override
		public boolean processLine(String line, ErrorParserManager eoParser) {
			ProblemMarkerInfo problemMarkerInfo = new ProblemMarkerInfo(null, 0, "Workspace level marker", IMarker.SEVERITY_INFO, null);
			eoParser.addProblemMarker(problemMarkerInfo);
			return true;
		}
	}
	public void testWorkspaceLevelError() throws IOException {
		String id = addErrorParserExtension("test4", TestParser4.class);
		epManager = new ErrorParserManager(null, markerGenerator, new String[] { id });
		
		StringBuffer buf = new StringBuffer("errorT: ");
		output(buf.toString()+"\n");
		end();
		assertEquals(1, errorList.size());
		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("Workspace level marker", problemMarkerInfo.description);
		assertTrue(problemMarkerInfo.file instanceof IWorkspaceRoot);
	}
	
}
