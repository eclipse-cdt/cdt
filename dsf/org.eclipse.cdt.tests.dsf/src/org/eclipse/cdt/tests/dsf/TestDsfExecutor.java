/*******************************************************************************
 *  Copyright (c) 2009 Wind River Systems and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;

/**
 * DsfExecutor for use with unit tests.  It records the exceptions that were 
 * thrown in the executor thread so that they can be re-thrown by the test. 
 *
 */
public class TestDsfExecutor extends DefaultDsfExecutor {
    private List<Throwable> fExceptions = Collections.synchronizedList(new ArrayList<Throwable>());
    
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (r instanceof Future<?>) {
            Future<?> future = (Future<?>)r;
            try {
                if (future.isDone()) {
                    future.get();
                }
                future.get();
            } catch (InterruptedException e) { // Ignore
            } catch (CancellationException e) { // Ignore also
            } catch (ExecutionException e) {
                if (e.getCause() != null) {
                    fExceptions.add(e.getCause());
                }
            }
        }
    }
    
    public boolean exceptionsCaught() {
        return fExceptions.size() != 0;
    }
    
    public Throwable[] getExceptions() {
        synchronized (fExceptions) {
            return fExceptions.toArray(new Throwable[fExceptions.size()]);
        }
    }

}
