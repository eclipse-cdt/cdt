/*******************************************************************************
 * Copyright (c) 2015, 2016 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.commands.IDebugCommandHandler;

/**
 * Handler interface for the reverse debug change method command
 *
 * @since 8.0
 */
public interface IChangeReverseMethodHandler extends IReverseToggleHandler, IDebugCommandHandler {

	/**
	 * List of different values for the reverse debugging method.
	 */
    enum ReverseDebugMethod {OFF, SOFTWARE, HARDWARE, BRANCH_TRACE, PROCESSOR_TRACE, GDB_TRACE};

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
