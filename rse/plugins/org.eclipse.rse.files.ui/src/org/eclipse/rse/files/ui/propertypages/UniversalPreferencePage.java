/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.propertypages;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.window.Window;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageFile;
import org.eclipse.rse.subsystems.files.core.model.SystemFileTransferModeMapping;
import org.eclipse.rse.subsystems.files.core.model.SystemFileTransferModeRegistry;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.Mnemonics;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.dialogs.FileExtensionDialog;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;



/**
 * "Files" Preference page within the Remote Systems preference node.
 * This allows users to specify, per file extension, whether files are
 *  source or binary.
 */
public class UniversalPreferencePage 
       extends FieldEditorPreferencePage 
       implements IWorkbenchPreferencePage, Listener, IPropertyListener
{


	
	protected Label resourceTypeLabel;
	protected Table resourceTypeTable;
	protected Button addResourceTypeButton;
	protected Button removeResourceTypeButton;
	protected Button binaryButton;
	protected Button textButton;
	protected Button doSuperTransferButton;
	
	protected Button defaultBinaryButton;
	protected Button defaultTextButton;
	
	protected SystemFileTransferModeRegistry modeRegistry;
	protected IEditorRegistry editorRegistry;
	
	protected ArrayList modeMappings; 
	protected ArrayList editorMappings;
	protected ArrayList imagesToDispose;
	
	protected Combo archiveTypeCombo;
	protected Combo defaultArchiveTypeCombo;
	
	protected Text downloadBufferSize;
	protected Text uploadBufferSize;
	
	/**
	 * Constructor
	 */
	public UniversalPreferencePage() {
		super(GRID);
		setPreferenceStore(SystemPlugin.getDefault().getPreferenceStore());
		setDescription(FileResources.RESID_PREF_UNIVERSAL_FILES_TITLE);
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		// added for 1GEUGE6: ITPJUI:WIN2000 - Help is the same on all preference pages
		super.createControl(parent);
		
	}
	
	
	protected void createFieldEditors() {
		
		modeRegistry = (SystemFileTransferModeRegistry)(SystemFileTransferModeRegistry.getDefault());
		editorRegistry = (IEditorRegistry)(SystemPlugin.getDefault().getWorkbench().getEditorRegistry());
		
		modeMappings = new ArrayList();
		editorMappings = new ArrayList();
		imagesToDispose = new ArrayList();
		
		Composite parent = getFieldEditorParent();

		
		
		// define container and its layout
		Composite pageComponent = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		pageComponent.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		pageComponent.setLayoutData(data);
		
		// file types label
		resourceTypeLabel = new Label(pageComponent, SWT.LEFT);
		resourceTypeLabel.setText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_TYPE_LABEL);
		resourceTypeLabel.setToolTipText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_TYPE_TOOLTIP);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		resourceTypeLabel.setLayoutData(data);

		// file types table
		resourceTypeTable = new Table(pageComponent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		resourceTypeTable.addListener(SWT.Selection, this);
		resourceTypeTable.addListener(SWT.DefaultSelection, this);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = resourceTypeTable.getItemHeight() * 12;
		data.widthHint = 200;
		data.grabExcessHorizontalSpace = true;
		resourceTypeTable.setLayoutData(data);
		
		// container for buttons
		Composite groupComponent= new Composite(pageComponent, SWT.NULL);
		groupComponent.setLayout(new GridLayout());
		data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		groupComponent.setLayoutData(data);

		// add button
		addResourceTypeButton = new Button(groupComponent, SWT.PUSH);
		addResourceTypeButton.setText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_ADDBUTTON_LABEL);
		addResourceTypeButton.setToolTipText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_ADDBUTTON_TOOLTIP);
		addResourceTypeButton.addListener(SWT.Selection, this);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, addResourceTypeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		addResourceTypeButton.setLayoutData(data);

		// remove button	
		removeResourceTypeButton = new Button(groupComponent, SWT.PUSH);
		removeResourceTypeButton.setText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_REMOVEBUTTON_LABEL);
		removeResourceTypeButton.setToolTipText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_REMOVEBUTTON_TOOLTIP);
		removeResourceTypeButton.addListener(SWT.Selection, this);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, removeResourceTypeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		removeResourceTypeButton.setLayoutData(data);
		
		
		// transfer mode 
		Group modeGroup = new Group(groupComponent, SWT.SHADOW_ETCHED_IN);
		modeGroup.setText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_LABEL);
		modeGroup.setToolTipText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_TOOLTIP);
		
		layout = new GridLayout();
		layout.numColumns = 1;
		//layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		modeGroup.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;		
		data.widthHint = 100;
		data.grabExcessHorizontalSpace = true;
		modeGroup.setLayoutData(data);
		
		// add the binary radio button
		binaryButton = new Button(modeGroup, SWT.RADIO);
		binaryButton.addListener(SWT.Selection, this);
		binaryButton.setText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_BINARY_LABEL);
		binaryButton.setToolTipText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_BINARY_TOOLTIP);
				
		// add the text radio button
		textButton = new Button(modeGroup, SWT.RADIO);
		textButton.addListener(SWT.Selection, this);
		textButton.setText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_TEXT_LABEL);
		textButton.setToolTipText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_TEXT_TOOLTIP);

	
//		 default file transfer mode
		Group defaultModeGroup = new Group(groupComponent, SWT.SHADOW_ETCHED_IN);
		defaultModeGroup.setText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_DEFAULT_MODE_LABEL);
		defaultModeGroup.setToolTipText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_DEFAULT_MODE_TOOLTIP);
		
		layout = new GridLayout();
		layout.numColumns = 1;
		//layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		defaultModeGroup.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		data.widthHint = 100;		
		data.grabExcessHorizontalSpace = true;
		defaultModeGroup.setLayoutData(data);
		
		// add the binary radio button
		defaultBinaryButton = new Button(defaultModeGroup, SWT.RADIO);
		defaultBinaryButton.addListener(SWT.Selection, this);
		defaultBinaryButton.setText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_BINARY_LABEL);
		defaultBinaryButton.setToolTipText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_BINARY_TOOLTIP);		
		
		// add the text radio button
		defaultTextButton = new Button(defaultModeGroup, SWT.RADIO);
		defaultTextButton.addListener(SWT.Selection, this);
		defaultTextButton.setText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_TEXT_LABEL);
		defaultTextButton.setToolTipText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_TEXT_TOOLTIP);
	

	
		// Add the boolean field editor for users to choose whether
		// hidden files should be displayed
		BooleanFieldEditor showHiddenEditor = new BooleanFieldEditor(
			ISystemPreferencesConstants.SHOWHIDDEN,
			FileResources.RESID_PREF_UNIVERSAL_SHOWHIDDEN_LABEL,
			groupComponent);
			
		addField(showHiddenEditor);
		
		IPreferenceStore store= SystemPlugin.getDefault().getPreferenceStore();
		
		// download and upload buffer size
	
		Group transferGroup = new Group(parent, SWT.NULL);
		transferGroup.setText(FileResources.RESID_FILES_PREFERENCES_BUFFER);
		
		GridLayout tlayout = new GridLayout();
		tlayout.numColumns = 4;
		transferGroup.setLayout(tlayout);
		transferGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label downloadBufferLabel = new Label(transferGroup, SWT.NULL);
		downloadBufferLabel.setText(FileResources.RESID_FILES_PREFERENCES_DOWNLOAD_BUFFER_SIZE_LABEL);
		downloadBufferLabel.setToolTipText(FileResources.RESID_FILES_PREFERENCES_DOWNLOAD_BUFFER_SIZE_TOOLTIP);
		
		downloadBufferSize = SystemWidgetHelpers.createTextField(transferGroup, this);
		GridData tgd = new GridData();
		tgd.widthHint = 75;
		downloadBufferSize.setLayoutData(tgd);
		downloadBufferSize.setTextLimit(10);
		downloadBufferSize.setText(getDownloadBufferSize() + "");
		downloadBufferSize.addVerifyListener(new VerifyListener()
		    			{
		    				public void verifyText(VerifyEvent e)
		    				{
		    					e.doit = true;
		    					for (int loop = 0; loop < e.text.length(); loop++)
		    					{
		    						if (!Character.isDigit(e.text.charAt(loop)))
		    							e.doit = false;
		    					}
		    				}
		    			});
		
		Label uploadBufferLabel = new Label(transferGroup, SWT.NULL);
		uploadBufferLabel.setText(FileResources.RESID_FILES_PREFERENCES_UPLOAD_BUFFER_SIZE_LABEL);
		uploadBufferLabel.setToolTipText(FileResources.RESID_FILES_PREFERENCES_UPLOAD_BUFFER_SIZE_TOOLTIP);
		uploadBufferSize = SystemWidgetHelpers.createTextField(transferGroup, this);
		tgd = new GridData();
		tgd.widthHint = 75;
		uploadBufferSize.setLayoutData(tgd);
		uploadBufferSize.setTextLimit(10);
		uploadBufferSize.setText(getUploadBufferSize() +"");
		uploadBufferSize.addVerifyListener(new VerifyListener()
		    			{
		    				public void verifyText(VerifyEvent e)
		    				{
		    					e.doit = true;
		    					for (int loop = 0; loop < e.text.length(); loop++)
		    					{
		    						if (!Character.isDigit(e.text.charAt(loop)))
		    						{
		    							e.doit = false;
		    						}		    						
		    					}
		    				}
		    			});

		
		// archive transfer
		Composite archiveGroup = new Composite(parent, SWT.NULL);
		GridLayout alayout = new GridLayout();
		alayout.numColumns = 2;
		archiveGroup.setLayout(alayout);
		archiveGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		
		doSuperTransferButton = SystemWidgetHelpers.createCheckBox(archiveGroup, FileResources.RESID_SUPERTRANSFER_PREFS_ENABLE, this);
		GridData cdata = new GridData();
		cdata.horizontalSpan = 2;		
		doSuperTransferButton.setLayoutData(cdata);
		
		archiveTypeCombo = SystemWidgetHelpers.createLabeledReadonlyCombo(archiveGroup, null, FileResources.RESID_SUPERTRANSFER_PREFS_TYPE_LABEL, FileResources.RESID_SUPERTRANSFER_PREFS_TYPE_TOOLTIP);
		archiveTypeCombo.setItems(ArchiveHandlerManager.getInstance().getRegisteredExtensions());

		boolean doSuperTransfer = getDoSuperTransfer();
		doSuperTransferButton.setSelection(doSuperTransfer);
		
		String initialArchiveType = store.getString(ISystemPreferencesConstants.SUPERTRANSFER_ARC_TYPE);
		if (initialArchiveType == null ||
				!ArchiveHandlerManager.getInstance().isRegisteredArchive("test." + initialArchiveType))
		{
			initialArchiveType = ISystemPreferencesConstants.DEFAULT_SUPERTRANSFER_ARCHIVE_TYPE;
		}
		archiveTypeCombo.setText(initialArchiveType);
		archiveTypeCombo.setTextLimit(6);
		archiveTypeCombo.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) 
			{
				 setSuperTransferTypePreference(archiveTypeCombo.getText());
			}
			});
		
		archiveTypeCombo.setEnabled(doSuperTransfer);
			

	
			
		fillResourceTypeTable();
		
		if (resourceTypeTable.getItemCount() > 0) {
			resourceTypeTable.setSelection(0);
			resourceTypeTable.setFocus();
		}
		
		fillMode();
		updateEnabledState();
		
        (new Mnemonics()).setOnPreferencePage(true).setMnemonics(parent);	
		SystemWidgetHelpers.setCompositeHelp(parent, SystemPlugin.HELPPREFIX+"ufpf0000");
		
	}

	public void init(IWorkbench workbench) 
	{
	}

	public static void initDefaults(IPreferenceStore store) 
	{
		store.setDefault(ISystemPreferencesConstants.SHOWHIDDEN, false);
		store.setDefault(ISystemPreferencesConstants.DOSUPERTRANSFER, ISystemPreferencesConstants.DEFAULT_DOSUPERTRANSFER);
		store.setDefault(ISystemPreferencesConstants.SUPERTRANSFER_ARC_TYPE, ISystemPreferencesConstants.DEFAULT_SUPERTRANSFER_ARCHIVE_TYPE);
		store.setDefault(ISystemPreferencesConstants.DOWNLOAD_BUFFER_SIZE, ISystemPreferencesConstants.DEFAULT_DOWNLOAD_BUFFER_SIZE);
		store.setDefault(ISystemPreferencesConstants.UPLOAD_BUFFER_SIZE, ISystemPreferencesConstants.DEFAULT_DOWNLOAD_BUFFER_SIZE);
	}

	
	/**
	 * Fill the resource type table
	 */
	protected void fillResourceTypeTable() {
		
		// Setup the columns (icon, type)
		TableLayout tableLayout = new TableLayout();
		resourceTypeTable.setLayout(tableLayout);
		resourceTypeTable.setHeaderVisible(true);

		ColumnLayoutData layoutData = new ColumnPixelData(20, false);
		tableLayout.addColumnData(layoutData);
		TableColumn tableCol = new TableColumn(resourceTypeTable, SWT.NONE);
		tableCol.setResizable(false);
		tableCol.setText("");

		layoutData = new ColumnWeightData(40, false);
		tableLayout.addColumnData(layoutData);
		tableCol = new TableColumn(resourceTypeTable, SWT.NONE);
		tableCol.setResizable(false);
		tableCol.setText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_TABLECOL_LABEL);
		
		IFileEditorMapping[] mappingArray = editorRegistry.getFileEditorMappings();
		
		for (int i = 0; i < mappingArray.length; i++) {
			newResourceTableItem(mappingArray[i], i, false);
		}
		
		int defaultFileTransferMode = getFileTransferModeDefaultPreference();
		if (defaultFileTransferMode == ISystemPreferencesConstants.FILETRANSFERMODE_BINARY)
		{		
			defaultBinaryButton.setSelection(true);
		}
		else if (defaultFileTransferMode == ISystemPreferencesConstants.FILETRANSFERMODE_TEXT)
		{		
			defaultTextButton.setSelection(true);
		}
	}
	
	/**
	 * Used during reset defaults
	 */
	protected void resetResourceTypeTable()
	{
		//clear table and reload defaults
		editorMappings.clear();
		modeMappings.clear();
		resourceTypeTable.setRedraw(false);
		resourceTypeTable.removeAll();
		
		
		IFileEditorMapping[] mappingArray = editorRegistry.getFileEditorMappings();
		for (int i = 0; i < mappingArray.length; i++) 
		{
		 newResourceTableItem(mappingArray[i], i, false);
		}
		resourceTypeTable.setRedraw(true);

		int defaultFileTransferMode = getFileTransferModeDefaultPreference();
		defaultBinaryButton.setSelection(defaultFileTransferMode == ISystemPreferencesConstants.FILETRANSFERMODE_BINARY);
		defaultTextButton.setSelection(defaultFileTransferMode == ISystemPreferencesConstants.FILETRANSFERMODE_TEXT);
		
		if (resourceTypeTable.getItemCount() > 0) 
		{
			resourceTypeTable.setSelection(0);
			resourceTypeTable.setFocus();
		}
		
		fillMode();
		updateEnabledState();
	}
	
	protected void resetSuperTransferPrefs()
	{
		archiveTypeCombo.setText(ISystemPreferencesConstants.DEFAULT_SUPERTRANSFER_ARCHIVE_TYPE);
		setSuperTransferTypePreference(ISystemPreferencesConstants.DEFAULT_SUPERTRANSFER_ARCHIVE_TYPE);
		doSuperTransferButton.setSelection(ISystemPreferencesConstants.DEFAULT_DOSUPERTRANSFER);
		setDoSuperTransfer(ISystemPreferencesConstants.DEFAULT_DOSUPERTRANSFER);
	}
	
	protected void resetBufferSizePrefs()
	{
	    downloadBufferSize.setText(ISystemPreferencesConstants.DEFAULT_DOWNLOAD_BUFFER_SIZE + "");
	    uploadBufferSize.setText(ISystemPreferencesConstants.DEFAULT_DOWNLOAD_BUFFER_SIZE + "");
	}
	
	/**
 	 * Create a new <code>TableItem</code> to represent the resource
 	 * type editor description supplied.
 	 */
	protected TableItem newResourceTableItem(IFileEditorMapping mapping, int index, boolean selected) {
		
		editorMappings.add(index, ((FileEditorMapping)mapping).clone());
		modeMappings.add(index, modeRegistry.getMapping(mapping).clone());
		
		Image image = mapping.getImageDescriptor().createImage(false);
		
		if (image != null)
			imagesToDispose.add(image);
	
		TableItem item = new TableItem(resourceTypeTable, SWT.NULL, index);
		item.setImage(0, image);
		item.setText(1, mapping.getLabel());
		
		if (selected)
			resourceTypeTable.setSelection(index);

		return item;
	}
	
	
	/**
 	 * The preference page is going to be disposed. So deallocate all allocated
 	 * SWT resources that aren't disposed automatically by disposing the page
 	 * (i.e. fonts, cursors, etc). Subclasses should reimplement this method to 
 	 * release their own allocated SWT resources.
 	 */
	public void dispose() {
		
		super.dispose();
		
		if(imagesToDispose != null) {
			
			for (Iterator e = imagesToDispose.iterator(); e.hasNext();) {
				((Image)e.next()).dispose();
			}
			
			imagesToDispose = null;
		}
	}
	
	
	/**
	 * @see Listener#handleEvent(Event)
	 */
	public void handleEvent(Event event) {
		
		if ((event.widget == resourceTypeTable) && ((event.type == SWT.Selection) || (event.type == SWT.DefaultSelection))) {
			fillMode();
		}
		if ((event.widget == addResourceTypeButton) && (event.type == SWT.Selection)) {
			promptForResourceType();
		}
		else if ((event.widget == removeResourceTypeButton) && (event.type == SWT.Selection)) {
			removeSelectedResourceType();
		}
		else if ((event.widget == binaryButton) && (event.type == SWT.Selection)) {
			binaryButtonSelected();
		}
		else if ((event.widget == textButton) && (event.type == SWT.Selection)) {
			textButtonSelected();
		}
		else if ((event.widget == doSuperTransferButton) && (event.type == SWT.Selection))
		{
		    doSuperTransferButtonSelected();
		}
		updateEnabledState();
	}
	
	
	/**
	 * Fill the mode widgets
	 */
	public void fillMode() {
		
		int index = resourceTypeTable.getSelectionIndex();
		SystemFileTransferModeMapping modeMapping = getModeMapping(index);
		
		if (modeMapping !=null)
		{
			if (modeMapping.isBinary()) {
				binaryButton.setSelection(true);
				textButton.setSelection(false);
			}
			else {
				binaryButton.setSelection(false);
				textButton.setSelection(true);
			}
		}
	}
	
	
	/**
	 * Prompt for file type
	 */
	public void promptForResourceType() {
		
		FileExtensionDialog dialog = new FileExtensionDialog(getControl().getShell());
		
		if (dialog.open() == Window.OK) {
			
			String name = dialog.getName();
			String extension = dialog.getExtension();
			
			if (extension.length() > 0) {
				addResourceType(name, extension);
			}
		}
	}
	
	
	/**
	 * Remove the type from the table
	 */
	public void removeSelectedResourceType() {
		
		int index = resourceTypeTable.getSelectionIndex();
		
		editorMappings.remove(index);
		modeMappings.remove(index);
		
		TableItem[] items = resourceTypeTable.getSelection();
		
		if (items.length > 0) {
			items[0].dispose();
		}
	}
	
	
	/**
	 * Add a new resource type to the collection shown in the top of the page.
	 * This is typically called after the extension dialog is shown to the user.
	 */
	public void addResourceType(String newName, String newExtension) {
	
		// no extension is provided
		if (newExtension == null || newExtension.length() < 1) {
// Note by DWD - this path is never taken because the dialog that gathers resource types checks for this condition
			SystemMessageFile mf = SystemPlugin.getPluginMessageFile();
			Shell shell = getControl().getShell();
			SystemMessage message = mf.getMessage(ISystemMessages.MSG_ERROR_EXTENSION_EMPTY);
			SystemMessageDialog.displayErrorMessage(shell, message);
// Removed by DWD - 2004-07-28 - these two messages don't exist
//			MessageDialog.openInformation(getControl().getShell(),
//				GenericMessages.getString("FileEditorPreference.extensionEmptyTitle"),
//				GenericMessages.getString("FileEditorPreference.extensionEmptyMessage"));
			return;
		}

		if (newName == null || newName.length() < 1)
			newName = "*";
		else {
			
			int index = newName.indexOf('*');
			
			if (index > -1) {
				
				// if the name is more than one character, and it has a '*' in it
				if (!(index == 0 && newName.length() == 1)) {
// Note by DWD - this path is never taken because the dialog that gathers resource types checks for this condition.
					SystemMessageFile mf = SystemPlugin.getPluginMessageFile();
					Shell shell = getControl().getShell();
					SystemMessage message = mf.getMessage(ISystemMessages.MSG_ERROR_FILENAME_INVALID);
					SystemMessageDialog.displayErrorMessage(shell, message);
// removed by DWD - 2004-07-28 - these two messages don't exist
//					MessageDialog.openInformation(getControl().getShell(),
//						GenericMessages.getString("FileEditorPreference.fileNameInvalidTitle"),
//						GenericMessages.getString("FileEditorPreference.fileNameInvalidMessage"));
					return;
				}
			}
		}
	
		// Find the index at which to insert the new entry.
		String newFilename = (newName + "." + newExtension).toUpperCase();
		IFileEditorMapping resourceType;
		boolean found = false;
		int i = 0;
		
		while (i < editorMappings.size() && !found) {
			
			resourceType = (FileEditorMapping)(editorMappings.get(i));
			
			int result = newFilename.compareTo(resourceType.getLabel().toUpperCase());
			
			// if the type already exists
			if (result == 0) {
				
				MessageDialog.openInformation(getControl().getShell(),
					// TODO: Cannot use WorkbenchMessages -- it's internal
					FileResources.FileEditorPreference_existsTitle,
					// TODO: Cannot use WorkbenchMessages -- it's internal
					FileResources.FileEditorPreference_existsMessage);
				return;
			}

			if (result < 0)
				found = true;
			else
				i++;
		}

		// Create the new type and insert it
		resourceType = new FileEditorMapping(newName, newExtension);
		newResourceTableItem(resourceType, i, true);
		resourceTypeTable.setFocus();
		fillMode();
	}
	
	
	/**
	 * Helper method to configure things when binary mode radio button
	 * is selected
	 */
	private void binaryButtonSelected() {
		//binaryButton.setSelection(true); // causes hang on linux
		//textButton.setSelection(false);
		
		int index = resourceTypeTable.getSelectionIndex();
		SystemFileTransferModeMapping modeMapping = getModeMapping(index);
		if (modeMapping != null)
			modeMapping.setAsBinary();
	}
	
	
	/**
	 * Helper method to configure things when text mode is selected
	 */
	private void textButtonSelected() {
	//	textButton.setSelection(true); // causes hang on linux
	//	binaryButton.setSelection(false);
		
		int index = resourceTypeTable.getSelectionIndex();
		SystemFileTransferModeMapping modeMapping = getModeMapping(index);
		if (modeMapping != null)
			modeMapping.setAsText();
	}		
	
	private void doSuperTransferButtonSelected()
	{
	    if (doSuperTransferButton.getSelection())
	    {
	       archiveTypeCombo.setEnabled(true);
	    }
	    else
	    {
	        archiveTypeCombo.setEnabled(false);
	    }
	}
	
	/**
	 * Gets the mode mapping given the editor mapping selected
	 */
	private SystemFileTransferModeMapping getModeMapping(int index) {
		
		if (index >=0 && index < modeMappings.size())
			return (SystemFileTransferModeMapping)(modeMappings.get(index));
		else
			return null;
	}
	
	
	/**
	 * Update enabled state of buttons
	 */
	public void updateEnabledState() {
		
		boolean resourceTypeSelected = resourceTypeTable.getSelectionIndex() != -1;
		removeResourceTypeButton.setEnabled(resourceTypeSelected);
	}
	
	
	protected void performDefaults() 
	{
		super.performDefaults();	
		resetResourceTypeTable();

		resetSuperTransferPrefs();
		resetBufferSizePrefs();
	}
	
	/**
	 * Stuff to do when ok is pressed
	 */
	public boolean performOk() {
		
		super.performOk();
		
		// first save the transfer mode registry
		Object[] array1 = modeMappings.toArray();
		SystemFileTransferModeMapping[] mappingArray1 = new SystemFileTransferModeMapping[array1.length];
		
		for (int i = 0; i < array1.length; i++) {
			mappingArray1[i] = (SystemFileTransferModeMapping)(array1[i]);
		}
			
		modeRegistry.setModeMappings(mappingArray1);
		modeRegistry.saveAssociations();
		
		// then save the editor registry
		Object[] array2 = editorMappings.toArray();
		FileEditorMapping[] mappingArray2 = new FileEditorMapping[array2.length];
		
		for (int j = 0; j < array2.length; j++) {
			mappingArray2[j] = (FileEditorMapping)(array2[j]);
		}
		
		((EditorRegistry)editorRegistry).setFileEditorMappings(mappingArray2);
		((EditorRegistry)editorRegistry).saveAssociations();
		
		// editorRegistry.removePropertyListener(this);
		int defaultFileTransferMode = ISystemPreferencesConstants.FILETRANSFERMODE_BINARY;
		if (defaultBinaryButton.getSelection())
		{
			defaultFileTransferMode = ISystemPreferencesConstants.FILETRANSFERMODE_BINARY;
		}
		else
		{
			defaultFileTransferMode = ISystemPreferencesConstants.FILETRANSFERMODE_TEXT;			
		}
		setFileTransferModeDefaultPreference(defaultFileTransferMode);
		setDoSuperTransfer(doSuperTransferButton.getSelection());
		setSuperTransferTypePreference(archiveTypeCombo.getText());
		setDownloadBufferSize(downloadBufferSize.getText());
		setUploadBufferSize(uploadBufferSize.getText());

		return true;
	}
	
	/**
	 * Return whether to automatically detect, use binary or text during file transfer 
	 * for unspecified file types
	 */
	public static int getFileTransferModeDefaultPreference() 
	{
		IPreferenceStore store= SystemPlugin.getDefault().getPreferenceStore();
		return store.getInt(ISystemPreferencesConstants.FILETRANSFERMODEDEFAULT);
	}
	/**
	 * Set the default file transfer mode to use for unspecified file types
	 */
	public static void setFileTransferModeDefaultPreference(int defaultMode) 
	{
		IPreferenceStore store= SystemPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemPreferencesConstants.FILETRANSFERMODEDEFAULT,defaultMode);
		savePreferenceStore();
	}	
	
	/**
	 * Return whether to compress directories before transferring them over the network
	 */
	public static String getSuperTransferTypePreference() 
	{
		IPreferenceStore store= SystemPlugin.getDefault().getPreferenceStore();
		return store.getString(ISystemPreferencesConstants.SUPERTRANSFER_ARC_TYPE);
	}
	/**
	 * Set the default as to whether or not to compress directories before remote transfer
	 */
	public static void setSuperTransferTypePreference(String type) 
	{
		IPreferenceStore store= SystemPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemPreferencesConstants.SUPERTRANSFER_ARC_TYPE,type);
		savePreferenceStore();
	}	
	
	public static boolean getDoSuperTransfer() 
	{
		IPreferenceStore store= SystemPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(ISystemPreferencesConstants.DOSUPERTRANSFER);
	}
	
	public static int getDownloadBufferSize()
	{
		IPreferenceStore store= SystemPlugin.getDefault().getPreferenceStore();
		int result = store.getInt(ISystemPreferencesConstants.DOWNLOAD_BUFFER_SIZE);
		if (result == 0)
		{
		    result = ISystemPreferencesConstants.DEFAULT_DOWNLOAD_BUFFER_SIZE;
		}
		return result;
	}
	
	public static int getUploadBufferSize()
	{
		IPreferenceStore store= SystemPlugin.getDefault().getPreferenceStore();
		int result = store.getInt(ISystemPreferencesConstants.UPLOAD_BUFFER_SIZE);
		if (result == 0)
		{
		    result = ISystemPreferencesConstants.DEFAULT_DOWNLOAD_BUFFER_SIZE;
		}
		return result;
	}
	
	public static void setDoSuperTransfer(boolean flag)
	{
		IPreferenceStore store= SystemPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemPreferencesConstants.DOSUPERTRANSFER,flag);
		savePreferenceStore();
	}
	
	public static void setDownloadBufferSize(String size)
	{
	   setDownloadBufferSize(Integer.parseInt(size));
	}
	
	public static void setUploadBufferSize(String size)
	{
	   setUploadBufferSize(Integer.parseInt(size));
	}
	
	public static void setDownloadBufferSize(int size)
	{
	    if (size > 0)
	    {
	        IPreferenceStore store = SystemPlugin.getDefault().getPreferenceStore();
	    	store.setValue(ISystemPreferencesConstants.DOWNLOAD_BUFFER_SIZE, size);
	    	savePreferenceStore();
	    }
	}
	
	public static void setUploadBufferSize(int size)
	{
	    if (size > 0)
	    {
	        IPreferenceStore store = SystemPlugin.getDefault().getPreferenceStore();
	    	store.setValue(ISystemPreferencesConstants.UPLOAD_BUFFER_SIZE, size);
	    	savePreferenceStore();
	    }
	}
	
	/**
	 * Save the preference store
	 */
	private static void savePreferenceStore()
	{	 
		/* DY:  This was causing ClassCastException in 2.0
		 *      getPreferenceStore retutrns CompatibilityPreferenceStore now
		PreferenceStore store = (PreferenceStore)SystemPlugin.getDefault().getPreferenceStore();				
		try {
			store.save();
		} catch (Exception exc)
		{
			System.out.println("Error saving preferences: " + exc.getMessage() + ": " + exc.getClass().getName());
		}
		*/
		// ok, a couple hours of research leads me to believe this is now the new
		// thing to do... phil
		SystemPlugin.getDefault().savePluginPreferences();
	}
	
	/**
	 * Listen for changes to the Editor Registry content.
	 * Update our registry by changing the hashmap and saving the new
	 * mappings on disk.
	 * @see IPropertyListener#propertyChanged(Object, int)
	 */
	public void propertyChanged(Object source, int propId) {
	
		if ((source instanceof IEditorRegistry) && (propId == IEditorRegistry.PROP_CONTENTS)) {
			
			// the OK button was pressed, and we need to incorporate changes from the File Editors preference page
			IEditorRegistry registry = (IEditorRegistry)source;
			
			IFileEditorMapping[] editorMappingArray = registry.getFileEditorMappings();
		
			for (int i = 0; i < editorMappingArray.length; i++) {
			}
		}
	}
}