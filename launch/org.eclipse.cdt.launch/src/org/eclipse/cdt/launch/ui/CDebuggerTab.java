/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.launch.ui;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.launch.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.CLaunchConfigurationTab;
import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class CDebuggerTab extends CLaunchConfigurationTab {
	ArrayList fDinfo;
	int fDindex;
	Combo fDlist;
	Button stopInMain;

	// Dynamic Debugger UI widgets
	protected ILaunchConfigurationTab fDynamicTab;
	protected Composite fDynamicTabHolder;
	
	protected ILaunchConfigurationWorkingCopy fWorkingCopy;
	protected ILaunchConfiguration fLaunchConfiguration;
	
	public void createControl(Composite parent) {
		Composite comp= new Composite(parent, SWT.NONE);	
		setControl(comp);	
		GridLayout topLayout = new GridLayout(3, false);
		comp.setLayout(topLayout);
		Label dlabel = new Label(comp, SWT.NONE);
		dlabel.setText("Debugger:");
		fDlist = new Combo(comp, SWT.DROP_DOWN|SWT.READ_ONLY);
		fDlist.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleDebuggerComboBoxModified();
			}
		});
		stopInMain = new Button(comp, SWT.CHECK);
		stopInMain.setText("Stop at main() on startup.");
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
		stopInMain.setLayoutData(gd);
		Group debuggerGroup = new Group(comp, SWT.SHADOW_ETCHED_IN);
		debuggerGroup.setText("Debugger Options");
		setDynamicTabHolder(debuggerGroup);
		GridLayout tabHolderLayout = new GridLayout();
		tabHolderLayout.marginHeight= 0;
		tabHolderLayout.marginWidth= 0;
		tabHolderLayout.numColumns = 1;
		getDynamicTabHolder().setLayout(tabHolderLayout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		getDynamicTabHolder().setLayoutData(gd);
	}
	
	protected void setDynamicTabHolder(Composite tabHolder) {
		this.fDynamicTabHolder = tabHolder;
	}

	protected Composite getDynamicTabHolder() {
		return fDynamicTabHolder;
	}

	protected void setDynamicTab(ILaunchConfigurationTab tab) {
		fDynamicTab = tab;
	}

	protected ILaunchConfigurationTab getDynamicTab() {
		return fDynamicTab;
	}


	/**
	 * Notification that the user changed the selection in the JRE combo box.
	 */
	protected void handleDebuggerComboBoxModified() {
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
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP, (Map)null);
			}
		} else {
			if (wc == null) {
				try {
					if (getLaunchConfiguration().isWorkingCopy()) {
						// get a fresh copy to work on
						wc = ((ILaunchConfigurationWorkingCopy)getLaunchConfiguration()).getOriginal().getWorkingCopy();
					} else {
							wc = getLaunchConfiguration().getWorkingCopy();
					}
				} catch (CoreException e) {
					return;
				}
			}
			getDynamicTab().setDefaults(wc);
			getDynamicTab().initializeFrom(wc);
		}
				
		updateLaunchConfigurationDialog();		
	}
	
	protected void loadDebuggerComboBox(ILaunchConfiguration config) {
		ICDebugConfiguration[] debugConfigs;
		String platform;
		try {
			platform = getPlatform(config);
		}
		catch (CoreException e) {
			return;
		}
		if ( fDinfo != null ) {
			fDinfo.clear();
		}
		fDlist.removeAll();
		debugConfigs = CDebugCorePlugin.getDefault().getDebugConfigurations();
		fDinfo = new ArrayList(debugConfigs.length);
		for( int i = 0; i < debugConfigs.length; i++ ) {
			String supported[] = debugConfigs[i].getPlatforms();
			for( int j = 0; j < supported.length; j++ ) {
				if (supported[j].equals("*") || supported[j].equalsIgnoreCase(platform)) {
					fDinfo.add(debugConfigs[i]);
					fDlist.add(debugConfigs[i].getName());
					break;
				}
			}
		}
		fDlist.getParent().layout();
	}
	
	protected void setSelection(String id) {
		for (int i = 0; i < fDinfo.size(); i++ ) {
			ICDebugConfiguration debugConfig = (ICDebugConfiguration) fDinfo.get(i);
			if ( debugConfig != null && debugConfig.getID().equals(id) ) {
				fDlist.select(i);
				return;
			}
		}
	}
	
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		setLaunchConfigurationWorkingCopy(config);
		loadDebuggerComboBox(config);
		if ( fDinfo.size() > 0 ) {
			ICDebugConfiguration dbgCfg = (ICDebugConfiguration) fDinfo.get(0);
			if ( dbgCfg != null ) {
				setSelection(dbgCfg.getID());
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, dbgCfg.getID());
			}
		}
		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.setDefaults(config);
		}
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
	}

	public void initializeFrom(ILaunchConfiguration config) {
		String id;
		setLaunchConfiguration(config);
		loadDebuggerComboBox(config);
		try {
			id = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, "");
			setSelection(id);
			ILaunchConfigurationTab dynamicTab = getDynamicTab();
			if (dynamicTab != null) {
				dynamicTab.initializeFrom(config);
			}
			if ( config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false) == true ) {
				stopInMain.setSelection(true);
			}
		}
		catch (CoreException e) {
			return;
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) {
		if ( isValid(config) ) {
			ICDebugConfiguration dbgCfg = (ICDebugConfiguration)fDinfo.get(fDlist.getSelectionIndex());
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, dbgCfg.getID() );
			ILaunchConfigurationTab dynamicTab = getDynamicTab();
			if (dynamicTab == null) {
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP, (Map)null);
			} else {
				dynamicTab.performApply(config);
			}
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, stopInMain.getSelection());
		}
	}
	
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);

		if ( fDlist.getSelectionIndex() == -1 ) {
			setErrorMessage("No debugger avalible");
			return false;
		}

		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			return dynamicTab.isValid(config);
		}
		return true;
	}

	/**
	 * Return the class that implements <code>ILaunchConfigurationTab</code>
	 * that is registered against the debugger id of the currently selected debugger.
	 */
	protected ILaunchConfigurationTab getTabForCurrentDebugger() {
		int selectedIndex = fDlist.getSelectionIndex();
		if (selectedIndex > 0) {
			ICDebugConfiguration dbgCfg = (ICDebugConfiguration) fDinfo.get(selectedIndex);
			return CDebugUIPlugin.getDefault().getDebuggerPage(dbgCfg.getID());
		}
		return null;
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
		setDynamicTab(getTabForCurrentDebugger());
		if (getDynamicTab() == null) {
			return;
		}
			
		// Ask the dynamic UI to create its Control
		getDynamicTab().setLaunchConfigurationDialog(getLaunchConfigurationDialog());
		getDynamicTab().createControl(getDynamicTabHolder());
		getDynamicTab().getControl().setVisible(true);
		getDynamicTabHolder().layout(true);
	}

	/**
	 * Overridden here so that any error message in the dynamic UI gets returned.
	 * 
	 * @see ILaunchConfigurationTab#getErrorMessage()
	 */
	public String getErrorMessage() {
		ILaunchConfigurationTab tab = getDynamicTab();
		if ((super.getErrorMessage() != null) || (tab == null)) {
			return super.getErrorMessage();
		} else {
			return tab.getErrorMessage();
		}
	}

	protected void setLaunchConfigurationWorkingCopy(ILaunchConfigurationWorkingCopy workingCopy) {
		fWorkingCopy = workingCopy;
	}

	protected ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}

	protected void setLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
		fLaunchConfiguration = launchConfiguration;
	}

	protected ILaunchConfigurationWorkingCopy getLaunchConfigurationWorkingCopy() {
		return fWorkingCopy;
	}

	public String getName() {
		return "Debugger";
	}

	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_DEBUGGER_TAB);
	}
}
