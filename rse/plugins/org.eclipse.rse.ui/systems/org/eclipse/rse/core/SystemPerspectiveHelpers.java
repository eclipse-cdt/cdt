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

package org.eclipse.rse.core;
import org.eclipse.rse.ui.view.SystemPerspectiveLayout;
import org.eclipse.rse.ui.view.SystemView;
import org.eclipse.rse.ui.view.SystemViewPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;


/**
 * @author coulthar
 *
 * Helper methods related to finding and switching perspectives, views and
 *  so on.
 */
public class SystemPerspectiveHelpers
{

	// constants
	/**
	 * The ID of the RSE perspective
	 */
	public static String RSE_PERSP_ID = SystemPerspectiveLayout.ID;


    /**
     * Opens the RSE perspective, if not already, in the current window
     */
    public static boolean openRSEPerspective()
    {
    	return openInNewPage(RSE_PERSP_ID);
    }
    /**
     * Tests if the RSE perspective is the active perspective
     */
    public static boolean isRSEPerspectiveActive()
    {
    	IPerspectiveDescriptor activePersp = getActivePerspective();
    	if ((activePersp!=null) && activePersp.getId().equals(RSE_PERSP_ID))
    	  return true;
    	else
    	  return false;
    }
	/**
	 * Opens a new page with a particular perspective, given the perspective's ID
	 * @return true if was open or successfully opened/focused. False if anything went wrong
	 */
    public static boolean openInNewPage(String perspID) 
	{
		boolean ok = false;
		IPerspectiveDescriptor persp = getActivePerspective();
		if (persp == null)
		  return ok;
		if (persp.getId().equals(perspID))
		  return true;
		// If the perspective is already open, then reuse it.
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage[] pages = window.getPages();
		for (int i = 0; i < pages.length; i++) 
		{
			persp = pages[i].getPerspective();
			if (persp != null && persp.getId().equals(perspID)) 
			{
				window.setActivePage(pages[i]);
				return true;
			}
		}
		// Open the page.
		try {
			//window.openPage(perspID, ResourcesPlugin.getWorkspace().getRoot()); OPENS A NEW WINDOW!!
			window.getWorkbench().showPerspective(perspID, window);
			ok = true;
		} catch (WorkbenchException e) {
		   	SystemBasePlugin.logError("Error opening perspective "+perspID, e);
		}
		return ok;
	}
	/**
	 * Return the currently active perpsective in the currently active page in the
	 *  currently active window.
	 * May return null!
	 */
	public static IPerspectiveDescriptor getActivePerspective()
	{
		// get the active window
		IWorkbenchWindow window = getActiveWindow();
		if (window != null)
		{
		  // get the active page
		  IWorkbenchPage page = window.getActivePage();
		  if (page != null) 
			// get the active perspective
			return page.getPerspective();
		}
		return null;
	}
	/**
	 * Return the currently active window. 
	 * May return null!
	 */
	public static IWorkbenchWindow getActiveWindow()
	{
		// get the active window
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}
	
	/**
	 * Search for, and return, a view with the given ID, in the active perspective
	 */
	public static IViewPart findView(String viewID)
	{
		IViewPart viewPart = null;
		IWorkbenchWindow window = getActiveWindow();
		if (window != null) 
		{
			IWorkbenchPage page = window.getActivePage();
		    if (page != null) 
			  viewPart= page.findView(viewID);													
		} // end if window != null
		return viewPart;		
	}
	
	/**
	 * Return the RSE tree view in the active perspective, or null if the active
	 * perspective is NOT the RSE perspective.
	 */
	public static SystemView findRSEView()
	{
		IViewPart viewPart = findView(SystemViewPart.ID);
		if ((viewPart != null) && (viewPart instanceof SystemViewPart))
		  return ((SystemViewPart)viewPart).getSystemView();
		else
		  return null;
	}
	
	/**
	 * Show the view with given ID, if not already showing in current perspective,
	 * in current page, in current window.
	 * @return the view part instance if found or opened successfully. Null if something went wrong
	 */
	public static IViewPart showView(String viewID)
	{
		IViewPart viewPart = null;
		IWorkbenchWindow window = getActiveWindow();
		if (window != null) 
		{
			IWorkbenchPage page = window.getActivePage();
		    if (page != null) 
			{
			   try 
			   {
				 viewPart= page.findView(viewID);													
				 if ( viewPart != null )
				    page.bringToTop(viewPart);
				 else 
				 {
					//IWorkbenchPart activePart= page.getActivePart(); not used? Phil
				    viewPart = page.showView(viewID);
				 }
			   } 
			   catch (PartInitException pie) 
			   {
			   	  SystemBasePlugin.logError("Error opening view " + viewID, pie);
			   }
		    } //end if page != null
		} // end if window != null
		return viewPart;
	}
	
}