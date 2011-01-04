/*******************************************************************************
 * Copyright (c) 2000, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [190231] initial API and implementation
 * IBM Corporation - Javadoc for runInDefaultContext() method
 * Martin Oberhuber (Wind River) - [236355] [api] Add an IRSEInteractionProvider#eventExec() method
 *******************************************************************************/
package org.eclipse.rse.core;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;

/**
 * Interaction Provider Interface.
 *
 * Classes implementing this interface provide a means for RSE to communicate
 * with the outside world: via progress monitors, events and messages. A UI
 * implementation of this interface would typically use UI components for user
 * interaction; although this can be changed also intermittently.
 *
 * Non-UI headless applications may log messages rather than doing interactive
 * messages, and may use different Threads for sending messages.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the <a href="http://www.eclipse.org/tm/">Target Management</a>
 * team.
 * </p>
 *
 * @since org.eclipse.rse.core 3.0
 */
public interface IRSEInteractionProvider {

	/**
	 * Return a default progress monitor for the context that's currently
	 * active.
	 *
	 * Usually, long-running operations should always be created from the client
	 * with a progress monitor that they can use. Historically, however, this
	 * has not always been done and is especially problematic in operations that
	 * are performed as the result of Callbacks.
	 *
	 * For such situations, this method returns a default progress monitor in a
	 * context that we guess. We try to use one default progress use one for all
	 * phases of a single operation, such as connecting and resolving.
	 *
	 * @return a default progress monitor
	 */
	public IProgressMonitor getDefaultProgressMonitor();

    /**
	 * <p>
	 * Runs the given <code>IRSERunnableWithProgress</code> in the default
	 * context available to this interaction provider, that provides a progress
	 * monitor. For example, if the default context is a
	 * <code>ProgressMonitorDialog</code> then the runnable is run using the
	 * dialog's progress monitor. This method is derived from
	 * <code>IRunnableContext#run()</code>.
	 * </p>
	 * <p>
	 * If <code>fork</code> is <code>false</code>, the current thread is
	 * used to run the runnable. Note that if <code>fork</code> is
	 * <code>true</code>, it is unspecified whether or not this method blocks
	 * until the runnable has been run. Implementers should document whether the
	 * runnable is run synchronously (blocking) or asynchronously
	 * (non-blocking), or if no assumption can be made about the blocking
	 * behaviour.
	 * </p>
	 *
	 * @param fork <code>true</code> if the runnable should be run in a
	 *            separate thread, and <code>false</code> to run in the same
	 *            thread
	 * @param cancellable <code>true</code> to enable the cancellation, and
	 *            <code>false</code> to make the operation uncancellable
	 * @param runnable the runnable to run
	 *
	 * @exception InvocationTargetException wraps any exception or error which
	 *                occurs while running the runnable
	 * @exception InterruptedException propagated by the context if the runnable
	 *                acknowledges cancellation by throwing this exception. This
	 *                should not be thrown if cancellable is <code>false</code>.
	 */
	public void runInDefaultContext(boolean fork, boolean cancellable, IRSERunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException;

	/**
	 * Asynchronously run the given runnable in a separate thread.
	 *
	 * UI implementations should have the runnable run in the dispatch thread,
	 * where it has access to UI components. This is used for notifications.
	 * Non-UI applications may choose any Thread they like, provided that two
	 * conditions are met:
	 * <ol>
	 * <li>All Runnables are run on the same Thread.
	 * <li>The ordering of Runnables remains intact.
	 * </ol>
	 *
	 * @param runnable the Runnable to run asynchronously
	 */
	public void asyncExec(Runnable runnable);

	/**
	 * Run the given runnable with "event" semantics, that is: asynchronously
	 * run it through {@link #asyncExec(Runnable)} on the interaction provider's
	 * designated event thread, unless the call is already coming from that very
	 * thread.
	 * 
	 * In that case, the Runnable is run immediately and synchronously.
	 *
	 * @param runnable the Runnable to run asynchronously with "event" semantics
	 * @see #asyncExec(Runnable)
	 */
	public void eventExec(Runnable runnable);

	/**
	 * Flush the Queue of Runnables enqueued with {@link #asyncExec(Runnable)}.
	 *
	 * This needs to be done when this interaction provider is to be replaced by
	 * a different one, in order to ensure that the ordering of all Runnables
	 * remains intact.
	 */
	public void flushRunnableQueue();

	/**
	 * Show the given message or log it.
	 *
	 * In an interactive environment, this pops up a dialog asking the user to
	 * press an OK button. The method will not return before the OK button is
	 * pressed.
	 *
	 * @param msg the message to show
	 */
	public void showMessage(SystemMessage msg);
}
