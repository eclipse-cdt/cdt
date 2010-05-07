/*******************************************************************************
 * Copyright (c) 2008, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Ken Ryall (Nokia) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=118894
 * IBM Corporation
 * Ericsson
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.ui.ICDebuggerPage;
import org.eclipse.cdt.debug.ui.ICDebuggerPageExtension;
import org.eclipse.cdt.debug.ui.ICDebuggerPageExtension.IContentChangeListener;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CDebuggerTab extends CLaunchConfigurationTab {
	
    /**
     * Tab identifier used for ordering of tabs added using the 
     * <code>org.eclipse.debug.ui.launchConfigurationTabs</code>
     * extension point.
     *   
     * @since 2.0
     */
    public static final String TAB_ID = "org.eclipse.cdt.dsf.gdb.launch.debuggerTab"; //$NON-NLS-1$

	private final static String LOCAL_DEBUGGER_ID = "gdb";//$NON-NLS-1$
	private final static String REMOTE_DEBUGGER_ID = "gdbserver";//$NON-NLS-1$
	
	protected ILaunchConfiguration fLaunchConfiguration;
	protected ILaunchConfigurationWorkingCopy fWorkingCopy;
	protected String fDebuggerId;

	// Dynamic Debugger UI widgets
	protected ICDebuggerPage fDynamicTab;
	protected Composite fDynamicTabHolder;
	private boolean fInitDefaults;
	private Combo fDCombo;
	private boolean fIsInitializing;
	
	protected boolean fAttachMode;
	protected boolean fRemoteMode;
	protected boolean fCoreMode;
	
	protected Button fStopInMain;
	protected Text fStopInMainSymbol;
	
	private ScrolledComposite fContainer;

	private Composite fContents;

    private IContentChangeListener fContentListener = new IContentChangeListener() {
        public void contentChanged() {
            contentsChanged();
        }
    };
    
	public CDebuggerTab(SessionType sessionType, boolean attach) {
		if (sessionType == SessionType.REMOTE) {
			fRemoteMode = true;
		} else if (sessionType == SessionType.CORE) {
			fCoreMode = true;
		}
		fAttachMode = attach;
		 
		ICDebugConfiguration dc = CDebugCorePlugin.getDefault().getDefaultDefaultDebugConfiguration();
		if (dc == null) {
			CDebugCorePlugin.getDefault().getPluginPreferences().setDefault(ICDebugConstants.PREF_DEFAULT_DEBUGGER_TYPE,
					                                                        LOCAL_DEBUGGER_ID);
		}
	}

	@Override
	public String getId() {
	    return TAB_ID;
	}

	public void createControl(Composite parent) {
		fContainer = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		fContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		fContainer.setLayout(new FillLayout());
		fContainer.setExpandHorizontal(true);
		fContainer.setExpandVertical(true);
		
		fContents = new Composite(fContainer, SWT.NONE);
		setControl(fContainer);
		GdbUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(),
				ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_DEBBUGER_TAB);
		int numberOfColumns = (fAttachMode) ? 2 : 1;
		GridLayout layout = new GridLayout(numberOfColumns, false);
		fContents.setLayout(layout);
		GridData gd = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
		fContents.setLayoutData(gd);

		if (fAttachMode) {
			createDebuggerCombo(fContents);
		}
		
		createOptionsComposite(fContents);
		createDebuggerGroup(fContents, 2);
		
		fContainer.setContent(fContents);
	}

	protected void initDebuggerTypes(String selection) {
		if (fAttachMode) {
			setInitializeDefault(selection.equals("") ? true : false); //$NON-NLS-1$

			if (selection.equals("")) { //$NON-NLS-1$
				selection = LOCAL_DEBUGGER_ID;
			}

			loadDebuggerCombo(new String[] { LOCAL_DEBUGGER_ID, REMOTE_DEBUGGER_ID }, selection);
		} else {
			if (fRemoteMode) {
				setDebuggerId(REMOTE_DEBUGGER_ID);
			} else {
				setDebuggerId(LOCAL_DEBUGGER_ID);
			}
			updateComboFromSelection();
		}
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		setLaunchConfigurationWorkingCopy(config);
		ICDebuggerPage dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.setDefaults(config);
			setInitializeDefault(false);
		}
		
		if (fAttachMode && fRemoteMode) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE_ATTACH);
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, REMOTE_DEBUGGER_ID);
		} else if (fAttachMode) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, LOCAL_DEBUGGER_ID);
		} else if (fRemoteMode) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
				    IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, REMOTE_DEBUGGER_ID);
		} else if (fCoreMode){
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE);
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, LOCAL_DEBUGGER_ID);
		} else {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, LOCAL_DEBUGGER_ID);
		}
		
		if (!fAttachMode && !fCoreMode) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
					ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_DEFAULT);
		}
		
	}

	public void initializeFrom(ILaunchConfiguration config) {
		setInitializing(true);

		setLaunchConfiguration(config);
		ICDebuggerPage dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.initializeFrom(config);
		}

		try {
			String id = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, ""); //$NON-NLS-1$
			initDebuggerTypes(id);
			initializeCommonControls(config);
		} catch (CoreException e) {
		}
		setInitializing(false);
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) {
		if (getDebuggerId() != null) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, getDebuggerId());
			ICDebuggerPage dynamicTab = getDynamicTab();
			if (dynamicTab == null) {
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP, (Map<?,?>)null);
			} else {
				dynamicTab.performApply(config);
			}
		}
		
		if (fAttachMode && fRemoteMode) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE_ATTACH);
		} else if (fAttachMode) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
		} else if (fRemoteMode) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
				    IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);
		} else if (fCoreMode){
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE);
		} else {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
		}
		if (!fAttachMode && !fCoreMode) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
					fStopInMain.getSelection());
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, 
				    fStopInMainSymbol.getText());
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		if (!validateDebuggerConfig(config)) {
			return false;
		}
		if (fStopInMain != null && fStopInMainSymbol != null) {
			// The "Stop on startup at" field must not be empty
			String mainSymbol = fStopInMainSymbol.getText().trim();
			if (fStopInMain.getSelection() && mainSymbol.length() == 0) {
				setErrorMessage(LaunchMessages.getString("CDebuggerTab.Stop_on_startup_at_can_not_be_empty")); //$NON-NLS-1$
				return false;
			}
		}
		
		setErrorMessage(null);
		setMessage(null);
		if (getDebuggerId() == null) {
			setErrorMessage(LaunchMessages.getString("AbstractCDebuggerTab.No_debugger_available")); //$NON-NLS-1$
			return false;
		}

		ICDebuggerPage dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			return dynamicTab.isValid(config);
		}
		return true;
	}

	protected boolean validateDebuggerConfig(ILaunchConfiguration config) {
		String debuggerType = getDebuggerId();
		if (debuggerType == null) {
			setErrorMessage(LaunchMessages.getString("CDebuggerTab.No_debugger_available")); //$NON-NLS-1$
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
		if (!fAttachMode && !fCoreMode) {
			fStopInMain = createCheckButton(optionsComp, LaunchMessages.getString("CDebuggerTab.Stop_at_main_on_startup")); //$NON-NLS-1$
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
				public void modifyText(ModifyEvent evt) {
					update();
				}
			});
			fStopInMainSymbol.getAccessible().addAccessibleListener(
				new AccessibleAdapter() {                       
					@Override
					public void getName(AccessibleEvent e) {
                            e.result = LaunchMessages.getString("CDebuggerTab.Stop_at_main_on_startup"); //$NON-NLS-1$
					}
				}
			);
		}
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
		ICDebuggerPage debuggerPage = getDynamicTab();
		if (debuggerPage != null)
			debuggerPage.dispose();
		super.dispose();
	}

	protected void initializeCommonControls(ILaunchConfiguration config) {
		try {
			if (!fAttachMode && !fCoreMode) {
				fStopInMain.setSelection(config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
						ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_DEFAULT));
				fStopInMainSymbol.setText(config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
						ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT));
				fStopInMainSymbol.setEnabled(fStopInMain.getSelection());
			} else if (fAttachMode) {
				// In attach mode, figure out if we are doing a remote connect based on the currently
				// chosen debugger
				if (getDebuggerId().equals(REMOTE_DEBUGGER_ID)) fRemoteMode = true;
				else fRemoteMode = false;
			}
		} catch (CoreException e) {
		}
	}

	protected void setInitializeDefault(boolean init) {
		fInitDefaults = init;
	}
	
	protected void contentsChanged() {
		fContainer.setMinSize(fContents.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	protected void loadDynamicDebugArea() {
		Composite dynamicTabHolder = getDynamicTabHolder();
		// Dispose of any current child widgets in the tab holder area
		Control[] children = dynamicTabHolder.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}

		String debuggerId = getIdForCurrentDebugger();
		if (debuggerId == null) {
			setDynamicTab(null);
		} else {
			if (debuggerId.equals(LOCAL_DEBUGGER_ID)) {
				if (fCoreMode) {
					setDynamicTab(new GdbCoreDebuggerPage());
				} else {
					setDynamicTab(new GdbDebuggerPage());
				}
			} else if (debuggerId.equals(REMOTE_DEBUGGER_ID)) {
				setDynamicTab(new GdbServerDebuggerPage());
			} else {
				assert false : "Unknown debugger id"; //$NON-NLS-1$
			}
		}
		setDebuggerId(debuggerId);

		ICDebuggerPage debuggerPage = getDynamicTab();
		if (debuggerPage == null) {
			return;
		}
		// Ask the dynamic UI to create its Control
		debuggerPage.setLaunchConfigurationDialog(getLaunchConfigurationDialog());
		debuggerPage.createControl(dynamicTabHolder);
		debuggerPage.getControl().setVisible(true);
		dynamicTabHolder.layout(true);
		contentsChanged();
	}
	
	protected void setDebuggerId(String id) {
		fDebuggerId = id;
	}

	protected String getDebuggerId() {
		return fDebuggerId;
	}

	protected ICDebuggerPage getDynamicTab() {
		return fDynamicTab;
	}

	protected void setDynamicTab(ICDebuggerPage tab) {
        if ( fDynamicTab instanceof ICDebuggerPageExtension )
            ((ICDebuggerPageExtension)fDynamicTab).removeContentChangeListener( fContentListener );
        fDynamicTab = tab;
        if ( fDynamicTab instanceof ICDebuggerPageExtension )
            ((ICDebuggerPageExtension)fDynamicTab).addContentChangeListener( fContentListener );
	}

	protected Composite getDynamicTabHolder() {
		return fDynamicTabHolder;
	}

	protected void setDynamicTabHolder(Composite tabHolder) {
		fDynamicTabHolder = tabHolder;
	}

	protected ILaunchConfigurationWorkingCopy getLaunchConfigurationWorkingCopy() {
		return fWorkingCopy;
	}

	protected void setLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
		fLaunchConfiguration = launchConfiguration;
		setLaunchConfigurationWorkingCopy(null);
	}

	protected ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}

	protected void setLaunchConfigurationWorkingCopy(ILaunchConfigurationWorkingCopy workingCopy) {
		fWorkingCopy = workingCopy;
	}

	/**
	 * Overridden here so that any error message in the dynamic UI gets
	 * returned.
	 * 
	 * @see ILaunchConfigurationTab#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		ICDebuggerPage tab = getDynamicTab();
		if ( (super.getErrorMessage() != null) || (tab == null)) {
			return super.getErrorMessage();
		}
		return tab.getErrorMessage();
	}

	/**
	 * Notification that the user changed the selection of the Debugger.
	 */
	protected void handleDebuggerChanged() {
		loadDynamicDebugArea();

		// always set the newly created area with defaults
		ILaunchConfigurationWorkingCopy wc = getLaunchConfigurationWorkingCopy();
		if (getDynamicTab() == null) {
			// remove any debug specfic args from the config
			if (wc == null) {
				if (getLaunchConfiguration().isWorkingCopy()) {
					wc = (ILaunchConfigurationWorkingCopy)getLaunchConfiguration();
				}
			}
			if (wc != null) {
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP, (Map<?,?>)null);
			}
		} else {
			if (wc == null) {
				try {
					if (getLaunchConfiguration().isWorkingCopy()) {
						setLaunchConfigurationWorkingCopy((ILaunchConfigurationWorkingCopy)getLaunchConfiguration());
					} else {
						setLaunchConfigurationWorkingCopy(getLaunchConfiguration().getWorkingCopy());
					}
					wc = getLaunchConfigurationWorkingCopy();

				} catch (CoreException e) {
					return;
				}
			}
			if (initDefaults()) {
				getDynamicTab().setDefaults(wc);
			}
			setInitializeDefault(false);
			getDynamicTab().initializeFrom(wc);
		}
	}

	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		ICDebuggerPage dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.activated(workingCopy);
		}
	}

	protected boolean initDefaults() {
		return fInitDefaults;
	}

	@Override
	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_DEBUGGER_TAB);
	}

	public String getName() {
		return LaunchMessages.getString("AbstractCDebuggerTab.Debugger"); //$NON-NLS-1$
	}

	protected void createDebuggerCombo(Composite parent) {
		Composite comboComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comboComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comboComp.setLayoutData(gd);
		Label dlabel = new Label(comboComp, SWT.NONE);
		dlabel.setText(LaunchMessages.getString("Launch.common.DebuggerColon")); //$NON-NLS-1$
		fDCombo = new Combo(comboComp, SWT.READ_ONLY | SWT.DROP_DOWN);
		fDCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fDCombo.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		        if (!isInitializing()) {
		            setInitializeDefault(true);
		            updateComboFromSelection();
		        }
		    }

		    public void widgetDefaultSelected(SelectionEvent e) {
		    }
		});
	}

	protected void loadDebuggerCombo(String[] names, String current) {
		if (fDCombo == null) {
			return;
		}
		
		fDCombo.removeAll();
		int select = -1;
		for (int i = 0; i < names.length; i++) {
			fDCombo.add(names[i]);
			if (names[i].equalsIgnoreCase(current)) {
				select = i;
			}
		}

		if (select != -1) {
			fDCombo.select(select);
		} else {
			fDCombo.select(0);
		}

		updateComboFromSelection();
		getControl().getParent().layout(true);
	}

	protected void createDebuggerGroup(Composite parent, int colspan) {
		Group debuggerGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		debuggerGroup.setText(LaunchMessages.getString("CDebuggerTab.Debugger_Options")); //$NON-NLS-1$
		setDynamicTabHolder(debuggerGroup);
		GridLayout tabHolderLayout = new GridLayout();
		tabHolderLayout.marginHeight = 0;
		tabHolderLayout.marginWidth = 0;
		tabHolderLayout.numColumns = 1;
		debuggerGroup.setLayout(tabHolderLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = colspan;
		debuggerGroup.setLayoutData(gd);
	}

	protected void updateComboFromSelection() {
		handleDebuggerChanged();
		updateLaunchConfigurationDialog();
		initializeCommonControls(getLaunchConfiguration());
	}

	protected boolean isInitializing() {
		return fIsInitializing;
	}

	protected void setInitializing(boolean isInitializing) {
		fIsInitializing = isInitializing;
	}

	protected String getIdForCurrentDebugger() {
		if (fDCombo == null) {
			return getDebuggerId();
		} else {
			int selectedIndex = fDCombo.getSelectionIndex();
			return fDCombo.getItem(selectedIndex);
		}
	}
}