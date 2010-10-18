/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    
    public void execute(final Runnable command) {
        if (fDsfExecutor.isInExecutorThread()) {
            command.run();
        } else {
            fDsfExecutor.execute(new DsfRunnable() {
                public void run() {
                    command.run();
                }
            });
        }
    }

}
