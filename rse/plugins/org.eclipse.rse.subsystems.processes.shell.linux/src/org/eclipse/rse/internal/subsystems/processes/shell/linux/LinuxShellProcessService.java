/*******************************************************************************
 * Copyright (c) 2006, 2009 MontaVista Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Yu-Fen Kuo       (MontaVista) - initial API and implementation
 * Martin Oberhuber (Wind River) - [refactor] "shell" instead of "ssh" everywhere
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * David McKnight   (IBM)        - [175308] Need to use a job to wait for shell to exit
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable and add Javadoc
 * Martin Oberhuber (Wind River) - [226301][api] IShellService should throw SystemMessageException on error
 * Anna Dushistova  (MontaVista) - [239159] The shell process subsystem not working without the shells subsystem present for the systemType
 * David McKnight   (IBM)        - [272882] [api] Handle exceptions in IService.initService()
 * Anna Dushistova  (MontaVista) - [175300][performance] processes.shell.linux subsystem is slow over ssh
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.processes.shell.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteConstants;
import org.eclipse.rse.services.processes.AbstractProcessService;
import org.eclipse.rse.services.shells.HostShellProcessAdapter;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;

/**
 * class to fetch remote linux target's process info
 *
 */
public class LinuxShellProcessService extends AbstractProcessService {

    private static String COMMAND_GET_SIGNAL_TYPES = "kill -l"; //$NON-NLS-1$

    private static String COMMAND_GET_PROCESSES = "cat /proc/[0-9]*/status"; //$NON-NLS-1$

    private static String COMMAND_KILL_PROCESSES = "kill "; //$NON-NLS-1$

    private String[] statusTypes;

    private LinuxProcessHelper linuxProcessHelper;

    private IHost host;

    /**
     * constructor
     *
     * @param host the connection to work on
     */
    public LinuxShellProcessService(final IHost host) {
        this.host = host;
    }

    public String[] getSignalTypes() {
        if (statusTypes == null)
            statusTypes = internalGetSignalTypes();
        return statusTypes;
    }

    public boolean kill(final long PID, final String signal,
            final IProgressMonitor monitor) throws SystemMessageException {
        String signalString;
        if (signal
                .equals(ISystemProcessRemoteConstants.PROCESS_SIGNAL_TYPE_DEFAULT))
            signalString = ""; //$NON-NLS-1$
        else
            signalString = "-" + signal; //$NON-NLS-1$
        IShellService shellService = Activator.getShellService(host);
        IHostShell hostShell = shellService.launchShell(
                "", null, new NullProgressMonitor()); //$NON-NLS-1$
        hostShell.writeToShell(getKillCommand(PID, signalString));
        Process p = null;
        try {
            p = new HostShellProcessAdapter(hostShell);
            // p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            if (p != null) {
                p.destroy();
            }
            return false;
        }
        // if (p.exitValue() != 0) {
        String errMsg = Activator.getErrorMessage(p.getErrorStream());
        if (!errMsg.trim().equals("")) { //$NON-NLS-1$
            Activator.logErrorMessage(errMsg.toString());
        } else
            return true;
        // }
        return false;
    }

    public IHostProcess[] listAllProcesses(final IHostProcessFilter filter,
            final IProgressMonitor monitor) throws SystemMessageException {
        // this is to workaround RSE bug 147531
        if (filter.getUsername().equals("${user.id}") && host != null) { //$NON-NLS-1$
        	ISubSystem ss = Activator.getSuitableSubSystem(host);
        	if (ss!=null) {
                // change filter username so the filter will filter out the right
                // process for my processes
        		String connectionUserId=ss.getConnectorService().getUserId();
                filter.setUsername(connectionUserId);
        	}
        }
        
        if (monitor != null) {
            monitor.beginTask(
            		LinuxShellProcessResources.LinuxRemoteProcessService_monitor_fetchProcesses,
            		100);
        }
        if(!linuxProcessHelper.isInitialized()){
            // initialize username /uid hashmap before getting any process
            if (monitor != null) {
            	monitor.setTaskName(LinuxShellProcessResources.LinuxShellProcessService_initHelper);
            }
        	linuxProcessHelper.populateUsernames();
            if (monitor != null) {
            	monitor.setTaskName(LinuxShellProcessResources.LinuxRemoteProcessService_monitor_fetchProcesses);
            }
        }
        IShellService shellService = Activator.getShellService(host);
        IHostShell hostShell = shellService.launchShell(
                "", null, new NullProgressMonitor()); //$NON-NLS-1$
        hostShell.writeToShell(getProcessesCommand());
        Process p = null;
        try {
            p = new HostShellProcessAdapter(hostShell);
            // p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            if (p != null) {
                p.destroy();
            }
            return null;
        }
        BufferedReader bufferReader = new BufferedReader(
                new InputStreamReader(p.getInputStream()));

        String nextLine;
        LinuxHostProcess hostProcess = null;
        final ArrayList hostProcessList = new ArrayList();
        try {
            while ((nextLine = bufferReader.readLine()) != null
                    && !nextLine.equals(Activator.DONE_MARKUP_STRING)) {
                if ((hostProcess == null)
                        || LinuxHostProcess.isNewRecord(nextLine)) {
                    if (hostProcess != null) {
                        boolean allows = filter.allows(hostProcess
                                .getStatusLine());
                        if (allows)
                            hostProcessList.add(hostProcess);
                    }
                    hostProcess = new LinuxHostProcess(linuxProcessHelper);
                }

                hostProcess.processLine(nextLine);
                if (progressWorked(monitor, 1)) {
                    break;
                }
            }
            if (hostProcess != null) {
                // add the last record if allows by filter
                boolean allows = filter.allows(hostProcess.getStatusLine());
                if (allows)
                    hostProcessList.add(hostProcess);
            }
            bufferReader.close();
        } catch (IOException e) {
            Activator.log(e);
        }

        // Wait for remote process to exit.
        WaiterJob waiter = new WaiterJob(p);
        waiter.schedule();

        return (IHostProcess[]) hostProcessList
                .toArray(new IHostProcess[hostProcessList.size()]);
    }

    public String getDescription() {
        return LinuxShellProcessResources.LinuxRemoteProcessService_description;
    }

    public String getName() {
        return LinuxShellProcessResources.LinuxRemoteProcessService_name;
    }

    public void initService(final IProgressMonitor monitor) throws SystemMessageException {
    	super.initService(monitor);
        linuxProcessHelper = new LinuxProcessHelper(host);
    }

    private boolean progressWorked(final IProgressMonitor monitor,
            final int work) {
        boolean cancelRequested = false;
        if (monitor != null) {
            monitor.worked(work);
            cancelRequested = monitor.isCanceled();
        }
        return cancelRequested;
    }

    /**
     * Returns a list of the signal types supported by the 'kill' command on
     * this system. Signal Types will be used in the Kill dialog for user to
     * choose which signal they want to use for killing a process.
     *
     * @return a list of the signal types or null if there are none or there is
     *         an error in executing the kill command.
     */
    protected String[] internalGetSignalTypes() {
        IShellService shellService = Activator.getShellService(host);
        Process p = null;
        try {
            IHostShell hostShell = shellService.launchShell("", null, new NullProgressMonitor()); //$NON-NLS-1$
			hostShell.writeToShell(getSignalTypesCommand());
            p = new HostShellProcessAdapter(hostShell);
            // p.waitFor();
        } catch (Exception e) {
            Activator.log(e);
            if (p != null) {
                p.destroy();
            }
            return null;
        }
        BufferedReader bufferReader = new BufferedReader(
                new InputStreamReader(p.getInputStream()));
        String line = null;
        ArrayList lines = null;
        try {

            StringBuffer output = new StringBuffer();
            while ((line = bufferReader.readLine()) != null
                    && !line.equals(Activator.DONE_MARKUP_STRING)) {
                output = output.append(line);
            }
            bufferReader.close();

            if (output.length() > 0) {
                StringTokenizer st = new StringTokenizer(output.toString());
                lines = new ArrayList();
                while (st.hasMoreTokens()) {
                    String token = st.nextToken().trim();
                    if (token.matches("([A-Z]*)")) { //$NON-NLS-1$
                        lines.add(token);
                    }
                }
            }
        } catch (IOException e) {
            Activator.log(e);
        }

        // Wait for remote process to exit.
        WaiterJob waiter = new WaiterJob(p);
        waiter.schedule();

        if (lines == null || lines.size() <= 0) {
            Activator.logErrorMessage(LinuxShellProcessResources.LinuxRemoteProcessService_getSignalTypes_empty);
        } else {
            return (String[]) lines.toArray(new String[lines.size()]);
        }
        return null;

    }

    protected String getSignalTypesCommand() {
        return Activator.formatShellCommand(COMMAND_GET_SIGNAL_TYPES);
    }

    protected String getProcessesCommand() {
        return Activator.formatShellCommand(COMMAND_GET_PROCESSES);
    }

    protected String getKillCommand(final long PID, final String signalString) {
        String cmdLine = COMMAND_KILL_PROCESSES + signalString + " " + PID; //$NON-NLS-1$
        return Activator.formatShellCommand(cmdLine);
    }
}
