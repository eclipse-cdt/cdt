/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * QNX Software Systems - Move to Make plugin
***********************************************************************/
package org.eclipse.cdt.make.ui.dialogs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;

public class BuildPathInfoBlock extends AbstractCOptionPage {
	private static final int PROJECT_LIST_MULTIPLIER = 15;
	private static final int INITIAL_LIST_WIDTH = 60;

	private static final String PREF_SYMBOLS = "ScannerSymbols"; //$NON-NLS-1$
	private static final String PREF_INCLUDES = "ScannerIncludes"; //$NON-NLS-1$
	private static final String PREFIX = "BuildPathInfoBlock"; //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String PATHS = PREFIX + ".paths"; //$NON-NLS-1$
	private static final String SYMBOLS = PREFIX + ".symbols"; //$NON-NLS-1$
	private static final String MANAGE = "BuildPropertyCommon.label.manage"; //$NON-NLS-1$
	private static final String SC_GROUP_LABEL = PREFIX + ".scGroup.label";	//$NON-NLS-1$
	private static final String SC_ENABLED_LABEL = PREFIX + ".scGroup.enabled.label";	//$NON-NLS-1$
	private static final String SC_OPTIONS_LABEL = PREFIX + ".scGroup.options.label";	//$NON-NLS-1$
	private static final String MISSING_BUILDER_MSG = "ScannerConfigOptionsDialog.label.missingBuilderInformation";	//$NON-NLS-1$

	private Button scEnabledButton;
	private Button scOptionsButton;
	private List pathList;
	private List symbolList;
	private Composite pathButtonComp;
	private Button managePathsButton;
	private Composite symbolButtonComp;
	private Button manageSymbolsButton;
	
	private ScannerConfigOptionsDialog scOptionsDialog;
	private ManageIncludePathsDialog manageIncludesDialog;
	private ManageDefinedSymbolsDialog manageSymbolsDialog;
	
	private boolean needsSCNature = false;
	
	/**
	 * This class add a "browse" button to the selection to be used for the path
	 */
	static class SelectPathInputDialog extends InputDialog {
		public SelectPathInputDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue, IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		}
		
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			Button browse = createButton(parent, 3, MakeUIPlugin.getResourceString("BuildPathInfoBlock.button.browse"), true); //$NON-NLS-1$
			browse.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent ev) {
					DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
					String currentName = getText().getText();
					if(currentName != null && currentName.trim().length() != 0) {
						dialog.setFilterPath(currentName);
					}
					String dirname = dialog.open();
					if(dirname != null) {
						getText().setText(dirname);
					}
				}
			});
		}

	}

	public BuildPathInfoBlock() {
		super(MakeUIPlugin.getResourceString(LABEL));
		setDescription(MakeUIPlugin.getResourceString("BuildPathInfoBlock.description")); //$NON-NLS-1$
	}

	private void createPathListButtons(Composite parent) {
		// Create a ManageIncludePathsDialog
		if (manageIncludesDialog == null) {
			manageIncludesDialog = new ManageIncludePathsDialog(getShell(), getContainer());
		}
		
		// Create a composite for the buttons
		pathButtonComp = ControlFactory.createComposite(parent, 1);
		((GridData) pathButtonComp.getLayoutData()).verticalAlignment = GridData.BEGINNING;
		((GridData) pathButtonComp.getLayoutData()).grabExcessHorizontalSpace = false;
		pathButtonComp.setFont(parent.getFont());

		// Add the buttons
		managePathsButton = ControlFactory.createPushButton(pathButtonComp, MakeUIPlugin.getResourceString(MANAGE));
		managePathsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleManagePaths();
			}
		});
		managePathsButton.setFont(parent.getFont());
		managePathsButton.setLayoutData(new GridData());
		SWTUtil.setButtonDimensionHint(managePathsButton);
		return;
	}

	protected void handleManagePaths() {
		if (manageIncludesDialog.open() == Window.OK) {
			pathList.setItems(manageIncludesDialog.getManagedIncludes());
		}
	}

	private void createPathListControl(Composite parent, int numColumns) {
		// Create the list
		pathList = new List(parent, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		// Make it occupy the first 2 columns
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = numColumns - 1;
		gd.heightHint = getDefaultFontHeight(pathList, PROJECT_LIST_MULTIPLIER);
		gd.widthHint = convertWidthInCharsToPixels(INITIAL_LIST_WIDTH);
		pathList.setLayoutData(gd);
		pathList.setFont(parent.getFont());
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

	private void createSymbolListButtons(Composite parent) {
		// Create a ManageDefinedSymbolsDialog
		if (manageSymbolsDialog == null) {
			manageSymbolsDialog = new ManageDefinedSymbolsDialog(getShell(), getContainer());
		}

		// Create a composite for the buttons
		symbolButtonComp = ControlFactory.createComposite(parent, 1);
		((GridData) symbolButtonComp.getLayoutData()).verticalAlignment = GridData.BEGINNING;
		((GridData) symbolButtonComp.getLayoutData()).grabExcessHorizontalSpace = false;
		symbolButtonComp.setFont(parent.getFont());

		// Add the Manage button
		manageSymbolsButton = ControlFactory.createPushButton(symbolButtonComp, MakeUIPlugin.getResourceString(MANAGE));
		manageSymbolsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleManageSymbols();
			}
		});
		manageSymbolsButton.setFont(parent.getFont());
		manageSymbolsButton.setLayoutData(new GridData());
		SWTUtil.setButtonDimensionHint(manageSymbolsButton);
		return;
	}

	protected void handleManageSymbols() {
		if (manageSymbolsDialog.open() == Window.OK) {
			symbolList.setItems(manageSymbolsDialog.getManagedSymbols());
		}
	}

	private void createSymbolListControl(Composite parent, int numColumns) {
		// Create the list
		symbolList = new List(parent, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		// Make it occupy the first n-1 columns
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = numColumns - 1;
		gd.grabExcessHorizontalSpace = true;
		gd.heightHint = getDefaultFontHeight(pathList, PROJECT_LIST_MULTIPLIER);
		gd.widthHint = convertWidthInCharsToPixels(INITIAL_LIST_WIDTH);
		symbolList.setLayoutData(gd);
		symbolList.setFont(parent.getFont());

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#doRun(org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		// First store scanner config options
		if (scOptionsDialog.isInitialized()) {
			try {
				scOptionsDialog.performApply(monitor);
			} 
			catch (CoreException e) {
				// builder was disabled while scOptionsDialog was initialized
			}
		}
		
		IProject project = getContainer().getProject();
		if (project != null) {
			// Store the paths and symbols 
			monitor.beginTask(MakeUIPlugin.getResourceString("BuildPathInfoBlock.monitor.settingScannerInfo"), 3); //$NON-NLS-1$
			IDiscoveredPathInfo info = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(project);
			boolean changed = manageIncludesDialog.saveTo(info);
			monitor.worked(1);
			changed |= manageSymbolsDialog.saveTo(info);
			monitor.worked(1);
			if (changed) {
				MakeCorePlugin.getDefault().getDiscoveryManager().updateDiscoveredInfo(info);
			}
			monitor.done();
		} 
		else {
			setIncludes(MakeCorePlugin.getDefault().getPluginPreferences());
			setSymbols(MakeCorePlugin.getDefault().getPluginPreferences());
		}
	}

	public void performDefaults() {
		// First restore scanner config options
		scOptionsDialog.performDefaults();
		scEnabledButton.setSelection(scOptionsDialog.isScannerConfigDiscoveryEnabled());
		handleScannerConfigEnable();
		
		pathList.removeAll();
		symbolList.removeAll();
//		if (getContainer().getProject() != null) {
//			pathList.setItems(getIncludes(MakeCorePlugin.getDefault().getPluginPreferences()));
//			symbolList.setItems(getSymbols(MakeCorePlugin.getDefault().getPluginPreferences()));
//		}
		manageIncludesDialog.restore();
		manageSymbolsDialog.restore();
		getContainer().updateContainer();
	}

	private void setSymbols(Preferences prefs) {
		prefs.setValue(PREF_SYMBOLS, stringArrayToString(getSymbolListContents()));
	}

	private void setIncludes(Preferences prefs) {
		prefs.setValue(PREF_INCLUDES, stringArrayToString(getPathListContents()));
	}

	private String stringArrayToString(String[] strings) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < strings.length; i++) {
			buf.append(strings[i]).append(';');
		}
		return buf.toString();
	}

	static String[] getSymbols(Preferences prefs) {
		String syms = prefs.getString(PREF_SYMBOLS);
		return parseStringToList(syms);
	}

	static String[] getIncludes(Preferences prefs) {
		String syms = prefs.getString(PREF_INCLUDES);
		return parseStringToList(syms);
	}

	private static String[] parseStringToList(String syms) {
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#getControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		// Create the composite control for the tab
		int tabColumns = 3;
		Font font = parent.getFont();
		Composite composite = ControlFactory.createComposite(parent, tabColumns);
		((GridLayout) composite.getLayout()).makeColumnsEqualWidth = false;
		composite.setFont(font);
		GridData gd;
		setControl(composite);

		WorkbenchHelp.setHelp(getControl(), IMakeHelpContextIds.MAKE_PATH_SYMBOL_SETTINGS);

		// Create a group for scanner config discovery
		createScannerConfigControls(composite, tabColumns);
		
		// Create a label for the include paths control
		Label paths = ControlFactory.createLabel(composite, MakeUIPlugin.getResourceString(PATHS));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = tabColumns;
		gd.grabExcessHorizontalSpace = false;
		paths.setLayoutData(gd);
		paths.setFont(font);

		//Create the list and button controls
		createPathListControl(composite, tabColumns);
		createPathListButtons(composite);

		// Create a label for the symbols control
		Label symbols = ControlFactory.createLabel(composite, MakeUIPlugin.getResourceString(SYMBOLS));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = tabColumns;
		gd.grabExcessHorizontalSpace = false;
		symbols.setLayoutData(gd);
		symbols.setFont(font);

		// Create list and button controls for symbols
		createSymbolListControl(composite, tabColumns);
		createSymbolListButtons(composite);

		setListContents();
	}

	/**
	 * @param composite
	 */
	private void createScannerConfigControls(Composite parent, int numColumns) {
		// Check if it is an old project
		IProject project = getContainer().getProject();
		boolean showMissingBuilder = false;
		try {
			if (project != null &&
					project.hasNature(MakeProjectNature.NATURE_ID) &&
					!project.hasNature(ScannerConfigNature.NATURE_ID)) {
				needsSCNature = true;	// an old project
			}
		} 
		catch (CoreException e) {
			showMissingBuilder = true;
		}
		
		// Create a ScannerConfigOptionsDialog
		if (scOptionsDialog == null) {
			if (needsSCNature) {
				// create a temporary dialog
				scOptionsDialog = new ScannerConfigOptionsDialog(getContainer());
			}
			else {
				scOptionsDialog = new ScannerConfigOptionsDialog(getShell(), getContainer());
			}
		}
		
		Group scGroup = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(SC_GROUP_LABEL), numColumns);
		scGroup.setFont(parent.getFont());
		((GridData) scGroup.getLayoutData()).grabExcessHorizontalSpace = false;
		((GridData) scGroup.getLayoutData()).horizontalSpan = numColumns;
		((GridData) scGroup.getLayoutData()).horizontalAlignment = GridData.FILL;
		((GridLayout) scGroup.getLayout()).marginWidth = 7;

		if ((!needsSCNature && !scOptionsDialog.isInitialized())) {
			ControlFactory.createLabel(scGroup, MakeUIPlugin.getResourceString(MISSING_BUILDER_MSG));
			return;
		}
		
		// Add checkbox
		scEnabledButton = ControlFactory.createCheckBox(scGroup, MakeUIPlugin.getResourceString(SC_ENABLED_LABEL));
		scEnabledButton.setFont(parent.getFont());
		((GridData) scEnabledButton.getLayoutData()).horizontalSpan = 2;
		((GridData) scEnabledButton.getLayoutData()).grabExcessHorizontalSpace = true;
		// VMIR* old projects will have discovery disabled by default
		scEnabledButton.setSelection(needsSCNature ? false : scOptionsDialog.isScannerConfigDiscoveryEnabled());
		scEnabledButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleScannerConfigEnable();
			}
		});
		// Add Options... button 
		scOptionsButton = ControlFactory.createPushButton(scGroup, MakeUIPlugin.getResourceString(SC_OPTIONS_LABEL));
		scOptionsButton.setFont(parent.getFont());
		((GridData) scOptionsButton.getLayoutData()).grabExcessHorizontalSpace = false;
		SWTUtil.setButtonDimensionHint(scOptionsButton);
		scOptionsButton.setEnabled(scEnabledButton.getSelection());
		scOptionsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				scOptionsDialog.open();
			}
		});
//		handleScannerConfigEnable(); Only if true in VMIR*
	}

	/**
	 * Handles scanner configuration discovery selection change
	 */
	protected void handleScannerConfigEnable() {
		boolean enable = scEnabledButton.getSelection();
		scOptionsButton.setEnabled(enable);
		if (enable && needsSCNature) {
			// first install the SC nature
			try {
				ScannerConfigNature.addScannerConfigNature(getContainer().getProject());
				// create the real dialog
				scOptionsDialog = new ScannerConfigOptionsDialog(getShell(), getContainer());
				needsSCNature = false;
			} 
			catch (CoreException e) {
				MakeCorePlugin.log(e.getStatus());
			}
		}
		scOptionsDialog.setScannerConfigDiscoveryEnabled(enable);
	}

	private String[] getPathListContents() {
		return pathList.getItems();
	}

	private String[] getSymbolListContents() {
		return symbolList.getItems();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#isValid()
	 */
	public boolean isValid() {
		// Info on this page is not critical
		return true;
	}

	private void setListContents() {
		IProject project = getContainer().getProject();
		if (project != null) {
//			IScannerInfo info = CCorePlugin.getDefault().getScannerInfoProvider(project).getScannerInformation(project);
//			if (info != null) {
//				pathList.setItems(info.getIncludePaths());
//				symbolList.setItems(info.getPreprocessorSymbols());
//			}
			try {
				IDiscoveredPathInfo info = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(project);
				pathList.setItems(ScannerConfigUtil.iPathArray2StringArray(info.getIncludePaths()));
				LinkedHashMap discoveredSymbols = info.getSymbolMap();
				ArrayList activeSymbols = new ArrayList();
				activeSymbols.addAll(ScannerConfigUtil.scSymbolsSymbolEntryMap2List(discoveredSymbols, true));
				symbolList.setItems((String[]) activeSymbols.toArray(new String[activeSymbols.size()]));
			} catch (CoreException e) {
				MakeCorePlugin.log(e);
			}
		}
//		else {
//			pathList.setItems(getIncludes(MakeCorePlugin.getDefault().getPluginPreferences()));
//			symbolList.setItems(getSymbols(MakeCorePlugin.getDefault().getPluginPreferences()));
//		}
	}
}
