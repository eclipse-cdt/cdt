/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.ui.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.index.AbstractIndexerPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Bogdan Gheorghe
 */

/**
 * This <code>IndexerBlock2</code> is used in the <code>MakeProjectWizardOptionPage</code> and
 * the <code>NewManagedProjectOptionPage</code> to display the indexer options during the creation of
 * a new project.
 */

public class IndexerBlock extends AbstractCOptionPage {

	public final static String INDEX_UI_ID = CUIPlugin.PLUGIN_ID + ".cdtindexers"; //$NON-NLS-1$
	public final static String INDEXERUIID = "indexerUIID"; //$NON-NLS-1$
	public final static QualifiedName indexerUIIDKey = new QualifiedName(INDEX_UI_ID, INDEXERUIID);
	
	public static final String INDEXER_UI = "indexerUI"; //$NON-NLS-1$
	public static final String INDEXER_UI_VALUE = "indexerUIValue"; //$NON-NLS-1$
	
	private static final String INDEXER_LABEL = CUIPlugin.getResourceString("BaseIndexerBlock.label" ); //$NON-NLS-1$
	private static final String INDEXER_DESCRIPTION = CUIPlugin.getResourceString("BaseIndexerBlock.desc"); //$NON-NLS-1$
	private static final String INDEXER_COMBO_LABEL = CUIPlugin.getResourceString("BaseIndexerBlock.comboLabel"); //$NON-NLS-1$
	
	
    private Combo 					indexersComboBox;
    private HashMap 				indexerPageMap;
    private String 					selectedIndexerId = null;
	private Composite 				parentComposite;
    private ICOptionPage 		 	currentPage;
    
    
    public IndexerBlock(){
		super(INDEXER_LABEL);
		setDescription(INDEXER_DESCRIPTION);
		initializeIndexerPageMap();
	}
    
    /**
     * Create a profile page only on request
     */
    protected static class IndexerPageConfiguration {

        ICOptionPage page;
        IConfigurationElement element;

        public IndexerPageConfiguration(IConfigurationElement _element) {
            element = _element;
        }

        public ICOptionPage getPage() throws CoreException {
            if (page == null) {
                page = (ICOptionPage) element.createExecutableExtension("class"); //$NON-NLS-1$
            }
            return page;
        }
        public String getName() {
            return element.getAttribute("name"); //$NON-NLS-1$
        }
        public String getIndexerID(){
        	return element.getAttribute("indexerID"); //$NON-NLS-1$
        }
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
       
        
        Composite composite = ControlFactory.createComposite(parent, 1);
        Font font = parent.getFont();
		GridLayout layout=  ((GridLayout)composite.getLayout());
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.verticalSpacing= 0;
		layout.horizontalSpacing= GridData.FILL_HORIZONTAL;
		composite.setFont(font);
		setControl(composite);
      
		  
	    // create a composite for general scanner config discovery options
        Composite scComp = ControlFactory.createComposite(composite, 1);
        ((GridLayout)scComp.getLayout()).marginHeight = 0;
        ((GridLayout)scComp.getLayout()).marginTop = 5;
        scComp.setFont(font);
	        
        // Create a group for discovered indexer's UI
        if (createIndexerControls(scComp)) {
          // create a composite for discovery profile options
            Composite indexPageComposite = ControlFactory.createComposite(composite, 1);
            indexPageComposite.setFont(font);
            GridData gd = (GridData) indexPageComposite.getLayoutData();
            gd.grabExcessHorizontalSpace = true;
    //        gd.grabExcessVerticalSpace = true;
            gd.horizontalAlignment = GridData.FILL;
            gd.verticalAlignment = GridData.FILL;
            indexPageComposite.setLayout(new TabFolderLayout());
            // Must set the composite parent to super class.
            parentComposite = indexPageComposite;
            
            setPage();
           
        }
        parent.layout(true);
    }

 

	/**
	 * 
	 */
	private void setPage() {

        String profileId = getCurrentIndexPageId();
        
        //If no indexer has been selected, return
        if (profileId == null)
        	return;
        
        ICOptionPage page = getIndexerPage(profileId);
        if (page != null) {
            if (page.getControl() == null) {
                page.setContainer(getContainer());
                page.createControl(parentComposite);
                parentComposite.layout(true);
                parentComposite.pack(true);
            } else {
                page.setVisible(false);
            }
            page.setVisible(true);
        }
		setCurrentPage(page);
	}


	/**
	 * @param page
	 */
	private void setCurrentPage(ICOptionPage page) {
		currentPage = page;
	}



	protected String getCurrentIndexPageId() {
        String selectedIndexPageName = getSelectedIndexerID();
        
        if (selectedIndexPageName == null)
        	return null;
        
        String selectedIndexPageId = getIndexerPageId(selectedIndexPageName);
        return selectedIndexPageId;
    }
	/**
     * @param scComp
     * @param numberOfColumns
     * @return
     */
    private boolean createIndexerControls(Composite parent) {
        //TODO: Put in some logic to deal with old CDT project: upgrade old projects
        //to use the Classic CDT Indexer
        
        Group group= ControlFactory.createGroup(parent,INDEXER_COMBO_LABEL,2);
       
       // Add discovered indexers combo box
        indexersComboBox = ControlFactory.createSelectCombo(group,"", ""); //$NON-NLS-1$ //$NON-NLS-2$
  	
        //Add combo box listener
        indexersComboBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setPage();
			}
		});
        //Add button
        ControlFactory.createPushButton(group,"Info ..."); //$NON-NLS-1$
        
        // fill the combobox and set the initial value
        for (Iterator items = getIndexerPageIdList().iterator(); items.hasNext();) {
            String profileId = (String)items.next();
            String pageName = getIndexerPageName(profileId);
            if (pageName != null) {
                indexersComboBox.add(pageName);
            }
        }

        String[] indexerList = indexersComboBox.getItems();
        int selectedIndex = 0;
        for (int i=0; i<indexerList.length; i++){
        	if (indexerList[i].equals("No Indexer")) //$NON-NLS-1$
        		selectedIndex = i;
        }
        
        indexersComboBox.select(selectedIndex);
        
        return true;
    }
    
    
    
    
    /**
     * Adds all the contributed Indexer Pages to a map
     */
    private void initializeIndexerPageMap() {
        indexerPageMap = new HashMap(5);
        
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CUIPlugin.getPluginId(), "IndexerPage"); //$NON-NLS-1$
        IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getName().equals("indexerUI")) { //$NON-NLS-1$
                String id = infos[i].getAttribute("id"); //$NON-NLS-1$
                indexerPageMap.put(id, new IndexerPageConfiguration(infos[i]));
            }
        }
    }

    
    /**
     * Returns the contributed Indexer Pages as a list
     */
    protected List getIndexerPageIdList() {
        return new ArrayList(indexerPageMap.keySet());
    }
  
    /**
     * Returns the page name for the given id
     * @param profileId 
     * @return String 
     */
    protected String getIndexerPageName(String indexerPageId) {
        IndexerPageConfiguration configElement = 
                (IndexerPageConfiguration) indexerPageMap.get(indexerPageId);
        if (configElement != null) {
            return configElement.getName();
        }
        return null;
    }
    /**
     * Returns the indexer page id for the give name
     * @param indexerPageName 
     * @return String 
     */
    protected String getIndexerPageId(String indexerPageName) {
        for (Iterator I = indexerPageMap.keySet().iterator(); I.hasNext();) {
            String indexerPageId = (String) I.next();
            String tempPageName = getIndexerPageName(indexerPageId);
            if (indexerPageName.equals(tempPageName)) {
                return indexerPageId;
            }
        }
        return null;
    }
    
    protected ICOptionPage getIndexerPage(String indexerPageId) {
        IndexerPageConfiguration configElement = 
                (IndexerPageConfiguration) indexerPageMap.get(indexerPageId);
        if (configElement != null) {
            try {
                return configElement.getPage();
            } catch (CoreException e) {
            }
        }
        return null;
    }
    
    /**
     * Returns the indexer id for the given name
     * @param profileId 
     * @return String 
     */
    protected String getIndexerIdName(String indexerPageId) {
        IndexerPageConfiguration configElement = 
                (IndexerPageConfiguration) indexerPageMap.get(indexerPageId);
        if (configElement != null) {
            return configElement.getIndexerID();
        }
        return null;
    }
    
    public void performApply(IProgressMonitor monitor) throws CoreException {
    	IProject newProject = null;
		newProject = getContainer().getProject();
	
		persistIndexerSettings(newProject, monitor);
    }

    /**
     * Persists BasicIndexerBlock settings to disk and allows current indexer page to persist settings
     * This is needed since we need to pass in the project if we are trying to save changes made to the 
     * property page.
     */
    public void persistIndexerSettings(IProject project, IProgressMonitor monitor) throws CoreException{
    	
    	persistIndexerValues(project);
		
    	if (currentPage instanceof AbstractIndexerPage)
    		((AbstractIndexerPage)currentPage).setCurrentProject(project);
    	
		//Give the chosen indexer a chance to persist its values
		if (currentPage != null){
			currentPage.performApply(monitor);
		}
    }
    
    public void performDefaults() {
        // TODO Auto-generated method stub
        
    }

	/**
	 * @return
	 */
	public boolean isIndexEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	//Storage section
	
	public void persistIndexerValues(IProject project){
		ICDescriptor descriptor = null;
		Element rootElement = null;
		IProject newProject = null;
		
		//Get the currently selected indexer from the UI
		String indexerName = getSelectedIndexerID();
		//If no indexer has been selected, return
		if (indexerName == null)
			return; 
		
		//Match up the selected indexer in the UI to a corresponding 
		//contributed Indexer Page ID
		String indexerPageID = getIndexerPageId(indexerName);
		
		if (indexerPageID == null)
			return;
	
		//Get the id of the indexer that goes along with this UI page - this gets persisted
		String indexerID = getIndexerIdName(indexerPageID);
		
		try {
			newProject = project;
			descriptor = CCorePlugin.getDefault().getCProjectDescription(newProject, true);
			rootElement = descriptor.getProjectData(IndexManager.CDT_INDEXER);
		
			// Clear out all current children
			Node child = rootElement.getFirstChild();
			while (child != null) {
				rootElement.removeChild(child);
				child = rootElement.getFirstChild();
			}
			Document doc = rootElement.getOwnerDocument();
			
			saveIndexerInfo(indexerID, indexerPageID, rootElement, doc);
		
			descriptor.saveProjectData();
			
			//Update project session property
			
			project.setSessionProperty(IndexManager.indexerIDKey, indexerID);	
			project.setSessionProperty(indexerUIIDKey, indexerPageID);
	
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param oldIndexerID
	 * @param project
	 */
	public void setIndexerID(String oldIndexerID, IProject project) {
		//Get the corresponding text for the given indexer id
		selectedIndexerId = getIndexerPageName(oldIndexerID);

		//Set the appropriate indexer in the combo box
		indexersComboBox.setText(selectedIndexerId);
		//Load the appropriate page
		setPage();	
		//Give the contributed page a chance to initialize
		if (currentPage instanceof AbstractIndexerPage){
			((AbstractIndexerPage) currentPage).initialize(project);
		}
	}
	
	 
	private static void saveIndexerInfo (String indexerID, String indexerUIID, Element rootElement, Document doc ) {
		
		//Save the indexer id
		Element indexerIDElement = doc.createElement(IndexManager.INDEXER_ID);
		indexerIDElement.setAttribute(IndexManager.INDEXER_ID_VALUE,indexerID);
		rootElement.appendChild(indexerIDElement);
		
		//Save the indexer UI id
		Element indexerUIIDElement = doc.createElement(INDEXER_UI); 
		indexerUIIDElement.setAttribute(INDEXER_UI_VALUE,indexerUIID);
		rootElement.appendChild(indexerUIIDElement);
	}
	
	public String getSelectedIndexerID(){
		String indexerID = null;
		
		int selIndex = indexersComboBox.getSelectionIndex();
		
		//If no indexer has been selected return
		if (selIndex != -1)
		 indexerID = indexersComboBox.getItem(selIndex);
		
		return indexerID;
	}
}
