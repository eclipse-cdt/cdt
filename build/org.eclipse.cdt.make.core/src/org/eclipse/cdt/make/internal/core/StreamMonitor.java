/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 *
 * @deprecated as of CDT 8.1. Use org.eclipse.cdt.internal.core.StreamMonitor
 *
 */
@Deprecated
public class StreamMonitor extends org.eclipse.cdt.internal.core.StreamProgressMonitor {
	public StreamMonitor(IProgressMonitor mon, OutputStream cos, int totalWork) {
		super(mon, cos, totalWork);
	}
}
