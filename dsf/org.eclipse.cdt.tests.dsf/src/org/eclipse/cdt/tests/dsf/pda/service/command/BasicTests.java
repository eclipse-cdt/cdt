/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.pda.service.command;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandListener;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDACommandResult;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 */
public class BasicTests extends CommandControlTestsBase {

    @BeforeClass
    public static void setProgram() {
        File programFile = PDAPlugin.getFileInPlugin(new Path("samples/example.pda"));
        fProgram = programFile.getPath();
    }

    @Test
    public void testCommandListener() throws CoreException, InterruptedException, ExecutionException {
        
        class CommandInfo {
            CommandInfo(ICommand<?> command, ICommandResult result) { fCommand = command; fResult = result; }
            ICommand<?> fCommand;
            ICommandResult fResult;
        }
        
        class CommandListener implements ICommandListener {
            
            List<CommandInfo> fDoneCommands = new LinkedList<CommandInfo>(); 
            List<CommandInfo> fQueuedCommands = new LinkedList<CommandInfo>(); 
            List<CommandInfo> fRemovedCommands = new LinkedList<CommandInfo>(); 
            List<CommandInfo> fSentCommands = new LinkedList<CommandInfo>(); 
            
            public void commandDone(ICommandToken token, ICommandResult result) { 
                fDoneCommands.add(new CommandInfo(token.getCommand(), result));
            }
            public void commandQueued(ICommandToken token) {
                fQueuedCommands.add(new CommandInfo(token.getCommand(), null));
            }
            public void commandRemoved(ICommandToken token) {
                fRemovedCommands.add(new CommandInfo(token.getCommand(), null));
            }
            public void commandSent(ICommandToken token) {
                fSentCommands.add(new CommandInfo(token.getCommand(), null));
            }
            
            void reset() {
                fDoneCommands.clear();
                fQueuedCommands.clear();
                fRemovedCommands.clear();
                fSentCommands.clear();
            }
        }
        
        final CommandListener listener = new CommandListener();
        fExecutor.execute(new DsfRunnable() {
            public void run() {
                fCommandControl.addCommandListener(listener);
            }
        });
        
        final PDATestCommand testCommand = new PDATestCommand(fCommandControl.getContext(), "data 1");
        
        // Test sending the command and checking all listeners were called.
        Query<PDACommandResult> sendCommandQuery = new Query<PDACommandResult>() {
            @Override
            protected void execute(DataRequestMonitor<PDACommandResult> rm) {
                fCommandControl.queueCommand(testCommand, rm);
            }
        };
        fExecutor.execute(sendCommandQuery);
        PDACommandResult result = sendCommandQuery.get();
        Assert.assertEquals(1, listener.fQueuedCommands.size());
        Assert.assertEquals(testCommand, listener.fQueuedCommands.get(0).fCommand);
        Assert.assertEquals(0, listener.fRemovedCommands.size());
        Assert.assertEquals(1, listener.fSentCommands.size());
        Assert.assertEquals(testCommand, listener.fSentCommands.get(0).fCommand);
        Assert.assertEquals(1, listener.fDoneCommands.size());
        Assert.assertEquals(testCommand, listener.fDoneCommands.get(0).fCommand);
        Assert.assertEquals(result, listener.fDoneCommands.get(0).fResult);

        // Test queuing then removing command
        listener.reset();
        Query<Object> queueRemoveCommandQuery = new Query<Object>() {
            @Override
            protected void execute(DataRequestMonitor<Object> rm) {
                ICommandToken token = fCommandControl.queueCommand(
                    testCommand, 
                    new DataRequestMonitor<PDACommandResult>(fExecutor, null) {
                        @Override
                        protected void handleCompleted() {
                            Assert.fail("This command should never have been executed.");
                        }
                    });
                fCommandControl.removeCommand(token);

                rm.setData(new Object());
                rm.done();
            }
        };
        fExecutor.execute(queueRemoveCommandQuery);
        queueRemoveCommandQuery.get();
        Assert.assertEquals(1, listener.fQueuedCommands.size());
        Assert.assertEquals(testCommand, listener.fQueuedCommands.get(0).fCommand);
        Assert.assertEquals(1, listener.fRemovedCommands.size());
        Assert.assertEquals(testCommand, listener.fRemovedCommands.get(0).fCommand);
        Assert.assertEquals(0, listener.fSentCommands.size());
        Assert.assertEquals(0, listener.fDoneCommands.size());
        
    }
}
