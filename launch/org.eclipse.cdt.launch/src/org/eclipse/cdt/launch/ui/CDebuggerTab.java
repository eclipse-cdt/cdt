/*******************************************************************************
 * Copyright (c) 2005, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Ken Ryall (Nokia) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=118894
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.launch.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.ui.ICDebuggerPage;
import org.eclipse.cdt.launch.LaunchUtils;
import org.eclipse.cdt.launch.internal.ui.AbstractCDebuggerTab;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.MessageFormat;

public class CDebuggerTab extends AbstractCDebuggerTab {

    /**
     * Tab identifier used for ordering of tabs added using the 
     * <code>org.eclipse.debug.ui.launchConfigurationTabs</code>
     * extension point.
     *   
     * @since 6.0
     */
    public static final String TAB_ID = "org.eclipse.cdt.cdi.launch.debuggerTab"; //$NON-NLS-1$
    
	public class AdvancedDebuggerOptionsDialog extends Dialog {

		private Button fVarBookKeeping;

		private Button fRegBookKeeping;

		/**
		 * Constructor for AdvancedDebuggerOptionsDialog.
		 */
		protected AdvancedDebuggerOptionsDialog(Shell parentShell) {
			super(parentShell);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite)super.createDialogArea(parent);
			Group group = new Group(composite, SWT.NONE);
			group.setText(LaunchMessages.CDebuggerTab_Automatically_track_values_of); 
			GridLayout layout = new GridLayout();
			group.setLayout(layout);
			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			fVarBookKeeping = new Button(group, SWT.CHECK);
			fVarBookKeeping.setText(LaunchMessages.CDebuggerTab_Variables); 
			fRegBookKeeping = new Button(group, SWT.CHECK);
			fRegBookKeeping.setText(LaunchMessages.CDebuggerTab_Registers); 
			initialize();
			return composite;
		}

		@Override
		protected void okPressed() {
			saveValues();
			super.okPressed();
		}

		private void initialize() {
			Map attr = getAdvancedAttributes();
			Object varBookkeeping = attr.get(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING);
			fVarBookKeeping.setSelection((varBookkeeping instanceof Boolean) ? !((Boolean)varBookkeeping).booleanValue() : true);
			Object regBookkeeping = attr.get(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING);
			fRegBookKeeping.setSelection((regBookkeeping instanceof Boolean) ? !((Boolean)regBookkeeping).booleanValue() : true);
		}

		private void saveValues() {
			Map attr = getAdvancedAttributes();
			Boolean varBookkeeping = Boolean.valueOf(!fVarBookKeeping.getSelection());
			attr.put(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, varBookkeeping);
			Boolean regBookkeeping = Boolean.valueOf(!fRegBookKeeping.getSelection());
			attr.put(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, regBookkeeping);
			update();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
		 */
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(LaunchMessages.CDebuggerTab_Advanced_Options_Dialog_Title); 
		}
	}

	final protected boolean fAttachMode;

	protected Button fAdvancedButton;
	protected Button fStopInMain;
	protected Text fStopInMainSymbol;
	protected Button fAttachButton;

	private Map fAdvancedAttributes = new HashMap(5);

	private ScrolledComposite fContainer;

	private Composite fContents;

	public CDebuggerTab(boolean attachMode) {
		fAttachMode = attachMode;
		// If the default debugger has not been set, use the MI debugger.
		// The MI plug-in also does this, but it may not have been loaded yet. Bug 158391.
		ICDebugConfiguration dc = CDebugCorePlugin.getDefault().getDefaultDefaultDebugConfiguration();
		if (dc == null) {
			CDebugCorePlugin.getDefault().getPluginPreferences().setDefault(ICDebugConstants.PREF_DEFAULT_DEBUGGER_TYPE, "org.eclipse.cdt.debug.mi.core.CDebuggerNew"); //$NON-NLS-1$
		}
	}

	@Override
	public String getId() {
	    return TAB_ID;
	}
	
	@Override
	public void createControl(Composite parent) {
		fContainer = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		fContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		fContainer.setLayout(new FillLayout());
		fContainer.setExpandHorizontal(true);
		fContainer.setExpandVertical(true);
		
		fContents = new Composite(fContainer, SWT.NONE);
		setControl(fContainer);
		LaunchUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(),
				ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_DEBBUGER_TAB);
		int numberOfColumns = (fAttachMode) ? 2 : 1;
		GridLayout layout = new GridLayout(numberOfColumns, false);
		fContents.setLayout(layout);
		GridData gd = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
		fContents.setLayoutData(gd);

		createDebuggerCombo(fContents, (fAttachMode) ? 1 : 2);
		createOptionsComposite(fContents);
		createDebuggerGroup(fContents, 2);
		
		fContainer.setContent(fContents);
	}

	protected void loadDebuggerComboBox(ILaunchConfiguration config, String selection) {
		ICDebugConfiguration[] debugConfigs;
		String configPlatform = getPlatform(config);
		debugConfigs = CDebugCorePlugin.getDefault().getActiveDebugConfigurations();
		Arrays.sort(debugConfigs, new Comparator<ICDebugConfiguration>() {
			@Override
			public int compare(ICDebugConfiguration c1, ICDebugConfiguration c2) {
				return Collator.getInstance().compare(c1.getName(), c2.getName());
			}
		});
		List list = new ArrayList();
		String mode;
		if (fAttachMode) {
			mode = ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH;
		} else {
			mode = ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN;
		}
		if (selection.equals("")) { //$NON-NLS-1$
			ICDebugConfiguration dc = CDebugCorePlugin.getDefault().getDefaultDebugConfiguration();
			if (dc == null) {
				CDebugCorePlugin.getDefault().saveDefaultDebugConfiguration("org.eclipse.cdt.debug.mi.core.CDebuggerNew");
				dc = CDebugCorePlugin.getDefault().getDefaultDebugConfiguration();
			}
			if (dc != null)
				selection = dc.getID();
		}
		String defaultSelection = selection;
		for (int i = 0; i < debugConfigs.length; i++) {
			if (debugConfigs[i].supportsMode(mode)) {
				String debuggerPlatform = debugConfigs[i].getPlatform();
				if (validatePlatform(config, debugConfigs[i])) {
					list.add(debugConfigs[i]);
					// select first exact matching debugger for platform or
					// requested selection
					if ((defaultSelection.equals("") && debuggerPlatform.equalsIgnoreCase(configPlatform))) { //$NON-NLS-1$
						defaultSelection = debugConfigs[i].getID();
					}
				}
			}
		}
		// if no selection meaning nothing in config the force initdefault on
		// tab
		setInitializeDefault(selection.equals("") ? true : false); //$NON-NLS-1$
		loadDebuggerCombo((ICDebugConfiguration[])list.toArray(new ICDebugConfiguration[list.size()]), defaultSelection);
	}

	@Override
	protected void updateComboFromSelection() {
		super.updateComboFromSelection();
		initializeCommonControls(getLaunchConfiguration());
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		super.setDefaults(config);
		if (fAttachMode) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
		} else {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
					ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_DEFAULT);
		}
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, false);
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, false);
		
		// Set the default debugger based on the active toolchain on the project (if possible)
		String defaultDebugger = null;
		try {
			String projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			if (projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            	ICProjectDescription projDesc = CoreModel.getDefault().getProjectDescription(project);
            	ICConfigurationDescription configDesc = projDesc.getActiveConfiguration();
            	String configId = configDesc.getId();
        		ICDebugConfiguration[] debugConfigs = CDebugCorePlugin.getDefault().getActiveDebugConfigurations();
        		outer: for (int i = 0; i < debugConfigs.length; ++i) {
        			ICDebugConfiguration debugConfig = debugConfigs[i];
        			String[] patterns = debugConfig.getSupportedBuildConfigPatterns();
        			if (patterns != null) {
        				for (int j = 0; j < patterns.length; ++j) {
        					if (configId.matches(patterns[j])) {
        						defaultDebugger = debugConfig.getID();
        						break outer;
        					}
        				}
        			}
        		}
			}
		} catch (CoreException e) {
		}
		
		if (defaultDebugger == null) {
			ICDebugConfiguration dc = CDebugCorePlugin.getDefault().getDefaultDebugConfiguration();
			if (dc != null) {
				defaultDebugger = dc.getID();
			}
		}
		
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, defaultDebugger);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		setInitializing(true);
		super.initializeFrom(config);
		try {
			String id = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, ""); //$NON-NLS-1$
			loadDebuggerComboBox(config, id);
			initializeCommonControls(config);
		} catch (CoreException e) {
		}
		setInitializing(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		super.performApply(config);
		if (fAttachMode) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
		} else {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, fStopInMain.getSelection());
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, fStopInMainSymbol.getText());
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
		}
		applyAdvancedAttributes(config);
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		if (!validateDebuggerConfig(config)) {
			return false;
		}
		ICDebugConfiguration debugConfig = getDebugConfig();
		String mode = fAttachMode
				? ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH
				: ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN;
		if (!debugConfig.supportsMode(mode)) {
			setErrorMessage(MessageFormat.format(LaunchMessages.CDebuggerTab_Mode_not_supported, new String[]{mode})); 
			return false;
		}
		if (fStopInMain != null && fStopInMainSymbol != null) {
			// The "Stop on startup at" field must not be empty
			String mainSymbol = fStopInMainSymbol.getText().trim();
			if (fStopInMain.getSelection() && mainSymbol.length() == 0) {
				setErrorMessage(LaunchMessages.CDebuggerTab_Stop_on_startup_at_can_not_be_empty); 
				return false;
			}
		}
		if (super.isValid(config) == false) {
			return false;
		}
		return true;
	}

	protected boolean validatePlatform(ILaunchConfiguration config, ICDebugConfiguration debugConfig) {
		String configPlatform = getPlatform(config);
		String debuggerPlatform = debugConfig.getPlatform();
		return (debuggerPlatform.equals("*") || debuggerPlatform.equalsIgnoreCase(configPlatform)); //$NON-NLS-1$
	}

	protected boolean validateCPU(ILaunchConfiguration config, ICDebugConfiguration debugConfig) {
		IBinaryObject binaryFile = null;
		try {
			binaryFile = getBinary(config);
		} catch (CoreException e) {
			setErrorMessage(e.getLocalizedMessage());
		}
		String projectCPU = ICDebugConfiguration.CPU_NATIVE;
		if (binaryFile != null) {
			projectCPU = binaryFile.getCPU();
		}
		return debugConfig.supportsCPU(projectCPU);
	}

	protected IBinaryObject getBinary(ILaunchConfiguration config) throws CoreException {
		String programName = null;
		String projectName = null;
		try {
			projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
			programName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String)null);
		} catch (CoreException e) {
		}
		if (programName != null) {
			return LaunchUtils.getBinary(programName, projectName);
		}
		return null;
	}

	protected boolean validateDebuggerConfig(ILaunchConfiguration config) {
		ICDebugConfiguration debugConfig = getDebugConfig();
		if (debugConfig == null) {
			setErrorMessage(LaunchMessages.CDebuggerTab_No_debugger_available); 
			return false;
		}
		// We do not validate platform and CPU compatibility to avoid accidentally disabling
		// a valid configuration. It's much better to let an incompatible configuration through
		// than to disable a valid one.
		return true;
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	protected void update() {
		if (!isInitializing()) {
			super.updateLaunchConfigurationDialog();
		}
	}

	protected void createOptionsComposite(Composite parent) {
		Composite optionsComp = new Composite(parent, SWT.NONE);
		int numberOfColumns = (fAttachMode) ? 1 : 3;
		GridLayout layout = new GridLayout(numberOfColumns, false);
		optionsComp.setLayout(layout);
		optionsComp.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false, 1, 1));
		if (fAttachMode == false) {
			fStopInMain = createCheckButton(optionsComp, LaunchMessages.CDebuggerTab_Stop_at_main_on_startup);
			fStopInMain.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					fStopInMainSymbol.setEnabled(fStopInMain.getSelection());
					update();
				}
			});
			fStopInMainSymbol = new Text(optionsComp, SWT.SINGLE | SWT.BORDER);
			final GridData gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
			gridData.widthHint = 100;
			fStopInMainSymbol.setLayoutData(gridData);
			fStopInMainSymbol.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent evt) {
					update();
				}
			});
			fStopInMainSymbol.getAccessible().addAccessibleListener(
				new AccessibleAdapter() {                       
					@Override
					public void getName(AccessibleEvent e) {
						e.result = LaunchMessages.CDebuggerTab_Stop_at_main_on_startup;
					}
				}
			);
		}
		fAdvancedButton = createPushButton(optionsComp, LaunchMessages.CDebuggerTab_Advanced, null); 
		((GridData)fAdvancedButton.getLayoutData()).horizontalAlignment = GridData.END;
		fAdvancedButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Dialog dialog = new AdvancedDebuggerOptionsDialog(getShell());
				dialog.open();
			}
		});
	}

	protected Map getAdvancedAttributes() {
		return fAdvancedAttributes;
	}

	private void initializeAdvancedAttributes(ILaunchConfiguration config) {
		Map attr = getAdvancedAttributes();
		try {
			Boolean varBookkeeping = (config.getAttribute(
					ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, false))
					? Boolean.TRUE
					: Boolean.FALSE;
			attr.put(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, varBookkeeping);
		} catch (CoreException e) {
		}
		try {
			Boolean regBookkeeping = (config.getAttribute(
					ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, false))
					? Boolean.TRUE
					: Boolean.FALSE;
			attr.put(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, regBookkeeping);
		} catch (CoreException e) {
		}
	}

	private void applyAdvancedAttributes(ILaunchConfigurationWorkingCopy config) {
		Map attr = getAdvancedAttributes();
		Object varBookkeeping = attr.get(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING);
		if (varBookkeeping instanceof Boolean)
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING,
					((Boolean)varBookkeeping).booleanValue());
		Object regBookkeeping = attr.get(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING);
		if (regBookkeeping instanceof Boolean)
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING,
					((Boolean)regBookkeeping).booleanValue());
	}

	@Override
	protected Shell getShell() {
		return super.getShell();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	@Override
	public void dispose() {
		getAdvancedAttributes().clear();
		ICDebuggerPage debuggerPage = getDynamicTab();
		if (debuggerPage != null)
			debuggerPage.dispose();
		super.dispose();
	}

	protected void initializeCommonControls(ILaunchConfiguration config) {
		try {
			if (!fAttachMode) {
				fStopInMain.setSelection(config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
						ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_DEFAULT));
				fStopInMainSymbol.setText(config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
						ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT));
				fStopInMainSymbol.setEnabled(fStopInMain.getSelection());
			}
			initializeAdvancedAttributes(config);
		} catch (CoreException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.launch.internal.ui.AbstractCDebuggerTab#setInitializeDefault(boolean)
	 */
	@Override
	protected void setInitializeDefault(boolean init) {
		super.setInitializeDefault(init);
	}
	
	@Override
	protected void contentsChanged() {
		fContainer.setMinSize(fContents.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
}