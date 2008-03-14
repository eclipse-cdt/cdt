/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.files.compile;

import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileManager;

/**
 * Specialization of the compile profile class, uniquely for the compile support of files from
 *  the local file subsystem.
 */
public class LocalCompileProfile extends UniversalCompileProfile {
	/**
	 * Constructor 
	 * @param manager SystemCompileManager of this compile file
	 * @param profileName System profile name
	 */
	public LocalCompileProfile(SystemCompileManager manager, String profileName) {
		super(manager, profileName);
	}
}
