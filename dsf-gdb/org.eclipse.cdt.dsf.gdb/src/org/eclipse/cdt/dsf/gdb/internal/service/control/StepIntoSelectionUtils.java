/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.service.control;

import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.Messages;
import org.eclipse.cdt.dsf.mi.service.command.output.MIArg;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.osgi.util.NLS;

public class StepIntoSelectionUtils {

	public static final String cppSep = "::"; //$NON-NLS-1$

	public static boolean sameSignature(MIFrame currentFrame, StepIntoSelectionActiveOperation stepOperation) {
		String currentFunctionName = currentFrame.getFunction();
		String targetFunctionName = stepOperation.getTargetFunctionSignature();

		if (!sameNumberOfArgumets(currentFrame, stepOperation)) {
			return false;
		}

		// Simplified validation
		if (currentFunctionName.equals(targetFunctionName)) {
			return true;
		}

		// Check if the last segment of the function name match
		String[] currentFunctTokens = currentFunctionName.split(cppSep);
		String[] targetFunctTokens = targetFunctionName.split(cppSep);
		if (currentFunctTokens.length > 1) {
			currentFunctionName = currentFunctTokens[currentFunctTokens.length - 1];
		}

		if (targetFunctTokens.length > 1) {
			targetFunctionName = targetFunctTokens[targetFunctTokens.length - 1];
		}

		if (currentFunctionName.equals(targetFunctionName)) {
			// Function name matches and parent name does not. One of the parents may be a super class
			// Simple enough for initial implementation.
			return true;
		}

		// TODO: A more detailed check can be implemented in order to cover for parameter types return types, etc..
		// This with the intention to avoid early stops, however this implementation need to be tested extensively in
		// order to avoid missing the target due to unexpected formatting mismatch between declaration and GDB representation.

		return false;
	}

	private static boolean sameNumberOfArgumets(MIFrame currentFrame, StepIntoSelectionActiveOperation stepOperation) {
		int argSizeAdjustment = 0;
		MIArg[] args = currentFrame.getArgs();
		if (args.length > 0) {
			// GDB may add the argument "this" e.g. in c++ programs
			if (args[0].getName().equals("this")) { //$NON-NLS-1$
				argSizeAdjustment = 1;
			}
		}

		return ((currentFrame.getArgs().length - argSizeAdjustment) == stepOperation.getTargetFunctionDeclaration()
				.getNumberOfParameters());
	}

	public static void missedSelectedTarget(StepIntoSelectionActiveOperation stepOperation) {
		final String functionName = stepOperation.getTargetFunctionDeclaration().getElementName();
		IStatus status = new Status(IStatus.INFO, GdbPlugin.PLUGIN_ID, IGdbDebugConstants.STATUS_HANDLER_CODE,
				Messages.StepIntoSelection + "\n" //$NON-NLS-1$
						+ NLS.bind(Messages.StepIntoSelection_Execution_did_not_enter_function, functionName),
				null);
		IStatusHandler statusHandler = DebugPlugin.getDefault().getStatusHandler(status);
		if (statusHandler != null) {
			try {
				statusHandler.handleStatus(status, null);
			} catch (CoreException ex) {
				GdbPlugin.getDefault().getLog().log(ex.getStatus());
			}
		}
	}

}
