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
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerInfoCollector;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.SymbolEntry;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.MessageLine;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
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
 * Manage defined symbols dialog
 * 
 * @author vhirsl
 */
public class ManageDefinedSymbolsDialog extends Dialog {
	private static final String PREF_SYMBOLS = "ScannerSymbols"; //$NON-NLS-1$

	private static final String PREFIX = "ManageDefinedSymbolsDialog"; //$NON-NLS-1$
	private static final String DIALOG_TITLE = PREFIX + ".title"; //$NON-NLS-1$
	private static final String USER_GROUP = PREFIX + ".userGroup.title"; //$NON-NLS-1$
	private static final String NEW = "BuildPropertyCommon.label.new"; //$NON-NLS-1$
	private static final String EDIT = "BuildPropertyCommon.label.edit"; //$NON-NLS-1$
	private static final String REMOVE = "BuildPropertyCommon.label.remove"; //$NON-NLS-1$
	private static final String UP = "BuildPropertyCommon.label.up"; //$NON-NLS-1$
	private static final String DOWN = "BuildPropertyCommon.label.down"; //$NON-NLS-1$

	private static final String BROWSE = "BuildPathInfoBlock.browse"; //$NON-NLS-1$
	private static final String SYMBOL_TITLE = BROWSE + ".symbol"; //$NON-NLS-1$
	private static final String EDIT_SYMBOL_TITLE = BROWSE + ".symbol.edit"; //$NON-NLS-1$
	private static final String SYMBOL_LABEL = BROWSE + ".symbol.label"; //$NON-NLS-1$

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
	
	boolean alreadyCreated;	// Set when dialog is created for the first time (vs. reopened)
	private ArrayList returnSymbols;
	private ArrayList deletedDiscoveredSymbols;
	private LinkedHashMap discoveredSymbols;
	private LinkedHashMap workingDiscoveredSymbols; // working copy of discoveredSymbols, until either OK or CANCEL is pressed
	private boolean fDirty;
	private boolean fWorkingDirty;
	
	private ICOptionContainer fContainer;
	private IProject fProject;
	private Shell fShell;
	private MessageLine fStatusLine;

	private Group discoveredGroup;
	private Label selectedLabel;
	private Label removedLabel;
	private List discActiveList;
	private List discRemovedList;
	private Button removeDiscSymbol;
	private Button restoreDiscSymbol;
	private Button deleteDiscSymbol;
	private Button deleteAllDiscSymbols;

	/**
	 * @param parentShell
	 */
	protected ManageDefinedSymbolsDialog(Shell parentShell, ICOptionContainer container) {
		super(parentShell);
		fShell = parentShell;
		fContainer = container;
		fProject = fContainer.getProject();
		IDiscoveredPathInfo scanInfo;
		if (fProject != null) {
			try {
				scanInfo = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(fProject); 
			} catch (CoreException e) {
				scanInfo = new DiscoveredPathInfo(fProject);
			}
		}
		else {
			scanInfo = new DiscoveredPathInfo(fProject);
		}
		discoveredSymbols = scanInfo.getSymbolMap();
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
		setDirty(false);
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
		
		createOptionsControls(composite);
		createDiscoveredControls(composite);
		
		setListContents();
		discActiveList.select(0);
		enableDiscoveredButtons();
		
		return composite;
	}

	/**
	 * 
	 */
	private void setListContents() {
		workingDiscoveredSymbols = new LinkedHashMap(discoveredSymbols);
		
		discActiveList.setItems(getDiscDefinedSymbols(workingDiscoveredSymbols, ACTIVE));
		discRemovedList.setItems(getDiscDefinedSymbols(workingDiscoveredSymbols, REMOVED));
	}

	/**
	 * @param discoveredPaths
	 * @return
	 */
	private String[] getDiscDefinedSymbols(Map dSymbols, int type) {
		ArrayList aSymbols = new ArrayList();
		for (Iterator i = dSymbols.keySet().iterator(); i.hasNext(); ) {
			String symbol = (String) i.next();
			SymbolEntry values = (SymbolEntry) dSymbols.get(symbol);
			java.util.List aValues = (type == ACTIVE ? values.getActiveRaw() : values.getRemovedRaw());
			aSymbols.addAll(aValues);
		}
		return (String[]) aSymbols.toArray(new String[aSymbols.size()]);
	}

	private String[] getIncludes(Preferences prefs) {
		String syms = prefs.getString(PREF_SYMBOLS);
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

		// Create composite
//		Composite c1 = ControlFactory.createComposite(discoveredGroup, 1);
		Composite c1 = discoveredGroup;
		
		// Create label Selected:
		selectedLabel = ControlFactory.createLabel(c1, getTitle(SELECTED_LABEL));
		((GridData) selectedLabel.getLayoutData()).horizontalSpan = 1;
		
		// Add a dummy label
		ControlFactory.createLabel(discoveredGroup, "");//$NON-NLS-1$
		
		// Create label Removed:
		removedLabel = ControlFactory.createLabel(c1, getTitle(REMOVED_LABEL));
		((GridData) removedLabel.getLayoutData()).horizontalSpan = 1;
		
		// Create list
		discActiveList = new List(c1, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
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
		removeDiscSymbol = ControlFactory.createPushButton(pathButtonComp, getTitle(REMOVE_DISCOVERED));
		removeDiscSymbol.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleRemoveRestoreDiscSymbol(DO_REMOVE);
			}
		});
		removeDiscSymbol.setEnabled(true);
		SWTUtil.setButtonDimensionHint(removeDiscSymbol);

		restoreDiscSymbol = ControlFactory.createPushButton(pathButtonComp, getTitle(RESTORE_DISCOVERED));
		restoreDiscSymbol.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleRemoveRestoreDiscSymbol(DO_RESTORE);
			}
		});
		restoreDiscSymbol.setEnabled(true);
		SWTUtil.setButtonDimensionHint(restoreDiscSymbol);

		Label sep = ControlFactory.createSeparator(pathButtonComp, 1);
		
		deleteDiscSymbol = ControlFactory.createPushButton(pathButtonComp, getTitle(DELETE_DISCOVERED));
		deleteDiscSymbol.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleDeleteDiscSymbol();
			}
		});
		deleteDiscSymbol.setEnabled(true);
		SWTUtil.setButtonDimensionHint(deleteDiscSymbol);

		deleteAllDiscSymbols = ControlFactory.createPushButton(pathButtonComp, getTitle(DELETE_ALL_DISCOVERED));
		deleteAllDiscSymbols.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleDeleteAllDiscSymbols();
			}
		});
		deleteAllDiscSymbols.setEnabled(true);
		SWTUtil.setButtonDimensionHint(deleteAllDiscSymbols);

		// Create composite
//		Composite c2 = ControlFactory.createComposite(discoveredGroup, 1);
		Composite c2 = discoveredGroup;
		
		// Create list
		discRemovedList = new List(c2, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
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
	protected void handleRemoveRestoreDiscSymbol(int type) {
		if (workingDiscoveredSymbols != null) {
			List discList = discRemovedList;
			List discOtherList = discActiveList;
			boolean newStatus = true;	// active
			if (type == DO_REMOVE) {
				discList = discActiveList;
				discOtherList = discRemovedList;
				newStatus = false;	// removed
			}
			
			int id = discList.getSelectionIndex();
			if (id != -1) {
				String symbol = discList.getItem(id);
				String key = ScannerConfigUtil.getSymbolKey(symbol);
				String value = ScannerConfigUtil.getSymbolValue(symbol);
				// find it in the discoveredSymbols Map of SymbolEntries
				SymbolEntry se = (SymbolEntry) workingDiscoveredSymbols.get(key);
				if (se != null) {
					se = new SymbolEntry(se); // deep copy
					se.replace(value, newStatus);
					workingDiscoveredSymbols.put(key, se);
					// update UI
					discActiveList.setItems(getDiscDefinedSymbols(workingDiscoveredSymbols, ACTIVE));
					discRemovedList.setItems(getDiscDefinedSymbols(workingDiscoveredSymbols, REMOVED));
					discOtherList.setSelection(discOtherList.indexOf(symbol));
					enableDiscoveredButtons();
					setDirty(true);
				}
				else {
					//TODO VMIR generate an error
				}
			}
		}
	}

	/**
	 * 
	 */
	protected void handleDeleteDiscSymbol() {
		deleteDiscSymbol(REMOVED);
		deleteDiscSymbol(ACTIVE);
	}

	private void deleteDiscSymbol(int type) {
		List discList = discRemovedList;
		if (type == ACTIVE) {
			discList = discActiveList;
		}
		int id = discList.getSelectionIndex();
		if (id >= 0) {
			String symbol = discList.getItem(id);
			// add it to the deleted list
			if (deletedDiscoveredSymbols == null) {
				deletedDiscoveredSymbols = new ArrayList();
			}
			deletedDiscoveredSymbols.add(symbol);
			
			// remove it from the Map of SymbolEntries 
			ScannerConfigUtil.removeSymbolEntryValue(symbol, workingDiscoveredSymbols);

			int items = discList.getItemCount();
			discList.setItems(getDiscDefinedSymbols(workingDiscoveredSymbols, type));
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
	protected void handleDeleteAllDiscSymbols() {
		ScannerInfoCollector.getInstance().deleteAllSymbols(fProject);
		workingDiscoveredSymbols.clear();
		discActiveList.setItems(getDiscDefinedSymbols(workingDiscoveredSymbols, ACTIVE));
		discRemovedList.setItems(getDiscDefinedSymbols(workingDiscoveredSymbols, REMOVED));
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
			removeDiscSymbol.setEnabled(activeItems > 0 && activeSeclection >= 0);
			restoreDiscSymbol.setEnabled(removedItems > 0 && removedSelection >= 0);
		}
		else {
			restoreDiscSymbol.setEnabled(removedItems > 0 && removedSelection >= 0);
			removeDiscSymbol.setEnabled(activeItems > 0 && activeSeclection >= 0);
		}
		deleteDiscSymbol.setEnabled((activeItems > 0 && activeSeclection >= 0) ||
				(removedItems > 0 && removedSelection >= 0));
		deleteAllDiscSymbols.setEnabled(activeItems > 0 || removedItems > 0);
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
	public String[] getManagedSymbols() {
		if (returnSymbols == null) {
			return new String[0];
		}
		return (String[]) returnSymbols.toArray(new String[returnSymbols.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			// Store discovered part
			discoveredSymbols = workingDiscoveredSymbols;
			// Return sum of user and active discovered paths
			returnSymbols = new ArrayList(discActiveList.getItemCount());
			returnSymbols.addAll(new ArrayList(Arrays.asList(discActiveList.getItems())));

			fDirty = fWorkingDirty;
		}
		else if (IDialogConstants.CANCEL_ID == buttonId) {
			deletedDiscoveredSymbols = null;
			workingDiscoveredSymbols = null;
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
	 * @return boolean - true if changed
	 */
	public boolean saveTo(IDiscoveredPathInfo info) {
		if (fDirty || (fProject == null && fContainer.getProject() != null)) {// New Standard Make project wizard
			info.setSymbolMap(discoveredSymbols);
			// remove deleted symbols from discovered SC
			if (deletedDiscoveredSymbols != null) {
				for (Iterator i = deletedDiscoveredSymbols.iterator(); i.hasNext(); ) {
					ScannerInfoCollector.getInstance().deleteSymbol(fProject, (String) i.next());
				}
				deletedDiscoveredSymbols = null;
			}
		}
		setDirty(false);
		boolean rc = fDirty;
		fDirty = false;
		return rc;
	}

	/**
	 * Called by BuildPathInfoBlock.performDefaults
	 */
	public void restore() {
		if (fProject != null) {
			// remove discovered symbols
			ScannerInfoCollector.getInstance().deleteAllSymbols(fProject);
		}
		discoveredSymbols = new LinkedHashMap();
		deletedDiscoveredSymbols = null;
		fDirty = true;
	}
}
