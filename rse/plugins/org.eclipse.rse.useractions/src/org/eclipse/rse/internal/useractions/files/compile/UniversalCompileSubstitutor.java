/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.files.compile;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.internal.useractions.ui.compile.ISystemCompileCommandSubstitutor;
import org.eclipse.rse.shells.ui.RemoteCommandHelpers;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;

/**
 * This class is responsible for doing variable substitution for iSeries compile
 *  commands
 */
public class UniversalCompileSubstitutor implements ISystemCompileCommandSubstitutor {
	private IHost connection;

	/**
	 * Constructor for UniversalCompileSubstitutor.
	 */
	public UniversalCompileSubstitutor(IHost connection) {
		super();
		this.connection = connection;
	}

	/**
	 * Reset the connection so one instance can be re-used
	 */
	public void setConnection(IHost connection) {
		this.connection = connection;
	}

	/**
	 * @see org.eclipse.rse.internal.useractions.ui.ISystemSubstitutor#getSubstitutionValue(String, Object)
	 */
	public String getSubstitutionValue(String substitutionVariable, Object context) {
		//private static final String[] UNIVERSAL_FILES_VARNAMES = 
		//{"system_filesep", "system_homedir", "system_pathsep", "system_tempdir", 
		// "user_id", "resource_name", "resource_path", "resource_path_root", "resource_path_drive", "container_name", "container_path"};
		if (substitutionVariable.equals("${system_filesep}")) //$NON-NLS-1$
			return getFileSeparator();
		else if (substitutionVariable.equals("${system_homedir}")) //$NON-NLS-1$
			return getHomeDirectory();
		else if (substitutionVariable.equals("${system_pathsep}")) //$NON-NLS-1$
			return getPathSeparator();
		else if (substitutionVariable.equals("${system_tempdir}")) //$NON-NLS-1$
			return getTempDirectory();
		else if (substitutionVariable.equals("${user_id}")) //$NON-NLS-1$
			return getUserId();
		else if (substitutionVariable.equals("${resource_name}")) //$NON-NLS-1$
			return getResourceName(context);
		else if (substitutionVariable.equals("${resource_name_root}")) //$NON-NLS-1$
			return getResourceNameRoot(context);
		else if (substitutionVariable.equals("${resource_path}")) //$NON-NLS-1$
			return getResourcePath(context);
		else if (substitutionVariable.equals("${resource_path_root}")) //$NON-NLS-1$
			return getPathRoot(context);
		else if (substitutionVariable.equals("${resource_path_drive}")) //$NON-NLS-1$
			return getPathDrive(context);
		else if (substitutionVariable.equals("${container_name}")) //$NON-NLS-1$
			return getContainerName(context);
		else if (substitutionVariable.equals("${container_path}")) //$NON-NLS-1$
			return getContainerPath(context);
		return null;
	}

	/**
	 * Get the command subsystem
	 */
	protected IRemoteCmdSubSystem getCmdsSubSystem() {
		return RemoteCommandHelpers.getCmdSubSystem(connection);
	}

	/**
	 * Get the files subsystem
	 */
	protected IRemoteFileSubSystem getFilesSubSystem() {
		return RemoteFileUtility.getFileSubSystem(connection);
	}

	/**
	 * Get the files subsystem factory
	 */
	protected IRemoteFileSubSystemConfiguration getFilesSubSystemFactory() {
		return RemoteFileUtility.getFileSubSystem(connection).getParentRemoteFileSubSystemConfiguration();
	}

	/**
	 * Return the file separator for the ${system_filesep} variable
	 */
	protected String getFileSeparator() {
		return getFilesSubSystemFactory().getSeparator();
	}

	/**
	 * Return the path separator for the ${system_pathsep} variable
	 */
	protected String getPathSeparator() {
		return getFilesSubSystemFactory().getPathSeparator();
	}

	/**
	 * Return the user's home directory on the remote system, for the ${system_homedir} variable
	 */
	protected String getHomeDirectory() {
		return getCmdsSubSystem().getConnectorService().getHomeDirectory();
	}

	/**
	 * Return the temporary directory on the remote system, for the ${system_tempdir} variable
	 */
	protected String getTempDirectory() {
		return getCmdsSubSystem().getConnectorService().getTempDirectory();
	}

	/**
	 * Return the user ID used to connect with the remote system, for the ${user_id} variable
	 */
	protected String getUserId() {
		return getCmdsSubSystem().getConnectorService().getUserId();
	}

	/**
	 * Return the name of the currently selected resource, for the ${resource_name} variable
	 */
	protected String getResourceName(Object context) {
		return SystemAdapterHelpers.getRemoteAdapter(context).getName(context);
	}

	/**
	 * Return the root part of the name of the currently selected resource, for the ${resource_name_root} variable
	 */
	protected String getResourceNameRoot(Object context) {
		IRemoteFile selectedFile = (IRemoteFile) context;
		String name = selectedFile.getName();
		int dotIdx = name.lastIndexOf('.');
		if (dotIdx == 0)
			return ""; //$NON-NLS-1$
		else if (dotIdx > 0)
			return name.substring(0, dotIdx);
		else
			return name;
	}

	/**
	 * Return the path of the currently selected resource, for the ${resource_path} variable
	 */
	protected String getResourcePath(Object context) {
		return SystemAdapterHelpers.getRemoteAdapter(context).getAbsoluteName(context);
	}

	/**
	 * Return the root part of the path, for the ${resource_path_root} variable
	 */
	protected String getPathRoot(Object context) {
		IRemoteFile selectedFile = (IRemoteFile) context;
		String name = selectedFile.getAbsolutePath();
		if (name != null) {
			if (name.startsWith("/") || name.startsWith("\\")) //$NON-NLS-1$ //$NON-NLS-2$
				return name.substring(0, 1);
			else {
				int idx = name.indexOf(":\\"); //$NON-NLS-1$
				if (idx > 0) return name.substring(0, idx + 2);
			}
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Return the drive part of the path, for the ${resource_path_drive} variable
	 */
	protected String getPathDrive(Object context) {
		IRemoteFile selectedFile = (IRemoteFile) context;
		String name = selectedFile.getAbsolutePath();
		if ((name != null) && (name.length() > 1)) {
			int idx = name.indexOf(':');
			if (idx > 0) return name.substring(0, idx);
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Return the name of the parent folder, for the ${container_name} variable
	 */
	protected String getContainerName(Object context) {
		IRemoteFile selectedFile = (IRemoteFile) context;
		String fn = selectedFile.getParentName();
		if (fn != null)
			return fn;
		else
			return ""; //$NON-NLS-1$
	}

	/**
	 * Return the path of the parent folder, for the ${container_path} variable
	 */
	protected String getContainerPath(Object context) {
		IRemoteFile selectedFile = (IRemoteFile) context;
		String name = selectedFile.getAbsolutePath();
		if ((name != null) && (name.length() > 1)) {
			int idx = name.indexOf(':');
			if (idx > 0) return name.substring(0, idx);
		}
		return ""; //$NON-NLS-1$
	}
}
