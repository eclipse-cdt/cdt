/*******************************************************************************
 * Copyright (c) 2006, 2020 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.testplugin.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

import junit.framework.AssertionFailedError;

class LogMonitoring {

	private List<IStatus> statusLog;

	private ILogListener logListener;

	private CCorePlugin corePlugin;

	void start() {
		statusLog = Collections.synchronizedList(new ArrayList());
		logListener = new ILogListener() {
			@Override
			public void logging(IStatus status, String plugin) {
				if (!status.isOK() && status.getSeverity() != IStatus.INFO) {
					switch (status.getCode()) {
					case IResourceStatus.NOT_FOUND_LOCAL:
					case IResourceStatus.NO_LOCATION_LOCAL:
					case IResourceStatus.FAILED_READ_LOCAL:
					case IResourceStatus.RESOURCE_NOT_LOCAL:
						// Logged by the resources plugin.
						return;
					}
					statusLog.add(status);
				}
			}
		};
		corePlugin = CCorePlugin.getDefault();
		if (corePlugin != null) { // Iff we don't run as a JUnit Plugin Test.
			corePlugin.getLog().addLogListener(logListener);
		}

	}

	void stop(int expectedLoggedNonOK) {
		if (statusLog.size() != expectedLoggedNonOK) {
			StringBuilder msg = new StringBuilder("Expected number (").append(expectedLoggedNonOK).append(") of ");
			msg.append("Non-OK status objects in log differs from actual (").append(statusLog.size()).append(").\n");
			Throwable cause = null;
			if (!statusLog.isEmpty()) {
				synchronized (statusLog) {
					for (IStatus status : statusLog) {
						IStatus[] ss = { status };
						ss = status instanceof MultiStatus ? ((MultiStatus) status).getChildren() : ss;
						for (IStatus s : ss) {
							msg.append('\t').append(s.getMessage()).append(' ');

							Throwable t = s.getException();
							cause = cause != null ? cause : t;
							if (t != null) {
								msg.append(t.getMessage() != null ? t.getMessage() : t.getClass().getCanonicalName());
							}

							msg.append("\n");
						}
					}
				}
			}
			AssertionFailedError afe = new AssertionFailedError(msg.toString());
			afe.initCause(cause);
			throw afe;
		}
		if (corePlugin != null) {
			corePlugin.getLog().removeLogListener(logListener);
		}
	}
}