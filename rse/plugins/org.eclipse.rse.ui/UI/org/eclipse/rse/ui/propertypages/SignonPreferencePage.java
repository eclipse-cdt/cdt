/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.propertypages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.rse.core.PasswordPersistenceManager;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.SystemSignonInformation;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.dialogs.SystemPasswordPersistancePrompt;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * Remote systems preference page which allows users to manage (add / change /
 * remove) their saved passwords.
 */
public final class SignonPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, 
																			  Listener{

	
	// SWT Widgets and content providers
	private Table pwdTable;
	private TableViewer pwdTableViewer;
	private PasswordContentProvider provider;
	private Button addButton, changeButton, removeButton;
	
	// List of information for table
	private List passwords;
	
	// List to keep track of additions / deletions / changes.  We need to 
	// keep track of these until the user decides whether to cancel the preference
	// page (and we forget about the changes) or press ok (and we commit the changes)
	private List modifications;

	/**
	 * Inner class to keep track of password modifications (without committing them
	 * to the keyring) while the user modifies the preferences.     
	 */	 
	protected class PasswordModification
	{
		protected static final int ADD = 1;
		protected static final int DELETE = 2;
		
		protected int changeFlag; 	
		protected SystemSignonInformation info;
		
		protected PasswordModification(int changeFlag, SystemSignonInformation info)
		{
			this.changeFlag = changeFlag;
			this.info = info;
		}
	}

	private final class PasswordContentProvider implements IStructuredContentProvider, ITableLabelProvider {
		
		/**
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return passwords.toArray();
		}


		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}


		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
				case 0:
					return ((SystemSignonInformation) element).getHostname();
				
				case 1:
					return ((SystemSignonInformation) element).getSystemType();
					
				case 2:
					return ((SystemSignonInformation) element).getUserid();
			}
			
			// Should never get here
			return null;
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(Object, String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
		}

	}

	
	/**
	 * 
	 */
	public SignonPreferencePage() {
		noDefaultAndApplyButton();
		provider = new PasswordContentProvider();
		modifications = new ArrayList();
	}
	
	
	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) 
	{
				
		Composite page = SystemWidgetHelpers.createComposite(parent, 2);
		GridData gd = (GridData) page.getLayoutData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		
		SystemWidgetHelpers.createLabel(page, SystemResources.RESID_PREF_SIGNON_DESCRIPTION, 2);
		
		// Password table
		pwdTable = new Table(page, SWT.FULL_SELECTION |SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		pwdTable.setLinesVisible(true);
		pwdTable.setHeaderVisible(true);
		pwdTable.addListener(SWT.Selection, this);
		SystemWidgetHelpers.setHelp(pwdTable, SystemPlugin.HELPPREFIX + "pwdi0000");
		
		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(100, true));
		tableLayout.addColumnData(new ColumnWeightData(100, true));
		tableLayout.addColumnData(new ColumnWeightData(100, true));
		pwdTable.setLayout(tableLayout);
		
		gd = new GridData(GridData.FILL_BOTH);
	    gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;		  

		pwdTable.setLayoutData(gd);
		
		// Hostname column
		TableColumn hostnameColumn = new TableColumn(pwdTable, SWT.NONE);
		hostnameColumn.setText(SystemResources.RESID_PREF_SIGNON_HOSTNAME_TITLE);		
		
		// System type column
		TableColumn sysTypeColumn = new TableColumn(pwdTable, SWT.NONE);
		sysTypeColumn.setText(SystemResources.RESID_PREF_SIGNON_SYSTYPE_TITLE);
		
		// User ID column
		TableColumn useridColumn = new TableColumn(pwdTable, SWT.NONE);
		useridColumn.setText(SystemResources.RESID_PREF_SIGNON_USERID_TITLE);
		
		pwdTableViewer = new TableViewer(pwdTable);
		pwdTableViewer.setContentProvider(provider);
		pwdTableViewer.setLabelProvider(provider);
		pwdTableViewer.setInput(passwords);		
		
		// Create the Button bar for add, change and remove
		Composite buttonBar = SystemWidgetHelpers.createComposite(page, 1);
		gd = (GridData) buttonBar.getLayoutData();
		gd.grabExcessHorizontalSpace = false;
		gd.grabExcessVerticalSpace = true;

		addButton = SystemWidgetHelpers.createPushButton(buttonBar, this, SystemResources.RESID_PREF_SIGNON_ADD_LABEL, SystemResources.RESID_PREF_SIGNON_ADD_TOOLTIP);
		changeButton = SystemWidgetHelpers.createPushButton(buttonBar, this, SystemResources.RESID_PREF_SIGNON_CHANGE_LABEL, SystemResources.RESID_PREF_SIGNON_CHANGE_TOOLTIP);
		removeButton = SystemWidgetHelpers.createPushButton(buttonBar, this, SystemResources.RESID_PREF_SIGNON_REMOVE_LABEL, SystemResources.RESID_PREF_SIGNON_REMOVE_TOOLTIP);

		changeButton.setEnabled(false);
		removeButton.setEnabled(false);
		
		SystemWidgetHelpers.setHelp(addButton, SystemPlugin.HELPPREFIX + "pwdi0000");
		SystemWidgetHelpers.setHelp(changeButton, SystemPlugin.HELPPREFIX + "pwdi0000");
		SystemWidgetHelpers.setHelp(removeButton, SystemPlugin.HELPPREFIX + "pwdi0000");

		SystemWidgetHelpers.setCompositeHelp(parent, SystemPlugin.HELPPREFIX + "pwdi0000");
		SystemWidgetHelpers.setMnemonics(parent);
		
		return parent;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// reinit passwords list
		passwords = PasswordPersistenceManager.getInstance().getSavedUserIDs();

		// refresh password table
		if (pwdTableViewer != null)
		{
			pwdTableViewer.refresh();			
		}
	}


	/**
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(Event)
	 */
	public void handleEvent(Event event) {
		if (event.type == SWT.Selection) {
			if (event.widget == addButton) {
				SystemPasswordPersistancePrompt dialog = new SystemPasswordPersistancePrompt(getShell(), SystemResources.RESID_PREF_SIGNON_ADD_DIALOG_TITLE, passwords, false);
				if (dialog.open() == Window.OK)
				{
					SystemSignonInformation info = dialog.getSignonInformation();
					passwords.add(info);
					modifications.add(new PasswordModification(PasswordModification.ADD, info));
					
					pwdTableViewer.refresh();	
					pwdTable.select(passwords.size() - 1);	// select the new entry		
				}
				
			} else if (event.widget  == changeButton) {
				SystemPasswordPersistancePrompt dialog = new SystemPasswordPersistancePrompt(getShell(), SystemResources.RESID_PREF_SIGNON_CHANGE_DIALOG_TITLE, passwords, true);
				int index = pwdTable.getSelectionIndex();
				SystemSignonInformation info = (SystemSignonInformation) passwords.get(index);
				dialog.setInputData(info.getSystemType(), info.getHostname(), info.getUserid());
				if (dialog.open() == Window.OK)
				{
					// Remove old and add new
					info = dialog.getSignonInformation();
					SystemSignonInformation oldInfo = (SystemSignonInformation) passwords.remove(index);
					passwords.add(index, info);
					
					modifications.add(new PasswordModification(PasswordModification.DELETE, oldInfo));
					modifications.add(new PasswordModification(PasswordModification.ADD, info));
					
					pwdTableViewer.refresh();	
					pwdTable.select(index);		
				}
				
			} else if (event.widget == removeButton) {
				int[] indicies = pwdTable.getSelectionIndices();
				for (int idx = indicies.length - 1; idx >= 0; idx--)
				{
					PasswordPersistenceManager.getInstance().remove((SystemSignonInformation)passwords.get(indicies[idx]));
					modifications.add(new PasswordModification(PasswordModification.DELETE,
															   (SystemSignonInformation) passwords.remove(indicies[idx])));
				}

				pwdTableViewer.refresh();			
			}
			
			// Update table buttons based on changes
			switch (pwdTable.getSelectionCount())
			{
				case 0:
					changeButton.setEnabled(false);
					removeButton.setEnabled(false);
					break;
					
				case 1:
					changeButton.setEnabled(true);
					removeButton.setEnabled(true);
					break;
					
				default:
					changeButton.setEnabled(false);
					removeButton.setEnabled(true);
					break;
			}					
		}
	}

	
	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		
		if (modifications.size() > 0)
		{
			PasswordModification mod;
			PasswordPersistenceManager manager = PasswordPersistenceManager.getInstance();
			IHost[] connections = SystemPlugin.getTheSystemRegistry().getHosts();
			ISubSystem[] subsystems;
			IConnectorService system;
			
			for (int i = 0; i < modifications.size(); i++)
			{
				mod = (PasswordModification) modifications.get(i);
				
				if (mod.changeFlag == PasswordModification.ADD)
				{
					manager.add(mod.info, true);	

					// yantzi: artemis 6.0, clear any cached passwords that are affected additions 
					// (this is either a real add or a change (which is really a remove followed
					// by an add)
					if (connections != null)
					{
						for (int j = 0; j < connections.length; j++)
						{
							if (connections[j].getHostName().equalsIgnoreCase(mod.info.getHostname()))
							{
								subsystems = connections[j].getSubSystems();
								if (subsystems != null)
								{
									for (int k = 0; k < subsystems.length; k++)
									{
										system = subsystems[k].getConnectorService();
										if (system != null)
										{
											system.clearPasswordCache();
										}
									}
								}
							}
						}
					}
				}
				else if (mod.changeFlag == PasswordModification.DELETE)
				{
					manager.remove(mod.info);
				}				
			}
			
			modifications.clear();
		}
		
		return super.performOk();
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performCancel()
	 */
	public boolean performCancel() {
		modifications.clear();
		return super.performCancel();
	}

}