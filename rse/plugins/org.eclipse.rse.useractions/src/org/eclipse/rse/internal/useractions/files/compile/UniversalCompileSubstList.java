/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.files.compile;

import org.eclipse.rse.internal.useractions.ui.SystemCmdSubstVarList;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;

/**
 * Encapsulation of the compile command substitution variables for universal files.
 */
public class UniversalCompileSubstList extends SystemCmdSubstVarList {
	private static final String[] UNIVERSAL_FILES_VARNAMES = { "system_filesep", //$NON-NLS-1$
			"system_homedir", //$NON-NLS-1$
			"system_pathsep", //$NON-NLS-1$
			"system_tempdir", //$NON-NLS-1$
			"resource_name", //$NON-NLS-1$
			"resource_name_root", //$NON-NLS-1$
			"resource_path", //$NON-NLS-1$
			"resource_path_root", //$NON-NLS-1$
			"resource_path_drive", //$NON-NLS-1$
			"container_name", //$NON-NLS-1$
			"container_path" //$NON-NLS-1$
	};
	private static final String[] UNIVERSAL_FILES_DESCRIPTIONS = { SystemUDAResources.RESID_COMPILE_FILES_SUBVAR_SYSTEM_FILESEP, SystemUDAResources.RESID_COMPILE_FILES_SUBVAR_SYSTEM_HOMEDIR,
			SystemUDAResources.RESID_COMPILE_FILES_SUBVAR_SYSTEM_PATHSEP, SystemUDAResources.RESID_COMPILE_FILES_SUBVAR_SYSTEM_TEMPDIR, SystemUDAResources.RESID_COMPILE_FILES_SUBVAR_RESOURCE_NAME,
			SystemUDAResources.RESID_COMPILE_FILES_SUBVAR_RESOURCE_NAME_ROOT, SystemUDAResources.RESID_COMPILE_FILES_SUBVAR_RESOURCE_PATH,
			SystemUDAResources.RESID_COMPILE_FILES_SUBVAR_RESOURCE_PATH_ROOT, SystemUDAResources.RESID_COMPILE_FILES_SUBVAR_RESOURCE_PATH_DRIVE,
			SystemUDAResources.RESID_COMPILE_FILES_SUBVAR_CONTAINER_NAME, SystemUDAResources.RESID_COMPILE_FILES_SUBVAR_CONTAINER_PATH };
	private static UniversalCompileSubstList inst = null;

	/**
	 * Constructor .
	 * Not to be used directly. Rather, use {@link #getInstance()}.
	 */
	UniversalCompileSubstList() {
		super(UNIVERSAL_FILES_VARNAMES, UNIVERSAL_FILES_DESCRIPTIONS);
	}

	/**
	 * Return the singleton of this object. No need ever for more than one instance
	 */
	public static UniversalCompileSubstList getInstance() {
		if (inst == null) inst = new UniversalCompileSubstList();
		return inst;
	}
}
