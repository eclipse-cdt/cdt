/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.core.launch;

import java.io.IOException;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;

/**
 * A simple process that only spits out a message then terminates.
 *
 * @since 8.3
 */
public class NullProcess extends PlatformObject implements IProcess {

	private final String message;
	private final ILaunch launch;

	public NullProcess(ILaunch launch, String message) {
		this.launch = launch;
		this.message = message;
	}

	@Override
	public boolean canTerminate() {
		return true;
	}

	@Override
	public boolean isTerminated() {
		return true;
	}

	@Override
	public void terminate() throws DebugException {
	}

	@Override
	public String getLabel() {
		return launch.getLaunchConfiguration().getName();
	}

	@Override
	public ILaunch getLaunch() {
		return launch;
	}

	@Override
	public IStreamsProxy getStreamsProxy() {
		return new IStreamsProxy() {
			@Override
			public void write(String input) throws IOException {
				// TODO Auto-generated method stub

			}

			@Override
			public IStreamMonitor getOutputStreamMonitor() {
				return new IStreamMonitor() {
					@Override
					public void removeListener(IStreamListener listener) {
					}

					@Override
					public String getContents() {
						return message;
					}

					@Override
					public void addListener(IStreamListener listener) {
					}
				};
			}

			@Override
			public IStreamMonitor getErrorStreamMonitor() {
				return null;
			}
		};
	}

	@Override
	public void setAttribute(String key, String value) {
	}

	@Override
	public String getAttribute(String key) {
		return null;
	}

	@Override
	public int getExitValue() throws DebugException {
		return 0;
	}

}
