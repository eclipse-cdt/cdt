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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.ui.dialogs.AbstractDiscoveryPage;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.ui.newui.CDTListComparator;
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
import org.eclipse.swt.custom.SashForm;
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

    private static final String NAMESPACE = "org.eclipse.cdt.make.ui"; //$NON-NLS-1$
    private static final String POINT = "DiscoveryProfilePage"; //$NON-NLS-1$
//    private static final String NAMESPACE = "org.eclipse.cdt.managedbuilder.ui"; //$NON-NLS-1$
//    private static final String POINT = "DiscoveryProfileUI"; //$NON-NLS-1$
    protected static final String PREFIX = "ScannerConfigOptionsDialog"; //$NON-NLS-1$
    private static final String PROFILE_PAGE = "profilePage"; //$NON-NLS-1$
//    private static final String PROFILE_PATTERN = "profilePattern"; //$NON-NLS-1$
    private static final String PROFILE_ID = "profileId"; //$NON-NLS-1$
    private static final String PROFILE_NAME = "name"; //$NON-NLS-1$
    private static final String SC_GROUP_LABEL = PREFIX + ".scGroup.label"; //$NON-NLS-1$
    private static final String SC_ENABLED_BUTTON = PREFIX + ".scGroup.enabled.button"; //$NON-NLS-1$
    private static final String SC_PROBLEM_REPORTING_ENABLED_BUTTON = PREFIX + ".scGroup.problemReporting.enabled.button"; //$NON-NLS-1$
    private static final String SC_SELECTED_PROFILE_COMBO = PREFIX + ".scGroup.selectedProfile.combo"; //$NON-NLS-1$
    private static final int DEFAULT_HEIGHT = 110;
	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 10, 20 };
    
    private static final String PATTERN_PER_FILE = "PerFileProfile"; //$NON-NLS-1$ 
    private static final String PATTERN_PER_PROJ = "PerProjectProfile"; //$NON-NLS-1$ 

    private Table resTable;
    private Button scEnabledButton;
    private Button scProblemReportingEnabledButton;
    private Combo profileComboBox;
    private Combo scopeComboBox;
    private Composite profileComp;
    private Group scGroup;
    
    private ICfgScannerConfigBuilderInfo2Set cbi;
    private IScannerConfigBuilderInfo2 buildInfo;
    private CfgInfoContext iContext;
    private List pagesList = null;
    private List profilesList = null;
    private IPath configPath;
    private AbstractDiscoveryPage[] realPages;
	protected SashForm sashForm;
   
    private DiscoveryPageWrapper wrapper = null; 
     /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControls(Composite parent) {
		super.createControls(parent);
		wrapper = new DiscoveryPageWrapper(this.page, this);
		usercomp.setLayout(new GridLayout(1, false));
		
		if (page.isForProject() || page.isForPrefs()) {
			Group scopeGroup = setupGroup(usercomp, Messages.getString("DiscoveryTab.0"), 1, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
			scopeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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

		// Create the sash form
		sashForm = new SashForm(usercomp, SWT.NONE);
		sashForm.setOrientation(SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
    	
		resTable = new Table(sashForm, SWT.SINGLE|SWT.H_SCROLL|SWT.V_SCROLL|SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = 150;
		resTable.setLayoutData(gd);
		resTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleToolSelected();
			}});
		initializeProfilePageMap();
		
		Composite c = new Composite(sashForm, 0);
		c.setLayout(new GridLayout(1, false));
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		
        createScannerConfigControls(c);
        
        profileComp = new Composite(c, SWT.NONE);
        gd = new GridData(GridData.FILL, GridData.FILL, true, true);
        gd.heightHint = Dialog.convertVerticalDLUsToPixels(getFontMetrics(parent), DEFAULT_HEIGHT);
        profileComp.setLayoutData(gd);
        profileComp.setLayout(new TabFolderLayout());

        sashForm.setWeights(DEFAULT_SASH_WEIGHTS);
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
 		cbi = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(cfg);
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
 			CfgInfoContext ic = (CfgInfoContext)it.next();
 			IResourceInfo rci = ic.getResourceInfo();
 			if (rci == null) { // per configuration
 				s = ic.getConfiguration().getName();
 			} else { // per resource
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
		
		performOK();
		
		TableItem ti = resTable.getSelection()[0];
		buildInfo = (IScannerConfigBuilderInfo2)ti.getData("info"); //$NON-NLS-1$
		iContext  = (CfgInfoContext)ti.getData("cont"); //$NON-NLS-1$
        scEnabledButton.setSelection(buildInfo.isAutoDiscoveryEnabled());
        scProblemReportingEnabledButton.setSelection(buildInfo.isProblemReportingEnabled());

        profileComboBox.removeAll();
        profilesList = buildInfo.getProfileIdList();

        Collections.sort(profilesList, CDTListComparator.getInstance());
        
        realPages = new AbstractDiscoveryPage[profilesList.size()];
        Iterator it = profilesList.iterator();
        int counter = 0;
        int pos = 0;
        
        String savedId = buildInfo.getSelectedProfileId();
        
        while (it.hasNext()) {
            String profileId = (String)it.next();
 			if (!cbi.isProfileSupported(iContext, profileId)) continue; 
            
            String profileName = getProfileName(profileId);
            profileComboBox.add(profileName);
            if (profileId.equals(savedId)) pos = counter;

            buildInfo.setSelectedProfileId(profileId); // needs to create page
            
    		for (Iterator it2 = pagesList.iterator(); it2.hasNext(); ) {
    			DiscoveryProfilePageConfiguration p = (DiscoveryProfilePageConfiguration)it2.next();
    			if (p != null) {
    				AbstractDiscoveryPage pg = p.getPage();
    				if (pg != null && p.isProfileAccepted(profileId)) {
    					realPages[counter] = pg;
    					pg.setContainer(wrapper);
    					pg.createControl(profileComp);
    					profileComp.layout(true);
    					break;
    				}
    			}
    		}
    		counter ++;
        }
        buildInfo.setSelectedProfileId(savedId);
        if (profileComboBox.getItemCount() > 0) profileComboBox.select(pos);
    	enableAllControls(); 
        handleDiscoveryProfileChanged();
 	}
 	
	private void handleDiscoveryProfileChanged() {
		int pos = profileComboBox.getSelectionIndex();
		for (int i=0; i<realPages.length; i++)
			if (realPages[i] != null)
				realPages[i].setVisible(i==pos);
	}
	
    /**
     * 
     */
    private void initializeProfilePageMap() {
        pagesList = new ArrayList(5);
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				NAMESPACE, POINT);
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
        private String pattern, name;
        
        protected DiscoveryProfilePageConfiguration(IConfigurationElement element) {
            fElement = element;
            String s = fElement.getAttribute(PROFILE_ID);
            // extracting pattern from Profile Id
            // for compatibility with make.ui.
            if (s != null && s.indexOf(PATTERN_PER_PROJ) >= 0)
            	pattern = PATTERN_PER_PROJ;
            else
            	pattern = PATTERN_PER_FILE;            	
//            pattern = fElement.getAttribute(PROFILE_PATTERN);
            name = fElement.getAttribute(PROFILE_NAME);
        }
        protected String getPattern() { return pattern; }
        protected String getName() {  return name; }

        private boolean isProfileAccepted(String id) { 
        	return (id.indexOf(getPattern()) > -1);
        }
        
        private AbstractDiscoveryPage getPage() 
        {
        	AbstractDiscoveryPage discoveryPage = null;
           	try {
           		discoveryPage = (org.eclipse.cdt.make.ui.dialogs.AbstractDiscoveryPage) fElement.createExecutableExtension("class"); //$NON-NLS-1$
           	} catch (CoreException e) {}
            if (discoveryPage == null) {
//               	System.out.println(Messages.getString("DiscoveryTab.13") + fElement.getAttribute("class")); //$NON-NLS-1$  //$NON-NLS-2$
            }
            return discoveryPage;
        }
    }

	public void performApply(ICResourceDescription src,ICResourceDescription dst) {
 		ICfgScannerConfigBuilderInfo2Set cbi1 =
 			CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(getCfg(src.getConfiguration()));
 		ICfgScannerConfigBuilderInfo2Set cbi2 =
 			CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(getCfg(dst.getConfiguration()));
 		cbi2.setPerRcTypeDiscovery(cbi1.isPerRcTypeDiscovery());
 		
 		Map m1 = cbi1.getInfoMap();
 		Map m2 = cbi2.getInfoMap();
 		Iterator it2 = m2.keySet().iterator();
 		while (it2.hasNext()) {
 			CfgInfoContext ic = (CfgInfoContext)it2.next();
 			if (m1.keySet().contains(ic))  {
 				IScannerConfigBuilderInfo2 bi1 = (IScannerConfigBuilderInfo2)m1.get(ic);
 				try { 
 					cbi2.applyInfo(ic, bi1);
 				} catch (CoreException e) {
// 					System.out.println(Messages.getString("DiscoveryTab.15") + e.getLocalizedMessage()); //$NON-NLS-1$
 				}
 			} else {
// 				System.out.println(Messages.getString("DiscoveryTab.16")); //$NON-NLS-1$
 			}
 		}
	}
	
	public boolean canBeVisible() {
		if (page.isForProject() || page.isForPrefs()) return true;
		// Hide this page for folders and files 
		// if Discovery scope is "per configuration", not "per resource"
 		ICfgScannerConfigBuilderInfo2Set _cbi =
 			CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(getCfg(page.getResDesc().getConfiguration()));
 		return _cbi.isPerRcTypeDiscovery();
	}

	/**
	 * IBuildInfoContainer methods - called from dynamic pages
	 */
	public IScannerConfigBuilderInfo2 getBuildInfo() { return buildInfo; }
	public CfgInfoContext getContext() { return iContext; }
	public IProject getProject() { return page.getProject(); }
	public ICConfigurationDescription getConfiguration() { return getResDesc().getConfiguration(); }

	protected void performOK() {
		if (buildInfo == null) 
			return;
        String savedId = buildInfo.getSelectedProfileId();
		for (int i=0; i<realPages.length; i++) {
			if (realPages != null && realPages[i] != null) {
				String s = (String)profilesList.get(i);
				buildInfo.setSelectedProfileId(s);
				realPages[i].performApply();
				realPages[i].setVisible(false);
			}
		}
        buildInfo.setSelectedProfileId(savedId);
	}
	
	
	protected void performDefaults() {
 		cbi.setPerRcTypeDiscovery(true);
 		Iterator it = cbi.getInfoMap().keySet().iterator();
 		while (it.hasNext()) {
			try { 
				cbi.applyInfo((CfgInfoContext)it.next(), null);
 			} catch (CoreException e) {}
 		}
 		updateData();
	}
}
