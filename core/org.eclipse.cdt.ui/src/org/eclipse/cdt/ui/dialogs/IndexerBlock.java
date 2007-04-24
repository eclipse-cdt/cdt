/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.PropertyPage;

import com.ibm.icu.text.Collator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionPreferences;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionWorkspacePreferences;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;

/**
 * @author Bogdan Gheorghe
 */

/**
 * This <code>IndexerBlock</code> is used in the <code>MakeProjectWizardOptionPage</code> and
 * the <code>NewManagedProjectOptionPage</code> to display the indexer options during the creation of
 * a new project.
 */
public class IndexerBlock extends AbstractCOptionPage {
	private static final String NODE_INDEXERUI = "indexerUI"; //$NON-NLS-1$
    private static final String ATTRIB_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTRIB_NAME = "name"; //$NON-NLS-1$
	private static final String ATTRIB_INDEXERID = "indexerID"; //$NON-NLS-1$

	private static final String PREF_PAGE_ID = "org.eclipse.cdt.ui.preferences.IndexerPreferencePage"; //$NON-NLS-1$

	private static final String INDEXER_LABEL = "C/C++ Indexer"; //$NON-NLS-1$
	private static final String INDEXER_DESCRIPTION = "C/C++ Indexer setting for this project."; //$NON-NLS-1$
	private static final String INDEXER_COMBO_LABEL = "Select indexer"; //$NON-NLS-1$
	
	private PreferenceScopeBlock    fPrefScopeBlock;
    private Combo 					fIndexersComboBox;
    private HashMap 				fIndexerConfigMap;
	private Composite 				fIndexerPageComposite;
    private AbstractIndexerPage 	fCurrentPage;
    private Properties				fCurrentProperties;
	private Composite 				fPreferenceContent;
	private Composite 				fParent;
	private Button 					fUseActiveBuildButton;
	private Button 					fUseFixedBuildConfig;
	private Combo 					fBuildConfigComboBox;
	private ControlEnableState 		fEnableState;
    
    public IndexerBlock(){
		super(INDEXER_LABEL);
		setDescription(INDEXER_DESCRIPTION);
		initializeIndexerConfigMap();
    }

	/**
     * Create a profile page only on request
     */
    private static class IndexerConfig {
    	private AbstractIndexerPage fPage;
        private IConfigurationElement fElement;

        public IndexerConfig(IConfigurationElement element) {
        	fElement= element;
        }

        public AbstractIndexerPage getPage() throws CoreException {
            if (fPage == null) {
            	try {
            		fPage= (AbstractIndexerPage) fElement.createExecutableExtension(ATTRIB_CLASS);
            	}
            	catch (Exception e) {
            		CUIPlugin.getDefault().log(e);
            	}
            	if (fPage == null) {
            		fPage= new NullIndexerBlock();
            	}
            }
            return fPage;
        }
        
        public String getName() {
            return fElement.getAttribute(ATTRIB_NAME); 
        }
        
        public String getIndexerID(){
        	return fElement.getAttribute(ATTRIB_INDEXERID); 
        }
    }

    public void createControl(Composite parent) {
		fParent= parent;

        Composite composite = ControlFactory.createComposite(parent, 1);
		GridLayout layout=  ((GridLayout)composite.getLayout());
		layout.marginWidth= 0;
		
		GridData gd= (GridData) composite.getLayoutData();
		gd.grabExcessHorizontalSpace= true;
		setControl(composite);
      
		if (getProject() != null || getContainer() instanceof ICOptionContainerExtension) {
			fPrefScopeBlock= new PreferenceScopeBlock(PREF_PAGE_ID) {
				protected void onPreferenceScopeChange() {
					IndexerBlock.this.onPreferenceScopeChange();
				}
			};
			fPrefScopeBlock.createControl(composite);
		}		

		fPreferenceContent= ControlFactory.createComposite(composite, 1);
		layout=  (GridLayout)fPreferenceContent.getLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		gd= (GridData) fPreferenceContent.getLayoutData();
		gd.horizontalIndent= 0; 
		
		// add combo to select indexer
		Group group= ControlFactory.createGroup(fPreferenceContent,INDEXER_COMBO_LABEL, 1);
		gd= (GridData) group.getLayoutData();
		gd.grabExcessHorizontalSpace= true;
		fIndexersComboBox = ControlFactory.createSelectCombo(group,"", ""); //$NON-NLS-1$ //$NON-NLS-2$
		fIndexersComboBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onIndexerChange();
			}
		});

		// add composite for pages
        fIndexerPageComposite= ControlFactory.createComposite(group, 1);
        fIndexerPageComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));
        fIndexerPageComposite.setLayout(new TabFolderLayout());

        if (needBuildConfigOptions()) {
        	group= ControlFactory.createGroup(composite, DialogsMessages.IndexerStrategyBlock_buildConfigGroup, 1);
        	gd= (GridData) group.getLayoutData();
        	gd.grabExcessHorizontalSpace= true;
        	fUseActiveBuildButton= ControlFactory.createRadioButton(group, DialogsMessages.IndexerStrategyBlock_activeBuildConfig, null, null);
        	fUseFixedBuildConfig= ControlFactory.createRadioButton(group, DialogsMessages.IndexerBlock_fixedBuildConfig, null, null);
        	fBuildConfigComboBox= ControlFactory.createSelectCombo(group, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
        	final SelectionAdapter listener = new SelectionAdapter() {
        		public void widgetSelected(SelectionEvent e) {
        			setUseActiveBuildConfig(fUseActiveBuildButton.getSelection());
        		}
        	};
			fUseActiveBuildButton.addSelectionListener(listener);
			fUseFixedBuildConfig.addSelectionListener(listener);
        }
        
        initializeScope();
		initializeIndexerCombo();
		initializeBuildConfigs();
		onPreferenceScopeChange();
        fParent.layout(true);
    }

	protected boolean needBuildConfigOptions() {
		if (fPrefScopeBlock == null || !(getContainer() instanceof PropertyPage)) {
			return false;
		}
		IProject prj= getProject();
		if (prj != null) {
			ICProjectDescriptionManager prjDescMgr= CCorePlugin.getDefault().getProjectDescriptionManager();
			if (prjDescMgr.isNewStyleProject(prj)) {
				return true;
			}
		}
		return false;
	}
	
	private void updateBuildConfigForScope(int scope) {
		if (fBuildConfigComboBox != null) {
			if (scope == IndexerPreferences.SCOPE_INSTANCE) {
		    	ICProjectDescriptionManager prjDescMgr= CCorePlugin.getDefault().getProjectDescriptionManager();
		    	ICProjectDescriptionWorkspacePreferences prefs= prjDescMgr.getProjectDescriptionWorkspacePreferences(false);
		    	boolean useActive= prefs.getConfigurationReltations() == ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE;
		    	setUseActiveBuildConfig(useActive);
			}
		}
	}		
	
	protected void setUseActiveBuildConfig(boolean useActive) {
		if (fBuildConfigComboBox != null) {
			if (useActive) {
		    	ICProjectDescriptionManager prjDescMgr= CCorePlugin.getDefault().getProjectDescriptionManager();
		    	ICProjectDescription prefs= prjDescMgr.getProjectDescription(getProject(), false);
		    	selectBuildConfigInCombo(prefs.getActiveConfiguration().getName());
				fBuildConfigComboBox.setEnabled(false);
			}
			else {
				fBuildConfigComboBox.setEnabled(true);
			}
			fUseActiveBuildButton.setSelection(useActive);
			fUseFixedBuildConfig.setSelection(!useActive);
		}
	}

	private void enablePreferenceContent(boolean enable) {
		if (fEnableState != null) {
			fEnableState.restore();
			if (fUseActiveBuildButton != null) {
				fUseActiveBuildButton.setEnabled(true);
				fUseFixedBuildConfig.setEnabled(true);
			}
		}
		if (enable) {
			fEnableState= null;
		}
		else {
			fEnableState= ControlEnableState.disable(fPreferenceContent);
			if (fUseActiveBuildButton != null) {
				fUseActiveBuildButton.setEnabled(false);
				fUseFixedBuildConfig.setEnabled(false);
			}
		}
	}

	private void initializeScope() {
    	IProject proj= getProject();
    	if (fPrefScopeBlock == null) {
    		return;
    	}
    	
    	int scope= proj == null ? IndexerPreferences.SCOPE_INSTANCE : IndexerPreferences.getScope(proj);
    	switch(scope) {
    	case IndexerPreferences.SCOPE_PROJECT_PRIVATE:
    		fPrefScopeBlock.setProjectLocalScope();
    		break;
    	case IndexerPreferences.SCOPE_PROJECT_SHARED:
    		fPrefScopeBlock.setProjectScope();
    		break;
    	default:
    		fPrefScopeBlock.setInstanceScope();
    		break;
    	}
	}

	private void initializeIndexerCombo() {
		String[] names= new String[fIndexerConfigMap.size()];
		int j= 0;
		for (Iterator i = fIndexerConfigMap.values().iterator(); i.hasNext();) {
			IndexerConfig config = (IndexerConfig) i.next();
			names[j++]= config.getName();
        }
		Arrays.sort(names, Collator.getInstance());
		fIndexersComboBox.setItems(names);
	}

	private void initializeBuildConfigs() {
		if (fBuildConfigComboBox != null) {
	    	ICProjectDescriptionManager prjDescMgr= CCorePlugin.getDefault().getProjectDescriptionManager();
	    	ICProjectDescription prefs= prjDescMgr.getProjectDescription(getProject(), false);
	    	setUseActiveBuildConfig(prefs.getConfigurationReltations() == ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE);
	    	ICConfigurationDescription[] configs= prefs.getConfigurations();
	    	String[] names= new String[configs.length];
	    	for (int i = 0; i < configs.length; i++) {
				ICConfigurationDescription config = configs[i];
				names[i]= config.getName();
			}
			Arrays.sort(names, Collator.getInstance());
			fBuildConfigComboBox.setItems(names);
	        selectBuildConfigInCombo(prefs.getDefaultSettingConfiguration().getName());
		}
	}

	private void selectBuildConfigInCombo(String useName) {
		String[] names= fBuildConfigComboBox.getItems();
		int selectedIndex = 0;
		for (int i=0; i<names.length; i++){
			if (names[i].equals(useName))
				selectedIndex = i;
		}
		fBuildConfigComboBox.select(selectedIndex);
	}
	
    protected void onPreferenceScopeChange() {
    	int scope= computeScope();
    	updateBuildConfigForScope(scope);
    	if (fCurrentProperties == null || scope != IndexerPreferences.SCOPE_PROJECT_PRIVATE) {
        	Properties props= IndexerPreferences.getProperties(getProject(), scope);

    		String indexerId= props.getProperty(IndexerPreferences.KEY_INDEXER_ID);
    		if (getIndexerName(indexerId) == null) {
    			if (fCurrentProperties != null) {
    				props= fCurrentProperties;
    			}
    			else {
    				props= IndexerPreferences.getProperties(getProject(), IndexerPreferences.SCOPE_INSTANCE);
    			}
    		}
    		fCurrentProperties= props;
		}
		updateForNewProperties(scope);
	}

	private void updateForNewProperties(int scope) {
		String indexerId= fCurrentProperties.getProperty(IndexerPreferences.KEY_INDEXER_ID);
		String indexerName = getIndexerName(indexerId);
        String[] indexerList = fIndexersComboBox.getItems();
        int selectedIndex = 0;
        for (int i=0; i<indexerList.length; i++){
        	if (indexerList[i].equals(indexerName))
        		selectedIndex = i;
        }
        fIndexersComboBox.select(selectedIndex);
        setPage();
        if (fPrefScopeBlock != null) {
        	enablePreferenceContent(scope != IndexerPreferences.SCOPE_INSTANCE);
        }
	}

	protected void onIndexerChange() {
    	if (fCurrentPage != null) {
    		Properties props= fCurrentPage.getProperties();
    		if (props != null) {
    			fCurrentProperties.putAll(props);
    		}
    	}
    	setPage();
    }

	private int computeScope() {
		if (fPrefScopeBlock != null) {
			if (fPrefScopeBlock.isProjectLocalScope()) {
				return IndexerPreferences.SCOPE_PROJECT_PRIVATE;
			}
			if (fPrefScopeBlock.isProjectScope()) {
				return IndexerPreferences.SCOPE_PROJECT_SHARED;
			}
		}
		return IndexerPreferences.SCOPE_INSTANCE;
	}
	
    private void setPage() {
        String indexerID= getSelectedIndexerID();
        AbstractIndexerPage page = getIndexerPage(indexerID);
        if (page != null) {
            if (page.getControl() == null) {
                page.setContainer(getContainer());
                page.createControl(fIndexerPageComposite);
                fIndexerPageComposite.layout(true);
                fParent.layout(true);
            }
        }
        
        if (fCurrentPage != null){
        	fCurrentPage.setVisible(false);
        }
            
        if (page != null) {
        	page.setProperties(fCurrentProperties);
        	page.setVisible(true);
        }
        
        fCurrentPage= page;
	}


	/**
     * Adds all the contributed Indexer Pages to a map
     */
    private void initializeIndexerConfigMap() {
        fIndexerConfigMap = new HashMap(5);        
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CUIPlugin.getPluginId(), "IndexerPage"); //$NON-NLS-1$
        IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getName().equals(NODE_INDEXERUI)) { 
                String id = infos[i].getAttribute(ATTRIB_INDEXERID);
                if (id != null) {
                	IndexerConfig config= new IndexerConfig(infos[i]);
                	if (config.getName() != null) {
                		fIndexerConfigMap.put(id, config);
                	}
                }
            }
        }
    }
  
    private String getIndexerName(String indexerID) {
        IndexerConfig configElement= (IndexerConfig) fIndexerConfigMap.get(indexerID);
        if (configElement != null) {
            return configElement.getName();
        }
        return null;
    }

    private String getIndexerID(String indexerName) {
    	for (Iterator i = fIndexerConfigMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			String id = (String) entry.getKey();
			IndexerConfig config = (IndexerConfig) entry.getValue();
			if (indexerName.equals(config.getName())) {
				return id;
			}
    	}
    	return null;
    }
    
    private AbstractIndexerPage getIndexerPage(String indexerID) {
        IndexerConfig configElement= (IndexerConfig) fIndexerConfigMap.get(indexerID);
        if (configElement != null) {
            try {
                return configElement.getPage();
            } catch (CoreException e) {
            	CUIPlugin.getDefault().log(e);
            }
        }
        return null;
    }
    
    public void performApply(IProgressMonitor monitor) throws CoreException {
    	int scope= computeScope();
    	IProject project= getProject();
    	String indexerID = getSelectedIndexerID();
    	if (indexerID == null) {
    		return;
    	}

    	if (scope != IndexerPreferences.SCOPE_INSTANCE || project == null) {
    		Properties props= new Properties();
    		props.setProperty(IndexerPreferences.KEY_INDEXER_ID, indexerID);
    		if (fCurrentPage != null) {
    			Properties p1= fCurrentPage.getProperties();
    			if (p1 != null) {
    				props.putAll(p1);
    			}
    		}
        	IndexerPreferences.setProperties(project, scope, props);
    	}
    	
    	if (project != null) {
    		IndexerPreferences.setScope(project, scope);
    	}
    	
    	if (fBuildConfigComboBox != null) {
    		boolean useActive= fUseActiveBuildButton.getSelection();
	    	ICProjectDescriptionManager prjDescMgr= CCorePlugin.getDefault().getProjectDescriptionManager();
	    	ICProjectDescription prefs= prjDescMgr.getProjectDescription(getProject(), true);
    		if (scope == IndexerPreferences.SCOPE_INSTANCE) {
		    	prefs.useDefaultConfigurationRelations();
    		}
    		else {
    			prefs.setConfigurationRelations(useActive
    					? ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE 
    				    : ICProjectDescriptionPreferences.CONFIGS_INDEPENDENT);
    		}
    		if (!useActive) {
    			final ICConfigurationDescription config= prefs.getConfigurationByName(fBuildConfigComboBox.getText());
    			if (config != null) {
    				prefs.setDefaultSettingConfiguration(config);
    			}
    		}
    		prjDescMgr.setProjectDescription(getProject(), prefs);
    	}
    	CCoreInternals.savePreferences(project);
    }

    public void performDefaults() {
    	fCurrentProperties= null;
    	if (fPrefScopeBlock != null) {
    		fPrefScopeBlock.setInstanceScope();
        	onPreferenceScopeChange();
    	}
    	else {
    		fCurrentProperties= IndexerPreferences.getDefaultIndexerProperties();
    		updateForNewProperties(IndexerPreferences.SCOPE_INSTANCE);
    	}
    }

    /**
     * @deprecated always returns false
     */
	public boolean isIndexEnabled() {
		return false;
	}

	private String getSelectedIndexerID(){
		return getIndexerID(fIndexersComboBox.getText());
	}
	
	public IProject getProject() {
		ICOptionContainer container = getContainer();
		if (container != null){
			if (container instanceof ICOptionContainerExtension) {
				try {
					return ((ICOptionContainerExtension) container).getProjectHandle();
				}
				catch (Exception e) {
					return null;
				}
			}
			return container.getProject();
		}
		return null;
	}
}
