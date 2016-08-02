/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
