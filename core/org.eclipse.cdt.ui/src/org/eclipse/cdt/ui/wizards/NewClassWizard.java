/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * description for "NewClassWizard".
 * @see Wizard
 */
public class NewClassWizard extends BasicNewResourceWizard implements INewWizard {
	private NewClassWizardPage fPage;
	private String wz_title;
	// a boolean to programatically control opening the sources in the editor
	private boolean openInEditor = true;

	private static final String WZ_TITLE = "NewClassWizard.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "NewClassWizard.description"; //$NON-NLS-1$
	private static final String PAGE_TITLE = "NewClassWizard.page.title"; //$NON-NLS-1$

	/**
	 * "NewClassWizard" constructor.
	 */
	public NewClassWizard() {
		this(CUIPlugin.getResourceString(WZ_TITLE));
		
	}

	public NewClassWizard(String title) {
		super();
		wz_title = title;
		setDefaultPageImageDescriptor(CPluginImages.DESC_WIZBAN_NEWCLASS);
		setWindowTitle(wz_title); //$NON-NLS-1$				
		setNeedsProgressMonitor(true);
	}
	
	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
		if(fPage.createClass(monitor)){
			ITranslationUnit headerTU= fPage.getCreatedClassHeaderFile();
			ITranslationUnit bodyTU= fPage.getCreatedClassBodyFile();
			if (headerTU != null) {
				IResource resource= headerTU.getResource();
				selectAndReveal(resource);
				if(doOpenInEditor()){
					openResource((IFile) resource);
				}
			}	
			if (bodyTU != null) {
				IResource resource= bodyTU.getResource();
				selectAndReveal(resource);
				if(doOpenInEditor()){
					openResource((IFile) resource);
				}
			}	
		}
	}
	/**
	 * @see Wizard#performFinish
	 */
	public boolean performFinish()  {			
		if(!fPage.selectionIsCpp())
			return true;
		IWorkspaceRunnable op= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
				try {
					finishPage(monitor);
				} catch (InterruptedException e) {
					throw new OperationCanceledException(e.getMessage());
				}
			}
		};
		try {
			getContainer().run(false, true, new WorkbenchRunnableAdapter(op));
		} catch (InvocationTargetException e) {
			handleFinishException(getShell(), e);
			return false;
		} catch  (InterruptedException e) {
			return false;
		}
		return true;
	}	
	public void addPages() {
		super.addPages();
		fPage= new NewClassWizardPage(getSelection());
		addPage(fPage);
		fPage.setTitle(CUIPlugin.getResourceString(PAGE_TITLE));
		fPage.setDescription(CUIPlugin.getResourceString(WZ_DESC));
		fPage.init();		
	}	
	
	protected void handleFinishException(Shell shell, InvocationTargetException e) {
		String title= NewWizardMessages.getString("NewElementWizard.op_error.title"); //$NON-NLS-1$
		String message= NewWizardMessages.getString("NewElementWizard.op_error.message"); //$NON-NLS-1$
		ExceptionHandler.handle(e, shell, title, message);
	}

	protected void openResource(final IFile resource) {
		final IWorkbenchPage activePage= CUIPlugin.getActivePage();
		if (activePage != null) {
			final Display display= getShell().getDisplay();
			if (display != null) {
				display.asyncExec(new Runnable() {
					public void run() {
						try {
							IDE.openEditor(activePage, resource, true);
						} catch (PartInitException e) {
							CUIPlugin.getDefault().log(e);
						}
					}
				});
			}
		}
	}
	
	public ICElement getCreatedClassElement(){
		return fPage.getCreatedClassElement();
	}
	/**
	 * @return Returns the openInEditor.
	 */
	public boolean doOpenInEditor() {
		return openInEditor;
	}
	/**
	 * @param openInEditor The openInEditor to set.
	 */
	public void setOpenInEditor(boolean openInEditor) {
		this.openInEditor = openInEditor;
	}
}
