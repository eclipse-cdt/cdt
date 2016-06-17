/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack;

/**
 * @since 5.2
 */
public interface IGDBStack extends IStack {

    /**
     * Creates a frame context.  This method is intended to be used by other MI
	 * services and sub-classes which need to create a frame context directly.
	 * <p>
	 * Sub-classes can override this method to provide custom stack frame
	 * context implementation.
	 * </p>
	 * @param execDmc Execution context that this frame is to be a child of.
	 * @param level Level of the new frame context.
	 * @return A new frame context.
	 * 
     * @since 2.9
     */
    IFrameDMContext createFrameDMContext(IExecutionDMContext execDmc, int level);
}
