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

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIImages;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.cdt.ui.wizards.CDTMainWizardPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class CDTConfigWizardPage extends WizardPage {

	public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.CConfigWizardPage"; //$NON-NLS-1$

	private static final Image IMG = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_CONFIG);
	private static final String TITLE = UIMessages.getString("CConfigWizardPage.0"); //$NON-NLS-1$
	private static final String MESSAGE = UIMessages.getString("CConfigWizardPage.1"); //$NON-NLS-1$
	private static final String COMMENT = UIMessages.getString("CConfigWizardPage.12"); //$NON-NLS-1$
	private static final String EMPTY_STR = "";  //$NON-NLS-1$
	
	private Table table;
	private CheckboxTableViewer tv;
	private Label l_projtype;
	private Label l_chains;
	private Composite parent;

	private String propertyId;
	private String errorMessage = null;
	private String message = MESSAGE;
	public boolean isVisible = false;
	private MBSWizardHandler handler;
	public boolean pagesLoaded = false;
	
	public CDTConfigWizardPage(MBSWizardHandler h) {
        super(UIMessages.getString("CDTConfigWizardPage.0")); //$NON-NLS-1$
        setPageComplete(false);
        handler = h;
        setWizard(h.getWizard());
    }
	
	private void addCustomPages() {
		if (pagesLoaded) return;
		pagesLoaded = true;
		
		if (! (getWizard() instanceof CDTCommonProjectWizard)) return; 
		CDTCommonProjectWizard wz = (CDTCommonProjectWizard)getWizard();
		
		IWizardPage p = getWizard().getStartingPage();  
		MBSCustomPageManager.init();
		MBSCustomPageManager.addStockPage(p, CDTMainWizardPage.PAGE_ID);
		MBSCustomPageManager.addStockPage(this, CDTConfigWizardPage.PAGE_ID);
		
		 setCustomPagesFilter(wz);
		// load all custom pages specified via extensions
		try	{
			MBSCustomPageManager.loadExtensions();
		} catch (BuildException e) { e.printStackTrace(); }
		
		IWizardPage[] customPages = MBSCustomPageManager.getCustomPages();
		if (customPages != null) {
			for (int k = 0; k < customPages.length; k++) {
				wz.addPage(customPages[k]);
			}
		}
	}
	
	
	public CfgHolder[] getCfgItems(boolean getDefault) {
		CfgHolder[] its;
		if (getDefault)  
			its = getDefaultCfgs(handler);
		else {
			ArrayList out = new ArrayList(table.getItemCount());
			TableItem[] tis = table.getItems();
			for (int i=0; i < tis.length; i++) {
				if (tis[i].getChecked())
					out.add(tis[i].getData()); 
			}
			its = (CfgHolder[])out.toArray(new CfgHolder[out.size()]);
		}
		return its;
	}

	public void createControl(Composite p) {
		parent = new Composite(p, SWT.NONE);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		parent.setLayout(new GridLayout(3, false));
		
		setupLabel(parent, UIMessages.getString("CConfigWizardPage.4"), 1, GridData.BEGINNING); //$NON-NLS-1$
		l_projtype = setupLabel(parent, EMPTY_STR, 2, GridData.FILL_HORIZONTAL);
		setupLabel(parent, UIMessages.getString("CConfigWizardPage.5"), 1, GridData.BEGINNING); //$NON-NLS-1$
		l_chains = setupLabel(parent, EMPTY_STR, 2, GridData.FILL_HORIZONTAL);
		setupLabel(parent, UIMessages.getString("CConfigWizardPage.6"), 3, GridData.BEGINNING); //$NON-NLS-1$
		
		table = new Table(parent, SWT.BORDER | SWT.CHECK);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		table.setLayoutData(gd);
		
		tv = new CheckboxTableViewer(table);
		tv.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return (Object[])inputElement;
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		tv.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return element == null ? EMPTY_STR : ((CfgHolder)element).getName();
			}
			public Image getImage(Object element) { return IMG; }
		});
		tv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				setPageComplete(isCustomPageComplete());
				update();
			}});
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		c.setLayout(new GridLayout(1, false));

		Button b1 = new Button(c, SWT.PUSH);
		b1.setText(UIMessages.getString("CConfigWizardPage.7")); //$NON-NLS-1$
		b1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) { 
				tv.setAllChecked(true);
				setPageComplete(isCustomPageComplete());
				update();
			}});

		Button b2 = new Button(c, SWT.PUSH);
		b2.setText(UIMessages.getString("CConfigWizardPage.8")); //$NON-NLS-1$
		b2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tv.setAllChecked(false);
				setPageComplete(isCustomPageComplete());
				update();
			}});

		// dummy placeholder
		new Label(c, 0).setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Button b3 = new Button(c, SWT.PUSH);
		b3.setText(UIMessages.getString("CConfigWizardPage.13")); //$NON-NLS-1$
		b3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b3.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				advancedDialog();
			}});
		
		Group gr = new Group(parent, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		gr.setLayoutData(gd);
		gr.setLayout(new FillLayout());
		Label lb = new Label(gr, SWT.NONE);
		lb.setText(COMMENT);
	}

	/**
	 * 
	 * @param handler
	 * @return
	 */
	static public CfgHolder[] getDefaultCfgs(MBSWizardHandler handler) {
		IToolChain[] tcs = handler.getSelectedToolChains();
		String id = handler.getPropertyId();
		IProjectType pt = handler.getProjectType(); 
		ArrayList out = new ArrayList(tcs.length * 2);
		for (int i=0; i < tcs.length; i++) {
			CfgHolder[] cfgs = null;
			if (id != null) 
				cfgs = CfgHolder.cfgs2items(ManagedBuildManager.getExtensionConfigurations(tcs[i], MBSWizardHandler.ARTIFACT, id));
			else if (pt != null) 
				cfgs = CfgHolder.cfgs2items(ManagedBuildManager.getExtensionConfigurations(tcs[i], pt));
			else { // Create default configuration for StdProject
				cfgs = new CfgHolder[1];
				cfgs[0] = new CfgHolder(tcs[i], null);
			}
			if (cfgs == null) return null;
			
			for (int j=0; j<cfgs.length; j++) {
				if (cfgs[j].isSystem() || (handler.supportedOnly() && !cfgs[j].isSupported())) continue;
				out.add(cfgs[j]);
			}
		}
		return (CfgHolder[])out.toArray(new CfgHolder[out.size()]);
	}
	
    /**
     * Returns whether this page's controls currently all contain valid 
     * values.
     *
     * @return <code>true</code> if all controls are valid, and
     *   <code>false</code> if at least one is invalid
     */
    public boolean isCustomPageComplete() {
    	if (!isVisible) return true;
    	
		if (table.getItemCount() == 0) {
			errorMessage = UIMessages.getString("CConfigWizardPage.10"); //$NON-NLS-1$
			message = errorMessage; 
			return false;
		}
		if (tv.getCheckedElements().length == 0) {
			errorMessage = UIMessages.getString("CConfigWizardPage.11"); //$NON-NLS-1$
			message = errorMessage; 
			return false;
		}
		errorMessage = null;
		message = MESSAGE;
        return true;
    }

    /**
     * 
     */
    public void setVisible(boolean visible) {
    	isVisible = visible;
		if (visible && handler != null) {
			tv.setInput(CfgHolder.unique(getDefaultCfgs(handler)));
			tv.setAllChecked(true);
			String s = EMPTY_STR;
			IToolChain[] tc = handler.getSelectedToolChains();
			for (int i=0; i < tc.length; i++) {
				s = s + ((tc[i] == null) ? 
						"" : //$NON-NLS-1$
						tc[i].getName());  
				if (i < tc.length - 1) s = s + ", ";  //$NON-NLS-1$
			}
			l_chains.setText(s);
			l_projtype.setText(handler.getName());			
			setPageComplete(isCustomPageComplete());
		}
		parent.setVisible(visible);
		if (visible) update();
    }

    	//------------------------
    private Label setupLabel(Composite c, String name, int span, int mode) {
		Label l = new Label(c, SWT.NONE);
		l.setText(name);
		GridData gd = new GridData(mode);
		gd.horizontalSpan = span;
		l.setLayoutData(gd);
		Composite p = l.getParent();
		l.setFont(p.getFont());
		return l;
	}

	public String getName() { return TITLE; }
	public void dispose() {}
	public Control getControl() { return parent; }
	public String getDescription() { return null; }
	public String getErrorMessage() { return errorMessage; }
//	public Image getImage() { return wizard.getDefaultPageImage(); }
	public String getMessage() { return message; }
	public String getTitle()   { return TITLE; }
	public void performHelp() {}
	public void setDescription(String description) {}
	public void setImageDescriptor(ImageDescriptor image) {}
	public void setTitle(String _title) {}

	protected void update() {
		getWizard().getContainer().updateButtons();
		getWizard().getContainer().updateMessage();
		getWizard().getContainer().updateTitleBar();
	}
	
	/**
	 * Edit properties
	 */
	private void advancedDialog() {
		if (getWizard() instanceof CDTCommonProjectWizard) {
			CDTCommonProjectWizard nmWizard = (CDTCommonProjectWizard)getWizard();
			IProject newProject = nmWizard.getProject(true);
			if (newProject != null) {
				boolean oldManage = CDTPrefUtil.getBool(CDTPrefUtil.KEY_NOMNG);
				// disable manage configurations button
				CDTPrefUtil.setBool(CDTPrefUtil.KEY_NOMNG, true);
				try {
					PreferencesUtil.createPropertyDialogOn(getWizard().getContainer().getShell(), newProject, propertyId, null, null).open();
				} finally {
					CDTPrefUtil.setBool(CDTPrefUtil.KEY_NOMNG, oldManage);
				}
			}
		}
	}
	
	public IWizardPage getNextPage() {
		addCustomPages();
		return MBSCustomPageManager.getNextPage(PAGE_ID);
	}
	
	private void setCustomPagesFilter(CDTCommonProjectWizard wz) {
		String[] natures = wz.getNatures();
		if (natures == null || natures.length == 0)
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID, MBSCustomPageManager.NATURE, null);
		else if (natures.length == 1)
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID, MBSCustomPageManager.NATURE, natures[0]);
		else {
			Set x = new TreeSet();
			for (int i=0; i<natures.length; i++) x.add(natures[i]);
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID, MBSCustomPageManager.NATURE, x);
		}
		if (handler.getProjectType() != null) {
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID, MBSCustomPageManager.PROJECT_TYPE, handler.getProjectType().getId());
		} else {
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID, MBSCustomPageManager.PROJECT_TYPE, null);
		}
		IToolChain[] tcs = handler.getSelectedToolChains();
		int n = (tcs == null) ? 0 : tcs.length;
		Set x = new TreeSet();			
		for (int i=0; i<n; i++) {
			x.add(tcs[i]); 
		}
		MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID, MBSCustomPageManager.TOOLCHAIN, x);
	}

}
