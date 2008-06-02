/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.tests.pda.service.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.Query;
import org.eclipse.dd.dsf.debug.service.command.IEventListener;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.examples.pda.service.PDACommandControl;
import org.eclipse.dd.examples.pda.service.commands.PDACommandResult;
import org.eclipse.dd.tests.pda.util.Launching;
import org.junit.After;
import org.junit.Before;

/**
 * 
 */
public class CommandControlTestsBase {

    protected static String fProgram;
    
    protected DsfExecutor fExecutor;
    protected DsfSession fSession;
    protected Process fPDAProcess;
    protected PDACommandControl fCommandControl;
    private BlockingQueue<Object> fEventsQueue = new LinkedBlockingQueue<Object>();

    private BufferedReader fOutputReader;
    
    @Before
    public void startup() throws CoreException, InterruptedException, ExecutionException, IOException {
        
        class InitializeCommandServiceQuery extends Query<Object> {
            @Override
            protected void execute(DataRequestMonitor<Object> rm) {
                fCommandControl.initialize(rm);
            }
        };

        int requestPort = Launching.findFreePort();
        int eventPort = Launching.findFreePort();

        fPDAProcess = Launching.launchPDA(fProgram, requestPort, eventPort);
        fOutputReader = new BufferedReader(new InputStreamReader(fPDAProcess.getInputStream()));
        Assert.assertEquals("-debug " + requestPort + " " + eventPort, fOutputReader.readLine());
        
        fExecutor = new DefaultDsfExecutor();
        fSession = DsfSession.startSession(fExecutor, "PDA Test");
        fCommandControl = new PDACommandControl(fSession, fProgram, requestPort, eventPort);

        fCommandControl.addEventListener(new IEventListener() {
            public void eventReceived(Object output) {
                fEventsQueue.add(output);
            }
        });
        
        InitializeCommandServiceQuery initQuery = new InitializeCommandServiceQuery();
        fExecutor.execute(initQuery);
        initQuery.get();        
        Assert.assertEquals("debug connection accepted", fOutputReader.readLine());
    }
    
    @After
    public void shutdown() throws CoreException, InterruptedException, ExecutionException, IOException {
        fOutputReader.close();
        fPDAProcess.destroy();
        
        class ShutdownCommandServiceQuery extends Query<Object> {
            @Override
            protected void execute(DataRequestMonitor<Object> rm) {
                fCommandControl.shutdown(rm);
            }
        };
        
        if (fExecutor != null) {
            ShutdownCommandServiceQuery shutdownQuery = new ShutdownCommandServiceQuery();
            fExecutor.execute(shutdownQuery);
            shutdownQuery.get();
        }
    }
    
    protected void sendCommand(String command)  throws Throwable {
        sendCommand(command, "ok");
    }

    protected void sendCommand(String command, String expectedResult) throws Throwable {

        final PDATestCommand testCommand = new PDATestCommand(fCommandControl.getProgramDMContext(), command);
        
        // Test sending the command and checking all listeners were called.
        Query<PDACommandResult> sendCommandQuery = new Query<PDACommandResult>() {
            @Override
            protected void execute(DataRequestMonitor<PDACommandResult> rm) {
                fCommandControl.queueCommand(testCommand, rm);
            }
        };
        
        fExecutor.execute(sendCommandQuery);
        try {
            PDACommandResult result = sendCommandQuery.get();
            Assert.assertEquals("Command returned an unexpected result", expectedResult, result.fResponseText);
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }
    
    protected void clearEvents() {
        fEventsQueue.clear();
    }
    
    protected void expectEvent(String expectedEvent) throws InterruptedException {
        Assert.assertEquals("Unexpected event received", expectedEvent, fEventsQueue.take());
    }
    
    protected void expectOutput(String expectedOutput) throws IOException {
        Assert.assertEquals("Unexpected output received", expectedOutput, fOutputReader.readLine());
    }
    
}
