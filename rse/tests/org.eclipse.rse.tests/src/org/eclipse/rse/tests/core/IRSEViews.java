/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.tests.core;

/**
 * Defines the several unique shared RSE view ids.
 */
public interface IRSEViews {

	/**
	 * The unique remote systems perspective id.
	 */
	public final String RSE_PERSPECTIVE_ID = "org.eclipse.rse.ui.view.SystemPerspective"; //$NON-NLS-1$

	/**
	 * The unique remote systems view id.
	 */
	public final String RSE_REMOTE_SYSTEMS_VIEW_ID = "org.eclipse.rse.ui.view.systemView"; //$NON-NLS-1$

	/**
	 * The unique remote team view id.
	 */
	public final String RSE_TEAM_VIEW_ID = "org.eclipse.rse.ui.view.teamView"; //$NON-NLS-1$

	/**
	 * The unique remote system details view id.
	 */
	public final String RSE_REMOTE_SYSTEMS_DETAILS_VIEW_ID = "org.eclipse.rse.ui.view.systemTableView"; //$NON-NLS-1$

	/**
	 * The unique remote search view id.
	 */
	public final String RSE_REMOTE_SEARCH_VIEW_ID = "org.eclipse.rse.ui.view.SystemSearchView"; //$NON-NLS-1$

	/**
	 * The unique remote scratchpad view id.
	 */
	public final String RSE_REMOTE_SCRATCHPAD_VIEW_ID = "org.eclipse.rse.ui.view.scratchpad.SystemScratchpadViewPart"; //$NON-NLS-1$

	/**
	 * The unique remote monitor view id.
	 */
	public final String RSE_REMOTE_MONITOR_VIEW_ID = "org.eclipse.rse.ui.view.monitorView"; //$NON-NLS-1$
}
