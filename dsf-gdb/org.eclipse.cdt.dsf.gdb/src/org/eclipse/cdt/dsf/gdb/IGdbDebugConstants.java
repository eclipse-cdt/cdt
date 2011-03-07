/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial implementation
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


}

