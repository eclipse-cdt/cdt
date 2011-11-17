/*******************************************************************************
 * Copyright (c) 2004, 2010 BitMethods Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BitMethods Inc - initial API and implementation
 *     Sascha Radike <sradike@ejectlag.com> - Support for workspace browsing and small improvements
 *     James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.utils.ui.controls;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.swt.IFocusService;
import org.eclipse.ui.views.navigator.ResourceComparator;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.CDTStatusInfo;
import org.eclipse.cdt.ui.newui.TypedCDTViewerFilter;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;
import org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableManager;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableSubstitutor;

import org.eclipse.cdt.internal.core.resources.ResourceLookup;

import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * Instances of this class allow the user to add, remove, delete, moveup and movedown
 * the items in the list control.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FileListControl {

	/**
	 * Constant copied from ManagedBuild IOption indicating that the entries in this
	 * FileListControl are neither files nor directories -- they're treated as a plain
	 * String list.
	 *
	 * #see org.eclipse.cdt.managedbuilder.core.IOption#BROWSE_NONE
	 * @since 5.2
	 */
	public static final int BROWSE_NONE = 0;
	/**
	 * Constant copied from ManagedBuild IOption indicating that the entries in this
	 * FileListControl are Files.
	 *
	 * #see org.eclipse.cdt.managedbuilder.core.IOption#BROWSE_FILE
	 * @since 5.2
	 */
	public static final int BROWSE_FILE = 1;
	/**
	 * Constant copied from ManagedBuild IOption indicating that the entries in this
	 * FileListControl are Directories.
	 *
	 * #see org.eclipse.cdt.managedbuilder.core.IOption#BROWSE_DIR
	 * @since 5.2
	 */
	public static final int BROWSE_DIR = 2;

	/**
	 * Multi-purpose dialog to prompt the user for a value, path, or file.
	 *
	 * @since 2.0
	 */
	class SelectPathInputDialog extends InputDialog {

		private String[] values = new String[0];

		private int type;
		/* True if user successfully set the text value by a browse dialog */
		private boolean fSetByBrowseDialog = false;

		/**
		 * @param parentShell
		 * @param dialogTitle
		 * @param dialogMessage
		 * @param initialValue
		 * @param validator
		 * @param browseType
		 */
		public SelectPathInputDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue, IInputValidator validator, int browseType) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
			this.type = browseType;
		}

		/**
		 * Returns true if the value has been set by a browse dialog.
		 */
		public boolean isValueSetByBrowse() {
			return fSetByBrowseDialog;
		}

		/**
		 * Allow this dialog to return multiple entires
		 * @return String[] represeting the collected values
		 */
		public String[] getValues() {
			// If values only has one entry or fewer, then return getValue() to catch more recent changes to edit field
			if (values.length <= 1)
				return new String[] { getValue() };
			return values;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);

			if (((type == BROWSE_DIR) || (type == BROWSE_FILE)
					) && (fWorkspaceSupport)) {

				/* Browse button for workspace folders/files */
				final Button workspaceButton = createButton(parent, 3, WORKSPACEBUTTON_NAME, false);
				workspaceButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent ev) {
						/* Before opening the browse dialog we try to convert the current
						 * path text to a valid workspace resource, so we can set it
						 * as initial selection in the dialog.
						 *
						 * First we remove all double-quotes. Then the build macro provider
						 * will resolve all macros/variables (like workspace_loc, ...).
						 *
						 * If the workspace location path is a prefix of our resolved path,
						 * we will remove that part and finally get a full path relative to the
						 * workspace. We can use that path to set the initially selected resource.
						 */

						String currentPathText = getText().getText();

						/* Remove double quotes */
						currentPathText = currentPathText.replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$

						/* Resolve variables */
						IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();

						/* See if we can discover the project from the context *
						 * and check whether the path must be resolved... */
						IProject project = null;
						IResource resource = null;
						if(contextInfo != null) {
							try {
								// Try to find the project
								ICdtVariable var = SupplierBasedCdtVariableManager.getVariable(PROJECTNAME_VAR, contextInfo, true);
								if (var != null && var.getValueType() == ICdtVariable.VALUE_TEXT)
									project = ResourcesPlugin.getWorkspace().getRoot().getProject(var.getStringValue());

								// Try to resolve the currentPathText
								IVariableSubstitutor varSubs = new SupplierBasedCdtVariableSubstitutor(contextInfo, "", "");  //$NON-NLS-1$//$NON-NLS-2$
								String value = CdtVariableResolver.resolveToString(currentPathText, varSubs);
								if (!"".equals(value)) { //$NON-NLS-1$
									IResource rs[] = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(URIUtil.toURI(value));
									if (rs == null || rs.length == 0)
										resource = ResourceLookup.selectFileForLocation(new Path(value), null);
									else
										resource = rs[0];
								}
							} catch (CdtVariableException e) {
								// It's OK not to find the project... carry on as before
							}
						}

						/* Create workspace folder/file selection dialog and
						 * set initial selection */
						ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(),
								new WorkbenchLabelProvider(), new WorkbenchContentProvider());

		                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
		                dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));

						if (type == BROWSE_DIR)	{
							dialog.setInitialSelection(resource);
							Class<?>[] filteredResources = {IContainer.class, IProject.class};
							dialog.addFilter(new TypedCDTViewerFilter(filteredResources));
							dialog.setTitle(WORKSPACE_DIR_DIALOG_TITLE);
			                dialog.setMessage(WORKSPACE_DIR_DIALOG_MSG);
						} else {
							dialog.setInitialSelection(resource);
							dialog.setValidator(new ISelectionStatusValidator() {
							    @Override
								public IStatus validate(Object[] selection) {
							    	if (selection != null)
						    			for (Object sel : selection)
							    			if (!(sel instanceof IFile))
							    				return new CDTStatusInfo(IStatus.ERROR, WORKSPACE_FILE_DIALOG_ERR);
							    	return new CDTStatusInfo();
							    }
							});
							dialog.setTitle(WORKSPACE_FILE_DIALOG_TITLE);
			                dialog.setMessage(WORKSPACE_FILE_DIALOG_MSG);
						}

						/* Open dialog and process result.
						 * If a resource has been selected we create a workspace relative path for it.
						 * Use ${ProjName} if the full path is relative to the context's location */
						if (dialog.open() == Window.OK) {
							fSetByBrowseDialog = true;

							Object[] rs = dialog.getResult();

							if (rs != null) {
								int i = 0;
								values = new String[rs.length];
								for (Object o : rs) {
									resource = (IResource) o;
									if (resource.getProject().equals(project))
										values[i++] = variableManager.generateVariableExpression(WORKSPACELOC_VAR,
												PROJECTNAME_PATH.append(resource.getProjectRelativePath()).makeAbsolute().toString());
									else
										values[i++] = variableManager.generateVariableExpression(WORKSPACELOC_VAR,
												resource.getFullPath().toString());
								}
								// If only one entry, update the text field
								if (values.length == 1)
									getText().setText(values[0]);
								else
									// More then one item selected and OK pressed. Exit this edit dialog
									buttonPressed(IDialogConstants.OK_ID);
							}
						}
					}
				});
			}

			if (type != BROWSE_NONE) {
				/* Browse button for external directories/files */
				final Button externalButton = createButton(parent, 4, FILESYSTEMBUTTON_NAME, false);
				externalButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent ev) {
						String currentName;
						String result;
						switch (type) {
							case BROWSE_DIR :
								DirectoryDialog dialog = new DirectoryDialog(getParentShell(),
										SWT.OPEN|SWT.APPLICATION_MODAL);
								currentName = getText().getText();
								if(currentName != null && currentName.trim().length() != 0) {
									dialog.setFilterPath(currentName);
								} else if(FileListControl.this.filterPath != null) {
									dialog.setFilterPath(FileListControl.this.filterPath);
								}
								dialog.setMessage(FILESYSTEM_DIR_DIALOG_MSG);
								result = dialog.open();
								if(result != null) {
									fSetByBrowseDialog = true;
									getText().setText(result);
								}
								break;
							case BROWSE_FILE:
								FileDialog browseDialog = new FileDialog(getParentShell());
								currentName = getText().getText();
								if (currentName != null && currentName.trim().length() != 0) {
									browseDialog.setFilterPath(currentName);
								} else if (FileListControl.this.filterPath != null) {
									browseDialog.setFilterPath(FileListControl.this.filterPath);
								}
								if (FileListControl.this.filterExtensions != null) {
									browseDialog.setFilterExtensions(FileListControl.this.filterExtensions);
								}
								result = browseDialog.open();
								if (result != null) {
									fSetByBrowseDialog = true;
									getText().setText(result);
								}
								break;
						}
					}
				});
			}
		}

	}

	/**
	 * An extended List control with support for cut / copy / paste & undo
	 * Needs to be public for the copy method to be called by the platform via reflection
	 * @since 5.2
	 * @noinstantiate This class is not intended to be instantiated by clients.
	 */
	public final class ClipboardList extends List {
		private Clipboard clipboard;

		public ClipboardList(Composite parent, int style) {
			super (parent, style);
		}
		private String[] getClipboardContents() {
			Clipboard cp = getClipboard();
			String contents = (String)cp.getContents(TextTransfer.getInstance());
			if (contents != null) {
				String[] arr = contents.split("\n"); //$NON-NLS-1$
				return arr;
			}
			return new String[0];
		}
		public void copy() {
			String[] toCopy = getSelection();
			if (toCopy != null && toCopy.length > 0) {
				StringBuilder sb = new StringBuilder();
				for (String item : toCopy)
					sb.append(item.trim()).append("\n"); //$NON-NLS-1$
				Clipboard cp = getClipboard();
				cp.setContents(new Object[]{sb.toString().trim()}, new Transfer[] {TextTransfer.getInstance()});
			}
		}
		public void cut() {
			copy();
			// Only remove from the list box if the cut was successful
			if (Arrays.equals(getClipboardContents(), getSelection()))
				removePressed();
		}
		public void paste() {
			String[] pasteBuffer = getClipboardContents();
			int i = getSelectionIndex();
			// insert items at the correct location
			for (String item : pasteBuffer)
				if (!item.trim().equals("")) //$NON-NLS-1$
					add(item.trim(), ++i);
			checkNotificationNeeded();
		}
		public void undo() {
			try {
				operationHistory.undo(undoContext, null, null);
			} catch (ExecutionException e) {
				CUIPlugin.log(e);
			}
		}
		public void redo() {
			try {
				operationHistory.redo(undoContext, null, null);
			} catch (ExecutionException e) {
				CUIPlugin.log(e);
			}
		}
		private Clipboard getClipboard() {
			if (clipboard == null)
				clipboard = new Clipboard(Display.getDefault());
			return clipboard;
		}
		@Override
		public void dispose() {
			super.dispose();
			if (clipboard != null)
				clipboard.dispose();
		}
		/**
		 * Handle backspace / delete key
		 */
		public void delete() {
			removePressed();
		}
		@Override
		protected void checkSubclass() {
			// We're adding action handlers, override...
		}
	}


	/* Variable names */
	/* See CdtMacroSupplier: used for making absolute paths relative if desired */
	private static final String WORKSPACELOC_VAR = "workspace_loc"; //$NON-NLS-1$
	private static final String PROJECTNAME_VAR = "ProjName"; //$NON-NLS-1$
	private static final IPath PROJECTNAME_PATH = new Path(VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression(PROJECTNAME_VAR, null));

	/* Names, messages and titles */
	private static final String WORKSPACEBUTTON_NAME = Messages.FileListControl_button_workspace;
	private static final String FILESYSTEMBUTTON_NAME = Messages.FileListControl_button_fs;

	private static final String ADD_STR = Messages.FileListControl_add;
	private static final String DEL_STR = Messages.FileListControl_delete;
	private static final String EDIT_STR = Messages.FileListControl_edit;
	private static final String MOVEUP_STR = Messages.FileListControl_moveup;
	private static final String MOVEDOWN_STR = Messages.FileListControl_movedown;
	private static final String FILE_TITLE_ADD = Messages.BrowseEntryDialog_file_title_add;
	private static final String DIR_TITLE_ADD = Messages.BrowseEntryDialog_dir_title_add;
	private static final String FILE_TITLE_EDIT = Messages.BrowseEntryDialog_file_title_edit;
	private static final String DIR_TITLE_EDIT = Messages.BrowseEntryDialog_dir_title_edit;
	private static final String WORKSPACE_DIR_DIALOG_TITLE = Messages.BrowseEntryDialog_wsp_dir_dlg_title;
	private static final String WORKSPACE_FILE_DIALOG_TITLE = Messages.BrowseEntryDialog_wsp_file_dlg_title;
	private static final String WORKSPACE_DIR_DIALOG_MSG = Messages.FileListControl_BrowseEntryDialog_wsp_dir_dlg_msg;
	private static final String WORKSPACE_FILE_DIALOG_MSG = Messages.FileListControl_BrowseEntryDialog_wsp_file_dlg_msg;
	private static final String WORKSPACE_FILE_DIALOG_ERR = Messages.FileListControl_BrowseEntryDialog_wsp_file_dlg_err;
	private static final String FILESYSTEM_DIR_DIALOG_MSG = Messages.BrowseEntryDialog_fs_dir_dlg_msg;
	private static final String FILE_MSG = Messages.BrowseEntryDialog_message_file;
	private static final String DIR_MSG = Messages.BrowseEntryDialog_message_directory;
	private static final String TITLE = Messages.BuildPropertyCommon_label_title;

	/** flag which prevents us from resetting the prompt for delete flag */
	private boolean neverPromptForDelete;
	/** Flag indicating whether the user should be prompted for delete */
	private boolean promptForDelete;

	//toolbar
	private ToolBar toolBar;
	// toolbar items
	private ToolItem addItem, deleteItem, editItem, moveUpItem, moveDownItem;
	// title label
	private Label title;
	// list control
	private ClipboardList list;
	private String compTitle;
	private SelectionListener selectionListener;
	private GridData tgdata, grid3, grid4, grid2;

	// The type of browse support that is required
	private int browseType;
	private String filterPath;
	private String[] filterExtensions;
	/** The base path that should be used when adding new resources */
	private IPath path = new Path(""); //$NON-NLS-1$

	/* Workspace support */
	private boolean fWorkspaceSupport = false;
	private IVariableContextInfo contextInfo;
	/** Undo support */
	IUndoContext undoContext;
	IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();

	private java.util.List<IFileListChangeListener> listeners = new ArrayList<IFileListChangeListener>();
	private String[] oldValue;

	//images
	private final Image IMG_ADD = CDTSharedImages.getImage(CDTSharedImages.IMG_FILELIST_ADD);
	private final Image IMG_DEL = CDTSharedImages.getImage(CDTSharedImages.IMG_FILELIST_DEL);
	private final Image IMG_EDIT = CDTSharedImages.getImage(CDTSharedImages.IMG_FILELIST_EDIT);
	private final Image IMG_MOVEUP = CDTSharedImages.getImage(CDTSharedImages.IMG_FILELIST_MOVEUP);
	private final Image IMG_MOVEDOWN = CDTSharedImages.getImage(CDTSharedImages.IMG_FILELIST_MOVEDOWN);

	/**
	 * Constructor
	 *
	 * @param parent
	 * @param compTitle
	 * @param type
	 * @param promptForDelete indicates whether the user should be prompted on delete
	 * @see #FileListControl(Composite, String, int)
	 * @since 5.2
	 */
	public FileListControl(Composite parent, String compTitle, int type, boolean promptForDelete) {
		this(parent, compTitle, type);
		this.promptForDelete = promptForDelete;
		this.neverPromptForDelete = !promptForDelete;
	}

	/**
	 * Constructor
	 *
	 * This FileListControl only prompts the user on Delete for BROWSE_FILE and BROWSE_DIR
	 * @param parent
	 * @param compTitle
	 * @param type one of the IOption BROWSE types
	 * @see #BROWSE_NONE
	 * @see #BROWSE_FILE
	 * @see #BROWSE_DIR
	 */
	public FileListControl(Composite parent, String compTitle, int type) {
		promptForDelete = type == BROWSE_FILE || type == BROWSE_DIR;

		// Default to no browsing
		browseType = type;

		//file panel
		Composite filePanel = new Composite(parent, SWT.NONE);
		GridLayout form1 = new GridLayout();
		form1.numColumns = 1;
		form1.horizontalSpacing = 0;
		form1.verticalSpacing = 0;
		form1.marginHeight = 0;
		form1.marginWidth = 0;
		filePanel.setLayout(form1);
		filePanel.setLayoutData(new GridData(GridData.FILL_BOTH));

		// title panel
		Composite titlePanel = new Composite(filePanel, SWT.BORDER);
		GridLayout titleform = new GridLayout(2, false);
		titleform.horizontalSpacing = 0;
		titleform.verticalSpacing = 0;
		titleform.marginHeight = 0;
		titleform.marginWidth = 0;
		titlePanel.setLayout(titleform);
		tgdata = new GridData(GridData.FILL_HORIZONTAL);
		tgdata.heightHint = IDialogConstants.BUTTON_BAR_HEIGHT;
		titlePanel.setLayoutData(tgdata);
		title = new Label(titlePanel, SWT.NONE | SWT.BOLD);
		this.compTitle = "  " + compTitle; //$NON-NLS-1$
		title.setText(this.compTitle);
		grid2 = new GridData(GridData.FILL_HORIZONTAL);
		title.setLayoutData(grid2);
		//button panel
		Composite buttonPanel = new Composite(titlePanel, SWT.NONE);
		GridLayout form2 = new GridLayout();
		form2.numColumns = 5;
		form2.horizontalSpacing = 0;
		form2.verticalSpacing = 0;
		form2.marginWidth = 0;
		form2.marginHeight = 0;
		buttonPanel.setLayout(form2);
		// toolbar
		toolBar = new ToolBar(buttonPanel, SWT.HORIZONTAL | SWT.RIGHT
				| SWT.FLAT);
		// add toolbar item
		addItem = new ToolItem(toolBar, SWT.PUSH);
		addItem.setImage(IMG_ADD);
		addItem.setToolTipText(ADD_STR);
		addItem.addSelectionListener(getSelectionListener());
		// delete toolbar item
		deleteItem = new ToolItem(toolBar, SWT.PUSH);
		deleteItem.setImage(IMG_DEL);
		deleteItem.setToolTipText(DEL_STR);
		deleteItem.addSelectionListener(getSelectionListener());
		// edit toolbar item
		editItem = new ToolItem(toolBar, SWT.PUSH);
		editItem.setImage(IMG_EDIT);
		editItem.setToolTipText(EDIT_STR);
		editItem.addSelectionListener(getSelectionListener());
		// moveup toolbar item
		moveUpItem = new ToolItem(toolBar, SWT.PUSH);
		moveUpItem.setImage(IMG_MOVEUP);
		moveUpItem.setToolTipText(MOVEUP_STR);
		moveUpItem.addSelectionListener(getSelectionListener());
		// movedown toolbar item
		moveDownItem = new ToolItem(toolBar, SWT.PUSH);
		moveDownItem.setImage(IMG_MOVEDOWN);
		moveDownItem.setToolTipText(MOVEDOWN_STR);
		moveDownItem.addSelectionListener(getSelectionListener());
		grid3 = new GridData(GridData.FILL_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_BEGINNING);
		buttonPanel.setLayoutData(grid3);
		// list control
		list = new ClipboardList(filePanel, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.MULTI);
		grid4 = new GridData(GridData.FILL_BOTH);
		// force the list to be no wider than the title bar
		Point preferredSize = titlePanel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		grid4.widthHint = preferredSize.x;
		grid4.heightHint = preferredSize.y * 3;
		grid4.horizontalSpan = 2;
		list.setLayoutData(grid4);
		list.addSelectionListener(getSelectionListener());
		//Add a double-click event handler
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// Popup the editor on the selected item from the list
				editSelection();
			}
		});
		// Add a delete key listener
		list.addKeyListener(new KeyAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.KeyAdapter#keyPressed(org.eclipse.swt.events.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
				case SWT.BS:
				case SWT.DEL:
					if (e.stateMask == SWT.NONE)
						removePressed();
					break;
				}
			}
		});

		// Set-up Undo history
		undoContext = new ObjectUndoContext(this);
		operationHistory.setLimit(undoContext, 50);

		// Add command handlers for undo to the control
		try {
			IFocusService fs = (IFocusService)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.getActivePart().getSite().getService(IFocusService.class);
			fs.addFocusTracker(list, "org.eclipse.cdt.ui.FileListControl"); //$NON-NLS-1$
		} catch (Exception e) {
			// Any of the get* methods may return null. As this is in the UI constructor for this control
			// it shouldn't happen. Log and carry on.
			CUIPlugin.log(e);
		}

		selectionChanged();
	}

	/**
	 * Set list values
	 *
	 * @param listVal
	 */
	public void setList(String[] listVal) {
		if (list != null) {
			list.removeAll();
		}
		for (String element : listVal) {
			list.add(element);
		}
		checkNotificationNeeded();
	}

	public void addChangeListener(IFileListChangeListener listener){
		listeners.add(listener);
	}

	public void removeChangeListener(IFileListChangeListener listener){
		listeners.remove(listener);
	}

	/**
	 * Checks whether a notification is needed, and notifies listeners
	 *
	 * Persist any changes in the undo history.
	 *
	 * This method must be called after every change to the contents of the list box
	 *
	 * At end of method oldValue.equals(list.getItems())
	 */
	public void checkNotificationNeeded(){
		final String items[] = getItems();
		if(oldValue != null) {
			if (Arrays.equals(oldValue, items))
				return;

			// Add some context to the undo history
			IUndoableOperation op = new AbstractOperation("") { //$NON-NLS-1$
				final String[] previousValue = oldValue;
				final String[] newValue = items;
				@Override
				public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
					list.setItems(previousValue);
					notifyListeners(newValue, previousValue);
					oldValue = previousValue;
					return Status.OK_STATUS;
				}
				@Override
				public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
					list.setItems(newValue);
					notifyListeners(previousValue, newValue);
					oldValue = newValue;
					return Status.OK_STATUS;
				}
				@Override
				public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
					return Status.CANCEL_STATUS;
				}
			};
			op.addContext(undoContext);
			operationHistory.add(op);
			System.arraycopy(items, 0, oldValue = new String[items.length], 0, items.length);
			notifyListeners(oldValue, items);
			list.setFocus(); // Ensure this control retains focus
		} else
			System.arraycopy(items, 0, oldValue = new String[items.length], 0, items.length);
	}

	public void notifyListeners(String oldVal[], String newVal[]){
		for (IFileListChangeListener listener: listeners) {
			listener.fileListChanged(this,oldVal,newVal);
		}
	}

	/**
	 * Set selection
	 *
	 * @param sel
	 */
	public void setSelection(int sel) {
		if (list.getItemCount() > 0)
			list.setSelection(sel);
		selectionChanged();
	}
	/**
	 * Set default selection
	 */
	public void setSelection() {
		if (list.getItemCount() > 0)
			list.setSelection(0);
	}
	/**
	 * removes all items from list control
	 */
	public void removeAll() {
		if (list != null){
			list.removeAll();
			checkNotificationNeeded();
		}
	}
	/**
	 * get list items
	 */
	public String[] getItems() {
		return list.getItems();
	}
	/**
	 * Create selection listener for buttons
	 */
	private void createSelectionListener() {
		selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Widget widget = event.widget;
				if (widget == addItem) {
					addPressed();
				} else if (widget == deleteItem) {
					removePressed();
				} else if (widget == moveUpItem) {
					upPressed();
				} else if (widget == moveDownItem) {
					downPressed();
				} else if (widget == list) {
					selectionChanged();
				} else if (widget == editItem) {
					editSelection();
				}
			}
		};
	}
	/**
	 * Returns selection listener
	 *
	 * @return
	 */
	private SelectionListener getSelectionListener() {
		if (selectionListener == null)
			createSelectionListener();
		return selectionListener;
	}

	/**
	 * This method will be called when the add button is pressed
	 */
	private void addPressed() {
		// Prompt user for a new item
		String[] input = getNewInputObject();

		// Add it to the list
		if (input.length > 0) {
			int index = list.getSelectionIndex();
			int i = 0;
			for (String s : input)
				list.add(s, index + ++i);
			list.setSelection(index + 1);
			checkNotificationNeeded();
		}

		selectionChanged();
	}

	/**
	 * This method will be called when the remove button is pressed
	 */
	private void removePressed() {
		if (list.getSelectionCount() == 0 || list.getSelectionIndex() == -1)
			return;
		boolean delDir = true;
		if (promptForDelete) {
			String quest = Messages.FileListControl_deletedialog_message;
			String title = Messages.FileListControl_deletedialog_title;
			delDir = MessageDialog.openQuestion(list.getShell(), title, quest);
		}
		if (delDir){
			int i;
			while ((i = list.getSelectionIndex()) != -1)
				list.remove(i);
			checkNotificationNeeded();
		}
		selectionChanged();
	}
	/**
	 * This method will be called when the move up button is pressed
	 */
	private void upPressed() {
		int index = list.getSelectionIndex();
		String curSelList = list.getItem(index);
		String preList = list.getItem(index - 1);
		list.setItem(index - 1, curSelList);
		list.setItem(index, preList);
		list.setSelection(index - 1);
		checkNotificationNeeded();
		selectionChanged();
	}
	/**
	 * This method will be called when the move down button is pressed
	 */
	private void downPressed() {
		int index = list.getSelectionIndex();
		String curSelList = list.getItem(index);
		String nextList = list.getItem(index + 1);
		list.setItem(index + 1, curSelList);
		list.setItem(index, nextList);
		list.setSelection(index + 1);
		checkNotificationNeeded();
		selectionChanged();
	}
	/**
	 * This method will be called when the edit button is pressed
	 */
	private void editSelection() {
		final int index = list.getSelectionIndex();
		if (index != -1) {
			String selItem = list.getItem(index);
			if (selItem != null) {
				/* Use SelectPathInputDialog for IOption.BROWSE_DIR and
				 * IOption.BROWSE_FILE. Use simple input dialog otherwise.
				 */
				InputDialog dialog;
				if ((browseType == BROWSE_DIR) ||
						(browseType == BROWSE_FILE)) {

					String title;
					String message;
					if (browseType == BROWSE_DIR) {
						title = DIR_TITLE_EDIT;
						message = DIR_MSG;
					} else {
						title = FILE_TITLE_EDIT;
						message = FILE_MSG;
					}
					dialog =  new SelectPathInputDialog(getListControl().getShell(), title, message, selItem, null, browseType);
				} else {
					String title = Messages.FileListControl_editdialog_title;
					dialog = new InputDialog(null, title, compTitle, selItem, null);
				}

				if (dialog.open() == Window.OK) {
					String[] newItems;

					/* If newItem is a directory or file path we need to
					 * double-quote it if required. We only do this if the user
					 * selected a new path using a browse button. If he/she simply
					 * edited the text, we skip this so the user can remove quotes if he/she
					 * wants to.
					 */
					if (dialog instanceof SelectPathInputDialog) {
						SelectPathInputDialog selDialog = (SelectPathInputDialog)dialog;
						newItems = selDialog.getValues();
						if (selDialog.isValueSetByBrowse())
							for (int i = 0 ; i < newItems.length ; i++)
								newItems[i] = doubleQuotePath(newItems[i]);
					} else
						newItems = new String[] { dialog.getValue() };

					// If no change, return
					if (newItems.length == 1 && newItems[0].equals(selItem))
						return;

					// Replace the changed item & insert new items
					list.setItem(index, newItems[0]);
					for (int i = 1 ; i < newItems.length ; i++)
						list.add(newItems[i], index + i);
					checkNotificationNeeded();
					selectionChanged();
				}
			}
		}
	}

	/**
	 * This method will be called when the list selection changed
	 */
	public void selectionChanged() {
		int index = list.getSelectionIndex();
		int size = list.getItemCount();
		int selectionCount = list.getSelectionCount();
		deleteItem.setEnabled(size > 0);
		moveUpItem.setEnabled(size > 1 && index > 0 && selectionCount == 1);
		moveDownItem.setEnabled(size > 1 && index >= 0 && index < size - 1 && selectionCount == 1);
		editItem.setEnabled(selectionCount == 1);
	}
	/**
	 * Returns List control
	 */
	public List getListControl() {
		return list;
	}

	/**
	 * Sets the IPath of the project the field editor was
	 * created for.
	 *
	 * @param path The path to the
	 */
	public void setPath(IPath path) {
		this.path = path;
	}

	/**
	 * Set browseType
	 * @deprecated This class should be constructed with the correct type
	 */
	@Deprecated
	public void setType(int type) {
		browseType = type;
		if (!neverPromptForDelete)
			promptForDelete = type == BROWSE_FILE || type == BROWSE_DIR;
	}

	/**
	 * Sets the default filter-path for the underlying Browse dialog. Only applies when browseType is 'file' or 'dir'.
	 * @param filterPath
	 *
	 * @since 5.2
	 */
	public void setFilterPath(String filterPath) {
		this.filterPath = filterPath;
	}

	/**
	 * Sets the filter-extensions for the underlying Browse dialog. Only applies when browseType is 'file'.
	 * @param filterExtensions
	 *
	 * @since 5.2
	 */
	public void setFilterExtensions(String[] filterExtensions) {
		this.filterExtensions = filterExtensions;
	}

	/**
	 * Enable/Disable workspace support. If enabled, the workspace browse button
	 * will be visible in the SelectPathInputDialog.
	 * @param enable
	 */
	public void setWorkspaceSupport(boolean enable)	{
		fWorkspaceSupport = enable;
	}

	/**
	 * Set the field editor context.
	 */
	public void setContext(IVariableContextInfo info) {
		contextInfo = info;
		for(;info != null;info = info.getNext()){
			/*
			if(info.getContextType() == IBuildMacroProvider.CONTEXT_PROJECT){
				IManagedProject mngProj = (IManagedProject)info.getContextData();
				this.rc = mngProj.getOwner();
				break;
			}
			*/
		}
	}

	/**
	 * Returns the input dialog string
	 */
	private String[] getNewInputObject() {
		// Create a dialog to prompt for a new list item
		String[] input = new String[0];
		String title = ""; //$NON-NLS-1$
		String message = ""; //$NON-NLS-1$
		String initVal = ""; //$NON-NLS-1$

		if (browseType == BROWSE_DIR) {
			title = DIR_TITLE_ADD;
			message = DIR_MSG;
			initVal = path.toString();
		} else if (browseType == BROWSE_FILE) {
			title = FILE_TITLE_ADD;
			message = FILE_MSG;
			initVal = path.toString();
		} else {
			title = TITLE;
			message = compTitle;
		}

		// Prompt for value
		SelectPathInputDialog dialog = new SelectPathInputDialog(getListControl().getShell(), title, message, initVal, null, browseType);
		if (dialog.open() == Window.OK) {
			input = dialog.getValues();

			/* Double-quote (if required) the text if it is a directory or file */
			if (input.length > 0) {
				if (browseType == BROWSE_DIR || browseType == BROWSE_FILE)
					for (int i = 0 ; i < input.length ; i++)
						input[i] = doubleQuotePath(input[i]);
			}
		}

		return input;
	}

	public Label getLabelControl(){
		return title;
	}

	public void setEnabled(boolean enabled){
		title.setEnabled(enabled);
		toolBar.setEnabled(enabled);
		list.setEnabled(enabled);
	}

	/**
	 * Double-quotes a path name if it contains white spaces, backslahes
	 * or a macro/variable (We don't know if a macro will contain spaces, so we
	 * have to be on the safe side).
	 * @param pathName The path name to double-quote.
	 * @return
	 */
	private String doubleQuotePath(String pathName)	{
		/* Trim */
		pathName = pathName.trim();

		/* Check if path is already double-quoted */
		boolean bStartsWithQuote = pathName.startsWith("\""); //$NON-NLS-1$
		boolean bEndsWithQuote = pathName.endsWith("\""); //$NON-NLS-1$

		/* Check for spaces, backslashes or macros */
		int i = pathName.indexOf(" ") + pathName.indexOf("\\") //$NON-NLS-1$ //$NON-NLS-2$
			+ pathName.indexOf("${"); //$NON-NLS-1$

		/* If indexof didn't fail all three times, double-quote path */
		if (i != -3) {
			if (!bStartsWithQuote)
				pathName = "\"" + pathName; //$NON-NLS-1$
			if (!bEndsWithQuote)
				pathName = pathName + "\""; //$NON-NLS-1$
		}

		return pathName;
	}
}
