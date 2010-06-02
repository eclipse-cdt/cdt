/*******************************************************************************
 * Copyright (c) 2008, 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nokia - initial implementation. Oct. 2008
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.tests.dsf.TestDsfExecutor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test whether a step in a sequence can control the progress monitor. 
 */
public class DsfSequenceProgressTests {
    private List<Throwable> fExceptions = Collections.synchronizedList(new ArrayList<Throwable>());
    TestDsfExecutor fExecutor;
    
    @Before 
    public void startExecutor() throws ExecutionException, InterruptedException {
        fExecutor = new TestDsfExecutor();
    }   
    
    @After 
    public void shutdownExecutor() throws ExecutionException, InterruptedException {
        fExecutor.submit(new DsfRunnable() { public void run() {
            fExecutor.shutdown();
        }}).get();
        if (fExecutor.exceptionsCaught()) {
            Throwable[] exceptions = fExecutor.getExceptions();
            throw new ExecutionException(exceptions[0]);
        }
        fExecutor = null;
    }
    
    // Create a counter for tracking number of steps performed and steps 
    // rolled back.
    class IntegerHolder { int fInteger; }
    final IntegerHolder stepCounter = new IntegerHolder();
    final IntegerHolder rollBackCounter = new IntegerHolder();

    class SleepStep extends Sequence.Step {
    	
        @Override
		public int getTicks() {
			return 6;
		}
        
		@Override public void execute(RequestMonitor requestMonitor) {
        	stepCounter.fInteger++;
        	
        	sleep(getTicks(), requestMonitor, null);
        	
            requestMonitor.done(); 
        }
		
        @Override public void rollBack(RequestMonitor requestMonitor) {
            rollBackCounter.fInteger++;

        	sleep(1, null, null);

        	requestMonitor.done(); 
        }
        
    }
    
    class SleepStepWithProgress extends Sequence.StepWithProgress {
    	
        @Override
		public int getTicks() {
			return 6;
		}
        
        private final static int SUB_TICKS = 3;
        
		@Override 
		public void execute(RequestMonitor rm, IProgressMonitor pm) {
        	stepCounter.fInteger++;
        	
        	
            // step has its own sub-progress ticks which take the total ticks
        	// of this step and divides them into subticks
        	pm.beginTask(getTaskName() + ": ", SUB_TICKS);
        	sleep(SUB_TICKS, rm, pm);
        	
            rm.done(); 
            pm.done();
        }
		
        @Override 
        public void rollBack(RequestMonitor rm) {
            rollBackCounter.fInteger++;

        	sleep(2, null, null);
        	rm.done(); 
        }
        
    }
    
    @Test
    /**
     * It's better to run this as a manual interactive test. Run this as a JUnit
     * plugin test.<br>
     * <br>
     * In the test workbench, watch the progress bar in the Progress View.<br>
     * <br>
     * During execution of a StepWithProgress, you should see the progress bar
     * is growing and you can have more responsive cancel.<br>
     * <br>
     * Meanwhile, during execution of a step without progress, you should see
     * that progress bar does not grow and cancel does not work until end of the
     * step.<br>
     * <br>
     * Also watch that when you cancel the progress bar during the execution of
     * the sequence, you should see that "Rollback.." appears in the progress bar 
     * label.<br>
     */    
    public void sequenceProgressTest() throws InterruptedException, ExecutionException {

        final Sequence.Step[] steps = new Sequence.Step[] {
        	
            new SleepStepWithProgress() {
				@Override
				public String getTaskName() {
					return "StepWithProgress #1";
				}},

            new SleepStepWithProgress() {
				@Override
				public String getTaskName() {
					return "StepWithProgress #2";
				}},

			new SleepStep() {
				@Override
				public String getTaskName() {
					return "Step #3";
				}},

			new SleepStep() {
				@Override
				public String getTaskName() {
					return "Step #4";
				}},
        };
    	

        fExceptions.clear();
        
        Job myJob = new Job("Run test sequence") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
		        // Create and start.
		        Sequence sequence = new Sequence(fExecutor, monitor, "Run my sequence", "Rollback my sequence") {
		            @Override public Step[] getSteps() { return steps; }
		        };
		        fExecutor.execute(sequence);
		     
		        // Block and wait for sequence to complete.
		        try {
		            sequence.get();
		        } catch (InterruptedException e) {
					// ignore here. 
				} catch (ExecutionException e) {
					// Expected exception, ignore here. 
				} finally {
					try {
						System.out.println("StepCounter: " + stepCounter.fInteger);
						System.out.println("RollBackCounter: " + rollBackCounter.fInteger);
						
						if (sequence.isCancelled())
							Assert.assertTrue(
									"Wrong number of steps were rolled back after cancellation.", 
									stepCounter.fInteger == rollBackCounter.fInteger);
						else {
							Assert.assertTrue(
									"Wrong number of steps executed.", 
									stepCounter.fInteger == steps.length);
							Assert.assertTrue(
									"Some steps are mistakenly rolled back", 
									rollBackCounter.fInteger == 0);
						}
						
			            // Check state from Future interface
			            Assert.assertTrue(sequence.isDone());
					} catch (AssertionFailedError e) {
						fExceptions.add(e);
					}
		        }
				return null;
			}};
        
		myJob.schedule();
		
		// Wait for the job to finish
		waitForJob(myJob);
		
		// now throw any assertion errors.
		if (fExceptions.size() > 0)
			throw (AssertionFailedError)fExceptions.get(0);

    }

    private static void sleep(int seconds, RequestMonitor rm, IProgressMonitor pm) {
    	try {
    		for (int i = 0; i < seconds; i++) {
    		    if (pm != null)
    		        pm.subTask("subStep - " + (i+1));
    		               
    		    Thread.sleep(1000);
    			
    			if (pm != null) {
    				pm.worked(1);

    				if (pm.isCanceled()) {
    					return;
    				}
    			}
    			
    			if (rm != null && rm.isCanceled()) {
    				return;
    			}
    		}
    		
		} catch (InterruptedException e) {
			// ignore
		}
    }
    
    // Wait for a job to finish without possible blocking of UI thread.
    //
    private static void waitForJob(Job job) {
		Display display = Display.getCurrent();
		while (true) {
			IStatus status = job.getResult();
			if (status != null)
				break;
			if (display != null) {
				while (display.readAndDispatch()) ;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				job.cancel();
				break;
			}
		}
	}
}
