/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * David McKnight   (IBM)        - [208951] no longer used editor registry for file type associations
 * David McKnight   (IBM)        - [203114] Usability improvements for file transfer mode prefs
 * David McKnight   (IBM)        - [210142] for accessibility need transfer mode toggle button
 * Martin Oberhuber (Wind River) - [220020][api][breaking] SystemFileTransferModeRegistry should be internal
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * David McKnight   (IBM)        - [223204] [cleanup] fix broken nls strings in files.ui and others
 * David McKnight   (IBM)        - [245260] Different user's connections on a single host are mapped to the same temp files cache
 *******************************************************************************/

package org.eclipse.rse.internal.files.ui.propertypages;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.rse.internal.files.ui.Activator;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.internal.files.ui.ISystemFileConstants;
import org.eclipse.rse.internal.subsystems.files.core.ISystemFilePreferencesConstants;
import org.eclipse.rse.internal.subsystems.files.core.model.SystemFileTransferModeMapping;
import org.eclipse.rse.internal.subsystems.files.core.model.SystemFileTransferModeRegistry;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageFile;
import org.eclipse.rse.subsystems.files.core.model.ISystemFileTransferModeMapping;
import org.eclipse.rse.ui.Mnemonics;
import org.eclipse.rse.ui.RSEUIPlugin;
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
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.dialogs.FileExtensionDialog;

/**
 * "Files" Preference page within the Remote Systems preference node.
 * This allows users to specify, per file extension, whether files are
 *  source or binary.
 */
public class UniversalPreferencePage 
       extends FieldEditorPreferencePage 
       implements IWorkbenchPreferencePage, Listener, ICellModifier
{


	
	protected Label resourceTypeLabel;
	protected Table resourceTypeTable;
	protected Button addResourceTypeButton;
	protected Button removeResourceTypeButton;
	protected Button toggleModeButton;
	
	protected Button doSuperTransferButton;
	
	protected Button defaultBinaryButton;
	protected Button defaultTextButton;
	
	protected SystemFileTransferModeRegistry modeRegistry;
	
	protected ArrayList modeMappings; 
	protected ArrayList imagesToDispose;
	
	protected Combo archiveTypeCombo;
	protected Combo defaultArchiveTypeCombo;
	
	protected Text downloadBufferSize;
	protected Text uploadBufferSize;
	
	protected Image fileImage;
	protected Image binaryFileImage;
	
	protected String[] columnProperties = { "P_ICON", "P_TYPE", "P_CONTENT" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * Constructor
	 */
	public UniversalPreferencePage() {
		super(GRID);
		setPreferenceStore(RSEUIPlugin.getDefault().getPreferenceStore());
//		setDescription(FileResources.RESID_PREF_UNIVERSAL_FILES_TITLE);
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, RSEUIPlugin.HELPPREFIX+"ufpf0000"); //$NON-NLS-1$
	}
	
	
	protected void createFieldEditors() {
		
		modeRegistry = SystemFileTransferModeRegistry.getInstance();
		
		modeMappings = new ArrayList();
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
		
		toggleModeButton = new Button(groupComponent, SWT.PUSH);
		toggleModeButton.setText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_TOGGLEBUTTON_LABEL);
	    toggleModeButton.setToolTipText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_TOGGLEBUTTON_TOOLTIP);
		toggleModeButton.addListener(SWT.Selection, this);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, toggleModeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		toggleModeButton.setLayoutData(data);
		
			
		
		Composite afterTableComposite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		afterTableComposite.setLayout(layout);
		data = new GridData();
		data.horizontalSpan = 2;
		afterTableComposite.setLayoutData(data);
		
		// default file transfer mode
		Group defaultModeGroup = new Group(afterTableComposite, SWT.SHADOW_ETCHED_IN);
		defaultModeGroup.setText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_DEFAULT_MODE_LABEL);
		defaultModeGroup.setToolTipText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_DEFAULT_MODE_TOOLTIP);
		
		layout = new GridLayout();
		layout.numColumns = 1;
		//layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		defaultModeGroup.setLayout(layout);
		data = new GridData();//GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 1;	
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
	
		
		Composite propertiesComposite = new Composite(afterTableComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		
		data = new GridData();
		data.horizontalSpan = 1;
		propertiesComposite.setLayout(layout);
		propertiesComposite.setLayoutData(data);

	
		// Add the boolean field editor for users to choose whether
		// hidden files should be displayed
		BooleanFieldEditor showHiddenEditor = new BooleanFieldEditor(
			ISystemFilePreferencesConstants.SHOWHIDDEN,
			FileResources.RESID_PREF_UNIVERSAL_SHOWHIDDEN_LABEL,
			propertiesComposite);
			
		addField(showHiddenEditor);
	
		
		// field to indicate whether or not to preserve timestamps during copy
		BooleanFieldEditor preserveTimestamps = new BooleanFieldEditor(
				ISystemFilePreferencesConstants.PRESERVETIMESTAMPS, 
				FileResources.RESID_PREF_UNIVERSAL_PRESERVE_TIMESTAMPS_LABEL,
				propertiesComposite);

		addField(preserveTimestamps);


		// field to indicate whether or not to share cached files between different connections
		// to the same remote host
		BooleanFieldEditor shareCachedFiles = new BooleanFieldEditor (
				ISystemFilePreferencesConstants.SHARECACHEDFILES,
				FileResources.RESID_PREF_UNIVERSAL_SHARE_CACHED_FILES_LABEL,
				propertiesComposite);
		
		addField(shareCachedFiles);
		
		// download and upload buffer size
		Group transferGroup = new Group(parent, SWT.NULL);
		transferGroup.setText(FileResources.RESID_FILES_PREFERENCES_BUFFER);
		
		GridLayout tlayout = new GridLayout();
		tlayout.numColumns = 4;
		transferGroup.setLayout(tlayout);
		transferGroup.setLayoutData(new GridData());//GridData.FILL_HORIZONTAL));
		
		Label downloadBufferLabel = new Label(transferGroup, SWT.NULL);
		downloadBufferLabel.setText(FileResources.RESID_FILES_PREFERENCES_DOWNLOAD_BUFFER_SIZE_LABEL);
		downloadBufferLabel.setToolTipText(FileResources.RESID_FILES_PREFERENCES_DOWNLOAD_BUFFER_SIZE_TOOLTIP);
		
		downloadBufferSize = SystemWidgetHelpers.createTextField(transferGroup, this);
		GridData tgd = new GridData();
		tgd.widthHint = 75;
		downloadBufferSize.setLayoutData(tgd);
		downloadBufferSize.setTextLimit(10);
		downloadBufferSize.setText(getDownloadBufferSize() + ""); //$NON-NLS-1$
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
		uploadBufferSize.setText(getUploadBufferSize() +""); //$NON-NLS-1$
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

		updateEnabledState();
		
        (new Mnemonics()).setOnPreferencePage(true).setMnemonics(parent);	
		
	}

	public void init(IWorkbench workbench) 
	{
	}

	/**
	 * 
	 * @deprecated moved to Activator
	 */
	public static void initDefaults(IPreferenceStore store) 
	{
		// no longer needed here - moved to Activator
	}

	
	/**
	 * Fill the resource type table
	 */
	protected void fillResourceTypeTable() {
		
		
		
		// Setup the columns (icon, type)
		TableLayout tableLayout = new TableLayout();
		resourceTypeTable.setLayout(tableLayout);
		resourceTypeTable.setHeaderVisible(true);
		resourceTypeTable.setLinesVisible(true);
		
		// cell modifier stuff so that we can change the transfer
		// mode directly from the cell
		TableViewer tableViewer = new TableViewer(resourceTypeTable);
		tableViewer.setCellModifier(this);
		
		ColumnLayoutData layoutData = new ColumnPixelData(20, false);
		tableLayout.addColumnData(layoutData);
		TableColumn tableCol = new TableColumn(resourceTypeTable, SWT.NONE, 0);
		tableCol.setResizable(false);
		tableCol.pack();
		tableCol.setWidth(20);
		tableCol.setText(""); //$NON-NLS-1$


		layoutData = new ColumnWeightData(40, true);
		tableLayout.addColumnData(layoutData);
		tableCol = new TableColumn(resourceTypeTable, SWT.NONE, 1);
		tableCol.setResizable(true);
		tableCol.setText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_TABLECOL_LABEL);
		tableCol.setToolTipText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_TABLECOL_TOOLTIP);
		tableCol.pack();
		tableCol.setWidth(100);

		
		layoutData = new ColumnWeightData(60, true);
		tableLayout.addColumnData(layoutData);
		tableCol = new TableColumn(resourceTypeTable, SWT.NONE, 2);
		tableCol.setResizable(true);
		tableCol.setText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_LABEL);
		tableCol.setToolTipText(FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_TOOLTIP);
		tableCol.pack();
		tableCol.setWidth(200);
		
		
	
		String[] contentTypes = new String[2];
		contentTypes[0] = FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_BINARY_LABEL;
		contentTypes[1] = FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_TEXT_LABEL;
		
	    CellEditor editors[] = new CellEditor[3];
	    editors[0] = null;
	    editors[1] = new TextCellEditor(resourceTypeTable);
	    editors[2] = new ComboBoxCellEditor(resourceTypeTable, contentTypes, SWT.READ_ONLY); 
	    
	    
	    tableViewer.setColumnProperties(columnProperties);
	    tableViewer.setCellEditors(editors);
		

		initControls();
	
	}
	
	private void initControls()
	{
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();

		// init mode mappings for resource type table control
		ISystemFileTransferModeMapping[] mappings =  getModeMappings();		
		for (int i = 0; i < mappings.length; i++) {
			newResourceTableItem(mappings[i], i, false);
		}		
		
		// init default file transfer controls
		int defaultFileTransferMode = getFileTransferModeDefaultPreference();
		if (defaultFileTransferMode == ISystemFilePreferencesConstants.FILETRANSFERMODE_BINARY)
		{		
			defaultBinaryButton.setSelection(true);
		}
		else if (defaultFileTransferMode == ISystemFilePreferencesConstants.FILETRANSFERMODE_TEXT)
		{		
			defaultTextButton.setSelection(true);
		}	
		
		// init super transfer controls
		String superTransferArcType = store.getString(ISystemFilePreferencesConstants.SUPERTRANSFER_ARC_TYPE);		
		archiveTypeCombo.setText(superTransferArcType);
		
		boolean doSuperTransfer = store.getBoolean(ISystemFilePreferencesConstants.DOSUPERTRANSFER);
		doSuperTransferButton.setSelection(doSuperTransfer);
		
		// buffer sizes
		int downloadSize = store.getInt(ISystemFilePreferencesConstants.DOWNLOAD_BUFFER_SIZE);		
	    downloadBufferSize.setText(downloadSize + ""); //$NON-NLS-1$
	    
	    int uploadSize = store.getInt(ISystemFilePreferencesConstants.UPLOAD_BUFFER_SIZE);	    
	    uploadBufferSize.setText(uploadSize + ""); //$NON-NLS-1$
		
	}
	
	protected ISystemFileTransferModeMapping[] getModeMappings()
	{
		// cloning the registry ones so that we can restore later	
		ISystemFileTransferModeMapping[] mappings = modeRegistry.getModeMappings();
		
		ISystemFileTransferModeMapping[] clonedMappings = new ISystemFileTransferModeMapping[mappings.length];
		for (int i = 0; i < mappings.length; i++){
			SystemFileTransferModeMapping mapping = (SystemFileTransferModeMapping)mappings[i];
			clonedMappings[i] = (ISystemFileTransferModeMapping)mapping.clone();			
		}
		return clonedMappings;
	}
	
	/**
	 * Used during reset defaults
	 */
	protected void resetResourceTypeTable()
	{
		//clear table and reload defaults
		modeRegistry.renit();		
		modeMappings.clear();
		resourceTypeTable.setRedraw(false);
		resourceTypeTable.removeAll();
				
		ISystemFileTransferModeMapping[] mappings = getModeMappings();
		for (int i = 0; i < mappings.length; i++) 
		{
		 newResourceTableItem(mappings[i], i, false);
		}
		resourceTypeTable.setRedraw(true);

		IEclipsePreferences prefs = new DefaultScope().getNode(RSEUIPlugin.getDefault().getBundle().getSymbolicName());
	
		int defaultFileTransferMode = prefs.getInt(ISystemFilePreferencesConstants.FILETRANSFERMODEDEFAULT, ISystemFilePreferencesConstants.DEFAULT_FILETRANSFERMODE);

		defaultBinaryButton.setSelection(defaultFileTransferMode == ISystemFilePreferencesConstants.FILETRANSFERMODE_BINARY);
		defaultTextButton.setSelection(defaultFileTransferMode == ISystemFilePreferencesConstants.FILETRANSFERMODE_TEXT);
		
		if (resourceTypeTable.getItemCount() > 0) 
		{
			resourceTypeTable.setSelection(0);
			resourceTypeTable.setFocus();
		}
		
		updateEnabledState();
		
		archiveTypeCombo.setEnabled(false);
	}
	
	protected void resetSuperTransferPrefs()
	{
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		String superTransferArcType = store.getDefaultString(ISystemFilePreferencesConstants.SUPERTRANSFER_ARC_TYPE);		
		archiveTypeCombo.setText(superTransferArcType);
		
		boolean doSuperTransfer = store.getDefaultBoolean(ISystemFilePreferencesConstants.DOSUPERTRANSFER);
		doSuperTransferButton.setSelection(doSuperTransfer);
	}
	
	protected void resetBufferSizePrefs()
	{
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();

		int downloadSize = store.getDefaultInt(ISystemFilePreferencesConstants.DOWNLOAD_BUFFER_SIZE);				
	    downloadBufferSize.setText(downloadSize + ""); //$NON-NLS-1$
	    
	    int uploadSize = store.getDefaultInt(ISystemFilePreferencesConstants.UPLOAD_BUFFER_SIZE);	    
	    uploadBufferSize.setText(uploadSize + ""); //$NON-NLS-1$
	}
	
	
	private Image applyBinaryDecoration(Image source) {
		ImageDescriptor binaryOverlay = Activator.getImageDescriptor("/icons/full/ovr16/binary_ovr.gif"); //$NON-NLS-1$
		DecorationOverlayIcon icon = new DecorationOverlayIcon(source, binaryOverlay, 3);
		return icon.createImage();
	}
	
	private Image getImageFor(ISystemFileTransferModeMapping mapping)
	{
		if (fileImage == null){
			fileImage = WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FILE).createImage();
		}
		if (binaryFileImage == null)
		{
			binaryFileImage = applyBinaryDecoration(fileImage);
		}
		
		// for now just always using the same image
		if (mapping.isBinary())
		{
			return binaryFileImage;
		}
		else
		{
			return fileImage;
		}
	}
	
	/**
 	 * Create a new <code>TableItem</code> to represent the resource
 	 * type editor description supplied.
 	 */
	protected TableItem newResourceTableItem(ISystemFileTransferModeMapping mapping, int index, boolean selected) {
		
		modeMappings.add(index, mapping);
		
		Image image = getImageFor(mapping);
		if (image != null)
			imagesToDispose.add(image);
	
		TableItem item = new TableItem(resourceTypeTable, SWT.NULL, index);
		item.setData(mapping);
		item.setImage(0, image);
		item.setText(1, mapping.getLabel());
		
		boolean binary = mapping.isBinary();
		item.setText(2, binary ? FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_BINARY_LABEL : FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_TEXT_LABEL);
				
		
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
		
		if ((event.widget == addResourceTypeButton) && (event.type == SWT.Selection)) {
			promptForResourceType();
		}
		else if ((event.widget == removeResourceTypeButton) && (event.type == SWT.Selection)) {
			removeSelectedResourceType();
		}
		else if ((event.widget == toggleModeButton) && (event.type == SWT.Selection)) {
			toggleSelectedResourceTypeMode();
		}
		else if ((event.widget == doSuperTransferButton) && (event.type == SWT.Selection))
		{
		    doSuperTransferButtonSelected();
		}
		updateEnabledState();
	}
	
	
	/**
	 * Prompt for file type
	 */
	public void promptForResourceType() {
		
		FileExtensionDialog dialog = new FileExtensionDialog(getControl().getShell());
		
		if (dialog.open() == Window.OK) {
			
			String name = dialog.getName();
			String extension = dialog.getExtension();
			
			// add the resource type
			addResourceType(name, extension);
		}
	}
	
	
	/**
	 * Remove the type from the table
	 */
	public void removeSelectedResourceType() {
		
		int index = resourceTypeTable.getSelectionIndex();
		
		modeMappings.remove(index);
		
		TableItem[] items = resourceTypeTable.getSelection();
		
		if (items.length > 0) {
			items[0].dispose();
		}
	}
	
	public void toggleSelectedResourceTypeMode() {
		
		TableItem item= resourceTypeTable.getSelection()[0];
		SystemFileTransferModeMapping mapping = (SystemFileTransferModeMapping)item.getData();
	
		if (mapping.isBinary()){
			mapping.setAsText();
			item.setText(2, FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_TEXT_LABEL);
			item.setImage(getImageFor(mapping));
		}
		else if (mapping.isText()){
			mapping.setAsBinary();
			item.setText(2, FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_BINARY_LABEL);
			item.setImage(getImageFor(mapping));
		}
	}
	
	/**
	 * Add a new resource type to the collection shown in the top of the page.
	 * This is typically called after the extension dialog is shown to the user.
	 */
	public void addResourceType(String newName, String newExtension) {

		if (newName == null || newName.length() < 1) {
			newName = "*"; //$NON-NLS-1$
		}
		else {
			
			int index = newName.indexOf('*');
			
			if (index > -1) {
				
				// if the name is more than one character, and it has a '*' in it
				if (!(index == 0 && newName.length() == 1)) {
// Note by DWD - this path is never taken because the dialog that gathers resource types checks for this condition.
					SystemMessageFile mf = RSEUIPlugin.getPluginMessageFile();
					Shell shell = getControl().getShell();
					String msgTxt = FileResources.MSG_ERROR_FILENAME_INVALID;
					SystemMessage message = new SimpleSystemMessage(Activator.PLUGIN_ID, 
							ISystemFileConstants.MSG_ERROR_FILENAME_INVALID,
							IStatus.ERROR, msgTxt);
					SystemMessageDialog.displayErrorMessage(shell, message);
					return;
				}
			}
		}
	
		// Find the index at which to insert the new entry.
		String newFileName = null;
		
		if (newExtension == null || newExtension.length() < 1) {
			newFileName = newName.toUpperCase();
		}
		else {
			newFileName = (newName + "." + newExtension).toUpperCase(); //$NON-NLS-1$
		}
		
		
		boolean found = false;
		int i = 0;
		SystemFileTransferModeMapping mapping = null;
		
		while (i < modeMappings.size() && !found) {
			
			mapping = (SystemFileTransferModeMapping)(modeMappings.get(i));
			
			int result = newFileName.compareTo(mapping.getLabel().toUpperCase());
			
			// if the type already exists
			if (result == 0) {
				
				MessageDialog.openInformation(getControl().getShell(),
					// TODO: Cannot use WorkbenchMessages -- it's internal
					FileResources.FileEditorPreference_existsTitle,
					// TODO: Cannot use WorkbenchMessages -- it's internal
					FileResources.FileEditorPreference_existsMessage);
				
				// select the existing mapping
				resourceTypeTable.select(i);
				return;
			}

			if (result < 0)
			{
				found = true;
			}
			else
				i++;
		}

		
		// Create the new type and insert it
		mapping = new SystemFileTransferModeMapping(newName,newExtension);	
		
		// default to default
		if (defaultBinaryButton.getSelection()) {
			mapping.setAsBinary();
		}
		else {
			mapping.setAsText();
		}
		
		newResourceTableItem(mapping, i, true);
		
		resourceTypeTable.setFocus();
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
		if (modeMappings != null)
		{
			//IFileEditorMapping[] originalMappingArray = editorRegistry.getFileEditorMappings();
			
			// first save the transfer mode registry
			Object[] array1 = modeMappings.toArray();
			SystemFileTransferModeMapping[] mappingArray1 = new SystemFileTransferModeMapping[array1.length];
			
			for (int i = 0; i < array1.length; i++) {
				mappingArray1[i] = (SystemFileTransferModeMapping)(array1[i]);
			}
				
			modeRegistry.setModeMappings(mappingArray1);
			modeRegistry.saveAssociations();					
			
			// editorRegistry.removePropertyListener(this);
			int defaultFileTransferMode = ISystemFilePreferencesConstants.FILETRANSFERMODE_BINARY;
			if (defaultBinaryButton.getSelection())
			{
				defaultFileTransferMode = ISystemFilePreferencesConstants.FILETRANSFERMODE_BINARY;
			}
			else
			{
				defaultFileTransferMode = ISystemFilePreferencesConstants.FILETRANSFERMODE_TEXT;			
			}
			setFileTransferModeDefaultPreference(defaultFileTransferMode);
			setDoSuperTransfer(doSuperTransferButton.getSelection());
			setSuperTransferTypePreference(archiveTypeCombo.getText());
			setDownloadBufferSize(downloadBufferSize.getText());
			setUploadBufferSize(uploadBufferSize.getText());
		}
		return true;
	}
	
	/**
	 * Return whether to automatically detect, use binary or text during file transfer 
	 * for unspecified file types
	 */
	public static int getFileTransferModeDefaultPreference() 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();			
		return store.getInt(ISystemFilePreferencesConstants.FILETRANSFERMODEDEFAULT);
	}
	/**
	 * Set the default file transfer mode to use for unspecified file types
	 */
	public static void setFileTransferModeDefaultPreference(int defaultMode) 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemFilePreferencesConstants.FILETRANSFERMODEDEFAULT,defaultMode);
		savePreferenceStore();
	}	
	
	/**
	 * Return whether to compress directories before transferring them over the network
	 */
	public static String getSuperTransferTypePreference() 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		return store.getString(ISystemFilePreferencesConstants.SUPERTRANSFER_ARC_TYPE);
	}
	/**
	 * Set the default as to whether or not to compress directories before remote transfer
	 */
	public static void setSuperTransferTypePreference(String type) 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemFilePreferencesConstants.SUPERTRANSFER_ARC_TYPE,type);
		savePreferenceStore();
	}	
	
	public static boolean getDoSuperTransfer() 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(ISystemFilePreferencesConstants.DOSUPERTRANSFER);
	}
	
	public static int getDownloadBufferSize()
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		int result = store.getInt(ISystemFilePreferencesConstants.DOWNLOAD_BUFFER_SIZE);
		if (result == 0)
		{
		    result = ISystemFilePreferencesConstants.DEFAULT_DOWNLOAD_BUFFER_SIZE;
		}
		return result;
	}
	
	public static int getUploadBufferSize()
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		int result = store.getInt(ISystemFilePreferencesConstants.UPLOAD_BUFFER_SIZE);
		if (result == 0)
		{
		    result = ISystemFilePreferencesConstants.DEFAULT_DOWNLOAD_BUFFER_SIZE;
		}
		return result;
	}
	
	public static void setDoSuperTransfer(boolean flag)
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemFilePreferencesConstants.DOSUPERTRANSFER,flag);
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
	        IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
	    	store.setValue(ISystemFilePreferencesConstants.DOWNLOAD_BUFFER_SIZE, size);
	    	savePreferenceStore();
	    }
	}
	
	public static void setUploadBufferSize(int size)
	{
	    if (size > 0)
	    {
	        IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
	    	store.setValue(ISystemFilePreferencesConstants.UPLOAD_BUFFER_SIZE, size);
	    	savePreferenceStore();
	    }
	}
	
	/**
	 * Save the preference store
	 */
	private static void savePreferenceStore()
	{	 
		RSEUIPlugin.getDefault().savePluginPreferences();
	}

	public boolean canModify(Object element, String property) {
		if (property.equals(columnProperties[2]))
		{
			return true;
		}
		else if (property.equals(columnProperties[1]))
		{
			return true;
		}
		return false;
	}

	public Object getValue(Object element, String property) {
		SystemFileTransferModeMapping mapping = (SystemFileTransferModeMapping)element;
		if (property.equals(columnProperties[2])){
			return mapping.isBinary() ? new Integer(0) : new Integer(1);
		}
		else if (property.equals(columnProperties[1])){
			return mapping.getLabel();		
		}
		return null;
	}

	public void modify(Object element, String property, Object value) 
	{
		TableItem item = (TableItem)element;
		SystemFileTransferModeMapping mapping = (SystemFileTransferModeMapping)item.getData();
		if (mapping != null)
		{
			if (property.equals(columnProperties[2])){
	
				if (value instanceof Integer)
				{
					int index = ((Integer)value).intValue();
					if (index == 0)
					{
						mapping.setAsBinary();
						item.setText(2, FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_BINARY_LABEL);
						item.setImage(getImageFor(mapping));
					}
					else
					{
						mapping.setAsText();
						item.setText(2, FileResources.RESID_PREF_UNIVERSAL_FILES_FILETYPES_MODE_TEXT_LABEL);
						item.setImage(getImageFor(mapping));
					}
				}
				
			}
			else if (property.equals(columnProperties[1])){
				if (value instanceof String)
				{
					String nameExtension = (String)value;
					
					int dotIndex = nameExtension.lastIndexOf('.');
					if (dotIndex != -1)
					{
						String name = nameExtension.substring(0, dotIndex);					
						
						String ext = nameExtension.substring(dotIndex + 1);

						mapping.setName(name);
						mapping.setExtension(ext);
						item.setText(1, nameExtension);
					}
				}
			}
		}
	}
	
}