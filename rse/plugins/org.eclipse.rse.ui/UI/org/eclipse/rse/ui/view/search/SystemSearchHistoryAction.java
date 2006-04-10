/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view.search;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * This is the history action for the remote system search view.
 */
public class SystemSearchHistoryAction extends Action {


	
	private SystemSearchViewPart searchView;
	private int index;

	/**
	 * Constructor for SystemSearchHistoryAction.
	 * @param text the text for the action.
	 * @param image the image.
	 * @param searchView the search view.
	 * @param index the index in the history.
	 */
	public SystemSearchHistoryAction(String text, ImageDescriptor image, SystemSearchViewPart searchView, int index) {
		super(text, image);
		setToolTipText(text);
		this.searchView = searchView;
		this.index = index;
	}
	
	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		searchView.showSearchResult(index);		
	}
	
	/**
	 * Sets the text and the tooltip.
	 * @see org.eclipse.jface.action.IAction#setText(java.lang.String)
	 */
	public void setText(String text) {
		super.setText(text);
		setToolTipText(text);
	}
}