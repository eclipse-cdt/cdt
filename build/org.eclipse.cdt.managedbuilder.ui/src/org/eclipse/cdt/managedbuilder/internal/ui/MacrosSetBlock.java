/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildPropertyPage;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * Whe used in the BuildPropertyPage the displays the tab-folder that contains the following tabs:
 * 1. a tab containing configuration-specific macros
 * 2. a tab containing project-specific macros
 * 
 * Otherwise displays a single MacrosBlock that contains
 * the workspace-specific, CDT/Eclipse installation and eclipse process environment macros
 *
 */public class MacrosSetBlock extends AbstractCOptionPage {
	/*
	 * String constants
	 */
	private static final String PREFIX = "MacrosSetBlock";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String MACROS_LABEL = LABEL + ".macros";	//$NON-NLS-1$

	private static final String MACROS_GROUP_LABEL = LABEL + ".macros.group";	//$NON-NLS-1$

	private static final String TAB = LABEL + ".tab";	//$NON-NLS-1$
	private static final String TAB_CONFIGURATION = TAB + ".configuration";	//$NON-NLS-1$
	private static final String TAB_PROJECT = TAB + ".project";	//$NON-NLS-1$
	private static final String TAB_WORKSPACE = TAB + ".workspace";	//$NON-NLS-1$
	private static final String TAB_ECLIPSE = TAB + ".eclipse";	//$NON-NLS-1$

	private MacrosTabFolder fMacroTabs;
	private MacrosBlock fMacroBlock;
	
	private ICOptionContainer fParentContainer;
	
	private UIMacroProvider fMacroProvider;
	
	private class UIMacroContextInfo extends DefaultMacroContextInfo{
		public UIMacroContextInfo(int type, Object data){
			super(type,data);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getNext()
		 */
		public IMacroContextInfo getNext(){
			IMacroContextInfo info = super.getNext();
			if(info != null){
				MacrosBlock blocks[] = getAllBlocks();
				for(int i = 0; i < blocks.length; i++){
					if(blocks[i].getContextType() == info.getContextType() &&
							blocks[i].getContextData() == info.getContextData())
						return blocks[i].getContextInfo();
				}
				return new UIMacroContextInfo(info.getContextType(),info.getContextData());
			}
			return null;
		}
	}
	
	/*
	 * The BuildMacroProvider to be used in UI
	 * Unlike the default provider, this provider also contains
	 * the user-modified macros that are not applied yet
	 */
	private class UIMacroProvider extends BuildMacroProvider {
		public IMacroContextInfo getMacroContextInfo(int contextType, Object contextData){
			MacrosBlock blocks[] = getAllBlocks();
			for(int i = 0; i < blocks.length; i++){
				if(blocks[i].getContextType() == contextType &&
						blocks[i].getContextData() == contextData)
					return blocks[i].getContextInfo();
			}
			return new UIMacroContextInfo(contextType,contextData);
		}
	}

	private class MacrosTabFolder extends TabFolderOptionBlock{
		private MacrosBlock fFolderTabs[];
		public MacrosTabFolder() {
			super(fParentContainer, false);
		}
		
		public MacrosBlock[] getTabs(){
			return fFolderTabs;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock#addTabs()
		 */
		protected void addTabs(){
			if(fParentContainer instanceof BuildPropertyPage) {
				// the EnvironmentSetBlock is used whithing the Build Property Page
				// create the project and configuration tabs
				fFolderTabs = new MacrosBlock[2];
				addTab(fFolderTabs[0] = new MacrosBlock(fParentContainer,
						ManagedBuilderUIMessages.getResourceString(TAB_CONFIGURATION),
						true,
						true));
				addTab(fFolderTabs[1] = new MacrosBlock(fParentContainer,
						ManagedBuilderUIMessages.getResourceString(TAB_PROJECT),
						true,
						true));
			}
/*			else {
				// the EnvironmentSetBlock is used whithing the Build Preference Page
				// create the workspace and eclipse environment tabs
				fFolderTabs = new MacrosBlock[2];
				addTab(fFolderTabs[0] = new MacrosBlock(fParentContainer,
						ManagedBuilderUIMessages.getResourceString(TAB_WORKSPACE),
						true,
						true));
				addTab(fFolderTabs[1] = new MacrosBlock(fParentContainer,
						ManagedBuilderUIMessages.getResourceString(TAB_ECLIPSE),
						false,
						false));
			}
*/
		}
		
		/*
		 * set the appropriate context data to the tabs
		 */
		public void updateContexts(){
			if(fFolderTabs == null)
				return;

			if(fParentContainer instanceof BuildPropertyPage){
				BuildPropertyPage page = (BuildPropertyPage)fParentContainer;
				if(page.getSelectedConfiguration() != null)
					fFolderTabs[1].setContext(IBuildMacroProvider.CONTEXT_PROJECT,page.getSelectedConfiguration().getManagedProject());

				fFolderTabs[0].setContext(IBuildMacroProvider.CONTEXT_CONFIGURATION,page.getSelectedConfiguration());
				fFolderTabs[0].setParentContextInfo(fFolderTabs[1].getContextInfo());
			}
/*			else {
				fFolderTabs[1].setContext(null);
				
				fFolderTabs[0].setContext(ResourcesPlugin.getWorkspace());
				fFolderTabs[0].setParentContextInfo(fFolderTabs[1].getContextInfo());
			}
*/
		}
		
		public void setCurrentPage(ICOptionPage page) {
			((MacrosBlock)page).updateValues();
			super.setCurrentPage(page);
		}
		
	}

	
	public MacrosSetBlock(ICOptionContainer parent){
		super(ManagedBuilderUIMessages.getResourceString(MACROS_LABEL));
		super.setContainer(parent);
		fParentContainer = parent;
//		fOptionBlock = optionBlock;
		
		if(fParentContainer instanceof BuildPropertyPage)
			fMacroTabs = new MacrosTabFolder();
		else {
			fMacroBlock = new MacrosBlock(fParentContainer,
					ManagedBuilderUIMessages.getResourceString(TAB_WORKSPACE),
					true,
					false);
			fMacroBlock.displayParentMacros(true);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		MacrosBlock tabs[] = getAllBlocks();
		if(tabs != null){
			for(int i = 0; i < tabs.length; i++)
				tabs[i].performApply(monitor);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
		MacrosBlock tab = getSelectedBlock();
		if(tab != null)
			tab.performDefaults();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible){
		if(visible)
			updateValues();
		if(fMacroTabs != null)
			fMacroTabs.setVisible(visible);
		super.setVisible(visible);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Control ctrl = null;
		if(fMacroTabs != null){
			Group group = new Group(parent, SWT.NONE);
			group.setFont(parent.getFont());
			group.setText(ManagedBuilderUIMessages.getResourceString(MACROS_GROUP_LABEL));
			group.setLayoutData(new GridData(GridData.FILL_BOTH));
			GridLayout gl = new GridLayout();
			gl.marginHeight = 0;
			gl.marginWidth = 0;
			group.setLayout(gl);
			Control tabs = fMacroTabs.createContents(group);
			GridData gd = new GridData(GridData.FILL_BOTH);
			tabs.setLayoutData(gd);
			ctrl = group;
		}
		else if(fMacroBlock != null){
			fMacroBlock.createControl(parent);
			ctrl = fMacroBlock.getControl();
			ctrl.setLayoutData(new GridData(GridData.FILL_BOTH));
		}

		setControl(ctrl);
	}
	
	public void updateValues(){
		MacrosBlock tab = getSelectedBlock();

		updateContexts();
		if(tab != null)
			tab.updateValues();
	}
	
	public boolean isConfigSelectionAllowed(){
		MacrosBlock block = getSelectedBlock();
		if(block != null)
			return block.getContextData() instanceof IConfiguration;
		return false;
	}
	
	public boolean isModified(){
		MacrosBlock tabs[] = getAllBlocks();
		for(int i = 0; i < tabs.length; i++){
			if(tabs[i].isModified())
				return true;
		}
		return false;
	}
	
	public void setModified(boolean modified){
		MacrosBlock tabs[] = getAllBlocks();
		for(int i = 0; i < tabs.length; i++){
			tabs[i].setModified(modified);
		}
	}
	
	/*
	 * returns the selected environment block
	 */
	protected MacrosBlock getSelectedBlock(){
		if(fMacroTabs != null)
			return (MacrosBlock)fMacroTabs.getCurrentPage();
		return fMacroBlock;
	}
	
	/*
	 * returns all available environment blocks
	 */
	protected MacrosBlock[] getAllBlocks(){
		if(fMacroTabs != null)
			return fMacroTabs.getTabs();
		else if(fMacroBlock != null)
			return new MacrosBlock[]{fMacroBlock};
		return new MacrosBlock[0];
	}
	
	/*
	 * updates the context of each EnvironmentBlock
	 */
	protected void updateContexts(){
		if(fMacroTabs != null)
			fMacroTabs.updateContexts();
		else if(fMacroBlock != null)
			fMacroBlock.setContext(IBuildMacroProvider.CONTEXT_WORKSPACE,ResourcesPlugin.getWorkspace());
	}
	
	/*
	 * returns the BuildMacroProvider to be used in UI
	 * Unlike the default provider, the returned provider also contains
	 * the user-modified macros that are not applied yet
	 */
	public BuildMacroProvider getBuildMacroProvider(){
		if(fMacroProvider == null){
			updateContexts();
			fMacroProvider = new UIMacroProvider();
		}
		return fMacroProvider;
	}
	
}
