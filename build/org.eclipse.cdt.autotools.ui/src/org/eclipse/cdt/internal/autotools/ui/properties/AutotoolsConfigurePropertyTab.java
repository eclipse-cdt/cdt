/*******************************************************************************
 * Copyright (c) 2009 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.internal.autotools.core.configure.AutotoolsConfigurationManager;
import org.eclipse.cdt.internal.autotools.core.configure.IAConfiguration;
import org.eclipse.cdt.internal.autotools.core.configure.IConfigureOption;
import org.eclipse.cdt.internal.autotools.ui.AbstractAutotoolsCPropertyTab;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.ui.newui.AbstractPage;
import org.eclipse.cdt.ui.newui.PageLayout;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;

public class AutotoolsConfigurePropertyTab extends AbstractAutotoolsCPropertyTab implements IPreferencePageContainer {

	private TreeViewer fTree;
	private SashForm sashForm;
	private Composite settingsPageContainer;
	private AutotoolsConfigurePrefStore settingsStore;
	private AbstractConfigurePropertyOptionsPage currentSettingsPage;
	private ScrolledComposite containerSC;
	private ToolListContentProvider listprovider;
	private ToolListElement selectedElement;
	private ICConfigurationDescription icfgd;

	private Map<String, List<AbstractConfigurePropertyOptionsPage>> configToPageListMap;

	private IProject getProject() {
		return page.getProject();
	}
	
	public boolean canBeVisible() {
		if (page.isForProject() || page.isForPrefs()) {
			return true;
		}
		return false;
	}

	public IAConfiguration getAutotoolsCfg() {
		AutotoolsConfigurePropertyPage ap = (AutotoolsConfigurePropertyPage)page;
		// We call getConfigurationData() to get the name because if the configuration has been renamed,
		// it will cause the option value handler to clone the IAConfiguration
		return ap.getConfiguration(icfgd);
	}

	private void syncClones() {
		AutotoolsConfigurePropertyPage ap = (AutotoolsConfigurePropertyPage)page;
		// We call getConfigurationData() to get the name because if the configuration has been renamed,
		// it will cause the option value handler to clone the IAConfiguration
		ap.getAllConfigurationData();
	}
	
	public IPreferenceStore getPreferenceStore() {
		return settingsStore;
	}
	
	public void createControls(Composite parent) {
		AutotoolsConfigurationManager.getInstance().clearTmpConfigurations(getProject());
		syncClones();
		
		super.createControls(parent);
		Composite composite= usercomp;
		
		settingsStore = AutotoolsConfigurePrefStore.getInstance();
		configToPageListMap = new HashMap<String, List<AbstractConfigurePropertyOptionsPage>>();
		
		// assume parent page uses griddata
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL
				| GridData.FILL_VERTICAL);
		composite.setLayoutData(gd);
		GridLayout layout= new GridLayout();
//		layout.numColumns= 2;
		//PixelConverter pc= new PixelConverter(composite);
		//layout.verticalSpacing= pc.convertHeightInCharsToPixels(1) / 2;
		composite.setLayout(layout);
		
		// Create the sash form
		sashForm = new SashForm(usercomp, SWT.NONE);
		sashForm.setOrientation(SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		layout = new GridLayout(2, true);
		layout.marginHeight = 5;
		sashForm.setLayout(layout);
		createSelectionArea(sashForm);
		createEditArea(sashForm);

//		usercomp.addControlListener(new ControlAdapter() {
//			@Override
//			public void controlResized(ControlEvent e) {
//				specificResize();
//			}});
		
	}

//	private void specificResize() {
//		Point p1 = fTree.getTree().computeSize(-1, -1);
//		Point p2 = fTree.getTree().getSize();
//		Point p3 = usercomp.getSize();
//		p1.x += calcExtra();
//		if (p3.x >= p1.x && (p1.x < p2.x || (p2.x * 2 < p3.x))) {
//			fTree.getTree().setSize(p1.x , p2.y);
//			sashForm.setWeights(new int[] {p1.x, (p3.x - p1.x)});
//		} 
//	}
//
//	private int calcExtra() {
//		int x = fTree.getTree().getBorderWidth() * 2;
//		ScrollBar sb = fTree.getTree().getVerticalBar();
//		if (sb != null) x += sb.getSize().x;
//		return x;
//	}
	
	protected void createSelectionArea (Composite parent) {
		fTree = new TreeViewer(parent, SWT.SINGLE|SWT.H_SCROLL|SWT.V_SCROLL|SWT.BORDER);
		fTree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleOptionSelection();
			}});
		fTree.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		// Create a temporary default AutotoolsConfiguration to use for label info
		IAConfiguration tmp = AutotoolsConfigurationManager.getInstance().createDefaultConfiguration(getProject(), "");
		fTree.setLabelProvider(new ToolListLabelProvider(tmp));
	}

	protected void createEditArea(Composite parent) {
		containerSC = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		containerSC.setExpandHorizontal(true);
		containerSC.setExpandVertical(true);
		
		// Add a container for the build settings page
		settingsPageContainer = new Composite(containerSC, SWT.NONE);
		settingsPageContainer.setLayout(new PageLayout());

		containerSC.setContent(settingsPageContainer);
//		containerSC.setMinSize(settingsPageContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		settingsPageContainer.layout();
	}
	
	protected void setValues() {
		/*
		 *  This method updates the context of the build property pages
		 *   - Which configuration/resource configuration is selected
		 *   - Which tool/option category is selected
		 *    
		 *  It is called:
		 *   - When a property page becomes visible
		 *   - When the user changes the configuration selection
		 *   - When the user changes the "exclude" setting for a resource
		 */
		
		
		IConfiguration icfg = getCfg(icfgd.getConfiguration());
		if (icfg instanceof IMultiConfiguration) {
			fTree.setInput(null);
			fTree.getControl().setEnabled(false);
			currentSettingsPage.setVisible(false);
			return;
		}

		IAConfiguration currCfg = getAutotoolsCfg();
		
		//  Create the Tree Viewer content provider if first time
		if (listprovider == null) {
			listprovider = new ToolListContentProvider();
			fTree.setContentProvider(listprovider);
		}

		//  Update the selected configuration and the Tree Viewer
		ToolListElement[] newElements;
		
		fTree.setInput(currCfg);
		fTree.getControl().setEnabled(true);
		newElements = (ToolListElement[])listprovider.getElements(currCfg);
		fTree.expandAll();
		
		selectedElement = newElements[0];
		fTree.setSelection(new StructuredSelection(selectedElement), true);
		
//		//  Determine what the selection in the tree should be
//		//  If the saved selection is not null, try to match the saved selection
//		//  with an object in the new element list.
//		//  Otherwise, select the first tool in the tree
//		Object primaryObject = null;
//		if (selectedElement != null) {
//			selectedElement = matchSelectionElement(selectedElement, newElements);
//		}
//			
//		if (selectedElement == null) {
//			selectedElement = (newElements != null && newElements.length > 0 ? newElements[0] : null);
//		}
//			
//		if (selectedElement != null) {
//			primaryObject = selectedElement.getTool();
//			if (primaryObject == null) {
//				primaryObject = selectedElement.getOptionCategory();
//			}
//			if (primaryObject != null) {
//				if (primaryObject instanceof IOptionCategory) {
//					((ToolSettingsPrefStore)settingsStore).setSelection(getResDesc(), selectedElement, (IOptionCategory)primaryObject);
//				}
//				optionList.setSelection(new StructuredSelection(selectedElement), true);
//			}
//		}
//		specificResize();
	}

	private void handleOptionSelection() {
		// Get the selection from the tree list
		if (fTree == null) return;
		IStructuredSelection selection = (IStructuredSelection) fTree.getSelection();
		ToolListElement element = (ToolListElement)selection.getFirstElement();
		if (element != null) {
			displayPageForElement(element);
		}

		ScrollBar sb = containerSC.getHorizontalBar();
		if (sb != null && sb.isVisible()) {
			settingsPageContainer.pack(true);
			containerSC.setMinSize(settingsPageContainer.getSize());
			((AbstractPage)page).resize();
		}
	}

	private void displayPageForElement(ToolListElement element) {
		selectedElement = element;
		settingsStore.setSelection(getAutotoolsCfg(), selectedElement);
		
		AbstractConfigurePropertyOptionsPage oldPage = currentSettingsPage;
		currentSettingsPage = null;

		// Create a new settings page if necessary
		List<AbstractConfigurePropertyOptionsPage> pages = getPagesForConfig();
		ListIterator<AbstractConfigurePropertyOptionsPage> iter = pages.listIterator();
		
		while (iter.hasNext()) {
			AbstractConfigurePropertyOptionsPage page = iter.next();
			if (page.getName().equals(element.getName())) {
				currentSettingsPage = page;
				break;
			}
		}
		if (currentSettingsPage == null) {
			if (element.getType() == IConfigureOption.TOOL) {
				currentSettingsPage = new AutotoolsToolPropertyOptionPage(
					element, getAutotoolsCfg());
			}
			else {
				currentSettingsPage = new AutotoolsCategoryPropertyOptionPage(
						element, getAutotoolsCfg());
			}
			pages.add(currentSettingsPage);
			currentSettingsPage.setContainer(this);
			if (currentSettingsPage.getControl() == null) {
				currentSettingsPage.createControl(settingsPageContainer);
			}
		}
		
		// Make all the other pages invisible
		Control[] children = settingsPageContainer.getChildren();
		Control currentControl = currentSettingsPage.getControl();
		for (int i = 0; i < children.length; i++) {
			if (children[i] != currentControl)
				children[i].setVisible(false);
		}
		currentSettingsPage.setVisible(true);
		currentSettingsPage.updateFields();
		
		if (oldPage != null  && oldPage != currentSettingsPage)
			oldPage.setVisible(false);

		// Set the size of the scrolled area
		containerSC.setMinSize(currentSettingsPage.computeSize());
		settingsPageContainer.layout();
	
	}

	/* (non-Javadoc)
	 * Answers the list of settings pages for the selected configuration 
	 */
	private List<AbstractConfigurePropertyOptionsPage> getPagesForConfig() {
		if (getCfg() == null) return null;
		List<AbstractConfigurePropertyOptionsPage> pages = configToPageListMap.get(getCfg().getName());
		if (pages == null) {
			pages = new ArrayList<AbstractConfigurePropertyOptionsPage>();
			configToPageListMap.put(getCfg().getName(), pages);	
		}
		return pages;
	}
	
	/**
	 * Returns the "dirty" state
	 */
	public boolean isDirty() {
		// Check each settings page
		List<AbstractConfigurePropertyOptionsPage> pages = getPagesForConfig();
		// Make sure we have something to work on
		if (pages == null) {
			// Nothing to do
			return false;
		}
		ListIterator<AbstractConfigurePropertyOptionsPage> iter = pages.listIterator();
		while (iter.hasNext()) {
			AbstractConfigurePropertyOptionsPage page = iter.next();
			if (page == null) continue;
			if (page.isDirty()) return true;
		}
		return false;
	}
	
	protected void performOK() {
		ICConfigurationDescription[] cfgs = page.getCfgsEditable();
		AutotoolsConfigurePropertyPage ap = (AutotoolsConfigurePropertyPage)page;
		Map<String, IAConfiguration> cfgList = new HashMap<String, IAConfiguration>();
		for (int i = 0; i < cfgs.length; ++i) {
			ICConfigurationDescription cd = cfgs[i];
			IAConfiguration acfg = ap.getConfiguration(cd);
			cfgList.put(cd.getId(), acfg);
		}
		IProject project = getProject();
		AutotoolsConfigurationManager.getInstance().replaceProjectConfigurations(project, cfgList, cfgs);
		AutotoolsConfigurationManager.getInstance().clearTmpConfigurations(project);
	}
	
	protected void performCancel() {
		AutotoolsConfigurationManager.getInstance().clearTmpConfigurations(getProject());
	}
	
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		IProject project = getProject();
		ICConfigurationDescription[] cfgs = page.getCfgsEditable();
		// Apply all changes to existing saved configurations and new configurations, but do not perform
		// deletions.
		AutotoolsConfigurationManager.getInstance().applyConfigs(project.getName(), cfgs);
	}
	
	protected void performDefaults() {
		IAConfiguration cfg = getAutotoolsCfg();
		cfg.setDefaultOptions();
		setValues();
	}
	
	protected void updateData(ICResourceDescription rd) {
		if (rd == null) return;
		icfgd = rd.getConfiguration();
		setValues();
	}
	
	public void setVisible (boolean b) {
		super.setVisible(b);
	}

	// IPreferencePageContainer methods
	@Override
	public void updateButtons() {}
	public void updateMessage() {}
	public void updateTitle() {}
}
