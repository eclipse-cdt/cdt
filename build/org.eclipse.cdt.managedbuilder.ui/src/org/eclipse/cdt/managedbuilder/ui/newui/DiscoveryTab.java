/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.newui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.make.core.scannerconfig.IConfigurationScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.ui.newui.NewUIMessages;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class DiscoveryTab extends AbstractCBuildPropertyTab implements IBuildInfoContainer {

	protected static final String PREFIX = "ScannerConfigOptionsDialog"; //$NON-NLS-1$
//    private static final String UNSAVEDCHANGES_TITLE = PREFIX + ".unsavedchanges.title"; //$NON-NLS-1$
//    private static final String UNSAVEDCHANGES_MESSAGE = PREFIX + ".unsavedchanges.message"; //$NON-NLS-1$
//    private static final String UNSAVEDCHANGES_BSAVE = PREFIX + ".unsavedchanges.button.save"; //$NON-NLS-1$
//    private static final String UNSAVEDCHANGES_BCANCEL = PREFIX + ".unsavedchanges.button.cancel"; //$NON-NLS-1$
//    private static final String ERROR_TITLE = PREFIX + ".error.title"; //$NON-NLS-1$
//    private static final String ERROR_MESSAGE = PREFIX + ".error.message"; //$NON-NLS-1$
    private static final String PROFILE_PAGE = "profilePage"; //$NON-NLS-1$
//    private static final String PROFILE_ID = "profileId"; //$NON-NLS-1$
    private static final String PROFILE_PATTERN = "profilePattern"; //$NON-NLS-1$

//    private static final String MISSING_BUILDER_MSG = "ScannerConfigOptionsDialog.label.missingBuilderInformation"; //$NON-NLS-1$

//    private static final String DIALOG_TITLE = PREFIX + ".title"; //$NON-NLS-1$
//    private static final String DIALOG_DESCRIPTION = PREFIX + ".description"; //$NON-NLS-1$
    private static final String SC_GROUP_LABEL = PREFIX + ".scGroup.label"; //$NON-NLS-1$
    private static final String SC_ENABLED_BUTTON = PREFIX + ".scGroup.enabled.button"; //$NON-NLS-1$
    private static final String SC_PROBLEM_REPORTING_ENABLED_BUTTON = PREFIX + ".scGroup.problemReporting.enabled.button"; //$NON-NLS-1$
    private static final String SC_SELECTED_PROFILE_COMBO = PREFIX + ".scGroup.selectedProfile.combo"; //$NON-NLS-1$
//    private static final String BO_PROVIDER_GROUP_LABEL = PREFIX + ".boProvider.group.label"; //$NON-NLS-1$
//    private static final String SC_APPLY_PROGRESS_MESSAGE = PREFIX + ".apply.progressMessage"; //$NON-NLS-1$ 
    private static final int DEFAULT_HEIGHT = 110;

    private Table resTable;
    private Button scEnabledButton;
    private Button scProblemReportingEnabledButton;
    private Combo profileComboBox;
    private Combo scopeComboBox;
    private Composite profileComp;
    private Group scGroup;
    
    private IConfigurationScannerConfigBuilderInfo cbi;
    private IScannerConfigBuilderInfo2 buildInfo;
    private InfoContext icontext;
    private List pagesList = null;
    private List profilesList = null;
    private IPath configPath;
     /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));

		if (page.isForProject() || page.isForPrefs()) {
			Group scopeGroup = setupGroup(usercomp, Messages.getString("DiscoveryTab.0"), 1, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			scopeGroup.setLayoutData(gd);
			scopeComboBox = new Combo(scopeGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
			scopeComboBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			scopeComboBox.add(Messages.getString("DiscoveryTab.1")); //$NON-NLS-1$
			scopeComboBox.add(Messages.getString("DiscoveryTab.2")); //$NON-NLS-1$
			scopeComboBox.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (cbi == null) return;
					cbi.setPerRcTypeDiscovery(scopeComboBox.getSelectionIndex() == 0);
					updateData();
				}
			});
		}
    	
		resTable = new Table(usercomp, SWT.SINGLE|SWT.H_SCROLL|SWT.V_SCROLL|SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.verticalSpan = 2;
		gd.widthHint = 150;
		resTable.setLayoutData(gd);
		resTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleToolSelected();
			}});
		initializeProfilePageMap();
		
        createScannerConfigControls(usercomp);
        
        profileComp = new Composite(usercomp, SWT.NONE);
        gd = new GridData(GridData.FILL, GridData.FILL, true, true);
        gd.heightHint = Dialog.convertVerticalDLUsToPixels(getFontMetrics(parent), DEFAULT_HEIGHT);
        profileComp.setLayoutData(gd);
        profileComp.setLayout(new TabFolderLayout());
    }
    
    private void createScannerConfigControls(Composite parent) {
        scGroup = setupGroup(parent, NewUIMessages.getResourceString(SC_GROUP_LABEL), 2, GridData.FILL_HORIZONTAL);
        
        scEnabledButton = setupCheck(scGroup, NewUIMessages.getResourceString(SC_ENABLED_BUTTON), 2, GridData.FILL_HORIZONTAL);
        scEnabledButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                buildInfo.setAutoDiscoveryEnabled(scEnabledButton.getSelection());
            	enableAllControls(); 
                if (scEnabledButton.getSelection()) 
                	handleDiscoveryProfileChanged();
            }
        });
        scProblemReportingEnabledButton = setupCheck(scGroup, NewUIMessages.getResourceString(SC_PROBLEM_REPORTING_ENABLED_BUTTON), 2, GridData.FILL_HORIZONTAL);
        scProblemReportingEnabledButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	buildInfo.setProblemReportingEnabled(scProblemReportingEnabledButton.getSelection());
            }
        });

        // Add profile combo box
        setupLabel(scGroup,NewUIMessages.getResourceString(SC_SELECTED_PROFILE_COMBO), 1, GridData.BEGINNING); 
        profileComboBox = new Combo(scGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        profileComboBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        profileComboBox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	int x = profileComboBox.getSelectionIndex();
            	String s = (String)profilesList.get(x); 
            	buildInfo.setSelectedProfileId(s);
                handleDiscoveryProfileChanged();
            }
        });
    }

    private void enableAllControls() {
        boolean isSCDEnabled = scEnabledButton.getSelection();
        scProblemReportingEnabledButton.setEnabled(isSCDEnabled);
        profileComboBox.setEnabled(isSCDEnabled);
        profileComp.setVisible(isSCDEnabled);
    }

 	public void updateData(ICResourceDescription rcfg) {
 		configPath = rcfg.getPath();
 		IConfiguration cfg = getCfg(rcfg.getConfiguration());
 		cbi = ScannerConfigProfileManager.getCfgScannerConfigBuildInfo(cfg);
 		updateData();
 	}
 	
 	private void updateData() {
 		if (scopeComboBox != null) 
 			scopeComboBox.select(cbi.isPerRcTypeDiscovery() ? 0 : 1);
 		
 		Map m = cbi.getInfoMap();
 		Iterator it = m.keySet().iterator();
 		int pos = resTable.getSelectionIndex();
 		resTable.removeAll();
 		while (it.hasNext()) {
 			String s = null; 
 			InfoContext ic = (InfoContext)it.next();
 			IResourceInfo rci = ic.getResourceInfo();
 			if (rci == null) { // per configuration
 				s = ic.getConfiguration().getName();
 			} else { // pre resource
 				if ( ! configPath.equals(rci.getPath())) continue;
 				IInputType typ = ic.getInputType();
 				if (typ != null) s = typ.getName();
 				if (s == null) {
 					ITool tool = ic.getTool();
 					if (tool != null) 
 						s = tool.getName();
 				}
 				if (s == null) s = Messages.getString("DiscoveryTab.3"); //$NON-NLS-1$
 			}
 			IScannerConfigBuilderInfo2 bi2 = (IScannerConfigBuilderInfo2)m.get(ic);
 			TableItem ti = new TableItem(resTable, SWT.NONE);
 			ti.setText(s);
 			ti.setData("cont", ic); //$NON-NLS-1$
 			ti.setData("info", bi2); //$NON-NLS-1$
 		}
 		int len = resTable.getItemCount(); 
 		if (len > 0) {
 			scGroup.setVisible(true);
 			profileComp.setVisible(true);
 			resTable.setEnabled(true);
 			resTable.select((pos < len && pos > -1) ? pos : 0);
 			handleToolSelected();
 		} else {
 			scGroup.setVisible(false);
 			profileComp.setVisible(false);
 			TableItem ti = new TableItem(resTable, SWT.NONE);
 			resTable.setEnabled(false);
 			ti.setText(Messages.getString("DiscoveryTab.6")); //$NON-NLS-1$
 		}
	}

 	private String getProfileName(String id) {
 		int x = id.lastIndexOf("."); //$NON-NLS-1$
 		return (x == -1) ? id : id.substring(x+1);
 	}
 	
 	private void handleToolSelected() {
		if (resTable.getSelectionCount() == 0) return;
		TableItem ti = resTable.getSelection()[0];
		buildInfo = (IScannerConfigBuilderInfo2)ti.getData("info"); //$NON-NLS-1$
		icontext  = (InfoContext)ti.getData("cont"); //$NON-NLS-1$
        scEnabledButton.setSelection(buildInfo.isAutoDiscoveryEnabled());
        scProblemReportingEnabledButton.setSelection(buildInfo.isProblemReportingEnabled());

        profileComboBox.removeAll();
        profilesList = buildInfo.getProfileIdList();
        Iterator it = profilesList.iterator();
        int counter = 0;
        int pos = 0;
        while (it.hasNext()) {
            String profileId = (String)it.next();
            String profileName = getProfileName(profileId);
            profileComboBox.add(profileName);
            if (profileId.equals(buildInfo.getSelectedProfileId())) pos = counter;
            counter ++;
        }
        if (profileComboBox.getItemCount() > 0) profileComboBox.select(pos);
    	enableAllControls(); 
        handleDiscoveryProfileChanged();
 	}
 	
	private void handleDiscoveryProfileChanged() {
		boolean found = false;
		for (Iterator it = pagesList.iterator(); it.hasNext(); ) {
			DiscoveryProfilePageConfiguration p = (DiscoveryProfilePageConfiguration)it.next();
			if (p != null) {
				AbstractDiscoveryPage pg = p.getPage();
				if (pg != null) {
					if (pg.getControl() == null) {
						pg.setContainer(this);
						pg.createControl(profileComp);
						profileComp.layout(true);
					} 
					// set visible and current only 1 page
					if (!found) {
						found = p.isProfileAccepted(buildInfo.getSelectedProfileId());
						if (found) 
							pg.initializeValues();
						pg.setVisible(found);
					} else {
						pg.setVisible(false); // for remaining pages
					}
				}
			}
		}
	}
	
    /**
     * 
     */
    private void initializeProfilePageMap() {
        pagesList = new ArrayList(5);
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				Messages.getString("DiscoveryTab.10"), Messages.getString("DiscoveryTab.11")); //$NON-NLS-1$ //$NON-NLS-2$
		if (point == null) return; 
        IConfigurationElement[] infos = point.getConfigurationElements();
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getName().equals(PROFILE_PAGE)) {
                pagesList.add(new DiscoveryProfilePageConfiguration(infos[i]));
            }
        }
    }

    /**
     * Create a profile page only on request
     * 
     * @author vhirsl
     */
    protected static class DiscoveryProfilePageConfiguration {
        IConfigurationElement fElement;
        AbstractDiscoveryPage page;
        String pattern, name; 
        public DiscoveryProfilePageConfiguration(IConfigurationElement element) {
            fElement = element;
            pattern = fElement.getAttribute(PROFILE_PATTERN);
            name = fElement.getAttribute("name"); //$NON-NLS-1$
        }
        public String getPattern() { return pattern; }
        public String getName() {  return name; }

        public boolean isProfileAccepted(String id) { 
        	return (id.indexOf(getPattern()) > -1);
        }
        
        public AbstractDiscoveryPage getPage() 
        {
            if (page == null) {
            	try {
            		page = (AbstractDiscoveryPage) fElement.createExecutableExtension("class"); //$NON-NLS-1$
            	} catch (CoreException e) {}
                if (page == null) 
                	System.out.println(Messages.getString("DiscoveryTab.13") + fElement.getAttribute("class")); //$NON-NLS-1$  //$NON-NLS-2$
            }
            return page;
        }
    }

	public void performApply(ICResourceDescription src,ICResourceDescription dst) {
 		IConfigurationScannerConfigBuilderInfo cbi1 =
 			ScannerConfigProfileManager.getCfgScannerConfigBuildInfo(getCfg(src.getConfiguration()));
 		IConfigurationScannerConfigBuilderInfo cbi2 =
 			ScannerConfigProfileManager.getCfgScannerConfigBuildInfo(getCfg(dst.getConfiguration()));
 		cbi2.setPerRcTypeDiscovery(cbi1.isPerRcTypeDiscovery());
 		
 		Map m1 = cbi1.getInfoMap();
 		Map m2 = cbi2.getInfoMap();
 		Iterator it2 = m2.keySet().iterator();
 		while (it2.hasNext()) {
 			InfoContext ic = (InfoContext)it2.next();
 			if (m1.keySet().contains(ic))  {
 				IScannerConfigBuilderInfo2 bi1 = (IScannerConfigBuilderInfo2)m1.get(ic);
 				try { 
 					cbi2.applyInfo(ic, bi1);
 				} catch (CoreException e) {
 					System.out.println(Messages.getString("DiscoveryTab.15") + e.getLocalizedMessage()); //$NON-NLS-1$
 				}
 			} else
 				System.out.println(Messages.getString("DiscoveryTab.16")); //$NON-NLS-1$
 		}
	}
	
	public boolean canBeVisible() {
		if (page.isForProject() || page.isForPrefs()) return true;
		// Hide this page for folders and files 
		// if Discovery scope is "per configuration", not "per resource"
 		IConfigurationScannerConfigBuilderInfo _cbi =
 			ScannerConfigProfileManager.getCfgScannerConfigBuildInfo(getCfg(page.getResDesc().getConfiguration()));
 		return _cbi.isPerRcTypeDiscovery();
	}

	/**
	 * IBuildInfoContainer methods - called from dynamic pages
	 */
	public IScannerConfigBuilderInfo2 getBuildInfo() { return buildInfo; }
	public InfoContext getContext() { return icontext; }
	public IProject getProject() { return page.getProject(); }
	public ICConfigurationDescription getConfiguration() { return getResDesc().getConfiguration(); }

	protected void performDefaults() {
 		cbi.setPerRcTypeDiscovery(true);
 		Iterator it = cbi.getInfoMap().keySet().iterator();
 		while (it.hasNext()) {
			try { 
				cbi.applyInfo((InfoContext)it.next(), null);
 			} catch (CoreException e) {}
 		}
 		updateData();
	}
}
