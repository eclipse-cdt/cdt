/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import java.util.List;

/**
 * Interface should be implemented by some visible object
 * (usually - 1st page in CDT New Project wizard) 
 * to be informed about changes in tool chains selection 
 * performed by ICNewWizard implementors.
 */
public interface IWizardItemsListListener {
	/**
	 * Called by ICNewWizard instance when user has changed tool chains selection.
	 *  
	 * @param count - number of selected toolchains.
	 */
	void toolChainListChanged(int count);

	/**
	 * @return true if this page is visible 
	 */
	boolean isCurrent();

	/**
	 * Ability to filter items which would be shown in the left pane of Main Wizard Page.
	 * Standard implementation: returns unchanged list. When filtering, please note that
	 * the items have hierarchical relations with others, so removing some item(s) can
	 * break a tree. 
	 *
	 * @param items - list of EntryDescriptor objects 
	 * @return - list with filtered items
	 */
	List<EntryDescriptor> filterItems(List<EntryDescriptor> items);
}
