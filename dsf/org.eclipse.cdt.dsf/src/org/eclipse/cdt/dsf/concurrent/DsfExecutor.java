/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.concurrent;

import java.util.concurrent.ScheduledExecutorService;

/**
 * DSF executor service.  Implementations of this executor must ensure
 * that all runnables and callables are executed in the same thread: the
 * executor's single dispatch thread.
 * <br>Note: A DSF executor dispatch thread does not necessarily have
 * to be exclusive to the executor, it could be shared with
 * another event dispatch service, such as the SWT display dispatch thread.
 *
 * @since 1.0
 */
@ThreadSafe
public interface DsfExecutor extends ScheduledExecutorService {
	/**
	 * Checks if the thread that this method is called in is the same as the
	 * executor's dispatch thread.
	 * @return true if in DSF executor's dispatch thread
	 */
	public boolean isInExecutorThread();
}
