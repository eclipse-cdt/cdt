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
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.resources.IPathEntryStore;
import org.eclipse.cdt.core.resources.IPathEntryStoreListener;
import org.eclipse.cdt.core.resources.PathEntryStoreChangedEvent;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
 * C/C++ Include Paths and Symbols page for 3.X projects.
 * 
 * This page lives dormant as of writing (CDT 7.0) but may get activated for
 * {@code org.eclipse.cdt.make.core.makeNature} project (3.X style).
 * 
 * @deprecated as of CDT 4.0.
 */
@Deprecated
public class IncludesSymbolsPropertyPage extends PropertyPage implements IStatusChangeListener, IPathEntryStoreListener {

	private static final String PAGE_SETTINGS = "IncludeSysmbolsPropertyPage"; //$NON-NLS-1$
	private static final String INDEX = "pageIndex"; //$NON-NLS-1$

	NewIncludesSymbolsTabBlock fIncludesSymbolsBlock;
	IPathEntryStore fStore;

	@Override
	protected Control createContents(Composite parent) {
		IProject project = getProject();
		Control result;
		if (project == null || !isCProject(project)) {
			result = createWithoutCProject(parent);
		} else if (!project.isOpen()) {
			result = createForClosedProject(parent);
		} else {
			try {
				fStore = CoreModel.getPathEntryStore(getProject());
				fStore.addPathEntryStoreListener(this);
			} catch (CoreException e) {
			}
			result = createWithCProject(parent, project);
		}
		Dialog.applyDialogFont(result);
		noDefaultAndApplyButton();
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
		fIncludesSymbolsBlock = new NewIncludesSymbolsTabBlock(this, getSettings().getInt(INDEX));
		fIncludesSymbolsBlock.init(getCElement(), null);
		return fIncludesSymbolsBlock.createContents(parent);
	}

	/*
	 * Content for non-C projects.
	 */
	private Control createWithoutCProject(Composite parent) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(CPathEntryMessages.CPathsPropertyPage_no_C_project_message); 

		fIncludesSymbolsBlock = null;
		setValid(true);
		return label;
	}

	/*
	 * Content for closed projects.
	 */
	private Control createForClosedProject(Composite parent) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(CPathEntryMessages.CPathsPropertyPage_closed_project_message); 

		fIncludesSymbolsBlock = null;
		setValid(true);
		return label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	@Override
	public void dispose() {
		if (fStore != null) {
			fStore.removePathEntryStoreListener(this);
		}
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (fIncludesSymbolsBlock != null) {
			if (!visible) {
				if (fIncludesSymbolsBlock.hasChangesInDialog()) {
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
						fIncludesSymbolsBlock.init(getCElement(), null);
					}
				}
			} else {
				if (!fIncludesSymbolsBlock.hasChangesInDialog() && fIncludesSymbolsBlock.hasChangesInCPathFile()) {
					fIncludesSymbolsBlock.init(getCElement(), null);
				}
			}
		}
		super.setVisible(visible);
	}

	private IProject getProject() {
		IAdaptable adaptable = getElement();
		if (adaptable != null) {
			IResource resource = (IResource)adaptable.getAdapter(IResource.class);
			return resource.getProject();
		}
		return null;
	}

	protected ICElement getCElement() {
		IAdaptable adaptable = getElement();
		if (adaptable != null) {
			ICElement elem = (ICElement)adaptable.getAdapter(ICElement.class);
			return elem;
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
		if (fIncludesSymbolsBlock != null) {
			getSettings().put(INDEX, fIncludesSymbolsBlock.getPageIndex());

			Shell shell = getControl().getShell();
			IRunnableWithProgress runnable = new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						fIncludesSymbolsBlock.configureCProject(monitor);
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
	@Override
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
		if (fIncludesSymbolsBlock != null) {
			getSettings().put(INDEX, fIncludesSymbolsBlock.getPageIndex());
		}
		return super.performCancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.resources.IPathEntryStoreListener#pathEntryStoreChanged(org.eclipse.cdt.core.resources.PathEntryStoreChangedEvent)
	 */
	@Override
	public void pathEntryStoreChanged(PathEntryStoreChangedEvent event) {
		if (event.hasContentChanged()) {
			Control control = getControl();
			if (control != null && !control.isDisposed()) {
				control.getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						Control control = getControl();
						if (control != null && !control.isDisposed()) {
							try {
								fIncludesSymbolsBlock.init(getCElement(), fIncludesSymbolsBlock.getRawCPath());
							} catch (CModelException e) {
								CUIPlugin.log(e);
							}
						}
					}
				});
			}
		}

	}

}
