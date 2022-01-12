/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.io.PrintStream;

public abstract class DbgUtilBase {
	protected int fFlags = ~0;
	protected PrintStream fOut = System.out;
	protected boolean fDbgOn;

	public void traceln(int flags, String str) {
		if (dbgOn(flags))
			doTraceln(str);
	}

	protected void doTraceln(String str) {
		fOut.println(str);
	}

	protected boolean dbgOn(int flags) {
		return fDbgOn && checkFlags(flags);
	}

	protected boolean checkFlags(int check) {
		return checkFlags(fFlags, check);
	}

	protected static boolean checkFlags(int flags, int check) {
		return (flags & check) == check;
	}
}
