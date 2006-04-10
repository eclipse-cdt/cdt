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

package org.eclipse.rse.ui;
import org.eclipse.ui.IWorkbenchActionConstants;
/**
 * Constants defining our groups inside our right-click popup menu in the system view.
 * <pre><code>
 * 	    // simply sets partitions in the menu, into which actions can be directed.
 *	    // Each partition can be delimited by a separator (new Separator) or not (new GroupMarker).
 *		menu.add(new Separator(ISystemContextMenuConstants.GROUP_NEW));          // new->
 *		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_GOTO));       // goto into, go->
 *		menu.add(new Separator(ISystemContextMenuConstants.GROUP_EXPANDTO));     // expand to->
 *		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_EXPAND));     // expand, collapse
 *		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_OPEN));       // open xxx
 *		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_OPENWITH));   // open with->
 *		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_BROWSEWITH)); // open with->
 *		menu.add(new Separator(ISystemContextMenuConstants.GROUP_WORKWITH));     // work with->
 *		menu.add(new Separator(ISystemContextMenuConstants.GROUP_BUILD));        // build, rebuild, refresh
 *		menu.add(new Separator(ISystemContextMenuConstants.GROUP_CHANGE));       // update, change
 *		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORGANIZE));   // rename,move,copy,delete,bookmark,refactoring
 *		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORDER));      // move up, move down		
 *		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_GENERATE));   // getters/setters, etc. Typically in editor
 *		menu.add(new Separator(ISystemContextMenuConstants.GROUP_SEARCH));       // search
 *		menu.add(new Separator(ISystemContextMenuConstants.GROUP_CONNECTION));   // connection-related actions
 *		menu.add(new Separator(ISystemContextMenuConstants.GROUP_IMPORTEXPORT)); // get or put actions
 *		menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADAPTERS));     // actions queried from adapters
 *		menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADDITIONS));    // user or BP/ISV additions
 *		menu.add(new Separator(ISystemContextMenuConstants.GROUP_TEAM));         // Team
 *		menu.add(new Separator(ISystemContextMenuConstants.GROUP_PROPERTIES));   // Properties
 * </code></pre>
 */
public interface ISystemContextMenuConstants
{
	/**
	 * Pop-up menu: name of group for goto actions (value <code>"group.goto"</code>).
	 * <p>
	 * Examples for open actions are:
	 * <ul>
	 *  <li>Go Into</li>
	 *  <li>Go To</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_GOTO=		"group.goto";
	/**
	 * ID of the submenu for "Go To->"
	 */
	public static final String MENU_GOTO= "menu.goto";
	/**
	 * ID of the submenu for "Preferences->"
	 */
	public static final String MENU_PREFERENCES= "menu.preferences";
		
	/**
	 * Pop-up menu: name of group for open-with actions (value <code>"group.openwith"</code>).
	 * <p>
	 * Examples for open-with actions are:
	 * <ul>
	 *  <li>Open With->Editor</li>
	 *  <li>Open With->Designer</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_OPENWITH=		"group.openwith";
	/**
	 * ID of the submenu for "Open With->"
	 */
	public static final String MENU_OPENWITH= "menu.openwith";

	/**
	 * Group name for the "Browse With" submenu
	 */
	public static final String GROUP_BROWSEWITH=	"group.browsewith";
	
	/**
	 * Group name for the "Compare With" submenu
	 */
	public static final String GROUP_COMPAREWITH=	"group.comparewith";
	
	/**
	 * Group name for the "Replace With" submenu
	 */
	public static final String GROUP_REPLACEWITH=	"group.replacewith";

	/**
	 * ID for "Browse With" submenu
	 */
	public static final String MENU_BROWSEWITH = 	"menu.browsewith";

	/**
	 * ID for "Compare With" submenu
	 */
	public static final String MENU_COMPAREWITH = 	"menu.comparewith";

	/**
	 * ID for "Compare With" submenu
     */
	public static final String MENU_REPLACEWITH = 	"menu.replacewith";

	/**
	 * Pop-up menu: name of group for expand actions (value <code>"group.expand"</code>).
	 */
	public static final String GROUP_EXPAND =		"group.expand";

	/**
	 * Pop-up menu: name of group for expand-to cascading actions (value <code>"group.expandto"</code>).
	 */
	public static final String GROUP_EXPANDTO=		"group.expandto";
	/**
	 * ID of the submenu for "Expand to->"
	 */
	public static final String MENU_EXPANDTO= "menu.expandto";

	/**
	 * Pop-up menu: name of group for open-to actions (value <code>"group.opento"</code>).
	 * <p>
	 * Examples for open-to actions are:
	 * <ul>
	 *  <li>Open To->Navigator</li>
	 *  <li>Open To->iSeries Navigator</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_OPENTO=		"group.opento";
	/**
	 * ID of the submenu for "Open To->"
	 */
	public static final String MENU_OPENTO= "menu.opento";
	
	/**
	 * Pop-up menu: name of group for work-with actions (value <code>"group.workwith"</code>).
	 * <p>
	 * Examples for work-with actions are:
	 * <ul>
	 *  <li>Work with->Filter Pools...</li>
	 *  <li>Work with->User Actions...</li>
	 *  <li>Work with->File Types...</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_WORKWITH=		"group.workwith";
	/**
	 * ID of the submenu for "Work Work->"
	 */
	public static final String MENU_WORKWITH= "menu.workwith";
	/**
	 * Pop-up menu: name of group for open actions (value <code>"group.open"</code>).
	 * <p>
	 * Examples for open actions are:
	 * <ul>
	 *  <li>Open To</li>
	 *  <li>Open With</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_OPEN=		"group.open";
	/**
	 * Pop-up menu: name of group for show actions (value <code>"group.show"</code>).
	 * <p>
	 * Examples for show actions are:
	 * <ul>
	 *  <li>Show in Navigator</li>
	 *  <li>Show in Type Hierarchy</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_SHOW=		"group.show";
	
	/**
	 * Pop-up menu: name of group for new actions (value <code>"group.new"</code>).
	 * This is a cascading group.
	 * <p>
	 * Examples for new actions are:
	 * <ul>
	 *  <li>Create new filter</li>
	 *  <li>Create new folder</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_NEW=		"group.new";
	/**
	 * ID of the submenu for "New->"
	 */
	public static final String MENU_NEW= "menu.new";

	/**
	 * Pop-up menu: name of group for new actions (value <code>"group.new.noncascade"</code>).
	 * This is a non-cascading group.
	 * <p>
	 * This is used in the Team view
	 * </p>
	 */
	public static final String GROUP_NEW_NONCASCADING="group.new.noncascade";
		
	/**
	 * Pop-up menu: name of group for build actions (value <code>"group.build"</code>).
	 */
	public static final String GROUP_BUILD=		"group.build";
	
	/**
	 * Pop-up menu: name of group for reorganize actions (value <code>"group.reorganize"</code>).
	 */	
	public static final String GROUP_REORGANIZE=	"group.reorganize";	
	/**
	 * Pop-up menu: name of group for reorder actions like move up/down(value <code>"group.reorder"</code>).
	 */	
	public static final String GROUP_REORDER=	"group.reorder";	
	/**
	 * Pop-up menu: name of group for CHANGE actions. (value <code>"group.change"</code>).
	 * <p>
	 * Examples for change actions are:
	 * <ul>
	 *  <li>Change...</li>
	 *  <li>Update...</li>
	 * </ul>
	 * SHould you even have a change action? Maybe it should be a PropertyPage instead!
	 * </p>
	 */
	public static final String GROUP_CHANGE = "group.change";
		
	/**
	 * Pop-up menu: name of group for code generation or refactoring actions (
	 * value <code>"group.generate"</code>).
	 */	
	public static final String GROUP_GENERATE=	"group.generate";

	/**
	 * Pop-up menu: name of group for search actions (value <code>"group.search"</code>).
	 */	
	public static final String GROUP_SEARCH=		"group.search";
	
	/**
	 * Pop-up menu: name of group for additional actions (value <code>"group.additions"</code>).
	 */	
	public static final String GROUP_ADDITIONS=	IWorkbenchActionConstants.MB_ADDITIONS; //"additions";

	/**
	 * Pop-up menu: name of group for viewer setup actions (value <code>"group.viewerSetup"</code>).
	 */	
	public static final String GROUP_VIEWER_SETUP=	"group.viewerSetup";

	/**
	 * Pop-up menu: name of group for properties actions (value <code>"group.properties"</code>).
	 */	
	public static final String GROUP_PROPERTIES=	"group.properties";	
	/**
	 * Pop-up menu: name of group for actions contributed by the adaptors for the selected object, which
	 *  are related to the live connection.
	 */
	public static final String GROUP_CONNECTION= "group.connection";	
	/**
	 * Pop-up menu: name of group for actions related to getting and putting the selected object.
	 */
	public static final String GROUP_IMPORTEXPORT= "group.importexport";	
	/**
	 * Pop-up menu: name of group for actions contributed by the adaptors for the selected object
	 */
	public static final String GROUP_ADAPTERS= "group.adapters";
	/**
	 * Pop-up menu: name of group for team actions
	 */
	public static final String GROUP_TEAM= "group.team";


	/**
	 * ID of the submenu for "Compile->"
	 */
	public static final String MENU_COMPILE= "menu.compile";
	/**
	 * ID of the submenu for "User Actions->"
	 */
	public static final String MENU_USERACTIONS= "menu.useractions";

	/**
	 * Group for "Start Server->"
	 */
	public static final String GROUP_STARTSERVER= "group.remoteservers";	
	/**
	 * ID of the submenu for "Start Server->"
	 */
	public static final String MENU_STARTSERVER= "menu.remoteservers";
}