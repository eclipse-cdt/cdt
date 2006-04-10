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

package org.eclipse.rse.ui.filters.dialogs;

/**
 * Much of the new filter wizard is configurable, especially with respect to translated strings.
 * While there exists setters and overridable methods for most of it, sometimes that gets overwhelming.
 * This interface is designed to capture all the configurable attributes that are not likely to change
 * from usage to usage of the wizard (eg, not context sensitive) such that for convenience you can 
 * implement it in a class and instantiate a singleton instance of that class to re-use for your
 * wizard.
 * <p>
 * Your best option is to subclass {@link SystemNewFilterWizardConfigurator} and override just those
 * things you want to change.
 */
public interface ISystemNewFilterWizardConfigurator 
{
	
	/**
	 * Return the default page title to use for each page, unless overridden individually
	 */
	public String getPageTitle();
	/**
	 * Return the page title for page 1 (which prompts for the filter string)
	 */
	public String getPage1Title();	
	/**
	 * Return the description for page 1 (which prompts for the filter string)
	 */
	public String getPage1Description();	
	
	/**
	 * Return the page title for page 2 (which prompts for the name and filter pool)
	 */
	public String getPage2Title();	
	/**
	 * Return the description for page 2 (which prompts for the name and filter pool)
	 */
	public String getPage2Description();		
	/**
	 * Return the help ID for page 2
	 */
	public String getPage2HelpID();
	/**
	 * Return the verbage for the name prompt on page 2
	 */
	public String getPage2NameVerbage();
	/**
	 * Return the verbage for the pool prompt on page 3
	 */
	public String getPage2PoolVerbage();
	/**
	 * Return the verbage tooltip for the name prompt on page 2
	 */
	public String getPage2PoolVerbageTip();
	/**
	 * Return the label for the filter name
	 *  prompt on page 2.
	 */
	public String getPage2NamePromptLabel();
	
	/**
	 * Return the tooltip for the filter name
	 *  prompt on page 2.
	 */
	public String getPage2NamePromptTooltip();
	
	/**
	 * Return the label  for the filter pool
	 *  prompt on page 2.
	 */
	public String getPage2PoolPromptLabel();
	
	/**
	 * Return the label  for the filter pool
	 *  prompt on page 2.
	 */
	public String getPage2PoolPromptTooltip();
	
	/**
	 * Get the "Unique to this connection" checkbox label 
	 */
	public String getPage2UniqueToConnectionLabel();
	/**
	 * Set the "Unique to this connection" checkbox tooltip
	 */
	public String getPage2UniqueToConnectionToolTip();
		
	/**
	 * Return the page title for page 3 (which shows 2 tips)
	 */
	public String getPage3Title();	
	/**
	 * Return the description for page 3 (which shows 2 tips)
	 */
	public String getPage3Description();		
	/**
	 * Return the first tip on page 3
	 */
	public String getPage3Tip1();
	/**
	 * Return the second tip on page 3
	 */
	public String getPage3Tip2();
	
	
}