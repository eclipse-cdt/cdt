/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [174945] split importexport icons from rse.ui
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [216252] MessageFormat.format -> NLS.bind
 * David McKnight   (IBM)        - [219792] use background query when doing import
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * David McKnight   (IBM)        - [219792][importexport][ftp] RSE hangs on FTP import
 * Takuya Miyamoto - [185925] Integrate Platform/Team Synchronization
 * David McKnight   (IBM)        - [272708] [import/export] fix various bugs with the synchronization support
 * David McKnight   (IBM)        - [276535] File Conflict when Importing Remote Folder with Case-Differentiated-Only Filenames into Project
 * David McKnight   (IBM)        - [191558] [importexport][efs] Import to Project doesn't work with remote EFS projects
 * David McKnight   (IBM)        - [368465] Import Files -RSE - Cyclic Symbolic Reference problem
 * David McKnight   (IBM)        - [417033] [import/export] RSE import wizard won't let user to select new source
 *******************************************************************************/
package org.eclipse.rse.internal.importexport.files;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.messages.SystemMessageLine;
import org.eclipse.rse.ui.wizards.ISystemWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.dialogs.WizardResourceImportPage;
import org.eclipse.ui.internal.ide.dialogs.IElementFilter;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 *	Page 1 of the base resource import-from-file-system Wizard
 */
class RemoteImportWizardPage1 extends WizardResourceImportPage implements Listener, ISystemWizardPage {

	private class DummyProvider implements ISelectionProvider {

			public void addSelectionChangedListener(
					ISelectionChangedListener listener) {
				// TODO Auto-generated method stub

			}

			public ISelection getSelection() {
				// TODO Auto-generated method stub
				return null;
			}

			public void removeSelectionChangedListener(
					ISelectionChangedListener listener) {
				// TODO Auto-generated method stub

			}

			public void setSelection(ISelection selection) {
				// TODO Auto-generated method stub

			}
	}

	private class QueryAllJob extends Job
	{
		private Object _fileSystemObject;
		private IImportStructureProvider _provider;
		private MinimizedFileSystemElement _element;
		private List _resultsQueried;
		private volatile boolean _isActive = false;

		public QueryAllJob(Object fileSystemObject, IImportStructureProvider provider, MinimizedFileSystemElement element){
			super("Querying All"); //$NON-NLS-1$
			_fileSystemObject = fileSystemObject;
			_provider = provider;
			_element = element;
			_resultsQueried = new ArrayList();
		}


		public IStatus run(IProgressMonitor monitor){
			_isActive = true;
			Display.getDefault().asyncExec(new Runnable(){
				public void run(){
					updateWidgetEnablements();
					selectionGroup.setAllSelections(true);
				}
			});
			query(_fileSystemObject, _element, monitor);
			_isActive = false;
			
			// make sure to update enablement after query
			Display.getDefault().syncExec(new Runnable(){
				public void run(){
					updateWidgetEnablements();
				}
			});
			return Status.OK_STATUS;
		}

		public boolean isActive()
		{
			return _isActive;
		}


		private void query(Object parent, MinimizedFileSystemElement element, IProgressMonitor monitor){

			if (monitor.isCanceled()){
				return;
			}

			List children = _provider.getChildren(parent);
			if (children == null) children = new ArrayList(1);

			Iterator childrenEnum = children.iterator();

			List resultsToQuery = new ArrayList();

			while (childrenEnum.hasNext()) {
				Object child = childrenEnum.next();
				if (!_resultsQueried.contains(child)){
					_resultsQueried.add(child);
				
					String elementLabel = _provider.getLabel(child);
					//Create one level below
					MinimizedFileSystemElement result = new MinimizedFileSystemElement(elementLabel, element, _provider.isFolder(child));
					result.setFileSystemObject(child);
	
					if (child instanceof UniFilePlus){
						if (((UniFilePlus)child).isDirectory()){
							resultsToQuery.add(result);
						}
					}
				}
			}

			// only with first level query do this to asynchronously update the table view
			if (element == _element){
				Display.getDefault().asyncExec(new Runnable(){
					public void run(){
						DummyProvider provider = new DummyProvider();

						ISelection sel1 = new StructuredSelection(_element.getParent());
						SelectionChangedEvent evt1 = new SelectionChangedEvent(provider, sel1);
						selectionGroup.selectionChanged(evt1);

						ISelection sel2 = new StructuredSelection(_element);
						SelectionChangedEvent evt2 = new SelectionChangedEvent(provider, sel2);
						selectionGroup.selectionChanged(evt2);
					}
				});
			}

			for (int i = 0; i < resultsToQuery.size(); i++) {
				MinimizedFileSystemElement celement = (MinimizedFileSystemElement)resultsToQuery.get(i);
				query(celement.getFileSystemObject(), celement, monitor);
				celement.setPopulated(true);
			}

			element.setPopulated(true);
		}

	}



	private Object sourceDirectory = null;
	private String helpId;
	private Composite parentComposite;
	private SystemMessageLine msgLine;
	private SystemMessage pendingMessage, pendingErrorMessage;
	private String pendingString, pendingErrorString;
	protected Composite sourceComposite;
	protected Combo sourceNameField;
	protected Button reviewSynchronizeCheckbox;
	protected Button overwriteExistingResourcesCheckbox;
	protected Button createContainerStructureButton;
	protected Button createOnlySelectedButton;
	protected Button saveSettingsButton;
	protected Label descFilePathLabel;
	protected Text descFilePathField;
	protected Button descFileBrowseButton;
	protected Button sourceBrowseButton;
	protected Button selectTypesButton;
	protected Button selectAllButton;
	protected Button deselectAllButton;
	// a boolean to indicate if the user has typed anything
	private boolean entryChanged = false;

	private QueryAllJob _queryAllJob;
	private MinimizedFileSystemElement _fileSystemTree;

	// input object
	protected Object inputObject = null;
	// flag to indicate whether initial selection was used to set source field
	protected boolean initSourceNameSet = false;
	// dialog store id constants
	private final static String STORE_SOURCE_NAMES_ID = "RemoteImportWizardPage1.STORE_SOURCE_NAMES_ID"; //$NON-NLS-1$
	private final static String STORE_REVIEW_SYNCHRONIZE_ID = "RemoteImportWizardPage1.STORE_REVIEW_SYNCHRONIZE_ID"; //$NON-NLS-1$
	private final static String STORE_OVERWRITE_EXISTING_RESOURCES_ID = "RemoteImportWizardPage1.STORE_OVERWRITE_EXISTING_RESOURCES_ID"; //$NON-NLS-1$
	private final static String STORE_CREATE_CONTAINER_STRUCTURE_ID = "RemoteImportWizardPage1.STORE_CREATE_CONTAINER_STRUCTURE_ID"; //$NON-NLS-1$
	private static final String STORE_CREATE_DESCRIPTION_FILE_ID = "RemoteImportWizardPage1.STORE_CREATE_DESCRIPTION_FILE_ID"; //$NON-NLS-1$
	private static final String STORE_DESCRIPTION_FILE_NAME_ID = "RemoteImportWizardPage1.STORE_DESCRIPTION_FILE_NAME_ID"; //$NON-NLS-1$
	// messages
	protected static final SystemMessage SOURCE_EMPTY_MESSAGE = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
			IRemoteImportExportConstants.FILEMSG_SOURCE_EMPTY,
			IStatus.ERROR,
			RemoteImportExportResources.FILEMSG_SOURCE_EMPTY,
			RemoteImportExportResources.FILEMSG_SOURCE_EMPTY_DETAILS);


	/**
	 *	Creates an instance of this class
	 */
	protected RemoteImportWizardPage1(String name, IWorkbench aWorkbench, IStructuredSelection selection) {
		super(name, selection);
		setInputObject(selection);
	}

	/**
	 *	Creates an instance of this class
	 *
	 * @param aWorkbench IWorkbench
	 * @param selection IStructuredSelection
	 */
	public RemoteImportWizardPage1(IWorkbench aWorkbench, IStructuredSelection selection) {
		this("fileSystemImportPage1", aWorkbench, selection); //$NON-NLS-1$
		setTitle(SystemImportExportResources.RESID_FILEIMPORT_PAGE1_TITLE);
		setDescription(SystemImportExportResources.RESID_FILEIMPORT_PAGE1_DESCRIPTION);
	}

	/**
	 * Creates a new button with the given id.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method
	 * creates a standard push button, registers for selection events
	 * including button presses and registers
	 * default buttons with its shell.
	 * The button id is stored as the buttons client data.
	 * Note that the parent's layout is assumed to be a GridLayout and
	 * the number of columns in this layout is incremented.
	 * Subclasses may override.
	 * </p>
	 *
	 * @param parent the parent composite
	 * @param id the id of the button (see
	 *  <code>IDialogConstants.*_ID</code> constants
	 *  for standard dialog button ids)
	 * @param label the label from the button
	 * @param defaultButton <code>true</code> if the button is to be the
	 *   default button, and <code>false</code> otherwise
	 */
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.PUSH);
		GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
		button.setLayoutData(buttonData);
		button.setData(new Integer(id));
		button.setText(label);
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
			button.setFocus();
		}
		return button;
	}

	/**
	 * Creates the buttons for selecting specific types or selecting all or none of the
	 * elements.
	 *
	 * @param parent the parent control
	 */
	protected final void createButtonsGroup(Composite parent) {
		Composite buttonComposite = SystemWidgetHelpers.createComposite(parent, 3);
		((GridLayout) buttonComposite.getLayout()).makeColumnsEqualWidth = true;
		selectTypesButton = SystemWidgetHelpers.createPushButton(buttonComposite, null, SystemImportExportResources.RESID_FILEIMPEXP_BUTTON_SELECTTYPES_LABEL,
				SystemImportExportResources.RESID_FILEIMPEXP_BUTTON_SELECTTYPES_TOOLTIP);
		selectAllButton = SystemWidgetHelpers.createPushButton(buttonComposite, null, SystemImportExportResources.RESID_FILEIMPEXP_BUTTON_SELECTALL_LABEL,
				SystemImportExportResources.RESID_FILEIMPEXP_BUTTON_SELECTALL_TOOLTIP);
		deselectAllButton = SystemWidgetHelpers.createPushButton(buttonComposite, null, SystemImportExportResources.RESID_FILEIMPEXP_BUTTON_DESELECTALL_LABEL,
				SystemImportExportResources.RESID_FILEIMPEXP_BUTTON_DESELECTALL_TOOLTIP);
		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleTypesEditButtonPressed();
			}
		};
		selectTypesButton.addSelectionListener(listener);
		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setAllSelections(true);
			}
		};
		selectAllButton.addSelectionListener(listener);
		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setAllSelections(false);
			}
		};
		deselectAllButton.addSelectionListener(listener);
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
		if (pendingMessage != null) {
			setMessage(pendingMessage);
		}
		if (pendingErrorMessage != null) {
			setErrorMessage(pendingErrorMessage);
		}
		if (pendingString != null) {
			setMessage(pendingString);
		}
		if (pendingErrorString != null) {
			setErrorMessage(pendingErrorString);
		}
		validateSourceGroup();
		// if source not set from remote file selection, set focus to source name field
		if (!initSourceNameSet) {
			sourceNameField.setFocus();
		}
		// otherwise, set focus to selection group
		else {
			selectionGroup.setFocus();
		}
		SystemWidgetHelpers.setWizardPageMnemonics(parentComposite);
		if (helpId != null) {
			SystemWidgetHelpers.setHelp(parentComposite, helpId);
		} else {
			SystemWidgetHelpers.setHelp(parentComposite, RemoteImportExportPlugin.HELPPREFIX + "import_context"); //$NON-NLS-1$
		}
		setControl(parentComposite);
		//		SystemWidgetHelpers.setHelp(getControl(), RemoteImportExportPlugin.HELPPREFIX + "import_context");
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
	 *	Create the import options specification widgets.
	 */
	protected void createOptionsGroupButtons(Group optionsGroup) {
		reviewSynchronizeCheckbox = SystemWidgetHelpers.createCheckBox(optionsGroup, 1, null, SystemImportExportResources.RESID_FILEIMPORT_REVIEW_LABEL,
				SystemImportExportResources.RESID_FILEIMPORT_REVIEW_TOOLTIP);
		reviewSynchronizeCheckbox.addListener(SWT.Selection, this);
		overwriteExistingResourcesCheckbox = SystemWidgetHelpers.createCheckBox(optionsGroup, 1, null, SystemImportExportResources.RESID_FILEIMPORT_OPTION_OVERWRITE_LABEL,
				SystemImportExportResources.RESID_FILEIMPORT_OPTION_OVERWRITE_TOOLTIP);
		createContainerStructureButton = SystemWidgetHelpers.createRadioButton(optionsGroup, null, SystemImportExportResources.RESID_FILEIMPORT_OPTION_CREATEALL_LABEL,
				SystemImportExportResources.RESID_FILEIMPORT_OPTION_CREATEALL_TOOLTIP);
		createOnlySelectedButton = SystemWidgetHelpers.createRadioButton(optionsGroup, null, SystemImportExportResources.RESID_FILEIMPORT_OPTION_CREATESEL_LABEL,
				SystemImportExportResources.RESID_FILEIMPORT_OPTION_CREATESEL_TOOLTIP);
		createOnlySelectedButton.setSelection(true);
		Composite comp = SystemWidgetHelpers.createComposite(optionsGroup, 3);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginWidth = 0;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		saveSettingsButton = SystemWidgetHelpers.createCheckBox(comp, 3, null, SystemImportExportResources.RESID_FILEIMPORT_OPTION_SETTINGS_LABEL,
				SystemImportExportResources.RESID_FILEIMPORT_OPTION_SETTINGS_TOOLTIP);
		saveSettingsButton.addListener(SWT.Selection, this);
		descFilePathLabel = new Label(comp, SWT.NONE);
		descFilePathLabel.setText(SystemImportExportResources.RESID_FILEIMPORT_OPTION_SETTINGS_DESCFILE_LABEL);
		GridData data = new GridData();
		descFilePathLabel.setLayoutData(data);
		descFilePathField = new Text(comp, SWT.SINGLE | SWT.BORDER);
		descFilePathField.setToolTipText(SystemImportExportResources.RESID_FILEIMPORT_OPTION_SETTINGS_DESCFILE_PATH_TOOLTIP);
		data = new GridData();
		data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_FILL;
		data.grabExcessHorizontalSpace = true;
		data.widthHint = convertWidthInCharsToPixels(60);
		descFilePathField.setLayoutData(data);
		descFilePathField.addListener(SWT.Modify, this);
		descFileBrowseButton = SystemWidgetHelpers.createPushButton(comp, null, SystemImportExportResources.RESID_FILEIMPORT_OPTION_SETTINGS_DESCFILE_BROWSE_LABEL,
				SystemImportExportResources.RESID_FILEIMPORT_OPTION_SETTINGS_DESCFILE_BROWSE_TOOLTIP);
		descFileBrowseButton.addListener(SWT.Selection, this);
	}

	/**
	 *	Create the group for creating the root directory
	 */
	protected void createRootDirectoryGroup(Composite parent) {
		Composite sourceContainerGroup = SystemWidgetHelpers.createComposite(parent, 3);
		((GridData) sourceContainerGroup.getLayoutData()).grabExcessHorizontalSpace = true;
		sourceNameField = SystemWidgetHelpers.createLabeledReadonlyCombo(sourceContainerGroup, null, SystemImportExportResources.RESID_FILEIMPORT_SOURCE_LABEL,
				SystemImportExportResources.RESID_FILEIMPORT_SOURCE_TOOLTIP);
		((GridData) sourceNameField.getLayoutData()).widthHint = SIZING_TEXT_FIELD_WIDTH;
		((GridData) sourceNameField.getLayoutData()).grabExcessHorizontalSpace = true;
		sourceNameField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateFromSourceField();
			}
		});
		sourceNameField.addKeyListener(new KeyListener() {
			/*
			 * @see KeyListener.keyPressed
			 */
			public void keyPressed(KeyEvent e) {
				//If there has been a key pressed then mark as dirty
				entryChanged = true;
			}

			/*
			 * @see KeyListener.keyReleased
			 */
			public void keyReleased(KeyEvent e) {
			}
		});
		sourceNameField.addFocusListener(new FocusListener() {
			/*
			 * @see FocusListener.focusGained(FocusEvent)
			 */
			public void focusGained(FocusEvent e) {
				//Do nothing when getting focus
			}

			/*
			 * @see FocusListener.focusLost(FocusEvent)
			 */
			public void focusLost(FocusEvent e) {
				//Clear the flag to prevent constant update
				if (entryChanged) {
					entryChanged = false;
					updateFromSourceField();
				}
			}
		});
		// source browse button
		sourceBrowseButton = SystemWidgetHelpers.createPushButton(sourceContainerGroup, null, SystemImportExportResources.RESID_FILEEXPORT_DESTINATION_BROWSE_LABEL,
				SystemImportExportResources.RESID_FILEEXPORT_DESTINATION_BROWSE_TOOLTIP);
		((GridData) sourceBrowseButton.getLayoutData()).grabExcessHorizontalSpace = false;
		sourceBrowseButton.addListener(SWT.Selection, this);
	}

	/**
	 * Update the receiver from the source name field.
	 */
	private void updateFromSourceField() {
		setSourceName(sourceNameField.getText());
		//Update enablements when this is selected
		updateWidgetEnablements();
	}

	/**
	 * Creates and returns a <code>FileSystemElement</code> if the specified
	 * file system object merits one.  The criteria for this are:
	 * Also create the children.
	 */
	protected MinimizedFileSystemElement createRootElement(Object fileSystemObject, IImportStructureProvider provider) {
		boolean isContainer = provider.isFolder(fileSystemObject);
		String elementLabel = provider.getLabel(fileSystemObject);

		// Use an empty label so that display of the element's full name
		// doesn't include a confusing label
		MinimizedFileSystemElement dummyParent = new MinimizedFileSystemElement("", null, true); //$NON-NLS-1$
		MinimizedFileSystemElement result = new MinimizedFileSystemElement(elementLabel, dummyParent, isContainer);
		result.setFileSystemObject(fileSystemObject);

		if (_queryAllJob == null){
			_queryAllJob = new QueryAllJob(fileSystemObject, provider, result);
			_queryAllJob.schedule();
		}

		////Get the files for the element so as to build the first level
		//result.getFiles(provider);

		return dummyParent;
	}


	/**
	 *	Create the import source specification widgets
	 */
	protected void createSourceGroup(Composite parent) {
		sourceComposite = parent;
		createRootDirectoryGroup(parent);
		createFileSelectionGroup(parent);
		createButtonsGroup(parent);
	}

	/**
	 * Enable or disable the button group.
	 */
	protected void enableButtonGroup(boolean enable) {
		selectTypesButton.setEnabled(enable);
		selectAllButton.setEnabled(enable);
		deselectAllButton.setEnabled(enable);
	}

	/**
	 *	Answer a boolean indicating whether the specified source currently exists
	 *	and is valid
	 */
	protected boolean ensureSourceIsValid() {
		if (((File) sourceDirectory).isDirectory()) return true;
		String msgTxt = RemoteImportExportResources.FILEMSG_FOLDER_IS_FILE;
		String msgDetails = NLS.bind(RemoteImportExportResources.FILEMSG_FOLDER_IS_FILE_DETAILS, ((File)sourceDirectory).getAbsolutePath());

		SystemMessage msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
				IRemoteImportExportConstants.FILEMSG_FOLDER_IS_FILE,
				IStatus.ERROR, msgTxt, msgDetails);
		setErrorMessage(msg);
		sourceNameField.setFocus();
		return false;
	}

	/**
	 *	Execute the passed import operation.  Answer a boolean indicating success.
	 */
	protected boolean executeImportOperation(RemoteFileImportOperation op) {
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			displayErrorDialog(e.getTargetException());
			return false;
		}
		IStatus status = op.getStatus();
		if (!status.isOK()) {
			if (status.isMultiStatus()){
				if (((MultiStatus)status).getChildren().length > 0){
					status = ((MultiStatus)status).getChildren()[0];
				}
			}			
			String msgTxt = NLS.bind(RemoteImportExportResources.MSG_IMPORT_EXPORT_UNEXPECTED_EXCEPTION, status.getMessage());

			SystemMessage msg = null;
			if (status.getException() != null){
				msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
						IRemoteImportExportConstants.FILEMSG_IMPORT_FAILED,
						IStatus.ERROR, msgTxt, status.getException());
			} else {
				msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
						IRemoteImportExportConstants.FILEMSG_IMPORT_FAILED,
						IStatus.ERROR, msgTxt);
			}

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
	 * @return boolean
	 */
	public boolean finish() {
		clearMessage();
		clearErrorMessage();
		Object file;
		IHost conn;
		String temp;
		if (!ensureSourceIsValid()) return false;
		saveWidgetValues();
		Iterator resourcesEnum = getSelectedResources().iterator();
		List fileSystemObjects = new ArrayList();
		conn = Utilities.parseForSystemConnection(sourceNameField.getText());
		while (resourcesEnum.hasNext()) {
			Object fo = ((FileSystemElement) resourcesEnum.next()).getFileSystemObject();
			temp = ((File) fo).getAbsolutePath();
			if (UniFilePlus.class.isInstance(fo))
				file = fo;
			else
				file = new UniFilePlus(Utilities.getIRemoteFile(conn, temp));
			fileSystemObjects.add(file);
		}
		if (fileSystemObjects.size() > 0) {
			RemoteFileImportData data = new RemoteFileImportData();
			data.setContainerPath(getContainerFullPath());
			data.setSource(getSourceDirectory());
			data.setElements(fileSystemObjects);
			data.setOverWriteExistingFiles(overwriteExistingResourcesCheckbox.getSelection());
			data.setCreateDirectoryStructure(createContainerStructureButton.getSelection());
			data.setCreateSelectionOnly(createOnlySelectedButton.getSelection());
			data.setSaveSettings(saveSettingsButton.getSelection());
			data.setDescriptionFilePath(getDescriptionLocation());

			if (!reviewSynchronizeCheckbox.getSelection()) {
				boolean ret = executeImportOperation(new RemoteFileImportOperation(data, FileSystemStructureProvider.INSTANCE, this));
				return ret;
			} else {
				// run synchronization
				SynchronizeData data2 = null;
				try {
					data2 = new SynchronizeData(data);
				} catch (SystemMessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (reviewSynchronizeCheckbox.getSelection()) {
					data2.setSynchronizeType(ISynchronizeOperation.SYNC_MODE_UI_REVIEW_INITIAL);
				}

				boolean ret = false;

				try {
					ret = new Synchronizer(data2).run(new SynchronizeOperation());
				} catch (Exception e) {
					e.printStackTrace();
				}

				// save description after synchronize operation
				try {
					if (data.isSaveSettings()) {
						RSESyncUtils.saveDescription(data);
					}
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return ret;
			}
		}
		String msgTxt = RemoteImportExportResources.FILEMSG_IMPORT_NONE_SELECTED;
		String msgDetails = RemoteImportExportResources.FILEMSG_IMPORT_NONE_SELECTED_DETAILS;

		SystemMessage msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
				IRemoteImportExportConstants.FILEMSG_IMPORT_NONE_SELECTED,
				IStatus.ERROR, msgTxt, msgDetails);
		setErrorMessage(msg);
		return false;
	}




	/**
	 * Returns a content provider for <code>FileSystemElement</code>s that returns
	 * only files as children.
	 */
	protected ITreeContentProvider getFileProvider() {
		//IFS: add BusyIndicator
		return new WorkbenchContentProvider() {
			boolean busy = false;

			public Object[] getChildren(Object o) {
				if (o instanceof MinimizedFileSystemElement && !busy) {
					final MinimizedFileSystemElement element = (MinimizedFileSystemElement) o;
					final Object[] oa = new Object[1];
					busy = true;
					BusyIndicator.showWhile(sourceComposite.getDisplay(), new Runnable() {
						public void run() {
							oa[0] = element.getFiles(FileSystemStructureProvider.INSTANCE).getChildren(element);
						}
					});
					busy = false;
					return (Object[]) oa[0];
				}
				return new Object[0];
			}
		};
	}

	/**
	 *	Answer the root FileSystemElement that represents the contents of
	 *	the currently-specified source.  If this FileSystemElement is not
	 *	currently defined then create and return it.
	 */
	protected MinimizedFileSystemElement getFileSystemTree() {
		File sourceDirectory = getSourceDirectory();
		if (sourceDirectory == null) return null;

		if (_fileSystemTree == null){
			_fileSystemTree = selectFiles(sourceDirectory, FileSystemStructureProvider.INSTANCE);
		}
		else {
			// update _fileSystemTree
			Object root = _fileSystemTree.getFileSystemObject();
			if (root == null){
				AdaptableList flds = _fileSystemTree.getFolders();
				if (flds != null){
					Object child = flds.getChildren()[0];
					if (child instanceof MinimizedFileSystemElement){
						Object fsObj = ((MinimizedFileSystemElement)child).getFileSystemObject();
						if (fsObj != sourceDirectory){							
							_fileSystemTree = selectFiles(sourceDirectory, FileSystemStructureProvider.INSTANCE);
						}
					}
				}
			}
		}
		return _fileSystemTree;
	}

	/**
	 * Returns a content provider for <code>FileSystemElement</code>s that returns
	 * only folders as children.
	 */
	protected ITreeContentProvider getFolderProvider() {
		//IFS: add BusyIndicator
		return new WorkbenchContentProvider() {
			boolean busy = false;

			public Object[] getChildren(Object o) {
				if (o instanceof MinimizedFileSystemElement && !busy) {
					final MinimizedFileSystemElement element = (MinimizedFileSystemElement) o;
					final Object[] oa = new Object[1];
					busy = true;
					BusyIndicator.showWhile(sourceComposite.getDisplay(), new Runnable() {
						public void run() {
							oa[0] = element.getFolders(FileSystemStructureProvider.INSTANCE).getChildren(element);
						}
					});
					busy = false;
					return (Object[]) oa[0];
				}
				return new Object[0];
			}

			public boolean hasChildren(Object o) {
				if (o instanceof MinimizedFileSystemElement) {
					MinimizedFileSystemElement element = (MinimizedFileSystemElement) o;
					if (!element.isPopulated()){
						return true;
					} else {
						return getChildren(element).length > 0;
					}
				}
				return false;
			}
		};
	}

	/**
	 * Returns this page's collection of currently-specified resources to be
	 * imported. This is the primary resource selection facility accessor for
	 * subclasses.
	 *
	 * Added here to allow access for inner classes.
	 *
	 * @return a collection of resources currently selected
	 * for export (element type: <code>IResource</code>)
	 */
	protected List getSelectedResources() {
		return super.getSelectedResources();
	}

	/**
	 * Returns a File object representing the currently-named source directory iff
	 * it exists as a valid directory, or <code>null</code> otherwise.
	 */
	protected File getSourceDirectory() {
		return getSourceDirectory(this.sourceNameField.getText());
	}

	/**
	 * Returns a File object representing the currently-named source directory iff
	 * it exists as a valid directory, or <code>null</code> otherwise.
	 *
	 * @param path a String not yet formatted for java.io.File compatability
	 */
	private File getSourceDirectory(String path) {
		return (File) sourceDirectory;
	}

	/**
	 *	Answer the directory name specified as being the import source.
	 *	Note that if it ends with a separator then the separator is first
	 *	removed so that java treats it as a proper directory
	 */
	private String getSourceDirectoryName() {
		return getSourceDirectoryName(this.sourceNameField.getText());
	}

	/**
	 *	Answer the directory name specified as being the import source.
	 *	Note that if it ends with a separator then the separator is first
	 *	removed so that java treats it as a proper directory
	 */
	private String getSourceDirectoryName(String sourceName) {
		IPath result = new Path(sourceName.trim());
		if (result.getDevice() != null && result.segmentCount() == 0) // something like "c:"
			result = result.addTrailingSeparator();
		else
			result = result.removeTrailingSeparator();
		//return result.toOSString();
		return result.toString();
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
		// need this check
		if (saveSettingsButton != null) {
			return saveSettingsButton.getSelection();
		} else {
			return false;
		}
	}

	/**
	 *	Handle all events and enablements for widgets in this dialog
	 *
	 * @param event Event
	 */
	public void handleEvent(Event event) {
		if (event.widget == sourceBrowseButton) {
			handleSourceBrowseButtonPressed();
		} else if (event.widget == descFileBrowseButton) {
			handleDescriptionFileBrowseButtonPressed();
		}
		super.handleEvent(event);
	}

	/**
	 *	Open an appropriate source browser so that the user can specify a source
	 *	to import from
	 */
	protected void handleSourceBrowseButtonPressed() {
		SystemSelectRemoteFolderAction action = new SystemSelectRemoteFolderAction(this.getShell());
		action.setShowNewConnectionPrompt(true);
		action.setShowPropertySheet(true, false);
		action.setFoldersOnly(true);
		action.run();
		IRemoteFile folder = action.getSelectedFolder();
		if (folder != null) {
			clearErrorMessage();
			setSourceName(Utilities.getAsString(folder));
			selectionGroup.setFocus();
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
			path = path.removeFileExtension().addFileExtension(Utilities.IMPORT_DESCRIPTION_EXTENSION);
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
	 * Open a registered type selection dialog and note the selections
	 * in the receivers types-to-export field.,
	 * Added here so that inner classes can have access
	 */
	protected void handleTypesEditButtonPressed() {
		super.handleTypesEditButtonPressed();
	}

	/**
	 * Returns whether the extension provided is an extension that
	 * has been specified for import by the user.
	 * @param extension the resource extension.
	 * @return <code>true</code> if the resource name is suitable for import based upon its extension.
	 */
	protected boolean isImportableExtension(String extension) {
		// i.e. - all extensions are acceptable
		if (selectedTypes == null) {
			return true;
		}
		Iterator z = selectedTypes.iterator();
		while (z.hasNext()) {
			if (extension.equalsIgnoreCase((String) (z.next()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Is the element included in import data.
	 * @param element the file element.
	 * @return <code>true</code> if the element is included in the import data, <code>false</code> if not.
	 */
	protected boolean isIncludedInImportData(MinimizedFileSystemElement element) {
		UniFilePlus file = (UniFilePlus) (element.getFileSystemObject());
		RemoteImportWizard parentWizard = (RemoteImportWizard) getWizard();
		RemoteFileImportData importData = parentWizard.getImportData();
		return importData.doesExist(file);
	}

	/**
	 *	Repopulate the view based on the currently entered directory.
	 */
	protected void resetSelection() {
		_queryAllJob = null; // a new one will be created
		MinimizedFileSystemElement currentRoot = getFileSystemTree();
		this.selectionGroup.setRoot(currentRoot);
	}

	/**
	 *	Use the dialog store to restore widget values to the values that they held
	 *	last time this wizard was used to completion
	 */
	protected void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] sourceNames = settings.getArray(STORE_SOURCE_NAMES_ID);
			if (sourceNames != null) {
				// set filenames history
				for (int i = 0; i < sourceNames.length; i++) {
					sourceNameField.add(sourceNames[i]);
				}
			}
			RemoteImportWizard parentWizard = (RemoteImportWizard) getWizard();
			boolean isInitializingFromImportData = parentWizard.getInitializeFromImportData();
			if (!isInitializingFromImportData) {
				// radio buttons and checkboxes
				reviewSynchronizeCheckbox.setSelection(settings.getBoolean(STORE_REVIEW_SYNCHRONIZE_ID));
				overwriteExistingResourcesCheckbox.setSelection(settings.getBoolean(STORE_OVERWRITE_EXISTING_RESOURCES_ID));
				boolean createStructure = settings.getBoolean(STORE_CREATE_CONTAINER_STRUCTURE_ID);
				createContainerStructureButton.setSelection(createStructure);
				createOnlySelectedButton.setSelection(!createStructure);
				boolean saveSettings = settings.getBoolean(STORE_CREATE_DESCRIPTION_FILE_ID);
				saveSettingsButton.setSelection(saveSettings);
				String descFilePathStr = settings.get(STORE_DESCRIPTION_FILE_NAME_ID);
				if (descFilePathStr == null) {
					descFilePathStr = ""; //$NON-NLS-1$
				}
				descFilePathField.setText(descFilePathStr);
			} else {
				RemoteFileImportData importData = parentWizard.getImportData();
				// container path
				String containerPath = importData.getContainerPath().toString();
				if (containerPath != null) {
					setContainerFieldValue(containerPath);
				}
				// radio buttons and checkboxes
				reviewSynchronizeCheckbox.setSelection(importData.isReviewSynchronize());
				overwriteExistingResourcesCheckbox.setSelection(importData.isOverWriteExistingFiles());
				createContainerStructureButton.setSelection(importData.isCreateDirectoryStructure());
				createOnlySelectedButton.setSelection(importData.isCreateSelectionOnly());
				saveSettingsButton.setSelection(importData.isSaveSettings());
				String descFilePathStr = importData.getDescriptionFilePath();
				if (descFilePathStr == null) {
					descFilePathStr = ""; //$NON-NLS-1$
				}
				descFilePathField.setText(descFilePathStr);
				UniFilePlus source = (UniFilePlus) (importData.getSource());
				if (source != null) {
					setSourceName(source.getCanonicalPath());
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
						setSourceName(Utilities.getAsString(file));
						// indicate source name set initially from remote folder selection
						initSourceNameSet = true;
					}
				}
			}
		}
	}

	/**
	 * 	Since Finish was pressed, write widget values to the dialog store so that they
	 *	will persist into the next invocation of this wizard page
	 */
	protected void saveWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			// update source names history
			String[] sourceNames = settings.getArray(STORE_SOURCE_NAMES_ID);
			if (sourceNames == null) sourceNames = new String[0];
			sourceNames = addToHistory(sourceNames, getSourceDirectoryName());
			settings.put(STORE_SOURCE_NAMES_ID, sourceNames);
			// radio buttons and checkboxes
			settings.put(STORE_REVIEW_SYNCHRONIZE_ID, reviewSynchronizeCheckbox.getSelection());
			settings.put(STORE_OVERWRITE_EXISTING_RESOURCES_ID, overwriteExistingResourcesCheckbox.getSelection());
			settings.put(STORE_CREATE_CONTAINER_STRUCTURE_ID, createContainerStructureButton.getSelection());
			settings.put(STORE_CREATE_DESCRIPTION_FILE_ID, isSaveSettings());
			settings.put(STORE_DESCRIPTION_FILE_NAME_ID, getDescriptionLocation());
		}
	}

	/**
	 * Invokes a file selection operation using the specified file system and
	 * structure provider.  If the user specifies files to be imported then
	 * this selection is cached for later retrieval and is returned.
	 */
	protected MinimizedFileSystemElement selectFiles(final Object rootFileSystemObject, final IImportStructureProvider structureProvider) {
		final MinimizedFileSystemElement[] results = new MinimizedFileSystemElement[1];
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				//Create the root element from the supplied file system object
				results[0] = createRootElement(rootFileSystemObject, structureProvider);
			}
		});
		return results[0];
	}

	/**
	 * Set all of the selections in the selection group to value. Implemented here
	 * to provide access for inner classes.
	 * @param value boolean
	 */
	protected void setAllSelections(boolean value) {
		super.setAllSelections(value);
	}

	/**
	 * Sets the source name of the import to be the supplied path.
	 * Adds the name of the path to the list of items in the
	 * source combo and selects it.
	 *
	 * @param path the path to be added
	 */
	protected void setSourceName(String path) {
		if (path.length() > 0) {
			// Clear selection in case this method is Excepted.
			sourceDirectory = null;
			sourceNameField.setText(""); //$NON-NLS-1$
			resetSelection();
			String[] currentItems = this.sourceNameField.getItems();
			int selectionIndex = -1;
			for (int i = 0; i < currentItems.length && selectionIndex < 0; i++) {
				if (currentItems[i].equals(path)) selectionIndex = i;
			}
			if (selectionIndex < 0) { // New one from Browse
				int oldLength = currentItems.length;
				String[] newItems = new String[oldLength + 1];
				System.arraycopy(currentItems, 0, newItems, 0, oldLength);
				newItems[oldLength] = path;
				this.sourceNameField.setItems(newItems);
				selectionIndex = oldLength;
			}
			if (Utilities.isConnectionValid(path, getShell()))
			// At time of writing no exceptions were expected from this code!
				// if one is received, issue it.
				try {
					sourceDirectory = null;
					IHost as400Conn = Utilities.parseForSystemConnection(path);
					if (as400Conn != null) {
						IRemoteFile rf = Utilities.parseForIRemoteFile(path);
						if (rf != null && rf.exists()) sourceDirectory = new UniFilePlus(rf);
						sourceNameField.select(selectionIndex);
					}
					resetSelection();
				} catch (Exception e) {
					Utilities.error(e);
				}
		}
	}

	/**
	 * Update the tree to only select those elements that match the selected types
	 */
	protected void setupSelectionsBasedOnSelectedTypes() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getContainer().getShell());
		final Map selectionMap = new Hashtable();
		final IElementFilter filter = new IElementFilter() {
			public void filterElements(Collection files, IProgressMonitor monitor) throws InterruptedException {
				if (files == null) {
					throw new InterruptedException();
				}
				Iterator filesList = files.iterator();
				while (filesList.hasNext()) {
					if (monitor.isCanceled()) throw new InterruptedException();
					checkFile(filesList.next());
				}
			}

			public void filterElements(Object[] files, IProgressMonitor monitor) throws InterruptedException {
				if (files == null) {
					throw new InterruptedException();
				}
				for (int i = 0; i < files.length; i++) {
					if (monitor.isCanceled()) throw new InterruptedException();
					checkFile(files[i]);
				}
			}

			private void checkFile(Object fileElement) {
				MinimizedFileSystemElement file = (MinimizedFileSystemElement) fileElement;
				if (isImportableExtension(file.getFileNameExtension())) {
					List elements = new ArrayList();
					FileSystemElement parent = file.getParent();
					if (selectionMap.containsKey(parent)) elements = (List) selectionMap.get(parent);
					elements.add(file);
					selectionMap.put(parent, elements);
				}
			}
		};
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor) throws InterruptedException {

				String msg = RemoteImportExportResources.FILEMSG_IMPORT_FILTERING;
				monitor.beginTask(msg, IProgressMonitor.UNKNOWN);
				getSelectedResources(filter, monitor);
			}
		};
		try {
			// have to set fork to false to avoid InvocationTargetException !!
			dialog.run(false, true, runnable);
		} catch (InvocationTargetException exception) {
			//Couldn't start. Do nothing.
			return;
		} catch (InterruptedException exception) {
			//Got interrupted. Do nothing.
			return;
		}
		// make sure that all paint operations caused by closing the progress
		// dialog get flushed, otherwise extra pixels will remain on the screen until
		// updateSelections is completed
		getShell().update();
		// The updateSelections method accesses SWT widgets so cannot be executed
		// as part of the above progress dialog operation since the operation forks
		// a new process.
		updateSelections(selectionMap);
	}

	/**
	 * Update the tree to only select those elements that are in the import data.
	 */
	protected void setupSelectionsBasedOnImportData() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getContainer().getShell());
		final Map selectionMap = new Hashtable();
		final IElementFilter filter = new IElementFilter() {
			public void filterElements(Collection files, IProgressMonitor monitor) throws InterruptedException {
				if (files == null) {
					throw new InterruptedException();
				}
				Iterator filesList = files.iterator();
				while (filesList.hasNext()) {
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
					checkFile(filesList.next());
				}
			}

			public void filterElements(Object[] files, IProgressMonitor monitor) throws InterruptedException {
				if (files == null) {
					throw new InterruptedException();
				}
				for (int i = 0; i < files.length; i++) {
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
					checkFile(files[i]);
				}
			}

			private void checkFile(Object fileElement) {
				MinimizedFileSystemElement file = (MinimizedFileSystemElement) fileElement;
				if (isIncludedInImportData(file)) {
					List elements = new ArrayList();
					FileSystemElement parent = file.getParent();
					if (selectionMap.containsKey(parent)) {
						elements = (List) (selectionMap.get(parent));
					}
					elements.add(file);
					selectionMap.put(parent, elements);
				}
			}
		};
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor) throws InterruptedException {
				String msg = RemoteImportExportResources.FILEMSG_IMPORT_FILTERING;
				monitor.beginTask(msg, IProgressMonitor.UNKNOWN);
				getSelectedResources(filter, monitor);
			}
		};
		try {
			// have to set fork to false to avoid InvocationTargetException !!
			dialog.run(false, true, runnable);
		} catch (InvocationTargetException exception) {
			//Couldn't start. Do nothing.
			return;
		} catch (InterruptedException exception) {
			//Got interrupted. Do nothing.
			return;
		}
		// make sure that all paint operations caused by closing the progress
		// dialog get flushed, otherwise extra pixels will remain on the screen until
		// updateSelections is completed
		getShell().update();
		// The updateSelections method accesses SWT widgets so cannot be executed
		// as part of the above progress dialog operation since the operation forks
		// a new process.
		updateSelections(selectionMap);
	}

	/* (non-Javadoc)
	 * Method declared on IDialogPage. Set the selection up when it becomes visible.
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		resetSelection();
		// importing from remote folder selection
		if (initSourceNameSet) {
			setAllSelections(true);
			selectionGroup.setFocus();
		}
		RemoteImportWizard parentWizard = (RemoteImportWizard) getWizard();
		boolean isInitializingFromImportData = parentWizard.getInitializeFromImportData();
		// initializing from import data
		if (isInitializingFromImportData) {
			setAllSelections(true);
			setupSelectionsBasedOnImportData();
		}
	}

	/**
	 * Update the selections with those in map . Implemented here to give inner class
	 * visibility
	 * @param map Map - key tree elements, values Lists of list elements
	 */
	protected void updateSelections(Map map) {
		super.updateSelections(map);
	}

	/**
	 * Check if widgets are enabled or disabled by a change in the dialog.
	 * Provided here to give access to inner classes.
	 */
	protected void updateWidgetEnablements() {
		// need this check because handleEvent(), which calls this, is called when restoring container
		if (saveSettingsButton != null && descFilePathLabel != null && descFilePathField != null && descFileBrowseButton != null) {
			boolean isSaveSettings = isSaveSettings();
			descFilePathLabel.setEnabled(isSaveSettings);
			descFilePathField.setEnabled(isSaveSettings);
			descFileBrowseButton.setEnabled(isSaveSettings);
		}

		// if review is selected, the other options are grayed out without save settings
		if (reviewSynchronizeCheckbox != null){ // event handling could come before the widgets are created
			boolean isReview = reviewSynchronizeCheckbox.getSelection();
			overwriteExistingResourcesCheckbox.setEnabled(!isReview);
			createContainerStructureButton.setEnabled(!isReview);
			createOnlySelectedButton.setEnabled(!isReview);
		}
		
			
		// this calls to determine whether page can be completed
		super.updateWidgetEnablements();
	}

	/**
	 *	Answer a boolean indicating whether self's source specification
	 *	widgets currently all contain valid values.
	 */
	protected boolean validateSourceGroup() {
		File sourceDirectory = getSourceDirectory();
		if (sourceDirectory == null) {
			setMessage(SOURCE_EMPTY_MESSAGE);
			enableButtonGroup(false);
			return false;
		}
		if (sourceConflictsWithDestination(new Path(sourceDirectory.getPath()))) {
			setErrorMessage(getSourceConflictMessage());
			enableButtonGroup(false);
			return false;
		}
		enableButtonGroup(true);
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
			if (fileExtension == null || !fileExtension.equals(Utilities.IMPORT_DESCRIPTION_EXTENSION)) {
				setErrorMessage(NLS.bind(RemoteImportExportResources.IMPORT_EXPORT_ERROR_DESCRIPTION_INVALID_EXTENSION, Utilities.IMPORT_DESCRIPTION_EXTENSION));
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
	 * Returns whether the source location conflicts
	 * with the destination resource. This will occur if
	 * the source is already under the destination.
	 *
	 * @param sourcePath the path to check
	 * @return <code>true</code> if there is a conflict, <code>false</code> if not
	 */
	protected boolean sourceConflictsWithDestination(IPath sourcePath) {
		IContainer container = getSpecifiedContainer();
		if (container == null)
			return false;
		else {
			if (container.getLocation() == null){
				// this is an EFS project
				return false;
			}	
			else {
				return container.getLocation().isPrefixOf(sourcePath);
			}
		}
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

	public void cancel() {
		if (_queryAllJob != null && _queryAllJob.isActive()){
			_queryAllJob.cancel();
		}
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

	// ----------------
	// INTERNAL METHODS
	// ----------------
	/**
	 * Internal method <br>
	 * Configure the message line
	 */
	//	private void configureMessageLine()
	//	{
	//		msgLine = SystemDialogPageMessageLine.createWizardMsgLine(this);
	//		if (msgLine!=null)
	//		{
	//			if (pendingMessage!=null)
	//			  setMessage(pendingMessage);
	//			if (pendingErrorMessage!=null)
	//			  setErrorMessage(pendingErrorMessage);
	//		}
	//	}
	// -----------------------------
	// ISystemMessageLine methods...
	// -----------------------------
	/**
	 * ISystemMessageLine method. <br>
	 * Clears the currently displayed error message and redisplayes
	 * the message which was active before the error message was set.
	 */
	public void clearErrorMessage() {
		if (msgLine != null) msgLine.clearErrorMessage();
	}

	/**
	 * ISystemMessageLine method. <br>
	 * Clears the currently displayed message.
	 */
	public void clearMessage() {
		if (msgLine != null) msgLine.clearMessage();
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
			if (message != null) {
				msgLine.setErrorMessage(message);
			} else {
				msgLine.clearErrorMessage();
			}
		} else { // not configured yet
			pendingErrorMessage = message;
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

			SystemMessage msg = new SimpleSystemMessage(RemoteImportExportPlugin.PLUGIN_ID,
					ICommonMessageIds.MSG_ERROR_UNEXPECTED,
					IStatus.ERROR,
					CommonMessages.MSG_ERROR_UNEXPECTED, exc);
			pendingErrorMessage = msg;
		}
	}

	/**
	 * ISystemMessageLine method. <br>
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(String message) {
		if (msgLine != null) {
			if (message != null) {
				msgLine.setErrorMessage(message);
			} else {
				msgLine.clearErrorMessage();
			}
		} else { // not configured yet
			pendingErrorString = message;
		}
	}

	/**
	 * ISystemMessageLine method. <br>
	 * If the message line currently displays an error,
	 *  the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(SystemMessage message) {
		if (msgLine != null) {
			msgLine.setMessage(message);
		} else { // not configured yet
			pendingMessage = message;
		}
	}

	/**
	 * ISystemMessageLine method. <br>
	 * Set the non-error message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(String message) {
		if (msgLine != null) {
			msgLine.setMessage(message);
		} else { // not configured yet
			pendingString = message;
		}
	}

	public boolean determinePageCompletion(){
			if (_queryAllJob != null && _queryAllJob.isActive()){
				return false;
			}
			return super.determinePageCompletion();
	}
}
