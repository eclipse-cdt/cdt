/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
