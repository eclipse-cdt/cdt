/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.concurrent;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.DsfPlugin;

/**
 * Convenience class for implementing a series of commands that need to be 
 * executed asynchronously.  
 * <p>
 * Certain complex tasks require multiple commands to be executed in a chain, 
 * because for example result of one command is used as input into another 
 * command.  The typical Riverbed pattern of solving this problem is the following:
 * <li> 
 * <br> 1. original caller passes a Done callback to a method and invokes it
 * <br> 2. the method is executed by a subsystem
 * <br> 3. when the method is finished it calls another method and passes 
 * the original callback to it
 * <br> 4. steps 2-3 are repeated number of times
 * <br> 5. when the last method in a chain is executed, it submits the original
 * Done callback
 * </li>
 * <p>
 * This pattern is very useful in itself, but it proves very difficult to follow
 * because the methods can be scattered accross many classes and systems.  Also
 * if progress reporting, cancellability, and roll-back ability is required, it
 * has to be re-implemented every time.  The Sequence class tries to address
 * this problem by containing this pattern in a single class. 
 * 
 * <br>TODO: should a sequence be re-entrant.  I.e. should the arguments be 
 * passed in through a map, and the return values returned back in the done?
 * <br>FIXME: convert to implement Future interface 
 */
abstract public class DsfSequence {

    /**
     * The abstract class that each step has to implement
     * <br>FIXME: convert execute() and rollBacl() to take "done" as an argument.
     * This way we can make step a static class, and make its paradigm
     * more consistent with rest of Riverbed. 
     */
    abstract public class Step {
        public void execute() { stepFinished(); }
        public void rollBack() { stepRolledBack(); }
        public int getTicks() { return 1; }
    }
    
    private DsfExecutor fExecutor;
    private Step[] fSteps;
    private Done fDoneQC;
    private String fTaskName;
    private String fRollbackTaskName;
    private IProgressMonitor fProgressMonitor = new NullProgressMonitor();
    private int fCurrentStepIdx = 0;
    boolean fCancelled = false;
    
    /**
     * Default constructor.  If this constructor is used, the steps need to be initialized
     * before the sequence can be invoked.
     * @param executor the Riverbed executor which will be used to invoke all steps 
     */
    public DsfSequence(DsfExecutor executor) { 
        this(executor, null); 
    }
    
    /**
     * Constructor that initialized the steps.
     * @param executor the Riverbed executor which will be used to invoke all steps 
     * @param steps sequence steps 
     */
    public DsfSequence(DsfExecutor executor, Step[] steps) {
        fExecutor = executor;
        fSteps = steps;
    }
    
    /** Returns the riverbed executor for this sequence */
    public DsfExecutor getExecutor() { return fExecutor; }
    
    /**
     * Sets the done callback to be submitted when the sequence is finished. 
     * If the sequence is submitted by a caller in the dispatch thread, this is 
     * the way that the original caller can be notified of the sequence 
     * completion.  If the caller blocks and waits for the sequence
     * completion, the Done callback is not necessary.
     * @param doneQC callback to submit when sequence completes, can be null
     */
    public void setDone(Done doneQC) {
        fDoneQC = doneQC;
    }
    
    /**
     * Returns the Done callback that is registered with the Sequence
     * @param doneQC callback that will be submitted when sequence completes, 
     * null if there is no callback configured
     */
    public Done getDone() { return fDoneQC; }
    
    /** Sets the steps to be executed. */
    public void setSteps(Step[] steps) {
        assert fCurrentStepIdx == 0;
        fSteps = steps;
    }
    
    /** Returns the steps to be executed. */
    public Step[] getSteps() { return fSteps; }
    
    /**
     * Returns index of the step that is currently being executed.
     * <br>NOTE: After sequence is invoked, this method should be called 
     * only in the Riverbed executor thread.
     * @return
     */
    public int getCurrentIdx() { return fCurrentStepIdx; }
    
    /**
     * Sets the progress monitor that will be called by the sequence with udpates.
     * @param pm
     */
    public void setProgressMonitor(IProgressMonitor pm) { fProgressMonitor = pm; }
    
    /**
     * Sets the task name for this sequence.  To be used with progress monitor;
     * @param taskName
     */
    public void setTaskName(String taskName) { fTaskName = taskName; }

    /**
     * Sets the task name to be used with progress monitor, if this sequence needs 
     * to be rolled back as result of cancellation or error.
     * @param taskName
     */
    public void setRollBackTaskName(String n) { fRollbackTaskName = n; }
        

    /** Submits this sequence to the executor. */
    public void invokeLater() {
        getExecutor().submit( new DsfRunnable() { public void run() { doInvoke(); } });
    }
    
    /**
     * Submits this sequence to the Riverbed executor, and blocks waiting for the 
     * sequence to complete. 
     * <br>NOTE: This method is NOT to be called on the Riverbed executor thread. 
     */
    public synchronized void invoke() {
        assert !fExecutor.isInExecutorThread() :
            "Cannot be called on dispatch thread: " + this;
        setDone(new Done() {
                public void run() {
                    synchronized(DsfSequence.this) { DsfSequence.this.notifyAll(); } 
                }
            });
        invokeLater();
        try {
            wait();
        } catch (InterruptedException e) {
            // TODO: error handling?
        }
    }

    private void doInvoke() {
        assert fCurrentStepIdx == 0;
        if (fTaskName != null) {
            fProgressMonitor.subTask(fTaskName);
        }
        fSteps[fCurrentStepIdx].execute();
    }
    
    /**
     * Cancells the execution of this sequence.  The roll-back will start when
     * the current step completes.
     *
     */
    public void cancel() {
        fCancelled = true;
    }

    /**
     * To be called only by the step implementation, Tells the sequence to 
     * submit the next step. 
     */
    public void stepFinished() {
        getExecutor().submit(new DsfRunnable() { public void run() {
            fProgressMonitor.worked(getSteps()[fCurrentStepIdx].getTicks());
            fCurrentStepIdx++;
            if (fCurrentStepIdx < fSteps.length) {
                if (fCancelled) {
                    abort(new Status(
                        IStatus.CANCEL, DsfPlugin.PLUGIN_ID, -1, 
                        "Cancelled" + fTaskName != null ? ": " + fTaskName : "", 
                        null));
                }
                fSteps[fCurrentStepIdx].execute();
            } else {
                if (fDoneQC != null) getExecutor().submit(fDoneQC);
            }
        }});
    }
     
    /**
     * To be called only by the step implementation. Tells the sequence to 
     * roll back next step. 
     */
    public void stepRolledBack() {
        getExecutor().submit(new DsfRunnable() { public void run() {
            fProgressMonitor.worked(getSteps()[fCurrentStepIdx].getTicks());
            fCurrentStepIdx--;
            if (fCurrentStepIdx >= 0) {
                fSteps[fCurrentStepIdx].rollBack();
            } else {
                if (fDoneQC != null) getExecutor().submit(fDoneQC);
            }
        }});
    }
    
    /**
     * To be called only by step implementation.  Tells the sequence
     * that its execution is to be aborted and it should start rolling back
     * the sequence as if it was cancelled by user.
     * @param error
     */
    public void abort(final IStatus error) {
        getExecutor().submit(new DsfRunnable() { public void run() {
            if (fRollbackTaskName != null) {
                fProgressMonitor.subTask(fRollbackTaskName);
            }
            fDoneQC.setStatus(error);
            fCurrentStepIdx--;
            if (fCurrentStepIdx >= 0) {
                fSteps[fCurrentStepIdx].rollBack();
            } else {
                if (fDoneQC != null) getExecutor().submit(fDoneQC);
            }
        }});
    }
    
}
