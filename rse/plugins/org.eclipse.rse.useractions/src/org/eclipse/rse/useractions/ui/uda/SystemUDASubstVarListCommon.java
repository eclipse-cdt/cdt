package org.eclipse.rse.useractions.ui.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.rse.useractions.ui.SystemCmdSubstVarList;

/**
 * @author coulthar
 *
 * Encapsulation of the substitution variables that are typically common for 
 *  absolutely every subsystem.
 */
public class SystemUDASubstVarListCommon extends SystemCmdSubstVarList {
	private static final String[] COMMON_VARNAMES = { "action_name", "connection_name", "local_hostname", "local_ip", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"system_filesep", "system_homedir", "system_hostname", "system_pathsep", "system_tempdir", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"user_id" }; //$NON-NLS-1$
	private static final String[] COMMON_VARNAME_DESCRIPTIONS = { SystemUDAResources.RESID_UDA_SUBVAR_ACTION_NAME, SystemUDAResources.RESID_UDA_SUBVAR_CONNECTION_NAME,
			SystemUDAResources.RESID_UDA_SUBVAR_LOCAL_HOSTNAME, SystemUDAResources.RESID_UDA_SUBVAR_LOCAL_IP, SystemUDAResources.RESID_UDA_SUBVAR_SYSTEM_FILESEP,
			SystemUDAResources.RESID_UDA_SUBVAR_SYSTEM_HOMEDIR, SystemUDAResources.RESID_UDA_SUBVAR_SYSTEM_HOSTNAME, SystemUDAResources.RESID_UDA_SUBVAR_SYSTEM_PATHSEP,
			SystemUDAResources.RESID_UDA_SUBVAR_SYSTEM_TEMPDIR, SystemUDAResources.RESID_UDA_SUBVAR_USER_ID };
	private static SystemUDASubstVarListCommon inst = null;

	/**
	 * Constructor .
	 * Not to be used directly. Rather, use getSingleton().
	 */
	SystemUDASubstVarListCommon() {
		super(COMMON_VARNAMES, COMMON_VARNAME_DESCRIPTIONS);
	}

	/**
	 * Return the singleton of this object. No need ever for more than one instance
	 */
	public static SystemUDASubstVarListCommon getSingleton() {
		if (inst == null) inst = new SystemUDASubstVarListCommon();
		return inst;
	}
}
