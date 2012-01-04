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
package org.eclipse.cdt.dsf.debug.internal.ui;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.internal.ui.actions.MoveToLine;
import org.eclipse.cdt.dsf.debug.internal.ui.actions.ResumeAtLine;
import org.eclipse.cdt.dsf.debug.internal.ui.actions.RunToLine;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
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
public class SuspendResumeAdapterFactory implements IAdapterFactory {

    static class SuspendResume implements ISuspendResume, IAdaptable {

        private final RunToLine fRunToLine;
        private final MoveToLine fMoveToLine;
        private final ResumeAtLine fResumeAtLine;
        
        SuspendResume(IExecutionDMContext execCtx) {
            fRunToLine = new RunToLine(execCtx);
            fMoveToLine = new MoveToLine(execCtx);
            fResumeAtLine = new ResumeAtLine(execCtx);
        }
        
        @Override
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

        @Override
		public boolean canResume() { return false; }
        @Override
		public boolean canSuspend() { return false; }
        // This must return true because the platform
        // RunToLineActionDelegate will only enable the
        // action if we are suspended
        @Override
		public boolean isSuspended() { return true; }
        @Override
		public void resume() throws DebugException {}
        @Override
		public void suspend() throws DebugException {}
    }
    
    @Override
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
                    return new SuspendResume(execDmc);
                }
            }
        }
        return null;
    }

    @Override
	@SuppressWarnings("rawtypes")
    public Class[] getAdapterList() {
        return new Class[] { ISuspendResume.class };
    }
}