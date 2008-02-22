/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     Wind River Systems - adopted to use with DSF
 *******************************************************************************/
package org.eclipse.dd.examples.pda.launch;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.Query;
import org.eclipse.dd.examples.pda.PDAPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.core.sourcelookup.IPersistableSourceLocator2;


/**
 * Launches PDA program on a PDA interpretter written in Perl 
 */
public class PDALaunchDelegate extends LaunchConfigurationDelegate {

    @Override
    public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
        // Need to configure the source locator before creating the launch
        // because once the launch is created and added to launch manager, 
        // the adapters will be created for the whole session, including 
        // the source lookup adapter.
        ISourceLocator locator = getSourceLocator(configuration);

        return new PDALaunch(configuration, mode, locator);
    }

    @Override
    public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
        // PDA programs do not require building.
        return false;
    }

    /**
     * Returns a source locator created based on the attributes in the launch configuration.
     */
    private ISourceLocator getSourceLocator(ILaunchConfiguration configuration) throws CoreException {
        String type = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
        if (type == null) {
            type = configuration.getType().getSourceLocatorId();
        }
        if (type != null) {
            IPersistableSourceLocator locator = DebugPlugin.getDefault().getLaunchManager().newSourceLocator(type);
            String memento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String)null);
            if (memento == null) {
                locator.initializeDefaults(configuration);
            } else {
                if(locator instanceof IPersistableSourceLocator2)
                    ((IPersistableSourceLocator2)locator).initializeFromMemento(memento, configuration);
                else
                    locator.initializeFromMemento(memento);
            }
            return locator;
        }
        return null;
    }

    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
        String program = configuration.getAttribute(PDAPlugin.ATTR_PDA_PROGRAM, (String)null);
        if (program == null) {
            abort("Perl program unspecified.", null);
        }

        int requestPort = findFreePort();
        int eventPort = findFreePort();
        if (requestPort == -1 || eventPort == -1) {
            abort("Unable to find free port", null);
        }

        launchProcess(launch, program, requestPort, eventPort);
        PDALaunch pdaLaunch = (PDALaunch)launch; 
        initServices(pdaLaunch, program, requestPort, eventPort);
    }

    /**
     * Launches PDA interpreter with the given program.
     *  
     * @param launch Launch that will contain the new process.
     * @param program PDA program to use in the interpreter.
     * @param requestPort The port number for connecting the request socket.
     * @param eventPort The port number for connecting the events socket.
     * 
     * @throws CoreException 
     */
    private void launchProcess(ILaunch launch, String program, int requestPort, int eventPort) throws CoreException {
        List<String> commandList = new ArrayList<String>();

        // Find Perl executable
        IValueVariable perl = VariablesPlugin.getDefault().getStringVariableManager().getValueVariable(PDAPlugin.VARIALBE_PERL_EXECUTABLE);
        if (perl == null) {
            abort("Perl executable location undefined. Check value of ${dsfPerlExecutable}.", null);
        }
        String path = perl.getValue();
        if (path == null) {
            abort("Perl executable location unspecified. Check value of ${dsfPerlExecutable}.", null);
        }
        File exe = new File(path);
        if (!exe.exists()) {
            abort(MessageFormat.format("Specified Perl executable {0} does not exist. Check value of $dsfPerlExecutable.", new Object[]{path}), null);
        }
        commandList.add(path);

        // Add PDA VM
        File vm = PDAPlugin.getFileInPlugin(new Path("pdavm/pda.pl"));
        if (vm == null) {
            abort("Missing PDA VM", null);
        }
        commandList.add(vm.getAbsolutePath());

        // Add PDA program
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(program));
        if (!file.exists()) {
            abort(MessageFormat.format("Perl program {0} does not exist.", new Object[] {file.getFullPath().toString()}), null);
        }

        commandList.add(file.getLocation().toOSString());

        // Add debug arguments - i.e. '-debug requestPort eventPort'
        commandList.add("-debug");
        commandList.add("" + requestPort);
        commandList.add("" + eventPort);

        // Launch the perl process.
        String[] commandLine = commandList.toArray(new String[commandList.size()]);
        Process process = DebugPlugin.exec(commandLine, null);

        // Create a debug platform process object and add it to the launch.
        DebugPlugin.newProcess(launch, process, path);
    }

    /**
     * Calls the launch to initialize DSF services for this launch.
     */
    private void initServices(final PDALaunch pdaLaunch, final String program, final int requestPort, final int eventPort) 
    throws CoreException 
    {
        // Synchronization object to use when waiting for the services initialization.
        Query<Object> initQuery = new Query<Object>() {
            @Override
            protected void execute(DataRequestMonitor<Object> rm) {
                pdaLaunch.initializeServices(program, requestPort, eventPort, rm);
            }
        };

        // Submit the query to the executor.
        pdaLaunch.getSession().getExecutor().execute(initQuery);
        try {
            // Block waiting for query results.
            initQuery.get();
        } catch (InterruptedException e1) {
            throw new DebugException(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, "Interrupted Exception in dispatch thread", e1)); //$NON-NLS-1$
        } catch (ExecutionException e1) {
            throw new DebugException(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Error in launch sequence", e1.getCause())); //$NON-NLS-1$
        }
    }

    /**
     * Throws an exception with a new status containing the given
     * message and optional exception.
     * 
     * @param message error message
     * @param e underlying exception
     * @throws CoreException
     */
    private void abort(String message, Throwable e) throws CoreException {
        throw new CoreException(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, 0, message, e));
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
}
