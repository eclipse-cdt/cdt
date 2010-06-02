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
package org.eclipse.cdt.tests.dsf.pda.service.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.pda.service.PDABackend;
import org.eclipse.cdt.examples.dsf.pda.service.PDACommandControl;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDACommandResult;
import org.eclipse.cdt.tests.dsf.pda.util.Launching;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Before;

/**
 * 
 */
public class CommandControlTestsBase {

    protected static String fProgram;
    
    protected DsfExecutor fExecutor;
    protected DsfSession fSession;
    protected PDABackend fPDABackend;
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

        fExecutor = new DefaultDsfExecutor();
        fSession = DsfSession.startSession(fExecutor, "PDA Test");

        Process proc = Launching.launchPDA(fSession, null, fProgram);
        Assert.assertNotNull(proc);
        
        // Remember the backend service of this session.
        // Note this must be called after the above LaunchPDA().
        fPDABackend = Launching.getBackendService();
        
        fOutputReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        Assert.assertTrue(fOutputReader.readLine().contains("-debug"));
        
        fCommandControl = new PDACommandControl(fSession);

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
    	if (fOutputReader != null) {
    		fOutputReader.close();
    	}
        
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

        class ShutdownBackendServiceQuery extends Query<Object> {
            @Override
            protected void execute(DataRequestMonitor<Object> rm) {
                fPDABackend.shutdown(rm);
            }
        };
        
        if (fExecutor != null) {
            ShutdownBackendServiceQuery shutdownQuery = new ShutdownBackendServiceQuery();
            fExecutor.execute(shutdownQuery);
            shutdownQuery.get();
        }
    }
    
    protected void sendCommand(String command)  throws Throwable {
        sendCommand(command, "ok");
    }

    protected void sendCommand(String command, String expectedResult) throws Throwable {

        final PDATestCommand testCommand = new PDATestCommand(fCommandControl.getContext(), command);
        
        // Test sending the command and checking all listeners were called.
        Query<PDACommandResult> sendCommandQuery = new Query<PDACommandResult>() {
            @Override
            protected void execute(DataRequestMonitor<PDACommandResult> rm) {
                fCommandControl.queueCommand(testCommand, rm);
            }
        };

        String responseText = null;
        fExecutor.execute(sendCommandQuery);
        try {
            PDACommandResult result = sendCommandQuery.get();
            responseText = result.fResponseText;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof CoreException) {
                responseText = ((CoreException)e.getCause()).getStatus().getMessage();
            } else {
                throw e.getCause();
            }
        }
        Assert.assertEquals("Command returned an unexpected result", expectedResult, responseText);

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
