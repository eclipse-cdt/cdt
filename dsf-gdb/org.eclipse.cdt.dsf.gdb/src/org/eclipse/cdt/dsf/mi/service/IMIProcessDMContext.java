/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ercisson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;

/**
 * A process context object.  In the GDB/MI protocol, processes are represented 
 * by an string identifier, which is the basis for this context.
 * @since 1.1
 */
public interface IMIProcessDMContext extends IProcessDMContext { 
    /**
     * Returns the GDB/MI process identifier of this context.
     * @return
     */
    public String getProcId();
}
