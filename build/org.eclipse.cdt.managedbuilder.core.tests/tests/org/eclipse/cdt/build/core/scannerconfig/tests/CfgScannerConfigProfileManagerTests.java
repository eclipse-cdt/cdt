/*******************************************************************************
 *  Copyright (c) 2009, 2013 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.core.scannerconfig.tests;

import java.util.Map;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.core.language.settings.providers.ScannerDiscoveryLegacySupport;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CfgScannerConfigProfileManagerTests extends BaseTestCase {
	IProject fProject;

	public static Test suite() {
		TestSuite suite = new TestSuite(CfgScannerConfigProfileManagerTests.class.getName());
		suite.addTestSuite(CfgScannerConfigProfileManagerTests.class);
		return suite;
	}

	@Override
	protected void setUp() throws Exception {
		fProject = ManagedBuildTestHelper.createProject("CfgScannerConfigProfileManagerProj",
				"cdt.managedbuild.target.gnu.exe");
		ManagedBuildTestHelper.addManagedBuildNature(fProject);
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp(getName());
		ManagedBuildTestHelper.removeProject(fProject.getName());
	}

	/**
	 * Basic testing of Config based ScannerConfigProfile management.
	 *
	 * This test runs through some of the funcationality used by the DiscoveryTab
	 * @throws CoreException
	 */
	public void testBasicCfgScannerConfigProfileChanges() throws CoreException {
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(fProject, false);
		ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription(fProject);
		ICConfigurationDescription[] cfgDescs = prjDesc.getConfigurations();
		assertTrue(cfgDescs.length > 0);

		IConfiguration cfg0 = ManagedBuildManager.getConfigurationForDescription(cfgDescs[0]);
		ICfgScannerConfigBuilderInfo2Set scbis = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(cfg0);

		// Test changing ICfgScannerConfigBuilderInfo2Set settings
		boolean resCfgTypeDiscovery = scbis.isPerRcTypeDiscovery();
		scbis.setPerRcTypeDiscovery(!resCfgTypeDiscovery);

		// Test changing settings on one of the ScannerConfigBuilderInfos
		Map<CfgInfoContext, IScannerConfigBuilderInfo2> infoMap = scbis.getInfoMap();
		CfgInfoContext cic = infoMap.entrySet().iterator().next().getKey();
		IScannerConfigBuilderInfo2 scbi = infoMap.entrySet().iterator().next().getValue();
		// Get all the settings and invert them
		boolean autoDiscovery = scbi.isAutoDiscoveryEnabled();
		scbi.setAutoDiscoveryEnabled(!autoDiscovery);
		boolean problemReport = scbi.isProblemReportingEnabled();
		scbi.setProblemReportingEnabled(!problemReport);
		boolean buildOutputParser = scbi.isBuildOutputParserEnabled();
		scbi.setBuildOutputParserEnabled(!buildOutputParser);
		boolean buildOutputFileAction = scbi.isBuildOutputFileActionEnabled();
		scbi.setBuildOutputFileActionEnabled(!buildOutputFileAction);
		String buildOutputFilePath = scbi.getBuildOutputFilePath();
		scbi.setBuildOutputFilePath("dummyFile");
		// Persist the changes
		scbis.applyInfo(cic, scbi);

		// Save the project description
		CoreModel.getDefault().setProjectDescription(fProject, prjDesc);
		fProject.close(null);
		fProject.open(null);

		// Check that the changes have persisted
		prjDesc = CoreModel.getDefault().getProjectDescription(fProject);
		cfg0 = ManagedBuildManager.getConfigurationForDescription(prjDesc.getConfigurations()[0]);
		scbis = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(cfg0);
		assertTrue(scbis.isPerRcTypeDiscovery() != resCfgTypeDiscovery);
		scbi = scbis.getInfo(cic);
		// Check that the changes have persisted
		Assert.isTrue(autoDiscovery != scbi.isAutoDiscoveryEnabled());
		Assert.isTrue(problemReport != scbi.isProblemReportingEnabled());
		Assert.isTrue(buildOutputParser != scbi.isBuildOutputParserEnabled());
		Assert.isTrue(buildOutputFileAction != scbi.isBuildOutputFileActionEnabled());
		Assert.isTrue("dummyFile".equals(scbi.getBuildOutputFilePath()));

		// Test restore defaults
		scbis.applyInfo(cic, null);
		// Save the project description
		CoreModel.getDefault().setProjectDescription(fProject, prjDesc);
		fProject.close(null);
		fProject.open(null);

		// Check settings are back to original
		prjDesc = CoreModel.getDefault().getProjectDescription(fProject);
		cfg0 = ManagedBuildManager.getConfigurationForDescription(prjDesc.getConfigurations()[0]);
		scbis = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(cfg0);
		scbi = scbis.getInfo(cic);
		Assert.isTrue(autoDiscovery == scbi.isAutoDiscoveryEnabled());
		Assert.isTrue(problemReport == scbi.isProblemReportingEnabled());
		Assert.isTrue(buildOutputParser == scbi.isBuildOutputParserEnabled());
		Assert.isTrue(buildOutputFileAction == scbi.isBuildOutputFileActionEnabled());
		Assert.isTrue(buildOutputFilePath.equals(buildOutputFilePath));
	}

}
