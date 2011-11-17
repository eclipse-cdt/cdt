/*******************************************************************************
 * Copyright (c) 2010, 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIImages;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIPlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Dialog to let the user to clean and rebuild configurations of the selected projects.
 */
public class CleanAndBuildDialog extends MessageDialog {
	private static final String DIALOG_SETTINGS_SECTION = "RebuildConfigurationsDialogSettings"; //$NON-NLS-1$
	private static final String DIALOG_ORIGIN_X = "DIALOG_X_ORIGIN"; //$NON-NLS-1$
	private static final String DIALOG_ORIGIN_Y = "DIALOG_Y_ORIGIN"; //$NON-NLS-1$
	private static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$
	private static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$

	private Button cleanCheckbox;
	private Button buildCheckbox;

	private ContainerCheckedTreeViewer cfgCheckboxViewer;

	private IProject[] projects;
	private Object[] selected;
	private int cleanKind;
	private int buildKind;


	private class ConfigurationLabelProvider implements ILabelProvider {
		WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider();

		@Override
		public void addListener(ILabelProviderListener listener) {
			workbenchLabelProvider.addListener(listener);
		}

		@Override
		public void dispose() {
			workbenchLabelProvider.dispose();
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return workbenchLabelProvider.isLabelProperty(element, property);
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			workbenchLabelProvider.removeListener(listener);
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof ICConfigurationDescription)
				return ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_CONFIG);
			return workbenchLabelProvider.getImage(element);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof ICConfigurationDescription) {
				ICConfigurationDescription cfgDescription = (ICConfigurationDescription) element;
				String name = cfgDescription.getName();
				if (cfgDescription.isActive()) {
					return name + ' ' + Messages.CleanAndBuildDialog_Active;
				}
				return name;
			}

			return workbenchLabelProvider.getText(element);
		}
	}

	private class ConfigurationContentProvider implements ITreeContentProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		@Override
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof IProject[])
				return ((IProject[]) element).length > 0;

			if (element instanceof IProject) {
				IProject project = (IProject) element;
				ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(project, false);
				if (prjd == null)
					return false;

				ICConfigurationDescription[] cfgDescriptions = prjd.getConfigurations();
				return cfgDescriptions != null && cfgDescriptions.length > 0;
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		@Override
		public Object getParent(Object element) {
			if (element instanceof IProject) {
				return projects;
			}
			if (element instanceof ICConfigurationDescription) {
				ICConfigurationDescription cfgDescription = (ICConfigurationDescription) element;
				return cfgDescription.getProjectDescription().getProject();
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof IProject[])
				return (IProject[]) parentElement;

			if (parentElement instanceof IProject) {
				IProject project = (IProject) parentElement;
				ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(project, false);
				if (prjd != null) {
					ICConfigurationDescription[] cfgDescriptions = prjd.getConfigurations();
					return cfgDescriptions;
				}
			}
			return new Object[0];
		}
	}

	/**
	 * Creates a new Clean and Build dialog.
	 *
	 * @param projects - the currently selected projects
	 */
	public CleanAndBuildDialog(IProject[] projects) {
		super(CUIPlugin.getActiveWorkbenchShell(), Messages.CleanAndBuildDialog_RebuildConfigurations, null,
				Messages.CleanAndBuildDialog_SelectConfigurations, NONE,
				new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL },
				0);
		this.projects = projects;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		if (buttonId != IDialogConstants.OK_ID) {
			return;
		}

		// save all dirty editors
		BuildUtilities.saveEditors(null);

		if (selected!=null) {
			List<ICConfigurationDescription> cfgDescriptions = new ArrayList<ICConfigurationDescription>();
			for (Object sel : selected) {
				if (sel instanceof ICConfigurationDescription) {
					cfgDescriptions.add((ICConfigurationDescription)sel);
				}
			}

			if (cleanKind!=0 || buildKind!=0) {
				ICConfigurationDescription[] cfgdArray = cfgDescriptions.toArray(new ICConfigurationDescription[cfgDescriptions.size()]);
				Job buildJob = new BuildConfigurationsJob(cfgdArray, cleanKind, buildKind);
				buildJob.schedule();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createCustomArea(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		area.setLayout(layout);
		area.setLayoutData(new GridData(GridData.FILL_BOTH));
		SelectionListener updateEnablement = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		};

		// second row
		createProjectSelectionTable(area);

		// third row
		cleanCheckbox = new Button(parent, SWT.CHECK);
		cleanCheckbox.setText(Messages.CleanAndBuildDialog_CleanConfigurations);
		cleanCheckbox.setSelection(true);
		cleanCheckbox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		cleanCheckbox.addSelectionListener(updateEnablement);
		cleanKind = cleanCheckbox.getSelection() ? IncrementalProjectBuilder.CLEAN_BUILD : 0;

		buildCheckbox = new Button(parent, SWT.CHECK);
		buildCheckbox.setText(Messages.CleanAndBuildDialog_BuildConfigurations);
		buildCheckbox.setSelection(true);
		buildCheckbox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		buildCheckbox.addSelectionListener(updateEnablement);
		buildKind = buildCheckbox.getSelection() ? IncrementalProjectBuilder.INCREMENTAL_BUILD : 0;

		return area;
	}

	private void createProjectSelectionTable(Composite area) {
		cfgCheckboxViewer = new ContainerCheckedTreeViewer(area, SWT.BORDER);
		cfgCheckboxViewer.setContentProvider(new ConfigurationContentProvider());
		cfgCheckboxViewer.setLabelProvider(new ConfigurationLabelProvider());
		cfgCheckboxViewer.setInput(projects);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.heightHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		cfgCheckboxViewer.getControl().setLayoutData(data);

		ArrayList<ICConfigurationDescription> initialSelection = new ArrayList<ICConfigurationDescription>(projects.length);
		for (IProject prj : projects) {
			ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(prj, false);
			if (prjd == null)
				continue;
			cfgCheckboxViewer.setChecked(prjd.getActiveConfiguration(), true);
			initialSelection.add(prjd.getActiveConfiguration());
		}
		cfgCheckboxViewer.expandAll();
		cfgCheckboxViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateEnablement();
				selected = cfgCheckboxViewer.getCheckedElements();
			}
		});
		selected = cfgCheckboxViewer.getCheckedElements();
		selected = initialSelection.toArray(new ICConfigurationDescription[initialSelection.size()]);
	}

	/**
	 * Updates the enablement of the dialog's ok button based on the current choices in the dialog.
	 */
	protected void updateEnablement() {
		cleanKind = cleanCheckbox.getSelection() ? IncrementalProjectBuilder.CLEAN_BUILD : 0;
		buildKind = buildCheckbox.getSelection() ? IncrementalProjectBuilder.INCREMENTAL_BUILD : 0;
		boolean enabled = cfgCheckboxViewer.getCheckedElements().length>0 && (cleanKind!=0 || buildKind!=0);
		getButton(OK).setEnabled(enabled);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.window.Window#close()
	 */
	@Override
	public boolean close() {
		persistDialogSettings(getShell(), DIALOG_SETTINGS_SECTION);
		return super.close();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
	 */
	@Override
	protected Point getInitialLocation(Point initialSize) {
		Point p = getInitialLocation(DIALOG_SETTINGS_SECTION);
		return p != null ? p : super.getInitialLocation(initialSize);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	@Override
	protected Point getInitialSize() {
		Point p = super.getInitialSize();
		return getInitialSize(DIALOG_SETTINGS_SECTION, p);
	}

	/**
	 * Returns the initial location which is persisted in the IDE Plugin dialog settings under the provided
	 * dialog settings section name. If location is not persisted in the settings, the {@code null} is
	 * returned.
	 *
	 * @param dialogSettingsSectionName - The name of the dialog settings section
	 * @return The initial location or {@code null}
	 */
	private Point getInitialLocation(String dialogSettingsSectionName) {
		IDialogSettings settings = getDialogSettings(dialogSettingsSectionName);
		try {
			int x = settings.getInt(DIALOG_ORIGIN_X);
			int y = settings.getInt(DIALOG_ORIGIN_Y);
			return new Point(x, y);
		} catch (NumberFormatException e) {
		}
		return null;
	}

	private IDialogSettings getDialogSettings(String dialogSettingsSectionName) {
		IDialogSettings settings = ManagedBuilderUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(dialogSettingsSectionName);
		if (section == null) {
			section = settings.addNewSection(dialogSettingsSectionName);
		}
		return section;
	}

	/**
	 * Persists the location and dimensions of the shell and other user settings in the plugin's dialog
	 * settings under the provided dialog settings section name
	 *
	 * @param shell - The shell whose geometry is to be stored
	 * @param dialogSettingsSectionName - The name of the dialog settings section
	 */
	private void persistDialogSettings(Shell shell, String dialogSettingsSectionName) {
		Point shellLocation = shell.getLocation();
		Point shellSize = shell.getSize();
		IDialogSettings settings = getDialogSettings(dialogSettingsSectionName);
		settings.put(DIALOG_ORIGIN_X, shellLocation.x);
		settings.put(DIALOG_ORIGIN_Y, shellLocation.y);
		settings.put(DIALOG_WIDTH, shellSize.x);
		settings.put(DIALOG_HEIGHT, shellSize.y);
	}

	/**
	 * Returns the initial size which is the larger of the <code>initialSize</code> or the size persisted in
	 * the IDE UI Plugin dialog settings under the provided dialog setttings section name. If no size is
	 * persisted in the settings, the {@code initialSize} is returned.
	 *
	 * @param initialSize - The initialSize to compare against
	 * @param dialogSettingsSectionName - The name of the dialog settings section
	 * @return the initial size
	 */
	private Point getInitialSize(String dialogSettingsSectionName, Point initialSize) {
		IDialogSettings settings = getDialogSettings(dialogSettingsSectionName);
		try {
			int x, y;
			x = settings.getInt(DIALOG_WIDTH);
			y = settings.getInt(DIALOG_HEIGHT);
			return new Point(Math.max(x, initialSize.x), Math.max(y, initialSize.y));
		} catch (NumberFormatException e) {
		}
		return initialSize;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	@Override
	protected boolean isResizable() {
		return true;
	}
}
