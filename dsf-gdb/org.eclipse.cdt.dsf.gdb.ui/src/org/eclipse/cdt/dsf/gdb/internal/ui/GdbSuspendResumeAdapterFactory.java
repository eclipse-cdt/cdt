/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson           - Updated to support Move-To-Line
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.actions.GdbMoveToLine;
import org.eclipse.cdt.dsf.gdb.internal.ui.actions.GdbResumeAtLine;
import org.eclipse.cdt.dsf.gdb.internal.ui.actions.GdbRunToLine;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ISuspendResume;

/**
 * Adapter factory for Run-To-Line, Move-To-Line
 * and Resume-At-Line
 * 
 * @since 2.1
 */
public class GdbSuspendResumeAdapterFactory implements IAdapterFactory {

    static class GdbSuspendResume implements ISuspendResume, IAdaptable {

        private final GdbRunToLine fRunToLine;
        private final GdbMoveToLine fMoveToLine;
        private final GdbResumeAtLine fResumeAtLine;
        
        GdbSuspendResume(IExecutionDMContext execCtx) {
            fRunToLine = new GdbRunToLine(execCtx);
            fMoveToLine = new GdbMoveToLine(execCtx);
            fResumeAtLine = new GdbResumeAtLine(execCtx);
        }
        
        @SuppressWarnings("rawtypes")
        public Object getAdapter(Class adapter) {
            if (adapter.isInstance(fRunToLine)) {
                return fRunToLine;
            }
            if (adapter.isInstance(fMoveToLine)) {
                return fMoveToLine;
            }
            if (adapter.isInstance(fResumeAtLine)) {
                return fResumeAtLine;
            }
            return null;
        }

        public boolean canResume() { return false; }
        public boolean canSuspend() { return false; }
        public boolean isSuspended() { return false; }
        public void resume() throws DebugException {}
        public void suspend() throws DebugException {}
    }
    
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (ISuspendResume.class.equals(adapterType)) {
            if (adaptableObject instanceof IDMVMContext) {
                IExecutionDMContext execDmc = DMContexts.getAncestorOfType(
                    ((IDMVMContext)adaptableObject).getDMContext(),
                    IExecutionDMContext.class);
            	// It only makes sense to RunToLine, MoveToLine or
                // ResumeAtLine if we are dealing with a thread, not a container
                if (execDmc != null && !(execDmc instanceof IContainerDMContext)) {
                    return new GdbSuspendResume(execDmc);
                }
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Class[] getAdapterList() {
        return new Class[] { ISuspendResume.class };
    }
}