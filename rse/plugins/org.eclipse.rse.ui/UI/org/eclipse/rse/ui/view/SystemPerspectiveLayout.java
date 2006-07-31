/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view;



import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.rse.ui.view.scratchpad.SystemScratchpadViewPart;
import org.eclipse.rse.ui.view.team.SystemTeamViewPart;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


/**
 * This class is responsible for laying out the views in the RSE perspective
 */
public class SystemPerspectiveLayout implements IPerspectiveFactory
{

	public static final String ID = "org.eclipse.rse.ui.view.SystemPerspective"; // matches id in plugin.xml, layout tag
	/**
	 * Defines the initial layout for a perspective.
	 * This method is only called when a new perspective is created.  If
	 * an old perspective is restored from a persistence file then
	 * this method is not called.
	 *
	 * @param factory the factory used to add views to the perspective
	 */
	public void createInitialLayout(IPageLayout layout) 
	{
        String editorArea = layout.getEditorArea();
		
		IFolderLayout folder= layout.createFolder("org.eclipse.rse.ui.view.NavFolder", IPageLayout.LEFT, 
		                                   (float)0.25, editorArea);
		//folder.addView(IPageLayout.ID_RES_NAV);
		folder.addView(SystemViewPart.ID);
		folder.addView(SystemTeamViewPart.ID);
						           
		folder= layout.createFolder("org.eclipse.rse.ui.view.MiscFolder", IPageLayout.BOTTOM, 
		                            (float).60, editorArea);		           

		folder.addView(SystemTableViewPart.ID);
		//folder.addView(SystemMonitorViewPart.ID);
		folder.addView(IPageLayout.ID_TASK_LIST); // put in the desktop-supplied task list view

		
		folder= layout.createFolder("org.eclipse.rse.ui.view.OutlineFolder", IPageLayout.RIGHT, 
		                            (float).80, editorArea);		           
	
		folder.addView(IPageLayout.ID_OUTLINE); // put in desktop-supplied outline view
		// unfortunately we can't do the following as snippets aren't in wswb, according to DKM

		folder= layout.createFolder("org.eclipse.rse.ui.view.PropertiesFolder", IPageLayout.BOTTOM, 
                (float).75, "org.eclipse.rse.ui.view.NavFolder");	
		//layout.addView(IPageLayout.ID_PROP_SHEET, IPageLayout.BOTTOM, 
		  //     (float)0.75, "org.eclipse.rse.ui.view.NavFolder"); // put in desktop-supplied property sheet view         
		folder.addView(IPageLayout.ID_PROP_SHEET);
		folder.addView(SystemScratchpadViewPart.ID);
		       
		// update Show View menu with our views       
		layout.addShowViewShortcut(SystemViewPart.ID);
		layout.addShowViewShortcut(SystemTableViewPart.ID);

		layout.addShowViewShortcut(SystemTableViewPart.ID);		
		layout.addShowViewShortcut(SystemViewPart.ID);	
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
		// update Perspective Open menu with our perspective
		layout.addPerspectiveShortcut(ID);
		
		// Add action sets to the tool bar.
		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		layout.addActionSet(IDebugUIConstants.DEBUG_ACTION_SET);

	}
}