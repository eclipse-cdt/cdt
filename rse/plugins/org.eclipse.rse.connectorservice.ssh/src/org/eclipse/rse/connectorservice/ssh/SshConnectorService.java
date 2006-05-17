/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.connectorservice.ssh;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.core.subsystems.AbstractConnectorService;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.ssh.ISshSessionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ccvs.ssh2.CVSSSH2Plugin;
import org.eclipse.team.internal.ccvs.ssh2.ISSHContants;
import org.eclipse.team.internal.ccvs.ui.UserValidationDialog;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/** 
 * Create SSH connections.
 */
public class SshConnectorService extends AbstractConnectorService implements ISshSessionProvider
{
	private static final int SSH_DEFAULT_PORT = 22;
	private static JSch jsch=new JSch();
    private Session session;
//	protected SftpFileService _sftpFileService;

	public SshConnectorService(IHost host) {
		//TODO the port parameter doesnt really make sense here since
		//it will be overridden when the subsystem initializes (through
		//setPort() on our base class -- I assume the port is meant to 
		//be a local port.
		super("SSH Connector Service", "SSH Connector Service Description", host, 0);
	//	_sftpFileService = new SftpFileService(this);
	}

	//----------------------------------------------------------------------
	// <copied from org.eclipse.team.cvs.ssh2>
	//----------------------------------------------------------------------
	private static String current_ssh_home = null;
	private static String current_pkeys = ""; //$NON-NLS-1$

	static String SSH_HOME_DEFAULT = null;
	static {
		String ssh_dir_name = ".ssh"; //$NON-NLS-1$
		
		// Windows doesn't like files or directories starting with a dot.
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			ssh_dir_name = "ssh"; //$NON-NLS-1$
		}
		SSH_HOME_DEFAULT = System.getProperty("user.home"); //$NON-NLS-1$
		if (SSH_HOME_DEFAULT != null) {
		    SSH_HOME_DEFAULT = SSH_HOME_DEFAULT + java.io.File.separator + ssh_dir_name;
		}
	}

	static void loadSshPrefs()
	{
		IPreferenceStore store = CVSSSH2Plugin.getDefault().getPreferenceStore();
		String ssh_home = store.getString(ISSHContants.KEY_SSH2HOME);
		String pkeys = store.getString(ISSHContants.KEY_PRIVATEKEY);

		try {
			if (ssh_home.length() == 0)
				ssh_home = SSH_HOME_DEFAULT;

			if (current_ssh_home == null || !current_ssh_home.equals(ssh_home)) {
				loadKnownHosts(ssh_home);
				current_ssh_home = ssh_home;
			}

			if (!current_pkeys.equals(pkeys)) {
				java.io.File file;
				String[] pkey = pkeys.split(","); //$NON-NLS-1$
				String[] _pkey = current_pkeys.split(","); //$NON-NLS-1$
				current_pkeys = ""; //$NON-NLS-1$
				for (int i = 0; i < pkey.length; i++) {
					file = new java.io.File(pkey[i]);
					if (!file.isAbsolute()) {
						file = new java.io.File(ssh_home, pkey[i]);
					}
					if (file.exists()) {
						boolean notyet = true;
						for (int j = 0; j < _pkey.length; j++) {
							if (pkey[i].equals(_pkey[j])) {
								notyet = false;
								break;
							}
						}
						if (notyet)
							jsch.addIdentity(file.getPath());
						if (current_pkeys.length() == 0) {
							current_pkeys = pkey[i];
						} else {
							current_pkeys += ("," + pkey[i]); //$NON-NLS-1$
						}
					}
				}
			}
		} catch (Exception e) {
		}

	}

    static void loadKnownHosts(String ssh_home){
		try {
		  java.io.File file;
		  file=new java.io.File(ssh_home, "known_hosts"); //$NON-NLS-1$
		  jsch.setKnownHosts(file.getPath());
		} catch (Exception e) {
		}
	}

	//----------------------------------------------------------------------
	// </copied from org.eclipse.team.cvs.ssh2>
	//----------------------------------------------------------------------

	protected void internalConnect(IProgressMonitor monitor) throws Exception
    {
    	//TODO Set known hosts and identities from Preferences
    	//We could share the preferences from ssh2, or use RSE
    	//ConnectorService Properties / Server Launcher Properties
    	
        //jsch.setKnownHosts("/home/foo/.ssh/known_hosts");
		loadSshPrefs();
        String host = getHostName();
        String user = getUserId();

        session=jsch.getSession(user, host, SSH_DEFAULT_PORT);
        session.setPassword(getPasswordInformation().getPassword());
        //session.setPassword("your password");

        // username and password will be given via UserInfo interface.
        UserInfo ui=new MyUserInfo(user);
        session.setUserInfo(ui);

        //java.util.Hashtable config=new java.util.Hashtable();
        //config.put("StrictHostKeyChecking", "no");
        //session.setConfig(config);
        session.connect(3000);   // making connection with timeout.
        
        //now also connect the sftpFileService. It's not very nice to force
        //the connector service know about the file service, and connect it
        //so early -- lazy connect inside the sftpFileService would be better.
        //But the FileServiceSubsystem only calls internalConnect() on the
        //connector service, and doesn't try to connect the file service
        //individually.
        //TODO: From the API point of view, it might be better if the file
        //service had a connect() method and could decide itself if and how
        //it wants to use the connector service.
        //Or could we ensure that the FileService is instanciated only once
        //it needs to be connected? - Currently this is not the case (it is 
        //instanciated very early by the subsystem).
    //    _sftpFileService.connect();
    }

	public void internalDisconnect(IProgressMonitor monitor)
	{
		//TODO: Check, Is disconnect being called because the network (connection) went down?
		//TODO: Fire communication event (aboutToDisconnect) -- see DStoreConnectorService.internalDisconnect()
		//TODO: Wrap exception in an InvocationTargetException -- see DStoreConnectorService.internalDisconnect()
	//	_sftpFileService.disconnect();
		session.disconnect();
	}

	//TODO avoid having jsch type "Session" in the API.
	//Could be done by instanciating SshShellService and SshFileService here,
	//and implementing IShellService getShellService() 
	//and IFileService getFileService().
    public Session getSession() {
    	return session;
    }
    /*
    public IFileService getFileService() {
    	return _sftpFileService;
    }
    */

    private static Display getStandardDisplay() {
    	Display display = Display.getCurrent();
    	if( display==null ) {
    		display = Display.getDefault();
    	}
    	return display;
    }
    
    private static class MyUserInfo implements UserInfo {
    	String fPassphrase;
		String fPassword;
		final String fUser;

		public MyUserInfo(String user) {
			fUser = user;
		}
		public String getPassword() {
			return fPassword;
		}
		public boolean promptYesNo(final String str) {
			//need to switch to UI thread for prompting
			final boolean[] retval = new boolean[1];
			getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					retval[0] = MessageDialog.openQuestion(null, "Warning", str); 
				}
			});
			return retval[0]; 
		}
		private String promptString(final String message) {
			final String[] retval = new String[1];
			getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					//TODO Write our own UserValidationDialog instead of re-using the internal one from team.ssh
					UserValidationDialog uvd = new UserValidationDialog(null, null,
							fUser, message);
					uvd.setUsernameMutable(false);
					if (uvd.open() == uvd.OK) {
						retval[0] = uvd.getPassword();
					} else {
						retval[0] = null;
					}
				}
			});
			return retval[0];
		}
		public String getPassphrase() {
			return fPassphrase;
		}
		public boolean promptPassphrase(String message) {
			fPassphrase = promptString(message);
			return (fPassphrase!=null);
		}
		public boolean promptPassword(final String message) {
			fPassword = promptString(message);
			return (fPassphrase!=null);
		}
		public void showMessage(final String message) {
			getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(null, "Info", message);
				}
			});
		}
	}
    
	public boolean isConnected() {
		return (session!=null && session.isConnected());
	}

	public boolean hasRemoteServerLauncherProperties() {
		return false;
	}

	public boolean supportsRemoteServerLaunching() {
		return false;
	}

	public boolean supportsServerLaunchProperties() {
		return false;
	}

}
