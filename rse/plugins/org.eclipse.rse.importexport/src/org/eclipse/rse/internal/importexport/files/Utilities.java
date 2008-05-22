/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [180562][api] dont implement IRemoteImportExportConstants
 * Martin Oberhuber (Wind River) - [174945] split importexport icons from rse.ui
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 *******************************************************************************/
package org.eclipse.rse.internal.importexport.files;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.internal.importexport.IRemoteImportExportConstants;
import org.eclipse.rse.internal.importexport.RemoteImportExportPlugin;
import org.eclipse.rse.internal.importexport.RemoteImportExportResources;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Contains several helper methods.
 * A lot of these should really be provided by comm. layer, but for
 * many reasons they were not.  oh well ....
 */
public class Utilities {
	public static final String IMPORT_DESCRIPTION_EXTENSION = IRemoteImportExportConstants.REMOTE_FILE_IMPORT_DESCRIPTION_FILE_EXTENSION;
	public static final String EXPORT_DESCRIPTION_EXTENSION = IRemoteImportExportConstants.REMOTE_FILE_EXPORT_DESCRIPTION_FILE_EXTENSION;

	/**
	 * Use this method to get IRemoteFile object from SystemConnection, and path
	 * 
	 */
	public static IRemoteFile getIRemoteFile(IHost c, String path) {
		IRemoteFile ret = null;
		if (c != null) {
			try {
				IRemoteFileSubSystem ss = RemoteFileUtility.getFileSubSystem(c);
				
				char sep = ss.getSeparatorChar();
				if (sep != '/')
				{
					// on windows we need win path
					path = path.replace('/', sep);
				}
				
				ret = ss.getRemoteFileObject(path, new NullProgressMonitor());
			} catch (SystemMessageException e) {
				// get RemoteFileObject has been changed to raise
				// SystemMessageException.
				error(e);
			}
		}
		return ret;
	}

	/**
	 * Use this method to get selected string from an
	 * IRemoteFile object.
	 */
	public static String getAsString(IRemoteFile selectedDirectory) {
		return selectedDirectory.getHost().getSystemProfileName() + '.' + selectedDirectory.getHost().getAliasName() + ":" + selectedDirectory.getAbsolutePath(); //$NON-NLS-1$
	}

	/**
	 * Use this method to get selected string from an
	 * UniFilePlus object.
	 */
	public static String getAsString(UniFilePlus selectedDirectory) {
		return selectedDirectory.remoteFile.getHost().getSystemProfileName() + '.' + selectedDirectory.remoteFile.getHost().getAliasName() + ":" + selectedDirectory.getPath(); //$NON-NLS-1$
	}

	/**
	 * Validate remote connection, and issue error if required.
	 * 
	 */
	public static boolean isConnectionValid(String name, Shell s) {
		boolean ret = true;
		IHost sc = parseForSystemConnection(name);
		if (sc == null) {
			// invalid connection
			ret = false;
			
			String msgTxt = RemoteImportExportResources.MSG_IMPORT_EXPORT_UNABLE_TO_USE_CONNECTION;
			String msgDetails = RemoteImportExportResources.MSG_IMPORT_EXPORT_UNABLE_TO_USE_CONNECTION_DETAILS;
			
			SystemMessage msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID, 
					IRemoteImportExportConstants.MSG_IMPORT_EXPORT_UNABLE_TO_USE_CONNECTION,
					IStatus.ERROR, msgTxt, msgDetails);
			SystemMessageDialog.show(s, msg);
			//displayMessage(s, ISystemMessages.MSG_IMPORT_EXPORT_UNABLE_TO_USE_CONNECTION, true);
		}
		return ret;
	}

	/**
	 * Use this method to retrieve an IRemoteFile object from a 
	 * selection string.
	 */
	public static IRemoteFile parseForIRemoteFile(String sel) {
		IHost c = parseForSystemConnection(sel);
		if (c != null) {
			String path = parseForPath(sel);
			return getIRemoteFile(c, path);
		} else
			return null;
	}

	/**
	 * Use this method to retrieve the file path from a 
	 * selection string.
	 */
	public static String parseForPath(String sel) {
		return sel.indexOf(":") >= 0 ? sel.substring(sel.indexOf(":") + 1) : sel; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Use this method to retrieve a SystemConnection from profile and a 
	 * connectionName string.
	 */
	public static IHost getConnection(String profileName, String connectionName) {
		IHost[] connections = RSECorePlugin.getTheSystemRegistry().getHosts();
		if (profileName != null) {
			// given both profile and connection name...
			for (int loop = 0; loop < connections.length; loop++) {
				if (connections[loop].getAliasName().equalsIgnoreCase(connectionName) && connections[loop].getSystemProfileName().equalsIgnoreCase(profileName)) return connections[loop];
			}
		} else
			// given only connection name...
			for (int loop = 0; loop < connections.length; loop++) {
				if (connections[loop].getAliasName().equalsIgnoreCase(connectionName)) return connections[loop]; // return 1st match
			}
		return null;
	}

	/**
	 * Use this method to retrieve a SystemConnection from a 
	 * selection string.  Should really be a part of RSE.  If
	 * multiple separators ('.') are encountered will try 
	 * profile names with 0,1,2,...n separators in order until a 
	 * valid connection is found, or we run out of options.
	 * 
	 * Not perfect, for example will never return connection C for
	 * profile one.two.three, if connection three.C for profile one.two
	 * exists.  But this scheme should work fine for most practical
	 * cases.
	 * 
	 * Wish RSE had chosen a separator that could not be part of
	 * valid connection name. 
	 */
	public static IHost parseForSystemConnection(String sel) {
		try {
			// Assumption: following will return null if connection has 
			// been deleted or renamed!
			String connectionName = sel.substring(0, sel.indexOf(":")); //$NON-NLS-1$
			if (connectionName.indexOf('.') < 0)
				return getConnection(null, connectionName);
			else {
				// iterate through all possible combinations until we find a match, or 
				// run out of options
				int dots = 0, temp = 0;
				IHost sc = null;
				while (connectionName.indexOf('.', temp) >= 0) {
					dots++;
					temp = connectionName.indexOf('.', temp) + 1;
					sc = getConnection(connectionName.substring(0, temp - 1), connectionName.substring(temp));
					if (sc != null) return sc;
				}
				// did not find any, last hope try no profile, and '.' in name
				return getConnection(null, connectionName);
			}
		} catch (Exception e) {
			// Received exception while validating string.
			// Ignore exception, just return null on fall-thru
		}
		// Connection with specified name was not found
		return null;
	}

	// generic classes: 
	public static void error(Exception e) {
		Object[] o = null;
		// While developing launch configuration work we noticed that
		// we could enter here with no access to a Shell object. Changed
		// this method to simply log the exception in such cases.
		Shell s = getShell();
		try {
			s = SystemBasePlugin.getActiveWorkbenchWindow().getShell();
		} catch (Exception e1) {
			s = null;
		}
		if (SystemMessageException.class.isInstance(e)) {
			String mID = ((SystemMessageException) e).getSystemMessage().getFullMessageID().substring(0, 8);
			Debug.out("About to issue SystemMessageException for  " + mID); //$NON-NLS-1$
			if (mID.compareToIgnoreCase("EVFC9104") != 0 && mID.compareToIgnoreCase("EVFC9112") != 0) { //$NON-NLS-1$ //$NON-NLS-2$
				// As per DY, do not issue 9104, or 9112 messages; they must have already been issued!
				if (s != null) {
					SystemMessageDialog d = new SystemMessageDialog(s, ((SystemMessageException) e).getSystemMessage());
					d.open();
				}
			}
		} else {
			o = new Object[] { e.getLocalizedMessage() == null ? e.toString() : e.getLocalizedMessage() };
			logExceptionError(NLS.bind(RemoteImportExportResources.MSG_IMPORT_EXPORT_UNEXPECTED_EXCEPTION, o), e);
			if (s != null) {
				String msgTxt = NLS.bind(RemoteImportExportResources.MSG_IMPORT_EXPORT_UNEXPECTED_EXCEPTION, o[0]);
				String msgDetails = RemoteImportExportResources.MSG_IMPORT_EXPORT_UNEXPECTED_EXCEPTION_DETAILS;
				SystemMessage msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID, 
						IRemoteImportExportConstants.MSG_IMPORT_EXPORT_UNEXPECTED_EXCEPTION,
						IStatus.ERROR, msgTxt, msgDetails);
				SystemMessageDialog.show(s, msg);
			}
		}
	}

	public static Shell getShell() {
		Shell s = null;
		try {
			s = SystemBasePlugin.getActiveWorkbenchWindow().getShell();
		} catch (Exception e1) {
			s = null;
		}
		return s;
	}

	public static void logExceptionError(String msgTxt, Throwable exception) {
		SystemBasePlugin.logError(msgTxt, exception);
	}


}
