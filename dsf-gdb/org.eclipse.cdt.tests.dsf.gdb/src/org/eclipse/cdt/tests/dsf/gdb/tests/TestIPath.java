package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

public class TestIPath {

	@Test
	public void testIPath() {
		// Really isValidPath is a static method that is declared on the interface so
		// you need to access it via an instance.
		IPath path = new Path("/this/is/unused/when/doing/isValidPath");
		assertTrue(path.isValidPath(""));
		assertTrue(path.isValidPath("//"));
		assertTrue(path.isValidPath("\\"));
		assertTrue(path.isValidPath(" "));
	}

}
