/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.gnu.tools.tabs;
import java.util.HashMap;

import org.eclipse.cdt.core.builder.model.ICBuildConfig;
import org.eclipse.cdt.core.builder.model.ICBuildConfigWorkingCopy;
import org.eclipse.cdt.core.builder.model.ICPosixBuildConstants;
import org.eclipse.cdt.ui.builder.ACToolTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * The control for editing and viewing compiler options.
 */
public class CTabCompiler extends ACToolTab {
		
	private Combo		fOptimizationLevel;
	private Button 	btnDebugging;
	private Button		btnProfiling;
	private Text		fUserOptions;
	private Table 		fWarningsTable;
	private TableItem	fWarnAll;
	private TableItem 	fWarnAsErrors;
	private TableItem	fWarnFormatStrings;
	private TableItem 	fWarnPointerArith;
	private TableItem 	fWarnSwitch;
	private TableItem 	fWarnUnreachable;
	private TableItem 	fWarnUnused;		
	private HashMap 	optLevelMap = new HashMap();
	
	private final String[] OPT_LEVELS = {"None", "Medium", "High" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private final String[] OPT_ARGS = {"-O0", "-O1", "-O2" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * Constructs the object
	 */
	public CTabCompiler() {
		// populate hash map
		for (int nIndex = 0; nIndex < OPT_LEVELS.length; nIndex++) 
		{
			optLevelMap.put(OPT_ARGS[nIndex], OPT_LEVELS[nIndex]);
		}
		
	}

	/**
	 * Helper to add an item to the table of compiler settings
	 *
	 * @param parent				the owning control
	 * @param txtLabel				text for the table item
	 */
	private TableItem createTableItem(Table parent, String txtLabel)
	{
		TableItem retval = new TableItem(parent, SWT.NONE);
		
		if (retval != null) {
			retval.setText(txtLabel);
		}
		
		return retval;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {

		Composite ths = new Composite(parent, SWT.NONE);

		// Panel
		
		ths.setLayout(new GridLayout(2, true));
		ths.setLayoutData(new GridData(GridData.FILL_BOTH));

		// --------------------------------------------------
		// Left column
		// --------------------------------------------------

		Composite cmpLeft = new Composite(ths, SWT.NONE);

		cmpLeft.setLayout(new GridLayout());
		cmpLeft.setLayoutData(new GridData(GridData.FILL_BOTH));

		// row 1
		new Label(cmpLeft, SWT.LEFT).setText(("Optimization_Level_7")); //$NON-NLS-1$

		// row 2
		fOptimizationLevel = new Combo(cmpLeft, SWT.RIGHT | SWT.TOP | SWT.READ_ONLY);
		fOptimizationLevel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

		// row 3
		btnDebugging = new Button(cmpLeft, SWT.CHECK | SWT.LEFT);
		btnDebugging.setText(("Enable_Debugging_9")); //$NON-NLS-1$

		// row 4
		btnProfiling = new Button(cmpLeft, SWT.CHECK | SWT.LEFT);
		btnProfiling.setText(("Enable_Profiling_10")); //$NON-NLS-1$
		
		// row 5			
		new Label(cmpLeft, SWT.LEFT).setText(("Additional_Options_11")); //$NON-NLS-1$

		// row 6
		fUserOptions = new Text(cmpLeft, SWT.BORDER | SWT.LEFT);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL);
		gridData.verticalAlignment = GridData.BEGINNING;
		fUserOptions.setLayoutData(gridData);	

		// --------------------------------------------------
		// Right column
		// --------------------------------------------------

		Composite cmpRight = new Composite(ths, SWT.NONE);

		cmpRight.setLayout(new GridLayout());
		cmpRight.setLayoutData(new GridData(GridData.FILL_BOTH));

		// row 1
		new Label(cmpRight, SWT.LEFT).setText(("Warnings__8")); //$NON-NLS-1$

		// row 2
		fWarningsTable = new Table(cmpRight, SWT.BORDER | SWT.MULTI | SWT.CHECK | SWT.HIDE_SELECTION);
		fWarnAll = createTableItem(fWarningsTable, "All Warnings");
		fWarnAsErrors = createTableItem(fWarningsTable, "Warnings as errors");
		fWarnFormatStrings = createTableItem(fWarningsTable, "Bad format strings");
		fWarnPointerArith = createTableItem(fWarningsTable, "pointer aritemetic");
		fWarnSwitch = createTableItem(fWarningsTable, "No default switch statement");
		fWarnUnreachable = createTableItem(fWarningsTable, "Unreachable code");
		fWarnUnused = createTableItem(fWarningsTable, "Unused parameter");
	
		GridData grdWarnings = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		GC gc = new GC(fWarningsTable);
		gc.setFont(fWarningsTable.getFont());
		grdWarnings.widthHint = org.eclipse.jface.dialogs.Dialog.convertWidthInCharsToPixels(gc.getFontMetrics(), 35);
		gc.dispose(); 
		fWarningsTable.setLayoutData(grdWarnings);

		// set the size of this control
		ths.setSize(ths.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#initializeFrom(ICBuildConfig)
	 */
	public void initializeFrom(ICBuildConfig config) {
		boolean debug = false;
		boolean profile = false;
		boolean optimize = true;
		boolean[] warn = new boolean[7];
		int optLevel = 0;
		String userArgs = "";
	
		// can't populate with null data pointer
		if (config == null) {
			return;
		}
		
		try {
			profile = config.getAttribute(ICPosixBuildConstants.CC_ENABLE_PROFILE, false);
			debug = config.getAttribute(ICPosixBuildConstants.CC_ENABLE_DEBUG, false);
			optimize = config.getAttribute(ICPosixBuildConstants.CC_ENABLE_OPTIMIZE, false);
			optLevel = config.getAttribute(ICPosixBuildConstants.CC_OPTIMZE_LEVEL, 0);
			userArgs = config.getAttribute(ICPosixBuildConstants.CC_USER_ARGS, "");
			warn[0] = config.getAttribute(ICPosixBuildConstants.CC_WARN_ALL, false);
			warn[1] = config.getAttribute(ICPosixBuildConstants.CC_WARN_ASERROR, false);
			warn[2] = config.getAttribute(ICPosixBuildConstants.CC_WARN_FORMAT, false);
			warn[3] = config.getAttribute(ICPosixBuildConstants.CC_WARN_POINTERAR, false);
			warn[4] = config.getAttribute(ICPosixBuildConstants.CC_WARN_SWITCH, false);
			warn[5] = config.getAttribute(ICPosixBuildConstants.CC_WARN_UNREACH, false);
			warn[6] = config.getAttribute(ICPosixBuildConstants.CC_WARN_UNUSED, false);
		} catch (CoreException e) {
		}

		btnProfiling.setSelection(profile);
		btnDebugging.setSelection(debug);
		if (optimize) {
			fOptimizationLevel.select(optLevel);
		}
		fUserOptions.setText(userArgs);
		
		// check for all of the warnings (could have been better...)
		fWarnAll.setChecked(warn[0]);
		fWarnAsErrors.setChecked(warn[1]);
		fWarnFormatStrings.setChecked(warn[2]);
		fWarnPointerArith.setChecked(warn[3]);
		fWarnSwitch.setChecked(warn[4]);
		fWarnUnreachable.setChecked(warn[5]);
		fWarnUnused.setChecked(warn[6]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#performApply(ICBuildConfigWorkingCopy)
	 */
	public void performApply(ICBuildConfigWorkingCopy config) {

		boolean debug = false;
		boolean profile = false;
		boolean optimize = true;
		boolean[] warn = new boolean[7];
		int optLevel = 0;
		String userArgs = "";
	
		// can't populate with null data pointer
		if (config == null) {
			return;
		}

		profile = btnProfiling.getSelection();
		debug = btnDebugging.getSelection();
		// optimize = ???;
		optLevel = fOptimizationLevel.getSelectionIndex();
		userArgs = fUserOptions.getText();
		
		// check for all of the warnings (could have been better...)
		warn[0] = fWarnAll.getChecked();
		warn[1] = fWarnAsErrors.getChecked();
		warn[2] = fWarnFormatStrings.getChecked();
		warn[3] = fWarnPointerArith.getChecked();
		warn[4] = fWarnSwitch.getChecked();
		warn[5] = fWarnUnreachable.getChecked();
		warn[6] = fWarnUnused.getChecked();
		
		config.setAttribute(ICPosixBuildConstants.CC_ENABLE_PROFILE, profile);
		config.setAttribute(ICPosixBuildConstants.CC_ENABLE_DEBUG, debug);
		config.setAttribute(ICPosixBuildConstants.CC_ENABLE_OPTIMIZE, optimize);
		config.setAttribute(ICPosixBuildConstants.CC_OPTIMZE_LEVEL, optLevel);
		config.setAttribute(ICPosixBuildConstants.CC_USER_ARGS, userArgs);
		config.setAttribute(ICPosixBuildConstants.CC_WARN_ALL, warn[0]);
		config.setAttribute(ICPosixBuildConstants.CC_WARN_ASERROR, warn[1]);
		config.setAttribute(ICPosixBuildConstants.CC_WARN_FORMAT, warn[2]);
		config.setAttribute(ICPosixBuildConstants.CC_WARN_POINTERAR, warn[3]);
		config.setAttribute(ICPosixBuildConstants.CC_WARN_SWITCH, warn[4]);
		config.setAttribute(ICPosixBuildConstants.CC_WARN_UNREACH, warn[5]);
		config.setAttribute(ICPosixBuildConstants.CC_WARN_UNUSED, warn[6]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#setDefaults(ICBuildConfigWorkingCopy)
	 */
	public void setDefaults(ICBuildConfigWorkingCopy config) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#isValid(ICBuildConfigWorkingCopy)
	 */
	public boolean isValid(ICBuildConfigWorkingCopy config) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#getName()
	 */
	public String getName() {
		return "Compiler";
	}
}
