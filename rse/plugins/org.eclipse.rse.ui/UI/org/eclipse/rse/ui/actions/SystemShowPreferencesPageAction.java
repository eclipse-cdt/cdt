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

package org.eclipse.rse.ui.actions;
//import com.ibm.etools.systems.model.*; 
//import com.ibm.etools.systems.model.impl.*; 
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog;



/**
 * This action will launch the Prefences dialog, but only rooted at a given
 * preference page (it will include its children underneath), including the
 * child pages registered under that page ("category").
 * <p>
 * This is used by the org.eclipse.rse.core.remoteSystemsViewPreferencesActions
 *  extension point.
 * @see org.eclipse.rse.ui.actions.SystemCascadingPreferencesAction 
 */
public class SystemShowPreferencesPageAction extends SystemBaseAction implements IViewActionDelegate                                  
{
	
	private PreferenceManager preferenceManager;	
	private String[] preferencePageIDs;
	private String preferencePageCategory;
	
	/**
	 * Constructor. We are instantiated inside {@link RSEUIPlugin#getPreferencePageActionPlugins()}
	 *  for each extension of our extension point <code>org.eclipse.rse.core.remoteSystemsViewPreferencesActions</code>
	 */
	public SystemShowPreferencesPageAction()
	{
		super("temp label", null);
	}
    
	/**
	 * Set ID of the preference root page to show.
	 * @param preferencePageID The ID of the preference page root to show. All child nodes will also be shown.
	 */
	public void setPreferencePageID(String preferencePageID)
	{
		setPreferencePageID(new String[] {preferencePageID});
	}
	/**
	 * Set IDs of the preference root pages to show.
	 * @param preferencePageIDs The IDs of the preference page roots to show. All child nodes will also be shown.
	 */
	public void setPreferencePageID(String[] preferencePageIDs)
	{
		allowOnMultipleSelection(false);        
		setSelectionSensitive(false);
		this.preferencePageIDs = preferencePageIDs;
	}
	/**
	 * Set the category of the pages to be shown. This only needs to be called
	 *  for non-root pages. Note that the ID to give here is not of the immediate
	 *  parent, but that of the root parent. It tells us which root subtree to 
	 *  search for the given page(s).
	 */
	public void setPreferencePageCategory(String preferencePageCategory)
	{
		this.preferencePageCategory = preferencePageCategory;
	}
	
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) 
	{
		setShell(view.getSite().getShell());
	}


	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) 
	{
		run();
	}


	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) 
	{
	}
	
	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		// Bring up the preferences page
		/*
		PreferenceManager prefMgr = new PreferenceManager();
		prefMgr.addToRoot(new PreferenceNode("tempid", new RemoteSystemsPreferencePage()));
		PreferenceDialog dialog = new PreferenceDialog(shell, prefMgr);
		dialog.open();
		*/
		PreferenceManager pm = getPreferenceManager();
	
		if (pm != null) 
		{
			PreferenceDialog d = new WorkbenchPreferenceDialog(shell, pm);
			d.create();
			// TODO - hack to make this work in  3.1
			String id = PlatformUI.PLUGIN_ID + ".preference_dialog_context";
		
			PlatformUI.getWorkbench().getHelpSystem().setHelp(d.getShell(), id);
			d.open();	
		}				
	}		
	/*
	 * Get the preference manager.
	 */
	public PreferenceManager getPreferenceManager() 
	{
		if (preferenceManager == null) 
		{
			preferenceManager = new PreferenceManager('/');

			//Get the pages from the registry
			//PreferencePageRegistryReader registryReader = new PreferencePageRegistryReader(PlatformUI.getWorkbench());

			//List pageContributions = registryReader.getPreferenceContributions(Platform.getExtensionRegistry());
			
			PreferenceManager workbenchMgr = PlatformUI.getWorkbench().getPreferenceManager();
			
			List pageContributions = workbenchMgr.getElements(PreferenceManager.POST_ORDER);
			
			

			//Add the contributions to the manager
			Iterator iter = pageContributions.iterator();
			while (iter.hasNext()) 
			{
				IPreferenceNode prefNode = (IPreferenceNode) iter.next();
				//System.out.println("prefNode.getId() == "+prefNode.getId());
	 			//System.out.println("         getLabelText() == "+prefNode.getLabelText());
	 			boolean match = false;
	 			String prefNodeID = prefNode.getId();
	 			if (preferencePageCategory == null)
	 			{
	 				match = testForMatch(prefNodeID);
				}
				else if (prefNodeID.equals(preferencePageCategory))
				{
					//System.out.println("Made it here");
					prefNode = searchForSubPage(prefNode, prefNodeID);
					if (prefNode != null)
						match = true;				
				}
				if (match)
					preferenceManager.addToRoot(prefNode);
			}
			
		}
		return preferenceManager;
	}
	
	private IPreferenceNode searchForSubPage(IPreferenceNode parent, String prefNodeID)
	{
		IPreferenceNode match = null;

		IPreferenceNode[] subNodes = parent.getSubNodes();
		if (subNodes!=null)
			for (int idx=0; (match==null) && (idx<subNodes.length); idx++)
			{
				if (testForMatch(subNodes[idx].getId()))
					match = subNodes[idx];
				else 
					match = searchForSubPage(subNodes[idx], prefNodeID);
			}
		
		return match;
	}
	
	private boolean testForMatch(String prefNodeID)
	{
		boolean match = false;
		for (int idx=0; !match && (idx<preferencePageIDs.length); idx++)
		{	 				
			if (prefNodeID.equals(preferencePageIDs[idx]))
				match = true;
		}
		return match;
	}
}