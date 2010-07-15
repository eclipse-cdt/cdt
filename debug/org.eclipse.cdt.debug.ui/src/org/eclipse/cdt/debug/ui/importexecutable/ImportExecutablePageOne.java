/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.importexecutable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
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
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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

	private String[] supportedBinaryParserIds;
	private IBinaryParser[] supportedBinaryParsers;

	private IExtension[] binaryParserExtensions;

	private Combo binaryParserCombo;
	
	public ImportExecutablePageOne(AbstractImportExecutableWizard wizard) {
		super("ImportApplicationPageOne"); //$NON-NLS-1$
		this.wizard = wizard;
		setPageComplete(false);
		setTitle(wizard.getPageOneTitle());
		setDescription(wizard.getPageOneDescription());
		
		supportedBinaryParserIds = wizard.getDefaultBinaryParserIDs();
		
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, CCorePlugin.BINARY_PARSER_SIMPLE_ID);
		if (point != null)
		{
			IExtension[] exts = point.getExtensions();
			ArrayList extensionsInUse = new ArrayList();
			for (int i = 0; i < exts.length; i++) {
				if (isExtensionVisible(exts[i])) {
					extensionsInUse.add(exts[i]);
				}
			}
			binaryParserExtensions = (IExtension[]) extensionsInUse.toArray(new IExtension[extensionsInUse.size()]);
		}
		
		supportedBinaryParsers = new IBinaryParser[supportedBinaryParserIds.length];
		for (int i = 0; i < supportedBinaryParserIds.length; i++) {
			for (int j = 0; j < binaryParserExtensions.length; j++) {
				if (binaryParserExtensions[j].getUniqueIdentifier().equals(supportedBinaryParserIds[i]))
					supportedBinaryParsers[i] = instantiateBinaryParser(binaryParserExtensions[j]);				
			}
		}

	}

	public String[] getSupportedBinaryParserIds() {
		return supportedBinaryParserIds;
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
		File[] contents = directory.listFiles();
		monitor.subTask(directory.getPath());
		SubProgressMonitor sm = new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN);
		sm.beginTask(directory.getPath(), contents.length);
		for (int i = 0; i < contents.length; i++) {
			if (monitor.isCanceled())
				return false;
			File file = contents[i];
			sm.worked(1);
			if (contents[i].isDirectory())
				collectExecutableFiles(files, contents[i], monitor);
			else if (file.isFile() && isBinary(file, false)) {
				files.add(file);
			}
		}
		sm.done();
		return true;
	}

	public void createControl(Composite parent) {
		
		initializeDialogUnits(parent);

		Composite workArea = new Composite(parent, SWT.NONE);
		setControl(workArea);

		workArea.setLayout(new GridLayout());
		workArea.setLayoutData(new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		
		//bug 189003: to fix the tab order on the page
		if (wizard.userSelectsBinaryParser()) {
			Composite binaryParserGroup = new Composite(workArea, SWT.NONE);
			GridLayout layout = new GridLayout(3, false);
			layout.numColumns = 3;
			layout.makeColumnsEqualWidth = false;
			layout.marginWidth = 0;
			binaryParserGroup.setLayout(layout);
			createSelectBinaryParser(binaryParserGroup);
		}
		
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
		CDebugUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp( getControl(), ICDebugHelpContextIds.IMPORT_EXECUTABLE_PAGE_ONE );
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
		executablesViewer.getTree().getAccessible().addAccessibleListener(
	            new AccessibleAdapter() {                       
	                public void getName(AccessibleEvent e) {
	                        e.result = wizard.getExecutableListLabel();
	                }
	            }
	        );
		createSelectionButtons(listComposite);

	}

	private void createExecutablesRoot(Composite workArea) {

		selectMultipleButton = new Button(workArea, SWT.RADIO);
		selectMultipleButton.setText(Messages.ImportExecutablePageOne_SearchDirectory);
		selectMultipleButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				checkControlState();
				String selectedDirectory = multipleExecutablePathField
						.getText().trim();
				setErrorMessage(null);

				if (selectedDirectory.length() == 0) {
					noFilesSelected();
				} else
					updateExecutablesList(selectedDirectory);
			}

		});

		// project location entry field
		this.multipleExecutablePathField = new Text(workArea, SWT.BORDER);
		multipleExecutablePathField.getAccessible().addAccessibleListener(
            new AccessibleAdapter() {                       
                public void getName(AccessibleEvent e) {
                        e.result = Messages.ImportExecutablePageOne_SearchDirectory;
                }
            }
        );
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

	private void createSelectBinaryParser(Composite workArea) {

		if (binaryParserExtensions.length == 0)
			return;
		
		Label label = new Label(workArea, SWT.NONE);
		label.setText(Messages.ImportExecutablePageOne_SelectBinaryParser);
		
		binaryParserCombo = new Combo(workArea, SWT.READ_ONLY);
		final IExtension[] exts = binaryParserExtensions;
		for (int i = 0; i < exts.length; i++) {
				binaryParserCombo.add(exts[i].getLabel());
				if (supportedBinaryParserIds[0].equals(exts[i].getUniqueIdentifier()))
					binaryParserCombo.select(i);
			}
		
		
		binaryParserCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				supportedBinaryParsers[0] = instantiateBinaryParser(exts[binaryParserCombo.getSelectionIndex()]);
				supportedBinaryParserIds[0] = exts[binaryParserCombo.getSelectionIndex()].getUniqueIdentifier();
				if (selectSingleFile) {
					String path = singleExecutablePathField.getText();
					if (path.length() > 0)
						validateExe(path);
				} else {
					previouslySearchedDirectory = null;
					updateExecutablesList(multipleExecutablePathField.getText().trim());
				}
			}
		});
		
		// Dummy to fill out the third column
		new Label(workArea, SWT.NONE);
	}
	
	private static boolean isExtensionVisible(IExtension ext) {
 		IConfigurationElement[] elements = ext.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement[] children = elements[i].getChildren("filter"); //$NON-NLS-1$
			for (int j = 0; j < children.length; j++) {
				String name = children[j].getAttribute("name"); //$NON-NLS-1$
				if (name != null && name.equals("visibility")) { //$NON-NLS-1$
					String value = children[j].getAttribute("value"); //$NON-NLS-1$
					if (value != null && value.equals("private")) { //$NON-NLS-1$
						return false;
					}
				}
			}
			return true;
		}
		return false; // invalid extension definition (must have at least cextension elements)
	}

	private IBinaryParser instantiateBinaryParser(IExtension ext) {
		IBinaryParser parser = null;
 		IConfigurationElement[] elements = ext.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement[] children = elements[i].getChildren("run"); //$NON-NLS-1$
			for (int j = 0; j < children.length; j++) {
				try {
					parser = (IBinaryParser)children[j].createExecutableExtension("class"); //$NON-NLS-1$
				} catch (CoreException e) {
					CDebugUIPlugin.log(e);
				}
			}
		}
		return parser;
	}
	
	private void createSelectExecutable(Composite workArea) {
		// project specification group

		selectSingleButton = new Button(workArea, SWT.RADIO);
		selectSingleButton.setText(Messages.ImportExecutablePageOne_SelectExecutable);
		selectSingleButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				checkControlState();
				if (selectSingleFile) {
					if (singleExecutablePathField.getText().trim().length() == 0)
						noFilesSelected();
					else
						validateExe(singleExecutablePathField.getText());
				}
			}
		});

		// project location entry field
		this.singleExecutablePathField = new Text(workArea, SWT.BORDER);
		singleExecutablePathField.getAccessible().addAccessibleListener(
            new AccessibleAdapter() {                       
                public void getName(AccessibleEvent e) {
                        e.result = Messages.ImportExecutablePageOne_SelectExecutable;
                }
            }
        );
		// Set the data name field so Abbot based tests can find it.
		singleExecutablePathField.setData("name", "singleExecutablePathField"); //$NON-NLS-1$ //$NON-NLS-2$
		singleExecutablePathField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateExe(singleExecutablePathField.getText());
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
					if (Platform.getOS().equals(Platform.OS_MACOSX) && res.endsWith(".app")) //$NON-NLS-1$
					{
						// On Mac OS X the file dialog will let you select the 
						// package but not the executable inside.
						Path macPath = new Path(res);
						res = res + "/Contents/MacOS/" + macPath.lastSegment(); //$NON-NLS-1$
						res = res.substring(0, res.length() - 4);
					}
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
				executablesViewer.setAllChecked(true);
				setPageComplete(executables.length > 0);
			}
		});

		setButtonLayoutData(selectAll);

		deselectAll = new Button(buttonsComposite, SWT.PUSH);
		deselectAll.setText(Messages.ImportExecutablePageOne_DeselectAll);
		deselectAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				executablesViewer.setAllChecked(false);
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
		executablesViewer.setAllChecked(false);
		previouslySearchedDirectory = ""; //$NON-NLS-1$
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

					monitor.beginTask(Messages.ImportExecutablePageOne_Searching, IProgressMonitor.UNKNOWN);
					File directory = new File(path);
					executables = new File[0];
					if (directory.isDirectory()) {

						Collection files = new ArrayList();
						if (!collectExecutableFiles(files, directory, monitor))
							return;
						executables = (File[]) files.toArray(new File[files.size()]);
					}
					monitor.done();
				}

			});
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
			// Nothing to do if the user interrupts.
		}

		executablesViewer.refresh(true);
		executablesViewer.setAllChecked(true);
		setPageComplete(executables.length > 0);
	}

	private boolean isBinary(File file, IBinaryParser parser) {
		if (parser != null) {
			try {
				IBinaryParser.IBinaryFile bin = parser.getBinary(new Path(file
						.getAbsolutePath()));
				return bin != null
						&& (bin.getType() == IBinaryParser.IBinaryFile.EXECUTABLE || bin
								.getType() == IBinaryParser.IBinaryFile.SHARED);
			} catch (IOException e) {
				return false;
			}
		} else
			return false;
	}

	/**
	 * Checks to see if the file is a valid binary recognized by any of the
	 * available binary parsers. If the currently selected parser doesn't work
	 * it checks the other parsers. If another recognizes the file then the
	 * selected binary parser is changed accordingly.
	 * The effect is to allow the user's file choice to trump the binary
	 * parser selection since most people will have a better idea of what
	 * file they want to select and may not know which binary parser to try.
	 * @param file - the executable file.
	 * @return - is it recognized by any of the binary parsers?
	 */
	private boolean isBinary(File file, boolean checkOthers) {
		
		for (int i = 0; i < supportedBinaryParsers.length; i++) {
			if (isBinary(file, supportedBinaryParsers[i]))
				return true;			
		}
		// See if any of the other parsers will work with this file.
		// If so, pick the first one that will. Only do this if the user
		// is picking the binary parser.
		if (checkOthers && binaryParserCombo != null)
		{
			for (int i = 0; i < binaryParserExtensions.length; i++) {
				IBinaryParser parser = instantiateBinaryParser(binaryParserExtensions[i]);
				if (isBinary(file, parser))
				{
					supportedBinaryParserIds[0] = binaryParserExtensions[i].getUniqueIdentifier();
					supportedBinaryParsers[0] = parser;
					binaryParserCombo.select(i);
					return true;
				}
			}			
		}

		return false;
	}
	
	private void validateExe(String path) {
		setErrorMessage(null);
		setPageComplete(false);
		if (path.length() > 0) {
			File testFile = new File(path);
			if (testFile.exists()) {
				if (isBinary(testFile, true))
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
}
