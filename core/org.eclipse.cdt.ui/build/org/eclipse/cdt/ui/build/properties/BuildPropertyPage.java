package org.eclipse.cdt.ui.build.properties;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.IOptionCategory;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.cdt.core.build.managed.ManagedBuildManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class BuildPropertyPage extends PropertyPage implements IWorkbenchPropertyPage, IPreferencePageContainer {
	/*
	 * String constants
	 */
	private static final String PREFIX = "BuildPropertyPage";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String NAME_LABEL = LABEL + ".NameText";	//$NON-NLS-1$
	private static final String BUILD_TOOLS_LABEL = LABEL + ".BuildToolTree";	//$NON-NLS-1$
	private static final String PLATFORM_LABEL = LABEL + ".Platform";	//$NON-NLS-1$
	private static final String CONFIG_LABEL = LABEL + ".Configuration";	//$NON-NLS-1$
	private static final String ACTIVE_LABEL = LABEL + ".Active";	//$NON-NLS-1$
	private static final String SETTINGS_LABEL = LABEL + ".Settings";	//$NON-NLS-1$
	private static final String TREE_LABEL = LABEL + ".ToolTree";	//$NON-NLS-1$
	private static final String OPTIONS_LABEL = LABEL + ".ToolOptions";	//$NON-NLS-1$
	private static final String ADD_CONF = LABEL + ".AddConfButton";	//$NON-NLS-1$
	private static final String TIP = PREFIX + ".tip";	//$NON-NLS-1$
	private static final String PLAT_TIP = TIP + ".platform";	//$NON-NLS-1$
	private static final String CONF_TIP = TIP + ".config";	//$NON-NLS-1$
	private static final String ADD_TIP = TIP + ".addconf";	//$NON-NLS-1$
	private static final String MANAGE_TITLE = PREFIX + ".manage.title";	//$NON-NLS-1$
	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 20, 30 };
	
	/*
	 * Dialog widgets
	 */
	private Combo targetSelector;
	private Combo configSelector;
	private Button manageConfigs;
	private TreeViewer optionList;
	private SashForm sashForm;
	private Group sashGroup;
	private Composite settingsPageContainer;
			 
	/*
	 * Bookeeping variables
	 */
	private ITarget [] targets;
	private ITarget selectedTarget;
	private IConfiguration [] configurations;
	private IConfiguration selectedConfiguration;
	private BuildToolSettingsPage currentSettingsPage;
	private Map configToPageListMap;
	private BuildToolsSettingsStore settingsStore;
	private IOptionCategory selectedCategory;
	private Point lastShellSize;

	/**
	 * The minimum page size; 200 by 200 by default.
	 *
	 * @see #setMinimumPageSize
	 */
	private Point minimumPageSize = new Point(200, 200);

	/**
	 * Layout for the page container.
	 *
	 */
	private class PageLayout extends Layout {
		public void layout(Composite composite, boolean force) {
			Rectangle rect = composite.getClientArea();
			Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++) {
				children[i].setSize(rect.width, rect.height);
			}
		}
		public Point computeSize(Composite composite, int wHint, int hHint,	boolean force) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
				return new Point(wHint, hHint);
			}
			int x = minimumPageSize.x;
			int y = minimumPageSize.y;

			Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++) {
				Point size = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
				x = Math.max(x, size.x);
				y = Math.max(y, size.y);
			}
			if (wHint != SWT.DEFAULT) {
				x = wHint;
			}
			if (hHint != SWT.DEFAULT) {
				y = hHint;
			}
			return new Point(x, y);
		}
	}
		
	/**
	 * Default constructor
	 */
	public BuildPropertyPage() {
		configToPageListMap = new HashMap();
	}

	protected void constrainShellSize() {
		// limit the shell size to the display size
		Shell shell = getShell();
		Point size = shell.getSize();
		Rectangle bounds = shell.getDisplay().getClientArea();
		int newX = Math.min(size.x, bounds.width);
		int newY = Math.min(size.y, bounds.height);
		if (size.x != newX || size.y != newY)
			shell.setSize(newX, newY);

		// move the shell origin as required
		Point loc = shell.getLocation();

		//Choose the position between the origin of the client area and 
		//the bottom right hand corner
		int x =
			Math.max(
				bounds.x,
				Math.min(loc.x, bounds.x + bounds.width - size.x));
		int y =
			Math.max(
				bounds.y,
				Math.min(loc.y, bounds.y + bounds.height - size.y));
		shell.setLocation(x, y);
		
		// record opening shell size
		if (lastShellSize == null)
			lastShellSize = getShell().getSize();
	}

	protected Control createContents(Composite parent)  {
		// Initialize the key data
		targets = ManagedBuildManager.getTargets(getProject());

		// Create the container we return to the property page editor
		Composite composite = ControlFactory.createComposite(parent, 1);
		GridData gd;

		// Add a config selection area
		Group configGroup = ControlFactory.createGroup(composite, CUIPlugin.getResourceString(ACTIVE_LABEL), 1);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.grabExcessHorizontalSpace = true;
		configGroup.setLayoutData(gd);
		// Use the form layout inside the group composite
		FormLayout form = new FormLayout();
		form.marginHeight = 5;
		form.marginWidth = 5;
		configGroup.setLayout(form);
		
		Label platformLabel = ControlFactory.createLabel(configGroup, CUIPlugin.getResourceString(PLATFORM_LABEL));
		targetSelector = ControlFactory.createSelectCombo(configGroup, getPlatformNames(), null); 
		targetSelector.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				handleTargetSelection();
			}
		});
		targetSelector.setToolTipText(CUIPlugin.getResourceString(PLAT_TIP));
		Label configLabel = ControlFactory.createLabel(configGroup, CUIPlugin.getResourceString(CONFIG_LABEL));
		configSelector = new Combo(configGroup, SWT.READ_ONLY|SWT.DROP_DOWN);
		configSelector.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				handleConfigSelection();
			}
		});
		configSelector.setToolTipText(CUIPlugin.getResourceString(CONF_TIP));
		manageConfigs = ControlFactory.createPushButton(configGroup, CUIPlugin.getResourceString(ADD_CONF));
		manageConfigs.setToolTipText(CUIPlugin.getResourceString(ADD_TIP));
		manageConfigs.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
				handleManageConfig();
			}
		});
		// Now do the form layout for the widgets
		FormData fd = new FormData();
		// Anchor the labels in the centre of their respective combos
		fd.top = new FormAttachment(targetSelector, 0, SWT.CENTER);
		platformLabel.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(configSelector, 0, SWT.CENTER);
		configLabel.setLayoutData(fd);
		// Anchor platform combo left to the config selector
		fd = new FormData();
		fd.left = new FormAttachment(configSelector, 0, SWT.LEFT);
		fd.right = new FormAttachment(100, 0);
		targetSelector.setLayoutData(fd);
		// Anchor button right to combo and left to group
		fd = new FormData();
		fd.top = new FormAttachment(configSelector, 0, SWT.CENTER);
		fd.right = new FormAttachment(100,0);
		manageConfigs.setLayoutData(fd);
		// Anchor config combo left 5 pixels from label, top 5% below the centre, and right to the button
		fd = new FormData();
		fd.left = new FormAttachment(configLabel, 5); 
		fd.top = new FormAttachment(55,0);
		fd.right = new FormAttachment(manageConfigs, -5 , SWT.LEFT);
		configSelector.setLayoutData(fd);		

		// Create the sash form
		sashGroup = ControlFactory.createGroup(composite, CUIPlugin.getResourceString(SETTINGS_LABEL), 1);
		sashGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		sashForm = new SashForm(sashGroup, SWT.NONE);
		sashForm.setOrientation(SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		sashForm.setLayout(layout);
		
		createSelectionArea(sashForm);
		createEditArea(sashForm);
		initializeSashForm();

		// Do not call this until the widgets are constructed		
		handleTargetSelection();
		return composite;
	}
	
	/* (non-Javadoc)
	 * Add the tabs relevant to the project to edit area tab folder.
	 */
	protected void createEditArea(Composite parent) {
		// Add a container for the build settings page
		settingsPageContainer = new Composite(parent, SWT.NULL);
		settingsPageContainer.setLayout(new PageLayout());
	}
	
	protected void createSelectionArea (Composite parent) {
		// Create a label and list viewer
		Composite composite = ControlFactory.createComposite(parent, 1);
		optionList = new TreeViewer(composite, SWT.SINGLE|SWT.H_SCROLL|SWT.V_SCROLL|SWT.BORDER);
		optionList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleOptionSelection();
			}
		});
		optionList.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		optionList.setLabelProvider(new ToolListLabelProvider());
	}

	/**
	 * Method displayOptionsForTool.
	 * @param toolReference
	 */
	private void displayOptionsForCategory(IOptionCategory category) {
		// Do nothing if the selected category is is unchanged
		if (selectedCategory == category) {
			return;
		}
		selectedCategory = category;
		
		// Cache the current build setting page
		BuildToolSettingsPage oldPage = currentSettingsPage;
		currentSettingsPage = null;
		
		// Create a new settings page if necessary
		List pages = getPagesForConfig();
		ListIterator iter = pages.listIterator();
		while (iter.hasNext()) {
			BuildToolSettingsPage page = (BuildToolSettingsPage) iter.next();
			if (page.getCategory().equals(category)) {
				currentSettingsPage = page;
				break;
			}
		}
		if (currentSettingsPage == null) {
			currentSettingsPage = new BuildToolSettingsPage(selectedConfiguration, category);
			pages.add(currentSettingsPage);
			currentSettingsPage.setContainer(this);
			if (currentSettingsPage.getControl() == null) {
				currentSettingsPage.createControl(settingsPageContainer);
			}
		}

		// Force calculation of the page's description label because
		// label can be wrapped.
		Point contentSize = currentSettingsPage.computeSize();
		// Do we need resizing. Computation not needed if the
		// first page is inserted since computing the dialog's
		// size is done by calling dialog.open().
		// Also prevent auto resize if the user has manually resized
		Shell shell = getShell();
		Point shellSize = shell.getSize();
		if (oldPage != null) {
			Rectangle rect = settingsPageContainer.getClientArea();
			Point containerSize = new Point(rect.width, rect.height);
			int hdiff = contentSize.x - containerSize.x;
			int vdiff = contentSize.y - containerSize.y;

			if (hdiff > 0 || vdiff > 0) {
				if (shellSize.equals(lastShellSize)) {
					hdiff = Math.max(0, hdiff);
					vdiff = Math.max(0, vdiff);
					setShellSize(shellSize.x + hdiff, shellSize.y + vdiff);
					lastShellSize = shell.getSize();
				} else {
					currentSettingsPage.setSize(containerSize);
				}
			} else if (hdiff < 0 || vdiff < 0) {
				currentSettingsPage.setSize(containerSize);
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
		if (oldPage != null)
			oldPage.setVisible(false);
	}

	/* (non-Javadoc)
	 * @return an array of names for the configurations defined for the chosen target
	 */
	private String [] getConfigurationNames () {
		String [] names = new String[configurations.length];
		for (int index = 0; index < configurations.length; ++index) {
			names[index] = configurations[index].getName();
		}
		return names;
	}
	
	private List getPagesForConfig() {
		List pages = (List) configToPageListMap.get(selectedConfiguration.getId());
		if (pages == null) {
			pages = new ArrayList();
			configToPageListMap.put(selectedConfiguration.getId(), pages);
		}
		return pages;
	}
	
	private String [] getPlatformNames() {
		String [] names = new String[targets.length];
		for (int index = 0; index < targets.length; ++index) {
			names[index] = targets[index].getName();
		}
		return names;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#getPreferenceStore()
	 */
	public IPreferenceStore getPreferenceStore()
	{
		return settingsStore;
	}

	private IProject getProject() {
		Object element= getElement();
		if (element != null && element instanceof IProject) {
			return (IProject)element;
		}
		return null;
	}

	/**
	 * @return
	 */
	public IConfiguration getSelectedConfiguration() {
		return selectedConfiguration;
	}

	/*
	 * Event Handlers
	 */
	private void handleConfigSelection () {
		// If there is nothing in config selection widget just bail
		if (configSelector.getItemCount() == 0) return;
		
		// Cache the selected config 
		selectedConfiguration = configurations[configSelector.getSelectionIndex()];
		
		// Set the content provider for the list viewer
		ToolListContentProvider provider = new ToolListContentProvider();
		optionList.setContentProvider(provider);
		optionList.setInput(selectedConfiguration);
		optionList.expandAll();
		
		// Recreate the settings store for the configuration
		settingsStore = new BuildToolsSettingsStore(selectedConfiguration);
		
		// Select the first option in the list
		Object[] elements = provider.getElements(selectedConfiguration);
		Object primary = elements.length > 0 ? elements[0] : null;
		if (primary != null) {
			optionList.setSelection(new StructuredSelection(primary));
		}
	}

	// Event handler for the manage configuration button event
	private void handleManageConfig () {
		ManageConfigDialog manageDialog = new ManageConfigDialog(getShell(), CUIPlugin.getResourceString(MANAGE_TITLE), selectedTarget);
		if (manageDialog.open() == ManageConfigDialog.OK) {
			// Check to see if any configurations have to be deleted
			ArrayList deleteMe = manageDialog.getDeletedConfigs();
			ListIterator iter = deleteMe.listIterator();
			while (iter.hasNext()) {
			}
			return;
		}
	}

	private void handleOptionSelection() {
		// Get the selection from the tree list
		IStructuredSelection selection = (IStructuredSelection) optionList.getSelection();
	
		// Set the option page based on the selection
		Object element = selection.getFirstElement();
		if (element instanceof IOptionCategory) {
			displayOptionsForCategory((IOptionCategory)element);
		}
	}

	private void handleTargetSelection() {
		// Is there anything in the selector widget
		if (targetSelector.getItemCount() == 0) {
			manageConfigs.setEnabled(false);
			return;
		} 

		// Enable the manage button
		manageConfigs.setEnabled(true);

		// Cache the platform at the selection index
		selectedTarget = targets[targetSelector.getSelectionIndex()];
		
		// Update the contents of the configuration widget
		populateConfigurations();		
	}

	/**
	 * Initialize the relative weights (widths) of the 2 sides of the sash.
	 */
	protected void initializeSashForm() {
		sashForm.setWeights(DEFAULT_SASH_WEIGHTS);
	}

	public boolean performOk() {
		// Force each settings page to update
		List pages = getPagesForConfig();
		ListIterator iter = pages.listIterator();
		while (iter.hasNext()) {
			BuildToolSettingsPage page = (BuildToolSettingsPage) iter.next();
			page.performOk();
		}
		
		// Write out the build model info
		ManagedBuildManager.setDefaultConfiguration(getProject(), getSelectedConfiguration());
		ManagedBuildManager.saveBuildInfo(getProject());
		return true;
	}
	
	private void populateConfigurations() {
		// If the config select widget is not there yet, just stop		
		if (configSelector == null) return;
		
		// Find the configurations defined for the platform
		configurations = selectedTarget.getConfigurations();
		
		// Clear and replace the contents of the selector widget
		configSelector.removeAll();
		configSelector.setItems(getConfigurationNames());
		
		// Make sure the active configuration is selected
		configSelector.select(0);
		handleConfigSelection();
	}
	
	/**
	 * Changes the shell size to the given size, ensuring that
	 * it is no larger than the display bounds.
	 * 
	 * @param width the shell width
	 * @param height the shell height
	 */
	private void setShellSize(int width, int height) {
		getShell().setSize(width, height);
		constrainShellSize();
	}
	
	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateButtons()
	 */
	public void updateButtons() {
	}
	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateMessage()
	 */
	public void updateMessage() {
	}
	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateTitle()
	 */
	public void updateTitle() {
	}
	


}