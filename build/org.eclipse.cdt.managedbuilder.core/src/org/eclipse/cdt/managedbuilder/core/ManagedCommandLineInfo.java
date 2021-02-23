/*******************************************************************************
 * Copyright (c) 2004, 2011 Intel Corporation and others.
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
public class ManagedCommandLineInfo extends org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineInfo {

	public ManagedCommandLineInfo(String commandLine, String commandLinePattern, String commandName, String flags,
			String outputFlag, String outputPrefix, String outputName, String inputResources) {
		super(commandLine, commandLinePattern, commandName, flags, outputFlag, outputPrefix, outputName,
				inputResources);
	}
}
