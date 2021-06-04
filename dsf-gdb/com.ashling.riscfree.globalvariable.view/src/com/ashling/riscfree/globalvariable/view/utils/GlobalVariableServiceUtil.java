/**
 * 
 */
package com.ashling.riscfree.globalvariable.view.utils;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunch;

import com.ashling.riscfree.globalvariable.view.Activator;
import com.ashling.riscfree.globalvariable.view.dsf.IGlobalVariableService;

/**
 * @author vinod.appu
 *
 */
public enum GlobalVariableServiceUtil {
	INSTANCE;
	
	/**
	 * This method track the global variables service from present debug session
	 * @param debugSession
	 * @param selectedDMContext
	 * @param launch
	 * @return
	 */
	public IGlobalVariableService getGlobalVariablesService(DsfSession debugSession, IDMContext selectedDMContext, ILaunch launch)
	{
		DsfServicesTracker tracker = new DsfServicesTracker(Activator.getContext(), debugSession.getId());
		IGlobalVariableService service =  tracker.getService(IGlobalVariableService.class);
		tracker.dispose();
		return service;
	}
}
