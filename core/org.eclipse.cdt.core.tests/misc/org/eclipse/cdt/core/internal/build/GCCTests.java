package org.eclipse.cdt.core.internal.build;

import java.io.IOException;

import org.eclipse.cdt.core.build.gcc.GCCToolChainFactory;
import org.junit.Test;

public class GCCTests {

	@Test
	public void tryGCCDiscovery() throws IOException {
		long start = System.currentTimeMillis();
		new GCCToolChainFactory().discover();
		System.out.println("Time: " + (System.currentTimeMillis() - start));
	}

}
