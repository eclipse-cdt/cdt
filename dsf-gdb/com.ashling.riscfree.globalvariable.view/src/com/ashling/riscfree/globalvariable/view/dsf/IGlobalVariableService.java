/**
 *
 */
package com.ashling.riscfree.globalvariable.view.dsf;

import java.util.List;

import org.eclipse.cdt.debug.core.model.IGlobalVariableDescriptor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMData;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMData;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.debug.core.DebugException;

/**
 * @author vinod.appu
 *
 */
public interface IGlobalVariableService extends IDsfService {

	/**
	 * Variable context. This context only provides access to limited expression
	 * information. For displaying complete information, Expressions service should
	 * be used.
	 */
	public interface IGlobalVariableDMContext extends IDMContext {
	}

	/**
	 * Stack frame variable information.
	 */
	public interface IGlobalVariableDMData extends IDMData {
		String getName();

		String getValue();
	}

	/**
	 * Parse and get global variable descriptors
	 * 
	 * @param rm
	 */
	void getGlobalVariables(DataRequestMonitor<IGlobalVariableDescriptor[]> rm);

	/**
	 * Get global variables from the given context (frameDmc)
	 * 
	 * @param frameDmc
	 * @param rm
	 */
	public void getGlobals(final IFrameDMContext frameDmc, final DataRequestMonitor<IVariableDMContext[]> rm);

	/**
	 * Get the data for global variable from given DMC
	 * 
	 * @param variableDmc
	 * @param rm
	 */
	public void getGlobalVariableData(IVariableDMContext variableDmc, final DataRequestMonitor<IVariableDMData> rm);

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
	public List<IGlobalVariableDescriptor> getInitialGlobals(IDMContext frameDmc) throws DebugException;

	/**
	 * Remove all the global variables tracked
	 * 
	 * @param frameDmc
	 */
	void removeAllGlobals(IDMContext frameDmc);
}
