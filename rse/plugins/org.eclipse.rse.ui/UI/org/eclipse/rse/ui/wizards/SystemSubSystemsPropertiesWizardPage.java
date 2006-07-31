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

package org.eclipse.rse.ui.wizards;
 
import java.util.List;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.ui.ISystemVerifyListener;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.propertypages.ISystemConnectionWizardErrorUpdater;
import org.eclipse.rse.ui.propertypages.ISystemConnectionWizardPropertyPage;
import org.eclipse.rse.ui.view.monitor.TabFolderLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;


/**
 * Wizard page that display the property pages for a given subsystem in the
 * connection
 *  
 */
public class SystemSubSystemsPropertiesWizardPage
	extends AbstractSystemNewConnectionWizardPage
	implements ISystemVerifyListener, ISubSystemPropertiesWizardPage
{

	private CTabFolder _folder;
	private List _propertyPages;
	private String _lastHostName;
	/**
	 * Constructor
	 */
	public SystemSubSystemsPropertiesWizardPage(IWizard wizard, ISubSystemConfiguration parentFactory, List propertyPages)
	{
		//super(wizard, parentFactory); todo: use this when we enable port
		// selection
		super(
			wizard,
			parentFactory,
			parentFactory.getId(),
			// removed subsystem append since not correct for some languages
			parentFactory.getName(),
			//+ " " +  SystemResources.RESID_SUBSYSTEM_TYPE_VALUE),
			//" SubSystem Properties", //TODO create
															   // message for
															   // this
			//"Configure properties of this subsystem"
			SystemResources.RESID_NEWCONN_SUBSYSTEMPAGE_DESCRIPTION
			);
		_propertyPages = propertyPages;
	}

	/**
	 * @see AbstractSystemWizardPage#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl()
	{
		return getControl();
	}
	
	/*
	 * Updates wiard property pages with new hostname
	 */
	protected void hostNameUpdated(String hostName)
	{
		if (_folder != null)
		{
			for (int i = 0; i < _folder.getItemCount(); i++)
			{
				ISystemConnectionWizardPropertyPage page =
					(ISystemConnectionWizardPropertyPage) _folder.getItem(i).getData();
				page.setHostname(hostName);
			}
		}

	}

	/**
	 * @see AbstractSystemWizardPage#createContents(Composite)
	 */
	public Control createContents(Composite parent)
	{
		int nbrColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);

		_folder = new CTabFolder(composite_prompts, SWT.NONE);
		_folder.setLayout(new TabFolderLayout());

		int numAdded = 0;
		for (int i = 0; i < _propertyPages.size(); i++)
		{
			PropertyPage page = (PropertyPage)_propertyPages.get(i);
			if (page != null && page instanceof ISystemConnectionWizardPropertyPage)
			{
			    ISystemConnectionWizardPropertyPage cpage = (ISystemConnectionWizardPropertyPage)page;
				cpage.setSubSystemConfiguration(parentFactory);
				
				CTabItem titem = new CTabItem(_folder, SWT.NULL, numAdded);
				titem.setData(page);
				page.createControl(_folder);
				titem.setText(page.getTitle());
				try
				{
					titem.setControl(page.getControl());
		
				}
				catch (Exception e)
				{
					// TODO why does the tabfolder hit exception the
					// first tiem setcontrol is called?
				}
				
				//set the hostname for the page in case it's required
				cpage.setHostname(getMainPage().getHostName());
				cpage.setSystemType(getMainPage().getSystemType());
				
				numAdded++;
			}			

			}
		
		
		if (numAdded == 0)
		{
			
		}
		addVerifyListener();

		return composite_prompts;
	}

	/**
	 * @see ISystemWizardPage#performFinish()
	 */
	public boolean performFinish()
	{
		return true;
	}

	public boolean applyValues(ISubSystem ss)
	{
		boolean result = true;
		if (_folder != null)
		{
			for (int i = 0; i < _folder.getItemCount() && result; i++)
			{
				ISystemConnectionWizardPropertyPage page =
					(ISystemConnectionWizardPropertyPage) _folder.getItem(i).getData();
				result = page.applyValues(ss.getConnectorService());
			}
		}
		return result;
	}

	/**
	 * Return true if the page is complete, so to enable Finish. Called by
	 * wizard framework.
	 */
	public boolean isPageComplete()
	{
	    String hostName = getMainPage().getHostName();
	    if (!hostName.equals(_lastHostName))
	    {
	        hostNameUpdated(hostName);
	        _lastHostName = hostName;
	    }
		boolean result = true;
		if (_folder != null)
		{
			for (int i = 0; i < _folder.getItemCount() && result; i++)
			{
				if (_folder.getItem(i).getData() instanceof ISystemConnectionWizardErrorUpdater)
				{
					ISystemConnectionWizardErrorUpdater page =
						(ISystemConnectionWizardErrorUpdater) _folder.getItem(i).getData();
					result = page.isPageComplete();
				}
			}
		}
		return result;

	}
	
	protected void addVerifyListener()
	{
		if (_folder != null)
		{
			for (int i = 0; i < _folder.getItemCount(); i++)
			{
				if (_folder.getItem(i).getData() instanceof ISystemConnectionWizardErrorUpdater)
				{
					ISystemConnectionWizardErrorUpdater page =
						(ISystemConnectionWizardErrorUpdater) _folder.getItem(i).getData();
					page.addVerifyListener(this);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void handleVerifyComplete() 
	{
		boolean complete = isPageComplete();
		if (!complete)
		{
			if (_folder != null)
			{
				for (int i = 0; i < _folder.getItemCount(); i++)
				{
					if (_folder.getItem(i).getData() instanceof ISystemConnectionWizardErrorUpdater)
					{
						ISystemConnectionWizardErrorUpdater page =
							(ISystemConnectionWizardErrorUpdater) _folder.getItem(i).getData();
						String error = page.getTheErrorMessage();
						if (error != null && !error.equals(""))
						{
							setErrorMessage(_folder.getItem(i).getText() + ": " + page.getTheErrorMessage());
						}
					}
				}
			}
		}
		else
		{
			clearErrorMessage();
		}
		setPageComplete(complete);
	}
}