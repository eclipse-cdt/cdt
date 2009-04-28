/********************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Yu-Fen Kuo       (MontaVista) - adapted from RSE UniversalLinuxProcessHandler
 * Martin Oberhuber (Wind River) - [refactor] "shell" instead of "ssh" everywhere
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * David McKnight   (IBM)        - [175308] Need to use a job to wait for shell to exit
 * Martin Oberhuber (Wind River) - [226301][api] IShellService should throw SystemMessageException on error
 * Anna Dushistova  (MontaVista) - [175300][performance] processes.shell.linux subsystem is slow over ssh
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.processes.shell.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteConstants;
import org.eclipse.rse.services.shells.HostShellProcessAdapter;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;

/**
 * Helper class that helps to get state code and user name info most of the code
 *
 */
public class LinuxProcessHelper {
    private HashMap stateMap;

    private HashMap _usernamesByUid;

    private HashMap _uidsByUserName;
    
    private IHost _host;

    private static String COMMAND_GET_PASSWD = "getent passwd"; //$NON-NLS-1$

    /**
     * constructor
     */
    public LinuxProcessHelper(IHost host) {
        super();
        stateMap = new HashMap();
        for (int i = ISystemProcessRemoteConstants.STATE_STARTING_INDEX; i < ISystemProcessRemoteConstants.STATE_ENDING_INDEX; i++) {
            stateMap.put(new Character(ISystemProcessRemoteConstants.ALL_STATES[i]), ISystemProcessRemoteConstants.ALL_STATES_STR[i]);
        }
        _host = host;
    }

    /**
     * this code is adapted from
     * org.eclipse.rse.services.clientserver.processes.handlers.UniversalLinuxProcessHandler
     */
    public String convertToStateCode(String state) {
        String stateCode = " "; //$NON-NLS-1$
        if (state == null)
            return stateCode;
        if (state.trim().equals("")) //$NON-NLS-1$
            return stateCode;
        for (int i = 0; i < state.length(); i++) {
            String nextState = (String) stateMap.get(new Character(state
                    .charAt(i)));
            if (nextState != null) {
                stateCode = stateCode + nextState;
                if (i < state.length() - 1)
                    stateCode = stateCode + ","; //$NON-NLS-1$
            }
        }
        if (stateCode.trim().equals("")) //$NON-NLS-1$
            return " "; //$NON-NLS-1$
        else
            return stateCode.trim();
    }

    /**
     * this code is adapted from
     * org.eclipse.rse.services.clientserver.processes.handlers.UniversalLinuxProcessHandler
     */
    public void populateUsernames() {
        if (_usernamesByUid != null && _uidsByUserName != null || _host == null)
            return;
        _usernamesByUid = new HashMap();
        _uidsByUserName = new HashMap();

        IShellService shellService = Activator.getShellService(_host);
        Process p = null;
        try {
            IHostShell hostShell = shellService.launchShell("", null, new NullProgressMonitor()); //$NON-NLS-1$
			hostShell.writeToShell(getUserNameCommand());
            p = new HostShellProcessAdapter(hostShell);
            // when p.waitFor() is called here, the hostShell.isActive() always
            // return true.
            // p.waitFor();
        } catch (Exception e) {
            Activator.log(e);
            if (p != null) {
                p.destroy();
            }
            return;
        }
        BufferedReader bufferReader = new BufferedReader(
                new InputStreamReader(p.getInputStream()));

        String nextLine;
        try {
            while ((nextLine = bufferReader.readLine()) != null
                    && !nextLine.equals(Activator.DONE_MARKUP_STRING)) {
                String[] fields = nextLine.split(":"); //$NON-NLS-1$
                int length = fields.length;
                if (length < 3)
                    continue;
                String uid = fields[2];
                String username = fields[0];
                if (uid != null && username != null) {
                    _usernamesByUid.put(uid, username);
                    _uidsByUserName.put(username, uid);
                }
            }
            bufferReader.close();
        } catch (IOException e) {
            Activator.log(e);
        }

        // Wait for remote process to exit.
        WaiterJob waiter = new WaiterJob(p);
        waiter.schedule();
    }

    /**
     * Gets the uid associated with the given username on this system
     */
    public String getUid(String username) {
        if (_uidsByUserName != null)
            return (String) _uidsByUserName.get(username);
        return ""; //$NON-NLS-1$
    }

    /**
     * Gets the username associated with the given uid on this system
     */
    public String getUsername(String uid) {
        String username = null;
        if (_usernamesByUid != null)
            username = (String) _usernamesByUid.get(uid);
        if (username != null && !username.equals("")) //$NON-NLS-1$
            return username;
        return uid;
    }

    protected String getUserNameCommand() {
        return Activator.formatShellCommand(COMMAND_GET_PASSWD);
    }
    
    public boolean isInitialized(){
        if (_usernamesByUid != null && _uidsByUserName != null){
    		return true;
    	}
    	return false;
    }
}
