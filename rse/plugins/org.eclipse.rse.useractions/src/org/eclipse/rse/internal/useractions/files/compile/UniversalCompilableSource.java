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
import org.eclipse.rse.shells.ui.RemoteCommandHelpers;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.widgets.Shell;

/**
 * This encapsulates a file in a universal file system, which is to be compiled.
 */
public class UniversalCompilableSource extends SystemCompilableSource {
	/**
	 * Constructor for UniversalCompilableSource.
	 * @param shell - the shell to use if need to prompt
	 * @param firstSelection - the selected compilable source member
	 * @param compileCmd - the Compile Command that is to be run against the selected compilable source member
	 * @param isPrompt - true if the user choose the flavor of the action to prompt the compile command
	 * @param viewer - the viewer that originated the compile action
	 */
	public UniversalCompilableSource(Shell shell, Object firstSelection, SystemCompileCommand compileCmd, boolean isPrompt, Viewer viewer) {
		super(shell, firstSelection, compileCmd, isPrompt, viewer);
	}

	/**
	 * After the substituting and the prompting, it is now time to the remote running of the 
	 *  fully resolved compile command. Do that here.
	 * <p>
	 * We use the RemoteCommandHelpers class to run it.
	 */
	protected boolean internalRunCompileCommand(String compileCmd) {
		String path = RemoteCommandHelpers.getWorkingDirectory((IRemoteFile) firstSelection);
		boolean ok = RemoteCommandHelpers.runUniversalCommand(shell, compileCmd, path, getCommandSubSystem(), true);
		return ok;
	}
}
