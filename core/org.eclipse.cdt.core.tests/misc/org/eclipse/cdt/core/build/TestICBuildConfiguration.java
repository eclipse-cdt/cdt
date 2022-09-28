/*******************************************************************************
 * Copyright (c) 2022 Renesas Electronics Europe.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Tests for org.eclipse.cdt.core.build.ICBuildConfiguration
 */
public class TestICBuildConfiguration {

	private final static List<String> expectedBinParserIds = List.of("binParserId0", "binParserId1");
	private IToolChainManager toolchainMgr = null;

	@Before
	public void setup() {
		toolchainMgr = getService(IToolChainManager.class);
		assertNotNull("toolchainMgr must not be null", toolchainMgr);
	}

	@After
	public void shutdown() {
	}

	/**
	 * Tests that ICBuildConfiguration.getBinaryParserIds() meets API. <br>
	 * <code>
	 * List<String> getBinaryParserIds()
	 * </code>
	 */
	@Test
	public void getBinaryParserIdsTest00() throws Exception {
		IProject proj = getProject();
		IBuildConfiguration[] buildConfigs = proj.getBuildConfigs();
		assertNotNull(buildConfigs, "Must not be null");
		assertNotEquals(0, buildConfigs.length, "Must not be empty");
		IBuildConfiguration buildConfig = buildConfigs[0];
		//		ICBuildConfiguration adapter = buildConfig.getAdapter(ICBuildConfiguration.class);
		//		assertNotNull(adapter, "Must not be null");

		// TODO: rationalise this so the TC uses common in TestIToolChain too.
		// Add our test toolchain.
		Collection<IToolChain> toolChains = toolchainMgr.getAllToolChains();
		{
			// TODO: fix this. Can't use null for pathToToolChain
			IToolChain testTc = new TestToolchain(null, null, "testArch", null);
			toolchainMgr.addToolChain(testTc);
		}

		// Get our test toolchain.
		Map props = new HashMap<String, String>();
		props.put(IToolChain.ATTR_OS, "testOs");
		props.put(IToolChain.ATTR_ARCH, "testArch");
		Collection<IToolChain> testTcs = toolchainMgr.getToolChainsMatching(props);
		assertTrue("toolChains list must contain exactly 1 item", testTcs.size() == 1);
		IToolChain testTc = testTcs.iterator().next();
		assertNotNull("ourTc must not be null", testTc);

		StandardBuildConfiguration sbc = new StandardBuildConfiguration(buildConfig, "name", testTc, "run");
		assertNotNull(sbc, "Must not be null");

		sbc.getBinaryParserIds();
	}

	/**
	 * Tests that ICBuildConfiguration.getBinaryParserIds() can return a list of Binary Parser IDs.
	 */
	@Test
	public void getBinaryParserIdsTest01() throws Exception {
	}

	/**
	 * org.eclipse.cdt.internal.core.model.CModelManager.getBinaryParser(IProject)
	 */
	@Test
	public void getBinaryParserTest00() throws Exception {
	}

	//		ICBuildConfiguration cBuildConfig = null;
	//		String binParserId = cBuildConfig.getBinaryParserId();
	//		IBinary[] binaries = cBuildConfig.getBuildOutput();
	//		for (IBinary binary : binaries) {
	//			binary.exists();
	//		}

	private static <T> T getService(Class<T> serviceClass) {
		BundleContext bundleContext = FrameworkUtil.getBundle(CTestPlugin.class).getBundleContext();
		ServiceReference<T> serviceReference = bundleContext.getServiceReference(serviceClass);
		return bundleContext.getService(serviceReference);
	}

	private IProject getProject() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProjectDescription desc = root.getWorkspace().newProjectDescription("test");
		//		desc.setNatureIds(new String[] { "org.eclipse.linuxtools.tmf.project.nature" });
		IProject project = root.getProject("testProj");
		project.create(desc, new NullProgressMonitor());
		project.open(new NullProgressMonitor());
		return project;
	}

	private class TestToolchain extends GCCToolChain {

		public TestToolchain(IToolChainProvider provider, Path pathToToolChain, String arch,
				IEnvironmentVariable[] envVars) {
			super(provider, pathToToolChain, arch, envVars);
		}

		@Override
		public String getProperty(String key) {
			if (key.equals(IToolChain.ATTR_OS)) {
				return "testOs";
			} else if (key.equals(IToolChain.ATTR_ARCH)) {
				return "testArch";
			} else {
				return super.getProperty(key);
			}
		}

		@Override
		public List<String> getBinaryParserIds() {
			return expectedBinParserIds;
		}
	}

}
