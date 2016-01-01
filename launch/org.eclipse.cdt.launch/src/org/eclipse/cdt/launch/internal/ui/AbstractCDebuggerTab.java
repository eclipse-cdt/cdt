/*******************************************************************************
 * Copyright (c) 2005, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.launch.internal.ui;

import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebuggerPage;
import org.eclipse.cdt.debug.ui.ICDebuggerPageExtension;
import org.eclipse.cdt.debug.ui.ICDebuggerPageExtension.IContentChangeListener;
import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public abstract class AbstractCDebuggerTab extends CLaunchConfigurationTab {
	protected ILaunchConfiguration fLaunchConfiguration;
	protected ILaunchConfigurationWorkingCopy fWorkingCopy;
	protected ICDebugConfiguration fCurrentDebugConfig;

	// Dynamic Debugger UI widgets
	protected ICDebuggerPage fDynamicTab;
	protected Composite fDynamicTabHolder;
	private boolean fInitDefaults;
	private Combo fDCombo;
	private boolean fIsInitializing = false;
	private boolean fPageUpdated;

	private IContentChangeListener fContentListener = new IContentChangeListener() {
        
        /* (non-Javadoc)
         * @see org.eclipse.cdt.debug.ui.ICDebuggerPageExtension.IContentChangeListener#contentChanged()
         */
        @Override
		public void contentChanged() {
            contentsChanged();
        }
    };

    protected void setDebugConfig(ICDebugConfiguration config) {
		fCurrentDebugConfig = config;
	}

	protected ICDebugConfiguration getDebugConfig() {
		return fCurrentDebugConfig;
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
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP, (Map<String, String>) null);
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

	/**
	 * Show the contributed piece of UI that was registered for the debugger id
	 * of the currently selected debugger.
	 */
	protected void loadDynamicDebugArea() {
		// Dispose of any current child widgets in the tab holder area
		Control[] children = getDynamicTabHolder().getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}

		// Retrieve the dynamic UI for the current Debugger
		ICDebugConfiguration debugConfig = getConfigForCurrentDebugger();
		if (debugConfig == null) {
			setDynamicTab(null);
		} else {
			ICDebuggerPage tab = null;
			try {
				tab = CDebugUIPlugin.getDefault().getDebuggerPage(debugConfig.getID());
			} catch (CoreException e) {
				LaunchUIPlugin.errorDialog(LaunchMessages.AbstractCDebuggerTab_ErrorLoadingDebuggerPage, e.getStatus()); 
			}
			setDynamicTab(tab);
		}
		setDebugConfig(debugConfig);
		if (getDynamicTab() == null) {
			return;
		}
		// Ask the dynamic UI to create its Control
		getDynamicTab().setLaunchConfigurationDialog(getLaunchConfigurationDialog());
		getDynamicTab().createControl(getDynamicTabHolder());
		Control control = getDynamicTab().getControl();
		if (control != null) {
			control.setVisible(true);
		}
		getDynamicTabHolder().layout(true);
		contentsChanged();
	}

	/**
	 * Called whenever the controls within the Debugger tab has changed. 
	 */
	protected void contentsChanged() {
	}
	
	@Override
	abstract public void createControl(Composite parent);

	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		ICDebuggerPage dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.activated(workingCopy);
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		setLaunchConfiguration(config);
		ICDebuggerPage dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.initializeFrom(config);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		if (getDebugConfig() != null) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, getDebugConfig().getID());
			ICDebuggerPage dynamicTab = getDynamicTab();
			if (dynamicTab == null) {
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP, (Map<String, String>) null);
			} else {
				dynamicTab.performApply(config);
			}
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		setLaunchConfigurationWorkingCopy(config);
		ICDebuggerPage dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.setDefaults(config);
			setInitializeDefault(false);
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);
		if (getDebugConfig() == null) {
			setErrorMessage(LaunchMessages.AbstractCDebuggerTab_No_debugger_available); 
			return false;
		}

		ICDebuggerPage dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			return dynamicTab.isValid(config);
		}
		return true;
	}

	protected void setInitializeDefault(boolean init) {
		fInitDefaults = init;
	}

	protected boolean initDefaults() {
		return fInitDefaults;
	}

	@Override
	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_DEBUGGER_TAB);
	}

	@Override
	public String getName() {
		return LaunchMessages.AbstractCDebuggerTab_Debugger; 
	}

	protected void createDebuggerCombo(Composite parent, int colspan) {
		Composite comboComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comboComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colspan;
		comboComp.setLayoutData(gd);
		Label dlabel = new Label(comboComp, SWT.NONE);
		dlabel.setText(LaunchMessages.Launch_common_DebuggerColon); 
		fDCombo = new Combo(comboComp, SWT.READ_ONLY | SWT.DROP_DOWN);
		fDCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fDCombo.addSelectionListener(new SelectionListener() {
		    @Override
			public void widgetSelected(SelectionEvent e) {
		        if (!isInitializing()) {
		            setInitializeDefault(true);
		            updateComboFromSelection();
		        }
		    }

		    @Override
			public void widgetDefaultSelected(SelectionEvent e) {
		    }
		});
	}

	protected void loadDebuggerCombo(ICDebugConfiguration[] debugConfigs, String current) {
		fDCombo.removeAll();
		int select = -1;
		for (int i = 0; i < debugConfigs.length; i++) {
			fDCombo.add(debugConfigs[i].getName());
			fDCombo.setData(Integer.toString(i), debugConfigs[i]);
			if (debugConfigs[i].getID().equalsIgnoreCase(current)) {
				select = i;
			}
		}

		fPageUpdated = false;
		if (select != -1) {
			fDCombo.select(select);
		}
		//The behaviour is undefined for if the callbacks should be triggered
		// for this,
		//so force page update if needed.
		if (!fPageUpdated) {
			updateComboFromSelection();
		}
		fPageUpdated = false;
		getControl().getParent().layout(true);

	}

	protected void createDebuggerGroup(Composite parent, int colspan) {
		Group debuggerGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		debuggerGroup.setText(LaunchMessages.CDebuggerTab_Debugger_Options); 
		setDynamicTabHolder(debuggerGroup);
		GridLayout tabHolderLayout = new GridLayout();
		tabHolderLayout.marginHeight = 0;
		tabHolderLayout.marginWidth = 0;
		tabHolderLayout.numColumns = 1;
		getDynamicTabHolder().setLayout(tabHolderLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = colspan;
		getDynamicTabHolder().setLayoutData(gd);
	}

	protected void updateComboFromSelection() {
		fPageUpdated = true;
		handleDebuggerChanged();
		updateLaunchConfigurationDialog();
	}

	protected boolean isInitializing() {
		return fIsInitializing;
	}

	protected void setInitializing(boolean isInitializing) {
		fIsInitializing = isInitializing;
	}

	/**
	 * Return the class that implements <code>ICDebuggerPage</code>
	 * that is registered against the debugger id of the currently selected
	 * debugger.
	 */
	protected ICDebugConfiguration getConfigForCurrentDebugger() {
		int selectedIndex = fDCombo.getSelectionIndex();
		return (ICDebugConfiguration)fDCombo.getData(Integer.toString(selectedIndex));
	}
}
