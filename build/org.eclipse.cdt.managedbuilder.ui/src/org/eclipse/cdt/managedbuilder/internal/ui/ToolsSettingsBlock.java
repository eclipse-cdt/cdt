/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Move to Make plugin
 * Intel Corp - Use in Managed Make system
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractBuildPropertyPage;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildOptionSettingsPage;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildPreferencePage;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildPropertyPage;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildSettingsPage;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildToolSettingsPage;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildToolSettingsPreferenceStore;
import org.eclipse.cdt.managedbuilder.ui.properties.ResourceBuildPropertyPage;
import org.eclipse.cdt.managedbuilder.ui.properties.ToolListContentProvider;
import org.eclipse.cdt.managedbuilder.ui.properties.ToolListLabelProvider;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class ToolsSettingsBlock extends AbstractCOptionPage {

	/*
	 * String constants
	 */
	private static final String PREFIX = "ToolsSettingsBlock";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String SETTINGS_LABEL = LABEL + ".Settings";	//$NON-NLS-1$
	//private static final String TREE_LABEL = LABEL + ".ToolTree";	//$NON-NLS-1$
	//private static final String OPTIONS_LABEL = LABEL + ".ToolOptions";	//$NON-NLS-1$
	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 20, 30 };

	private static final String EMPTY_STRING = new String();
	
	/*
	 * Dialog widgets
	 */
	private TreeViewer optionList;
	private SashForm sashForm;
	private Composite settingsPageContainer;
	private ScrolledComposite containerSC;

	/*
	 * Bookeeping variables
	 */
	private Map configToPageListMap;
	private BuildToolSettingsPreferenceStore settingsStore;
	private BuildPropertyPage parent;
	private ResourceBuildPropertyPage resParent;
	private BuildSettingsPage currentSettingsPage;
	private IOptionCategory selectedCategory;
	private ToolListContentProvider provider;
	private ITool selectedTool;
	private Object element;
	
	private boolean defaultNeeded;

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

	/*
	 *  Constructor
	 */
	public ToolsSettingsBlock(BuildPropertyPage parent, Object element)
	{
		super(ManagedBuilderUIMessages.getResourceString(SETTINGS_LABEL));
		super.setContainer(parent);
		this.parent = parent;
		configToPageListMap = new HashMap();
		this.element = element;
		settingsStore = new BuildToolSettingsPreferenceStore(this);
	}

	public ToolsSettingsBlock(ResourceBuildPropertyPage resParent, Object element)
	{
		super(ManagedBuilderUIMessages.getResourceString(SETTINGS_LABEL));
		super.setContainer((ICOptionContainer) resParent);
		this.resParent = resParent;
		configToPageListMap = new HashMap();
		this.element = element;
		settingsStore = new BuildToolSettingsPreferenceStore(this);
	}
	
	public void createControl(Composite parent)  {
	
		// Create the sash form
		sashForm = new SashForm(parent, SWT.NONE);
		sashForm.setOrientation(SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		sashForm.setLayout(layout);
		
		setControl(sashForm);
		createSelectionArea(sashForm);
		createEditArea(sashForm);
		initializeSashForm();
		
		//WorkbenchHelp.setHelp(composite, ManagedBuilderHelpContextIds.MAN_PROJ_BUILD_PROP);
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
		optionList.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer,
									Object parent,
									Object element) {
				if(parent instanceof IResourceConfiguration && element instanceof ITool) {
					return !((ITool)element).getCustomBuildStep();
				} else {
					return true;
				}
			}
		});
	}

	/* (non-Javadoc)
	 * Method displayOptionsForCategory
	 * @param category
	 */
	private void displayOptionsForCategory(IOptionCategory category) {
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
			if ( this.element instanceof IProject) {
				currentSettingsPage = new BuildOptionSettingsPage(parent,parent.getSelectedConfigurationClone(), category);
				pages.add(currentSettingsPage);
				currentSettingsPage.setContainer(parent);
			} else if ( this.element instanceof IFile) {
				currentSettingsPage = new BuildOptionSettingsPage(resParent,resParent.getCurrentResourceConfigClone(), category);
				pages.add(currentSettingsPage);
				currentSettingsPage.setContainer(resParent);
			}
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

		//update the field editors in the current page
		if(currentSettingsPage instanceof BuildOptionSettingsPage)
			((BuildOptionSettingsPage)currentSettingsPage).updateFields();
		
		if (oldPage != null  && oldPage != currentSettingsPage)
			oldPage.setVisible(false);

		// Set the size of the scrolled area
		containerSC.setMinSize(currentSettingsPage.computeSize());
		settingsPageContainer.layout();
	}

	/* (non-Javadoc)
	 * Method displayOptionsForTool
	 * @param tool
	 */
	private void displayOptionsForTool(ITool tool) {
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
			if ( this.element instanceof IProject) {
				currentSettingsPage = new BuildToolSettingsPage(parent, 
						parent.getSelectedConfigurationClone(), tool);
				pages.add(currentSettingsPage);
				currentSettingsPage.setContainer(parent);
			} else if(this.element instanceof IFile) {
				currentSettingsPage = new BuildToolSettingsPage(resParent,
						resParent.getCurrentResourceConfigClone(), tool);
				pages.add(currentSettingsPage);
				currentSettingsPage.setContainer(resParent);
			}
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

		// Save the last page build options.
		if (oldPage != null && oldPage != currentSettingsPage){
			if(oldPage instanceof BuildOptionSettingsPage) {
				((BuildOptionSettingsPage)oldPage).storeSettings();
			}
			else if(oldPage instanceof BuildToolSettingsPage) {
				((BuildToolSettingsPage)oldPage).storeSettings();
				//((BuildToolSettingsPage)oldPage).parseAllOptions();
			}
		}
		// Update the field editor that displays all the build options
		if(currentSettingsPage instanceof BuildToolSettingsPage)
			((BuildToolSettingsPage)currentSettingsPage).setValues();

		if (oldPage != null && oldPage != currentSettingsPage)
			oldPage.setVisible(false);

		// Set the size of the scrolled area
		containerSC.setMinSize(currentSettingsPage.computeSize());
		settingsPageContainer.layout();			
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

	/* 
	 *  (non-javadoc)
	 * Initialize the relative weights (widths) of the 2 sides of the sash.
	 */
	protected void initializeSashForm() {
		sashForm.setWeights(DEFAULT_SASH_WEIGHTS);
	}

	protected void initializeValues() {
		setValues();
	}

	public void updateValues() {
		setValues();	
	}

	public void setVisible(boolean visible){
		if(visible){
			selectedCategory = null;
			selectedTool = null;
			handleOptionSelection();
		}
		super.setVisible(visible);
	}

	protected void setValues() {
		
		IConfiguration config = null;	
		IResourceConfiguration resConfig = null;
		
		if (provider == null) {
//			IResource element = parent.getProject(); 
			IResource resource = (IResource) element;
			provider = new ToolListContentProvider(resource.getType());
			optionList.setContentProvider(provider);
		}
		if ( element instanceof IProject ) {
			config = parent.getSelectedConfigurationClone();	
			optionList.setInput(config);	
		} else if ( element instanceof IFile){
			resConfig = resParent.getCurrentResourceConfigClone();
			optionList.setInput(resConfig);
		}
		
		optionList.expandAll();
		
		// Determine what the selection in the tree should be
		Object primary = null;
		if (selectedTool != null) {
			// There is a selected tool defined.  See if it matches any current tool (by name)
			ITool[] tools = null;
			if ( element instanceof IProject ) {
				tools = config.getFilteredTools();
			} else if ( element instanceof IFile){
				tools = resConfig.getTools();
			}			
			String matchName = selectedTool.getName();
			for (int i=0; i<tools.length; i++) {
				ITool tool = tools[i];
				if (tool.getName().equals(matchName)) {
					primary = tool;
					break;
				}
			}
		} else if (selectedCategory != null) {
			// There is a selected option or category.  
			// See if it matches any category in the current config (by name)
			ITool[] tools = null;
			IToolChain toolChain = null;
			
			if ( element instanceof IProject ) {
				tools = config.getFilteredTools();
				toolChain = config.getToolChain();
			} else if ( element instanceof IFile){
				tools = resConfig.getTools();
			}
			IBuildObject catOrTool = selectedCategory;
			// Make the match name
			String matchName = makeMatchName(catOrTool);
			// Search for selected category/tool in toolChain
			if ( toolChain != null ) {
				primary = findOptionCategoryByMatchName(matchName, toolChain.getChildCategories());
			}			
			// Search for selected category/tool in tools
			if ( primary == null ) {
				for (int i=0; i<tools.length && primary == null; i++) {
					primary = findOptionCategoryByMatchName(matchName, tools[i].getChildCategories());
				}
			}
		} 
		
		if (primary == null) {
			// Select the first tool in the list
			Object[] elements = null;
			if( element instanceof IProject){
				elements = provider.getElements(parent.getSelectedConfigurationClone());
			} else if ( element instanceof IFile) {
				elements = provider.getElements(resParent.getCurrentResourceConfigClone());
			}
			primary = elements.length > 0 ? elements[0] : null;			
		}
		
		if (primary != null) {
			if(primary instanceof IOptionCategory){
				if(resConfig != null)
					settingsStore.setSelection(resConfig,(IOptionCategory)primary);
				else
					settingsStore.setSelection(config,(IOptionCategory)primary);
			}
			optionList.setSelection(new StructuredSelection(primary), true);
		}
	}

	public void removeValues(String id) {
	}

	private void handleOptionSelection() {
		// Get the selection from the tree list
		IStructuredSelection selection = (IStructuredSelection) optionList.getSelection();
	
		// Set the option page based on the selection
		Object element = selection.getFirstElement();
		if(element instanceof IOptionCategory){
			if(resParent != null)
				settingsStore.setSelection(resParent.getCurrentResourceConfigClone(),(IOptionCategory)element);
			else
				settingsStore.setSelection(parent.getSelectedConfigurationClone(),(IOptionCategory)element);
		}
		if (element instanceof ITool) {
			displayOptionsForTool((ITool)element);
		} else if (element instanceof IOptionCategory) {
			displayOptionsForCategory((IOptionCategory)element);
		}
	}

	/**
	 * Call an MBS CallBack function to inform that default settings have been applied.
	 * This has to be sent to all the Options associated with this configuration.
	 */
/*	private void performSetDefaultsEventCallBack() {
		
		if ( element instanceof IProject) {
			// Do not send the event to the child resource configurations, as performDefaults
			// is only scoped to what is visible in the UI.
			ManagedBuildManager.performValueHandlerEvent(parent.getSelectedConfiguration(), 
					IManagedOptionValueHandler.EVENT_SETDEFAULT, false);
		} else if ( element instanceof IFile) {
			IResourceConfiguration rcCfg = resParent.getCurrentResourceConfig(false);
			if(rcCfg != null)
				ManagedBuildManager.performValueHandlerEvent(rcCfg, 
					IManagedOptionValueHandler.EVENT_SETDEFAULT);
		}
	}
*/	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
		if ( element instanceof IProject) {
			performDefaults( (IProject)element);
		} else if ( element instanceof IFile) {
			performDefaults( (IFile)element);
		}
		defaultNeeded = true;
		return;
	}
	
	public void performDefaults(IProject project) {
		// TODO:  Should this reset all tools of the configuration, or just
		//        the currently selected tool category?  Right now it is all tools.
		
		
		// Get the build manager to reset build info for project
		ManagedBuildManager.resetConfiguration(parent.getProject(), parent.getSelectedConfigurationClone());
		ITool tools[] = parent.getSelectedConfigurationClone().getFilteredTools();
		for( int i = 0; i < tools.length; i++ ){
			if(!tools[i].getCustomBuildStep())
				tools[i].setToolCommand(null);
		}
		
		// Reset the category or tool selection and run selection event handler
		selectedCategory = null;
		selectedTool = null;
		handleOptionSelection();
		
		setDirty(true);
	}

	public void performDefaults(IFile file) {
		// TODO:  Should this reset all options of the tool in current resource configuration, or just
		//        the currently selected tool category?  Right now it is all options.
		
		// Get the build manager to reset build info for project
		ManagedBuildManager.resetResourceConfiguration(resParent.getProject(), resParent.getCurrentResourceConfigClone());
		ITool tools[] = resParent.getCurrentResourceConfigClone().getTools();
		for( int i = 0; i < tools.length; i++ ){
			if(!tools[i].getCustomBuildStep())
				tools[i].setToolCommand(null);
		}

		// Reset the category or tool selection and run selection event handler
		selectedCategory = null;
		selectedTool = null;
		handleOptionSelection();
		
		setDirty(true);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		
		if(element instanceof IFile)
			resParent.getCurrentResourceConfig(true);
		
		if(defaultNeeded){
			if(element instanceof IFile)
				ManagedBuildManager.resetResourceConfiguration(resParent.getProject(), resParent.getCurrentResourceConfig(true));
			else
				ManagedBuildManager.resetConfiguration(parent.getProject(), parent.getSelectedConfiguration());

//			performSetDefaultsEventCallBack();
			
			defaultNeeded = false;
		}
		//some options might be changed that do not belong to the created pages,
		//we need to save all options instead
		saveAll();
		
		setDirty(false);
	}
	
	private void saveAll(){
		if(resParent != null)
			saveResourceConfig();
		else
			saveConfig();
	}
	
	private void saveResourceConfig(){
		IResourceConfiguration cloneRcCfg = resParent.getCurrentResourceConfigClone();
		
		ITool tools[] = cloneRcCfg.getTools();
		
		for(int i = 0; i < tools.length; i++){
			saveHoldsOptions(tools[i]);
		}
	}
	
	private void saveHoldsOptions(IHoldsOptions holder){
		if(holder instanceof ITool && ((ITool)holder).getCustomBuildStep())
			return;
		AbstractBuildPropertyPage page = resParent != null ? 
				(AbstractBuildPropertyPage)resParent : (AbstractBuildPropertyPage)parent;
		
		IHoldsOptions realHo = page.getRealHoldsOptions(holder);
		
		if(realHo != null){
			if(holder instanceof ITool)
				((ITool)realHo).setToolCommand(((ITool)holder).getToolCommand());
			
			IOption options[] = holder.getOptions();
			for(int i = 0; i < options.length; i++){
				saveOption(options[i], holder);
			}
		}
	}
	private void saveOption(IOption clonedOption, IHoldsOptions cloneHolder){
		IConfiguration realCfg = null;
		IResourceConfiguration realRcCfg = null;
//		IBuildObject handler = null;
		IOption realOption;
		IHoldsOptions realHolder;
		
		if(resParent != null){ 
			realOption = resParent.getRealOption(clonedOption,cloneHolder);
			realHolder = resParent.getRealHoldsOptions(cloneHolder);
			realRcCfg = (IResourceConfiguration)((ITool)realHolder).getParent();
			realCfg = realRcCfg.getParent();
//			handler = realRcCfg;
		} else {
			realOption = parent.getRealOption(clonedOption,cloneHolder);
			realHolder = parent.getRealHoldsOptions(cloneHolder);
			realCfg = parent.getConfigurationFromHoldsOptions(realHolder);
//			handler = realCfg;
		}		
		
		try {
			// Transfer value from preference store to options
			IOption setOption = null;
			switch (clonedOption.getValueType()) {
				case IOption.BOOLEAN :
					boolean boolVal = clonedOption.getBooleanValue();
					if(realRcCfg != null) {
						setOption = ManagedBuildManager.setOption(realRcCfg, realHolder, realOption, boolVal);
					} else {
						setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption, boolVal);
					}
					// Reset the preference store since the Id may have changed
//					if (setOption != option) {
//						getToolSettingsPreferenceStore().setValue(setOption.getId(), boolVal);
//						FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
//						fe.setPreferenceName(setOption.getId());
//					}
					break;
				case IOption.ENUMERATED :
					String enumVal = clonedOption.getStringValue();
					String enumId = clonedOption.getEnumeratedId(enumVal);
					if(realRcCfg != null) {
						setOption = ManagedBuildManager.setOption(realRcCfg, realHolder, realOption, 
								(enumId != null && enumId.length() > 0) ? enumId : enumVal);
					} else {
						setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption, 
								(enumId != null && enumId.length() > 0) ? enumId : enumVal);
					}
					// Reset the preference store since the Id may have changed
//					if (setOption != option) {
//						getToolSettingsPreferenceStore().setValue(setOption.getId(), enumVal);
//						FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
//						fe.setPreferenceName(setOption.getId());
//					}
					break;
				case IOption.STRING :
					String strVal = clonedOption.getStringValue();
					if(realRcCfg != null){
						setOption = ManagedBuildManager.setOption(realRcCfg, realHolder, realOption, strVal);
					} else {
						setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption, strVal);	
					}
					
					// Reset the preference store since the Id may have changed
//					if (setOption != option) {
//						getToolSettingsPreferenceStore().setValue(setOption.getId(), strVal);
//						FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
//						fe.setPreferenceName(setOption.getId());
//					}
					break;
				case IOption.STRING_LIST :
				case IOption.INCLUDE_PATH :
				case IOption.PREPROCESSOR_SYMBOLS :
				case IOption.LIBRARIES :
				case IOption.OBJECTS :
					String[] listVal = (String[])((List)clonedOption.getValue()).toArray(new String[0]);
					if( realRcCfg != null){
						setOption = ManagedBuildManager.setOption(realRcCfg, realHolder, realOption, listVal);
					}else {
						setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption, listVal);	
					}
					
					// Reset the preference store since the Id may have changed
//					if (setOption != option) {
//						getToolSettingsPreferenceStore().setValue(setOption.getId(), listStr);
//						FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
//						fe.setPreferenceName(setOption.getId());
//					}
					break;
				default :
					break;
			}

			// Call an MBS CallBack function to inform that Settings related to Apply/OK button 
			// press have been applied.
			if (setOption == null)
				setOption = realOption;
			
/*			if (setOption.getValueHandler().handleValue(
					handler, 
					setOption.getOptionHolder(), 
					setOption,
					setOption.getValueHandlerExtraArgument(), 
					IManagedOptionValueHandler.EVENT_APPLY)) {
				// TODO : Event is handled successfully and returned true.
				// May need to do something here say log a message.
			} else {
				// Event handling Failed. 
			}
*/ 
		} catch (BuildException e) {
		} catch (ClassCastException e) {
		}

	}

	private void saveConfig(){
		IConfiguration cfg = parent.getSelectedConfigurationClone();
		
		IToolChain tc = cfg.getToolChain();
		saveHoldsOptions(tc);
		
		ITool tools[] = cfg.getFilteredTools();
		for(int i = 0; i < tools.length; i++){
			saveHoldsOptions(tools[i]);
		}
	}
	
	public boolean containsDefaults(){
		if(resParent == null)
			return false;
		return containsDefaults(resParent.getCurrentResourceConfigClone());
	}
	
	protected boolean containsDefaults(IResourceConfiguration rcCfg){
		IConfiguration cfg = rcCfg.getParent();
		ITool tools[] = rcCfg.getTools();
		for(int i = 0; i < tools.length; i++){
			ITool tool = tools[i];
			if(!tool.getCustomBuildStep()){
				IOption options[] = tool.getOptions();
				for( int j = 0; j < options.length; j++){
					IOption option = options[j];
					if(option.getParent() == tool){
						IOption ext = option;
						do{
							if(ext.isExtensionElement())
								break;
						} while((ext = ext.getSuperClass()) != null);

						if(ext != null){
							ITool cfgTool = cfg.getToolChain().getTool(tool.getSuperClass().getId());
							if(cfgTool != null){
								IOption defaultOpt = cfgTool.getOptionBySuperClassId(ext.getId());
								try {								
									if(defaultOpt != null && defaultOpt.getValueType() == option.getValueType()){
										Object value = option.getValue();
										Object defaultVal = defaultOpt.getValue();
										
										if(value.equals(defaultVal))
											continue;
										//TODO: check list also
									}
								}catch (BuildException e) {
								}
							}
						}
						return false;
					}
				}
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * Answers the list of settings pages for the selected configuration 
	 * @return 
	 */
	private List getPagesForConfig() {
		List pages = null;
		if ( element instanceof IProject) {
//			 Make sure that something was selected
			if (parent.getSelectedConfigurationClone() == null) {
				return null;
			}
			pages = (List) configToPageListMap.get(parent.getSelectedConfigurationClone().getId());	
		} else if (element instanceof IFile) {
			if ( resParent.getCurrentResourceConfigClone() == null ) {
				return null;
			}
			pages = (List) configToPageListMap.get(resParent.getCurrentResourceConfigClone().getId());
		}
		
		if (pages == null) {
			pages = new ArrayList();
			if ( element instanceof IProject) {
				configToPageListMap.put(parent.getSelectedConfigurationClone().getId(), pages);	
			} else if ( element instanceof IFile) {
				configToPageListMap.put(resParent.getCurrentResourceConfigClone().getId(), pages);
			}
		}
		return pages;
	}

	public BuildToolSettingsPreferenceStore getPreferenceStore() {
		return settingsStore;
	}

	/**
	 * Sets the "dirty" state
	 */
	public void setDirty(boolean b) {
		// Set each settings page
		List pages = getPagesForConfig();
		// Make sure we have something to work on
		if (pages == null) {
			// Nothing to do
			return;
		}
		ListIterator iter = pages.listIterator();
		while (iter.hasNext()) {
			BuildSettingsPage page = (BuildSettingsPage) iter.next();
			if (page == null) continue;
			page.setDirty(b);
		}
	}

	/**
	 * Returns the "dirty" state
	 */
	public boolean isDirty() {
		// Check each settings page
		List pages = getPagesForConfig();
		// Make sure we have something to work on
		if (pages == null) {
			// Nothing to do
			return false;
		}
		ListIterator iter = pages.listIterator();
		while (iter.hasNext()) {
			BuildSettingsPage page = (BuildSettingsPage) iter.next();
			if (page == null) continue;
			if (page.isDirty()) return true;
		}
		return false;
	}
	
	/**
	 * Returns the build macro provider to be used for macro resolution
	 * In case the "Build Macros" tab is available, returns the BuildMacroProvider
	 * supplied by that tab. 
	 * Unlike the default provider, that provider also contains
	 * the user-modified macros that are not applied yet
	 * If the "Build Macros" tab is not available, returns the default BuildMacroProvider
	 */
	public BuildMacroProvider obtainMacroProvider(){
		ICOptionContainer container = getContainer();
		ManagedBuildOptionBlock optionBlock = null;
		if(container instanceof BuildPropertyPage){
			BuildPropertyPage page = (BuildPropertyPage)container;
			optionBlock = page.getOptionBlock();
		} else if(container instanceof BuildPreferencePage){
			BuildPreferencePage page = (BuildPreferencePage)container;
			optionBlock = page.getOptionBlock();
		}
		if(optionBlock != null){
			MacrosSetBlock block = optionBlock.getMacrosBlock();
			if(block != null)
				return block.getBuildMacroProvider();
		}
		return (BuildMacroProvider)ManagedBuildManager.getBuildMacroProvider();
	}
	
	/**
	 * Creates a name that uniquely identifies a category. The match name is 
	 * a concatenation of the tool and categories, e.g. Tool->Cat1->Cat2 
	 * maps onto the string "Tool|Cat1|Cat2|"
	 * 
	 * @param category or tool for which to build the match name 
	 * @return match name
	 */
	private String makeMatchName(IBuildObject catOrTool) {
		String catName = EMPTY_STRING;
		
		// Build the match name.
		do {
			catName = catOrTool.getName() + "|" + catName; //$NON-NLS-1$
			if (catOrTool instanceof ITool) break;
			else if (catOrTool instanceof IOptionCategory) {
				catOrTool = ((IOptionCategory)catOrTool).getOwner();					
			} else 
				break;
		} while (catOrTool != null);
		
		return catName;
	}
	
	/**
	 * Finds an option category from an array of categories by comparing against
	 * a match name. The match name is a concatenation of the tool and categories, 
	 * e.g. Tool->Cat1->Cat2 maps onto the string "Tool|Cat1|Cat2|"
	 * 
	 * @param matchName an identifier to search 
	 * @param categories as returned by getChildCategories(), i.e. non-flattened 
	 * @return category or tool, if found and null otherwise
	 */
	private Object findOptionCategoryByMatchName(String matchName, IOptionCategory[] cats) {
		Object primary = null;
		
		for (int j=0; j<cats.length; j++) {
			IBuildObject catOrTool = cats[j]; 
			// Build the match name
			String catName = makeMatchName(catOrTool);
			// Check whether the name matches
			if (catName.equals(matchName)) {
				primary = cats[j];
				break;
			} else if (matchName.startsWith(catName)) {
				// If there is a common root then check for any further children
				primary = findOptionCategoryByMatchName(matchName, cats[j].getChildCategories());
				if (primary != null)
					break;
			}
		}
		return primary;
	}	
}

