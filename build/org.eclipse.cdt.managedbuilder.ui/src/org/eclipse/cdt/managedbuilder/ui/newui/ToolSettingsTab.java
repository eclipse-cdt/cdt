/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.newui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class ToolSettingsTab extends AbstractCBuildPropertyTab implements IPreferencePageContainer {
		/*
		 * String constants
		 */
		//private static final String PREFIX = "ToolsSettingsBlock";	//$NON-NLS-1$
		//private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
		//private static final String SETTINGS_LABEL = LABEL + ".Settings";	//$NON-NLS-1$
		//private static final String TREE_LABEL = LABEL + ".ToolTree";	//$NON-NLS-1$
		//private static final String OPTIONS_LABEL = LABEL + ".ToolOptions";	//$NON-NLS-1$
		private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 20, 30 };
		
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
		private IPreferenceStore settingsStore;
		private AbstractToolSettingUI currentSettingsPage;
		private ToolListElement selectedElement;
		private ToolListContentProvider listprovider;
		private Object propertyObject;
		
		private boolean defaultNeeded;
		
		private IResourceInfo fInfo;
		
		
		public void createControls(Composite par)  {
			super.createControls(par);
			usercomp.setLayout(new GridLayout());

			configToPageListMap = new HashMap();
			settingsStore = ToolSettingsPrefStore.getDefault();
			
			// Create the sash form
			sashForm = new SashForm(usercomp, SWT.NONE);
			sashForm.setOrientation(SWT.HORIZONTAL);
			sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginHeight = 5;
			layout.marginWidth = 5;
			sashForm.setLayout(layout);
			createSelectionArea(sashForm);
			createEditArea(sashForm);
			sashForm.setWeights(DEFAULT_SASH_WEIGHTS);

			propertyObject = page.getElement();
			setValues();
//			WorkbenchHelp.setHelp(composite, ManagedBuilderHelpContextIds.MAN_PROJ_BUILD_PROP);
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
		private void displayOptionsForCategory(ToolListElement toolListElement) {
			
			selectedElement = toolListElement;
			IOptionCategory category = toolListElement.getOptionCategory();
			IHoldsOptions optionHolder = toolListElement.getHoldOptions();

			AbstractToolSettingUI oldPage = currentSettingsPage;
			currentSettingsPage = null;

			// Create a new settings page if necessary
			List pages = getPagesForConfig();
			ListIterator iter = pages.listIterator();
			
			while (iter.hasNext()) {
				AbstractToolSettingUI page = (AbstractToolSettingUI) iter.next();
				if (page.isFor(optionHolder, category)) {
					currentSettingsPage = page;
					break;
				}
			}
			if (currentSettingsPage == null) {
				currentSettingsPage = new BuildOptionSettingsUI(this, fInfo, optionHolder, category);
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
		 * Method displayOptionsForTool
		 * @param tool
		 */
		private void displayOptionsForTool(ToolListElement toolListElement) {
			selectedElement = toolListElement;
			ITool tool = toolListElement.getTool();
			
			// Cache the current build setting page
			AbstractToolSettingUI oldPage = currentSettingsPage;
			currentSettingsPage = null;

			// Create a new page if we need one
			List pages = getPagesForConfig();
			ListIterator iter = pages.listIterator();
			while (iter.hasNext()) {
				AbstractToolSettingUI page = (AbstractToolSettingUI) iter.next();
				if (page.isFor(tool, null)) {
					currentSettingsPage = page;
					break;
				}
			}
			
			if (currentSettingsPage == null) {
				currentSettingsPage = new BuildToolSettingUI(this, fInfo, tool);
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

			// Save the last page build options.
			if (oldPage != null && oldPage != currentSettingsPage){
				oldPage.storeSettings();
			}
			currentSettingsPage.setValues();

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

		public void setVisible(boolean visible){
			if(visible){
				selectedElement = null;
				handleOptionSelection();
			}
			super.setVisible(visible);
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

			//  Create the Tree Viewer content provider if first time
			if (listprovider == null) {
				IResource resource = (IResource) propertyObject;
				listprovider = new ToolListContentProvider(resource.getType());
				optionList.setContentProvider(listprovider);
			}

			//  Update the selected configuration and the Tree Viewer
			ToolListElement[] newElements;
			
			optionList.setInput(fInfo);	
			newElements = (ToolListElement[])listprovider.getElements(fInfo);
			optionList.expandAll();
			
			//  Determine what the selection in the tree should be
			//  If the saved selection is not null, try to match the saved selection
			//  with an object in the new element list.
			//  Otherwise, select the first tool in the tree
			Object primaryObject = null;
			if (selectedElement != null) {
				selectedElement = matchSelectionElement(selectedElement, newElements);
			}
				
			if (selectedElement == null) {
				selectedElement = (ToolListElement)(newElements != null && newElements.length > 0 ? newElements[0] : null);
			}
				
			if (selectedElement != null) {
				primaryObject = selectedElement.getTool();
				if (primaryObject == null) {
					primaryObject = selectedElement.getOptionCategory();
				}
				if (primaryObject != null) {
					if (primaryObject instanceof IOptionCategory) {
						((ToolSettingsPrefStore)settingsStore).setSelection(getResDesc(), selectedElement, (IOptionCategory)primaryObject);
					}
					optionList.setSelection(new StructuredSelection(selectedElement), true);
				}
			}
		}
							
		private ToolListElement matchSelectionElement(ToolListElement currentElement, ToolListElement[] elements) {
			//  First, look for an exact match
			ToolListElement match = exactMatchSelectionElement(currentElement, elements);
			if (match == null)
				//  Else, look for the same tool/category in the new set of elements
				match = equivalentMatchSelectionElement(currentElement, elements);
			return match;
		}

		private ToolListElement exactMatchSelectionElement(ToolListElement currentElement, ToolListElement[] elements) {
			for (int i=0; i<elements.length; i++) {
				ToolListElement e = elements[i];
				if (e == currentElement) {
					return currentElement;
				}
				e = exactMatchSelectionElement(currentElement, e.getChildElements());
				if (e != null) return e;
			}
			return null;
		}

		private ToolListElement equivalentMatchSelectionElement(ToolListElement currentElement, ToolListElement[] elements) {
			for (int i=0; i<elements.length; i++) {
				ToolListElement e = elements[i];
				if (e.isEquivalentTo(currentElement)) {
					return e;
				}
				e = equivalentMatchSelectionElement(currentElement, e.getChildElements());
				if (e != null) return e;
			}
			return null;
		}
		
		public void removeValues(String id) {
		}

		private void handleOptionSelection() {
			// Get the selection from the tree list
			if (optionList == null) return;
			IStructuredSelection selection = (IStructuredSelection) optionList.getSelection();
		
			// Set the option page based on the selection
			ToolListElement toolListElement = (ToolListElement)selection.getFirstElement();
			if (toolListElement != null) {
				IOptionCategory cat = toolListElement.getOptionCategory();
				if (cat == null)
					cat = (IOptionCategory)toolListElement.getTool();
				if (cat != null) 
					((ToolSettingsPrefStore)settingsStore).setSelection(getResDesc(), toolListElement, cat);

				cat = toolListElement.getOptionCategory();
				if (cat != null) {
					displayOptionsForCategory(toolListElement);
				} else {
					displayOptionsForTool(toolListElement);
				}
			}
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
		 */
		public void performDefaults() {
			if (page.isForProject()) {
				ManagedBuildManager.resetConfiguration(page.getProject(), getCfg());
			} else {
//				ManagedBuildManager.resetResourceConfiguration(provider.getProject(), );
//				ManagedBuildManager.performValueHandlerEvent(fInfo, IManagedOptionValueHandler.EVENT_SETDEFAULT);

			}
			ITool tools[];
			if (page.isForProject()) 
				tools = getCfg().getFilteredTools();
			else
				tools = getResCfg(getResDesc()).getTools();
			for( int i = 0; i < tools.length; i++ ){
				if(!tools[i].getCustomBuildStep()) {
					tools[i].setToolCommand(null);
					tools[i].setCommandLinePattern(null);
				}
			}
			// Reset the category or tool selection and run selection event handler
			selectedElement = null;
			handleOptionSelection();
			setDirty(true);
			defaultNeeded = true;
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(IProgressMonitor)
		 */
		public void performApply(IProgressMonitor monitor) throws CoreException {
			if(defaultNeeded){
				if(propertyObject instanceof IFile)
					ManagedBuildManager.resetResourceConfiguration(page.getProject(), getCfg().getResourceConfiguration(((IFile)propertyObject).getFullPath().toOSString()));
				else
					ManagedBuildManager.resetConfiguration(page.getProject(), getCfg());
				defaultNeeded = false;
			}
			saveConfig();
			setDirty(false);
		}
		
		private void saveHoldsOptions(IHoldsOptions holder){
			if(holder instanceof ITool && ((ITool)holder).getCustomBuildStep())
				return;
			if(holder instanceof ITool) {
				String currentValue = ((ITool)holder).getToolCommand();
				if (!(currentValue.equals(((ITool)holder).getToolCommand()))) {
					((ITool)holder).setToolCommand(((ITool)holder).getToolCommand());
						fInfo.setRebuildState(true);
				}
				currentValue = ((ITool)holder).getCommandLinePattern();
				if (!(currentValue.equals(((ITool)holder).getCommandLinePattern()))) {
					((ITool)holder).setCommandLinePattern(((ITool)holder).getCommandLinePattern());
					fInfo.setRebuildState(true);
				}
			}
			IOption options[] = holder.getOptions();
			for(int i = 0; i < options.length; i++) {
				saveOption(options[i], holder);
			}
		}

		private void saveOption(IOption option, IHoldsOptions holder){
//			IResourceInfo info = fCfg;
			try {
				IOption setOption = null;
				// Transfer value from preference store to options
				switch (option.getValueType()) {
					case IOption.BOOLEAN :
						boolean boolVal = option.getBooleanValue();
						setOption = ManagedBuildManager.setOption(fInfo, holder, option, boolVal);
						break;
					case IOption.ENUMERATED :
						String enumVal = option.getStringValue();
						String enumId = option.getEnumeratedId(enumVal);
						String out = (enumId != null && enumId.length() > 0) ? enumId : enumVal;
						setOption = ManagedBuildManager.setOption(fInfo, holder, option, out);
						break;
					case IOption.STRING :
						setOption = ManagedBuildManager.setOption(fInfo, holder, option, option.getStringValue());
						break;
					case IOption.STRING_LIST :
					case IOption.INCLUDE_PATH :
					case IOption.PREPROCESSOR_SYMBOLS :
					case IOption.LIBRARIES :
					case IOption.OBJECTS :
					case IOption.INCLUDE_FILES:
					case IOption.LIBRARY_PATHS:
					case IOption.LIBRARY_FILES:
					case IOption.MACRO_FILES:
						String[] data = (String[])((List)option.getValue()).toArray(new String[0]);
						setOption = ManagedBuildManager.setOption(fInfo, holder, option, data);
						break;
					default :
						break;
				}

				// Call an MBS CallBack function to inform that Settings related to Apply/OK button 
				// press have been applied.
				if (setOption == null) setOption = option;
				
//				if (setOption.getValueHandler().handleValue(
//						handler, 
//						setOption.getOptionHolder(), 
//						setOption,
//						setOption.getValueHandlerExtraArgument(), 
//						IManagedOptionValueHandler.EVENT_APPLY)) {
//					// TODO : Event is handled successfully and returned true.
//					// May need to do something here say log a message.
//				} else {
//					// Event handling Failed. 
//				}
//	
			} catch (BuildException e) {
			} catch (ClassCastException e) {
			}

		}

		private void saveConfig(){
			IToolChain tc = fInfo.getParent().getToolChain();
			saveHoldsOptions(tc);
			
			ITool tools[] = fInfo.getParent().getFilteredTools();
			for(int i = 0; i < tools.length; i++){
				saveHoldsOptions(tools[i]);
			}
		}
		
		protected boolean containsDefaults(){
			IConfiguration parentCfg = fInfo.getParent().getParent();
			ITool tools[] = fInfo.getParent().getTools();
			for(int i = 0; i < tools.length; i++){
				ITool tool = tools[i];
				if(!tool.getCustomBuildStep()){
					ITool cfgTool = parentCfg.getToolChain().getTool(tool.getSuperClass().getId());
					//  Check for a non-default command or command-line-pattern
					if(cfgTool != null){
						if (!(tool.getToolCommand().equals(cfgTool.getToolCommand()))) return false;
						if (!(tool.getCommandLinePattern().equals(cfgTool.getCommandLinePattern()))) return false;
					}
					//  Check for a non-default option
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
		 */
		private List getPagesForConfig() {
			if (getCfg() == null) return null;
			List pages = (List) configToPageListMap.get(getCfg().getId());
			if (pages == null) {
				pages = new ArrayList();
				configToPageListMap.put(getCfg().getId(), pages);	
			}
			return pages;
		}

		public IPreferenceStore getPreferenceStore() {
			return settingsStore;
		}

		/**
		 * Sets the "dirty" state
		 */
		public void setDirty(boolean b) {
			List pages = getPagesForConfig();
			if (pages == null) return;
			ListIterator iter = pages.listIterator();
			while (iter.hasNext()) {
				AbstractToolSettingUI page = (AbstractToolSettingUI) iter.next();
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
				AbstractToolSettingUI page = (AbstractToolSettingUI) iter.next();
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
			return (BuildMacroProvider)ManagedBuildManager.getBuildMacroProvider();
		}
	
	public void updateData(ICResourceDescription cfgd) {
		handleOptionSelection();
		fInfo = getResCfg(cfgd);
		setValues();
	}

	public void performApply(ICResourceDescription src, ICResourceDescription dst) {
		
	}

	// IPreferencePageContainer methods
	public void updateButtons() {}
	public void updateMessage() {}
	public void updateTitle() {}

	public boolean canBeVisible() {
		return getCfg().getBuilder().isManagedBuildOn();
	}
}
