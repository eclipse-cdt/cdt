/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ARM Limited - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.internal.ui.disassembly.editor.DisassemblyEditorManager;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.progress.UIJob;

public class SourceDisplayAdapter implements ISourceDisplay {

    class DelegatingStackFrame implements IStackFrame {
        private ICStackFrame fDelegate;

        DelegatingStackFrame(ICStackFrame delegate) {
            super();
            fDelegate = delegate;
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStackFrame#getCharEnd()
         */
        public int getCharEnd() throws DebugException {
            return fDelegate.getCharEnd();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStackFrame#getCharStart()
         */
        public int getCharStart() throws DebugException {
            return fDelegate.getCharStart();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStackFrame#getLineNumber()
         */
        public int getLineNumber() throws DebugException {
            return fDelegate.getLineNumber();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStackFrame#getName()
         */
        public String getName() throws DebugException {
            return fDelegate.getName();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStackFrame#getRegisterGroups()
         */
        public IRegisterGroup[] getRegisterGroups() throws DebugException {
            return fDelegate.getRegisterGroups();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStackFrame#getThread()
         */
        public IThread getThread() {
            return fDelegate.getThread();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStackFrame#getVariables()
         */
        public IVariable[] getVariables() throws DebugException {
            return fDelegate.getVariables();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStackFrame#hasRegisterGroups()
         */
        public boolean hasRegisterGroups() throws DebugException {
            return fDelegate.hasRegisterGroups();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStackFrame#hasVariables()
         */
        public boolean hasVariables() throws DebugException {
            return fDelegate.hasVariables();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
         */
        public IDebugTarget getDebugTarget() {
            return fDelegate.getDebugTarget();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
         */
        public ILaunch getLaunch() {
            return fDelegate.getLaunch();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
         */
        public String getModelIdentifier() {
            return fDelegate.getModelIdentifier();
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
            if (ICStackFrame.class.equals(adapter))
                return fDelegate;
            return fDelegate.getAdapter(adapter);
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStep#canStepInto()
         */
        public boolean canStepInto() {
            return fDelegate.canStepInto();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStep#canStepOver()
         */
        public boolean canStepOver() {
            return fDelegate.canStepOver();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStep#canStepReturn()
         */
        public boolean canStepReturn() {
            return fDelegate.canStepReturn();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStep#isStepping()
         */
        public boolean isStepping() {
            return fDelegate.isStepping();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStep#stepInto()
         */
        public void stepInto() throws DebugException {
            fDelegate.stepInto();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStep#stepOver()
         */
        public void stepOver() throws DebugException {
            fDelegate.stepOver();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStep#stepReturn()
         */
        public void stepReturn() throws DebugException {
            fDelegate.stepReturn();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
         */
        public boolean canResume() {
            return fDelegate.canResume();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
         */
        public boolean canSuspend() {
            return fDelegate.canSuspend();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
         */
        public boolean isSuspended() {
            return fDelegate.isSuspended();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.ISuspendResume#resume()
         */
        public void resume() throws DebugException {
            fDelegate.resume();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
         */
        public void suspend() throws DebugException {
            fDelegate.suspend();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
         */
        public boolean canTerminate() {
            return fDelegate.canTerminate();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
         */
        public boolean isTerminated() {
            return fDelegate.isTerminated();
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.ITerminate#terminate()
         */
        public void terminate() throws DebugException {
            fDelegate.terminate();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.sourcelookup.ISourceDisplay#displaySource(java.lang.Object, org.eclipse.ui.IWorkbenchPage, boolean)
     */
    public void displaySource(Object element, IWorkbenchPage page, boolean forceSourceLookup) {
        if (element instanceof ICStackFrame) {
            ICStackFrame frame = (ICStackFrame)element; 
            if (isDisplayDisassembly(frame, page)) {
                displayDisassembly(page, frame);
            } else {
                DelegatingStackFrame delegatingFrame = new DelegatingStackFrame((ICStackFrame)element);
                ISourceDisplay sd = (ISourceDisplay)Platform.getAdapterManager().getAdapter(delegatingFrame, ISourceDisplay.class);
                if (sd != null)
                    sd.displaySource(element, page, forceSourceLookup);
            }
        }
    }

    private boolean isDisplayDisassembly(ICStackFrame frame, IWorkbenchPage page) {
        // always go to the disassembly window if it is already open
        IEditorPart editor = getDisassemblyEditorManager().findEditor(page, frame);
        return (editor != null);
    }

    protected DisassemblyEditorManager getDisassemblyEditorManager() {
        return CDebugUIPlugin.getDefault().getDisassemblyEditorManager();
    }

    private void displayDisassembly(final IWorkbenchPage page, final Object debugContext) {
        Job uijob = new UIJob("Display Disassembly Job") { //$NON-NLS-1$
            /* (non-Javadoc)
             * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                try {
                    getDisassemblyEditorManager().openEditor(page, debugContext);
                }
                catch(DebugException e) {
                    return e.getStatus();
                }
                return Status.OK_STATUS;
            }

        };
        uijob.setSystem(true);
        uijob.schedule();
    }
}
