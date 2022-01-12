/*******************************************************************************
 * Copyright (c) 2001, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards;

import java.util.Iterator;

import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.NewProjectAction;

public abstract class AbstractOpenWizardAction extends Action implements IWorkbenchWindowActionDelegate {

	private Class<?>[] fActivatedOnTypes;
	private boolean fAcceptEmptySelection;

	/**
	 * Creates a AbstractOpenWizardAction.
	 * @param label The label of the action
	 * @param acceptEmptySelection Specifies if the action allows an empty selection
	 */
	public AbstractOpenWizardAction(String label, boolean acceptEmptySelection) {
		this(label, null, acceptEmptySelection);
	}

	/**
	 * Creates a AbstractOpenWizardAction.
	 * @param label The label of the action
	 * @param activatedOnTypes The action is only enabled when all objects in the selection
	 *                         are of the given types. <code>null</code> will allow all types.
	 * @param acceptEmptySelection Specifies if the action allows an empty selection
	 */
	public AbstractOpenWizardAction(String label, Class<?>[] activatedOnTypes, boolean acceptEmptySelection) {
		super(label);
		fActivatedOnTypes = activatedOnTypes;
		fAcceptEmptySelection = acceptEmptySelection;
	}

	/**
	 * Creates a AbstractOpenWizardAction with no restrictions on types, and does allow
	 * an empty selection.
	 */
	protected AbstractOpenWizardAction() {
		fActivatedOnTypes = null;
		fAcceptEmptySelection = true;
	}

	protected IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	private boolean isOfAcceptedType(Object obj) {
		if (fActivatedOnTypes != null) {
			for (Class<?> activatedOnType : fActivatedOnTypes) {
				if (activatedOnType.isInstance(obj)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	private boolean isEnabled(IStructuredSelection selection) {
		Iterator<?> iter = selection.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (!isOfAcceptedType(obj) || !shouldAcceptElement(obj)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Can be overridden to add more checks.
	 * obj is guaranteed to be instance of one of the accepted types
	 */
	protected boolean shouldAcceptElement(Object obj) {
		return true;
	}

	/**
	 * Creates the specific wizard.
	 * (to be implemented by a subclass)
	 */
	abstract protected Wizard createWizard() throws CoreException;

	protected IStructuredSelection getCurrentSelection() {
		IWorkbenchWindow window = CUIPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			ISelection selection = window.getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection) {
				return (IStructuredSelection) selection;
			}
			// Build the selection from the IFile of the editor
			IWorkbenchPart part = window.getPartService().getActivePart();
			if (part instanceof IEditorPart) {
				IEditorInput input = ((IEditorPart) part).getEditorInput();
				if (input instanceof IFileEditorInput) {
					IFile file = ((IFileEditorInput) input).getFile();
					if (file != null)
						return new StructuredSelection(file);
				}
			}
		}
		return StructuredSelection.EMPTY;
	}

	/**
	 * The user has invoked this action.
	 */
	@Override
	public void run() {
		/*		if (!fNoChecking && !canActionBeAdded()) {
					return;
				}
				if (!checkWorkspaceNotEmpty()) {
					return;
				}
		*/ Shell shell = CUIPlugin.getActiveWorkbenchShell();
		try {
			Wizard wizard = createWizard();
			if (wizard instanceof IWorkbenchWizard) {
				((IWorkbenchWizard) wizard).init(getWorkbench(), getCurrentSelection());
			}

			WizardDialog dialog = new WizardDialog(shell, wizard);
			PixelConverter converter = new PixelConverter(CUIPlugin.getActiveWorkbenchShell());

			dialog.setMinimumPageSize(converter.convertWidthInCharsToPixels(70),
					converter.convertHeightInCharsToPixels(20));
			dialog.create();
			dialog.open();
		} catch (CoreException e) {
			String title = NewWizardMessages.AbstractOpenWizardAction_createerror_title;
			String message = NewWizardMessages.AbstractOpenWizardAction_createerror_message;
			ExceptionHandler.handle(e, shell, title, message);
		}
	}

	/**
	 * Tests if the action can be run on the current selection.
	 */
	public boolean canActionBeAdded() {
		IStructuredSelection selection = getCurrentSelection();
		if (selection == null || selection.isEmpty()) {
			return fAcceptEmptySelection;
		}
		return isEnabled(selection);
	}

	/*
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {
		run();
	}

	/*
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
	}

	/*
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	@Override
	public void init(IWorkbenchWindow window) {
	}

	/*
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// selection taken from selectionprovider
	}

	protected boolean checkWorkspaceNotEmpty() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace.getRoot().getProjects().length == 0) {
			Shell shell = CUIPlugin.getActiveWorkbenchShell();
			String title = NewWizardMessages.AbstractOpenWizardAction_noproject_title;
			String message = NewWizardMessages.AbstractOpenWizardAction_noproject_message;
			if (MessageDialog.openQuestion(shell, title, message)) {
				IWorkbenchWindow window = CUIPlugin.getActiveWorkbenchWindow();
				(new NewProjectAction(window)).run();
				return workspace.getRoot().getProjects().length != 0;
			}
			return false;
		}
		return true;
	}
}
