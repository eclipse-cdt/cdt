/**
 *
 */
package com.ashling.riscfree.globalvariable.view.dsf;

import java.util.List;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMData;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.debug.core.DebugException;

import com.ashling.riscfree.globalvariable.view.datamodel.IGlobalVariableDMContext;
import com.ashling.riscfree.globalvariable.view.datamodel.IGlobalVariableDescriptor;

/**
 * @author vinod.appu
 *
 */
public interface IGlobalVariableService extends IDsfService {

	/**
	 * Get global variables from the given context (frameDmc)
	 * 
	 * @param frameDmc
	 * @param rm
	 */
	public void getGlobals(final IFrameDMContext frameDmc, final DataRequestMonitor<IGlobalVariableDMContext[]> rm);

	/**
	 * Get the data for global variable from given DMC
	 * 
	 * @param variableDmc
	 * @param rm
	 */
	public void getGlobalVariableData(IGlobalVariableDMContext variableDmc, final DataRequestMonitor<IVariableDMData> rm);

	/**
	 * Add a global variable to the service, so that it can be tracked
	 * 
	 * @param frameDmc
	 * @param globals
	 */
	public void addGlobals(IDMContext frameDmc, IGlobalVariableDescriptor[] globals);

	/**
	 * Remove a global variables from the view
	 * 
	 * @param frameDmc
	 * @param globals
	 */
	public void removeGlobals(IDMContext frameDmc, IExpressionDMContext[] globals);

	/**
	 * Remove a global variables from the view
	 * 
	 * @param frameDmc
	 * @param globals
	 */
	public void removeGlobals(IDMContext frameDmc, IGlobalVariableDescriptor[] globals);

	/**
	 * Get global variable descriptors tracked
	 * 
	 * @return
	 * @throws DebugException
	 */
	public List<IGlobalVariableDescriptor> getGlobals() throws DebugException;

	/**
	 * Get initial list of global variables from the debugger
	 * 
	 * @param frameDmc
	 * @return
	 * @throws DebugException
	 */
	public void getInitialGlobals(IDMContext frameDmc, DataRequestMonitor<IGlobalVariableDescriptor[]> dataRequestMonitor) throws DebugException;

	/**
	 * Remove all the global variables tracked
	 * 
	 * @param frameDmc
	 */
	void removeAllGlobals(IDMContext frameDmc);
}
