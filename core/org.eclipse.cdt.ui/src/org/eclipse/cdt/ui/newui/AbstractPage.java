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
package org.eclipse.cdt.ui.newui;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.WorkbenchPlugin;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.util.ProblemMarkerManager;

/**
 * It is a parent for all standard CDT property pages
 * in new CDT model. 
 * 
 * Although it is enougth for new page to implement
 * "IWorkbenchPropertyPage" interface, it would be
 * better to extend it from "AbstractPage".
 * 
 * In this case, we'll able to use:
 * - dynamic tabs support via cPropertyTab extension point
 * - a lot of utility methods: see ICPropertyProvider interface
 * - mechanism of messages sent to all pages and all tabs in them
 * 
 * In fact, descendants of AbstractPage have to implement
 * the only method:
 * 		protected boolean isSingle(); 
 * It it returns false, current page can contain multiple tabs
 * (obtained through "cPropertyTab" extension point).
 * If it returns true, only one content tab is possible. If
 * more than 1 tabs refer to this pas as a parent, only 1st
 * one would be taken into account, others will be ignored. 
 */
public abstract class AbstractPage extends PropertyPage 
implements
		IWorkbenchPropertyPage, // ext point 
		IPreferencePageContainer, // dynamic pages
		ICPropertyProvider // utility methods for tabs
{
	// Toggle this constant to <false> 
	// to hide "Manage configurations" button.
	// Corresponding menu items and toolbar button 
	// will not be affected by this change.
	private static final boolean ENABLE_MANAGE = true;
	
	private static ArrayList pages = new ArrayList(5);
	private static ICResourceDescription resd = null;
	private static ICConfigurationDescription[] cfgDescs = null;
	private static ICProjectDescription prjd = null;
	private static int cfgIndex = 0;
	private static boolean doneOK = false;
	/*
	 * String constants
	 */
	private static final String PREFIX = "CLanguagesPropertyPage"; //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String TIP = PREFIX + ".tip"; //$NON-NLS-1$
	private static final String CONFIG_LABEL = LABEL + ".Configuration"; //$NON-NLS-1$
	private static final String ALL_CONFS = PREFIX	+ ".selection.configuration.all"; //$NON-NLS-1$
	private static final String CONF_TIP = TIP + ".config"; //$NON-NLS-1$

	// tabs
	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.cPropertyTab"; //$NON-NLS-1$
	public static final String ELEMENT_NAME = "tab"; //$NON-NLS-1$
	public static final String CLASS_NAME = "class"; //$NON-NLS-1$
	public static final String PARENT_NAME = "parent"; //$NON-NLS-1$
	public static final String IMAGE_NAME = "icon"; //$NON-NLS-1$
	public static final String TIP_NAME = "tooltip"; //$NON-NLS-1$
	public static final String TEXT_NAME = "name"; //$NON-NLS-1$
	public static final String WEIGHT_NAME = "weight"; //$NON-NLS-1$

	private static final Object NOT_NULL = new Object();
	public static final String EMPTY_STR = "";  //$NON-NLS-1$

	/*
	 * Dialog widgets
	 */
	private Combo configSelector;
	private Button manageButton;
	private Button excludeFromBuildCheck;
	/*
	 * Bookeeping variables
	 */
	private boolean noContentOnPage = false;
	protected boolean displayedConfig = false;
	protected boolean isDirty = false;

	protected IResource internalElement = null;
	protected boolean isProject = false;
	protected boolean isFolder  = false;
	protected boolean isFile    = false;
	
	// tabs
	TabFolder folder;
	ArrayList itabs = new ArrayList();
	ICPropertyTab currentTab;

	class InternalTab {
		Composite comp;
		String text;
		String tip;
		Image image;
		ICPropertyTab tab;
		
		InternalTab(Composite _comp, String _text, Image _image, ICPropertyTab _tab, String _tip) {
			comp  = _comp;
			text  = _text;
			image = _image;
			tab   = _tab;
			tip   = _tip;
		}

		public TabItem createOn(TabFolder f) {
			if (tab.canBeVisible()) {
				TabItem ti = new TabItem(f, SWT.NONE);
				ti.setText(text);
				if (tip != null) ti.setToolTipText(tip);
				if (image != null) ti.setImage(image);
				ti.setControl(comp);
				ti.setData(tab);
				return ti;
			}
			return null;
		}
	}
	
	
	/**
	 * Default constructor
	 */
	public AbstractPage() {
		// reset static values before new session 
		if (pages.size() == 0) {
			doneOK = false; // see "performOk()"
			prjd = null;    // force getting new descriptors
		}
		// register current page 
		if (!pages.contains(this)) pages.add(this);
	}
	
	protected Control createContents(Composite parent) {
		//	Create the container we return to the property page editor
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.numColumns = 1;
		compositeLayout.marginHeight = 0;
		compositeLayout.marginWidth = 0;
		composite.setLayout( compositeLayout );

		String s = null;
		if (!checkElement()) {
			s = NewUIMessages.getResourceString("AbstractPage.0"); //$NON-NLS-1$
		} else if (!isApplicable()) {
			s = NewUIMessages.getResourceString("AbstractPage.1"); //$NON-NLS-1$
		} else if (!isCDTProject(getProject())) {
			s = NewUIMessages.getResourceString("AbstractPage.2"); //$NON-NLS-1$
		}
		
	    if (s == null) {
	    	contentForCDT(composite);
	    	return composite;
	    }
		
		// no contents
		Label label = new Label(composite, SWT.LEFT);
		label.setText(s);
		label.setFont(composite.getFont());
		noContentOnPage = true;
		noDefaultAndApplyButton();
		return composite;
	}
	
	protected void contentForCDT(Composite composite) {
		GridData gd;

		// Add a config selection area
		Group configGroup = ControlFactory.createGroup(composite, EMPTY_STR, 1);
//		Composite configGroup = new Composite(composite, SWT.NONE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.grabExcessHorizontalSpace = true;
		configGroup.setLayoutData(gd);
		// Use the form layout inside the group composite
		GridLayout ff = new GridLayout(3, false);
		configGroup.setLayout(ff);
		Label configLabel = ControlFactory.createLabel(configGroup, NewUIMessages.getResourceString(CONFIG_LABEL));
		configLabel.setLayoutData(new GridData(GridData.BEGINNING));
		
		configSelector = new Combo(configGroup, SWT.READ_ONLY | SWT.DROP_DOWN);
		configSelector.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				handleConfigSelection();
			}
		});
		configSelector.setToolTipText(NewUIMessages.getResourceString(CONF_TIP));
		gd = new GridData(GridData.FILL);
		gd.grabExcessHorizontalSpace = true;
	    gd.grabExcessVerticalSpace = true;
	    gd.horizontalAlignment = GridData.FILL;
	    gd.verticalAlignment = GridData.FILL;
	  	
		configSelector.setLayoutData(gd);
		
		if (ENABLE_MANAGE) {
			manageButton = new Button(configGroup, SWT.PUSH);
			manageButton.setText("Manage configurations"); //$NON-NLS-1$
			gd = new GridData(GridData.END);
			gd.widthHint = 150;
			manageButton.setLayoutData(gd);
			manageButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					performOk();
					if (ManageConfigDialog.manage(getProject())) {
						prjd = null;
						cfgDescs = null;
						populateConfigurations();					
					}
				}
			});
		} else { // dummy object to avoid breaking layout
			new Label(configGroup, SWT.NONE).setLayoutData(new GridData(GridData.END));
		}
		
		if (isForFolder() || isForFile()) {
			excludeFromBuildCheck = new Button(configGroup, SWT.CHECK);
			excludeFromBuildCheck.setText("Exclude resource from build"); //$NON-NLS-1$
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			excludeFromBuildCheck.setLayoutData(gd);
			excludeFromBuildCheck.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					getResDesc().setExcluded(excludeFromBuildCheck.getSelection());
				}
			});
		}
		
		//	Update the contents of the configuration widget
		populateConfigurations();
		
		if (excludeFromBuildCheck != null) {
			excludeFromBuildCheck.setSelection(getResDesc().isExcluded());
		}
		//	Create the Specific objects for each page
		createWidgets(composite);
	}
	
	public void createWidgets(Composite c) {
		Composite comp = new Composite(c, SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		itabs.clear(); 
		if (!isSingle()) {
			comp.setLayout(new FillLayout());
			folder = new TabFolder(comp, SWT.NONE);
		}
		loadExtensionsSynchronized(comp);
		
		// Set listener after data load, to avoid firing
		// selection event on not-initialized tab items 
		if (folder != null) {
		    folder.addSelectionListener(new SelectionAdapter() {
			      public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
			    	  if (folder.getSelection().length > 0 ) {
			    		  ICPropertyTab newTab = (ICPropertyTab)folder.getSelection()[0].getData();
			    		  if (newTab != null && currentTab != newTab) {
				    		  if (currentTab != null) currentTab.handleTabEvent(ICPropertyTab.VISIBLE, null);			    			  
				    		  currentTab = newTab;
				    		  currentTab.handleTabEvent(ICPropertyTab.VISIBLE, NOT_NULL);
			    		  }
			    	  }
			      }
			    });
		    if (folder.getItemCount() > 0) folder.setSelection(0);
		}
	}
	/**
	 * 
	 */
	public IProject getProject() {
		Object element = getElement();
		if (element != null) { 
			if (element instanceof IFile ||
				element instanceof IProject ||
				element instanceof IFolder)
				{
			IResource f = (IResource) element;
			return f.getProject();
				}
			else if (element instanceof ICProject)
				return ((ICProject)element).getProject();
		}
		return null;
	}

	/*
	 * Event Handlers
	 */
	private void handleConfigSelection() {
		// If there is nothing in config selection widget just bail
		if (configSelector.getItemCount() == 0) return;
		int selectionIndex = configSelector.getSelectionIndex();
		if (selectionIndex == -1) return;
		// Check if the user has selected the "all" configuration
		String configName = configSelector.getItem(selectionIndex);
		if (configName.equals(NewUIMessages.getResourceString(ALL_CONFS))) {
			// This is the all config
			return;
		} else {
			ICConfigurationDescription newConfig = cfgDescs[selectionIndex];
			if (newConfig != getResDesc()) {
				// If the user has changed values, and is now switching configurations, prompt for saving
			    if (getResDesc() != null) {
			        if (isDirty) {
						Shell shell = CUIPlugin.getActiveWorkbenchShell();
						boolean shouldApply = MessageDialog.openQuestion(shell,
						        NewUIMessages.getResourceString("BuildPropertyPage.changes.save.title"), //$NON-NLS-1$
						        NewUIMessages.getFormattedString("BuildPropertyPage.changes.save.question",  //$NON-NLS-1$
						                new String[] {getResDesc().getName(), newConfig.getName()}));
						if (shouldApply) {
						    if (performOk()) {
						    	isDirty = false;    
			        		} else {
						        MessageDialog.openWarning(shell,
								        NewUIMessages.getResourceString("BuildPropertyPage.changes.save.title"), //$NON-NLS-1$
								        NewUIMessages.getResourceString("BuildPropertyPage.changes.save.error")); //$NON-NLS-1$ 
						    }
						}
			        }
			    }
			    // Set the new selected configuration
			    cfgIndex = selectionIndex;
				cfgChanged(newConfig);
			}
		}
		return;
	}
	
	/**
	 * Saves ALL current changes in ALL affected configurations.
	 * Called after "OK" button pressed.
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if (doneOK || noContentOnPage || !displayedConfig) return true;
		forEach(ICPropertyTab.OK, null);
		doneOK = true; // further pages need not to do anything
		try {
			CoreModel.getDefault().setProjectDescription(getProject(), prjd);
		} catch (CoreException e) { }
		updateViews(internalElement);
		return true;
	}
	
    /**
     * Apply changes for all tabs but for given page & current cfg only.
	 * Called after "Apply" button pressed.
     */
    public void performApply() {
		if (noContentOnPage || !displayedConfig) return;
		// perform in separate thread
		final ICProjectDescription local_prjd = CoreModel.getDefault().getProjectDescription(prjd.getProject());
		ICConfigurationDescription c = local_prjd.getConfigurationById(resd.getConfiguration().getId());
		final ICResourceDescription local_cfgd = getResDesc(c); 
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				// ask all tabs to store changes in cfg
				forEach(ICPropertyTab.APPLY, local_cfgd);
				try {
					CoreModel.getDefault().setProjectDescription(getProject(), local_prjd);
				} catch (CoreException e) {
					System.out.println("setProjectDescription: " + e.getLocalizedMessage()); //$NON-NLS-1$
				}
				updateViews(internalElement);
			}
		};
		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);
		try {
			new ProgressMonitorDialog(getShell()).run(false, true, op);
		} catch (InvocationTargetException e) {
			Throwable e1 = e.getTargetException();
			CUIPlugin.errorDialog(getShell(), "Cannot apply", "Internal error", e1, true); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (InterruptedException e) {}
    }

    /**
     * Inform all pages. Nothing to save
     */
    public boolean performCancel() {
		if (! noContentOnPage && displayedConfig)
			forEach(ICPropertyTab.CANCEL);
        return true;
    }

    /**
     * Ask all pages to set default values to current cfg   
     */
	public void performDefaults() {
		if (! noContentOnPage && displayedConfig)
			forEach(ICPropertyTab.DEFAULTS);
	}
	
	private void populateConfigurations() {
		// Do nothing if widget not created yet.
		if (configSelector == null)	return;

		// if project changed, force re-read cfg.
		ICProjectDescription _prjDesc = CoreModel.getDefault().getProjectDescription(getProject());
		if (prjd == null || prjd.getProject() != _prjDesc.getProject()) {
			prjd = _prjDesc;
			cfgDescs = null;
			cfgIndex = 0;
		}
		// Do not re-read if list already created by another page
		if (cfgDescs == null) {
			cfgDescs = prjd.getConfigurations();
			if (cfgDescs == null || cfgDescs.length == 0) return;
		}

		// Clear and replace the contents of the selector widget
		configSelector.removeAll();
		for (int i = 0; i < cfgDescs.length; ++i) {
			configSelector.add(cfgDescs[i].getName());
			if (cfgDescs[i].isActive()) cfgIndex = i;
		}
		configSelector.select(cfgIndex);
		handleConfigSelection();
	}

	public void updateButtons() {}
	public void updateMessage() { }
	public void updateTitle() {	}
	public void updateContainer() {	}

	public boolean isValid() {
		updateContainer();
		return super.isValid();
	}
	
	public void setVisible(boolean visible) {
		/*
		if (visible) {
			Rectangle r = getControl().getBounds();
			if (r.width > MAX_WIDTH) {
				r.width = MAX_WIDTH;
				getControl().setBounds(r);
			}
		}
		*/
		super.setVisible(visible);
		if (visible) displayedConfig = true;

		if (itabs.size() < 1) return;
		
		if (currentTab == null && folder.getItemCount() > 0)	{
			Object ob = folder.getItem(0).getData();
			currentTab = (ICPropertyTab)ob;
		}
		if (currentTab != null)	
			currentTab.handleTabEvent(ICPropertyTab.VISIBLE, visible ? NOT_NULL : null);
	}
	
	public IPreferenceStore getPreferenceStore() {
		return CUIPlugin.getDefault().getPreferenceStore();
	}

	public Preferences getPreferences()	{
		return CUIPlugin.getDefault().getPluginPreferences();
	}
	
	public void enableConfigSelection (boolean enable) {
		if (configSelector != null) configSelector.setEnabled(enable);
		if (manageButton != null) manageButton.setEnabled(enable);
	}
	
	/**
	 * Returns configuration descriptions for given project
	 */
	public ICConfigurationDescription[] getCfgsReadOnly(IProject p) {
		ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(p, false); 
		if (prjd != null) 
			return prjd.getConfigurations();
		return null;
	}

	/**
	 * Returns loaded configuration descriptions for current project
	 */
	public ICConfigurationDescription[] getCfgsEditable() {
		return cfgDescs;
	}
	
	/** Checks whether project is new CDT project
	 * 
	 * @param p - project to check
	 * @returns true if it's new-style project. 
	 */ 
	public static boolean isCDTPrj(IProject p) {
		ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(p, false); 
		if (prjd == null) return false; 
		ICConfigurationDescription[] cfgs = prjd.getConfigurations();
		return (cfgs != null && cfgs.length > 0);
	}
	
	public boolean isCDTProject(IProject p) {
		return isCDTPrj(p);
	}
	
	public ICResourceDescription getResDesc() {
		if (resd == null) {
			if (cfgDescs == null) {
				populateConfigurations();
				if (cfgDescs == null || cfgDescs.length == 0) return null;
			}
			resd = getResDesc(cfgDescs[cfgIndex]);			
		}
		return resd;
	}

	public ICResourceDescription getResDesc(ICConfigurationDescription cf) {
		IAdaptable ad = getElement();
		
		if (isForProject()) 
			return cf.getRootFolderDescription();
		ICResourceDescription out = null;
		IResource res = (IResource)ad; 
		IPath p = res.getProjectRelativePath();
		if (isForFolder() || isForFile()) {
			out = cf.getResourceDescription(p, false);
			if (! p.equals(out.getPath()) ) {
				try {
					if (isForFolder())
						out = cf.createFolderDescription(p, (ICFolderDescription)out);
					else
						out = cf.createFileDescription(p, out);
				} catch (CoreException e) {
					System.out.println("Cannot create resource configuration for " + //$NON-NLS-1$
							p.toOSString() + "\n" + e.getLocalizedMessage()); //$NON-NLS-1$
				}
			}
		}
		return out;
	}
	
	private void cfgChanged(ICConfigurationDescription _cfgd) {
		resd = getResDesc(_cfgd);
		
		if (excludeFromBuildCheck != null)
			excludeFromBuildCheck.setSelection(resd.isExcluded());

		for (int i=0; i<pages.size(); i++) {
			AbstractPage ap = (AbstractPage)pages.get(i);
			if (ap.displayedConfig)
				ap.forEach(ICPropertyTab.UPDATE,getResDesc());
		}
	}
	
	public void dispose() {
		if (displayedConfig) forEach(ICPropertyTab.DISPOSE); 
		if (pages.contains(this)) pages.remove(this);
	} 
	
	/**
	 * The only method to be redefined in descendants
	 * @return 
	 * 		true if single page is required
	 * 		false if multiple pages are possible
	 */
	abstract protected boolean isSingle(); 
	
	/**
	 * Apply specified method to all tabs
	 */
	protected void forEach(int m) { forEach(m, null); }
	protected void forEach(int m, Object pars) {
		Iterator it = itabs.iterator();
		while(it.hasNext()) {
			InternalTab tab = (InternalTab)it.next();
			if (tab != null) tab.tab.handleTabEvent(m, pars);
		}
	}
	
	// redefine page width
	/*
	public Point computeSize() {
		Point p = super.computeSize();
		if (p.x > MAX_WIDTH) p.x = MAX_WIDTH;
		return p;
	}
	*/
	
	public static String getWeight(IConfigurationElement e) {
		String s = e.getAttribute(WEIGHT_NAME);
		return (s == null) ? EMPTY_STR : s;
	}
	
	private synchronized void loadExtensionsSynchronized(Composite parent)
	{
		// Get the extensions
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(EXTENSION_POINT_ID);
		if (extensionPoint == null) return;
		IExtension[] extensions = extensionPoint.getExtensions();
		if (extensions == null) return;
		
		for (int i = 0; i < extensions.length; ++i)	{
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			
			Arrays.sort(elements, CDTListComparator.getInstance());
			
			for (int k = 0; k < elements.length; k++) {
				if (elements[k].getName().equals(ELEMENT_NAME)) {
					if (loadTab(elements[k], parent)) return;
				} else {
					System.out.println("Cannot load " + elements[k].getName()); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * 
	 * @param element
	 * @param parent
	 * @return true if we should exit (no more loadings)
	 *         false if we should continue extensions scan.  
	 * @throws BuildException
	 */
	private boolean loadTab(IConfigurationElement element, Composite parent) {
		//	MBSCustomPageData currentPageData;
		// Check whether it's our tab
		if (!this.getClass().getName().equals(element.getAttribute(PARENT_NAME))) return false;
		
		ICPropertyTab page = null;
		try {
			page = (ICPropertyTab) element.createExecutableExtension(CLASS_NAME);
		} catch (CoreException e) {
			System.out.println("Cannot create page: " + e.getLocalizedMessage()); //$NON-NLS-1$
			return false; 
		}
		if (page == null) return false;
		
		Image _img = getIcon(element);
		if (_img != null) page.handleTabEvent(ICPropertyTab.SET_ICON, _img);
		
		if (isSingle()) {
			// note that name, image and tooltip
			// are ignored for single page.
			page.createControls(parent, this);
			InternalTab itab = new InternalTab(parent, EMPTY_STR, null, page, null);
			itabs.add(itab);
			currentTab = page;
			return true; // don't load other tabs
		} else {  // tabbed page
			String _name   = element.getAttribute(TEXT_NAME);
			String _tip = element.getAttribute(TIP_NAME);

			Composite _comp = new Composite(folder, SWT.NONE);
			page.createControls(_comp, this);	    
			InternalTab itab = new InternalTab(_comp, _name, _img, page, _tip);
			itab.createOn(folder);
			itabs.add(itab);
			return false;
		}
	}
	
	private Image getIcon(IConfigurationElement config) {
		ImageDescriptor idesc = null;
		try {
			String iconName = config.getAttribute(IMAGE_NAME);
			if (iconName != null) {
				URL pluginInstallUrl = Platform.getBundle(config.getDeclaringExtension().getContributor().getName()).getEntry("/"); //$NON-NLS-1$			
				idesc = ImageDescriptor.createFromURL(new URL(pluginInstallUrl, iconName));
			}
		} catch (MalformedURLException exception) {}
		return (idesc == null) ? null : idesc.createImage();
	}
	
	public void informAll(int code, Object data) {
		for (int i=0; i<pages.size(); i++) {
			AbstractPage ap = (AbstractPage)pages.get(i);
			ap.forEach(code, data);
		}
	}

	public void informPages(int code, Object data) {
		for (int i=0; i<pages.size(); i++) {
			AbstractPage ap = (AbstractPage)pages.get(i);
			ap.handleMessage(code, data);
		}
	}

	public void handleMessage(int code, Object data) {
		switch (code) {
			// First re-check visibility of all tabs.
		    // While tab deletion can be made on the fly,
		    // tabs adding will be made by re-creation
		    // of all elements, to preserve their order 
			case ICPropertyTab.MANAGEDBUILDSTATE:
				// generally, single-tabbed pages are not intended to handle this message
				if (folder == null) return;
				
				boolean willAdd = false;
				TabItem[] ts = folder.getItems();
				int x = folder.getSelectionIndex();
				String currHeader = (x == -1) ? null : ts[x].getText();
				for (int i=0; i<itabs.size(); i++) {
					InternalTab itab = (InternalTab)itabs.get(i);
					TabItem ti = null;
					for (int j=0; j<ts.length; j++) {
						if (ts[j].isDisposed()) continue;
						if (ts[j].getData() == itab.tab) {
							ti = ts[j];
							break;
						}
					}
					if (itab.tab.canBeVisible()) {
						if (ti == null)	{
							willAdd = true;
							break;
						}
					} else {
						if (ti != null) ti.dispose();
					}
				}
				// in case of new tab added, 
				// we have to dispose and re-create all tabs
				if (willAdd) {
					for (int j=0; j<ts.length; j++) 
						if (ts[j] != null && !ts[j].isDisposed())
							ts[j].dispose();
					TabItem ti = null;
					for (int i=0; i<itabs.size(); i++) {
						InternalTab itab = (InternalTab)itabs.get(i);
						if (itab.tab.canBeVisible()) {
							TabItem currTI = itab.createOn(folder);
							if (currHeader != null && currHeader.equals(itab.text))
								ti = currTI;
						}
					}
					if (ti != null) folder.setSelection(ti);
				}
				break;
		}
	}
	
	/**
	 * Performs conversion of incoming element to internal representation.
	 */
	protected boolean checkElement() {
		IAdaptable el = super.getElement();
		if (el instanceof ICElement) 
			internalElement = ((ICElement)el).getResource();
		else if (el instanceof IResource) 
			internalElement = (IResource)el;
		if (internalElement == null) return false;
		isProject = internalElement instanceof IProject;
		isFolder  = internalElement instanceof IFolder;
		isFile    = internalElement instanceof IFile;
		return true;
	}
	
	// override parent's method to use proper class
	public IAdaptable getElement() {
		if (internalElement == null && !checkElement()) 
			throw (new NullPointerException("element not initialized !")); //$NON-NLS-1$
		return internalElement; 
	}
	
	public boolean isForProject()  { return isProject; }
	public boolean isForFolder()   { return isFolder; }
	public boolean isForFile()     { return isFile; }
	public boolean isForPrefs()    { return false; }
	
	/**
	 * Checks whether CDT property pages can be open for given object.
	 * In particular, header files and text files are not allowed.
	 * 
	 * Note, that org.eclipse.cdt.ui.plugin.xml contains appropriate
	 * filters to avoid displaying CDT pages for unwanted objects. 
	 * So this check is only backup, it would prevent from NullPointer
	 * exceptions in case when xml filters were modified somehow.  
	 * 
	 * @return - true if element is applicable to CDT pages.
	 */
	public boolean isApplicable() {
		if (internalElement == null && !checkElement())
			return false; // unknown element
		if (isForFile()) // only source files are applicable
			return CoreModel.isValidSourceUnitName(getProject(), internalElement.getName());
		else
			return true; // Projects and folders are always applicable
	}

	// update views (in particular, display resource configurations)
	public static void updateViews(IResource res) {
		WorkbenchPlugin.getDefault().getDecoratorManager().updateForEnablementChange();
	}	
}
