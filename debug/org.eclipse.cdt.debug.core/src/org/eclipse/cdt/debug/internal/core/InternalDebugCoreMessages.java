/*******************************************************************************
 * Copyright (c) 2004, 2013 QNX Software Systems and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core;

import org.eclipse.osgi.util.NLS;

public class InternalDebugCoreMessages extends NLS {
	public static String CGlobalVariableManager_0;
	public static String CMemoryBlockRetrievalExtension_0;
	public static String CMemoryBlockRetrievalExtension_1;
	public static String CMemoryBlockRetrievalExtension_2;
	public static String CMemoryBlockRetrievalExtension_3;
	public static String CMemoryBlockRetrievalExtension_4;
	public static String CMemoryBlockRetrievalExtension_invalid_encoded_address;
	public static String CMemoryBlockRetrievalExtension_CDebugTarget_not_available;
	public static String DebugConfiguration_0;
	public static String CDebugAdapter_0;
	public static String CDebugAdapter_1;
	public static String CDebugAdapter_Program_file_not_specified;
	public static String CoreBuildLaunchBarTracker_Job;
	public static String CoreBuildLaunchBarTracker_NoToolchainForTarget;
	public static String CoreBuildLaunchBarTracker_TargetNotAvailable;
	public static String CoreBuildLaunchConfigDelegate_noBinaries;
	public static String CoreBuildLocalRunLaunchDelegate_ErrorLaunching;
	public static String CRegisterManager_0;
	public static String CRegisterManager_1;
	public static String StringSubstitutionEngine_undefined_variable;
	public static String StringSubstitutionEngine_unexpected_argument;

	private InternalDebugCoreMessages() {
	}

	static {
		// Load message values from a bundle file.
		NLS.initializeMessages(InternalDebugCoreMessages.class.getName(), InternalDebugCoreMessages.class);
	}

}
