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

package org.eclipse.cdt.dsf.ui.concurrent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * DSF executor which uses the display thread to run the submitted runnables 
 * and callables.  The implementation is based on the default DSF executor 
 * which still creates its own thread.  However this thread blocks when running
 * each executable in the display thread.   
 * 
 * @since 1.0
 */
public class DisplayDsfExecutor extends DefaultDsfExecutor 
{
    /**
     * Internal mapping of display objects to executors.
     */
    private static Map<Display, DisplayDsfExecutor> fExecutors = Collections.synchronizedMap( new HashMap<Display, DisplayDsfExecutor>() );

	/**
	 * Factory method for display executors.
	 * 
	 * <p>
	 * Call this from the GUI thread unless you are certain an instance has
	 * already been created for the given display (creation of new instance will
	 * fail on a non-GUI thread).
	 * 
	 * @param display
	 *            Display to create an executor for.
	 * @return The new (or re-used) executor.
	 */
    public static DisplayDsfExecutor getDisplayDsfExecutor(Display display) {
        synchronized (fExecutors) {
            DisplayDsfExecutor executor = fExecutors.get(display);
            if (executor == null) {
                executor = new DisplayDsfExecutor(display);
                fExecutors.put(display, executor);
            }
            return executor;
        }
    }
    
    /**
     * The display class used by this executor to execute the submitted runnables. 
     */
    private final Display fDisplay;
    
	private DisplayDsfExecutor(Display display) {
		super("Display DSF Executor"); //$NON-NLS-1$
		fDisplay = display;
		fDisplay.addListener(SWT.Dispose, new Listener() {
		    public void handleEvent(Event event) {
		        if (event.type == SWT.Dispose) {
                    DisplayDsfExecutor.super.shutdownNow();
		        }
		    }
		});
	}
	
	/**
	 * Override to check if we're in the display thread rather than the helper
	 * thread of the super-class.
	 */
	@Override
	public boolean isInExecutorThread() {
	    return Thread.currentThread().equals(fDisplay.getThread());
	}
	
	/**
	 * Creates a callable wrapper, which delegates to the display to perform the 
	 * operation.  The callable blocks the executor thread while each call
	 * is executed in the display thred.
	 * @param <V> Type used in the callable.
	 * @param callable Callable to wrap.
	 * @return Wrapper callable.
	 */
	private <V> Callable<V> createSWTDispatchCallable(final Callable<V> callable) {
        // Check if executable wasn't executed already.
        if (DEBUG_EXECUTOR && callable instanceof DsfExecutable) {
            assert !((DsfExecutable)callable).getSubmitted() : "Executable was previously executed."; //$NON-NLS-1$
            ((DsfExecutable)callable).setSubmitted();
        }

	    return new Callable<V>() {
			@SuppressWarnings("unchecked")
            public V call() throws Exception {
				final Object[] v = new Object[1];
				final Throwable[] e = new Throwable[1];
				
                try {
    				fDisplay.syncExec(new Runnable() {
    					public void run() {
    						try {
    							v[0] = callable.call();
    						} catch(Throwable exception) {
    							e[0] = exception;
    						}
    					}
    				});
                } catch (SWTException swtException) {
                    if (swtException.code == SWT.ERROR_DEVICE_DISPOSED) {
                        DisplayDsfExecutor.super.shutdown();
                    }
                }

				if(e[0] instanceof RuntimeException) {
					throw (RuntimeException) e[0];
                } else if (e[0] instanceof Error) {
                    throw (Error) e[0];
				} else if(e[0] instanceof Exception) {
					throw (Exception) e[0];
                }
				
				return (V) v[0];
			}
		};
	}
	
    /**
     * Creates a runnable wrapper, which delegates to the display to perform the 
     * operation.  The runnable blocks the executor thread while each call
     * is executed in the display thred.
     * @param runnable Runnable to wrap.
     * @return Wrapper runnable.
     */
	private Runnable createSWTDispatchRunnable(final Runnable runnable) {

	    // Check if executable wasn't executed already.
        if (DEBUG_EXECUTOR && runnable instanceof DsfExecutable) {
            assert !((DsfExecutable)runnable).getSubmitted() : "Executable was previously executed."; //$NON-NLS-1$
            ((DsfExecutable)runnable).setSubmitted();
        }

	    return new Runnable() {
			public void run() {
				try {
    				fDisplay.syncExec(new Runnable() {
    					public void run() {
    					    runnable.run();
    					}
    				});
				} catch (SWTException swtException) {
				    if (swtException.code == SWT.ERROR_DEVICE_DISPOSED) {
				        DisplayDsfExecutor.super.shutdownNow();
				    }
				}
			}
		};
	}
	
	@Override
	public <V> ScheduledFuture<V> schedule(final Callable<V> callable, long delay, TimeUnit unit) {
	    if (fDisplay.isDisposed()) {
            if (!super.isShutdown()) super.shutdown();
	        throw new RejectedExecutionException("Display " + fDisplay + " is disposed."); //$NON-NLS-1$ //$NON-NLS-2$
	    }
		return super.schedule(createSWTDispatchCallable(callable), delay, unit);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        if (fDisplay.isDisposed()) {
            if (!super.isShutdown()) super.shutdown();
            throw new RejectedExecutionException("Display " + fDisplay + " is disposed."); //$NON-NLS-1$ //$NON-NLS-2$
        }
		return super.schedule(createSWTDispatchRunnable(command), delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        if (fDisplay.isDisposed()) {
            if (!super.isShutdown()) super.shutdown();
            throw new RejectedExecutionException("Display " + fDisplay + " is disposed."); //$NON-NLS-1$ //$NON-NLS-2$
        }
		return super.scheduleAtFixedRate(createSWTDispatchRunnable(command), initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        if (fDisplay.isDisposed()) {
            if (!super.isShutdown()) super.shutdown();
            throw new RejectedExecutionException("Display " + fDisplay + " is disposed."); //$NON-NLS-1$ //$NON-NLS-2$
        }
		return super.scheduleWithFixedDelay(createSWTDispatchRunnable(command), initialDelay, delay, unit);
	}

	@Override
	public void execute(Runnable command) {
        if (fDisplay.isDisposed()) {
            if (!super.isShutdown()) super.shutdown();
            throw new RejectedExecutionException("Display " + fDisplay + " is disposed."); //$NON-NLS-1$ //$NON-NLS-2$
        }
		super.execute(createSWTDispatchRunnable(command));
	}

	@Override
	public <T> Future<T> submit(Callable<T> callable) {
        if (fDisplay.isDisposed()) {
            if (!super.isShutdown()) super.shutdown();
            throw new RejectedExecutionException("Display " + fDisplay + " is disposed."); //$NON-NLS-1$ //$NON-NLS-2$
        }
		return super.submit(createSWTDispatchCallable(callable));
	}

	@Override
	public <T> Future<T> submit(Runnable command, T result) {
        if (fDisplay.isDisposed()) {
            if (!super.isShutdown()) super.shutdown();
            throw new RejectedExecutionException("Display " + fDisplay + " is disposed."); //$NON-NLS-1$ //$NON-NLS-2$
        }
		return super.submit(createSWTDispatchRunnable(command), result);
	}

	@Override
	public Future<?> submit(Runnable command) {
        if (fDisplay.isDisposed()) {
            if (!super.isShutdown()) super.shutdown();
            throw new RejectedExecutionException("Display " + fDisplay + " is disposed."); //$NON-NLS-1$ //$NON-NLS-2$
        }
		return super.submit(createSWTDispatchRunnable(command));
	}
	
    /**
     * Override to prevent clients from shutting down.  The executor will be
     * shut down when the underlying display is discovered to be shut down. 
     */
	@Override
	public void shutdown() {
	}
	
    /**
     * Override to prevent clients from shutting down.  The executor will be
     * shut down when the underlying display is discovered to be shut down. 
     */
	@SuppressWarnings({ "cast", "unchecked" })
    @Override
	public List<Runnable> shutdownNow() {
	    return (List<Runnable>)Collections.EMPTY_LIST;
	}
}
