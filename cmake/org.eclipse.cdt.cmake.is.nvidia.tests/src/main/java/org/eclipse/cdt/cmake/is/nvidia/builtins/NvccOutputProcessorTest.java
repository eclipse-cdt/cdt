/*******************************************************************************
 * Copyright (c) 2018-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.nvidia.builtins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.cdt.cmake.is.core.participant.IRawIndexerInfoCollector;
import org.eclipse.cdt.cmake.is.core.participant.builtins.GccOutputProcessor;
import org.eclipse.cdt.cmake.is.core.participant.builtins.OutputSniffer;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Weber
 *
 */
public class NvccOutputProcessorTest {

	private GccOutputProcessor testee;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testee = new GccOutputProcessor();
	}

	@Test
	public void testProcessFile() throws IOException {
		// pass resource content line-wise to the testee...
		RawIndexerInfoMock pc = new RawIndexerInfoMock();
		try (InputStream is = getClass().getResourceAsStream("cbd-nvcc.output.txt");
				OutputSniffer os = new OutputSniffer(testee, null, pc)) {
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		}

		assertEquals("# include paths", 6, pc.systemIncludePaths.size());
		assertEquals("# macros", 242, pc.defines.size());

		// check __CUDACC_VER_BUILD__
		assertTrue("__CUDACC_VER_BUILD__", pc.defines.containsKey("__GNUC__"));
		assertEquals("value", "85", pc.defines.get("__CUDACC_VER_BUILD__"));
	}

	private static class RawIndexerInfoMock implements IRawIndexerInfoCollector {
		private final Map<String, String> defines = new HashMap<>();
		private final List<String> undefines = new ArrayList<>();
		private final List<String> includePaths = new ArrayList<>();
		private final List<String> systemIncludePaths = new ArrayList<>();

		@Override
		public void addDefine(String name, String value) {
			Objects.requireNonNull(name);
			value = Objects.toString(value, ""); //$NON-NLS-1$
			defines.put(name, value);
		}

		@Override
		public void addUndefine(String name) {
			Objects.requireNonNull(name);
			undefines.add(name);
		}

		@Override
		public void addIncludePath(String path) {
			Objects.requireNonNull(path);
			includePaths.add(path);
		}

		@Override
		public void addSystemIncludePath(String path) {
			Objects.requireNonNull(path);
			systemIncludePaths.add(path);
		}

		@Override
		public void addMacroFile(String path) {
			// nvcc does not have a corresponding option
		}

		@Override
		public void addIncludeFile(String path) {
			// nvcc does not have a corresponding option

		}
	}
}
