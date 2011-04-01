/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [174945] split importexport icons from rse.ui
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [216252] MessageFormat.format -> NLS.bind
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Takuya Miyamoto - [185925] Integrate Platform/Team Synchronization
 * David McKnight   (IBM)        - [272708] [import/export] fix various bugs with the synchronization support
 * David McKnight   (IBM)        - [341630] [import/export] destination prompt not cleared after destination chosen
 *******************************************************************************/
package org.eclipse.rse.internal.importexport.files;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.files.ui.actions.SystemSelectRemoteFolderAction;
import org.eclipse.rse.internal.importexport.IRemoteImportExportConstants;
import org.eclipse.rse.internal.importexport.RemoteImportExportPlugin;
import org.eclipse.rse.internal.importexport.RemoteImportExportResources;
import org.eclipse.rse.internal.importexport.RemoteImportExportUtil;
import org.eclipse.rse.internal.importexport.SystemImportExportResources;
import org.eclipse.rse.internal.synchronize.RSESyncUtils;
import org.eclipse.rse.internal.synchronize.SynchronizeData;
import org.eclipse.rse.internal.synchronize.provisional.ISynchronizeOperation;
import org.eclipse.rse.internal.synchronize.provisional.SynchronizeOperation;
import org.eclipse.rse.internal.synchronize.provisional.Synchronizer;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.messages.SystemMessageLine;
import org.eclipse.rse.ui.wizards.ISystemWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.dialogs.WizardExportResourcesPage;

/**
 *	Page 1 of the base resource export-to-file-system Wizard
 *
 *  040510 AR	Fix "Create folder" question.  Previous fix changed the way we were
 * 				asking user if they wanted target folder created, to use RSE
 * 				widgets.  But introduced error.
 */
class RemoteExportWizardPage1 extends WizardExportResourcesPage implements Listener, ISystemWizardPage {
	private Object destinationFolder = null;
	private String helpId;
	private Composite parentComposite;
	private SystemMessageLine msgLine;
	private SystemMessage pendingMessage, pendingErrorMessage;
	// widgets
	private Combo destinationNameField;
	private Button destinationBrowseButton;
	protected Button reviewSynchronizeCheckbox;
	protected Button overwriteExistingFilesCheckbox;
	protected Button createDirectoryStructureButton;
	protected Button createSelectionOnlyButton;
	protected Button saveSettingsButton;
	protected Label descFilePathLabel;
	protected Text descFilePathField;
	protected Button descFileBrowseButton;
	// input object
	protected Object inputObject = null;
	// constants
	private static final int MY_SIZING_TEXT_FIELD_WIDTH = 250;
	// dialog store id constants
	private static final String STORE_DESTINATION_NAMES_ID = "RemoteExportWizard.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$
	private static final String STORE_REVIEW_SYNCHRONIZE_ID = "RemoteExportWizard.STORE_REVIEW_SYNCHRONIZE_ID"; //$NON-NLS-1$
	private static final String STORE_OVERWRITE_EXISTING_FILES_ID = "RemoteExportWizard.STORE_OVERWRITE_EXISTING_FILES_ID"; //$NON-NLS-1$
	private static final String STORE_CREATE_STRUCTURE_ID = "RemoteExportWizard.STORE_CREATE_STRUCTURE_ID"; //$NON-NLS-1$
	private static final String STORE_CREATE_DESCRIPTION_FILE_ID = "RemoteExportWizard.STORE_CREATE_DESCRIPTION_FILE_ID"; //$NON-NLS-1$
	private static final String STORE_DESCRIPTION_FILE_NAME_ID = "RemoteExportWizard.STORE_DESCRIPTION_FILE_NAME_ID"; //$NON-NLS-1$
	//messages
	private static final SystemMessage DESTINATION_EMPTY_MESSAGE = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
			IRemoteImportExportConstants.FILEMSG_DESTINATION_EMPTY,
			IStatus.ERROR,
			RemoteImportExportResources.FILEMSG_DESTINATION_EMPTY,
			RemoteImportExportResources.FILEMSG_DESTINATION_EMPTY_DETAILS);


	/**
	 *	Create an instance of this class
	 */
	protected RemoteExportWizardPage1(String name, IStructuredSelection selection) {
		super(name, selection);
		setInputObject(selection);
	}

	/**
	 *	Create an instance of this class
	 */
	public RemoteExportWizardPage1(IStructuredSelection selection) {
		this("fileSystemExportPage1", selection); //$NON-NLS-1$
		setTitle(SystemImportExportResources.RESID_FILEEXPORT_PAGE1_TITLE);
		setDescription(SystemImportExportResources.RESID_FILEEXPORT_PAGE1_DESCRIPTION);
	}

	/**
	 *	Add the passed value to self's destination widget's history
	 *
	 *	@param value java.lang.String
	 */
	protected void addDestinationItem(String value) {
		destinationNameField.add(value);
	}

	/** (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		parentComposite = new Composite(parent, SWT.NONE);
		parentComposite.setLayout(new GridLayout(1, false));
		parentComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		super.createControl(parentComposite);
		msgLine = new SystemMessageLine(parentComposite);
		msgLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		if (pendingMessage != null) setMessage(pendingMessage);
		if (pendingErrorMessage != null) setErrorMessage(pendingErrorMessage);
		giveFocusToDestination();
		SystemWidgetHelpers.setWizardPageMnemonics(parentComposite);
		if (helpId != null) {
			SystemWidgetHelpers.setHelp(parentComposite, helpId);
		} else {
			SystemWidgetHelpers.setHelp(parentComposite, RemoteImportExportPlugin.HELPPREFIX + "import_context"); //$NON-NLS-1$
		}
		setControl(parentComposite);
		//		super.createControl(parent);
		//		parentComposite = parent;
		//		giveFocusToDestination();
		//		SystemWidgetHelpers.setHelp(getControl(), RemoteImportExportPlugin.HELPPREFIX + "export_context");
		//		Control c = getControl();
		//		if (c instanceof Composite)
		//		{
		//		  SystemWidgetHelpers.setWizardPageMnemonics((Composite)c);
		//		  parentComposite = (Composite)c;
		//		  if (helpId != null)
		//			SystemWidgetHelpers.setHelp(parentComposite, helpId);
		//		}
		//		else if (c instanceof Button)
		//		{
		//			Mnemonics ms = new Mnemonics();
		//			ms.setMnemonic((Button)c);
		//		}
		//		configureMessageLine();
	}

	/**
	 *	Create the export destination specification widgets
	 *
	 *	@param parent org.eclipse.swt.widgets.Composite
	 */
	protected void createDestinationGroup(Composite parent) {
		// destination specification group
		Composite destinationSelectionGroup = SystemWidgetHelpers.createComposite(parent, 3);
		((GridData) destinationSelectionGroup.getLayoutData()).verticalAlignment = GridData.FILL;
		destinationNameField = SystemWidgetHelpers.createLabeledReadonlyCombo(destinationSelectionGroup, null, SystemImportExportResources.RESID_FILEEXPORT_DESTINATION_LABEL,
				SystemImportExportResources.RESID_FILEEXPORT_DESTINATION_TOOLTIP);
		((GridData) destinationNameField.getLayoutData()).widthHint = MY_SIZING_TEXT_FIELD_WIDTH;
		((GridData) destinationNameField.getLayoutData()).grabExcessHorizontalSpace = true;
		destinationNameField.addListener(SWT.Modify, this);
		destinationNameField.addListener(SWT.Selection, this);
		// destination browse button
		destinationBrowseButton = SystemWidgetHelpers.createPushButton(destinationSelectionGroup, null, SystemImportExportResources.RESID_FILEEXPORT_DESTINATION_BROWSE_LABEL,
				SystemImportExportResources.RESID_FILEEXPORT_DESTINATION_BROWSE_TOOLTIP);
		((GridData) destinationBrowseButton.getLayoutData()).grabExcessHorizontalSpace = false;
		destinationBrowseButton.addListener(SWT.Selection, this);
		new Label(parent, SWT.NONE); // vertical spacer
	}

	/**
	 *	Create the export options specification widgets.
	 *	@param optionsGroup the group into which the option buttons will be placed
	 */
	protected void createOptionsGroupButtons(Group optionsGroup) {
		reviewSynchronizeCheckbox = SystemWidgetHelpers.createCheckBox(optionsGroup, 1, null, SystemImportExportResources.RESID_FILEEXPORT_REVIEW_LABEL,
				SystemImportExportResources.RESID_FILEEXPORT_REVIEW_TOOLTIP);
		reviewSynchronizeCheckbox.addListener(SWT.Selection, this);
		overwriteExistingFilesCheckbox = SystemWidgetHelpers.createCheckBox(optionsGroup, 1, null, SystemImportExportResources.RESID_FILEEXPORT_OPTION_OVERWRITE_LABEL,
				SystemImportExportResources.RESID_FILEEXPORT_OPTION_OVERWRITE_TOOLTIP);
		createDirectoryStructureButton = SystemWidgetHelpers.createRadioButton(optionsGroup, null, SystemImportExportResources.RESID_FILEEXPORT_OPTION_CREATEALL_LABEL,
				SystemImportExportResources.RESID_FILEEXPORT_OPTION_CREATEALL_TOOLTIP);
		createSelectionOnlyButton = SystemWidgetHelpers.createRadioButton(optionsGroup, null, SystemImportExportResources.RESID_FILEEXPORT_OPTION_CREATESEL_LABEL,
				SystemImportExportResources.RESID_FILEEXPORT_OPTION_CREATESEL_TOOLTIP);
		createSelectionOnlyButton.setSelection(true);
		Composite comp = SystemWidgetHelpers.createComposite(optionsGroup, 3);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginWidth = 0;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		saveSettingsButton = SystemWidgetHelpers.createCheckBox(comp, 3, null, SystemImportExportResources.RESID_FILEEXPORT_OPTION_SETTINGS_LABEL,
				SystemImportExportResources.RESID_FILEEXPORT_OPTION_SETTINGS_TOOLTIP);
		saveSettingsButton.addListener(SWT.Selection, this);
		descFilePathLabel = new Label(comp, SWT.NONE);
		descFilePathLabel.setText(SystemImportExportResources.RESID_FILEEXPORT_OPTION_SETTINGS_DESCFILE_LABEL);
		GridData data = new GridData();
		descFilePathLabel.setLayoutData(data);
		descFilePathField = new Text(comp, SWT.SINGLE | SWT.BORDER);
		descFilePathField.setToolTipText(SystemImportExportResources.RESID_FILEEXPORT_OPTION_SETTINGS_DESCFILE_PATH_TOOLTIP);
		data = new GridData();
		data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_FILL;
		data.grabExcessHorizontalSpace = true;
		data.widthHint = convertWidthInCharsToPixels(80);
		descFilePathField.setLayoutData(data);
		descFilePathField.addListener(SWT.Modify, this);
		descFileBrowseButton = SystemWidgetHelpers.createPushButton(comp, null, SystemImportExportResources.RESID_FILEEXPORT_OPTION_SETTINGS_DESCFILE_BROWSE_LABEL,
				SystemImportExportResources.RESID_FILEEXPORT_OPTION_SETTINGS_DESCFILE_BROWSE_TOOLTIP);
		descFileBrowseButton.addListener(SWT.Selection, this);
	}

	/**
	 * @see org.eclipse.ui.dialogs.WizardExportResourcesPage#setupBasedOnInitialSelections()
	 */
	protected void setupBasedOnInitialSelections() {
		Object input = getInputObject();
		boolean allResource = true;
		// ensure initial input, i.e. selection, comprises of IResources only
		if ((input != null) && (input instanceof IStructuredSelection)) {
			IStructuredSelection sel = (IStructuredSelection) input;
			if (sel.size() > 0) {
				Iterator z = sel.iterator();
				while (z.hasNext()) {
					Object obj = z.next();
					if (!(obj instanceof IResource)) {
						allResource = false;
						break;
					}
				}
			} else {
				allResource = false;
			}
		} else {
			allResource = false;
		}
		// if selections are all resources, call super method to setup
		if (allResource) {
			super.setupBasedOnInitialSelections();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardDataTransferPage#updateWidgetEnablements()
	 */
	protected void updateWidgetEnablements() {
		boolean isSaveSettings = isSaveSettings();
		descFilePathLabel.setEnabled(isSaveSettings);
		descFilePathField.setEnabled(isSaveSettings);
		descFileBrowseButton.setEnabled(isSaveSettings);

		// if review is selected, the other options are grayed out without save settings
		if (reviewSynchronizeCheckbox != null) { // event handling could come before the widgets are created
			boolean isReview = reviewSynchronizeCheckbox.getSelection();
			overwriteExistingFilesCheckbox.setEnabled(!isReview);
			createDirectoryStructureButton.setEnabled(!isReview);
			createSelectionOnlyButton.setEnabled(!isReview);
		}	
	}

	/**
	 * Attempts to ensure that the specified directory exists on the local file system.
	 * Answers a boolean indicating success.
	 *
	 * @return boolean
	 * @param directory java.io.File
	 */
	protected boolean ensureDirectoryExists(File directory) {
		if (!directory.exists()) {

			String msgTxt = RemoteImportExportResources.FILEMSG_TARGET_EXISTS;
			String msgDetails = NLS.bind(RemoteImportExportResources.FILEMSG_TARGET_EXISTS_DETAILS, directory.getAbsolutePath());

			SystemMessage msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
					IRemoteImportExportConstants.FILEMSG_TARGET_EXISTS,
					IStatus.ERROR, msgTxt, msgDetails);
			SystemMessageDialog dlg = new SystemMessageDialog(getContainer().getShell(), msg);
			if (!dlg.openQuestionNoException()) return false;
			if (!directory.mkdirs()) {
				msgTxt = RemoteImportExportResources.FILEMSG_CREATE_FOLDER_FAILED;
				msgDetails = NLS.bind(RemoteImportExportResources.FILEMSG_CREATE_FOLDER_FAILED_DETAILS, directory.getAbsolutePath());

				msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
						IRemoteImportExportConstants.FILEMSG_CREATE_FOLDER_FAILED,
						IStatus.ERROR, msgTxt, msgDetails);
				msg.makeSubstitution(directory.getAbsolutePath());
				setErrorMessage(msg);
				giveFocusToDestination();
				return false;
			}
		}
		return true;
	}

	/**
	 *	If the target for export does not exist then attempt to create it.
	 *	Answer a boolean indicating whether the target exists (ie.- if it
	 *	either pre-existed or this method was able to create it)
	 *
	 *	@return boolean
	 */
	protected boolean ensureTargetIsValid(File targetDirectory) {
		if (targetDirectory.exists() && !targetDirectory.isDirectory()) {
			String msgTxt = RemoteImportExportResources.FILEMSG_SOURCE_IS_FILE;
			String msgDetails = NLS.bind(RemoteImportExportResources.FILEMSG_SOURCE_IS_FILE_DETAILS, targetDirectory.getAbsolutePath());

			SystemMessage msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
					IRemoteImportExportConstants.FILEMSG_SOURCE_IS_FILE,
					IStatus.ERROR, msgTxt, msgDetails);

			setErrorMessage(msg);
			giveFocusToDestination();
			return false;
		}
		return ensureDirectoryExists(targetDirectory);
	}

	/**
	 *  Set up and execute the passed Operation.  Answer a boolean indicating success.
	 *
	 *  @return boolean
	 */
	protected boolean executeExportOperation(RemoteFileExportOperation op) {
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) { // Display error dialog if exception is NullPointer, assume this means
			// communication failure. See RemoteFileExportOperation.exportFile()
			if (!NullPointerException.class.isInstance(e.getTargetException())) {
				displayErrorDialog(e.getTargetException());
				return false;
			}
		} catch (Exception e) {
			displayErrorDialog(e.getMessage());
			return false;
		}
		IStatus status = op.getStatus();
		if (!status.isOK()) {
			String msgTxt = NLS.bind(RemoteImportExportResources.FILEMSG_EXPORT_FAILED, status);
			SystemMessage msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
					IRemoteImportExportConstants.FILEMSG_EXPORT_FAILED,
					IStatus.ERROR, msgTxt);
			SystemMessageDialog dlg = new SystemMessageDialog(getContainer().getShell(), msg);
			dlg.openWithDetails();
			return false;
		}
		return true;
	}

	/**
	 *	The Finish button was pressed.  Try to do the required work now and answer
	 *	a boolean indicating success.  If false is returned then the wizard will
	 *	not close.
	 *
	 *	@return boolean
	 */
	public boolean finish() {
		clearMessage();
		clearErrorMessage();
		boolean ret = false;
		setDestinationValue(destinationNameField.getText().trim());
		if (Utilities.isConnectionValid(destinationNameField.getText().trim(), getShell()) && isDestinationFolder()) {
			if (!ensureTargetIsValid((File) destinationFolder)) return false;
			List resourcesToExport = getWhiteCheckedResources();
			//Save dirty editors if possible but do not stop if not all are saved
			saveDirtyEditors();
			// about to invoke the operation so save our state
			saveWidgetValues();
			if (resourcesToExport.size() > 0) {
				// export data
				RemoteFileExportData data = new RemoteFileExportData();
				
				data.setContainerPath( null);
				data.setElements(resourcesToExport);
				data.setReviewSynchronize(reviewSynchronizeCheckbox.getSelection());
				data.setCreateDirectoryStructure(createDirectoryStructureButton.getSelection());
				data.setCreateSelectionOnly(createSelectionOnlyButton.getSelection());
				data.setOverWriteExistingFiles(overwriteExistingFilesCheckbox.getSelection());
				data.setSaveSettings(saveSettingsButton.getSelection());
				data.setDescriptionFilePath(getDescriptionLocation());
				data.setDestination(getDestinationValue());

				if (!reviewSynchronizeCheckbox.getSelection()) {
					// execute export
					ret = executeExportOperation(new RemoteFileExportOperation(data, this));
				} else {
					// run synchronization
					SynchronizeData data2 = new SynchronizeData(data);
					if (reviewSynchronizeCheckbox.getSelection()) {
						data2.setSynchronizeType(ISynchronizeOperation.SYNC_MODE_UI_REVIEW_INITIAL);
					}
					try {
						ret = new Synchronizer(data2).run(new SynchronizeOperation());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						ret = false;
						e.printStackTrace();
					}

					// save description after synchronize operation
					try {
						if (data.isSaveSettings()) {
							RSESyncUtils.saveDescription(data);
						}
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return ret;
			}

			String msgTxt = RemoteImportExportResources.FILEMSG_EXPORT_NONE_SELECTED;
			String msgDetails = RemoteImportExportResources.FILEMSG_EXPORT_NONE_SELECTED_DETAILS;

			SystemMessage msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
					IRemoteImportExportConstants.FILEMSG_EXPORT_NONE_SELECTED,
					IStatus.ERROR, msgTxt, msgDetails);
			setErrorMessage(msg);
			return false;
		}
		return false;
	}

	/**
	 * Gets the destination.
	 * @return the desstionation.
	 */
	protected String getDestinationValue() {
		return destinationNameField.getText().trim();
	}

	/**
	 * Gets the description.
	 * @return the description.
	 */
	protected String getDescriptionLocation() {
		return descFilePathField.getText().trim();
	}

	/**
	 * Returns whether the settings should be saved.
	 * @return whether settings should be saved.
	 */
	protected boolean isSaveSettings() {
		return saveSettingsButton.getSelection();
	}

	/**
	 *	Set the current input focus to self's destination entry field
	 */
	protected void giveFocusToDestination() {
		destinationNameField.setFocus();
	}

	/**
	 *	Open an appropriate destination browser so that the user can specify a source
	 *	to import from
	 */
	protected void handleDestinationBrowseButtonPressed() {
		SystemSelectRemoteFolderAction action = new SystemSelectRemoteFolderAction(this.getShell());
		action.setShowNewConnectionPrompt(true);
		action.setFoldersOnly(true);
		action.setShowPropertySheet(true, false);
		String destValue = getDestinationValue();
		if (destValue != null)
		{
			IHost host = Utilities.parseForSystemConnection(destValue);
			IRemoteFile path = Utilities.parseForIRemoteFile(destValue);
			action.setDefaultConnection(host);
			action.setPreSelection(path);
		}

		action.run();
		IRemoteFile folder = action.getSelectedFolder();

		if (folder != null) {
			destinationFolder = new UniFilePlus(folder);
			destValue = Utilities.getAsString((UniFilePlus)destinationFolder);
			setDestinationValue(destValue);
		}
	}

	/**
	 *	Open an appropriate destination browser so that the user can specify a source
	 *	to import from.
	 */
	protected void handleDescriptionFileBrowseButtonPressed() {
		SaveAsDialog dialog = new SaveAsDialog(getContainer().getShell());
		dialog.create();
		dialog.getShell().setText(RemoteImportExportResources.IMPORT_EXPORT_DESCRIPTION_FILE_DIALOG_TITLE);
		dialog.setMessage(RemoteImportExportResources.IMPORT_EXPORT_DESCRIPTION_FILE_DIALOG_MESSAGE);
		dialog.setOriginalFile(createFileHandle(new Path(getDescriptionLocation())));
		if (dialog.open() == Window.OK) {
			IPath path = dialog.getResult();
			path = path.removeFileExtension().addFileExtension(Utilities.EXPORT_DESCRIPTION_EXTENSION);
			descFilePathField.setText(path.toString());
		}
	}

	/**
	 * Creates a file resource handle for the file with the given workspace path.
	 * This method does not create the file resource; this is the responsibility
	 * of <code>createFile</code>.
	 *
	 * @param filePath the path of the file resource to create a handle for
	 * @return the new file resource handle
	 */
	protected IFile createFileHandle(IPath filePath) {
		if (filePath.isValidPath(filePath.toString()) && filePath.segmentCount() >= 2)
			return SystemBasePlugin.getWorkspace().getRoot().getFile(filePath);
		else
			return null;
	}

	/**
	 * Handle all events and enablements for widgets in this page
	 * @param e Event
	 */
	public void handleEvent(Event e) {
		Widget source = e.widget;
		if (source == destinationBrowseButton) {
			handleDestinationBrowseButtonPressed();
		} else if (source == descFileBrowseButton) {
			handleDescriptionFileBrowseButtonPressed();
		}
		updateWidgetEnablements();
		updatePageCompletion();
	}

	/**
	 *	Hook method for saving widget values for restoration by the next instance
	 *	of this class.
	 */
	protected void internalSaveWidgetValues() {
		// update directory names history
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
			if (directoryNames == null) directoryNames = new String[0];
			directoryNames = addToHistory(directoryNames, getDestinationValue());
			settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);
			// options
			settings.put(STORE_REVIEW_SYNCHRONIZE_ID, reviewSynchronizeCheckbox.getSelection());
			settings.put(STORE_OVERWRITE_EXISTING_FILES_ID, overwriteExistingFilesCheckbox.getSelection());
			settings.put(STORE_CREATE_STRUCTURE_ID, createDirectoryStructureButton.getSelection());
			settings.put(STORE_CREATE_DESCRIPTION_FILE_ID, isSaveSettings());
			settings.put(STORE_DESCRIPTION_FILE_NAME_ID, getDescriptionLocation());
		}
	}

	/**
	 * Method will return boolean value, will issue error if destination is
	 * null
	 */
	protected boolean isDestinationFolder() {
		boolean ret = destinationFolder != null;
		if (!ret) {
			String msgTxt = RemoteImportExportResources.MSG_IMPORT_EXPORT_UNABLE_TO_USE_CONNECTION;
			String msgDetails = RemoteImportExportResources.MSG_IMPORT_EXPORT_UNABLE_TO_USE_CONNECTION_DETAILS;
			SystemMessage msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
					IRemoteImportExportConstants.MSG_IMPORT_EXPORT_UNABLE_TO_USE_CONNECTION,
					IStatus.ERROR, msgTxt, msgDetails);
			SystemMessageDialog.show(getShell(), msg);
		}
		return ret;
	}

	/**
	 *	Hook method for restoring widget values to the values that they held
	 *	last time this wizard was used to completion.
	 */
	protected void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			// destination chosen on previous export
			String lastDestination = null;
			String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
			if (directoryNames != null) {
				for (int i = 0; i < directoryNames.length; i++) {
					// because of the way we add destination items to history, the
					// destination for the previous export would be first
					if (i == 0) {
						lastDestination = directoryNames[i];
					}
					addDestinationItem(directoryNames[i]);
				}
			}
			RemoteExportWizard parentWizard = (RemoteExportWizard) getWizard();
			boolean isInitializingFromExportData = parentWizard.getInitializeFromExportData();
			// options
			// no export data to initialize from, so prefill from previous export
			if (!isInitializingFromExportData) {
				reviewSynchronizeCheckbox.setSelection(settings.getBoolean(STORE_REVIEW_SYNCHRONIZE_ID));
				overwriteExistingFilesCheckbox.setSelection(settings.getBoolean(STORE_OVERWRITE_EXISTING_FILES_ID));
				boolean createDirectories = settings.getBoolean(STORE_CREATE_STRUCTURE_ID);
				createDirectoryStructureButton.setSelection(createDirectories);
				createSelectionOnlyButton.setSelection(!createDirectories);
				boolean saveSettings = settings.getBoolean(STORE_CREATE_DESCRIPTION_FILE_ID);
				saveSettingsButton.setSelection(saveSettings);
				String descFilePathStr = settings.get(STORE_DESCRIPTION_FILE_NAME_ID);
				if (descFilePathStr == null) {
					descFilePathStr = ""; //$NON-NLS-1$
				}
				descFilePathField.setText(descFilePathStr);
				// select previous export destination
				if (lastDestination != null) {
					setDestinationValue(lastDestination);
				}
			}
			// initialize from export data
			else {
				RemoteFileExportData data = parentWizard.getExportData();
				reviewSynchronizeCheckbox.setSelection(data.isReviewSynchronize());
				overwriteExistingFilesCheckbox.setSelection(data.isOverWriteExistingFiles());
				createDirectoryStructureButton.setSelection(data.isCreateDirectoryStructure());
				createSelectionOnlyButton.setSelection(data.isCreateSelectionOnly());
				saveSettingsButton.setSelection(data.isSaveSettings());
				String descFilePathStr = data.getDescriptionFilePath();
				if (descFilePathStr == null) {
					descFilePathStr = ""; //$NON-NLS-1$
				}
				descFilePathField.setText(descFilePathStr);
				String destinationPath = data.getDestination();
				if (destinationPath != null) {
					setDestinationValue(destinationPath);
				}
			}
		}
		// check if there was an initial selection
		// if it is a remote directory, then set the absolute path in the source name field
		Object initSelection = getInputObject();
		if ((initSelection != null) && (initSelection instanceof IStructuredSelection)) {
			IStructuredSelection sel = (IStructuredSelection) initSelection;
			if (sel.size() == 1) {
				Object theSel = sel.getFirstElement();
				if (theSel instanceof IRemoteFile) {
					IRemoteFile file = (IRemoteFile) theSel;
					// set source name if the input is a folder
					if (file.isDirectory()) {
						destinationFolder = new UniFilePlus(file);
						setDestinationValue(Utilities.getAsString((UniFilePlus) destinationFolder));
					}
				}
			}
		}
	}

	/**
	 *	Set the contents of the receivers destination specification widget to
	 *	the passed value
	 *
	 */
	protected void setDestinationValue(String path) {
		if (path.length() > 0) {
			String[] currentItems = destinationNameField.getItems();
			int selectionIndex = -1;
			for (int i = 0; i < currentItems.length && selectionIndex < 0; i++) {
				if (currentItems[i].equals(path)) selectionIndex = i;
			}
			if (selectionIndex < 0) {
				// need to add a new one.
				int oldLength = currentItems.length;
				String[] newItems = new String[oldLength + 1];
				System.arraycopy(currentItems, 0, newItems, 0, oldLength);
				newItems[oldLength] = path;
				destinationNameField.setText(path);
				destinationNameField.setItems(newItems);
				selectionIndex = oldLength;
			} else {
			}
			destinationNameField.select(selectionIndex);
		}
		destinationFolder = null; // clear destination
		IHost conn = Utilities.parseForSystemConnection(path);
		if (conn != null) {
			IRemoteFile rf = Utilities.parseForIRemoteFile(path);
			if (rf != null) destinationFolder = new UniFilePlus(rf);
		}
	}

	/**
	 *	Answer a boolean indicating whether the receivers destination specification
	 *	widgets currently all contain valid values.
	 */
	protected boolean validateDestinationGroup() {
		String destinationValue = getDestinationValue();
		if (destinationValue.length() == 0) {
			setMessage(DESTINATION_EMPTY_MESSAGE);
			return false;
		} 
		else {
			setMessage((String)null); // clear the message when a destination has been chosen
		}
		String conflictingContainer = getConflictingContainerNameFor(destinationValue);
		if (conflictingContainer != null) {
			String msgTxt = NLS.bind(RemoteImportExportResources.FILEMSG_DESTINATION_CONFLICTING, conflictingContainer);
			String msgDetails = RemoteImportExportResources.FILEMSG_DESTINATION_CONFLICTING_DETAILS;

			SystemMessage msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
					IRemoteImportExportConstants.FILEMSG_DESTINATION_CONFLICTING,
					IStatus.ERROR, msgTxt, msgDetails);
			setErrorMessage(msg);
			giveFocusToDestination();
			return false;
		}
		return true;
	}

	/**
	 * @see org.eclipse.ui.dialogs.WizardDataTransferPage#validateOptionsGroup()
	 */
	protected boolean validateOptionsGroup() {
		if (isSaveSettings()) {
			IPath location = new Path(getDescriptionLocation());
			// if location is empty, no error message, but it's not valid
			if (location.toString().length() == 0) {
				setErrorMessage((String) null);
				return false;
			}
			// location must start with '/'
			if (!location.toString().startsWith("/")) { //$NON-NLS-1$
				setErrorMessage(RemoteImportExportResources.IMPORT_EXPORT_ERROR_DESCRIPTION_ABSOLUTE);
				return false;
			}
			// find the resource, including a variant if any
			IResource resource = findResource(location);
			// if resource is not a file, it must be a container. So location is pointing to a container, which is an error
			if (resource != null && resource.getType() != IResource.FILE) {
				setErrorMessage(RemoteImportExportResources.IMPORT_EXPORT_ERROR_DESCRIPTION_EXISTING_CONTAINER);
				return false;
			}
			// get the resource (or any variant of it) after removing the last segment
			// this gets the parent resource
			resource = findResource(location.removeLastSegments(1));
			// if parent resource does not exist, or if it is a file, then it is not valid
			if (resource == null || resource.getType() == IResource.FILE) {
				setErrorMessage(RemoteImportExportResources.IMPORT_EXPORT_ERROR_DESCRIPTION_NO_CONTAINER);
				return false;
			}
			// get the file extension
			String fileExtension = location.getFileExtension();
			// ensure that file extension is valid
			if (fileExtension == null || !fileExtension.equals(Utilities.EXPORT_DESCRIPTION_EXTENSION)) {
				setErrorMessage(NLS.bind(RemoteImportExportResources.IMPORT_EXPORT_ERROR_DESCRIPTION_INVALID_EXTENSION, Utilities.EXPORT_DESCRIPTION_EXTENSION ));
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the resource for the specified path.
	 *
	 * @param path	the path for which the resource should be returned
	 * @return the resource specified by the path or <code>null</code>
	 */
	protected IResource findResource(IPath path) {
		IWorkspace workspace = SystemBasePlugin.getWorkspace();
		// validate path
		IStatus result = workspace.validatePath(path.toString(), IResource.ROOT | IResource.PROJECT | IResource.FOLDER | IResource.FILE);
		// if path valid
		if (result.isOK()) {
			// get the workspace root
			IWorkspaceRoot root = workspace.getRoot();
			// see if path exists. If it does, return the resource at the path
			if (root.exists(path)) {
				return root.findMember(path);
			}
			// see if a variant of the path exists
			else {
				// look for variant
				IResource variant = RemoteImportExportUtil.getInstance().findExistingResourceVariant(path);
				// if a variant does exist, return it
				if (variant != null) {
					return variant;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the name of a container with a location that encompasses targetDirectory.
	 * Returns null if there is no conflict.
	 *
	 * @param targetDirectory the path of the directory to check.
	 * @return the conflicting container name or <code>null</code>
	 */
	protected String getConflictingContainerNameFor(String targetDirectory) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath testPath = new Path(targetDirectory);
		if (root.getLocation().isPrefixOf(testPath)) return "workspace root"; //UniversalSystemPlugin.getString("IFSexport.rootName"); //$NON-NLS-1$
		IProject[] projects = root.getProjects();
		for (int i = 0; i < projects.length; i++) {
			IPath loc = projects[i].getLocation();
			if (loc != null && loc.isPrefixOf(testPath))
				return projects[i].getName();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.wizards.ISystemWizardPage#setInputObject(java.lang.Object)
	 */
	public void setInputObject(Object inputObject) {
		this.inputObject = inputObject;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.wizards.ISystemWizardPage#getInputObject()
	 */
	public Object getInputObject() {
		return inputObject;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.wizards.ISystemWizardPage#performFinish()
	 */
	public boolean performFinish() {
		return finish();
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.wizards.ISystemWizardPage#setHelp(java.lang.String)
	 */
	public void setHelp(String id) {
		if (parentComposite != null) SystemWidgetHelpers.setHelp(parentComposite, helpId);
		this.helpId = id;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.ui.wizards.ISystemWizardPage#getHelpContextId()
	 */
	public String getHelpContextId() {
		return helpId;
	}

	/**
	 * ISystemMessageLine method. <br>
	 * Clears the currently displayed error message and redisplayes
	 * the message which was active before the error message was set.
	 */
	public void clearErrorMessage() {
		if (msgLine != null)
			msgLine.clearErrorMessage();
		else
			super.setErrorMessage(null);
	}

	/**
	 * ISystemMessageLine method. <br>
	 * Clears the currently displayed message.
	 */
	public void clearMessage() {
		if (msgLine != null)
			msgLine.clearMessage();
		else
			super.setMessage(null);
	}

	/**
	 * ISystemMessageLine method. <br>
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public SystemMessage getSystemErrorMessage() {
		if (msgLine != null)
			return msgLine.getSystemErrorMessage();
		else
			return null;
	}

	/**
	 * ISystemMessageLine method. <br>
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(SystemMessage message) {
		if (msgLine != null) {
			if (message != null)
				msgLine.setErrorMessage(message);
			else
				msgLine.clearErrorMessage();
		} else // not configured yet
		{
			pendingErrorMessage = message;
			super.setErrorMessage(message.getLevelOneText());
		}
	}

	/**
	 * ISystemMessageLine method. <br>
	 * Convenience method to set an error message from an exception
	 */
	public void setErrorMessage(Throwable exc) {
		if (msgLine != null)
			msgLine.setErrorMessage(exc);
		else {
			String msgTxt = CommonMessages.MSG_ERROR_UNEXPECTED;
			SystemMessage msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
					ICommonMessageIds.MSG_ERROR_UNEXPECTED,
					IStatus.ERROR, msgTxt, exc);

			pendingErrorMessage = msg;
			super.setErrorMessage(msg.getLevelOneText());
		}
	}

	/**
	 * ISystemMessageLine method. <br>
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(String message) {
		if (msgLine != null) msgLine.setErrorMessage(message);
		//		super.setErrorMessage(message);
		//		if (msgLine != null)
		//		  ((SystemDialogPageMessageLine)msgLine).internalSetErrorMessage(message);
	}

	/**
	 * ISystemMessageLine method. <br>
	 * If the message line currently displays an error,
	 *  the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(SystemMessage message) {
		if (msgLine != null)
			msgLine.setMessage(message);
		else // not configured yet
		{
			pendingMessage = message;
			super.setMessage(message.getLevelOneText());
		}
	}

	/**
	 * ISystemMessageLine method. <br>
	 * Set the non-error message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(String message) {
		if (msgLine != null) msgLine.setMessage(message);
		//		super.setMessage(message);
		//		if (msgLine!=null)
		//		  ((SystemDialogPageMessageLine)msgLine).internalSetMessage(message);
	}
}
