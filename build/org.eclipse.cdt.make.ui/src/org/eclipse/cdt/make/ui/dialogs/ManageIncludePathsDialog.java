/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.make.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeScannerInfo;
import org.eclipse.cdt.make.core.scannerconfig.DiscoveredScannerInfo;
import org.eclipse.cdt.make.core.scannerconfig.DiscoveredScannerInfoProvider;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerInfoCollector;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.MessageLine;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**
 * Manage include paths dialog
 *  
 * @author vhirsl
 */
public class ManageIncludePathsDialog extends Dialog {
	private static final String PREF_INCLUDES = "ScannerIncludes"; //$NON-NLS-1$

	private static final String PREFIX = "ManageIncludePathsDialog"; //$NON-NLS-1$
	private static final String DIALOG_TITLE = PREFIX + ".title"; //$NON-NLS-1$
	private static final String USER_GROUP = PREFIX + ".userGroup.title"; //$NON-NLS-1$
	private static final String NEW = "BuildPropertyCommon.label.new"; //$NON-NLS-1$
	private static final String EDIT = "BuildPropertyCommon.label.edit"; //$NON-NLS-1$
	private static final String REMOVE = "BuildPropertyCommon.label.remove"; //$NON-NLS-1$
	private static final String UP = "BuildPropertyCommon.label.up"; //$NON-NLS-1$
	private static final String DOWN = "BuildPropertyCommon.label.down"; //$NON-NLS-1$

	private static final String BROWSE = "BuildPathInfoBlock.browse"; //$NON-NLS-1$
	private static final String PATH_TITLE = BROWSE + ".path"; //$NON-NLS-1$
	private static final String EDIT_PATH_TITLE = BROWSE + ".path.edit"; //$NON-NLS-1$
	private static final String PATH_LABEL = BROWSE + ".path.label"; //$NON-NLS-1$

	private static final String DISCOVERED_GROUP = PREFIX + ".discoveredGroup.title"; //$NON-NLS-1$
	
	private static final String DISC_COMMON_PREFIX = "ManageScannerConfigDialogCommon"; //$NON-NLS-1$
	private static final String SELECTED_LABEL = DISC_COMMON_PREFIX + ".discoveredGroup.selected.label"; //$NON-NLS-1$
	private static final String REMOVED_LABEL = DISC_COMMON_PREFIX + ".discoveredGroup.removed.label"; //$NON-NLS-1$
	private static final String REMOVE_DISCOVERED = DISC_COMMON_PREFIX + ".discoveredGroup.buttons.remove.label"; //$NON-NLS-1$
	private static final String RESTORE_DISCOVERED = DISC_COMMON_PREFIX + ".discoveredGroup.buttons.restore.label"; //$NON-NLS-1$
	private static final String DELETE_DISCOVERED = DISC_COMMON_PREFIX + ".discoveredGroup.buttons.delete.label"; //$NON-NLS-1$
	private static final String DELETE_ALL_DISCOVERED = DISC_COMMON_PREFIX + ".discoveredGroup.buttons.deleteAll.label"; //$NON-NLS-1$

	private static final int PROJECT_LIST_MULTIPLIER = 15;
	private static final int INITIAL_LIST_WIDTH = 40;

	private static final int ACTIVE = 0;
	private static final int REMOVED = 1;
	
	private static final int DO_REMOVE = 0;
	private static final int DO_RESTORE = 1;
	
	private ArrayList returnPaths;
	private ArrayList userPaths;
	private LinkedHashMap discoveredPaths;
	private LinkedHashMap workingDiscoveredPaths; // working copy of discoveredPaths, until either OK or CANCEL is pressed
	private boolean fDirty;
	private boolean fWorkingDirty;
	
	private ICOptionContainer fContainer;
	private IProject fProject;
	private Shell fShell;
	private MessageLine fStatusLine;

	private List userList;
	private Button addPath;
	private Button editPath;
	private Button removePath;
	private Button pathUp;
	private Button pathDown;
	
	private Group discoveredGroup;
	private Label selectedLabel;
	private Label removedLabel;
	private List discActiveList;
	private List discRemovedList;
	private Button removeDiscPath;
	private Button restoreDiscPath;
	private Button deleteDiscPath;
	private Button deleteAllDiscPaths;

	/**
	 * @param parentShell
	 */
	protected ManageIncludePathsDialog(Shell parentShell, ICOptionContainer container) {
		super(parentShell);
		fShell = parentShell;
		fContainer = container;
		fProject = fContainer.getProject();
		DiscoveredScannerInfo scanInfo;
		if (fProject != null) {
			scanInfo = (DiscoveredScannerInfo) DiscoveredScannerInfoProvider.getDefault().getScannerInformation(fProject);
		}
		else {
			scanInfo = new DiscoveredScannerInfo(null);
			MakeScannerInfo makeInfo = new MakeScannerInfo(null);
			Preferences store = MakeCorePlugin.getDefault().getPluginPreferences();
			makeInfo.setIncludePaths(BuildPathInfoBlock.getIncludes(store));
			scanInfo.setUserScannerInfo(makeInfo);
		}
		userPaths = new ArrayList(Arrays.asList(scanInfo.getUserIncludePaths()));
		discoveredPaths = scanInfo.getDiscoveredIncludePaths();
		setDirty(false);
		fDirty = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		newShell.setText(getTitle(DIALOG_TITLE));
		super.configureShell(newShell);
	}

	/**
	 * @return
	 */
	private String getTitle(String title) {
		return MakeUIPlugin.getResourceString(title);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		((GridLayout) composite.getLayout()).numColumns = 3;
		initializeDialogUnits(composite);

		// create message line
		fStatusLine = new MessageLine(composite);
		fStatusLine.setAlignment(SWT.LEFT);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
//		gd.widthHint = convertWidthInCharsToPixels(50);
		fStatusLine.setLayoutData(gd);
		fStatusLine.setMessage(getTitle(DIALOG_TITLE));
		
		createUserControls(composite);
		createOptionsControls(composite);
		createDiscoveredControls(composite);
		
		setListContents();
		userList.select(0);
		enableUserButtons();
		discActiveList.select(0);
		enableDiscoveredButtons();
		
		return composite;
	}

	/**
	 * 
	 */
	private void setListContents() {
		workingDiscoveredPaths = new LinkedHashMap(discoveredPaths);

		userList.setItems((String[]) userPaths.toArray(new String[userPaths.size()]));
		discActiveList.setItems(getDiscIncludePaths(workingDiscoveredPaths, ACTIVE));
		discRemovedList.setItems(getDiscIncludePaths(workingDiscoveredPaths, REMOVED));
	}

	/**
	 * @param discoveredPaths
	 * @return
	 */
	private String[] getDiscIncludePaths(LinkedHashMap dPaths, int type) {
		ArrayList aPaths = new ArrayList();
		boolean compareValue = (type == ACTIVE ? false : true);
		for (Iterator i = dPaths.keySet().iterator(); i.hasNext(); ) {
			String path = (String) i.next();
			if (((Boolean) dPaths.get(path)).booleanValue() == compareValue) {
				aPaths.add(path);
			}
		}
		return (String[]) aPaths.toArray(new String[aPaths.size()]);
	}

	private String[] getIncludes(Preferences prefs) {
		String syms = prefs.getString(PREF_INCLUDES);
		return parseStringToList(syms);
	}

	private String[] parseStringToList(String syms) {
		if (syms != null && syms.length() > 0) {
			StringTokenizer tok = new StringTokenizer(syms, ";"); //$NON-NLS-1$
			ArrayList list = new ArrayList(tok.countTokens());
			while (tok.hasMoreElements()) {
				list.add(tok.nextToken());
			}
			return (String[]) list.toArray(new String[list.size()]);
		}
		return new String[0];
	}

	/**
	 * @param composite
	 */
	private void createUserControls(Composite composite) {
		// Create group
		Group userGroup = ControlFactory.createGroup(composite, getTitle(USER_GROUP), 3);
		((GridData) userGroup.getLayoutData()).horizontalSpan = 2;
		((GridData) userGroup.getLayoutData()).grabExcessHorizontalSpace = false;
		
		// Create list
		userList = new List(userGroup, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		userList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				enableUserButtons();
			}
		});
		userList.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				editUserListItem();
			}
		});

		// Make it occupy the first column
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 1;
		gd.heightHint = getDefaultFontHeight(userList, PROJECT_LIST_MULTIPLIER);
		gd.widthHint = convertWidthInCharsToPixels(INITIAL_LIST_WIDTH);
		userList.setLayoutData(gd);

		// Create buttons
		// Create a composite for the buttons
		Composite pathButtonComp = ControlFactory.createComposite(userGroup, 1);

		// Add the buttons
		addPath = ControlFactory.createPushButton(pathButtonComp, getTitle(NEW));
		addPath.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAddPath();
			}
		});
		addPath.setEnabled(true);
		SWTUtil.setButtonDimensionHint(addPath);

		editPath = ControlFactory.createPushButton(pathButtonComp, getTitle(EDIT));
		editPath.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editUserListItem();
			}
		});
		editPath.setEnabled(true);
		SWTUtil.setButtonDimensionHint(editPath);

		removePath = ControlFactory.createPushButton(pathButtonComp, getTitle(REMOVE));
		removePath.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleRemovePath();
			}
		});
		SWTUtil.setButtonDimensionHint(removePath);

		pathUp = ControlFactory.createPushButton(pathButtonComp, getTitle(UP));
		pathUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handlePathUp();
			}
		});
		SWTUtil.setButtonDimensionHint(pathUp);

		pathDown = ControlFactory.createPushButton(pathButtonComp, getTitle(DOWN));
		pathDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handlePathDown();
			}
		});
		SWTUtil.setButtonDimensionHint(pathDown);
	}

	protected void handleAddPath() {
		// Popup an entry dialog
		InputDialog dialog = new BuildPathInfoBlock.
			SelectPathInputDialog(fShell, getTitle(PATH_TITLE), getTitle(PATH_LABEL), "", null); //$NON-NLS-1$
		String path = null;
		if (dialog.open() == Window.OK) {
			path = dialog.getValue();
			if (path != null && path.length() > 0) {
				setDirty(true);
				userList.add(path);
				userList.select(userList.getItemCount() - 1);
				enableUserButtons();
			}
		}
	}

	/*
	 * Double-click handler to allow edit of path information
	 */
	protected void editUserListItem() {
		// Edit the selection index
		int index = userList.getSelectionIndex();
		if (index != -1) {
			String selItem = userList.getItem(index);
			if (selItem != null) {
				InputDialog dialog = new BuildPathInfoBlock.SelectPathInputDialog(
						fShell, getTitle(EDIT_PATH_TITLE), getTitle(PATH_LABEL), selItem, null);
				String newItem = null;
				if (dialog.open() == Window.OK) {
					newItem = dialog.getValue();
					if (newItem != null && !newItem.equals(selItem)) {
						userList.setItem(index, newItem);
						setDirty(true);
					}
				}
			}
		}
	}

	protected void handleRemovePath() {
		// Get the selection index
		int index = userList.getSelectionIndex();
		if (index == -1) {
			return;
		}

		// Remove the element at that index
		userList.remove(index);
		index = index - 1 < 0 ? 0 : index - 1;
		userList.select(index);
		setDirty(true);

		// Check if the buttons should still be enabled
		enableUserButtons();
	}

	protected void handlePathUp() {
		// Get the selection index
		int index = userList.getSelectionIndex();
		if (index == -1 || index == 0) {
			return;
		}
		// Swap the items in the list
		String selItem = userList.getItem(index);
		userList.remove(index);
		userList.add(selItem, --index);
		setDirty(true);

		// Keep the index selected
		userList.select(index);
		enableUserButtons();
	}

	protected void handlePathDown() {
		// Get the selection index
		int index = userList.getSelectionIndex();
		int items = userList.getItemCount();
		if (index == -1 || index == items - 1) {
			return;
		}
		// Swap the items in the list
		String selItem = userList.getItem(index);
		userList.remove(index);
		if (index + 1 == items) {
			userList.add(selItem);
		} else {
			userList.add(selItem, ++index);
		}
		setDirty(true);

		// Keep the swapped item selected
		userList.select(index);
		enableUserButtons();
	}

	/*
	 * Enables the buttons on the path control if the right conditions are met
	 */
	protected void enableUserButtons() {
		// Enable the remove button if there is at least 1 item in the list
		int items = userList.getItemCount();
		if (items > 0) {
			editPath.setEnabled(true);
			removePath.setEnabled(true);
			// Enable the up/down buttons depending on what item is selected
			int index = userList.getSelectionIndex();
			pathUp.setEnabled(items > 1 && index > 0);
			pathDown.setEnabled(items > 1 && index < (items - 1));
		} else {
			editPath.setEnabled(false);
			removePath.setEnabled(false);
			pathUp.setEnabled(false);
			pathDown.setEnabled(false);
		}
	}

	/**
	 * @param composite
	 */
	private void createOptionsControls(Composite composite) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param composite
	 */
	private void createDiscoveredControls(Composite composite) {
		// Create group
		discoveredGroup = ControlFactory.createGroup(composite, getTitle(DISCOVERED_GROUP), 3);
		((GridData) discoveredGroup.getLayoutData()).horizontalSpan = 3;
		((GridData) discoveredGroup.getLayoutData()).grabExcessHorizontalSpace = true;

		// Create label Selected:
		selectedLabel = ControlFactory.createLabel(discoveredGroup, getTitle(SELECTED_LABEL));
		((GridData) selectedLabel.getLayoutData()).horizontalSpan = 1;
		
		// Add a dummy label
		ControlFactory.createLabel(discoveredGroup, "");//$NON-NLS-1$
		
		// Create label Removed:
		removedLabel = ControlFactory.createLabel(discoveredGroup, getTitle(REMOVED_LABEL));
		((GridData) removedLabel.getLayoutData()).horizontalSpan = 1;
		
		// Create list
		discActiveList = new List(discoveredGroup, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		discActiveList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				discRemovedList.deselectAll();
				enableDiscoveredButtons();
			}
		});
		// Make it occupy the first column
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 1;
		gd.heightHint = getDefaultFontHeight(discActiveList, PROJECT_LIST_MULTIPLIER);
		gd.widthHint = convertWidthInCharsToPixels(INITIAL_LIST_WIDTH);
		discActiveList.setLayoutData(gd);
		
		// Create buttons
		// Create a composite for the buttons
		Composite pathButtonComp = ControlFactory.createComposite(discoveredGroup, 1);

		// Add the buttons
		removeDiscPath = ControlFactory.createPushButton(pathButtonComp, getTitle(REMOVE_DISCOVERED));
		removeDiscPath.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleRemoveRestoreDiscPath(DO_REMOVE);
			}
		});
		removeDiscPath.setEnabled(true);
		SWTUtil.setButtonDimensionHint(removeDiscPath);

		restoreDiscPath = ControlFactory.createPushButton(pathButtonComp, getTitle(RESTORE_DISCOVERED));
		restoreDiscPath.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleRemoveRestoreDiscPath(DO_RESTORE);
			}
		});
		restoreDiscPath.setEnabled(true);
		SWTUtil.setButtonDimensionHint(restoreDiscPath);

		Label sep = ControlFactory.createSeparator(pathButtonComp, 1);
		
		deleteDiscPath = ControlFactory.createPushButton(pathButtonComp, getTitle(DELETE_DISCOVERED));
		deleteDiscPath.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleDeleteDiscPath();
			}
		});
		deleteDiscPath.setEnabled(true);
		SWTUtil.setButtonDimensionHint(deleteDiscPath);

		deleteAllDiscPaths = ControlFactory.createPushButton(pathButtonComp, getTitle(DELETE_ALL_DISCOVERED));
		deleteAllDiscPaths.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleDeleteAllDiscPath();
			}
		});
		deleteAllDiscPaths.setEnabled(true);
		SWTUtil.setButtonDimensionHint(deleteAllDiscPaths);

		// Create list
		discRemovedList = new List(discoveredGroup, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		discRemovedList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				discActiveList.deselectAll();
				enableDiscoveredButtons();
			}
		});
		// Make it occupy the first column
		GridData gd2 = new GridData(GridData.FILL_BOTH);
		gd2.grabExcessHorizontalSpace = true;
		gd2.horizontalSpan = 1;
		gd2.heightHint = getDefaultFontHeight(discRemovedList, PROJECT_LIST_MULTIPLIER);
		gd2.widthHint = convertWidthInCharsToPixels(INITIAL_LIST_WIDTH);
		discRemovedList.setLayoutData(gd2);
	}

	/**
	 * 
	 */
	protected void handleRemoveRestoreDiscPath(int type) {
		if (workingDiscoveredPaths != null) {
			List discList = discRemovedList;
			List discOtherList = discActiveList;
			boolean compareValue = true;	// removed
			if (type == DO_REMOVE) {
				discList = discActiveList;
				discOtherList = discRemovedList;
				compareValue = false;
			}
			
			int id = discList.getSelectionIndex();
			if (id != -1) {
				String path = discList.getItem(id);
				// find it in the discoveredPaths LinkedHashMap
				Boolean value = (Boolean) workingDiscoveredPaths.get(path);
				if (value != null) {
					if (value.booleanValue() == compareValue) {
						workingDiscoveredPaths.put(path, Boolean.valueOf(!compareValue));
						// update UI
						discActiveList.setItems(getDiscIncludePaths(workingDiscoveredPaths, ACTIVE));
						discRemovedList.setItems(getDiscIncludePaths(workingDiscoveredPaths, REMOVED));
						discOtherList.setSelection(discOtherList.indexOf(path));
						enableDiscoveredButtons();
						setDirty(true);
					}
				}
			}
		}
	}

	/**
	 * 
	 */
	protected void handleDeleteDiscPath() {
		deleteDiscPath(REMOVED);
		deleteDiscPath(ACTIVE);
	}

	/**
	 * @param discList
	 * @param type
	 */
	private void deleteDiscPath(int type) {
		List discList = discRemovedList;
		if (type == ACTIVE) {
			discList = discActiveList;
		}
		int id = discList.getSelectionIndex();
		if (id >= 0) {
			String path = discList.getItem(id);
			workingDiscoveredPaths.remove(path);
			discList.setItems(getDiscIncludePaths(workingDiscoveredPaths, type));
			int items = discList.getItemCount();
			if (items > 0) {
				if (id >= items) {
					id = items - 1;
				}
				discList.setSelection(id);
				enableDiscoveredButtons();
				setDirty(true);
			}
		}
	}

	/**
	 * 
	 */
	protected void handleDeleteAllDiscPath() {
		ScannerInfoCollector.getInstance().deleteAllPaths(fProject);
		workingDiscoveredPaths.clear();
		discActiveList.setItems(getDiscIncludePaths(workingDiscoveredPaths, ACTIVE));
		discRemovedList.setItems(getDiscIncludePaths(workingDiscoveredPaths, REMOVED));
		enableDiscoveredButtons();
		setDirty(true);
	}

	/**
	 * 
	 */
	protected void enableDiscoveredButtons() {
		discoveredGroup.setEnabled(fProject != null);
		selectedLabel.setEnabled(fProject != null);
		discActiveList.setEnabled(fProject != null);
		removedLabel.setEnabled(fProject != null);
		discRemovedList.setEnabled(fProject != null);

		int activeItems = discActiveList.getItemCount();
		int activeSeclection = discActiveList.getSelectionIndex();
		int removedItems = discRemovedList.getItemCount();
		int removedSelection = discRemovedList.getSelectionIndex();
		// To maintain the proper TAB order of enabled buttons 
		if (activeItems > 0 && activeSeclection >= 0) {
			removeDiscPath.setEnabled(activeItems > 0 && activeSeclection >= 0);
			restoreDiscPath.setEnabled(removedItems > 0 && removedSelection >= 0);
		}
		else {
			restoreDiscPath.setEnabled(removedItems > 0 && removedSelection >= 0);
			removeDiscPath.setEnabled(activeItems > 0 && activeSeclection >= 0);
		}
		deleteDiscPath.setEnabled((activeItems > 0 && activeSeclection >= 0) ||
				(removedItems > 0 && removedSelection >= 0));
		deleteAllDiscPaths.setEnabled(activeItems > 0 || removedItems > 0);
	}

	/**
	 * Get the defualt widget height for the supplied control.
	 * @return int
	 * @param control - the control being queried about fonts
	 * @param lines - the number of lines to be shown on the table.
	 */
	private static int getDefaultFontHeight(Control control, int lines) {
		FontData[] viewerFontData = control.getFont().getFontData();
		int fontHeight = 10;

		//If we have no font data use our guess
		if (viewerFontData.length > 0)
			fontHeight = viewerFontData[0].getHeight();
		return lines * fontHeight;
	}

	/**
	 * @return
	 */
	public String[] getManagedIncludes() {
		if (returnPaths == null) {
			return new String[0];
		}
		return (String[]) returnPaths.toArray(new String[returnPaths.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			// Store user part
			userPaths = new ArrayList(Arrays.asList(userList.getItems()));
			// Store discovered part
			discoveredPaths = workingDiscoveredPaths;
			// Return sum of user and active discovered paths
			returnPaths = new ArrayList(userPaths.size() + discActiveList.getItemCount());
			returnPaths.addAll(userPaths);
			returnPaths.addAll(new ArrayList(Arrays.asList(discActiveList.getItems())));
			
			fDirty = fWorkingDirty;
		}
		else if (IDialogConstants.CANCEL_ID == buttonId) {
			workingDiscoveredPaths = null;
			setDirty(false);
		}
		super.buttonPressed(buttonId);
	}

	private void setDirty(boolean dirty) {
		fWorkingDirty = dirty;
	}

	/**
	 * Called by BuildPathInfoBlock.performApply
	 * @param info
	 */
	public void saveTo(DiscoveredScannerInfo info) {
		if (fDirty || (fProject == null && fContainer.getProject() != null)) {// New Standard Make project wizard
			info.setUserIncludePaths(userPaths);
			info.setDiscoveredIncludePaths(discoveredPaths);
		}
		setDirty(false);
		fDirty = false;
	}

	/**
	 * Called by BuildPathInfoBlock.performDefaults
	 */
	public void restore() {
		if (fProject != null) {
			userPaths = new ArrayList(Arrays.asList(BuildPathInfoBlock.getIncludes(
					MakeCorePlugin.getDefault().getPluginPreferences())));
		}
		else {
			userPaths = new ArrayList();
		}
		discoveredPaths = new LinkedHashMap();
		fDirty = true;
	}
}
