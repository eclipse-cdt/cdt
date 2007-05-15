/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 ********************************************************************************/

package org.eclipse.rse.internal.ui.propertypages;
import java.util.ResourceBundle;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.SystemPropertyResources;
import org.eclipse.rse.internal.ui.SystemTabFolderLayout;
import org.eclipse.rse.ui.ISystemConnectionFormCaller;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.messages.ISystemMessageLineTarget;
import org.eclipse.rse.ui.propertypages.SystemBasePropertyPage;
import org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;


/**
 * The property page for subsystem properties when accessed from the connection property page.
 * The plugin.xml file registers this for objects of class org.eclipse.rse.ui.model.IHost
   <page
         objectClass="org.eclipse.rse.ui.model.IHost"
         name="SubSystems"
         class="org.eclipse.rse.core.ui.propertypages.SystemConnectionSubSystemsPropertyPage"
         id="org.eclipse.rse.SystemConnectionSubSystemsPropertyPage">
   </page>
 */
public class SystemConnectionSubSystemsPropertyPage extends SystemBasePropertyPage
       implements ISystemMessageLine, ISystemConnectionFormCaller, SelectionListener
{
	    
	protected CTabFolder tabFolder;
	protected ResourceBundle rb;
	protected String parentHelpId;	
	protected PropertyPage[] pages;
	protected CTabItem[] tabs;
	
	/**
	 * Constructor.
	 */
	public SystemConnectionSubSystemsPropertyPage()
	{
		super();
		//RSEUIPlugin sp = RSEUIPlugin.getDefault();
		parentHelpId = RSEUIPlugin.HELPPREFIX + "pcon0000"; //$NON-NLS-1$
	}
	
	/**
	 * Return false if you don't want to have mnemonics automatically applied to your page
	 * by this parent class. We return false as we assume each subpage does their own.
	 */
	protected boolean wantMnemonics()
	{
		return false;
	}
	
	/**
	 * Create the page's GUI contents.
	 */
	protected Control createContentArea(Composite parent)
	{
		// prepare input data
		//IHost conn = (IHost)getElement();
		
		// create notebook
		Composite composite_prompts = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;	
		layout.numColumns = 1;		
		composite_prompts.setLayout(layout);
		
		// create notebook pages
		ISubSystem[] subsystems = getSubSystems();        
		pages = getSubSystemPropertyPages(subsystems);
		
		if (pages != null)
		{
			tabFolder = createTabFolder(composite_prompts);    	
			createTabbedItems(pages);
			tabFolder.setFocus();
		}
		else
		{
			SystemWidgetHelpers.createLabel(composite_prompts, SystemPropertyResources.RESID_TERM_NOTAVAILABLE);
		}
		
		SystemWidgetHelpers.setCompositeHelp(parent, parentHelpId);
		return composite_prompts;
	}
	
	/**
	 * Create the notebook.
	 */
	protected CTabFolder createTabFolder(Composite parent)
	{
		tabFolder = new CTabFolder(parent, SWT.NONE);
		tabFolder.setLayout(new SystemTabFolderLayout());		
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		return tabFolder;
	}
	
	/**
	 * Query the property page for each subsystem and then add each to the 
	 *  tabbed notebook as a tab...
	 */
	private PropertyPage[] getSubSystemPropertyPages(ISubSystem[] subsystems)
	{
		PropertyPage[] pages = null;
		Vector v = new Vector();
		
		for (int idx = 0; idx < subsystems.length; idx++)
		{
			ISubSystem ss = subsystems[idx];
			
			ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ss.getSubSystemConfiguration().getAdapter(ISubSystemConfigurationAdapter.class);
			PropertyPage page = adapter.getPropertyPage(ss, tabFolder);
			
			if (page != null)
			{
				page.setContainer(getContainer());
			}
			
			if (page != null)
			{
				page.setElement((IAdaptable)ss);
				
				if (page instanceof ISystemMessageLineTarget)
					((ISystemMessageLineTarget)page).setMessageLine(msgLine);
				
				v.addElement(page);
			}
		}
		
		if (v.size() > 0)
		{
			pages = new PropertyPage[v.size()];
			
			for (int idx=0; idx<pages.length; idx++)
			{
				pages[idx] = (PropertyPage)v.elementAt(idx);
			}
		}
		
		return pages;
	}
	
	/** 
	 * For each property page create a tabbed item.
	 */
	private void createTabbedItems(PropertyPage[] pages)
	{
		if (pages != null)
		{
			tabs = new CTabItem[pages.length];
			
			for (int idx = 0; idx < pages.length; idx++)
			{
				PropertyPage page = pages[idx];  
				ISubSystem ss = (ISubSystem)((IWorkbenchPropertyPage)page).getElement();  	       
				CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
				tabItem.setText(ss.getName());
				
				ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ss.getSubSystemConfiguration().getAdapter(ISubSystemConfigurationAdapter.class);				
				Image image = adapter.getGraphicsImage(ss.getSubSystemConfiguration());
				
				if (image != null)
				{
					tabItem.setImage(image);
				}
				
				tabItem.setData(page);
				
				if (page.getControl() == null)
				{
					page.createControl(tabFolder);
				}
				
				// dy:  Need to force the control to repack incase the buttonbar was added
				// and subsequently removed.  Otherwise an ArrayIndexOutOfBounds exception
				// occurs when SWT tries to lay out the page
				page.getControl().pack(true);
				tabItem.setControl(page.getControl());
				tabs[idx] = tabItem;
			}
		}		
	}
	
	/**
	 * Get the input connection object
	 */
	protected IHost getConnection()
	{
		return (IHost)getElement();
	}
	
	/**
	 * Get the input subsystems
	 */
	protected ISubSystem[] getSubSystems()
	{
		ISubSystem[] subsystems = 
			RSECorePlugin.getTheSystemRegistry().getSubSystems(getConnection());		
		return subsystems;
	}
	
	/**
	 * The <code>PreferencePage</code> implementation of this 
	 * <code>IPreferencePage</code> method returns <code>true</code>
	 * if the page is valid.
	 * <p>
	 * We cycle through all pages calling okToLeave().
	 */
	public boolean okToLeave() 
	{
		boolean ok = isValid();
		
		if (ok && (pages!= null) && (pages.length > 0))
		{
			int currIdx = tabFolder.getSelectionIndex();
			
			// if a page is selected
			if (currIdx != -1)
			{
				PropertyPage currentPage = pages[currIdx];
				ok = currentPage.okToLeave();
			}
			
			for (int idx = 0; ok && (idx < pages.length); idx++)
			{
				if (idx != currIdx)
				{
					PropertyPage page = pages[idx];  
					ok = page.okToLeave();
					
					if (!ok)
					{
						tabFolder.setSelection(idx);
					}
				}
			}
		}
		
		return ok;
	}
	
	/**
	 * Return true if this page is valid. Override of parent.
	 * Cycles through all tab pages calling isValid.
	 */
	public boolean isValid()
	{
		boolean ok = super.isValid();
		if (ok && (pages!=null) && (pages.length>0))
		{
			for (int idx=0; ok && (idx<pages.length); idx++)
			{
				PropertyPage page = pages[idx];  
				ok = page.isValid();
			}
		}
		return ok;    	
	}
	
	/**
	 * Called by parent when user presses OK.
	 * We cycle through all subpages calling performOk
	 */
	public boolean performOk()
	{
		boolean okToClose = super.performOk();
		
		if (okToClose && (pages != null) && (pages.length > 0))
		{
			int currIdx = tabFolder.getSelectionIndex();
			
			if (currIdx != -1)
			{
				PropertyPage currentPage = pages[currIdx];
				okToClose = currentPage.performOk();
			}
			
			for (int idx = 0; okToClose && (idx < pages.length); idx++)
			{
				if (idx != currIdx)
				{
					PropertyPage page = pages[idx];  
					okToClose = page.performOk();
					
					if (!okToClose)
					{
						tabFolder.setSelection(idx);
					}
				}
			}
		}
		
		return okToClose;
	}
	
	/**
	 * Validate all the widgets on the page
	 * <p>
	 * Subclasses should override to do full error checking on all
	 *  the widgets on the page.
	 */
	protected boolean verifyPageContents()
	{
		return true;
	}
	
	/**
	 * Returns the implementation of ISystemViewElement for the given
	 * object.  Returns null if the adapter is not defined or the
	 * object is not adaptable.
	 */
	protected ISystemViewElementAdapter getViewAdapter(Object o) 
	{
		return SystemAdapterHelpers.getViewAdapter(o);
	}
	/**
	 * Returns the implementation of ISystemRemoteElement for the given
	 * object.  Returns null if this object does not adaptable to this.
	 */
	protected ISystemRemoteElementAdapter getRemoteAdapter(Object o) 
	{
		return SystemAdapterHelpers.getRemoteAdapter(o);
	}
	
	// ----------------------------------------
	// The following were for any aborted attempt to query registered subsystem property pages
	//  and use them. The problem with this is that for iSeries, all property pages for subsystems
	//  are shared and we ended up with redundancies.
	// ---------------------------------------- 
	
	// ----------------------------------------
	// SelectionListener methods...
	// ----------------------------------------
	/**
	 * A tab item selected
	 */
	public void widgetSelected(SelectionEvent event)
	{
		if (event.getSource() == tabFolder)
		{
		}
	}
	/**
	 * Not used
	 */
	public void widgetDefaultSelected(SelectionEvent event)
	{
	}
	
	// ----------------------------------------
	// CALLBACKS FROM SYSTEM CONNECTION FORM...
	// ----------------------------------------
	/**
	 * Event: the user has selected a system type.
	 */
	public void systemTypeSelected(IRSESystemType systemType, boolean duringInitialization)
	{
	}
}