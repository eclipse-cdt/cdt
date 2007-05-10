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

/**
 * @author coulthar
 *
 * Substitution variables for folders. Superset of common list
 */
public class UDSubstListFiles extends SystemCmdSubstVarList {
	/* from resource property file...
	 ...uda.files.subvar.resource_name_root=Name of selected resource without the extension
	 ...uda.files.subvar.resource_name_ext=Extension part of the name of the selected resource
	 */
	private static final String[] FILE_VARNAMES = { "resource_name_ext", //$NON-NLS-1$
			"resource_name_root" //$NON-NLS-1$
	};
	private static final String[] DESCRIPTIONS = { SystemUDAResources.RESID_UDA_FILES_SUBVAR_RESOURCE_NAME_EXT, SystemUDAResources.RESID_UDA_FILES_SUBVAR_RESOURCE_NAME_ROOT };
	private static UDSubstListFiles inst = null;

	/**
	 * Constructor .
	 * Not to be used directly. Rather, use {@link #getInstance()}.
	 */
	UDSubstListFiles() {
		super(UDSubstListCommonFiles.getInstance(), FILE_VARNAMES, DESCRIPTIONS);
		testForDuplicates();
	}

	/**
	 * Return the singleton of this object. No need ever for more than one instance
	 */
	public static UDSubstListFiles getInstance() {
		if (inst == null) inst = new UDSubstListFiles();
		return inst;
	}
}
