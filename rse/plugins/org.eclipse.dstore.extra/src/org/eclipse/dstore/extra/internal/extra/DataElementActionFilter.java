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

package org.eclipse.dstore.extra.internal.extra;

public class DataElementActionFilter implements org.eclipse.ui.IActionFilter {


	// constants to be used by Eclipse Filtering and Enablement Support.
	private static String _type = "type";
	private static String _name = "name";
	private static DataElementActionFilter _instance;

	public static DataElementActionFilter getInstance() {
		if (_instance == null)
			_instance = new DataElementActionFilter();
		return _instance;
	}

	/**
	 * Supports Eclipse filtering and enablement.
	 * eg:<code>
	 * <extension point="org.eclipse.rse.ui.popupMenus">
	 *	  <objectContribution id="xyz.perspective.rse.actions.createprojectcontribution"         
	 *                 subsytemfactoryid="xyz.files"
	 *                 typecategoryfilter="LIBRARIES">
	 *  	 <action id="xyz.perspective.rse.actions.createproject"
	 *            label="%create_xyz_project"
	 *            tooltip="%create_xyz_project"
	 *            helpContextId="xyz.perspective.rse01"
	 *            enablesFor="1"
	 *            class="xyz.rse.actions.RSECreateXyzProjectAction">
	 *           <enablement>              	    
	 *     	    <not> 
	 *   	             <objectState name="name" value="SPECIAL*"/> 
	 *	          </not>            
	 *           </enablement> 
	 *	        </action>
	 *	 </objectContribution>
	 *  </extension>
	 *	</code>
	 * 
	 * The above contribution uses the RSE pop-up extension point to contribute an action 
	 * to any single RSE object but not anything beginning with SPECIAL.
	 * @see IDataElementActionFilter#testAttribute(Object, String, String)
	 */
	public boolean testAttribute(Object target, String name, String value) {
		if (name.equals(_type) && target instanceof IDataElement) {
			// support for "type" filter
			IDataElement le = (IDataElement) target;
			if (le.getType().equals(value) || le.isOfType(value))
				return true;
		} else if (name.equals(_name) && target instanceof IDataElement) {
			// support for "name" filter.
			IDataElement le = (IDataElement) target;
			if (value.endsWith("*")) {
				// we have a wild card test, and * is the last character in the value
				if (le
					.getName()
					.startsWith(value.substring(0, value.length() - 1)))
					return true;
			} else if (le.getName().equals(value))
				return true;
		}

		// type and name filter do not match, or we have a filter we do not support.
		return false;
	}

	public static boolean matches(Class aClass) {
		return (aClass == org.eclipse.ui.IActionFilter.class);
	}

}