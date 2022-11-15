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
package org.eclipse.cdt.build.gcc.core.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Tests for org.eclipse.cdt.core.build.IToolChain
 */
public class TestIToolChain {
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
	 * Tests that IToolChain.getBinaryParserIds() meets API. <br>
	 * <code>
	 * List<String> getBinaryParserIds()
	 * </code>
	 */
	@Test
	public void getBinaryParserIdsTest00() throws Exception {
		Collection<IToolChain> toolChains = toolchainMgr.getAllToolChains();
		assertNotNull("toolChains list must not be null", toolChains);
		assertTrue("toolChains list must contain at 1 items", !toolChains.isEmpty());
		IToolChain tc = toolChains.iterator().next();
		List<String> ids = tc.getBinaryParserIds();
		assertNotNull("IToolChain.getBinaryParserIds() must return a list", ids);
	}

	/**
	 * Tests that IToolChain.getBinaryParserIds() can return a list of Binary Parser
	 * IDs.
	 */
	@Test
	public void getBinaryParserIdsTest01() throws Exception {
		// Add our test toolchain.
		{
			IToolChain testTc = new TestToolchain(null, null, "testArch", null);
			toolchainMgr.addToolChain(testTc);
		}

		// Get our test toolchain.
		Map<String, String> props = new HashMap<>();
		props.put(IToolChain.ATTR_OS, "testOs");
		props.put(IToolChain.ATTR_ARCH, "testArch");
		Collection<IToolChain> testTcs = toolchainMgr.getToolChainsMatching(props);
		assertTrue("toolChains list must contain exactly 1 item", testTcs.size() == 1);
		IToolChain testTc = testTcs.iterator().next();
		assertNotNull("ourTc must not be null", testTc);

		// Check our test toolchain returns multiple binary parsers
		List<String> actualBinParserIds = testTc.getBinaryParserIds();
		assertArrayEquals("Binary Parser Ids must match", expectedBinParserIds.toArray(new String[0]),
				actualBinParserIds.toArray(new String[0]));
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

	private static <T> T getService(Class<T> serviceClass) {
		BundleContext bundleContext = FrameworkUtil.getBundle(TestIToolChain.class).getBundleContext();
		ServiceReference<T> serviceReference = bundleContext.getServiceReference(serviceClass);
		return bundleContext.getService(serviceReference);
	}
}
