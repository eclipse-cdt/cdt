/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems and others.
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

import java.util.concurrent.Executor;

/**
 * An executor that behaves like ImmediateExecutor when the runnable is
 * submitted from a particular executor, otherwise it forwards the runnable to
 * that executor.
 *
 * @since 2.2
 *
 */
public class ImmediateInDsfExecutor implements Executor {

	final private DsfExecutor fDsfExecutor;

	public DsfExecutor getDsfExecutor() {
		return fDsfExecutor;
	}

	public ImmediateInDsfExecutor(DsfExecutor dsfExecutor) {
		fDsfExecutor = dsfExecutor;
	}

	@Override
	public void execute(final Runnable command) {
		if (fDsfExecutor.isInExecutorThread()) {
			command.run();
		} else {
			fDsfExecutor.execute(new DsfRunnable() {
				@Override
				public void run() {
					command.run();
				}
			});
		}
	}

}
