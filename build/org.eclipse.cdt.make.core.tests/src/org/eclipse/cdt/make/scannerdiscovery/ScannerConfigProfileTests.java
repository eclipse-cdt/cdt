/*******************************************************************************
 * Copyright (c) 2008, 2009 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.make.scannerdiscovery;

import java.io.ByteArrayInputStream;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.tests.StandardBuildTestHelper;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * This class tests ScannerConfigProfileManager
 */
public class ScannerConfigProfileTests extends BaseTestCase {
	private IProject fCProject = null;
	private IFile fCFile = null;

	@Override
	protected void setUp() throws Exception {
		fCProject = StandardBuildTestHelper.createProject("SCD", (IPath)null, MakeCorePlugin.MAKE_PROJECT_ID);
		fCFile = fCProject.getProject().getFile("main.c");
		if (!fCFile.exists()) {
			fCFile.create(new ByteArrayInputStream(" \n".getBytes()), false, new NullProgressMonitor());
		}
	}

	@Override
	protected void tearDown() throws Exception {
		StandardBuildTestHelper.removeProject("SCDC");
	}

	/**
	 * Test Basic get of scanner config profile for a project
	 */
	public void testBasicScannerConfigProfile() throws CoreException {
		// Add a scanner config profile to the project
		IScannerConfigBuilderInfo2 scProjInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(fCProject, ScannerConfigProfileManager.PER_PROJECT_PROFILE_ID);
		// Save
		scProjInfo.save();

		// Get all the settings and invert them
		boolean autoDiscovery = scProjInfo.isAutoDiscoveryEnabled();
		scProjInfo.setAutoDiscoveryEnabled(!autoDiscovery);
		boolean problemReport = scProjInfo.isProblemReportingEnabled();
		scProjInfo.setProblemReportingEnabled(!problemReport);

		boolean buildOutputParser = scProjInfo.isBuildOutputParserEnabled();
		scProjInfo.setBuildOutputParserEnabled(!buildOutputParser);
		boolean buildOutputFileAction = scProjInfo.isBuildOutputFileActionEnabled();
		scProjInfo.setBuildOutputFileActionEnabled(!buildOutputFileAction);
		String buildOutputFilePath = "dummyFile";
		scProjInfo.setBuildOutputFilePath(buildOutputFilePath);

		// Save
		scProjInfo.save();

		fCProject.close(new NullProgressMonitor());
		fCProject.open(new NullProgressMonitor());

		scProjInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(fCProject, ScannerConfigProfileManager.DEFAULT_SI_PROFILE_ID);
		// Check that the previously set items have persisted...
		Assert.isTrue(autoDiscovery != scProjInfo.isAutoDiscoveryEnabled());
		Assert.isTrue(problemReport != scProjInfo.isProblemReportingEnabled());
		Assert.isTrue(buildOutputParser != scProjInfo.isBuildOutputParserEnabled());
		Assert.isTrue(buildOutputFileAction != scProjInfo.isBuildOutputFileActionEnabled());
		Assert.isTrue(buildOutputFilePath.equals(scProjInfo.getBuildOutputFilePath()));
	}

}
