/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.resources.IPathEntryStore;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * C/C++ Project Paths page for 3.X projects.
 * 
 * @deprecated as of CDT 4.0. This property page was used to set properties
 * "C/C++ Make Project" for 3.X style projects.
 * This page lives dormant as of writing (CDT 7.0) but may get activated for
 * {@code org.eclipse.cdt.make.core.makeNature} project (3.X style).
 */
@Deprecated
public class CPathPropertyPage extends PropertyPage implements IStatusChangeListener{

	private static final String PAGE_SETTINGS = "CPathsPropertyPage"; //$NON-NLS-1$
	private static final String INDEX = "pageIndex"; //$NON-NLS-1$

	CPathTabBlock fCPathsBlock;
	IPathEntryStore fStore;

	@Override
	protected Control createContents(Composite parent) {
		IProject project = getProject();
		Control result;

		// ensure the page has no special buttons
		noDefaultAndApplyButton();
		if (project == null || !isCProject(project)) {
			result = createWithoutCProject(parent);
		} else if (!project.isOpen()) {
			result = createForClosedProject(parent);
		} else {
			result = createWithCProject(parent, project);
		}
		Dialog.applyDialogFont(result);
		return result;
	}

	private IDialogSettings getSettings() {
		IDialogSettings cSettings = CUIPlugin.getDefault().getDialogSettings();
		IDialogSettings pageSettings = cSettings.getSection(PAGE_SETTINGS);
		if (pageSettings == null) {
			pageSettings = cSettings.addNewSection(PAGE_SETTINGS);
			pageSettings.put(INDEX, 3);
		}
		return pageSettings;
	}

	/*
	 * Content for valid projects.
	 */
	private Control createWithCProject(Composite parent, IProject project) {
		fCPathsBlock = new CPathTabBlock(this, getSettings().getInt(INDEX));
		fCPathsBlock.init(CoreModel.getDefault().create(project), null);
		return fCPathsBlock.createContents(parent);
	}

	/*
	 * Content for non-C projects.
	 */
	private Control createWithoutCProject(Composite parent) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(CPathEntryMessages.CPathsPropertyPage_no_C_project_message); 

		fCPathsBlock = null;
		setValid(true);
		return label;
	}

	/*
	 * Content for closed projects.
	 */
	private Control createForClosedProject(Composite parent) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(CPathEntryMessages.CPathsPropertyPage_closed_project_message); 

		fCPathsBlock = null;
		setValid(true);
		return label;
	}

	@Override
	public void setVisible(boolean visible) {
		if (fCPathsBlock != null) {
			if (!visible) {
				if (fCPathsBlock.hasChangesInDialog()) {
					String title = CPathEntryMessages.CPathsPropertyPage_unsavedchanges_title; 
					String message = CPathEntryMessages.CPathsPropertyPage_unsavedchanges_message; 
					String[] buttonLabels = new String[]{
							CPathEntryMessages.CPathsPropertyPage_unsavedchanges_button_save, 
							CPathEntryMessages.CPathsPropertyPage_unsavedchanges_button_discard, 
					};
					MessageDialog dialog = new MessageDialog(getShell(), title, null, message, MessageDialog.QUESTION,
							buttonLabels, 0);
					int res = dialog.open();
					if (res == 0) {
						performOk();
					} else if (res == 1) {
						fCPathsBlock.init(CoreModel.getDefault().create(getProject()), null);
					}
				}
			} else {
				if (!fCPathsBlock.hasChangesInDialog() && fCPathsBlock.hasChangesInCPathFile()) {
					fCPathsBlock.init(CoreModel.getDefault().create(getProject()), null);
				}
			}
		}
		super.setVisible(visible);
	}

	private IProject getProject() {
		IAdaptable adaptable = getElement();
		if (adaptable != null) {
			ICElement elem = (ICElement)adaptable.getAdapter(ICElement.class);
			if (elem instanceof ICProject) {
				return ((ICProject)elem).getProject();
			}
		}
		return null;
	}

	private boolean isCProject(IProject proj) {
		try {
			return proj.hasNature(CProjectNature.C_NATURE_ID);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return false;
	}

	/*
	 * @see IPreferencePage#performOk
	 */
	@Override
	public boolean performOk() {
		if (fCPathsBlock != null) {
			getSettings().put(INDEX, fCPathsBlock.getPageIndex());

			Shell shell = getControl().getShell();
			IRunnableWithProgress runnable = new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						fCPathsBlock.configureCProject(monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			};
			IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);
			try {
				new ProgressMonitorDialog(shell).run(true, true, op);
			} catch (InvocationTargetException e) {
				String title = CPathEntryMessages.CPathsPropertyPage_error_title; 
				String message = CPathEntryMessages.CPathsPropertyPage_error_message; 
				ExceptionHandler.handle(e, shell, title, message);
				return false;
			} catch (InterruptedException e) {
				// cancelled
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IStatusChangeListener#statusChanged
	 */
	public void statusChanged(IStatus status) {
		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performCancel()
	 */
	@Override
	public boolean performCancel() {
		if (fCPathsBlock != null) {
			getSettings().put(INDEX, fCPathsBlock.getPageIndex());
		}
		return super.performCancel();
	}
}
