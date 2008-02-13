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

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.internal.DsfPlugin;
import org.eclipse.dd.dsf.service.IDsfService;

/**
 * Used to monitor the result of an asynchronous request.  Because of the
 * asynchronous nature of DSF code, a very large number of methods needs to 
 * signal the result of an operation through a call-back.  This class is the base 
 * class for such call backs.  
 * <p>
 * The intended use of this class, is that a client who is calling an asynchronous 
 * method, will sub-class RequestMonitor, and implement the method {@link #handleCompleted()}, 
 * or any of the other <code>handle...</code> methods, in order to interpret the 
 * results of the request.  The object implementing the asynchronous method is required 
 * to call the {@link #done()} method on the request monitor object that it received 
 * as an argument.  
 * </p>
 * <p>
 * This class handles an optional "parent" request monitor.  If a parent monitor is 
 * specified, it will automatically be invoked by this monitor when the request is 
 * completed.  The parent option is useful when implementing a method which is 
 * asynchronous (and accepts a request monitor as an argument) and which itself calls
 * another asynchronous method to complete its operation.  For example, in the request
 * monitor implementation below, the implementation only needs to override 
 * <code>handleOK()</code>, because the base implementation will handle notifying the 
 * parent <code>rm</code> in case the <code>getIngredients()</code> call fails. 
 * <pre>
 *     public void createCupCakes(final DataRequestMonitor<CupCake[]> rm) {
 *         getIngredients(new DataRequestMonitor<Ingredients>(fExecutor, rm) {
 *                 public void handleOK() {
 *                     rm.setData( new CupCake(getData().getFlour(), getData().getSugar(), 
 *                                             getData().getBakingPowder()));
 *                     rm.done();  
 *                 }
 *             });
 *     }
 * </pre>
 * </p>
 */
@ConfinedToDsfExecutor("")
public class RequestMonitor {
    public static final IStatus STATUS_CANCEL = new Status(IStatus.CANCEL, DsfPlugin.PLUGIN_ID, "Request canceled"); //$NON-NLS-1$
    
    /** 
     * The executor that will be used in order to invoke the handler of the results
     * of the request.
     */
    private final Executor fExecutor;
    
    /**
     * The request monitor which was used to call into the method that created this 
     * monitor.  
     */
    private final RequestMonitor fParentRequestMonitor;

    /**
     * Status 
     */
    private IStatus fStatus = Status.OK_STATUS;
    private boolean fCanceled = false;
    private boolean fDone = false;

    /**
     * Constructor with an optional parent monitor. 
     * @param executor This executor will be used to invoke the runnable that 
     * will allow processing the completion code of this request monitor.
     * @param parentRequestMonitor The optional parent request monitor to be invoked by
     * default when this request completes.  Parameter may be null.
     */
    public RequestMonitor(Executor executor, RequestMonitor parentRequestMonitor) {
        fExecutor = executor;
        fParentRequestMonitor = parentRequestMonitor;
    }
    
    /** 
     * Sets the status of the result of the request.  If status is OK, this 
     * method does not need to be called. 
     */
    public synchronized void setStatus(IStatus status) { fStatus = status; }
    
    /** Returns the status of the completed method. */
    public synchronized IStatus getStatus() {
        return fStatus; 
    }
    
    /**
     * Sets this request as canceled.  The operation may still be carried out
     * as it is up to the implementation of the asynchronous operation
     * to cancel the operation.
     * @param canceled Flag indicating whether to cancel.
     */
    public synchronized void setCanceled(boolean canceled) {
        if (fParentRequestMonitor != null) {
            fParentRequestMonitor.setCanceled(canceled);
        } else {
            fCanceled = canceled;
        }
    }
    
    /**
     * Returns whether the request was canceled.  Even if the request is
     * canceled by the client, the implementor handling the request should 
     * still call {@link #done()} in order to complete handling 
     * of the request monitor. 
     */
    public synchronized boolean isCanceled() { 
        if (fParentRequestMonitor != null) {
            return fParentRequestMonitor.isCanceled();
        } else {
            return fCanceled;
        }
    }
    
    /**
     * Marks this request as completed.  Once this method is called, the
     * monitor submits a runnable to the DSF Executor to call the 
     * <code>handle...</code> methods.  
     * <p>
     * Note: This method should be called once and only once, for every request 
     * issued.  Even if the request was canceled.
     * </p>  
     */
    public synchronized void done() {
        if (fDone) {
            throw new IllegalStateException("RequestMonitor: " + this + ", done() method called more than once");  //$NON-NLS-1$//$NON-NLS-2$
        }
        fDone = true;
        try {
            fExecutor.execute(new DsfRunnable() {
                public void run() {
                    RequestMonitor.this.handleCompleted();
                }
                @Override
                public String toString() {
                    return "Completed: " + RequestMonitor.this.toString(); //$NON-NLS-1$
                }
            });
        } catch (RejectedExecutionException e) {
            handleRejectedExecutionException();
        }
    }

    @Override
    public String toString() {
        return "RequestMonitor (" + super.toString() + "): " + getStatus().toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * Default handler for the completion of a request.  The implementation
     * calls {@link #handleOK()} if the request succeeded, and calls 
     * {@link #handleErrorOrCancel()} or cancel otherwise.
     * <br>
     * Note: Sub-classes may override this method.
     */
    protected void handleCompleted() {
        if (getStatus().isOK()) {
            handleOK();
        } else {
            handleErrorOrCancel();
        } 
    }
    
    /**
     * Default handler for a successful the completion of a request.  If this 
     * monitor has a parent monitor that was configured by the constructor, that 
     * parent monitor is notified.  Otherwise this method does nothing. 
     * {@link #handleErrorOrCancel()} or cancel otherwise.
     * <br>
     * Note: Sub-classes may override this method.
     */
    protected void handleOK() {
        if (fParentRequestMonitor != null) {
            fParentRequestMonitor.done();
        }
    }
    
    /**
     * The default implementation of a cancellation or an error result of a 
     * request.  The implementation delegates to {@link #handleCancel()} and
     * {@link #handleError()} as needed.
     * <br>
     * Note: Sub-classes may override this method.
     */
    protected void handleErrorOrCancel() {
        assert !getStatus().isOK();
        if (isCanceled()) {
            handleCancel();
        } else {
            if (getStatus().getCode() == IStatus.CANCEL) {
                DsfPlugin.getDefault().getLog().log(new Status(
                    IStatus.ERROR, DsfPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, "Request monitor: '" + this + "' resulted in a cancel status: " + getStatus() + ", even though the request is not set to cancel.", null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            handleError();
        } 
    }
    
    /**
     * The default implementation of an error result of a request.  If this 
     * monitor has a parent monitor that was configured by the constructor, that 
     * parent monitor is configured with a new error status containing this error.
     * Otherwise the error is logged.  
     * <br>
     * Note: Sub-classes may override this method.
     */    
    protected void handleError() {
        if (fParentRequestMonitor != null) {
            fParentRequestMonitor.setStatus(getStatus());
            fParentRequestMonitor.done();
        } else {
            MultiStatus logStatus = new MultiStatus(DsfPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, "Request for monitor: '" + toString() + "' resulted in an error.", null); //$NON-NLS-1$ //$NON-NLS-2$
            logStatus.merge(getStatus());
            DsfPlugin.getDefault().getLog().log(logStatus);
        }
    }
    
    /**
     * Default handler for a canceled the completion of a request.  If this 
     * monitor has a parent monitor that was configured by the constructor, that 
     * parent monitor is notified.  Otherwise this method does nothing. 
     * <br>
     * Note: Sub-classes may override this method.
     */
    protected void handleCancel() {
        if (fParentRequestMonitor != null) {
            fParentRequestMonitor.setStatus(getStatus());
            fParentRequestMonitor.done();
        }
    }
    
    /**
     * Default handler for when the executor supplied in the constructor 
     * rejects the runnable that is submitted invoke this request monitor.
     * This usually happens only when the executor is shutting down.
     */
    protected void handleRejectedExecutionException() {
        MultiStatus logStatus = new MultiStatus(DsfPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, "Request for monitor: '" + toString() + "' resulted in a rejected execution exception.", null); //$NON-NLS-1$ //$NON-NLS-2$
        logStatus.merge(getStatus());
        if (fParentRequestMonitor != null) {
            fParentRequestMonitor.setStatus(logStatus);
            fParentRequestMonitor.done();
        } else {
            DsfPlugin.getDefault().getLog().log(logStatus);
        }
    }
}
