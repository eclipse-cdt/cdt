/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial implementation
 *     Anton Gorenkov - Need to use a process factory (Bug 210366)
 *     Marc Khouzam (Ericsson) - Support for factory to create the gdb process (Bug 210366)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb;

import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;



/**
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 4.0
 */
public interface IGdbDebugConstants {
	
    public static final String PREFIX = GdbPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

    /**
     * Attribute key to be added to the IProcess associated with an IMIContainerDMContext.
     * The value should be the groupId as returned by {@link IMIContainerDMContext#getGroupId()}
     */
    public static final String INFERIOR_GROUPID_ATTR = PREFIX + "inferiorGroupId"; //$NON-NLS-1$

    /**
     * Attribute key to be passed to DebugPlugin.newProcess to specify the type of process
     * that should be created by our IProcessFactory.
     * @since 4.1
     */
    public static final String PROCESS_TYPE_CREATION_ATTR = PREFIX + "createProcessType"; //$NON-NLS-1$

    /**
     * Attribute value of PROCESS_TYPE_CREATION_ATTR to be passed to DebugPlugin.newProcess to 
     * require the creation of an InferiorRuntimeProcess instead of a RuntimeProcess
     * (which is used by default).
     * @since 4.1
     */
    public static final String INFERIOR_PROCESS_CREATION_VALUE = PREFIX + "inferiorProcess"; //$NON-NLS-1$
    
    /**
     * Attribute value of PROCESS_TYPE_CREATION_ATTR to be passed to DebugPlugin.newProcess to 
     * require the creation of an GdbProcess instead of a RuntimeProcess
     * (which is used by default).
     * @since 4.1
     */
    public static final String GDB_PROCESS_CREATION_VALUE = PREFIX + "gdbProcess"; //$NON-NLS-1$
    

}

