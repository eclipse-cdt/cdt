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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompilableSource;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileCommand;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileManager;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileProfile;
import org.eclipse.swt.widgets.Shell;

/**
 * Specialization of the compile profile class, uniquely for the compile support of files from
 *  a universal file subsystem.
 */
public class UniversalCompileProfile extends SystemCompileProfile {
	/**
	 * Constructor for UniversalCompileProfile.
	 * @param manager SystemCompileManager of this compile file
	 * @param profileName System profile name
	 */
	public UniversalCompileProfile(SystemCompileManager manager, String profileName) {
		super(manager, profileName);
	}

	/**
	 * When the time comes to actually run a compile command against a selected source object,
	 *  this method is called to return the instance of SystemCompilableSource to do that. 
	 * <p>
	 * This method must be implemented to return an instance of your subclass of SystemCompilableSource.
	 */
	public SystemCompilableSource getCompilableSourceObject(Shell shell, Object selectedObject, SystemCompileCommand compileCmd, boolean isPrompt, Viewer viewer) {
		UniversalCompilableSource compilableSrc = new UniversalCompilableSource(shell, selectedObject, compileCmd, isPrompt, viewer);
		return compilableSrc;
	}
}
