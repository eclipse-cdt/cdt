package org.eclipse.cdt.managedbuilder.ui.properties;

/**********************************************************************
 * Copyright (c) 2002,2004 Rational Software Corporation and others.
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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
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
import org.eclipse.swt.custom.ScrolledComposite;
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
	private static final String ALL_CONFS = PREFIX + ".selection.configuration.all";	//$NON-NLS-1$
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
	private static final String ID_SEPARATOR = ".";	//$NON-NLS-1$
	
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
	private ScrolledComposite containerSC;
				 
	/*
	 * Bookeeping variables
	 */
	private ITarget [] targets;
	private ITarget selectedTarget;
	private IConfiguration [] configurations;
	private IConfiguration selectedConfiguration;
	private BuildSettingsPage currentSettingsPage;
	private Map configToPageListMap;
	private BuildToolsSettingsStore settingsStore;
	private Map settingsStoreMap;
	private IOptionCategory selectedCategory;
	private Point lastShellSize;
	private ITool selectedTool;

	/**
	 * The minimum page size; 200 by 200 by default.
	 *
	 * @see #setMinimumPageSize
	 */
	private Point minimumPageSize = new Point(200, 200);
	private ToolListContentProvider provider;

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

	protected Control createContents(Composite parent)  {
		// Create the container we return to the property page editor
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(1, true));
		GridData gd;

		// Initialize the key data
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		if (info.getVersion() == null) {
			// Display a message page instead of the properties control
			final Label invalidInfo = new Label(composite, SWT.LEFT);
			invalidInfo.setFont(composite.getFont());
			invalidInfo.setText(ManagedBuilderUIMessages.getResourceString("BuildPropertyPage.error.version_low"));	//$NON-NLS-1$
			invalidInfo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING, GridData.VERTICAL_ALIGN_CENTER, true, true));
			return composite;
		}
		targets = ManagedBuildManager.getTargets(getProject());
		ITarget defaultTarget = info.getDefaultTarget();


		// Add a config selection area
		Group configGroup = ControlFactory.createGroup(composite, ManagedBuilderUIMessages.getResourceString(ACTIVE_LABEL), 1);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.grabExcessHorizontalSpace = true;
		configGroup.setLayoutData(gd);
		// Use the form layout inside the group composite
		FormLayout form = new FormLayout();
		form.marginHeight = 5;
		form.marginWidth = 5;
		configGroup.setLayout(form);
		
		Label platformLabel = ControlFactory.createLabel(configGroup, ManagedBuilderUIMessages.getResourceString(PLATFORM_LABEL));
		targetSelector = ControlFactory.createSelectCombo(configGroup, getPlatformNames(), defaultTarget.getName()); 
		targetSelector.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				handleTargetSelection();
			}
		});
		targetSelector.setToolTipText(ManagedBuilderUIMessages.getResourceString(PLAT_TIP));
		Label configLabel = ControlFactory.createLabel(configGroup, ManagedBuilderUIMessages.getResourceString(CONFIG_LABEL));
		configSelector = new Combo(configGroup, SWT.READ_ONLY|SWT.DROP_DOWN);
		configSelector.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				handleConfigSelection();
			}
		});
		configSelector.setToolTipText(ManagedBuilderUIMessages.getResourceString(CONF_TIP));
		manageConfigs = ControlFactory.createPushButton(configGroup, ManagedBuilderUIMessages.getResourceString(ADD_CONF));
		manageConfigs.setToolTipText(ManagedBuilderUIMessages.getResourceString(ADD_TIP));
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
		sashGroup = ControlFactory.createGroup(composite, ManagedBuilderUIMessages.getResourceString(SETTINGS_LABEL), 1);
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
		containerSC = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		containerSC.setExpandHorizontal(true);
		containerSC.setExpandVertical(true);
		
		// Add a container for the build settings page
		settingsPageContainer = new Composite(containerSC, SWT.NULL);
		settingsPageContainer.setLayout(new PageLayout());

		containerSC.setContent(settingsPageContainer);
		containerSC.setMinSize(settingsPageContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		settingsPageContainer.layout();
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

	/* (non-Javadoc)
	 * Method displayOptionsForTool.
	 * @param toolReference
	 */
	private void displayOptionsForCategory(IOptionCategory category) {
		// Do nothing if the selected category is is unchanged
		if (category == selectedCategory) {
			return;
		}
		selectedTool = null;
		selectedCategory = category;
		
		// Cache the current build setting page
		BuildSettingsPage oldPage = currentSettingsPage;
		currentSettingsPage = null;
		
		// Create a new settings page if necessary
		List pages = getPagesForConfig();
		ListIterator iter = pages.listIterator();
		while (iter.hasNext()) {
			BuildSettingsPage page = (BuildSettingsPage) iter.next();
			if (page instanceof BuildOptionSettingsPage && 
					((BuildOptionSettingsPage)page).isForCategory(category)) {
				currentSettingsPage = page;
				break;
			}
		}
		if (currentSettingsPage == null) {
			currentSettingsPage = new BuildOptionSettingsPage(selectedConfiguration, category);
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

		// save the last page build options.
		// If the last page is tool page then parse all the options
		// and put it in the appropriate preference store.
		if (oldPage != null){
			if(oldPage instanceof BuildOptionSettingsPage) {
				((BuildOptionSettingsPage)oldPage).storeSettings();
			}
			else if(oldPage instanceof BuildToolSettingsPage) {
				((BuildToolSettingsPage)oldPage).storeSettings();
				((BuildToolSettingsPage)oldPage).parseAllOptions();
			}
		}
		//update the field editors in the current page
		if(currentSettingsPage instanceof BuildOptionSettingsPage)
			((BuildOptionSettingsPage)currentSettingsPage).updateFields();
		
		if (oldPage != null)
			oldPage.setVisible(false);

		// Set the size of the scrolled area
		containerSC.setMinSize(currentSettingsPage.computeSize());
		settingsPageContainer.layout();
	}

	/* (non-Javadoc)
	 * @param tool
	 */
	private void displayOptionsForTool(ITool tool) {
		if (tool == selectedTool) {
			return;
		}
		// Unselect the category
		selectedCategory = null;
		// record that the tool selection has changed
		selectedTool = tool;
		
		// Cache the current build setting page
		BuildSettingsPage oldPage = currentSettingsPage;
		currentSettingsPage = null;

		// Create a new page if we need one
		List pages = getPagesForConfig();
		ListIterator iter = pages.listIterator();
		while (iter.hasNext()) {
			BuildSettingsPage page = (BuildSettingsPage) iter.next();
			if (page instanceof BuildToolSettingsPage && 
					((BuildToolSettingsPage)page).isForTool(tool)) {
				currentSettingsPage = page;
				break;
			}
		}
		if (currentSettingsPage == null) {
			currentSettingsPage = new BuildToolSettingsPage(selectedConfiguration, tool);
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
		
		// Make the current page visible
		currentSettingsPage.setVisible(true);

		// save the last page build options.
		// If the last page is tool page then parse all the options
		// and put it in the appropriate preference store.
		if (oldPage != null){
			if(oldPage instanceof BuildOptionSettingsPage) {
				((BuildOptionSettingsPage)oldPage).storeSettings();
			}
			else if(oldPage instanceof BuildToolSettingsPage) {
				((BuildToolSettingsPage)oldPage).storeSettings();
				((BuildToolSettingsPage)oldPage).parseAllOptions();
			}
		}
		//update the field editor that displays all the build options
		if(currentSettingsPage instanceof BuildToolSettingsPage)
			((BuildToolSettingsPage)currentSettingsPage).updateAllOptionField();

		if (oldPage != null)
			oldPage.setVisible(false);

		// Set the size of the scrolled area
		containerSC.setMinSize(currentSettingsPage.computeSize());
		settingsPageContainer.layout();			
	}

	/* (non-Javadoc)
	 * @return an array of names for the configurations defined for the chosen target
	 */
	private String [] getConfigurationNames () {
		String [] names = new String[configurations.length /*+ 1*/];
		for (int index = 0; index < configurations.length; ++index) {
			names[index] = configurations[index].getName();
		}
//		names[names.length - 1] = ManagedBuilderUIPlugin.getResourceString(ALL_CONFS);
		return names;
	}
	
	/* (non-Javadoc)
	 * @return
	 */
	protected Point getLastShellSize() {
		if (lastShellSize == null) {
			Shell shell = getShell();
			if (shell != null)
				lastShellSize = shell.getSize();
		}
		return lastShellSize;
	}

	/* (non-Javadoc)
	 * Answers the list of settings pages for the selected configuration 
	 * @return 
	 */
	private List getPagesForConfig() {
		// Make sure that something was selected
		if (selectedConfiguration == null) {
			return null;
		}
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

	/* (non-Javadoc)
	 * @return
	 */
	public ITarget getSelectedTarget() {
		return selectedTarget;
	}

	/* (non-Javadoc)
	 * @return
	 */
	protected IConfiguration getSelectedConfiguration() {
		return selectedConfiguration;
	}

	/* (non-Javadoc)
	 * Safe accessor method
	 * 
	 * @return Returns the Map of configurations to preference stores.
	 */
	protected Map getSettingsStoreMap() {
		if (settingsStoreMap == null) {
			settingsStoreMap = new HashMap();
		}
		return settingsStoreMap;
	}
	/*
	 * Event Handlers
	 */
	private void handleConfigSelection () {
		// If there is nothing in config selection widget just bail
		if (configSelector.getItemCount() == 0) return;
		
		// Check if the user has selected the "all" configuration
		int selectionIndex = configSelector.getSelectionIndex();
		if (selectionIndex == -1) return;
		String configName = configSelector.getItem(selectionIndex);
		if (configName.equals(ManagedBuilderUIMessages.getResourceString(ALL_CONFS))) {
			// This is the all config
			return;
		} else {
			// Cache the selected config 
			selectedConfiguration = configurations[selectionIndex];
		}
		
		if (provider == null) {
			provider = new ToolListContentProvider();
			optionList.setContentProvider(provider);
		}
		optionList.setInput(selectedConfiguration);
		optionList.expandAll();
		
		// Create (or retrieve) the settings store for the configuration
		BuildToolsSettingsStore store = (BuildToolsSettingsStore) getSettingsStoreMap().get(selectedConfiguration.getId());
		if (store == null) {
			store = new BuildToolsSettingsStore(selectedConfiguration);
			getSettingsStoreMap().put(selectedConfiguration.getId(), store);
		}
		settingsStore = store; 
		
		// Select the first tool in the list
		Object[] elements = provider.getElements(selectedConfiguration);
		Object primary = elements.length > 0 ? elements[0] : null;
		
/*		if (primary != null && primary instanceof ITool) {
			// set the tool as primary selection in the tree hence it displays all the build options.
			ITool tool = (ITool)primary;
			IOptionCategory top = tool.getTopOptionCategory();
			IOption[] topOpts = top.getOptions(selectedConfiguration);
			if (topOpts != null && topOpts.length == 0) {
				// Get the children categories and start looking
				IOptionCategory[] children = top.getChildCategories();
				for (int i = 0; i < children.length; i++) {
					IOptionCategory category = children[i];
					IOption[] catOpts = category.getOptions(selectedConfiguration);
					if (catOpts != null && catOpts.length > 0) {
						primary = category;
						break;
					}
				}
			}
		}
*/		
		if (primary != null) {
			optionList.setSelection(new StructuredSelection(primary));
		}
	}

	// Event handler for the manage configuration button event
	private void handleManageConfig () {
		ManageConfigDialog manageDialog = new ManageConfigDialog(getShell(), ManagedBuilderUIMessages.getResourceString(MANAGE_TITLE), selectedTarget);
		if (manageDialog.open() == ManageConfigDialog.OK) {
			boolean updateConfigs = false;
			
			// Get the build output name
			String newBuildOutput = manageDialog.getBuildArtifactName();
			if (!selectedTarget.getArtifactName().equals(newBuildOutput)) {
				selectedTarget.setArtifactName(newBuildOutput);
			}
			String newBuildExt = manageDialog.getBuildArtifaceExtension();
			if (!selectedTarget.getArtifactExtension().equals(newBuildExt)) {
				selectedTarget.setArtifactExtension(newBuildExt);
			}
			
			// Get the new make command
			if (manageDialog.useDefaultMakeCommand()) {
				// This is a cheap assignment to null so do it to be doubly sure
				selectedTarget.resetMakeCommand();
			} else {
				// Parse for command and arguments
				String rawCommand = manageDialog.getMakeCommand();
				String makeCommand = parseMakeCommand(rawCommand);
				selectedTarget.setMakeCommand(makeCommand);
				String makeArguments = parseMakeArgs(rawCommand);
				selectedTarget.setMakeArguments(makeArguments);
			}
			
			// Check to see if any configurations have to be deleted
			List deletedConfigs = manageDialog.getDeletedConfigIds();
			Iterator iter = deletedConfigs.listIterator();
			while (iter.hasNext()) {
				String id = (String)iter.next();
				
				// Remove the configurations from the target 
				selectedTarget.removeConfiguration(id);
				
				// Remove any settings stores
				getSettingsStoreMap().remove(id);
				
				// Clean up the UI
				configurations = selectedTarget.getConfigurations();
				configSelector.removeAll();
				configSelector.setItems(getConfigurationNames());
				configSelector.select(0);
				updateConfigs = true;
			}
			
			// Check to see if any have to be added
			SortedMap newConfigs = manageDialog.getNewConfigs();
			Set keys = newConfigs.keySet();
			Iterator keyIter = keys.iterator();
			Random r = new Random();
			r.setSeed(System.currentTimeMillis());
			while (keyIter.hasNext()) {
				String name = (String) keyIter.next();
				IConfiguration parent = (IConfiguration) newConfigs.get(name);
				if (parent != null) {
					int id = r.nextInt();
					if (id < 0) {
						id *= -1;
					}
					
					// Create ID for the new component based on the parent ID and random component
					String newId = parent.getId();
					int index = newId.lastIndexOf(ID_SEPARATOR);
					if (index > 0) {
						String lastComponent = newId.substring(index + 1, newId.length());
						if (Character.isDigit(lastComponent.charAt(0))) {
							// Strip the last component
							newId = newId.substring(0, index);
						}
					}
					newId += ID_SEPARATOR + id;
					IConfiguration newConfig = selectedTarget.createConfiguration(parent, newId);
					newConfig.setName(name);
					// Update the config lists
					configurations = selectedTarget.getConfigurations();
					configSelector.removeAll();
					configSelector.setItems(getConfigurationNames());
					configSelector.select(configSelector.indexOf(name));
					updateConfigs = true;
				}
			}
			if (updateConfigs){
				handleConfigSelection();
			}
		}
		return;
	}

	private void handleOptionSelection() {
		// Get the selection from the tree list
		IStructuredSelection selection = (IStructuredSelection) optionList.getSelection();
	
		// Set the option page based on the selection
		Object element = selection.getFirstElement();
		if (element instanceof ITool) {
			displayOptionsForTool((ITool)element);
		} else if (element instanceof IOptionCategory) {
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
		ManagedBuildManager.setSelectedTarget(getProject(), selectedTarget);
		
		// Update the contents of the configuration widget
		populateConfigurations();		
	}

	/* 
	 *  (non-javadoc)
	 * Initialize the relative weights (widths) of the 2 sides of the sash.
	 */
	protected void initializeSashForm() {
		sashForm.setWeights(DEFAULT_SASH_WEIGHTS);
	}

	/* (non-Javadoc)
	 * @param rawCommand
	 * @return
	 */
	private String parseMakeArgs(String rawCommand) {
		StringBuffer result = new StringBuffer();		
		
		// Parse out the command
		String actualCommand = parseMakeCommand(rawCommand);
		
		// The flags and targets are anything not in the command
		String arguments = rawCommand.substring(actualCommand.length());

		// If there aren't any, we can stop
		if (arguments.length() == 0) {
			return result.toString().trim();
		}

		String[] tokens = arguments.trim().split("\\s"); //$NON-NLS-1$
		/*
		 * Cases to consider
		 * --<flag>					Sensible, modern single flag. Add to result and continue.
		 * -<flags>					Flags in single token, add to result and stop
		 * -<flag_with_arg> ARG		Flag with argument. Add next token if valid arg.
		 * -<mixed_flags> ARG		Mix of flags, one takes arg. Add next token if valid arg.
		 * -<flag_with_arg>ARG		Corrupt case where next token should be arg but isn't
		 * -<flags> [target]..		Flags with no args, another token, add flags and stop.
		 */
		Pattern flagPattern = Pattern.compile("C|f|I|j|l|O|W"); //$NON-NLS-1$
		// Look for a '-' followed by 1 or more flags with no args and exactly 1 that expects args
		Pattern mixedFlagWithArg = Pattern.compile("-[^CfIjloW]*[CfIjloW]{1}.+"); //$NON-NLS-1$
		for (int i = 0; i < tokens.length; ++i) {
			String currentToken = tokens[i];
			if (currentToken.startsWith("--")) { //$NON-NLS-1$
				result.append(currentToken);
				result.append(" "); //$NON-NLS-1$
			} else if (currentToken.startsWith("-")) { //$NON-NLS-1$
				// Is there another token
				if (i + 1 >= tokens.length) {
					//We are done
					result.append(currentToken);
				} else {
					String nextToken = tokens[i + 1];
					// Are we expecting arguments
					Matcher flagMatcher = flagPattern.matcher(currentToken);
					if (!flagMatcher.find()) {
						// Evalutate whether the next token should be added normally
						result.append(currentToken);
						result.append(" "); //$NON-NLS-1$
					} else {
						// Look for the case where there is no space between flag and arg
						if (mixedFlagWithArg.matcher(currentToken).matches()) {
							// Add this single token and keep going
							result.append(currentToken);
							result.append(" ");							 //$NON-NLS-1$
						} else {
							// Add this token and the next one right now
							result.append(currentToken);
							result.append(" "); //$NON-NLS-1$
							result.append(nextToken);
							result.append(" "); //$NON-NLS-1$
							// Skip the next token the next time through, though
							++i;
						}
					}
				}
			}
		}
		
		return result.toString().trim();
	}

	/* (non-Javadoc)
	 * 
	 * @param string
	 * @return
	 */
	private String parseMakeCommand(String rawCommand) {
		StringBuffer command = new StringBuffer();
		boolean hasSpace = false;
		
		// Try to separate out the command from the arguments 
		String[] result = rawCommand.split("\\s"); //$NON-NLS-1$
		
		/*
		 * Here are the cases to consider:
		 * 	cmd								First segment is last segment, assume is command
		 * 	cmd [flags]						First segment is the command
		 * 	path/cmd [flags]				Same as above
		 * 	path with space/make [flags]	Must append each segment up-to flags as command
		 */
		for (int i = 0; i < result.length; ++i) {
			// Get the segment
			String cmdSegment = result[i];
			// If there is not another segment, we found the end
			if (i + 1 >= result.length) {
				command.append(cmdSegment);
			} else {
				// See if the next segment is the start of the flags
				String nextSegment = result[i + 1];
				if (nextSegment.startsWith("-")) { //$NON-NLS-1$
					// we have found the end of the command
					command.append(cmdSegment);
					break;
				} else {
					command.append(cmdSegment);
					// Add the whitespace back
					command.append(" "); //$NON-NLS-1$
					hasSpace = true;
				}
			}
		}
		
//		if (hasSpace == true) {
//			return "\"" + command.toString().trim() + "\"";
//		} else {
			return command.toString().trim();
//		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		// Empty the page list
		List pages = getPagesForConfig();
		pages.clear();
		
		// Get the build manager to reset build info for project
		ManagedBuildManager.resetConfiguration(getProject(), getSelectedConfiguration());
		
		// Recreate the settings store for the configuration
		settingsStore = new BuildToolsSettingsStore(getSelectedConfiguration());

		// Write out the build model info
		ManagedBuildManager.setDefaultConfiguration(getProject(), getSelectedConfiguration());
		ManagedBuildManager.saveBuildInfo(getProject(), false);

		// Reset the category or tool selection and run selection event handler
		selectedCategory = null;
		selectedTool = null;
		handleOptionSelection();
	}

	/* 
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		// Force each settings page to update
		List pages = getPagesForConfig();
		// Make sure we have something to work on
		if (pages == null) {
			// Nothing to do
			return true;
		}
		ListIterator iter = pages.listIterator();
		while (iter.hasNext()) {
			BuildSettingsPage page = (BuildSettingsPage) iter.next();
			if (page == null) continue;
			if (page instanceof BuildToolSettingsPage) {
				// if the currentsettings page is not the tool settings page
				// then update the all build options field editor based on the 
				// build options in other options settings page.
				if (!(currentSettingsPage instanceof BuildToolSettingsPage))
					((BuildToolSettingsPage)page).updateAllOptionField();
				((BuildToolSettingsPage)page).performOk();
			} else if (page instanceof BuildOptionSettingsPage) {
				((BuildOptionSettingsPage)page).performOk();				
			}
		}
		
		// Write out the build model info
		ManagedBuildManager.setDefaultConfiguration(getProject(), getSelectedConfiguration());
		ManagedBuildManager.saveBuildInfo(getProject(), false);
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
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		IConfiguration defaultConfig = info.getDefaultConfiguration(selectedTarget);
		int index = configSelector.indexOf(defaultConfig.getName());
		configSelector.select(index == -1 ? 0 : index);
		handleConfigSelection();
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