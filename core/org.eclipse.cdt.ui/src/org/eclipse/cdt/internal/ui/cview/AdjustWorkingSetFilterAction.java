package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.jface.action.Action;

/**
 * @author ThomasF
 *
 * Set a manager with a specific filter type/working set
 */
public class AdjustWorkingSetFilterAction extends Action {
	CWorkingSetFilter fFilter;
	String 			  fName;
				
	public AdjustWorkingSetFilterAction(String name, String setName, CWorkingSetFilter filter) {
		super(name);
		fName = setName;	
		fFilter = filter;
	}
		
	public void run() {
		if(fFilter == null) {
			return;
		}
		
		fFilter.setWorkingSetName(fName);
	}
}
