/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core.internal.launching;

/**
 * LLDB launch delegate for attaching.
 */
public class LLDBAttachLaunchDelegate extends LLDBLaunchDelegate {

	/**
	 * Creates the launch delegate.
	 */
	public LLDBAttachLaunchDelegate() {
		// For an attach session, we don't require a project
		// to be specified
		super(false);
	}
}
