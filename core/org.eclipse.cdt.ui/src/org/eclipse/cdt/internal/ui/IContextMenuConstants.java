/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Constants for menu groups used in context menus for C views and editors.
 * <p>
 * This interface declares constants only; it is not intended to be implemented.
 * </p>
 */
public interface IContextMenuConstants {

	/**
	 * Type hierarchy view part: pop-up menu target ID for supertype hierarchy viewer
	 * (value <code>"org.eclipse.cdt.ui.TypeHierarchy.supertypes"</code>).
	 * 
	 * @since 2.0
	 */
	public static final String TARGET_ID_SUPERTYPES_VIEW= CUIPlugin.ID_TYPE_HIERARCHY + ".supertypes"; //$NON-NLS-1$	

	/**
	 * Type hierarchy view part: Pop-up menu target ID for the subtype hierarchy viewer
	 * (value <code>"org.eclipse.cdt.ui.TypeHierarchy.subtypes"</code>).
	 * 
	 * @since 2.0
	 */
	public static final String TARGET_ID_SUBTYPES_VIEW= CUIPlugin.ID_TYPE_HIERARCHY + ".subtypes"; //$NON-NLS-1$	

	/**
	 * Type hierarchy view part: pop-up menu target ID for the meber viewer
	 * (value <code>"org.eclipse.cdt.ui.TypeHierarchy.members"</code>).
	 * 
	 * @since 2.0
	 */
	public static final String TARGET_ID_MEMBERS_VIEW= CUIPlugin.ID_TYPE_HIERARCHY + ".members"; //$NON-NLS-1$	

	/**
	 * Pop-up menu: name of group for goto actions (value <code>"group.open"</code>).
	 * <p>
	 * Examples for open actions are:
	 * <ul>
	 *  <li>Go Into</li>
	 *  <li>Go To</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_GOTO=		"group.goto"; //$NON-NLS-1$
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
	public static final String GROUP_OPEN=		"group.open"; //$NON-NLS-1$
	
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
	public static final String GROUP_SHOW=		"group.show"; //$NON-NLS-1$
	
	/**
	 * Pop-up menu: name of group for new actions (value <code>"group.new"</code>).
	 * <p>
	 * Examples for new actions are:
	 * <ul>
	 *  <li>Create new class</li>
	 *  <li>Create new interface</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_NEW=		"group.new"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for build actions (value <code>"group.build"</code>).
	 */
	public static final String GROUP_BUILD=		"group.build"; //$NON-NLS-1$
	
	/**
	 * Pop-up menu: name of group for reorganize actions (value <code>"group.reorganize"</code>).
	 */	
	public static final String GROUP_REORGANIZE=	IWorkbenchActionConstants.GROUP_REORGANIZE;	
	
	/**
	 * Pop-up menu: name of group for code generation or refactoring actions (
	 * value <code>"group.generate"</code>).
	 */	
	public static final String GROUP_GENERATE=	"group.generate"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for search actions (value <code>"group.search"</code>).
	 */	
	public static final String GROUP_SEARCH=		"group.search"; //$NON-NLS-1$
	
	/**
	 * Pop-up menu: name of group for additional actions (value <code>"group.additions"</code>).
	 */	
	public static final String GROUP_ADDITIONS=	"additions"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for viewer setup actions (value <code>"group.viewerSetup"</code>).
	 */	
	public static final String GROUP_VIEWER_SETUP=	"group.viewerSetup"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for properties actions (value <code>"group.properties"</code>).
	 */	
	public static final String GROUP_PROPERTIES=	"group.properties"; //$NON-NLS-1$
}

