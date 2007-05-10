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
package org.eclipse.rse.internal.useractions.files.uda;

import org.eclipse.rse.internal.useractions.ui.SystemCmdSubstVarList;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDASubstVarListCommon;

/**
 * Encapsulation of the common substitution variables for both folders and files.
 * Superset of overall system common variables.
 */
public class UDSubstListCommonFiles extends SystemCmdSubstVarList {
	/* from resource property file...
	 ...uda.files.subvar.resource_date = Last modified date of selected resource
	 ...uda.files.subvar.resource_name = Name of selected resource, unqualified
	 ...uda.files.subvar.resource_path = Path of selected resource, including name
	 ...uda.files.subvar.resource_path_root=Root of selected file's path. "c:\\" on Windows, or "/" on others
	 ...uda.files.subvar.resource_path_drive=Drive letter on Windows, empty string on others
	 ...uda.files.subvar.container_name=Name of folder containing selected resource, unqualified
	 ...uda.files.subvar.container_path=Path of folder containing selected resource, including name
	 */
	private static final String[] COMMON_VARNAMES = { "resource_date", //$NON-NLS-1$
			"resource_name", //$NON-NLS-1$
			"resource_path", //$NON-NLS-1$
			"resource_path_root", //$NON-NLS-1$
			"resource_path_drive", //$NON-NLS-1$
			"container_name", //$NON-NLS-1$
			"container_path" //$NON-NLS-1$
	};
	private static final String[] COMMON_DESCRIPTIONS = { SystemUDAResources.RESID_UDA_FILES_SUBVAR_RESOURCE_DATE, SystemUDAResources.RESID_UDA_FILES_SUBVAR_RESOURCE_NAME,
			SystemUDAResources.RESID_UDA_FILES_SUBVAR_RESOURCE_PATH, SystemUDAResources.RESID_UDA_FILES_SUBVAR_RESOURCE_PATH_ROOT, SystemUDAResources.RESID_UDA_FILES_SUBVAR_RESOURCE_PATH_DRIVE,
			SystemUDAResources.RESID_UDA_FILES_SUBVAR_CONTAINER_NAME, SystemUDAResources.RESID_UDA_FILES_SUBVAR_CONTAINER_PATH };
	private static UDSubstListCommonFiles inst = null;

	/**
	 * Constructor .
	 * Not to be used directly. Rather, use {@link #getInstance()}.
	 */
	UDSubstListCommonFiles() {
		super(SystemUDASubstVarListCommon.getInstance(), COMMON_VARNAMES, COMMON_DESCRIPTIONS);
	}

	/**
	 * Return the singleton of this object. No need ever for more than one instance
	 */
	public static UDSubstListCommonFiles getInstance() {
		if (inst == null) inst = new UDSubstListCommonFiles();
		return inst;
	}
}
