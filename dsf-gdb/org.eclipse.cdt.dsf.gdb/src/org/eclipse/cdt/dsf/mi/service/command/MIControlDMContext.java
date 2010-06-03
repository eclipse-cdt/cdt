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
package org.eclipse.cdt.dsf.mi.service.command;

import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.osgi.framework.Constants;

/**
 * 
 */
public class MIControlDMContext extends AbstractDMContext 
    implements ICommandControlDMContext
{
    final static String PROP_INSTANCE_ID = GdbPlugin.PLUGIN_ID + ".miControlInstanceId";    //$NON-NLS-1$

    private final String fCommandControlFilter;
    private final String fCommandControlId;
    
    public MIControlDMContext(String sessionId, String commandControlId) {
        this(sessionId, DMContexts.EMPTY_CONTEXTS_ARRAY, commandControlId);
    }

    public MIControlDMContext(String sessionId, IDMContext[] parents, String commandControlId) {
        super(sessionId, parents);

        fCommandControlId = commandControlId;
        fCommandControlFilter = 
            "(&" +  //$NON-NLS-1$
            "(" + Constants.OBJECTCLASS + "=" + ICommandControl.class.getName() + ")" + //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            "(" + IDsfService.PROP_SESSION_ID + "=" + sessionId + ")" + //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            "(" + PROP_INSTANCE_ID + "=" + commandControlId + ")" + //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            ")"; //$NON-NLS-1$
    }

    public String getCommandControlFilter() { 
        return fCommandControlFilter;
    }

    /**
     * @since 1.1
     */
    public String getCommandControlId() {
        return fCommandControlId;
    }
    
    @Override
    public boolean equals(Object obj) {
        return baseEquals(obj) && fCommandControlId.equals(((MIControlDMContext)obj).fCommandControlId);
    }

    @Override
    public int hashCode() {
        return baseHashCode() + fCommandControlId.hashCode(); 
    }
    
    @Override
    public String toString() {
        return baseToString() + fCommandControlId;
    }
}
