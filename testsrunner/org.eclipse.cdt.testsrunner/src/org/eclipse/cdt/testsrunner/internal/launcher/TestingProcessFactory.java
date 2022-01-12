/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.launcher;

import java.io.InputStream;
import java.util.Map;

import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.cdt.dsf.gdb.launching.GDBProcess;
import org.eclipse.cdt.dsf.gdb.launching.InferiorRuntimeProcess;
import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.model.TestingSession;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProviderInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.RuntimeProcess;

/**
 * Custom testing process factory allows to handle the output stream of the
 * testing process and prevent it from output to Console.
 */
public class TestingProcessFactory implements IProcessFactory {

	/**
	 * Runs data processing for the testing process and close IO stream when it
	 * is done.
	 */
	private class TestingSessionRunner implements Runnable {

		private TestingSession testingSession;
		private InputStream iStream;
		private ProcessWrapper processWrapper;

		TestingSessionRunner(TestingSession testingSession, InputStream iStream, ProcessWrapper processWrapper) {
			this.testingSession = testingSession;
			this.iStream = iStream;
			this.processWrapper = processWrapper;
		}

		@Override
		public void run() {
			try {
				testingSession.run(iStream);
			} finally {
				// Streams should be closed anyway to avoid testing process hang up
				processWrapper.allowStreamsClosing();
			}
		}
	}

	/**
	 * Creates a wrapper for the specified process to handle its input or error
	 * stream.
	 *
	 * @param launch launch
	 * @param process process to wrap
	 * @return wrapped process
	 * @throws CoreException
	 */
	private Process wrapProcess(ILaunch launch, Process process) throws CoreException {
		TestingSession testingSession = TestsRunnerPlugin.getDefault().getTestingSessionsManager().newSession(launch);
		ITestsRunnerProviderInfo testsRunnerProvider = testingSession.getTestsRunnerProviderInfo();
		InputStream iStream = testsRunnerProvider.isOutputStreamRequired() ? process.getInputStream()
				: testsRunnerProvider.isErrorStreamRequired() ? process.getErrorStream() : null;
		ProcessWrapper processWrapper = new ProcessWrapper(process, testsRunnerProvider.isOutputStreamRequired(),
				testsRunnerProvider.isErrorStreamRequired());
		Thread t = new Thread(new TestingSessionRunner(testingSession, iStream, processWrapper));
		t.start();
		return processWrapper;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public IProcess newProcess(ILaunch launch, Process process, String label, Map attributes) {

		try {
			// Mimic the behavior of DSF GDBProcessFactory.
			if (attributes != null) {
				Object processTypeCreationAttrValue = attributes.get(IGdbDebugConstants.PROCESS_TYPE_CREATION_ATTR);
				if (IGdbDebugConstants.GDB_PROCESS_CREATION_VALUE.equals(processTypeCreationAttrValue)) {
					return new GDBProcess(launch, process, label, attributes);
				}

				if (IGdbDebugConstants.INFERIOR_PROCESS_CREATION_VALUE.equals(processTypeCreationAttrValue)) {
					return new InferiorRuntimeProcess(launch, wrapProcess(launch, process), label, attributes);
				}

				// Probably, it is CDI creating a new inferior process
			} else {
				return new RuntimeProcess(launch, wrapProcess(launch, process), label, attributes);
			}

		} catch (CoreException e) {
			TestsRunnerPlugin.log(e);
		}

		return null;
	}

}
