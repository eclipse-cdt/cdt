/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Ericsson   - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;

/**
 * A special launch delegate for the attach debug session, which
 * supports the launch when the project and/or binary is not specified.
 * @since 4.0
 */
@ThreadSafe
public class GdbAttachLaunchDelegate extends GdbLaunchDelegate {
	public GdbAttachLaunchDelegate() {
		// For an attach session, we don't require a project
		// to be specified
		super(false);
	}
}
