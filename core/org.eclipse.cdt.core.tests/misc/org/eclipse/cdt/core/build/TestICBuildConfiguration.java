package org.eclipse.cdt.core.build;

import org.eclipse.cdt.core.testplugin.CTestPlugin;
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

	@Before
	public void setup() {
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

}
