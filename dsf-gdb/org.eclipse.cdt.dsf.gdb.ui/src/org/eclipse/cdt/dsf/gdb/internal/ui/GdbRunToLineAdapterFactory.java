/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.actions.GdbRunToLine;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ISuspendResume;

/**
 * @since 2.0
 */
public class GdbRunToLineAdapterFactory implements IAdapterFactory {

    static class GdbSuspendResume implements ISuspendResume, IAdaptable {

        private final GdbRunToLine fRunToLine;
        
        GdbSuspendResume(IExecutionDMContext execCtx) {
            fRunToLine = new GdbRunToLine(execCtx);
        }
        
        public Object getAdapter(Class adapter) {
            if (adapter.isInstance(fRunToLine)) {
                return fRunToLine;
            }
            return null;
        }

        public boolean canResume() { return false; }
        public boolean canSuspend() { return false; }
        public boolean isSuspended() { return false; }
        public void resume() throws DebugException {}
        public void suspend() throws DebugException {}
    }
    
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (ISuspendResume.class.equals(adapterType)) {
            if (adaptableObject instanceof IDMVMContext) {
                IExecutionDMContext execDmc = DMContexts.getAncestorOfType(
                    ((IDMVMContext)adaptableObject).getDMContext(),
                    IExecutionDMContext.class);
            	// It only makes sense to RunToLine if we are dealing with a thread, not a container
                if (execDmc != null && !(execDmc instanceof IContainerDMContext)) {
                    return new GdbSuspendResume(execDmc);
                }
            }
        }
        return null;
    }

    public Class[] getAdapterList() {
        return new Class[] { ISuspendResume.class };
    }
}