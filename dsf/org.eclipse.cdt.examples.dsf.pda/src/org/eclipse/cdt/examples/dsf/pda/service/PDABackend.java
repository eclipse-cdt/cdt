/*******************************************************************************
 * Copyright (c) 2008, 2009 Nokia Corporation.
 * All rights reserved. This fProgram and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nokia - initial version. Sep 28, 2008
 *******************************************************************************/

package org.eclipse.cdt.examples.dsf.pda.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import com.ibm.icu.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.concurrent.Sequence.Step;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.Launch;
import org.osgi.framework.BundleContext;

/**
 * Service that manages the backend process: starting the process
 * and monitoring for its shutdown.
 */
public class PDABackend extends AbstractDsfService {

	private int fRequestPort;
	private int fEventPort;

	@ThreadSafe
    private OutputStream fRequestOutputStream;
    @ThreadSafe
    private InputStream fRequestInputStream;
    @ThreadSafe
    private InputStream fEventInputStream;
	
	private String fProgram;
	private Process fBackendProcess;
	private String fBackendProcessName;
	
	/**
	 * 
	 * @param session
	 * @param launch - can be null, e.g. for JUnit test.
	 * @param program - can be a full path or a workspace resource path, must denote an existing file.
	 */
	public PDABackend(DsfSession session, Launch launch, String program) {
		super(session);
		
		fProgram = program;
	}

	@Override
	protected BundleContext getBundleContext() {
        return PDAPlugin.getBundleContext();
	}
	
	public Process getProcess() {
		return fBackendProcess;
	}
	
	public String getProcessName() {
	    return fBackendProcessName;
	}

	public String getPorgramName() {
	    return fProgram;
	}
	
    @Override
    public void initialize(final RequestMonitor rm) {
        super.initialize(new RequestMonitor(getExecutor(), rm) {
            @Override
            protected void handleSuccess() {
                doInitialize(rm);
            }
        });
    }

    private void doInitialize(final RequestMonitor requestMonitor) {

        final Sequence.Step[] initializeSteps = new Sequence.Step[] {
            new Step() {
            	// Launch the back end debugger process.

				@Override
				public void execute(final RequestMonitor rm) {
					
					new Job("Start PDA Virtual Machine") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								fBackendProcess = launchPDABackendDebugger();
							} catch (CoreException e) {
								rm.setStatus(e.getStatus());
							}
							rm.done();

							return Status.OK_STATUS;
						}
					}.schedule();
				}

				@Override
				public void rollBack(RequestMonitor rm) {
					if (fBackendProcess != null)
						fBackendProcess.destroy();
					
					rm.done();
				}
            	
            },
            new Step() {
				@Override
				public void execute(final RequestMonitor rm) {
		            
			        // To avoid blocking the DSF dispatch thread use a job to initialize communication sockets.  
			        new Job("PDA Socket Initialize") {
			            @Override
			            protected IStatus run(IProgressMonitor monitor) {
			                try {
			                    // give interpreter a chance to start
			                    try {
			                        Thread.sleep(1000);
			                    } catch (InterruptedException e) {
			                    }
			                    Socket socket = new Socket("localhost", fRequestPort);
			                    fRequestOutputStream = socket.getOutputStream();
			                    fRequestInputStream = socket.getInputStream();
			                    // give interpreter a chance to open next socket
			                    try {
			                        Thread.sleep(1000);
			                    } catch (InterruptedException e) {
			                    }

			                    socket = new Socket("localhost", fEventPort);
			                    fEventInputStream = socket.getInputStream();

			                } catch (UnknownHostException e) {
			                	rm.setStatus(new Status(
			                        IStatus.ERROR, PDAPlugin.PLUGIN_ID, REQUEST_FAILED, "Unable to connect to PDA VM", e));
			                } catch (IOException e) {
			                	rm.setStatus(new Status(
			                        IStatus.ERROR, PDAPlugin.PLUGIN_ID, REQUEST_FAILED, "Unable to connect to PDA VM", e));
			                }
		                	rm.done();

		                	return Status.OK_STATUS;
			            }
			        }.schedule();
				}
            },

            new Step() {	// register the service
				@Override
				public void execute(RequestMonitor rm) {
			        // Register this service
			        register(new String[] { PDABackend.class.getName() },
			        		 new Hashtable<String, String>());

			        rm.done();
				}
            },
        };

        Sequence startupSequence = new Sequence(getExecutor(), requestMonitor) {
            @Override public Step[] getSteps() { return initializeSteps; }
        };
        getExecutor().execute(startupSequence);
    }

    /**
     * Returns a free port number on localhost, or -1 if unable to find a free port.
     */
    public static int findFreePort() {
        ServerSocket socket= null;
        try {
            socket= new ServerSocket(0);
            return socket.getLocalPort();
        } catch (IOException e) { 
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        return -1;		
    }		

    private void abort(String message, Throwable e) throws CoreException {
        throw new CoreException(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, 0, message, e));
    }

    private Process launchPDABackendDebugger() throws CoreException {
        
    	List<String> commandList = new ArrayList<String>();

        // Get Java VM path
        String javaVMHome = System.getProperty("java.home");
        String javaVMExec = javaVMHome + File.separatorChar + "bin" + File.separatorChar + "java";
        if (File.separatorChar == '\\') {
            javaVMExec += ".exe";
        }   
        File exe = new File(javaVMExec);
        if (!exe.exists()) {
            abort(MessageFormat.format("Specified java VM executable {0} does not exist.", new Object[]{javaVMExec}), null);
        }
        
        fBackendProcessName = javaVMExec;
        
        commandList.add(javaVMExec);
        
        commandList.add("-cp");
        try {
        commandList.add(
            File.pathSeparator + PDAPlugin.getFileInPlugin(new Path("bin")) + 
            File.pathSeparator + new File(Platform.asLocalURL(PDAPlugin.getDefault().getDescriptor().getInstallURL()).getFile()));
        } catch (IOException e) {
        }
        
        commandList.add("org.eclipse.cdt.examples.pdavm.PDAVirtualMachine");

        String absolutePath = fProgram;
        
        // check if fProgram is already a full path of an existing file
        // Note if "fProgram" is workspace resource path like /ProjectName/file.pda, we should not 
        // change it to absolute path, otherwise the breakpoints in the PDA file won't work.
        // See PDABreakpoints.doInsertBreakpoint() for more.
        File f = new File(fProgram);
        if (! f.exists()) {
        	// Try to locate it in workspace
        	IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fProgram));
        	if (file.exists())
        		absolutePath = file.getLocation().toPortableString();
        	else
        		abort(MessageFormat.format("PDA program {0} does not exist.", new Object[] {file.getFullPath().toPortableString()}), null);
        }

        commandList.add(absolutePath);
        
        fRequestPort = findFreePort();
        fEventPort = findFreePort();
      
        if (fRequestPort == -1 || fEventPort == -1) {
            abort("Unable to find free port", null);
        }
        
        // Add debug arguments - i.e. '-debug fRequestPort fEventPort'
        commandList.add("-debug");
        commandList.add("" + fRequestPort);
        commandList.add("" + fEventPort);

        // Launch the perl process.
        String[] commandLine = commandList.toArray(new String[commandList.size()]);
        
        PDAPlugin.debug("Start PDA Virtual Machine:\n" + commandList);
        
        Process process = DebugPlugin.exec(commandLine, null);

        return process;
	}

	@Override
    public void shutdown(final RequestMonitor rm) {
		fBackendProcess.destroy();

		try {
			if (fRequestInputStream != null)
				fRequestInputStream.close();
			if (fRequestOutputStream != null)
				fRequestOutputStream.close();
			if (fEventInputStream != null)
				fEventInputStream.close();
		} catch (IOException e) {
			// ignore
		}
		
        unregister();
        rm.done();
    }

	/*
	 * =========== Following are PDA debugger specific ====================
	 * 
	 *  Caller should make sure these are called after the PDABackend is initialized.
	 */
	public OutputStream getRequestOutputStream() {
		return fRequestOutputStream;
	}

	public InputStream getRequestInputStream() {
		return fRequestInputStream;
	}

	public InputStream getEventInputStream() {
		return fEventInputStream;
	}
}
