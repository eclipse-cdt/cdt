/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
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
 * @see PropertyPage
 */
public class CPathPropertyPage extends PropertyPage implements IStatusChangeListener{

	private static final String PAGE_SETTINGS = "CPathsPropertyPage"; //$NON-NLS-1$
	private static final String INDEX = "pageIndex"; //$NON-NLS-1$

	CPathTabBlock fCPathsBlock;
	IPathEntryStore fStore;

	/**
	 * @see PropertyPage#createContents
	 */
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
		label.setText(CPathEntryMessages.getString("CPathsPropertyPage.no_C_project.message")); //$NON-NLS-1$

		fCPathsBlock = null;
		setValid(true);
		return label;
	}

	/*
	 * Content for closed projects.
	 */
	private Control createForClosedProject(Composite parent) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(CPathEntryMessages.getString("CPathsPropertyPage.closed_project.message")); //$NON-NLS-1$

		fCPathsBlock = null;
		setValid(true);
		return label;
	}

	public void setVisible(boolean visible) {
		if (fCPathsBlock != null) {
			if (!visible) {
				if (fCPathsBlock.hasChangesInDialog()) {
					String title = CPathEntryMessages.getString("CPathsPropertyPage.unsavedchanges.title"); //$NON-NLS-1$
					String message = CPathEntryMessages.getString("CPathsPropertyPage.unsavedchanges.message"); //$NON-NLS-1$
					String[] buttonLabels = new String[]{
							CPathEntryMessages.getString("CPathsPropertyPage.unsavedchanges.button.save"), //$NON-NLS-1$
							CPathEntryMessages.getString("CPathsPropertyPage.unsavedchanges.button.discard"), //$NON-NLS-1$
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
			CUIPlugin.getDefault().log(e);
		}
		return false;
	}

	/*
	 * @see IPreferencePage#performOk
	 */
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
				String title = CPathEntryMessages.getString("CPathsPropertyPage.error.title"); //$NON-NLS-1$
				String message = CPathEntryMessages.getString("CPathsPropertyPage.error.message"); //$NON-NLS-1$
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
	public boolean performCancel() {
		if (fCPathsBlock != null) {
			getSettings().put(INDEX, fCPathsBlock.getPageIndex());
		}
		return super.performCancel();
	}
}