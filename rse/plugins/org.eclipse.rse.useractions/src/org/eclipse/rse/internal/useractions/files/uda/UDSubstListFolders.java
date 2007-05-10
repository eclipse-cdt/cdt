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

/**
 * @author coulthar
 *
 * Substitution variables for folders. Superset of common list
 */
public class UDSubstListFolders extends SystemCmdSubstVarList {
	private static final String[] FOLDER_VARNAMES = {};
	private static final String[] DESCRIPTIONS = {};
	private static UDSubstListFolders inst = null;

	/**
	 * Constructor .
	 * Not to be used directly. Rather, use {@link #getInstance()}.
	 */
	UDSubstListFolders() {
		super(UDSubstListCommonFiles.getInstance(), FOLDER_VARNAMES, DESCRIPTIONS);
		testForDuplicates();
	}

	/**
	 * Return the singleton of this object. No need ever for more than one instance
	 */
	public static UDSubstListFolders getInstance() {
		if (inst == null) inst = new UDSubstListFolders();
		return inst;
	}
}
