/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;

/**
 * A command control which delays the <strong>results</strong> of commands 
 * sent to a command control, as well as events from the command control.  
 * The delay is specified in the constructor using a number of executor 
 * dispatch cycles.
 * 
 * @since 2.1
 */
public class BufferedCommandControl implements ICommandControl {

    private DsfExecutor fExecutor;
    private ICommandControl fControlDelegate;
    private int fDepth;
    
    private ICommandListener fCommandListener = new ICommandListener() {
        @Override
       public void commandQueued(ICommandToken token) {
            for (ICommandListener processor : fCommandProcessors) {
                processor.commandQueued(token);
            }
        };
            
        @Override
        public void commandRemoved(ICommandToken token) {
            for (ICommandListener processor : fCommandProcessors) {
                processor.commandRemoved(token);
            }
        };
            
        @Override
        public void commandSent(final ICommandToken token) {
            for (ICommandListener processor : fCommandProcessors) {
                processor.commandSent(token);
            }
        };
        
        @Override
        public void commandDone(final ICommandToken token, final ICommandResult result) {
            buffer(fDepth, new DsfRunnable() {
                @Override
                public void run() {
                    for (ICommandListener processor : fCommandProcessors) {
                        processor.commandDone(token, result);
                    }
                };
            });
        };
    };

    private IEventListener fEventListener = new IEventListener() {
        @Override
        public void eventReceived(final Object output) {
            buffer(fDepth, new DsfRunnable() {
                @Override
                public void run() {
                    for (IEventListener processor : fEventProcessors) {
                        processor.eventReceived(output);
                    }                    
                };
            });
        }
    };
    
    private final List<ICommandListener> fCommandProcessors = new ArrayList<ICommandListener>();
    private final List<IEventListener>   fEventProcessors = new ArrayList<IEventListener>();
    
    public BufferedCommandControl(ICommandControl controlDelegate, DsfExecutor executor, int depth) {
        fControlDelegate = controlDelegate;
        fExecutor = executor;
        fDepth = depth;
        assert fDepth > 0;
    }
    
    @Override
    public void addCommandListener(ICommandListener listener) {
        if (fCommandProcessors.isEmpty()) {
            fControlDelegate.addCommandListener(fCommandListener);
        }
        fCommandProcessors.add(listener);
    }


    @Override
    public void removeCommandListener(ICommandListener listener) {
        fCommandProcessors.remove(listener);
        if (fCommandProcessors.isEmpty()) {
            fControlDelegate.removeCommandListener(fCommandListener);
        }
    }

    @Override
    public void addEventListener(IEventListener listener) {
        if (fEventProcessors.isEmpty()) {
            fControlDelegate.addEventListener(fEventListener);
        }
        fEventProcessors.add(listener);
    }

    @Override
    public void removeEventListener(IEventListener listener) {
        fEventProcessors.remove(listener);
        if (fEventProcessors.isEmpty()) {
            fControlDelegate.removeEventListener(fEventListener);
        }
    }

    @Override
    public <V extends ICommandResult> ICommandToken queueCommand(final ICommand<V> command, final DataRequestMonitor<V> rm) {
        return fControlDelegate.queueCommand(
            command, 
            new DataRequestMonitor<V>(ImmediateExecutor.getInstance(), rm) {
                @Override
                protected void handleCompleted() {
                    buffer(fDepth, new DsfRunnable() {
                        @Override
                        public void run() {
                            rm.setData(getData());
                            rm.setStatus(getStatus());
                            rm.done();
                        }
                    });
                }
            });
    }

    @Override
    public void removeCommand(ICommandToken token) {
        fControlDelegate.removeCommand(token);
    }
    
    private void buffer(final int depth, final DsfRunnable runnable) {
        if (depth == 0) {
            runnable.run();
        } else {
            fExecutor.execute(new DsfRunnable() {
                @Override
                public void run() {
                    buffer(depth - 1, runnable);
                }
            });
        }
    }

}
