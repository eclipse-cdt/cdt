/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.scannerdiscovery;

import java.io.File;
import java.util.List;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCPerFileBOPConsoleParser;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;

public class GCCPerFileBOPConsoleParserTests extends BaseBOPConsoleParserTests {
	private final static IMarkerGenerator MARKER_GENERATOR= new IMarkerGenerator() {
		@Override
		public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
		}
		@Override
		public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
		}
	};

	public static TestSuite suite() {
		return suite(GCCPerFileBOPConsoleParserTests.class);
	}

	private ICProject fCProject;

	public GCCPerFileBOPConsoleParserTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fCProject= CProjectHelper.createCCProject("perfilescdtest", null);
		fOutputParser= new GCCPerFileBOPConsoleParser();
		final IProject project = fCProject.getProject();
		fOutputParser.startup(project, project.getLocation(), fCollector, MARKER_GENERATOR);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (fOutputParser != null) {
			fOutputParser.shutdown();
		}
		if (fCProject != null) {
			CProjectHelper.delete(fCProject);
		}
	}

	public void testParsingIfStatement_bug197930() throws Exception {
		fOutputParser.processLine("if gcc -g -O0 -I\"include abc\" -c impl/testmath.c; then ; fi"); //$NON-NLS-1$

        List<?> cmds = fCollector.getCollectedScannerInfo(null, ScannerInfoTypes.COMPILER_COMMAND);
        assertEquals(1, cmds.size());
        CCommandDSC command= (CCommandDSC) cmds.get(0);
        assertEquals("gcc", command.getCompilerName());
	}

	public void testResolvingLinkedFolder_Bug213690() throws Exception {
		File tempRoot= new File(System.getProperty("java.io.tmpdir"));
		File tempDir= new File(tempRoot, "cdttest_213690").getCanonicalFile();
		tempDir.mkdir();
		try {
			IFolder linkedFolder= fCProject.getProject().getFolder("cdttest");
			linkedFolder.createLink(new Path(tempDir.toString()), IResource.ALLOW_MISSING_LOCAL, null);
			fOutputParser.processLine("gcc -g -O0 -I\""+ tempDir.toString() + "\" -c test.c"); //$NON-NLS-1$
	        List<?> cmds = fCollector.getCollectedScannerInfo(null, ScannerInfoTypes.COMPILER_COMMAND);
	        assertEquals(1, cmds.size());
	        CCommandDSC command= (CCommandDSC) cmds.get(0);
	        List<?> includes= command.getIncludes();
	        assertEquals(1, includes.size());
	        assertEquals(tempDir.toString(), includes.get(0).toString());
		} finally {
			tempDir.delete();
		}
	}

	public void testResolvingLinkedResourceArgument_Bug216945() throws Exception {
		File tempRoot= new File(System.getProperty("java.io.tmpdir"));
		File tempDir= new File(tempRoot, "cdttest_216945");
		tempDir.mkdir();
		File tempFile= null;
		try {
			tempFile= new File(tempDir, "test.c");
			tempFile.createNewFile();
			IFolder linkedFolder= fCProject.getProject().getFolder("cdttest");
			linkedFolder.createLink(new Path(tempDir.toString()), IResource.ALLOW_MISSING_LOCAL, null);
			fOutputParser.processLine("gcc -g -O0 -c \""+ tempFile.toString() + "\""); //$NON-NLS-1$
			IFile file= linkedFolder.getFile("test.c");
	        List<?> cmds = fCollector.getCollectedScannerInfo(file, ScannerInfoTypes.COMPILER_COMMAND);
	        assertEquals(1, cmds.size());
		} finally {
			if (tempFile != null) {
				tempFile.delete();
			}
			tempDir.delete();
		}
	}

	public void testPwdInFilePath_Bug237958() throws Exception {
		IFile file1= fCProject.getProject().getFile("Bug237958_1.c");
		IFile file2= fCProject.getProject().getFile("Bug237958_2.c");
		fOutputParser.processLine("gcc -g -DTEST1 -c `pwd`/Bug237958_1.c");
		fOutputParser.processLine("gcc -DTEST2=12 -g -ggdb -Wall -c \"`pwd`/./Bug237958_2.c\"");

		List<?> cmds = fCollector.getCollectedScannerInfo(file1, ScannerInfoTypes.COMPILER_COMMAND);
		CCommandDSC cdsc= (CCommandDSC) cmds.get(0);
		List<?> symbols= cdsc.getSymbols();
		assertEquals(1, symbols.size());
		assertEquals("TEST1=1", symbols.get(0).toString());

		cmds = fCollector.getCollectedScannerInfo(file2, ScannerInfoTypes.COMPILER_COMMAND);
		cdsc= (CCommandDSC) cmds.get(0);
		symbols= cdsc.getSymbols();
		assertEquals(1, symbols.size());
		assertEquals("TEST2=12", symbols.get(0).toString());
	}
}
