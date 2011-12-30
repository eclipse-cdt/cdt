/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors:
 * 	   Martin Schwab & Thomas Kallenberg - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.cdt.internal.ui.refactoring.CreateFileChange;

public class ToggleFileCreator {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String H = ".h"; //$NON-NLS-1$
	private ToggleRefactoringContext context;
	private String ending;

	public ToggleFileCreator(ToggleRefactoringContext context, String ending) {
		this.context = context;
		this.ending = ending;
	}
	
	public IASTTranslationUnit loadTranslationUnit() {
		String filename;
		if (context.getDeclaration() != null) {
			filename = context.getDeclaration().getContainingFilename();
		} else {
			filename = context.getDefinition().getContainingFilename();
		}
		String other;
		if (ending.equals(H)) {
			other = ".cpp"; //$NON-NLS-1$
		} else {
			other = H;
		}
		filename = filename.replaceAll("\\w*" + other + "$", EMPTY_STRING) + getNewFileName();  //$NON-NLS-1$//$NON-NLS-2$
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(filename));
		IASTTranslationUnit result = null;
		try {
			result = CoreModelUtil.findTranslationUnitForLocation(file.getFullPath(), null).getAST();
		} catch (CModelException e) {
		} catch (CoreException e) {
		}
		if (result == null) {
			throw new NotSupportedException(Messages.ToggleFileCreator_NoTuForSibling);
		}
		return result;
	}
	
	public void createNewFile() {
		CreateFileChange change;
		String filename = getNewFileName();
		try {
			change = new CreateFileChange(filename, new	Path(getPath() + filename), 
					EMPTY_STRING, context.getSelectionFile().getCharset());
			change.perform(new NullProgressMonitor());
		} catch (CoreException e) {
			throw new NotSupportedException(Messages.ToggleFileCreator_CanNotCreateNewFile);
		}
	}
	
	public boolean askUserForFileCreation(final ToggleRefactoringContext context) {
		if (context.isSettedDefaultAnswer()) {
			return context.getDefaultAnswer();
		}
		final Container<Boolean> answer = new Container<Boolean>();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				Shell shell = CUIPlugin.getDefault().getWorkbench().getWorkbenchWindows()[0].getShell();
				String functionname;
				if (context.getDeclaration() != null) {
					functionname = context.getDeclaration().getRawSignature();
				} else {
					functionname = context.getDefinition().getDeclarator().getRawSignature();
				}
				boolean createnew = MessageDialog.openQuestion(shell, Messages.ToggleFileCreator_NewImplFile, 
						Messages.ToggleFileCreator_CreateNewFile + getNewFileName() + Messages.ToggleFileCreator_andMove + functionname + Messages.ToggleFileCreator_QMark);
				answer.setObject(createnew);
			}
		};
		PlatformUI.getWorkbench().getDisplay().syncExec(r);
		return answer.getObject();
	}

	public String getIncludeStatement() {
		return "#include \"" + ToggleNodeHelper.getFilenameWithoutExtension(getNewFileName()) + ".h\"\n";  //$NON-NLS-1$//$NON-NLS-2$
	}
	
	private String getNewFileName() {
		return ToggleNodeHelper.getFilenameWithoutExtension(context.getSelectionFile().getFullPath().toString()) + ending;
	}
	
	private String getPath() {
		String result = context.getSelectionFile().getFullPath().toOSString();
		return result.replaceAll("(\\w)*\\.(\\w)*", EMPTY_STRING); //$NON-NLS-1$
	}
}
