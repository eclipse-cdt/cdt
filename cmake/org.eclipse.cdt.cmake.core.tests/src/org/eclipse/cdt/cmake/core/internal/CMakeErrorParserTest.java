/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.core.internal;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Objects;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * @author Martin Weber
 *
 */
class CMakeErrorParserTest {

	@SuppressWarnings("resource")
	@Test
	void testCtorNPE() {
		try {
			new CMakeErrorParser(null);
			Assert.fail("NullPointerException expected");
		} catch (NullPointerException expected) {
		}
	}

	/**
	 * Test method for {@link org.eclipse.cdt.cmake.core.internal.CMakeErrorParser#addInput(java.lang.CharSequence)}.
	 * Just tests whether the call survives.
	 */
	@Test
	void testAddInput() {
		ICMakeExecutionMarkerFactory f = (m, s, fi, a) -> {
		};
		try (CMakeErrorParser testee = new CMakeErrorParser(f)) {
			testee.addInput("c f g b\n blah");
		}
	}

	/**
	 * Test method for {@link org.eclipse.cdt.cmake.core.internal.CMakeErrorParser#close()}.
	 * Just tests whether the call survives.
	 */
	@Test
	void testClose() {
		ICMakeExecutionMarkerFactory f = (m, s, fi, a) -> {
		};
		CMakeErrorParser testee = new CMakeErrorParser(f);
		testee.close();
	}

	/**
	 * test "CMake Error"
	 */
	@Test
	void testError() {
		String msgStart = "CMake Error";
		String fmt = "%1$s%2$s\n" + "-- ignored lkjdfflgjd an" + "\n%1$s%2$s\n";
		String fmt2 = "\n%1$s%2$s\n" + "\n%1$s%2$s" + "\n%1$s%2$s";
		String msgContent = " at xyz:123\n some message";
		String input = String.format(fmt, msgStart, msgContent);

		CountingCMakeExecutionMarkerFactory f = new CountingCMakeExecutionMarkerFactory();
		try (CMakeErrorParser testee = new CMakeErrorParser(f)) {
			testee.addInput("ignored leading\njunk adlasdj\n");
			testee.addInput(input);
			testee.addInput(input);
			testee.addInput(String.format(fmt2, msgStart, msgContent));
			testee.addInput("-- ignored lkjdfflgjd an");
		}
		assertEquals(7, f.markerCnt);
	}

	/**
	 * test "CMake Error (dev)"
	 */
	@Test
	void testErrorDev() {
		String msgStart = "CMake Error (dev)";
		String fmt = "%1$s%2$s\n" + "-- ignored lkjdfflgjd an" + "\n%1$s%2$s\n";
		String fmt2 = "\n%1$s%2$s\n" + "\n%1$s%2$s" + "\n%1$s%2$s";
		String msgContent = " at xyz:123\n some message";
		String input = String.format(fmt, msgStart, msgContent);

		CountingCMakeExecutionMarkerFactory f = new CountingCMakeExecutionMarkerFactory();
		try (CMakeErrorParser testee = new CMakeErrorParser(f)) {
			testee.addInput("ignored leading\njunk adlasdj\n");
			testee.addInput(input);
			testee.addInput(input);
			testee.addInput(String.format(fmt2, msgStart, msgContent));
			testee.addInput("-- ignored lkjdfflgjd an");
		}
		assertEquals(7, f.markerCnt);
	}

	/**
	 * test "CMake Internal Error (please report a bug)"
	 */
	@Test
	void testInternalError() {
		String msgStart = "CMake Internal Error (please report a bug)";
		String fmt = "%1$s%2$s\n" + "-- ignored lkjdfflgjd an" + "\n%1$s%2$s\n";
		String fmt2 = "\n%1$s%2$s\n" + "\n%1$s%2$s" + "\n%1$s%2$s";
		String msgContent = " at xyz:123\n some message";
		String input = String.format(fmt, msgStart, msgContent);

		CountingCMakeExecutionMarkerFactory f = new CountingCMakeExecutionMarkerFactory();
		try (CMakeErrorParser testee = new CMakeErrorParser(f)) {
			testee.addInput("ignored leading\njunk adlasdj\n");
			testee.addInput(input);
			testee.addInput(input);
			testee.addInput(String.format(fmt2, msgStart, msgContent));
			testee.addInput("-- ignored lkjdfflgjd an");
		}
		assertEquals(7, f.markerCnt);
	}

	/**
	 * test "CMake Deprecation Error"
	 */
	@Test
	void testDeprecationError() {
		String msgStart = "CMake Deprecation Error";
		String fmt = "%1$s%2$s\n" + "-- ignored lkjdfflgjd an" + "\n%1$s%2$s\n";
		String fmt2 = "\n%1$s%2$s\n" + "\n%1$s%2$s" + "\n%1$s%2$s";
		String msgContent = " at xyz:123\n some message";
		String input = String.format(fmt, msgStart, msgContent);

		CountingCMakeExecutionMarkerFactory f = new CountingCMakeExecutionMarkerFactory();
		try (CMakeErrorParser testee = new CMakeErrorParser(f)) {
			testee.addInput("ignored leading\njunk adlasdj\n");
			testee.addInput(input);
			testee.addInput(input);
			testee.addInput(String.format(fmt2, msgStart, msgContent));
			testee.addInput("-- ignored lkjdfflgjd an");
		}
		assertEquals(7, f.markerCnt);
	}

	/**
	 * test "CMake Deprecation Warning"
	 */
	@Test
	void testDeprecationWarning() {
		String msgStart = "CMake Deprecation Warning";
		String fmt = "%1$s%2$s\n" + "-- ignored lkjdfflgjd an" + "\n%1$s%2$s\n";
		String fmt2 = "\n%1$s%2$s\n" + "\n%1$s%2$s" + "\n%1$s%2$s";
		String msgContent = " at xyz:123\n some message";
		String input = String.format(fmt, msgStart, msgContent);

		CountingCMakeExecutionMarkerFactory f = new CountingCMakeExecutionMarkerFactory();
		try (CMakeErrorParser testee = new CMakeErrorParser(f)) {
			testee.addInput("ignored leading\njunk adlasdj\n");
			testee.addInput(input);
			testee.addInput(input);
			testee.addInput(String.format(fmt2, msgStart, msgContent));
			testee.addInput("-- ignored lkjdfflgjd an");
		}
		assertEquals(7, f.markerCnt);
	}

	/**
	 * test "CMake Warning"
	 */
	@Test
	void testCMakeWarning() {
		String msgStart = "CMake Warning";
		String fmt = "%1$s%2$s\n" + "-- ignored lkjdfflgjd an" + "\n%1$s%2$s\n";
		String fmt2 = "\n%1$s%2$s\n" + "\n%1$s%2$s" + "\n%1$s%2$s";
		String msgContent = " at xyz:123\n some message";
		String input = String.format(fmt, msgStart, msgContent);

		CountingCMakeExecutionMarkerFactory f = new CountingCMakeExecutionMarkerFactory();
		try (CMakeErrorParser testee = new CMakeErrorParser(f)) {
			testee.addInput("ignored leading\njunk adlasdj\n");
			testee.addInput(input);
			testee.addInput(input);
			testee.addInput(String.format(fmt2, msgStart, msgContent));
			testee.addInput("-- ignored lkjdfflgjd an");
		}
		assertEquals(7, f.markerCnt);
	}

	/**
	 * test "CMake Warning (dev)"
	 */
	@Test
	void testCMakeWarningDev() {
		String msgStart = "CMake Warning (dev)";
		String fmt = "%1$s%2$s\n" + "-- ignored lkjdfflgjd an" + "\n%1$s%2$s\n";
		String fmt2 = "\n%1$s%2$s\n" + "\n%1$s%2$s" + "\n%1$s%2$s";
		String msgContent = " at xyz:123\n some message";
		String input = String.format(fmt, msgStart, msgContent);

		CountingCMakeExecutionMarkerFactory f = new CountingCMakeExecutionMarkerFactory();
		try (CMakeErrorParser testee = new CMakeErrorParser(f)) {
			testee.addInput("ignored leading\njunk adlasdj\n");
			testee.addInput(input);
			testee.addInput(input);
			testee.addInput(String.format(fmt2, msgStart, msgContent));
			testee.addInput("-- ignored lkjdfflgjd an");
		}
		assertEquals(7, f.markerCnt);
	}

	/**
	 * Tests whether the file name and line number are extracted from the message.
	 */
	@Test
	void testFilenameLineNo() {
		final String msgStart = "CMake Warning";
		final String filename = "FileWithError.ext";
		final Integer lineNumber = Integer.valueOf(505);
		{
			String fmt = "%1$s at %2$s:%3$d Have\n some message text\n until here";
			HasFilenameAndLineNumberCEMFactory f = new HasFilenameAndLineNumberCEMFactory();
			try (CMakeErrorParser testee = new CMakeErrorParser(f)) {
				String input = String.format(fmt, msgStart, filename, lineNumber);
				testee.addInput(input);
			}
			assertEquals(1, f.markerCnt);
			assertEquals(filename, f.filePath);
			assertEquals(lineNumber, f.lineNumber);
		}
		{
			String fmt = "%1$s: Error in cmake code at %2$s:%3$d Have\n some message text\n until here";
			HasFilenameAndLineNumberCEMFactory f = new HasFilenameAndLineNumberCEMFactory();
			try (CMakeErrorParser testee = new CMakeErrorParser(f)) {
				String input = String.format(fmt, msgStart, filename, lineNumber);
				testee.addInput(input);
			}
			assertEquals(1, f.markerCnt);
			assertEquals(filename, f.filePath);
			assertEquals(lineNumber, f.lineNumber);
		}
		{
			String fmt = "%1$s at %2$s:%3$d Have\n some message text\n until here";
			HasFilenameAndLineNumberCEMFactory f = new HasFilenameAndLineNumberCEMFactory();
			try (CMakeErrorParser testee = new CMakeErrorParser(f)) {
				String input = String.format(fmt, msgStart, filename, lineNumber);
				testee.addInput(input);
			}
			assertEquals(1, f.markerCnt);
			assertEquals(filename, f.filePath);
			assertEquals(lineNumber, f.lineNumber);
		}
		{
			String fmt = "%1$s in %2$s:%3$d Have\n some message text\n until here";
			HasFilenameAndLineNumberCEMFactory f = new HasFilenameAndLineNumberCEMFactory();
			try (CMakeErrorParser testee = new CMakeErrorParser(f)) {
				String input = String.format(fmt, msgStart, filename, lineNumber);
				testee.addInput(input);
			}
			assertEquals(1, f.markerCnt);
			assertEquals(filename, f.filePath);
			assertEquals(lineNumber, f.lineNumber);
		}
	}

	/**
	 * Tests whether the file name is extracted from the message.
	 */
	@Test
	void testFilename() {
		final String msgStart = "CMake Warning";
		final String filename = "FileWithError.ext";
		{
			String fmt = "%1$s in %2$s: Have\n some message text\n until here";
			HasFilenameAndLineNumberCEMFactory f = new HasFilenameAndLineNumberCEMFactory();
			try (CMakeErrorParser testee = new CMakeErrorParser(f)) {
				String input = String.format(fmt, msgStart, filename);
				testee.addInput(input);
			}
			assertEquals(1, f.markerCnt);
			assertEquals(filename, f.filePath);
		}
	}

	/**
	 * Tests without the file name.
	 */
	@Test
	void testNoFilename() {
		final String msgStart = "CMake Warning";
		final String message = " Have\\n some message text\\n until here";
		{
			String fmt = "%1$s:%2$s";
			HasMessageCEMFactory f = new HasMessageCEMFactory();
			try (CMakeErrorParser testee = new CMakeErrorParser(f)) {
				String input = String.format(fmt, msgStart, message);
				testee.addInput(input);
			}
			assertEquals(1, f.markerCnt);
			assertTrue("mgsStart", f.message.startsWith(msgStart));
			assertTrue("message", f.message.endsWith(message));
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	private static class CountingCMakeExecutionMarkerFactory implements ICMakeExecutionMarkerFactory {
		int markerCnt = 0;

		@Override
		public void createMarker(String message, int severity, String filePath, Map<String, Object> mandatoryAttributes)
				throws CoreException {
			Objects.requireNonNull(message, "message");
			Objects.requireNonNull(mandatoryAttributes, "mandatoryAttributes");
			markerCnt++;
		}
	}

	private static class HasFilenameAndLineNumberCEMFactory extends CountingCMakeExecutionMarkerFactory {
		private String filePath;
		private Object lineNumber;

		@Override
		public void createMarker(String message, int severity, String filePath, Map<String, Object> mandatoryAttributes)
				throws CoreException {
			super.createMarker(message, severity, filePath, mandatoryAttributes);
			this.filePath = filePath;
			this.lineNumber = mandatoryAttributes.get(IMarker.LINE_NUMBER);
		}
	}

	private static class HasMessageCEMFactory extends CountingCMakeExecutionMarkerFactory {
		private String message;

		@Override
		public void createMarker(String message, int severity, String filePath, Map<String, Object> mandatoryAttributes)
				throws CoreException {
			super.createMarker(message, severity, filePath, mandatoryAttributes);
			this.message = message;
		}
	}
}
