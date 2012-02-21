/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.externaltool;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.codan.core.externaltool.AbstractOutputParser;
import org.eclipse.cdt.codan.core.externaltool.IConsolePrinter;
import org.eclipse.cdt.codan.core.externaltool.IConsolePrinterProvider;
import org.eclipse.cdt.codan.core.externaltool.InvocationFailure;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Tests for <code>{@link CommandLauncher}</code>.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
@SuppressWarnings("nls")
public class CommandLauncherTest extends TestCase {
	private String externalToolName;
	private IPath executablePath;
	private String[] args;
	private boolean shouldDisplayOutput;
	private String command;
	private IPath workingDirectory;
	private ConsolePrinterProviderStub consolePrinterProvider;
	private String[] externalToolOutput;
	private ProcessInvokerStub processInvoker;
	private OutputParserStub outputParser;
	private List<AbstractOutputParser> outputParsers;

	private CommandLauncher commandLauncher;

	@Override
	protected void setUp() throws Exception {
		externalToolName = "TestTool";
		executablePath = new Path("/usr/local/testtool");
		args = new String[] { "--include=all", "--debug=true" };
		shouldDisplayOutput = true;
		command = "/usr/local/testtool --include=all --debug=true";
		workingDirectory = new Path("/usr/local/project");
		consolePrinterProvider = new ConsolePrinterProviderStub();
		externalToolOutput = new String[] { "line1", "line2" };
		processInvoker = new ProcessInvokerStub(externalToolOutput);
		outputParser = new OutputParserStub();
		outputParsers = new ArrayList<AbstractOutputParser>();
		outputParsers.add(outputParser);
		commandLauncher = new CommandLauncher(consolePrinterProvider, processInvoker);
	}

	public void testInvokesProcessCorrectly() throws Throwable {
		commandLauncher.buildAndLaunchCommand(externalToolName, executablePath, args,
				workingDirectory, shouldDisplayOutput, outputParsers);
		consolePrinterProvider.assertThatReceivedExternalToolName(externalToolName);
		consolePrinterProvider.assertThatReceivedShouldDisplayOutputFlag(shouldDisplayOutput);
		consolePrinterProvider.consolePrinter.assertThatPrinted(command, externalToolOutput);
		consolePrinterProvider.consolePrinter.assertThatIsClosed();
		processInvoker.assertThatReceivedCommand(command);
		processInvoker.assertThatReceivedWorkingDirectory(workingDirectory);
		processInvoker.process.assertThatIsDestroyed();
		outputParser.assertThatParsed(externalToolOutput);
	}

	private static class ConsolePrinterProviderStub implements IConsolePrinterProvider {
		private String externalToolName;
		private boolean shouldDisplayOutput;

		final ConsolePrinterStub consolePrinter = new ConsolePrinterStub();

		@Override
		public ConsolePrinterStub createConsole(String externalToolName, boolean shouldDisplayOutput) {
			this.externalToolName = externalToolName;
			this.shouldDisplayOutput = shouldDisplayOutput;
			return consolePrinter;
		}

		void assertThatReceivedExternalToolName(String expected) {
			assertEquals(expected, externalToolName);
		}

		void assertThatReceivedShouldDisplayOutputFlag(boolean expected) {
			assertEquals(expected, shouldDisplayOutput);
		}
	}

	private static class ConsolePrinterStub implements IConsolePrinter {
		private final List<String> printed = new ArrayList<String>();
		private boolean closed;

		@Override
		public void clear() {
			printed.clear();
		}

		@Override
		public void println(String message) {
			printed.add(message);
		}

		@Override
		public void println() {}

		@Override
		public void close() {
			closed = true;
		}

		void assertThatPrinted(String command, String[] externalToolOutput) {
			List<String> expected = new ArrayList<String>();
			expected.add(command);
			expected.addAll(asList(externalToolOutput));
			assertEquals(expected, printed);
		}

		void assertThatIsClosed() {
			assertTrue(closed);
		}
	}

	private static class ProcessInvokerStub extends ProcessInvoker {
		final ProcessStub process;

		private String command;
		private IPath workingDirectory;

		ProcessInvokerStub(String[] externalToolOutput) {
			process = new ProcessStub(externalToolOutput);
		}

		@Override
		public ProcessStub invoke(String command, IPath workingDirectory) throws InvocationFailure {
			this.command = command;
			this.workingDirectory = workingDirectory;
			return process;
		}

		void assertThatReceivedCommand(String expected) {
			assertEquals(expected, command);
		}

		void assertThatReceivedWorkingDirectory(IPath expected) {
			assertSame(expected, workingDirectory);
		}
	}

	private static class ProcessStub extends Process {
		private static final String LINE_SEPARATOR = System.getProperty("line.separator");

		private final InputStream inputStream;
		private final InputStream errorStream;

		private boolean destroyed;

		ProcessStub(String[] externalToolOutput) {
			StringBuilder builder = new StringBuilder();
			for (String s : externalToolOutput) {
				builder.append(s).append(LINE_SEPARATOR);
			}
			inputStream = new ByteArrayInputStream(builder.toString().getBytes());
			errorStream = new ByteArrayInputStream(new byte[0]);
		}

		@Override
		public OutputStream getOutputStream() {
			throw new UnsupportedOperationException();
		}

		@Override
		public InputStream getInputStream() {
			return inputStream;
		}

		@Override
		public InputStream getErrorStream() {
			return errorStream;
		}

		@Override
		public int waitFor() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int exitValue() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void destroy() {
			destroyed = true;
		}

		void assertThatIsDestroyed() {
			assertTrue(destroyed);
		}
	}

	private static class OutputParserStub extends AbstractOutputParser {
		private final List<String> parsed = new ArrayList<String>();

		@Override
		public boolean parse(String line) throws InvocationFailure {
			parsed.add(line);
			return true;
		}

		@Override
		public void reset() {
			throw new UnsupportedOperationException();
		}

		void assertThatParsed(String[] expected) {
			assertArrayEquals(expected, parsed.toArray());
		}
	}
}
