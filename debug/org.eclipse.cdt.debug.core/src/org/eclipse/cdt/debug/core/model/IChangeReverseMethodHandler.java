/*******************************************************************************
 * Copyright (c) 2015, 2016 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

/**
 * Handler interface for the reverse debug change method command
 *
 * @since 8.0
 */
public interface IChangeReverseMethodHandler extends IReverseToggleHandler {

	/**
	 * List of different values for the reverse debugging method.
	 */
	enum ReverseDebugMethod {
		OFF, SOFTWARE, HARDWARE, BRANCH_TRACE, PROCESSOR_TRACE, GDB_TRACE
	}

	/**
	 * Sets the value for the reverse debugging method to be used when the button is toggled.
	 */
	void setReverseDebugMethod(ReverseDebugMethod traceMethod);

	/**
	* Return the reverse debugging method currently selected.
	*
	* @param context is the currently active context in the debug view
	*/
	ReverseDebugMethod getReverseDebugMethod(Object context);

	/**
	 * Return the reverse debugging method that was selected before the
	 * currently selected one.
	 *
	 * @param context is the currently active context in the debug view
	 */
	ReverseDebugMethod getPreviousReverseDebugMethod(Object context);
}
