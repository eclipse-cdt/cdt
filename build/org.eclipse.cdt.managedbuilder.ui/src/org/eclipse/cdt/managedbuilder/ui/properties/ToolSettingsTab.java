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
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.core.resources.IResource;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


public class ToolSettingsTab extends AbstractCBuildPropertyTab implements IPreferencePageContainer {
		/*
		 * String constants
		 */
		private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 10, 20 };
		
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
			sashForm.setLayout(layout);
			createSelectionArea(sashForm);
			createEditArea(sashForm);
			sashForm.setWeights(DEFAULT_SASH_WEIGHTS);
			sashForm.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					if (event.detail == SWT.DRAG) return;
					int shift = event.x - sashForm.getBounds().x;
					GridData data = (GridData) containerSC.getLayoutData();
					if ((data.widthHint + shift) < 20) return;
					Point computedSize = usercomp.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
					Point currentSize = usercomp.getShell().getSize();
					boolean customSize = !computedSize.equals(currentSize);
					data.widthHint = data.widthHint;
					sashForm.layout(true);
					computedSize = usercomp.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
					if (customSize)
						computedSize.x = Math.max(computedSize.x, currentSize.x);
					computedSize.y = Math.max(computedSize.y, currentSize.y);
					if (computedSize.equals(currentSize)) {
						return;
					}
				}
			});
			propertyObject = page.getElement();
			setValues();
		}
		
		protected void createSelectionArea (Composite parent) {
			optionList = new TreeViewer(parent, SWT.SINGLE|SWT.H_SCROLL|SWT.V_SCROLL|SWT.BORDER);
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
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(IProgressMonitor)
		 */
		private void copyHoldsOptions(IHoldsOptions src, IHoldsOptions dst, IResourceInfo res){
			if(src instanceof ITool) {
				ITool t1 = (ITool)src;
				ITool t2 = (ITool)dst;
				if (t1.getCustomBuildStep()) return; 
				t2.setToolCommand(t1.getToolCommand());
				t2.setCommandLinePattern(t1.getCommandLinePattern());
			}
			IOption op1[] = src.getOptions();
			IOption op2[] = dst.getOptions();
			for(int i = 0; i < op1.length; i++) {
				setOption(op1[i], op2[i], dst, res);
			}
		}

		private void setOption(IOption op1, IOption op2, IHoldsOptions dst, IResourceInfo res){
			try {
				switch (op1.getValueType()) {
					case IOption.BOOLEAN :
						boolean boolVal = op1.getBooleanValue();
						ManagedBuildManager.setOption(res, dst, op2, boolVal);
						break;
					case IOption.ENUMERATED :
						String enumVal = op1.getStringValue();
						String enumId = op1.getEnumeratedId(enumVal);
						String out = (enumId != null && enumId.length() > 0) ? enumId : enumVal;
						ManagedBuildManager.setOption(res, dst, op2, out);
						break;
					case IOption.STRING :
						ManagedBuildManager.setOption(res, dst, op2, op1.getStringValue());
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
					case IOption.UNDEF_INCLUDE_PATH:
					case IOption.UNDEF_PREPROCESSOR_SYMBOLS:
					case IOption.UNDEF_INCLUDE_FILES:
					case IOption.UNDEF_LIBRARY_PATHS:
					case IOption.UNDEF_LIBRARY_FILES:
					case IOption.UNDEF_MACRO_FILES:
						String[] data = (String[])((List)op1.getValue()).toArray(new String[0]);
						ManagedBuildManager.setOption(res, dst, op2, data);
						break;
					default :
						break;
				}
			} catch (BuildException e) {
			} catch (ClassCastException e) {
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

	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		IResourceInfo ri1 = getResCfg(src);
		IResourceInfo ri2 = getResCfg(dst);
		copyHoldsOptions(ri1.getParent().getToolChain(), ri2.getParent().getToolChain(), ri2);
		ITool[] t1, t2;
		if (ri1 instanceof IFolderInfo){
			t1 = ((IFolderInfo)ri1).getFilteredTools();
			t2 = ((IFolderInfo)ri2).getFilteredTools();
		} else if (ri1 instanceof IFileInfo) {
			t1 = ((IFileInfo)ri1).getToolsToInvoke();
			t2 = ((IFileInfo)ri2).getToolsToInvoke();
		} else return;
		if (t1.length != t2.length) return; // not our case
		for (int i=0; i<t1.length; i++)
			copyHoldsOptions(t1[i], t2[i], ri2);
		setDirty(false);
	}

	// IPreferencePageContainer methods
	public void updateButtons() {}
	public void updateMessage() {}
	public void updateTitle() {}

	public boolean canBeVisible() {
		return getCfg().getBuilder().isManagedBuildOn();
	}
}
