/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Nokia			  - added StepWithProgress. Oct, 2008
 *******************************************************************************/
package org.eclipse.cdt.dsf.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor.ICanceledListener;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Convenience class for implementing a series of commands that need to be 
 * executed asynchronously.  
 * <p>
 * Certain complex tasks require multiple commands to be executed in a chain, 
 * because for example result of one command is used as input into another 
 * command.  The typical DSF pattern of solving this problem is the following:
 * <li> 
 * <br> 1. original caller passes a RequestMonitor callback to a method and invokes it
 * <br> 2. the method is executed by a subsystem
 * <br> 3. when the method is finished it calls another method and passes 
 * the original callback to it
 * <br> 4. steps 2-3 are repeated a number of times
 * <br> 5. when the last method in a chain is executed, it submits the original
 * RequestMonitor callback
 * </li>
 * <p>
 * This pattern is very useful in itself, but it proves very difficult to follow
 * because the methods can be scattered across many classes and systems.  Also
 * if progress reporting, cancellability, and roll-back ability is required, it
 * has to be re-implemented every time.  The Sequence class tries to address
 * this problem by containing this pattern in a single class. 
 * 
 * @since 1.0
 */
@ThreadSafe
abstract public class Sequence extends DsfRunnable implements Future<Object> {

    /**
     * The abstract class that each step has to implement.  
     */
    abstract public static class Step {
        private Sequence fSequence;
        
        /**
         * Sets the sequence that this step belongs to.  It is only accessible 
         * by the sequence itself, and is not meant to be called by sequence
         * sub-classes. 
         */
        void setSequence(Sequence sequence) { fSequence = sequence; }
        
        /** Returns the sequence that this step is running in. */
        public Sequence getSequence() { return fSequence; }
        
        /** 
         * Executes the step.  Overriding classes should perform the 
         * work in this method.
         * @param rm Result token to submit to executor when step is finished.
         */
        public void execute(RequestMonitor rm) {
            rm.done();
        }

        /** 
         * Roll back gives the step implementation a chance to undo the 
         * operation that was performed by execute().
         * <br>
         * Note if the {@link #execute(RequestMonitor)} call completes with a 
         * non-OK status, then rollBack will not be called for that step.  
         * Instead it will be called for the previous step.
         * @param rm Result token to submit to executor when rolling back the step is finished.
         */
        public void rollBack(RequestMonitor rm) { 
            rm.done();
        }
        
        /** 
         * Returns the number of progress monitor ticks corresponding to this 
         * step.
         */
        public int getTicks() { return 1; }

        /**
         * Task name for this step. This will be displayed in the label of the
         * progress monitor of the owner sequence.
         * 
         * @return name of the task carried out by the step, can be 
         * <code>null</code>, in which case the overall task name will be used.
         * 
         * @since 1.1
         */
        public String getTaskName() {
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * A step that will report execution progress by itself on the progress
     * monitor of the owner sequence.<br>
     * <br>
     * Note we don't offer a rollBack(RequestMonitor, IProgressMonitor) as we 
     * don't want end user to be able to cancel the rollback.
     * 
     * @since 1.1
     */
    abstract public static class StepWithProgress extends Step {

        @Override
        // don't allow subclass to implement this by "final" it.
        final public void execute(RequestMonitor rm) {
            assert false : "execute(RequestMonitor rm, IProgressMonitor pm) should be called instead"; //$NON-NLS-1$
        }

        /**
         * Execute the step with a progress monitor. Note the given progress
         * monitor is a sub progress monitor of the owner sequence which is
         * supposed to be fully controlled by the step. Namely the step should
         * call beginTask() and done() of the monitor.
         * 
         * @param rm
         * @param pm
         */
        public void execute(RequestMonitor rm, IProgressMonitor pm) {
            rm.done();
            pm.done();
        }
    }

    /** The synchronization object for this future */
    final Sync fSync = new Sync();

    /** 
     * Executor that this sequence is running in.  It is used by the sequence
     * to submit the runnables for steps, and for submitting the result.
     */  
    final private DsfExecutor fExecutor;
    
    /** 
     * Result callback to invoke when the sequence is finished.  Intended to 
     * be used when the sequence is created and invoked from the executor 
     * thread.  Otherwise, the {@link Future#get()} method is the appropriate
     * method of retrieving the result. 
     */
    final private RequestMonitor fRequestMonitor;
    
    /** Status indicating the success/failure of the test.  Used internally only. */
    @ConfinedToDsfExecutor("getExecutor") 
    private IStatus fStatus = Status.OK_STATUS;    
    
    @ConfinedToDsfExecutor("getExecutor") 
    private int fCurrentStepIdx = 0;
    
    /** Task name for this sequence used with the progress monitor */ 
    final private String fTaskName;
    
    /** Task name used when the sequence is being rolled back. */
    final private String fRollbackTaskName;
    
    final private IProgressMonitor fProgressMonitor;
    
    /** Convenience constructor with limited arguments. */
    public Sequence(DsfExecutor executor) {
        this(executor, new NullProgressMonitor(), "", "", null); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /** 
     * Creates a sequence with a request monitor.  If the client cancels the 
     * request monitor, then the request monitors in the 
     * {@link Step#execute(RequestMonitor)}
     * implementations will immediately call the cancel listeners to notify.
     * 
     * @param executor The DSF executor which will be used to invoke all 
     * steps. 
     * @param rm The request monitor which will be invoked when the sequence
     * is completed.
     */
    public Sequence(DsfExecutor executor, RequestMonitor rm) {
        this(executor, new NullProgressMonitor(), "", "", rm); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Creates a sequence with a progress monitor.  If the progress monitor is
     * canceled, then request monitors in the 
     * {@link Step#execute(RequestMonitor)} implementations will need to call 
     * rm.isCanceled() to discover the cancellation.
     * @param executor The DSF executor which will be used to invoke all 
     * steps. 
     * @param pm Progress monitor for monitoring this sequence.  
     * @param taskName Name that will be used in call to 
     * {@link IProgressMonitor#beginTask(String, int)},when the task is 
     * started.
     * @param rollbackTaskName Name that will be used in call to 
     * {@link IProgressMonitor#subTask(String)} if the task is canceled or 
     * aborted. 
     * 
     * @since 1.1
     */
    public Sequence(DsfExecutor executor, IProgressMonitor pm, String taskName, String rollbackTaskName) {
        this(executor, pm, taskName, rollbackTaskName, new RequestMonitorWithProgress(ImmediateExecutor.getInstance(), pm));
    }
    
    /**
     * Creates a sequence with a request monitor that includes a progress 
     * monitor.  
     * @param executor The DSF executor which will be used to invoke all 
     * steps. 
     * @param rm The request monitor containing the progress monitor
     * @param taskName Name that will be used in call to 
     * {@link IProgressMonitor#beginTask(String, int)},when the task is 
     * started.
     * @param rollbackTaskName Name that will be used in call to 
     * {@link IProgressMonitor#subTask(String)} if the task is canceled or 
     * aborted. 
     * 
     * @since 1.1
     */
    public Sequence(DsfExecutor executor, RequestMonitorWithProgress rm, String taskName, String rollbackTaskName) {
        this(executor, rm.getProgressMonitor(), taskName, rollbackTaskName, rm); 
    }    
    
    /**
     * Constructor that initialized the steps and the result callback.
     * <p>Note: This constructor should not be used because it creates a 
     * potential ambiguity when one of the two monitors is canceled.</p>
     * 
     * @param executor The DSF executor which will be used to invoke all 
     * steps. 
     * @param pm Progress monitor for monitoring this sequence.  This 
     * parameter cannot be null.
     * @param taskName Name that will be used in call to 
     * {@link IProgressMonitor#beginTask(String, int)},when the task is 
     * started.
     * @param rollbackTaskName Name that will be used in call to 
     * {@link IProgressMonitor#subTask(String)} if the task is canceled or 
     * aborted. 
     * @param Result that will be submitted to executor when sequence is 
     * finished.  Can be null if calling from non-executor thread and using 
     * {@link Future#get()} method to wait for the sequence result.
     */
    private Sequence(DsfExecutor executor, IProgressMonitor pm, String taskName, String rollbackTaskName, RequestMonitor rm) {
        fExecutor = executor;
        fProgressMonitor = pm;
        fTaskName = taskName;
        fRollbackTaskName = rollbackTaskName;
        fRequestMonitor = rm;
        
        if (fRequestMonitor != null) {
            fRequestMonitor.addCancelListener(new ICanceledListener() {
                @Override
                public void requestCanceled(RequestMonitor rm) {
                    fSync.doCancel();
                }
            });
        }
    }

    /** 
     * Returns the steps to be executed.  It is up to the deriving class to
     * supply the steps and to ensure that the list of steps will not be
     * modified after the sequence is constructed.
     * <p>
     * Steps are purposely not accepted as part of the DsfConstructor, in 
     * order to allow deriving classes to create the steps as a field.  And a 
     * setSteps() method is not provided, to guarantee that the steps will not
     * be modified once set (perhaps this is a bit paranoid, but oh well).  
     */
    abstract public Step[] getSteps();

    
    /** Returns the DSF executor for this sequence */
    public DsfExecutor getExecutor() { return fExecutor; }
    
    /**
     * Returns the RequestMonitor callback that is registered with the Sequence
     */
    public RequestMonitor getRequestMonitor() { return fRequestMonitor; }
    
    /**
     * The get method blocks until sequence is complete, but always returns null.
     * @see java.concurrent.Future#get
     */
    @Override
    public Object get() throws InterruptedException, ExecutionException { 
        fSync.doGet();
        return null;
    }

    /**
     * The get method blocks until sequence is complete or until timeout is 
     * reached, but always returns null.
     * @see java.concurrent.Future#get
     */
    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    	fSync.doGet(unit.toNanos(timeout));
        return null;
    }

    /**
     * Don't try to interrupt the DSF executor thread, just ignore the request 
     * if set.
     * <p>If a request monitor was specified when creating a sequence, that 
     * request monitor will be canceled by this method as well.  The client 
     * can also use the request monitor's cancel method to cancel the sequence.
     * 
     * @see RequestMonitor#cancel()
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        // Cancel the request monitor first, to avoid a situation where
        // the request monitor is not canceled but the status is set
        // to canceled.
        if (fRequestMonitor != null) {
            fRequestMonitor.cancel();
        }
        return fSync.doCancel();
    }

    @Override
    public boolean isCancelled() { return fSync.doIsCancelled(); }

    @Override
    public boolean isDone() { return fSync.doIsDone(); }

    
    @Override
    public void run() {
        // Change the state to running.
        if (fSync.doRun()) {
            // Set the reference to this sequence in each step.
            int totalTicks = 0;
            for (Step step : getSteps()) { 
                step.setSequence(this); 
                totalTicks += step.getTicks();
            }
            
            // Set the task name
            if (fTaskName != null) {
                fProgressMonitor.beginTask(fTaskName, totalTicks);
            }
            
            // Call the first step
            executeStep(0);
        } else {
            fSync.doFinish();
        }
    }

    /**
     * To be called only by the step implementation, Tells the sequence to 
     * submit the next step. 
     */
    private void executeStep(int nextStepIndex) {
        /*
         * At end of each step check progress monitor to see if it's cancelled.
         * If progress monitor is cancelled, mark the whole sequence as 
         * cancelled.
         */
        if (fProgressMonitor.isCanceled()) {
            cancel(false);
        }

        /*
         * If sequence was cencelled during last step (or before the sequence 
         * was ever executed), start rolling back the execution. 
         */
        if (isCancelled()) {
            cancelExecution();
            return;
        } 
         
        /*
         *  Check if we've reached the last step.  Note that if execution was
         *  cancelled during the last step (and thus the sequence is 
         *  technically finished, since it was cancelled it will be rolled 
         *  back. 
         */
        if (nextStepIndex >= getSteps().length) {
            finish();
            return;
        }

        // Proceed with executing next step.
        fCurrentStepIdx = nextStepIndex;
        try {
            Step currentStep = getSteps()[fCurrentStepIdx];
            final boolean stepControlsProgress = (currentStep instanceof StepWithProgress);

            RequestMonitor rm = new RequestMonitor(fExecutor, fRequestMonitor) {
                final private int fStepIdx = fCurrentStepIdx;

                @Override
                public void handleSuccess() {
                    // Check if we're still the correct step.
                    assert fStepIdx == fCurrentStepIdx;
                    if (!stepControlsProgress) {
                        // then sequence handles the progress report.
                        fProgressMonitor.worked(getSteps()[fStepIdx].getTicks());
                    }
                    executeStep(fStepIdx + 1);
                }
                
                @Override
                protected void handleCancel() {
                    Sequence.this.cancel(false);
                    cancelExecution();
                };
                
                @Override
                protected void handleErrorOrWarning() {
                    abortExecution(getStatus(), true);                    
                };

                @Override
                protected void handleRejectedExecutionException() {
                    abortExecution(
                        new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, 0, 
                            "Executor shut down while executing Sequence " + this + ", step #" + fCurrentStepIdx,  //$NON-NLS-1$ //$NON-NLS-2$
                            null), 
                        false);
                }
                
                @Override
                public String toString() {
                    return "Sequence \"" + fTaskName + "\", result for executing step #" + fStepIdx + " = " + getStatus(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            };

            fProgressMonitor.subTask(currentStep.getTaskName());
            
            if (stepControlsProgress) {

                // Create a sub-monitor that will be controlled by the step.
                SubProgressMonitor subMon = new SubProgressMonitor(fProgressMonitor, currentStep.getTicks(),
                    SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);

                ((StepWithProgress) currentStep).execute(rm, subMon);
            } else { // regular Step
                currentStep.execute(rm);
            }

        } catch (Throwable t) {
            /*
             * Catching the exception here will only work if the exception 
             * happens within the execute method.  It will not work in cases 
             * when the execute submits other runnables, and the other runnables
             * encounter the exception.
             */ 
            abortExecution(
                new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, 0, 
                           "Unhandled exception when executing Sequence " + this + ", step #" + fCurrentStepIdx,  //$NON-NLS-1$ //$NON-NLS-2$
                           t), 
                true);
            
            /*
             * Since we caught the exception, it will not be logged by 
             * DefaultDsfExecutable.afterExecution().  So log it here.
             */
            DefaultDsfExecutor.logException(t);
        }
    }
     
    /**
     * To be called only by the step implementation. Tells the sequence to 
     * roll back next step. 
     */
    private void rollBackStep(int stepIdx) {
        // If we reach before step 0, finish roll back. 
        if (stepIdx < 0) {
            finish();
            return;
        }
        
        // Proceed with rolling back given step.
        fCurrentStepIdx = stepIdx;
        try {
            getSteps()[fCurrentStepIdx].rollBack(new RequestMonitor(fExecutor, null) {
                final private int fStepIdx = fCurrentStepIdx;
                @Override
                public void handleCompleted() {
                    // Check if we're still the correct step.
                    assert fStepIdx == fCurrentStepIdx;
             
                    // Proceed to the next step.
                    if (isSuccess()) {
                        // NOTE: The getTicks() is ticks for executing the step,
                        // not for rollBack,
                        // though it does not really hurt to use it here.
                        fProgressMonitor.worked(getSteps()[fStepIdx].getTicks());

                        rollBackStep(fStepIdx - 1);
                    } else {
                        abortRollBack(getStatus());
                    }
                }
                @Override
                public String toString() {
                    return "Sequence \"" + fTaskName + "\", result for rolling back step #" + fStepIdx + " = " + getStatus(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }                
            });
        } catch(Throwable t) {
            /*
             * Catching the exception here will only work if the exception 
             * happens within the execute method.  It will not work in cases 
             * when the execute submits other runnables, and the other runnables
             * encounter the exception.
             */ 
            abortRollBack(new Status(
                IStatus.ERROR, DsfPlugin.PLUGIN_ID, 0, 
                "Unhandled exception when rolling back Sequence " + this + ", step #" + fCurrentStepIdx,  //$NON-NLS-1$ //$NON-NLS-2$
                t));
            
            /*
             * Since we caught the exception, it will not be logged by 
             * DefaultDsfExecutable.afterExecution().  So log it here.
             */
            DefaultDsfExecutor.logException(t);
        }
    }

    /**
     * Tells the sequence that its execution is to be aborted and it 
     * should start rolling back the sequence as if it was cancelled by user.  
     */
    private void cancelExecution() {
        if (fRollbackTaskName != null) {
            fProgressMonitor.subTask(fRollbackTaskName);
        }
        fStatus = new Status(IStatus.CANCEL, DsfPlugin.PLUGIN_ID, -1, "Sequence \"" + fTaskName + "\" cancelled.", null); //$NON-NLS-1$ //$NON-NLS-2$
        if (fRequestMonitor != null) {
            fRequestMonitor.setStatus(fStatus);
        }

        /* 
         * No need to call fSync, it should have been taken care of by 
         * Future#cancel method. 
         * 
         * Note that we're rolling back starting with the current step, 
         * because the current step was fully executed.  This is unlike
         * abortExecution() where the current step caused the roll-back.
         */ 
        rollBackStep(fCurrentStepIdx);
    }

    /**
     * Tells the sequence that its execution is to be aborted and it 
     * should start rolling back the sequence as if it was cancelled by user.  
     * 
     * @param status Status to use for reporting the error.
     * @param rollBack Whether to start rolling back the sequence after abort.
     * If this parameter is <code>false</code> then the sequence will also 
     * finish. 
     */
    private void abortExecution(final IStatus error, boolean rollBack) {
        if (fRollbackTaskName != null) {
            fProgressMonitor.subTask(fRollbackTaskName);
        }
        fStatus = error;
        if (fRequestMonitor != null) {
            fRequestMonitor.setStatus(error);
        }
        fSync.doAbort(new CoreException(error));

        if (rollBack) {
            // Roll back starting with previous step, since current step failed.
            rollBackStep(fCurrentStepIdx - 1);
        } else {
            finish();
        }
    }

    /**
     * Tells the sequence that that is rolling back, to abort roll back, and
     * notify the clients.  
     */
    private void abortRollBack(final IStatus error) {
        if (fRollbackTaskName != null) {
            fProgressMonitor.subTask(fRollbackTaskName);
        }
        
        /*
         * Compose new status based on previous status information and new
         * error information.
         */
        MultiStatus newStatus = 
            new MultiStatus(DsfPlugin.PLUGIN_ID, error.getCode(), 
                            "Sequence \"" + fTaskName + "\" failed while rolling back.", null); //$NON-NLS-1$ //$NON-NLS-2$
        newStatus.merge(error);
        newStatus.merge(fStatus);
        fStatus = newStatus;

        if (fRequestMonitor != null) {
            fRequestMonitor.setStatus(newStatus);
        }
        
        finish();
    }
    
    private void finish() {
        if (fRequestMonitor != null) fRequestMonitor.done();
        fSync.doFinish();
    }

    @SuppressWarnings("serial")
    final class Sync extends AbstractQueuedSynchronizer {
        private static final int STATE_RUNNING = 1;
        private static final int STATE_FINISHED = 2;
        private static final int STATE_ABORTING = 4;
        private static final int STATE_ABORTED = 8;
        private static final int STATE_CANCELLING = 16;
        private static final int STATE_CANCELLED = 32;

        private Throwable fException;

        private boolean isFinished(int state) {
            return (state & (STATE_FINISHED | STATE_CANCELLED | STATE_ABORTED)) != 0;
        }

        @Override
        protected int tryAcquireShared(int ignore) {
            return doIsDone()? 1 : -1;
        }

        @Override
        protected boolean tryReleaseShared(int ignore) {
            return true; 
        }

        boolean doIsCancelled() {
            int state = getState();
            return (state & (STATE_CANCELLING | STATE_CANCELLED)) != 0;
        }
        
        boolean doIsDone() {
            return isFinished(getState());
        }

        void doGet() throws InterruptedException, ExecutionException {
            acquireSharedInterruptibly(0);
            if (getState() == STATE_CANCELLED) throw new CancellationException();
            if (fException != null) throw new ExecutionException(fException);
        }

        void doGet(long nanosTimeout) throws InterruptedException, ExecutionException, TimeoutException {
            if (!tryAcquireSharedNanos(0, nanosTimeout)) throw new TimeoutException();                
            if (getState() == STATE_CANCELLED) throw new CancellationException();
            if (fException != null) throw new ExecutionException(fException);
        }

        void doAbort(Throwable t) {
            while(true) {
                int s = getState();
                if (isFinished(s)) return;
                if (compareAndSetState(s, STATE_ABORTING)) break;
            }
            fException = t;
        }

        boolean doCancel() {
            while(true) {
                int s = getState();
                if (isFinished(s)) return false;
                if (s == STATE_ABORTING) return false;
                if (compareAndSetState(s, STATE_CANCELLING)) break;
            }
            return true;
        }

        void doFinish() {
            while(true) {
                int s = getState();
                if (isFinished(s)) return;
                if (s == STATE_ABORTING) {
                    if (compareAndSetState(s, STATE_ABORTED)) break;
                } else if (s == STATE_CANCELLING) {
                    if (compareAndSetState(s, STATE_CANCELLED)) break;
                } else {
                    if (compareAndSetState(s, STATE_FINISHED)) break;
                }
            }
            releaseShared(0);
        }

        boolean doRun() {
            return compareAndSetState(0, STATE_RUNNING); 
        }
    }

}
