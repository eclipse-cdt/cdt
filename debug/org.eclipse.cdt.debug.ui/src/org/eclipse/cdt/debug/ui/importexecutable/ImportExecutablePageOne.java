/*******************************************************************************
 * Copyright (c) 2006 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.importexecutable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ImportExecutablePageOne extends WizardPage {

	// Keep track of the directory that we browsed the last time
	// the wizard was invoked.
	private static String previouslyBrowsedDirectory = ""; //$NON-NLS-1$

	private Text multipleExecutablePathField;

	private CheckboxTreeViewer executablesViewer;

	private File[] executables = new File[0];

	private String previouslySearchedDirectory;

	private Text singleExecutablePathField;

	private boolean selectSingleFile = true;

	private Button selectSingleButton;

	private Button selectSingleBrowseButton;

	private Button selectMultipleButton;

	private Button selectMultipleBrowseButton;

	private Button selectAll;

	private Button deselectAll;

	private Label selectMultipleTitle;

	private AbstractImportExecutableWizard wizard;

	public ImportExecutablePageOne(AbstractImportExecutableWizard wizard) {
		super("ImportApplicationPageOne");
		this.wizard = wizard;
		setPageComplete(false);
		setTitle(wizard.getPageOneTitle());
		setDescription(wizard.getPageOneDescription());
	}

	private void checkControlState() {
		selectSingleFile = selectSingleButton.getSelection();
		singleExecutablePathField.setEnabled(selectSingleFile);
		selectSingleBrowseButton.setEnabled(selectSingleFile);

		multipleExecutablePathField.setEnabled(!selectSingleFile);
		selectMultipleBrowseButton.setEnabled(!selectSingleFile);
		selectAll.setEnabled(!selectSingleFile);
		deselectAll.setEnabled(!selectSingleFile);
		selectMultipleTitle.setEnabled(!selectSingleFile);
	}

	private boolean collectExecutableFiles(Collection files, File directory,
			IProgressMonitor monitor) {

		if (monitor.isCanceled())
			return false;
		monitor.subTask(directory.getPath());
		File[] contents = directory.listFiles();
		// first look for project description files

		for (int i = 0; i < contents.length; i++) {
			File file = contents[i];
			if (file.isFile() && wizard.isExecutableFile(file)) {
				files.add(file);
			}
		}
		// no project description found, so recurse into sub-directories
		for (int i = 0; i < contents.length; i++) {
			if (contents[i].isDirectory())
				collectExecutableFiles(files, contents[i], monitor);
		}
		return true;
	}

	public void createControl(Composite parent) {

		initializeDialogUnits(parent);

		Composite workArea = new Composite(parent, SWT.NONE);
		setControl(workArea);

		workArea.setLayout(new GridLayout());
		workArea.setLayoutData(new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

		Composite selectExecutableGroup = new Composite(workArea, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = 0;
		selectExecutableGroup.setLayout(layout);
		selectExecutableGroup.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));

		createSelectExecutable(selectExecutableGroup);
		createExecutablesRoot(selectExecutableGroup);
		createExecutablesList(workArea);
		Dialog.applyDialogFont(workArea);
		selectSingleButton.setSelection(true);
		checkControlState();
	}

	private void createExecutablesList(Composite workArea) {

		selectMultipleTitle = new Label(workArea, SWT.NONE);
		selectMultipleTitle.setText(wizard.getExecutableListLabel());

		Composite listComposite = new Composite(workArea, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.makeColumnsEqualWidth = false;
		listComposite.setLayout(layout);

		listComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL | GridData.FILL_BOTH));

		executablesViewer = new CheckboxTreeViewer(listComposite, SWT.BORDER);
		GridData listData = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		executablesViewer.getControl().setLayoutData(listData);

		executablesViewer.setContentProvider(new ITreeContentProvider() {

			public void dispose() {

			}

			public Object[] getChildren(Object parentElement) {
				return null;
			}

			public Object[] getElements(Object inputElement) {
				return executables;
			}

			public Object getParent(Object element) {
				return null;
			}

			public boolean hasChildren(Object element) {
				return false;
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}

		});

		executablesViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((File) element).getName();
			}
		});

		executablesViewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				setPageComplete(executablesViewer.getCheckedElements().length > 0);
			}
		});

		executablesViewer.setInput(this);
		createSelectionButtons(listComposite);

	}

	private void createExecutablesRoot(Composite workArea) {

		selectMultipleButton = new Button(workArea, SWT.RADIO);
		selectMultipleButton.setText(Messages.ImportExecutablePageOne_SearchDirectory);
		selectMultipleButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				checkControlState();

				if (!selectSingleFile) {
					singleExecutablePathField.setText("");
					noFilesSelected();
				}

			}

		});

		// project location entry field
		this.multipleExecutablePathField = new Text(workArea, SWT.BORDER);

		this.multipleExecutablePathField.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		selectMultipleBrowseButton = new Button(workArea, SWT.PUSH);
		selectMultipleBrowseButton.setText(Messages.ImportExecutablePageOne_Browse);
		setButtonLayoutData(selectMultipleBrowseButton);

		selectMultipleBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleLocationBrowseButtonPressed();
			}

		});

		multipleExecutablePathField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				updateExecutablesList(multipleExecutablePathField.getText()
						.trim());
			}
		});

	}

	private void createSelectExecutable(Composite workArea) {
		// project specification group

		selectSingleButton = new Button(workArea, SWT.RADIO);
		selectSingleButton.setText(Messages.ImportExecutablePageOne_SelectExecutable);
		selectSingleButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				checkControlState();
				if (selectSingleFile) {
					multipleExecutablePathField.setText("");
					noFilesSelected();
				}
			}
		});

		// project location entry field
		this.singleExecutablePathField = new Text(workArea, SWT.BORDER);
		// Set the data name field so Abbot based tests can find it.
		singleExecutablePathField.setData("name", "singleExecutablePathField");
		singleExecutablePathField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				setErrorMessage(null);
				setPageComplete(false);
				String path = singleExecutablePathField.getText();
				if (path.length() > 0) {
					File testFile = new File(path);
					if (testFile.exists()) {
						if (wizard.isExecutableFile(testFile))
						{
							executables = new File[1];
							executables[0] = testFile;
							setPageComplete(true);
						}
						else
							setErrorMessage(Messages.ImportExecutablePageOne_NoteAnEXE);
					} else {
						setErrorMessage(Messages.ImportExecutablePageOne_NoSuchFile);
					}
				}
			}

		});

		this.singleExecutablePathField.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		selectSingleBrowseButton = new Button(workArea, SWT.PUSH);
		selectSingleBrowseButton.setText(Messages.ImportExecutablePageOne_Browse);
		setButtonLayoutData(selectSingleBrowseButton);

		selectSingleBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				wizard.setupFileDialog(dialog);
				String res = dialog.open();
				if (res != null) {
					singleExecutablePathField.setText(res);
				}
			}
		});

	}

	private void createSelectionButtons(Composite listComposite) {
		Composite buttonsComposite = new Composite(listComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttonsComposite.setLayout(layout);

		buttonsComposite.setLayoutData(new GridData(
				GridData.VERTICAL_ALIGN_BEGINNING));

		selectAll = new Button(buttonsComposite, SWT.PUSH);
		selectAll.setText(Messages.ImportExecutablePageOne_SelectAll);
		selectAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				executablesViewer.setCheckedElements(executables);
				setPageComplete(executables.length > 0);
			}
		});

		setButtonLayoutData(selectAll);

		deselectAll = new Button(buttonsComposite, SWT.PUSH);
		deselectAll.setText(Messages.ImportExecutablePageOne_DeselectAll);
		deselectAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				executablesViewer.setCheckedElements(new Object[0]);
				setPageComplete(false);
			}
		});

		setButtonLayoutData(deselectAll);

	}

	public String[] getSelectedExecutables() {
		String[] selectedExecutablePaths = new String[0];
		if (selectSingleFile) {
			if (executables.length > 0) {
				selectedExecutablePaths = new String[1];
				selectedExecutablePaths[0] = executables[0].getAbsolutePath();

			}
		} else {
			Object[] checkedFiles = executablesViewer.getCheckedElements();
			selectedExecutablePaths = new String[checkedFiles.length];
			for (int i = 0; i < checkedFiles.length; i++) {
				selectedExecutablePaths[i] = ((File) checkedFiles[i])
						.getAbsolutePath();
			}
		}
		return selectedExecutablePaths;
	}

	protected void handleLocationBrowseButtonPressed() {

		DirectoryDialog dialog = new DirectoryDialog(
				multipleExecutablePathField.getShell());
		dialog
				.setMessage(Messages.ImportExecutablePageOne_SelectADirectory);

		String dirName = multipleExecutablePathField.getText().trim();
		if (dirName.length() == 0)
			dirName = previouslyBrowsedDirectory;

		if (dirName.length() > 0) {
			File path = new File(dirName);
			if (path.exists())
				dialog.setFilterPath(new Path(dirName).toOSString());
		}

		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			previouslyBrowsedDirectory = selectedDirectory;
			multipleExecutablePathField.setText(previouslyBrowsedDirectory);
			updateExecutablesList(selectedDirectory);
		}

	}

	protected void noFilesSelected() {
		executables = new File[0];
		executablesViewer.refresh(true);
		executablesViewer.setCheckedElements(executables);
		previouslySearchedDirectory = "";
		setPageComplete(false);
	}

	protected void updateExecutablesList(final String path) {
		// don't search on empty path
		if (path == null || path.length() == 0)
			return;
		// don't repeat the same search - the user might just be tabbing to
		// traverse
		if (previouslySearchedDirectory != null
				&& previouslySearchedDirectory.equals(path))
			return;
		previouslySearchedDirectory = path;
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {

					monitor.beginTask(Messages.ImportExecutablePageOne_Searching, 100);
					File directory = new File(path);
					executables = new File[0];
					monitor.worked(10);
					if (directory.isDirectory()) {

						Collection files = new ArrayList();
						if (!collectExecutableFiles(files, directory, monitor))
							return;
						Iterator filesIterator = files.iterator();
						executables = new File[files.size()];
						int index = 0;
						monitor.worked(50);
						monitor.subTask(Messages.ImportExecutablePageOne_ProcessingResults);
						while (filesIterator.hasNext()) {
							File file = (File) filesIterator.next();
							executables[index] = file;
							index++;
						}
					} else
						monitor.worked(60);
					monitor.done();
				}

			});
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
			// Nothing to do if the user interrupts.
		}

		executablesViewer.refresh(true);
		executablesViewer.setCheckedElements(executables);
		setPageComplete(executables.length > 0);
	}
}
