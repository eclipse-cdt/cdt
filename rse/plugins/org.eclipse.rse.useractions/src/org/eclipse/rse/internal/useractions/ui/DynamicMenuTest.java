/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.internal.useractions.ui;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.actions.CompoundContributionItem;

public class DynamicMenuTest extends CompoundContributionItem 
{
	private class TestContribution extends ContributionItem {
		public void fill(Menu menu, int index) 
		{
			MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
			menuItem.setText("My First Contribution");
		}
	}

	protected IContributionItem[] getContributionItems() {
		   // Here's where you would dynamically generate your list
        IContributionItem[] list = new IContributionItem[1];
        
        list[0] = new TestContribution();
  
		
		//IAction searchAction = new SystemSearchAction(null);
	//	ActionContributionItem item = new ActionContributionItem(searchAction);
	//	list[1] = item;
        return list;
	}

}
