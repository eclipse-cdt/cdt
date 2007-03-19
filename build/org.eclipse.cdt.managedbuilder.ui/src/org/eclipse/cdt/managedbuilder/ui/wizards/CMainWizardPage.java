/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;
	import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.ui.properties.Messages;
import org.eclipse.cdt.managedbuilder.ui.properties.PageLayout;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter;

	public class CMainWizardPage extends WizardPage implements IToolChainListListener {

		public static final String EXTENSION_POINT_ID = "org.eclipse.cdt.managedbuilder.ui.CDTWizard"; //$NON-NLS-1$
		public static final String ELEMENT_NAME = "wizard"; //$NON-NLS-1$
		public static final String CLASS_NAME = "class"; //$NON-NLS-1$

		public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.NewModelProjectWizardPage"; //$NON-NLS-1$

	       // initial value stores
	    private String initialProjectFieldValue;

	    // widgets
	    private Text projectNameField;
	    private Tree tree;
	    private Composite right;
	    private Button show_sup;
	    private Label right_label;
   
	    private CConfigWizardPage next;
	    protected ICWizardHandler h_selected = null;

	    private Listener nameModifyListener = new Listener() {
	        public void handleEvent(Event e) {
	        	setLocationForSelection();
	            setPageComplete(validatePage());
	        }
	    };

		private ProjectContentsLocationArea locationArea;

	    // constants
	    private static final int SIZING_TEXT_FIELD_WIDTH = 250;

	    /**
	     * Creates a new project creation wizard page.
	     *
	     * @param pageName the name of this page
	     */
	    public CMainWizardPage(String pageName, CConfigWizardPage _next) {
	        super(pageName);
	        setPageComplete(false);
	        next = _next;
	    }

	    /** (non-Javadoc)
	     * Method declared on IDialogPage.
	     */
	    public void createControl(Composite parent) {
	        Composite composite = new Composite(parent, SWT.NULL);
	        composite.setFont(parent.getFont());

	        initializeDialogUnits(parent);

	        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
	                IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

	        composite.setLayout(new GridLayout());
	        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

	        createProjectNameGroup(composite);
	        locationArea = new ProjectContentsLocationArea(getErrorReporter(), composite);
	        if(initialProjectFieldValue != null) {
				locationArea.updateProjectName(initialProjectFieldValue);
			}

			// Scale the button based on the rest of the dialog
			setButtonLayoutData(locationArea.getBrowseButton());
			
			createDynamicGroup(composite); 
			
			switchTo(updateData(tree, right, show_sup, CMainWizardPage.this));

			setPageComplete(validatePage());
	        // Show description on opening
	        setErrorMessage(null);
	        setMessage(null);
	        setControl(composite);
	    }
	    
	    private void createDynamicGroup(Composite parent) {
	        Composite c = new Composite(parent, SWT.NONE);
	        c.setLayoutData(new GridData(GridData.FILL_BOTH));
	    	c.setLayout(new GridLayout(2, true));
	    	
	        Label l1 = new Label(c, SWT.NONE);
	        l1.setText(Messages.getString("CMainWizardPage.0")); //$NON-NLS-1$
	        l1.setFont(parent.getFont());
	        l1.setLayoutData(new GridData(GridData.BEGINNING));
	        
	        right_label = new Label(c, SWT.NONE);
	        right_label.setFont(parent.getFont());
	        right_label.setLayoutData(new GridData(GridData.BEGINNING));
	    	
	        tree = new Tree(c, SWT.SINGLE | SWT.BORDER);
	        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
	        tree.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					TreeItem[] tis = tree.getSelection();
					if (tis == null || tis.length == 0) return;
					switchTo((ICWizardHandler)tis[0].getData());
					setPageComplete(validatePage());
				}});
	        
	        right = new Composite(c, SWT.NONE);
	        right.setLayoutData(new GridData(GridData.FILL_BOTH));
	        right.setLayout(new PageLayout());

	        show_sup = new Button(c, SWT.CHECK);
	        show_sup.setText(Messages.getString("CMainWizardPage.1")); //$NON-NLS-1$
	        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
	        gd.horizontalSpan = 2;
	        show_sup.setLayoutData(gd);
	        show_sup.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (h_selected != null)
						h_selected.setSupportedOnly(show_sup.getSelection());
					switchTo(updateData(tree, right, show_sup, CMainWizardPage.this));
				}} );

	        // restore settings from preferences
			show_sup.setSelection(!CDTPrefUtil.getBool(CDTPrefUtil.KEY_UNSUPP));
	    }
	    
	    /**
		 * Get an error reporter for the receiver.
		 * @return IErrorMessageReporter
		 */
		private IErrorMessageReporter getErrorReporter() {
			return new IErrorMessageReporter(){
				/* (non-Javadoc)
				 * @see org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter#reportError(java.lang.String)
				 */
				public void reportError(String errorMessage) {
					setErrorMessage(errorMessage);
					boolean valid = errorMessage == null;
					if(valid) {
						valid = validatePage();
					}
					
					setPageComplete(valid);
				}
			};
		}

	    public IWizardPage getNextPage() {
			if (h_selected == null || h_selected.isDummy()) // cannot continue
				return null;
//			MBSCustomPageManager.setPageHideStatus(next.pageID, false);
			return next;
	    }		
	    /**
	     * Creates the project name specification controls.
	     *
	     * @param parent the parent composite
	     */
	    private final void createProjectNameGroup(Composite parent) {
	        // project specification group
	        Composite projectGroup = new Composite(parent, SWT.NONE);
	        GridLayout layout = new GridLayout();
	        layout.numColumns = 2;
	        projectGroup.setLayout(layout);
	        projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	        // new project label
	        Label projectLabel = new Label(projectGroup, SWT.NONE);
	        projectLabel.setText(IDEWorkbenchMessages.WizardNewProjectCreationPage_nameLabel);
	        projectLabel.setFont(parent.getFont());

	        // new project name entry field
	        projectNameField = new Text(projectGroup, SWT.BORDER);
	        GridData data = new GridData(GridData.FILL_HORIZONTAL);
	        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	        projectNameField.setLayoutData(data);
	        projectNameField.setFont(parent.getFont());

	        // Set the initial value first before listener
	        // to avoid handling an event during the creation.
	        if (initialProjectFieldValue != null) {
				projectNameField.setText(initialProjectFieldValue);
			}
	        projectNameField.addListener(SWT.Modify, nameModifyListener);
	    }

	    /**
	     * Creates a project resource handle for the current project name field value.
	     * <p>
	     * This method does not create the project resource; this is the responsibility
	     * of <code>IProject::create</code> invoked by the new project resource wizard.
	     * </p>
	     *
	     * @return the new project resource handle
	     */
	    private IProject getProjectHandle() {
	    	return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
	    }

	    /**
	     * Returns the current project name as entered by the user, or its anticipated
	     * initial value.
	     *
	     * @return the project name, its anticipated initial value, or <code>null</code>
	     *   if no project name is known
	     */
	    
	    public String getProjectName() {
	        if (projectNameField == null) {
				return initialProjectFieldValue;
			}
	        return getProjectNameFieldValue();
	    }

	    public IPath getProjectLocation() {
	    	if (locationArea.isDefault()) return null;
	    	return new Path(locationArea.getProjectLocation());
	    }
	    
	    /**
	     * Returns the value of the project name field
	     * with leading and trailing spaces removed.
	     * 
	     * @return the project name in the field
	     */
	    private String getProjectNameFieldValue() {
	        if (projectNameField == null) {
				return ""; //$NON-NLS-1$
			}

	        return projectNameField.getText().trim();
	    }

	    /**
	     * Sets the initial project name that this page will use when
	     * created. The name is ignored if the createControl(Composite)
	     * method has already been called. Leading and trailing spaces
	     * in the name are ignored.
	     * Providing the name of an existing project will not necessarily 
	     * cause the wizard to warn the user.  Callers of this method 
	     * should first check if the project name passed already exists 
	     * in the workspace.
	     * 
	     * @param name initial project name for this page
	     * 
	     * @see IWorkspace#validateName(String, int)
	     * 
	     */
	    public void setInitialProjectName(String name) {
	        if (name == null) {
				initialProjectFieldValue = null;
			} else {
	            initialProjectFieldValue = name.trim();
	            if(locationArea != null) {
					locationArea.updateProjectName(name.trim());
				}
	        }
	    }

	    /**
	     * Set the location to the default location if we are set to useDefaults.
	     */
	    void setLocationForSelection() {
	    	locationArea.updateProjectName(getProjectNameFieldValue());
	    }

	  
	    /**
	     * Returns whether this page's controls currently all contain valid 
	     * values.
	     *
	     * @return <code>true</code> if all controls are valid, and
	     *   <code>false</code> if at least one is invalid
	     */
	    protected boolean validatePage() {
	        IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
    		setMessage(null);

            String projectFieldContents = getProjectNameFieldValue();
	        if (projectFieldContents.length() == 0) {
	            setErrorMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectNameEmpty);
	            return false;
	        }

	        IStatus nameStatus = workspace.validateName(projectFieldContents,
	                IResource.PROJECT);
	        if (!nameStatus.isOK()) {
	            setErrorMessage(nameStatus.getMessage());
	            return false;
	        }

	        boolean bad = true; // should we treat existing project as error

	        IProject handle = getProjectHandle();
	        if (handle.exists()) {
	        	if (getWizard() instanceof NewModelProjectWizard) {
	        		NewModelProjectWizard w = (NewModelProjectWizard)getWizard();
	        		if (w.lastProjectName != null && w.lastProjectName.equals(getProjectName()))
	        			bad = false;
	        	}
	        	if (bad) {
	        		setErrorMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectExistsMessage);
	        	    return false;
	        	}
	        }
	        
	        if (bad) { // skip this check if project already created 
	        	IPath p = getProjectLocation();
	        	if (p == null) p = ResourcesPlugin.getWorkspace().getRoot().getLocation();
	        	File f = p.append(getProjectName()).toFile();
	        	if (f.exists()) {
	        		if (f.isDirectory())
	        			setErrorMessage(Messages.getString("CMainWizardPage.6")); //$NON-NLS-1$
	        		else
	        			setErrorMessage(Messages.getString("CMainWizardPage.7")); //$NON-NLS-1$
	        		return false;
	        	}
	        }
	        
	        if (!locationArea.isDefault()) {
	            IStatus locationStatus = workspace.validateProjectLocationURI(handle,
	                    locationArea.getProjectLocationURI());
	            if (!locationStatus.isOK()) {
	                setErrorMessage(locationStatus.getMessage());
	                return false;
	            }
	        }

	        if (tree.getItemCount() == 0) {
	        	setErrorMessage(Messages.getString("CMainWizardPage.3")); //$NON-NLS-1$
	        	return false;
	        }
	        
	        // it is not an error, but we cannot continue
	        if (h_selected == null || h_selected.isDummy()) {
	            setErrorMessage(null);
		        return false;	        	
	        }

			if ( ! h_selected.canCreateWithoutToolchain()) {
				IToolChain tcs[] = h_selected.getSelectedToolChains(); 
				int cnt = tcs != null ? tcs.length : 0;
	        	if (cnt == 0) {
	        		setErrorMessage(Messages.getString("CMainWizardPage.4")); //$NON-NLS-1$
	        		return false;
	        	}
	        }
	        
            setErrorMessage(null);
	        return true;
	    }

	    /*
	     * see @DialogPage.setVisible(boolean)
	     */
	    public void setVisible(boolean visible) {
	        super.setVisible(visible);
	        if (visible) projectNameField.setFocus();
	    }

	    /**
	     * Returns the useDefaults.
	     * @return boolean
	     */
	    public boolean useDefaults() {
	        return locationArea.isDefault();
	    }

		public static ICWizardHandler updateData(Tree tree, Composite right, Button show_sup, IToolChainListListener ls) {
			// remember selected item
			TreeItem[] sel = tree.getSelection();
			String savedStr = (sel.length > 0) ? sel[0].getText() : null; 
			
			tree.removeAll();
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(EXTENSION_POINT_ID);
			if (extensionPoint == null) return null;
			IExtension[] extensions = extensionPoint.getExtensions();
			if (extensions == null) return null;
			for (int i = 0; i < extensions.length; ++i)	{
				IConfigurationElement[] elements = extensions[i].getConfigurationElements();
				for (int k = 0; k < elements.length; k++) {
					if (elements[k].getName().equals(ELEMENT_NAME)) {
						ICNewWizard w = null;
						try {
							w = (ICNewWizard) elements[k].createExecutableExtension(CLASS_NAME);
						} catch (CoreException e) {
							System.out.println(Messages.getString("CMainWizardPage.5") + e.getLocalizedMessage()); //$NON-NLS-1$
							return null; 
						}
						if (w == null) return null;
						
						w.setDependentControl(right, ls);
						w.createItems(tree, show_sup.getSelection());
					}
				}
			}
			if (tree.getItemCount() > 0) {
				TreeItem target = tree.getItem(0);
				// try to search item which was selected before
				if (savedStr != null) {
					TreeItem[] all = tree.getItems();
					for (int i=0; i<all.length; i++) {
						if (savedStr.equals(all[i].getText())) {
							target = all[i];
							break;
						}
					}
				}
				tree.setSelection(target);
				return (ICWizardHandler)target.getData();
			}
			return null;
		}

		/**
		 * @param h - new handler
		 */
		private void switchTo(ICWizardHandler h) {
			if (h == null) return;
			if (h_selected != null) h_selected.handleUnSelection();
			h_selected = h;
			right_label.setText(h_selected.getHeader());
			h_selected.handleSelection();
			next.setHandler(h_selected);
			h_selected.setSupportedOnly(show_sup.getSelection());
			setCustomPagesFilter();
		}
		
		private void setCustomPagesFilter() {
			// Set up Manager's filters
			if (h_selected.getProjectType() != null) {
				MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID, MBSCustomPageManager.PROJECT_TYPE, h_selected.getProjectType().getId());
			} else {
				MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID, MBSCustomPageManager.PROJECT_TYPE, null);
			}
			
			IToolChain[] tcs = h_selected.getSelectedToolChains();
			int n = (tcs == null) ? 0 : tcs.length;
			Set x = new TreeSet();			
			for (int i=0; i<n; i++) {
				x.add(tcs[i]); 
			}
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID, MBSCustomPageManager.TOOLCHAIN, x);
		}

		public void toolChainListChanged(int count) {
			if ( !h_selected.canCreateWithoutToolchain())
				setPageComplete(validatePage());
		}

		public boolean isCurrent() { return isCurrentPage(); }
}

