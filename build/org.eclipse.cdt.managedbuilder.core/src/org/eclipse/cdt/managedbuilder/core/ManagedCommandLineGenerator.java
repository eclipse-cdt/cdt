/*******************************************************************************
 * Copyright (c) 2004, 2016 Intel Corporation and others.
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
package org.eclipse.cdt.managedbuilder.core;

/**
 * @since 9.2
 */
@SuppressWarnings("deprecation")
public class ManagedCommandLineGenerator
		extends org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator {

	private static ManagedCommandLineGenerator cmdLineGen;

	protected ManagedCommandLineGenerator() {
		cmdLineGen = null;
	}

	public static ManagedCommandLineGenerator getCommandLineGenerator() {
		if (cmdLineGen == null) {
			cmdLineGen = new ManagedCommandLineGenerator();
		}
		return cmdLineGen;
	}
}
