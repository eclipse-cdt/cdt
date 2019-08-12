/*******************************************************************************
 * Copyright (c) 2011, 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Martin Schwab & Thomas Kallenberg - initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import org.eclipse.cdt.internal.ui.refactoring.changes.CreateFileChange;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class ToggleFileCreator {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private final ToggleRefactoringContext context;
	private final String ending;

	public ToggleFileCreator(ToggleRefactoringContext context, String ending) {
		this.context = context;
		this.ending = ending;
	}

	public IFile createNewFile() {
		String filename = getNewFileName();
		IPath path = new Path(getPath() + filename);
		try {
			CreateFileChange change = new CreateFileChange(filename, path, EMPTY_STRING,
					context.getSelectionFile().getCharset());
			change.perform(new NullProgressMonitor());
			return (IFile) change.getModifiedElement();
		} catch (CoreException e) {
			throw new NotSupportedException(NLS.bind(Messages.ToggleFileCreator_CanNotCreateNewFile, path.toString()));
		}
	}

	public boolean askUserForFileCreation(final ToggleRefactoringContext context) {
		if (context.isSettedDefaultAnswer()) {
			return context.getDefaultAnswer();
		}
		final boolean[] answer = new boolean[1];
		Runnable r = () -> {
			Shell shell = CUIPlugin.getDefault().getWorkbench().getWorkbenchWindows()[0].getShell();
			String functionName;
			if (context.getDeclaration() != null) {
				functionName = context.getDeclaration().getRawSignature();
			} else {
				functionName = context.getDefinition().getDeclarator().getRawSignature();
			}
			answer[0] = MessageDialog.openQuestion(shell, Messages.ToggleFileCreator_NewImplFile,
					NLS.bind(Messages.ToggleFileCreator_CreateNewFilePrompt, getNewFileName(), functionName));
		};
		PlatformUI.getWorkbench().getDisplay().syncExec(r);
		return answer[0];
	}

	public String getIncludeStatement() {
		return "#include \"" + ToggleNodeHelper.getFilenameWithoutExtension(getNewFileName()) + ".h\"\n"; //$NON-NLS-1$//$NON-NLS-2$
	}

	private String getNewFileName() {
		return ToggleNodeHelper.getFilenameWithoutExtension(context.getSelectionFile().getFullPath().toString())
				+ ending;
	}

	private String getPath() {
		String result = context.getSelectionFile().getFullPath().toOSString();
		return result.replaceAll("(\\w)*\\.(\\w)*", EMPTY_STRING); //$NON-NLS-1$
	}
}
