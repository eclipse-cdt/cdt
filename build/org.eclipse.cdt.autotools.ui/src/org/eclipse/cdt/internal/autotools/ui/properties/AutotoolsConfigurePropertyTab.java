/*******************************************************************************
 * Copyright (c) 2009, 2015 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.jface.viewers.IStructuredSelection;
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

	@Override
	public boolean canBeVisible() {
		if (page.isForProject() || page.isForPrefs()) {
			return true;
		}
		return false;
	}

	private IAConfiguration getAutotoolsCfg() {
		AutotoolsConfigurePropertyPage ap = (AutotoolsConfigurePropertyPage) page;
		// We call getConfigurationData() to get the name because if the configuration has been renamed,
		// it will cause the option value handler to clone the IAConfiguration
		return ap.getConfiguration(icfgd);
	}

	private void syncClones() {
		AutotoolsConfigurePropertyPage ap = (AutotoolsConfigurePropertyPage) page;
		// We call getConfigurationData() to get the name because if the configuration has been renamed,
		// it will cause the option value handler to clone the IAConfiguration
		ap.getAllConfigurationData();
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		return settingsStore;
	}

	@Override
	public void createControls(Composite parent) {
		AutotoolsConfigurationManager.getInstance().clearTmpConfigurations(page.getProject());
		syncClones();

		super.createControls(parent);
		Composite composite = usercomp;

		settingsStore = AutotoolsConfigurePrefStore.getInstance();
		configToPageListMap = new HashMap<>();

		// assume parent page uses griddata
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL
				| GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		composite.setLayoutData(gd);
		GridLayout layout = new GridLayout();
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
	}

	private void createSelectionArea(Composite parent) {
		fTree = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		fTree.addSelectionChangedListener(event -> handleOptionSelection());
		fTree.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		// Create a temporary default AutotoolsConfiguration to use for label info
		IAConfiguration tmp = AutotoolsConfigurationManager.getInstance().createDefaultConfiguration("");
		fTree.setLabelProvider(new ToolListLabelProvider(tmp));
	}

	private void createEditArea(Composite parent) {
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

	private void setValues() {
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
		newElements = (ToolListElement[]) listprovider.getElements(currCfg);
		fTree.expandAll();

		selectedElement = newElements[0];
		fTree.setSelection(new StructuredSelection(selectedElement), true);

	}

	private void handleOptionSelection() {
		// Get the selection from the tree list
		if (fTree == null)
			return;
		IStructuredSelection selection = (IStructuredSelection) fTree.getSelection();
		ToolListElement element = (ToolListElement) selection.getFirstElement();
		if (element != null) {
			displayPageForElement(element);
		}

		ScrollBar sb = containerSC.getHorizontalBar();
		if (sb != null && sb.isVisible()) {
			settingsPageContainer.pack(true);
			containerSC.setMinSize(settingsPageContainer.getSize());
			((AbstractPage) page).resize();
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
				currentSettingsPage = new AutotoolsToolPropertyOptionPage(element, getAutotoolsCfg());
			} else {
				currentSettingsPage = new AutotoolsCategoryPropertyOptionPage(element, getAutotoolsCfg());
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

		if (oldPage != null && oldPage != currentSettingsPage)
			oldPage.setVisible(false);

		// Set the size of the scrolled area
		containerSC.setMinSize(currentSettingsPage.computeSize());
		settingsPageContainer.layout();

	}

	/**
	 * Answers the list of settings pages for the selected configuration
	 */
	private List<AbstractConfigurePropertyOptionsPage> getPagesForConfig() {
		if (getCfg() == null)
			return null;
		List<AbstractConfigurePropertyOptionsPage> pages = configToPageListMap.get(getCfg().getName());
		if (pages == null) {
			pages = new ArrayList<>();
			configToPageListMap.put(getCfg().getName(), pages);
		}
		return pages;
	}

	@Override
	protected void performOK() {
		ICConfigurationDescription[] cfgs = page.getCfgsEditable();
		AutotoolsConfigurePropertyPage ap = (AutotoolsConfigurePropertyPage) page;
		Map<String, IAConfiguration> cfgList = new HashMap<>();
		for (int i = 0; i < cfgs.length; ++i) {
			ICConfigurationDescription cd = cfgs[i];
			IAConfiguration acfg = ap.getConfiguration(cd);
			cfgList.put(cd.getId(), acfg);
		}
		IProject project = page.getProject();
		AutotoolsConfigurationManager.getInstance().replaceProjectConfigurations(project, cfgList, cfgs);
		AutotoolsConfigurationManager.getInstance().clearTmpConfigurations(project);
	}

	@Override
	protected void performCancel() {
		AutotoolsConfigurationManager.getInstance().clearTmpConfigurations(page.getProject());
	}

	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		IProject project = page.getProject();
		ICConfigurationDescription[] cfgs = page.getCfgsEditable();
		// Apply all changes to existing saved configurations and new configurations, but do not perform
		// deletions.
		AutotoolsConfigurationManager.getInstance().applyConfigs(project.getName(), cfgs);
	}

	@Override
	protected void performDefaults() {
		IAConfiguration cfg = getAutotoolsCfg();
		cfg.setDefaultOptions();
		setValues();
	}

	@Override
	protected void updateData(ICResourceDescription rd) {
		if (rd == null)
			return;
		icfgd = rd.getConfiguration();
		setValues();
	}

	// IPreferencePageContainer methods
	@Override
	public void updateButtons() {
	}

	@Override
	public void updateMessage() {
	}

	@Override
	public void updateTitle() {
	}
}
