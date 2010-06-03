/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;

/**
 * An execution context object.  In the GDB/MI protocol, threads are represented 
 * by an integer identifier, which is the basis for this context.  The parent of this 
 * context should always be a container context.  
 */
public interface IMIExecutionDMContext extends IExecutionDMContext 
{
    /**
     * Returns the GDB/MI thread identifier of this context.
     * @return
     */
    public int getThreadId();
}