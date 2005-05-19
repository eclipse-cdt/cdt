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
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.envvar.DefaultContextInfo;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildPropertyPage;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
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
 * When used in the BuildPropertyPage the displays the tab-folder that contains the following tabs:
 * 1. a tab containing configuration-specific variables
 * 2. a tab containing project-specific variables
 * 
 * Otherwise displays a single EnvironmentBlock that contains
 * the workspace-specific and eclipse process environment variables
 *
 */
public class EnvironmentSetBlock extends AbstractCOptionPage {
	/*
	 * String constants
	 */
	private static final String PREFIX = "EnvironmentSetBlock";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String ENVIRONMENT_LABEL = LABEL + ".environment";	//$NON-NLS-1$

	private static final String ENVIRONMENT_GROUP_LABEL = LABEL + ".environment.group";	//$NON-NLS-1$

	private static final String TAB = LABEL + ".tab";	//$NON-NLS-1$
	private static final String TAB_CONFIGURATION = TAB + ".configuration";	//$NON-NLS-1$
	private static final String TAB_PROJECT = TAB + ".project";	//$NON-NLS-1$
	private static final String TAB_WORKSPACE = TAB + ".workspace";	//$NON-NLS-1$
	private static final String TAB_ECLIPSE = TAB + ".eclipse";	//$NON-NLS-1$

	private EnvironmentTabFolder fEnvTabs;
	private EnvironmentBlock fEnvBlock;
	
	private ICOptionContainer fParentContainer;

	private UIEnvVarProvider fEnvProvider = null;
	
	private class UIEnvVarContextInfo extends DefaultContextInfo{
		public UIEnvVarContextInfo(Object context){
			super(context);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getNext()
		 */
		public IContextInfo getNext(){
			IContextInfo info = super.getNext();
			if(info != null){
				EnvironmentBlock blocks[] = getAllBlocks();
				for(int i = 0; i < blocks.length; i++){
					if(blocks[i].getContext() == info.getContext())
						return blocks[i].getContextInfo();
				}
				return new UIEnvVarContextInfo(info.getContext());
			}
			return null;
		}
	}

	/*
	 * The EnvironmentVariableProvider to be used in UI
	 * Unlike the default provider, this provider also contains
	 * the user-modified variables that are not applied yet
	 */
	private class UIEnvVarProvider extends EnvironmentVariableProvider{
		protected IContextInfo getContextInfo(Object context){
			EnvironmentBlock blocks[] = getAllBlocks();
			for(int i = 0; i < blocks.length; i++){
				if(blocks[i].getContext() == context)
					return blocks[i].getContextInfo();
			}
			return new UIEnvVarContextInfo(context);
		}
	}

	private class EnvironmentTabFolder extends TabFolderOptionBlock{
		private EnvironmentBlock fFolderTabs[];
		public EnvironmentTabFolder() {
			super(fParentContainer, false);
		}
		
		public EnvironmentBlock[] getTabs(){
			return fFolderTabs;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock#addTabs()
		 */
		protected void addTabs(){
			if(fParentContainer instanceof BuildPropertyPage) {
				// the EnvironmentSetBlock is used whithing the Build Property Page
				// create the project and configuration tabs
				fFolderTabs = new EnvironmentBlock[2];
				addTab(fFolderTabs[0] = new EnvironmentBlock(fParentContainer,
						ManagedBuilderUIMessages.getResourceString(TAB_CONFIGURATION),
						true,
						true));
				addTab(fFolderTabs[1] = new EnvironmentBlock(fParentContainer,
						ManagedBuilderUIMessages.getResourceString(TAB_PROJECT),
						true,
						true));
			}
			else {
				// the EnvironmentSetBlock is used whithing the Build Preference Page
				// create the workspace and eclipse environment tabs
				fFolderTabs = new EnvironmentBlock[2];
				addTab(fFolderTabs[0] = new EnvironmentBlock(fParentContainer,
						ManagedBuilderUIMessages.getResourceString(TAB_WORKSPACE),
						true,
						true));
				addTab(fFolderTabs[1] = new EnvironmentBlock(fParentContainer,
						ManagedBuilderUIMessages.getResourceString(TAB_ECLIPSE),
						false,
						false));
			}
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
					fFolderTabs[1].setContext(page.getSelectedConfiguration().getManagedProject());

				fFolderTabs[0].setContext(page.getSelectedConfiguration());
				fFolderTabs[0].setParentContextInfo(fFolderTabs[1].getContextInfo());
			}
			else {
				fFolderTabs[1].setContext(null);
				
				fFolderTabs[0].setContext(ResourcesPlugin.getWorkspace());
				fFolderTabs[0].setParentContextInfo(fFolderTabs[1].getContextInfo());
			}
		}
		
	}

	
	public EnvironmentSetBlock(ICOptionContainer parent){
		super(ManagedBuilderUIMessages.getResourceString(ENVIRONMENT_LABEL));
		super.setContainer(parent);
		fParentContainer = parent;
		
		if(fParentContainer instanceof BuildPropertyPage)
			fEnvTabs = new EnvironmentTabFolder();
		else {
			fEnvBlock = new EnvironmentBlock(fParentContainer,
					ManagedBuilderUIMessages.getResourceString(TAB_WORKSPACE),
					true,
					false);
			fEnvBlock.displayParentVariables(true);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		EnvironmentBlock tabs[] = getAllBlocks();
		if(tabs != null){
			for(int i = 0; i < tabs.length; i++)
				tabs[i].performApply(monitor);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
		EnvironmentBlock tab = getSelectedBlock();
		if(tab != null)
			tab.performDefaults();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible){
		if(visible)
			updateValues();
		super.setVisible(visible);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Control ctrl = null;
		if(fEnvTabs != null){
			Group group = new Group(parent, SWT.NONE);
			group.setFont(parent.getFont());
			group.setText(ManagedBuilderUIMessages.getResourceString(ENVIRONMENT_GROUP_LABEL));
			group.setLayoutData(new GridData(GridData.FILL_BOTH));
			GridLayout gl = new GridLayout();
			gl.marginHeight = 0;
			gl.marginWidth = 0;
			group.setLayout(gl);
			Control tabs = fEnvTabs.createContents(group);
			GridData gd = new GridData(GridData.FILL_BOTH);
			tabs.setLayoutData(gd);
			ctrl = group;
		}
		else if(fEnvBlock != null){
			fEnvBlock.createControl(parent);
			ctrl = fEnvBlock.getControl();
			ctrl.setLayoutData(new GridData(GridData.FILL_BOTH));
		}

		setControl(ctrl);
	}
	
	public void updateValues(){
		EnvironmentBlock tab = getSelectedBlock();

		updateContexts();
		if(tab != null)
			tab.updateValues();
	}
	
	public boolean isConfigSelectionAllowed(){
		EnvironmentBlock block = getSelectedBlock();
		if(block != null)
			return block.getContext() instanceof IConfiguration;
		return false;
	}
	
	public boolean isModified(){
		EnvironmentBlock tabs[] = getAllBlocks();
		for(int i = 0; i < tabs.length; i++){
			if(tabs[i].isModified())
				return true;
		}
		return false;
	}
	
	public void setModified(boolean modified){
		EnvironmentBlock tabs[] = getAllBlocks();
		for(int i = 0; i < tabs.length; i++){
			tabs[i].setModified(modified);
		}
	}
	
	/*
	 * returns the selected environment block
	 */
	protected EnvironmentBlock getSelectedBlock(){
		if(fEnvTabs != null)
			return (EnvironmentBlock)fEnvTabs.getCurrentPage();
		return fEnvBlock;
	}
	
	/*
	 * returns all available environment blocks
	 */
	protected EnvironmentBlock[] getAllBlocks(){
		if(fEnvTabs != null)
			return fEnvTabs.getTabs();
		else if(fEnvBlock != null)
			return new EnvironmentBlock[]{fEnvBlock};
		return new EnvironmentBlock[0];
	}
	
	/*
	 * updates the context of each EnvironmentBlock
	 */
	protected void updateContexts(){
		if(fEnvTabs != null)
			fEnvTabs.updateContexts();
		else if(fEnvBlock != null)
			fEnvBlock.setContext(ResourcesPlugin.getWorkspace());
	}
	
	/*
	 * returns the EnvironmentVariableProvider to be used in UI
	 * Unlike the default provider, the returned provider also contains
	 * the user-modified variables that are not applied yet
	 */
	public IEnvironmentVariableProvider getEnvironmentVariableProvider(){
		if(fEnvProvider == null)
			fEnvProvider = new UIEnvVarProvider();
		return fEnvProvider;
	}
	
}
