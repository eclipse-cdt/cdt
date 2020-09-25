/*******************************************************************************
 * Copyright (c) 2018-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.internal.builtins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.cmake.is.core.participant.builtins.GccOutputProcessor;
import org.eclipse.cdt.cmake.is.core.participant.builtins.OutputSniffer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Martin Weber
 *
 */
public class GccOutputProcessorTest {

	private GccOutputProcessor testee;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testee = new GccOutputProcessor();
	}

	@Test
	@Ignore
	public void testProcessLine() {
		testee.processLine("#define AAA xyz", new RawIndexerInfo());
	}

	@Test
	public void testProcessFile() throws IOException {
		// pass resource content line-wise to the testee...
		RawIndexerInfo pc = new RawIndexerInfo();
		try (InputStream is = getClass().getResourceAsStream("cbd-gcc.output.txt");
				OutputSniffer os = new OutputSniffer(testee, null, pc)) {
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		}

		assertEquals("# include paths", 5, pc.getSystemIncludePaths().size());
		assertEquals("# macros", 238, pc.getDefines().size());

		// check __GNUC__
		assertTrue("__GNUC__", pc.getDefines().containsKey("__GNUC__"));
		assertNotNull("value", pc.getDefines().get("__GNUC__"));
	}
}
