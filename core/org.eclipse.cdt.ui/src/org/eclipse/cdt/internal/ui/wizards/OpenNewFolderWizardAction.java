package org.eclipse.cdt.internal.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.wizards.newresource.BasicNewFolderResourceWizard;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * @deprecated use NewFolderCreationWizard instead
 */

public class OpenNewFolderWizardAction extends Action implements IWorkbenchWindowActionDelegate {

	public void run() {
		BasicNewFolderResourceWizard wizard= new BasicNewFolderResourceWizard();
		wizard.init(CUIPlugin.getDefault().getWorkbench(), getCurrentSelection());
		wizard.setNeedsProgressMonitor(true);
		WizardDialog dialog=
			new WizardDialog(CUIPlugin.getActiveWorkbenchShell(), wizard);
		dialog.create();
		dialog.getShell().setText(
			CUIPlugin.getResourceString("OpenNewFolderWizardAction.title")); //$NON-NLS-1$
		dialog.open();
	}

	protected IStructuredSelection getCurrentSelection() {
		IWorkbenchWindow window= CUIPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			ISelection selection= window.getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection) {
				return (IStructuredSelection) selection;
			}
		}
		return StructuredSelection.EMPTY;
	}
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run();
	}
	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}
	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
