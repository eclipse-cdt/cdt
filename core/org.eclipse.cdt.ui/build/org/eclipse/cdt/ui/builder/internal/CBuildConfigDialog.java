/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Based on org.eclipse.debug.org.eclipse.cdt.ui.builder.internal.ui.launchConfigurations.LaunchConfigurationDialog.
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.ui.builder.internal;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.builder.BuilderPlugin;
import org.eclipse.cdt.core.builder.model.ICBuildConfig;
import org.eclipse.cdt.core.builder.model.ICBuildConfigManager;
import org.eclipse.cdt.core.builder.model.ICBuildConfigWorkingCopy;
import org.eclipse.cdt.core.builder.model.ICTool;
import org.eclipse.cdt.core.builder.model.ICToolType;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.builder.ICBuildConfigDialog;
import org.eclipse.cdt.ui.builder.ICToolTab;
import org.eclipse.cdt.ui.builder.ICToolTabGroup;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.PixelConverter;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * @author sam.robb
 * 
 * The dialog used to edit build configurations.
 */
public class CBuildConfigDialog extends TitleAreaDialog 
										implements ICBuildConfigDialog {

	/**
	 * List of tools available to thsi configuration
	 */
	private ListViewer fToolList;
	
	/**
	 * The workbench context present when this dialog is opened.
	 */
	private Object fContext;
	
	/**
	 * The IResource corresponding to <code>fContext</code>.
	 */
	private IResource fResourceContext;
	
	/**
	 * The Composite used to insert an adjustable 'sash' between the tree and the tabs.
	 */
	private SashForm fSashForm;
	
	/**
	 * Default weights for the SashForm that specify how wide the selection and
	 * edit areas aree relative to each other.
	 */
	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] {11, 30};
	
	/**
	 * The tool selection area.
	 */
	private Composite fSelectionArea;	
	
	/**
	 * The tool configuration edit area.
	 */
	private Composite fEditArea;
	
	/**
	 * The 'apply' button
	 */
	private Button fApplyButton;	
	
	/**
	 * The 'revert' button
	 */
	private Button fRevertButton;
	
	/**
	 * The text widget displaying the name of the
	 * build configuration under edit
	 */
	private Text fNameText;
	
	private String fLastSavedName = null;
	
	/**
	 * Container for the edit area <code>TabFolder</code>
	 */
	private Composite fTabComposite;
	
	/**
	 * The tab folder that contains tabs for the selected configuration
	 */
	private TabFolder fTabFolder;
	
	/**
	 * Flag that indicates when the tabs are being disposed.
	 */
	private boolean fDisposingTabs = false;
	
	/**
	 * The current (working copy) build configuration
	 * being displayed/edited or <code>null</code> if
	 * none
	 */
	private ICBuildConfigWorkingCopy fWorkingCopy;
	
	/**
	 * The actual (non-working copy) build configuration that underlies the current working copy
	 */
	private ICBuildConfig fUnderlyingConfig;
	
	/**
	 * The current tab group being displayed
	 */
	private ICToolTabGroup fTabGroup;
	
	/**
	 * The type of tool tabs are currently displayed for
	 */
	private ICToolType fToolType;
	
	/** 
	 * The index of the currently selected tab
	 */
	private int fCurrentTabIndex;
	
	private Cursor waitCursor;
	private Cursor arrowCursor;
	private MessageDialog fWindowClosingDialog;
	
	/**
	 * Whether initlialing tabs
	 */
	private boolean fInitializingTabs = false;
		
	/**
	 * Indicates if selection changes in the tree should be ignored
	 */
	private boolean fIgnoreSelectionChanges = false;
	
	/**
	 * Previously selected element in the tree
	 */
	private Object fSelectedTreeObject;
	
	/**
	 * The number of 'long-running' operations currently taking place in this dialog
	 */	
	private long fActiveRunningOperations = 0;
		
	/**
	 * Id for 'Close' button.
	 */
	protected static final int ID_CLOSE_BUTTON = IDialogConstants.CLIENT_ID + 1;
	
	/**
	 * Id for 'Cancel' button.
	 */
	protected static final int ID_CANCEL_BUTTON = IDialogConstants.CLIENT_ID + 2;
	
	/**
	 * Constrant String used as key for setting and retrieving current Control with focus
	 */
	private static final String FOCUS_CONTROL = "focusControl";//$NON-NLS-1$

	/**
	 * The height in pixels of this dialog's progress indicator
	 */
	private static int PROGRESS_INDICATOR_HEIGHT = 18;

	/**
	 * Constant specifying how wide this dialog is allowed to get (as a percentage of
	 * total available screen width) as a result of tab labels in the edit area.
	 */
	private static final float MAX_DIALOG_WIDTH_PERCENT = 0.75f;

	/**
	 * Empty array
	 */
	protected static final Object[] EMPTY_ARRAY = new Object[0];	
	
	protected static final String DEFAULT_NEW_CONFIG_NAME = "New configuration";
	
	/**
	 * Size of this dialog if there is no preference specifying a size.
	 */
	protected static final Point DEFAULT_INITIAL_DIALOG_SIZE = new Point(620, 560);

	private String fCantSaveErrorMessage;

	private static String DIALOG_SASH_WEIGHTS = CUIPlugin.getPluginId() + ".buildConfigurationDialogSashWeights";
	private static String DIALOG_LOCATION = CUIPlugin.getPluginId() + ".buildConfigurationDialogLocation";
	private static String DIALOG_SIZE = CUIPlugin.getPluginId() + ".buildConfigurationDialogSize";

	/**
	 * Constructs a new build configuration dialog on the given
	 * parent shell.
	 * 
	 * @param shell the parent shell
	 * @param selection the selection used to initialize this dialog, typically the 
	 *  current workbench selection
	 * @param config onfiguration to edit.
	 */
	public CBuildConfigDialog(Shell shell, ICBuildConfig config) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fUnderlyingConfig = config;
		try {
			setWorkingCopy(config.copy(config.getName()));
		} catch (CoreException e) {
		}
	}

	/**
	 * A build configuration dialog overrides this method
	 * to create a custom set of buttons in the button bar.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ID_CLOSE_BUTTON, "Close", false);
	}
	
	/**
	 * @see Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == ID_CLOSE_BUTTON) {
			handleClosePressed();
		} else {
			super.buttonPressed(buttonId);
		}
	}

	/**
	 * Returns the appropriate text for the build button
	 */
	protected String getBuildButtonText() {
		return ("B&uild");
	}

	/**
	 * @see Dialog#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		initializeBounds();
		initializeSashForm();
		doInitialListSelection();
		return contents;
	}

	/**
	 * Initialize the relative weights (widths) of the 2 sides of the sash.
	 */
	protected void initializeSashForm() {
		int[] sashWeights = DEFAULT_SASH_WEIGHTS;
		String sashWeightString = getPreferenceStore().getString(DIALOG_SASH_WEIGHTS);
		if (sashWeightString.length() > 0) {
			Point sashWeightPoint = parseCoordinates(sashWeightString);
			if (sashWeightPoint != null) {
				sashWeights[0] = sashWeightPoint.x;
				sashWeights[1] = sashWeightPoint.y;
			}
		}
		getSashForm().setWeights(sashWeights);
	}

	/**
	 * Set the initial selection in the tree.
	 */
	protected void doInitialListSelection() {
		// getListViewer().setSelection(null);
	}
	
	/**
	 * Write out this dialog's Shell size, location & sash weights to the preference store.
	 */
	protected void persistShellGeometry() {
		Point shellLocation = getShell().getLocation();
		Point shellSize = getShell().getSize();
		int[] sashWeights = getSashForm().getWeights();
		String locationString = serializeCoords(shellLocation);
		String sizeString = serializeCoords(shellSize);
		String sashWeightString = serializeCoords(new Point(sashWeights[0], sashWeights[1]));
		getPreferenceStore().setValue(DIALOG_LOCATION, locationString);
		getPreferenceStore().setValue(DIALOG_SIZE, sizeString);
		getPreferenceStore().setValue(DIALOG_SASH_WEIGHTS, sashWeightString);
	}
	
	/**
	 * @see Window#close()
	 */
	public boolean close() {
		persistShellGeometry();
		return super.close();
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		GridData gd;
		Composite dialogComp = (Composite)super.createDialogArea(parent);
		Composite topComp = new Composite(dialogComp, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		topComp.setLayoutData(gd);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		topLayout.marginHeight = 5;
		topLayout.marginWidth = 0;
		topComp.setLayout(topLayout);

		// Set the things that TitleAreaDialog takes care of
		setTitle("Build Configuration");
		setMessage(""); //$NON-NLS-1$

		// Create the SashForm that contains the selection area on the left,
		// and the edit area on the right
		setSashForm(new SashForm(topComp, SWT.NONE));
		getSashForm().setOrientation(SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		getSashForm().setLayoutData(gd);
		
		// Create the build configuration selection area and put it into the composite.
		Composite toolSelectionArea = createToolSelectionArea(getSashForm());
		gd = new GridData(GridData.FILL_VERTICAL);
		toolSelectionArea.setLayoutData(gd);
	
		// Create the build configuration edit area and put it into the composite.
		Composite editAreaComp = createEditArea(getSashForm());
		gd = new GridData(GridData.FILL_BOTH);
		editAreaComp.setLayoutData(gd);
			
		// Build the separator line that demarcates the button bar
		Label separator = new Label(topComp, SWT.HORIZONTAL | SWT.SEPARATOR);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		separator.setLayoutData(gd);
		
		dialogComp.layout(true);
		
		return dialogComp;
	}

	/**
	 * Returns tab group for the given type of tool.
	 * Tabs are initialized to be contained in this dialog.
	 * 
	 * @exception CoreException if unable to instantiate a tab group
	 */
	protected ICToolTabGroup createGroup(final ICToolType configType) throws CoreException {
		// Use a final Object array to store the tab group and any exception that
		// results from the Runnable
		final Object[] finalArray = new Object[2]; 		
 		Runnable runnable = new Runnable() {
 			public void run() {
		 		ICToolTabGroup tabGroup = null;
				try {
					tabGroup = CBuildConfigPresentationManager.getDefault().getTabGroup(configType);
					finalArray[0] = tabGroup;
				} catch (CoreException ce) {
					finalArray[1] = ce;
					return;
				}
		 		tabGroup.createTabs(CBuildConfigDialog.this);
		 		ICToolTab[] tabs = tabGroup.getTabs();
		 		for (int i = 0; i < tabs.length; i++) {
		 			tabs[i].setConfigurationDialog(CBuildConfigDialog.this);
		 		}
 			}
 		};
 		
 		// Creating the tabs can result in plugin loading, so we show the busy cursor
 		BusyIndicator.showWhile(getDisplay(), runnable);
 		
 		// Re-throw any CoreException if there was one
 		if (finalArray[1] != null) {
 			throw (CoreException)finalArray[1];
 		}
 		
 		// Otherwise return the tab group
 		return (ICToolTabGroup)finalArray[0];
	}
	
	/**
	 * Convenience method to set the selection on the configuration tree.
	 */
	protected void setListViewerSelection(ISelection selection) {
		getListViewer().setSelection(selection);
	}
	
	private void setLastSavedName(String lastSavedName) {
		this.fLastSavedName = lastSavedName;
	}

	private String getLastSavedName() {
		return fLastSavedName;
	}
	
	/**
	 * Update buttons and message.
	 */
	protected void refreshStatus() {
		updateButtons();
		updateMessage();
	}
	
	/**
	 * Verify the attributes common to all build configuration.
	 * Indicate failure by throwing a <code>CoreException</code>.
	 */
	protected void verifyStandardAttributes() throws CoreException {
		verifyName();
	}
	
	/**
	 * Verify that the build configuration name is valid.
	 */
	protected void verifyName() throws CoreException {
		String currentName = getNameTextWidget().getText().trim();

		// If there is no name, complain
		if (currentName.length() < 1) {
			throw new CoreException(new Status(IStatus.ERROR,
												 CUIPlugin.getPluginId(),
												 0,
												 "A name is required for this build configuration",
												 null));			
		}

		// If the name hasn't changed from the last saved name, do nothing
		if (currentName.equals(getLastSavedName())) {
			return;
		}	
		
		// See if name contains any 'illegal' characters
		IStatus status = ResourcesPlugin.getWorkspace().validateName(currentName, IResource.FILE);
		if (status.getCode() != IStatus.OK) {
			throw new CoreException(new Status(IStatus.ERROR,
												 CUIPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
												 0,
												 status.getMessage(),
												 null));									
		}			
		
		// Otherwise, if there's already a config with the same name, complain
		if (getBuildConfigManager().isExistingConfigurationName(fUnderlyingConfig.getProject(), currentName)) {
			throw new CoreException(new Status(IStatus.ERROR,
												 CUIPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
												 0,
												 "A build configuration with this name already exists",
												 null));						
		}						
	}
	
	/**
	 * If the name is valid, rename the current build configuration.  Otherwise, show an
	 * appropriate error message.
	 */
	protected void updateConfigFromName() {
		if (getConfiguration() != null) {
			try {
				verifyName();
			} catch (CoreException ce) {
				refreshStatus();
				return;				
			}
						
			getConfiguration().rename(getNameTextWidget().getText().trim());
			refreshStatus();
		}
	}
	
	protected Display getDisplay() {
		Shell shell = getShell();
		if (shell != null) {
			return shell.getDisplay();
		} else {
			return Display.getDefault();
		}
	}

	class ToolListElement {
		private ICToolType fToolType;
		public ToolListElement(Object o) {
			fToolType = (ICToolType) o;
		}
		public String toString() {
			return fToolType.getName();
		}
	}

	class ToolListContentProvider implements IStructuredContentProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			Map types = BuilderPlugin.getDefault().getToolTypes();
			Object[] objs = new Object[types.size()];
			int i = 0;
			for (Iterator iter = types.entrySet().iterator(); iter.hasNext(); i++) {
				Map.Entry element = (Map.Entry) iter.next();
				objs[i] = new ToolListElement(element.getValue());
			}
			return objs;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(
			Viewer viewer,
			Object oldInput,
			Object newInput) {
		}

	}

	class ToolListLabelProvider extends LabelProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
		 */
		public String getText(Object element) {
			return super.getText(element);
		}

	}
		
	/**
	 * Creates the build configuration selection area of the dialog.
	 * This area displays a list of available tools that the user
	 * may select.
	 * 
	 * @return the composite used for tools configuration selection area
	 */ 
	protected Composite createToolSelectionArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setSelectionArea(comp);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 5;
		comp.setLayout(layout);
		
		Label treeLabel = new Label(comp, SWT.NONE);
		treeLabel.setText("Build Tools"); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = 3;
		treeLabel.setLayoutData(gd);
		
		ListViewer list = new ListViewer(comp);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		// Set width hint to 0 to force tree to only be as wide as the combined
		// width of the 'New' & 'Delete' buttons.  Otherwise tree wants to be much wider.
		gd.widthHint = 0;
		list.getControl().setLayoutData(gd);
		list.setContentProvider(new ToolListContentProvider());
		list.setLabelProvider(new ToolListLabelProvider());
		list.setSorter(new WorkbenchViewerSorter());
		setListViewer(list);
		list.setInput(ResourcesPlugin.getWorkspace().getRoot());
		
		return comp;
	}	
	
	/**
	 * Creates the build configuration edit area of the dialog.
	 * This area displays the name of the build configuration
	 * currently being edited, as well as a tab folder of tabs
	 * that are applicable to the currently selected tool.
	 * 
	 * @return the composite used for build configuration editing
	 */ 
	protected Composite createEditArea(Composite parent) {
		Composite outerComp = new Composite(parent, SWT.NONE);
		GridLayout outerCompLayout = new GridLayout();
		outerCompLayout.numColumns = 1;
		outerCompLayout.marginHeight = 0;
		outerCompLayout.marginWidth = 0;
		outerComp.setLayout(outerCompLayout);
		
		Composite comp = new Composite(outerComp, SWT.NONE);
		setEditArea(comp);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 5;
		comp.setLayout(layout);		
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);
		
		Label nameLabel = new Label(comp, SWT.HORIZONTAL | SWT.LEFT);
		nameLabel.setText("&Name");
		gd = new GridData(GridData.BEGINNING);
		nameLabel.setLayoutData(gd);
		
		Text nameText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		nameText.setLayoutData(gd);
		setNameTextWidget(nameText);
		
		getNameTextWidget().addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					updateConfigFromName();
				}
			}
		);		
		
		Label spacer = new Label(comp, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 2;
		spacer.setLayoutData(gd);
		
		fTabComposite = new Composite(comp, SWT.NONE);
		GridLayout outerTabCompositeLayout = new GridLayout();
		outerTabCompositeLayout.marginHeight = 0;
		outerTabCompositeLayout.marginWidth = 0;
		fTabComposite.setLayout(outerTabCompositeLayout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		fTabComposite.setLayoutData(gd);		
		
		TabFolder tabFolder = new TabFolder(fTabComposite, SWT.NONE);
		setTabFolder(tabFolder);
		gd = new GridData(GridData.FILL_BOTH);
		tabFolder.setLayoutData(gd);
		getTabFolder().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleTabSelected();
			}
		});
		
		Composite buttonComp = new Composite(comp, SWT.NONE);
		GridLayout buttonCompLayout = new GridLayout();
		buttonCompLayout.numColumns = 2;
		buttonComp.setLayout(buttonCompLayout);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = 2;
		buttonComp.setLayoutData(gd);
		
		setApplyButton(new Button(buttonComp, SWT.PUSH));
		getApplyButton().setText("&Apply");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		getApplyButton().setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(getApplyButton());
		getApplyButton().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleApplyPressed();
			}
		});
		
		setRevertButton(new Button(buttonComp, SWT.PUSH));
		getRevertButton().setText("Revert");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		getRevertButton().setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(getRevertButton());
		getRevertButton().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleRevertPressed();
			}
		});
		
		return outerComp;
	}	
	
	/**
	 * @see Dialog#createButtonBar(Composite)
	 */
	protected Control createButtonBar(Composite parent) {
		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout pmLayout = new GridLayout();
		pmLayout.numColumns = 3;

		return super.createButtonBar(composite);
	}
	
	/**
	 * Sets the title for the dialog, and establishes the help context.
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(("Tools"));
		WorkbenchHelp.setHelp(
			shell,
			CUIPlugin.getPluginId() + ".build_configuration_dialog");
	}
	
	/**
	 * @see Window#getInitialLocation(Point)
	 */
	protected Point getInitialLocation(Point initialSize) {	
		String locationString = getPreferenceStore().getString(DIALOG_LOCATION);
		if (locationString.length() > 0) {
			Point locationPoint = parseCoordinates(locationString);
			if (locationPoint != null) {
				return locationPoint;
			}
		}
		return super.getInitialLocation(initialSize);
	}

	/**
	 * @see Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		String sizeString = getPreferenceStore().getString(DIALOG_SIZE);
		if (sizeString.length() > 0) {
			Point sizePoint = parseCoordinates(sizeString);
			if (sizePoint != null) {
				return sizePoint;
			}
		}
		return DEFAULT_INITIAL_DIALOG_SIZE;
	}
	
	/**
	 * Given a coordinate String of the form "123x456" return a Point object whose
	 * X value is 123 and Y value is 456.  Return <code>null</code> if the String
	 * is not in the specified form.
	 */
	protected Point parseCoordinates(String coordString) {
		int byIndex = coordString.indexOf('x');
		if (byIndex < 0) {
			return null;
		}
		
		try {
			int x = Integer.parseInt(coordString.substring(0, byIndex));
			int y = Integer.parseInt(coordString.substring(byIndex + 1));			
			return new Point(x, y);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}
	
	/**
	 * Given a Point object, return a String of the form "XCoordxYCoord".
	 */
	protected String serializeCoords(Point coords) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(coords.x);
		buffer.append('x');
		buffer.append(coords.y);
		return buffer.toString();
	}
	
	private void setSashForm(SashForm sashForm) {
		fSashForm = sashForm;
	}
	
	protected SashForm getSashForm() {
		return fSashForm;
	}

	/**
	 * Sets the tree viewer used to display build configurations.
	 * 
	 * @param viewer the tree viewer used to display tool tabs
	 */
	private void setListViewer(ListViewer viewer) {
		fToolList = viewer;
	}
	
	/**
	 * Returns the tree viewer used to display tool tabs
	 * 
	 * @param the tree viewer used to display tool tabs
	 */
	protected ListViewer getListViewer() {
		return fToolList;
	}
	
	protected IStructuredSelection getListViewerSelection() {
		return (IStructuredSelection)getListViewer().getSelection();
	}
	
	protected Object getListViewerFirstSelectedElement() {
		IStructuredSelection selection = getListViewerSelection();
		if (selection == null) {
			return null;
		}
		return selection.getFirstElement();
	}
		
	/**
	 * Returns the build manager.
	 * 
	 * @return the build manager
	 */
	protected ICBuildConfigManager getBuildConfigManager() {
		return BuilderPlugin.getDefault().getBuildConfigurationManager();
	}

	/**
	 * Returns whether this dialog is currently open
	 */
	protected boolean isVisible() {
		return getListViewer() != null;
	}	

	/**
	 * Utility method with conventions
	 */
	protected void errorDialog(Shell shell, String title, String message, Throwable t) {
		CUIPlugin.getDefault().log(t);
		IStatus status;
		if (t instanceof CoreException) {
			status= ((CoreException)t).getStatus();
			// if the 'message' resource string and the IStatus' message are the same,
			// don't show both in the dialog
			if (status != null && message.equals(status.getMessage())) {
				message= null;
			}
		} else {
			status= new Status(IStatus.ERROR, CUIPlugin.getPluginId(), IStatus.ERROR, "Error within plugin UI: ", t); //$NON-NLS-1$	
		}
		ErrorDialog.openError(shell, title, message, status);
	}
 	
 	/**
 	 * Sets the configuration to display/edit.
 	 * Updates the tab folder to contain the appropriate pages.
 	 * Sets all configuration-related state appropriately.
 	 * 
 	 * @param config the build configuration to display/edit
 	 * @param init whether to initialize the config with default values
 	 */
 	protected void setBuildConfiguration(ICBuildConfig config, boolean init) {
		try {
			
			// turn on initializing flag to ignore message updates
			setInitializingTabs(true);
			
			getEditArea().setVisible(true);
			
			if (config.isWorkingCopy()) {
		 		setWorkingCopy((ICBuildConfigWorkingCopy)config);
			} else {
				setWorkingCopy(config.getWorkingCopy());
			}
			fUnderlyingConfig = getConfiguration().getOriginal();
	 		
	 		// update the name field before to avoid verify error 
	 		getNameTextWidget().setText(config.getName());	 		
	 			 		
	 		// Set the defaults for all tabs before any are initialized
	 		// so that every tab can see ALL the default values
	 		if (init) {
				getTabGroup().setDefaults(getConfiguration());
	 		}

	 		// update the tabs with the new working copy	 		
			getTabGroup().initializeFrom(getConfiguration());
	 		
	 		// update the name field after in case client changed it 
	 		getNameTextWidget().setText(config.getName());
	 				
	 		// turn off initializing flag to update message
			setInitializingTabs(false);
			
	 		refreshStatus();
	 		
		} catch (CoreException ce) {
 			errorDialog(getShell(), "Error", "Exception occurred setting build configuration", ce);
 			clearBuildConfiguration();
 			return;					
		}
 	} 
 	
 	/**
 	 * Clears the configuration being shown/edited.   
 	 * Resets all configuration-related state.
 	 */
 	protected void clearBuildConfiguration() {
 		setWorkingCopy(null);
 		fUnderlyingConfig = null;
 		setLastSavedName(null);
 		getNameTextWidget().setText("");  //$NON-NLS-1$
 		refreshStatus();
 	}
 	
 	/**
 	 * Populate the tabs in the configuration edit area to be appropriate to the current
 	 * build configuration type.
 	 */
 	protected void showTabsForConfigType(ICToolType configType) {		
 		
 		// Don't do any work if the current tabs are for the current config type
 		if (getTabType() != null && getTabType().equals(configType)) {
 			return;
 		}
 		
 		// Avoid flicker
 		getEditArea().setVisible(false);
 		
		// Dispose the current tabs
		disposeExistingTabs();

		// Build the new tabs
 		ICToolTabGroup group = null;
 		try {
	 		group = createGroup(configType);
 		} catch (CoreException ce) {
 			errorDialog(getShell(), "Error", "Exception occurred creating build configuration tabs",ce);
 			return;
 		}
 		
 		// Create the Control for each tab, and determine the maximum tab dimensions
 		PixelConverter pixelConverter = new PixelConverter(getTabFolder());
 		int runningTabWidth = 0;
 		ICToolTab[] tabs = group.getTabs();
 		Point contentSize = new Point(0, 0);
 		for (int i = 0; i < tabs.length; i++) {
 			TabItem tab = new TabItem(getTabFolder(), SWT.NONE);
 			String name = tabs[i].getName();
 			if (name == null) {
 				name = "unspecified";
 			}
 			tab.setText(name);
 			Image image = tabs[i].getImage();
 			tab.setImage(image);
 			runningTabWidth += pixelConverter.convertWidthInCharsToPixels(name.length() + 5);
 			if (image != null) {
	 			runningTabWidth += image.getBounds().width;
 			}
 			tabs[i].createControl(tab.getParent());
 			Control control = tabs[i].getControl();
 			if (control != null) {
	 			tab.setControl(control);
	 			Point size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	 			if (size.x > contentSize.x) {
	 				contentSize.x = size.x;
	 			}
	 			if (size.y > contentSize.y) {
	 				contentSize.y = size.y;
	 			}
 			}
 		}
 		
 		// Determine if more space is needed to show all tab labels across the top of the
 		// tab folder.  If so, only increase size of dialog to some percent of the available
 		// screen real estate.
		if (runningTabWidth > contentSize.x) {
			int maxAllowedWidth = (int) (getDisplay().getBounds().width * MAX_DIALOG_WIDTH_PERCENT);
			int otherWidth = getSashForm().SASH_WIDTH + getSelectionArea().getBounds().width;
			int totalWidth = runningTabWidth + otherWidth;
			if (totalWidth > maxAllowedWidth) {
				contentSize.x = maxAllowedWidth - otherWidth;
			} else {
				contentSize.x = runningTabWidth;
			} 
		}
 		
 		// Adjust the maximum tab dimensions to account for the extra space required for the tab labels
 		Rectangle tabFolderBoundingBox = getTabFolder().computeTrim(0, 0, contentSize.x, contentSize.y);
		contentSize.x = tabFolderBoundingBox.width;
		contentSize.y = tabFolderBoundingBox.height;

 		// Force recalculation of sizes	
		getTabFolder().layout(true);
		
		// Calculate difference between required space for tab folder and current size,
		// then increase size of this dialog's Shell by that amount
 		Rectangle rect = fTabComposite.getClientArea();
		Point containerSize= new Point(rect.width, rect.height);
		int hdiff= contentSize.x - containerSize.x;
		int vdiff= contentSize.y - containerSize.y;
		// Only increase size of dialog, never shrink it
		if (hdiff > 0 || vdiff > 0) {
			int[] newSashWeights = null;
			if (hdiff > 0) {			
				newSashWeights = calculateNewSashWeights(hdiff);				
			}
			hdiff= Math.max(0, hdiff);
			vdiff= Math.max(0, vdiff);
			Shell shell= getShell();
			Point shellSize= shell.getSize();
			setShellSize(shellSize.x + hdiff, shellSize.y + vdiff);
			// Adjust the sash weights so that all of the increase in width
			// is given to the tab area
			if (newSashWeights != null) {
				getSashForm().setWeights(newSashWeights);
			}
		}

 		setTabGroup(group);
 		setTabType(configType);
 		getEditArea().setVisible(true);
 	}
 	
 	/**
 	 * Calculate & return a 2 element integer array that specifies the relative 
 	 * weights of the selection area and the edit area, based on the specified
 	 * increase in width of the owning shell.  The point of this method is calculate 
 	 * sash weights such that when the shell gets wider, all of the increase in width
 	 * is given to the edit area (tab folder), and the selection area (tree) stays
 	 * the same width.
 	 */
	protected int[] calculateNewSashWeights(int widthIncrease) {
		int[] newWeights = new int[2];
		newWeights[0] = getSelectionArea().getBounds().width;
		newWeights[1] = getEditArea().getBounds().width + widthIncrease;
		return newWeights;
	}

 	/**
 	 * Increase the size of this dialog's <code>Shell</code> by the specified amounts.
 	 * Do not increase the size of the Shell beyond the bounds of the Display.
 	 */
	private void setShellSize(int width, int height) {
		Rectangle bounds = getShell().getDisplay().getBounds();
		getShell().setSize(Math.min(width, bounds.width), Math.min(height, bounds.height));
	}
	
 	protected void disposeExistingTabs() {
		setDisposingTabs(true);
		TabItem[] oldTabs = getTabFolder().getItems();
		for (int i = 0; i < oldTabs.length; i++) {
			oldTabs[i].dispose();
		} 		
		if (getTabGroup() != null) {
			getTabGroup().dispose();
		}
		setTabGroup(null);
		setTabType(null);
		setDisposingTabs(false);
 	}
 	
 	/**
 	 * Sets the current build configuration that is being
 	 * displayed/edited.
 	 */
 	protected void setWorkingCopy(ICBuildConfigWorkingCopy workingCopy) {
 		fWorkingCopy = workingCopy;
 	}
 	
 	protected boolean isWorkingCopyDirty() {
 		ICBuildConfigWorkingCopy workingCopy = getConfiguration();
 		if (workingCopy == null) {
 			return false;
 		}
 		
 		// Working copy hasn't been saved
 		if (workingCopy.getOriginal() == null) {
 			return true;
 		}
 		
 		// Name has changed.  Normally, this would be caught in the 'contentsEqual'
 		// check below, however there are some circumstances where this fails, such as
 		// when the name is invalid
 		if (isNameDirty()) {
 			return true;
 		}

 		updateWorkingCopyFromPages();
 		ICBuildConfig original = workingCopy.getOriginal();
 		return !original.contentsEqual(workingCopy);
 	}
 	
 	/**
 	 * Return <code>true</code> if the name has been modified since the last time it was saved.
 	 */
 	protected boolean isNameDirty() {
 		String currentName = getNameTextWidget().getText().trim();
 		return !currentName.equals(getLastSavedName());
 	}
 	
	/**
	 * Sets the text widget used to display the name
	 * of the configuration being displayed/edited
	 * 
	 * @param widget the text widget used to display the name
	 *  of the configuration being displayed/edited
	 */
	private void setNameTextWidget(Text widget) {
		fNameText = widget;
	}
	
	/**
	 * Returns the text widget used to display the name
	 * of the configuration being displayed/edited
	 * 
	 * @return the text widget used to display the name
	 *  of the configuration being displayed/edited
	 */
	protected Text getNameTextWidget() {
		return fNameText;
	} 
	
 	/**
 	 * Sets the 'apply' button.
 	 * 
 	 * @param button the 'apply' button.
 	 */	
 	private void setApplyButton(Button button) {
 		fApplyButton = button;
 	}
 	
 	/**
 	 * Returns the 'apply' button
 	 * 
 	 * @return the 'apply' button
 	 */
 	protected Button getApplyButton() {
 		return fApplyButton;
 	}	
 	
 	/**
 	 * Sets the 'revert' button.
 	 * 
 	 * @param button the 'revert' button.
 	 */	
 	private void setRevertButton(Button button) {
 		fRevertButton = button;
 	}
 	
 	/**
 	 * Returns the 'revert' button
 	 * 
 	 * @return the 'revert' button
 	 */
 	protected Button getRevertButton() {
 		return fRevertButton;
 	}	
 	
 	private void setDisposingTabs(boolean disposing) {
 		fDisposingTabs = disposing;
 	}
 	
 	private boolean isDisposingTabs() {
 		return fDisposingTabs;
 	}
 
 	/**
 	 * Sets the tab folder
 	 * 
 	 * @param folder the tab folder
 	 */	
 	private void setTabFolder(TabFolder folder) {
 		fTabFolder = folder;
 	}
 	
 	/**
 	 * Returns the tab folder
 	 * 
 	 * @return the tab folder
 	 */
 	protected TabFolder getTabFolder() {
 		return fTabFolder;
 	}	 	
 	
 	/**
 	 * Sets the current tab group being displayed
 	 * 
 	 * @param group the current tab group being displayed
 	 */
 	private void setTabGroup(ICToolTabGroup group) {
 		fTabGroup = group;
 	}
 	
 	/**
 	 * Returns the current tab group
 	 * 
 	 * @return the current tab group, or <code>null</code> if none
 	 */
 	public ICToolTabGroup getTabGroup() {
 		return fTabGroup;
 	}
 	
 	/**
 	 * @see ICBuildConfigDialog#getTabs()
 	 */
 	public ICToolTab[] getTabs() {
 		if (getTabGroup() == null) {
 			return null;
 		} else {
 			return getTabGroup().getTabs();
 		}
 	} 	
 	
	protected void setIgnoreSelectionChanges(boolean ignore) {
		fIgnoreSelectionChanges = ignore;
	}
	
	protected boolean ignoreSelectionChanges() {
		return fIgnoreSelectionChanges;
	}
	
	/**
	 * Return whether the current configuration can be discarded.  This involves determining
	 * if it is dirty, and if it is, asking the user what to do.
	 */
	protected boolean canDiscardCurrentConfig() {		
		// If there is no working copy, there's no problem, return true
		ICBuildConfigWorkingCopy workingCopy = getConfiguration();
		if (workingCopy == null) {
			return true;
		}
		
		if (isWorkingCopyDirty()) {
			return showUnsavedChangesDialog();
		} else {
			return true;
		}
	}
	
	/**
	 * Show the user a dialog appropriate to whether the unsaved changes in the current config
	 * can be saved or not.  Return <code>true</code> if the user indicated that they wish to replace
	 * the current config, either by saving changes or by discarding the, return <code>false</code>
	 * otherwise.
	 */
	protected boolean showUnsavedChangesDialog() {
		if (canSaveConfig()) {
			return showSaveChangesDialog();
		} else {
			return showDiscardChangesDialog();
		}
	}
	
	/**
	 * Create and return a dialog that asks the user whether they want to save
	 * unsaved changes.  Return <code>true </code> if they chose to save changes,
	 * <code>false</code> otherwise.
	 */
	protected boolean showSaveChangesDialog() {
		StringBuffer buffer = new StringBuffer("The configuration '");
		buffer.append(getConfiguration().getName());
		buffer.append("' has unsaved changes. Do you wish to save them?");
		MessageDialog dialog = new MessageDialog(getShell(), 
												 ("Save changes?"),
												 null,
												 buffer.toString(),
												 MessageDialog.QUESTION,
												 new String[] {"Yes", "No", "Cancel"},
												 0);
		// If user clicked 'Cancel' or closed dialog, return false
		int selectedButton = dialog.open();
		if ((selectedButton < 0) || (selectedButton == 2)) {
			return false;
		}
		
		// If they hit 'Yes', save the working copy 
		if (selectedButton == 0) {
			saveConfig();
		}
		
		return true;
	}
	
	/**
	 * Create and return a dialog that asks the user whether they want to discard
	 * unsaved changes.  Return <code>true</code> if they chose to discard changes,
	 * <code>false</code> otherwise.
	 */
	protected boolean showDiscardChangesDialog() {
		StringBuffer buffer = new StringBuffer("The configuration '");
		buffer.append(getNameTextWidget().getText());
		buffer.append("' has unsaved changes that CANNOT be saved because of the following error:");
		buffer.append(fCantSaveErrorMessage);
		buffer.append("Do you wish to discard changes?");
		MessageDialog dialog = new MessageDialog(getShell(), 
												 "Discard changes?",
												 null,
												 buffer.toString(),
												 MessageDialog.QUESTION,
												 new String[] {"Yes", "No"},
												 1);
		// If user clicked 'Yes', return true
		int selectedButton = dialog.open();
		if (selectedButton == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Return <code>true</code> if the current configuration can be saved, <code>false</code>
	 * otherwise.  Note this is NOT the same thing as the config simply being valid.  It is
	 * possible to save a config that does not validate.  This method determines whether the
	 * config can be saved without causing a serious error.  For example, a shared config that
	 * has no specified location would cause this method to return <code>false</code>.
	 */
	protected boolean canSaveConfig() {
		
		fCantSaveErrorMessage = null;

		// First make sure that name doesn't prevent saving the config
		try {
			verifyName();
		} catch (CoreException ce) {
			fCantSaveErrorMessage = ce.getStatus().getMessage();
			return false;
		}
		
		// Next, make sure none of the tabs object to saving the config
		ICToolTab[] tabs = getTabs();
		if (tabs == null) {
			fCantSaveErrorMessage = "No tabs found";
			return false;
		}
		for (int i = 0; i < tabs.length; i++) {
			if (!tabs[i].canSave()) {
				fCantSaveErrorMessage = tabs[i].getErrorMessage();
				return false;
			}
		}
		return true;		
	}

	/**
	 * Notification the 'Close' button has been pressed.
	 */
	protected void handleClosePressed() {
		if (canDiscardCurrentConfig()) {
			disposeExistingTabs();
			cancelPressed();
		}
	}
	
	/**
	 * Notification that the 'Apply' button has been pressed
	 */
	protected void handleApplyPressed() {
		saveConfig();
		getListViewer().setSelection(new StructuredSelection(fUnderlyingConfig));
	}
	
	/**
	 * Notification that the 'Revert' button has been pressed
	 */
	protected void handleRevertPressed() {
		setBuildConfiguration(getConfiguration().getOriginal(), false);
	}
	
	protected void saveConfig() {
		try {
			// trim name
			Text widget = getNameTextWidget();
			widget.setText(widget.getText().trim());
			doSave();
		} catch (CoreException e) {
			errorDialog(getShell(), "Error", "Exception occurred while saving build configuration", e);
			return;
		}
		
		updateButtons();		
	}
	
	/**
	 * Notification that a tab has been selected
	 * 
	 * Disallow tab changing when the current tab is invalid.
	 * Update the config from the tab being left, and refresh
	 * the tab being entered.
	 */
	protected void handleTabSelected() {
		if (isDisposingTabs()) {
			return;
		}
		ICToolTab[] tabs = getTabs();
		if (fCurrentTabIndex == getTabFolder().getSelectionIndex() || tabs == null || tabs.length == 0 || fCurrentTabIndex > (tabs.length - 1)) {
			return;
		}
		if (fCurrentTabIndex != -1) {
			ICToolTab tab = tabs[fCurrentTabIndex];
			ICBuildConfigWorkingCopy wc = getConfiguration();
			if (wc != null) {
				// apply changes when leaving a tab
				tab.performApply(getConfiguration());
				// re-initialize a tab when entering it
				getActiveTab().initializeFrom(wc);
			}
		}
		fCurrentTabIndex = getTabFolder().getSelectionIndex();
		refreshStatus();
	}	
	
	/**
	 * Iterate over the pages to update the working copy
	 */
	protected void updateWorkingCopyFromPages() {
		ICBuildConfigWorkingCopy workingCopy = getConfiguration();
		if (getTabGroup() != null) {
			getTabGroup().performApply(workingCopy);
		}
	}
	
	/**
	 * Do the save
	 */
	protected void doSave() throws CoreException {
		ICBuildConfigWorkingCopy workingCopy = getConfiguration();
		updateWorkingCopyFromPages();
		if (isWorkingCopyDirty()) {
			fUnderlyingConfig = workingCopy.doSave();
			setWorkingCopy(fUnderlyingConfig.getWorkingCopy());
			setLastSavedName(fUnderlyingConfig.getName()); 
		}
	}

	protected IPreferenceStore getPreferenceStore() {
		return CUIPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Sets the given cursor for all shells currently active
	 * for this window's display.
	 *
	 * @param cursor the cursor
	 */
	private void setDisplayCursor(Cursor cursor) {
		Shell[] shells = getShell().getDisplay().getShells();
		for (int i = 0; i < shells.length; i++)
			shells[i].setCursor(cursor);
	}
	
	/**
	 * Checks whether it is alright to close this dialog
	 * and performed standard cancel processing. If there is a
	 * long running operation in progress, this method posts an
	 * alert message saying that the dialog cannot be closed.
	 * 
	 * @return <code>true</code> if it is alright to close this dialog, and
	 *  <code>false</code> if it is not
	 */
	private boolean okToClose() {
		if (fActiveRunningOperations > 0) {
			synchronized (this) {
				fWindowClosingDialog = createDialogClosingDialog();
			}	
			fWindowClosingDialog.open();
			synchronized (this) {
				fWindowClosingDialog = null;
			}
			return false;
		}
		
		return true;
	}

	/**
	 * Creates and return a new wizard closing dialog without opening it.
	 */ 
	private MessageDialog createDialogClosingDialog() {
		MessageDialog result= new MessageDialog(
			getShell(),
			JFaceResources.getString("WizardClosingDialog.title"), //$NON-NLS-1$
			null,
			JFaceResources.getString("WizardClosingDialog.message"), //$NON-NLS-1$
			MessageDialog.QUESTION,
			new String[] {IDialogConstants.OK_LABEL},
			0 ); 
		return result;
	}
	
	protected ICBuildConfigWorkingCopy getConfiguration() {
		return fWorkingCopy;
	}

	/**
	 * @see ICBuildConfigDialog#updateButtons()
	 */
	public void updateButtons() {
		if (isInitializingTabs()) {
			return;
		}
		
		// Get the current selection
		IStructuredSelection sel = (IStructuredSelection)getListViewer().getSelection();
		boolean singleSelection = sel.size() == 1;
		boolean firstItemConfig = sel.getFirstElement() instanceof ICBuildConfig;
		boolean firstItemConfigType = sel.getFirstElement() instanceof ICTool;
		
		// Apply & Launch buttons
		if (sel.isEmpty()) {
			getApplyButton().setEnabled(false);
		} else {
			getApplyButton().setEnabled(true);
		}
		
		// Revert button
		if (sel.isEmpty() || sel.size() > 1) {
			getRevertButton().setEnabled(false);
		} else {
			if (firstItemConfig && isWorkingCopyDirty()) {
				getRevertButton().setEnabled(true);
			} else {
				getRevertButton().setEnabled(false);
			}
		}
	}
	
	/**
	 * @see ICBuildConfigDialog#getActiveTab()
	 */
	public ICToolTab getActiveTab() {
		TabFolder folder = getTabFolder();
		ICToolTab[] tabs = getTabs();
		if (folder != null && tabs != null) {
			int pageIndex = folder.getSelectionIndex();
			if (pageIndex >= 0) {		
				return tabs[pageIndex];		
			}
		}
		return null;
	}
	
	/**
	 * Returns the currently active TabItem
	 * 
	 * @return build configuration tab item
	 */
	protected TabItem getActiveTabItem() {
		TabFolder folder = getTabFolder();
		TabItem tabItem = null;
		int selectedIndex = folder.getSelectionIndex();
		if (selectedIndex >= 0) {
			tabItem = folder.getItem(selectedIndex);
		}		
		return tabItem;
	}

	/**
	 * @see ICBuildConfigDialog#updateMessage()
	 */
	public void updateMessage() {
		if (isInitializingTabs()) {
			return;
		}
		
		// If there is no current working copy, show a default informational message and clear the error message
		if (getConfiguration() == null) {
			setErrorMessage(null);
			setMessage("Select a type of configuration to create, and press 'new'");
			return;
		}
		
		try {
			verifyStandardAttributes();
		} catch (CoreException ce) {
			setErrorMessage(ce.getMessage());
			return;
		}
		
		// Get the active tab.  If there isn't one, clear the informational & error messages
		ICToolTab activeTab = getActiveTab();
		if (activeTab == null) {
			setMessage(null);
			setErrorMessage(null);
			return;
		}
		
		// Always set the informational (non-error) message based on the active tab		
		setMessage(activeTab.getMessage());
		
		// The bias is to show the active page's error message, but if there isn't one,
		// show the error message for one of the other tabs that has an error.  Set the icon
		// for all tabs according to whether they contain errors.
		String errorMessage = checkTabForError(activeTab);
		boolean errorOnActiveTab = errorMessage != null;
		setTabIcon(getActiveTabItem(), errorOnActiveTab, activeTab);
		
		ICToolTab[] allTabs = getTabs();
		for (int i = 0; i < allTabs.length; i++) {
			if (getTabFolder().getSelectionIndex() == i) {
				continue;
			}
			String tabError = checkTabForError(allTabs[i]);				
			TabItem tabItem = getTabFolder().getItem(i);
			boolean errorOnTab = tabError != null;
			setTabIcon(tabItem, errorOnTab, allTabs[i]);
			if (errorOnTab && !errorOnActiveTab) {
				errorMessage = '[' + removeAmpersandsFrom(tabItem.getText()) + "]: " + tabError; //$NON-NLS-1$
			}
		}
		setErrorMessage(errorMessage);				
	}
	
	/**
	 * Force the tab to update it's error state and return any error message.
	 */
	protected String checkTabForError(ICToolTab tab) {
		tab.isValid(getConfiguration());
		return tab.getErrorMessage();
	}
	
	/**
	 * Set the specified tab item's icon to an error icon if <code>error</code> is true,
	 * or a transparent icon of the same size otherwise.
	 */
	protected void setTabIcon(TabItem tabItem, boolean error, ICToolTab tab) {
		Image image = null;
		if (error) {			
			image = tab.getImage(); /* CBuildConfigManager.getErrorTabImage(tab) */
		} else {
			image = tab.getImage();
		}
		tabItem.setImage(image);								
	}

	/**
	 * Return a copy of the specified string 
	 */
	protected String removeAmpersandsFrom(String string) {
		String newString = new String(string);
		int index = newString.indexOf('&');
		while (index != -1) {
			newString = string.substring(0, index) + newString.substring(index + 1, newString.length());
			index = newString.indexOf('&');
		}
		return newString;
	}

	/**
	 * Returns the build configuration selection area control.
	 * 
	 * @return control
	 */
	protected Composite getSelectionArea() {
		return fSelectionArea;
	}

	/**
	 * Sets the build configuration selection area control.
	 * 
	 * @param editArea control
	 */
	private void setSelectionArea(Composite selectionArea) {
		fSelectionArea = selectionArea;
	}

	/**
	 * Returns the build configuration edit area control.
	 * 
	 * @return control
	 */
	protected Composite getEditArea() {
		return fEditArea;
	}

	/**
	 * Sets the build configuration edit area control.
	 * 
	 * @param editArea control
	 */
	private void setEditArea(Composite editArea) {
		fEditArea = editArea;
	}

	/**
	 * Returns the type that tabs are currently displayed
	 * for, or <code>null</code> if none.
	 * 
	 * @return build configuration type or <code>null</code>
	 */
	protected ICToolType getTabType() {
		return fToolType;
	}

	/**
	 * Sets the type that tabs are currently displayed
	 * for, or <code>null</code> if none.
	 * 
	 * @param tabType build configuration type
	 */
	private void setTabType(ICToolType tabType) {
		fToolType = tabType;
	}

	protected Object getSelectedTreeObject() {
		return fSelectedTreeObject;
	}
	
	protected void setSelectedTreeObject(Object obj) {
		fSelectedTreeObject = obj;
	}	
	
	/**
	 * @see ICBuildConfigDialog#setName(String)
	 */
	public void setName(String name) {
		if (isVisible()) {
			if (name == null) {
				name = ""; //$NON-NLS-1$
			}
			fNameText.setText(name.trim());
			refreshStatus();
		}
	}
	
	/**
	 * Sets whether this dialog is initializing pages
	 * and should not bother to refresh status (butttons
	 * and message).
	 */
	private void setInitializingTabs(boolean init) {
		fInitializingTabs = init;
	}
	
	/**
	 * Returns whether this dialog is initializing pages
	 * and should not bother to refresh status (butttons
	 * and message).
	 */
	protected boolean isInitializingTabs() {
		return fInitializingTabs;
	}	
}
