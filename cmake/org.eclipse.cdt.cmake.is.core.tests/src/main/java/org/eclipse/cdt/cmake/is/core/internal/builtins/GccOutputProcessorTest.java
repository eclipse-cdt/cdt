/*******************************************************************************
 * Copyright (c) 2018 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.internal.builtins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.cmake.is.core.builtins.GccOutputProcessor;
import org.eclipse.cdt.cmake.is.core.builtins.OutputSniffer;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
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
		testee.processLine("#define AAA xyz", new ProcessingContext());
	}

	@Test
	public void testProcessFile() throws IOException {
		// pass resource content line-wise to the testee...
		ProcessingContext pc = new ProcessingContext();
		try (InputStream is = getClass().getResourceAsStream("cbd-gcc.output.txt");
				OutputSniffer os = new OutputSniffer(testee, null, pc)) {
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		}

		// check __GNUC__
		for (ICLanguageSettingEntry entry : pc.getSettingEntries()) {
			if (entry.getKind() == ICLanguageSettingEntry.MACRO) {
				if ("__GNUC__".equals(entry.getName()))
					assertEquals("value (" + entry.getName() + ")", "4", entry.getValue());
			}
		}

		int inc = 0;
		int macro = 0;
		for (ICLanguageSettingEntry entry : pc.getSettingEntries()) {
			if (entry.getKind() == ICLanguageSettingEntry.INCLUDE_PATH) {
				inc++;
				assertTrue("path", !"".equals(entry.getName()));
			} else if (entry.getKind() == ICLanguageSettingEntry.MACRO) {
				macro++;
				assertTrue("macro", !"".equals(entry.getName()));
				assertTrue("value (" + entry.getName() + ")", entry.getValue() != null);
			}
		}
		assertEquals("# include paths", 5, inc);
		assertEquals("# macros", 238, macro);
	}
}
