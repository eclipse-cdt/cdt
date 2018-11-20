/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.scannerdiscovery;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.make.core.tests.StandardBuildTestHelper;
import org.eclipse.cdt.make.internal.core.scannerconfig2.PerProjectSICollector;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Scanner configuration discovery tests
 *
 * @author vhirsl
 */
public class ScannerConfigDiscoveryTests extends BaseTestCase {
	private IProject fCProject = null;
	private IFile fCFile = null;
	IProgressMonitor fMonitor = null;

	/**
	 * @param name
	 */
	public ScannerConfigDiscoveryTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		fMonitor = new NullProgressMonitor();

		fCProject = StandardBuildTestHelper.createProject("SCD", (IPath) null, MakeCorePlugin.MAKE_PROJECT_ID);
		fCFile = fCProject.getProject().getFile("main.c");
		if (!fCFile.exists()) {
			fCFile.create(new ByteArrayInputStream(" \n".getBytes()), false, fMonitor);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp(getName());
		StandardBuildTestHelper.removeProject("SCDC");
	}

	public void testGetCCompilerBuiltins() throws CoreException {
		MakeProjectNature.addNature(fCProject, fMonitor);
		ScannerConfigNature.addScannerConfigNature(fCProject);

		PerProjectSICollector.calculateCompilerBuiltins(fCProject);
		IScannerInfo scInfo = CCorePlugin.getDefault().getScannerInfoProvider(fCProject).getScannerInformation(fCFile);
		assertNotNull(scInfo);
		String[] includes = scInfo.getIncludePaths();
		assertTrue(includes.length == 0);
		Map<String, String> symbols = scInfo.getDefinedSymbols();
		assertTrue(symbols.isEmpty());
	}

	public void testGetCCCompilerBuiltins() throws CoreException {
		CCProjectNature.addCCNature(fCProject, fMonitor);
		MakeProjectNature.addNature(fCProject, fMonitor);
		ScannerConfigNature.addScannerConfigNature(fCProject);

		PerProjectSICollector.calculateCompilerBuiltins(fCProject);
		IScannerInfo scInfo = CCorePlugin.getDefault().getScannerInfoProvider(fCProject).getScannerInformation(fCFile);
		assertNotNull(scInfo);
		String[] includes = scInfo.getIncludePaths();
		assertTrue(includes.length == 0);
		Map<String, String> symbols = scInfo.getDefinedSymbols();
		assertTrue(symbols.isEmpty());
	}

}
