package org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DSFDebugModelListener;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2.IResourcesInformation;

@SuppressWarnings("restriction")
public interface DSFDebugModelListenerEpiphany extends DSFDebugModelListener {

	public void getCoreLoadDone(IDMContext context, IResourcesInformation loads, Object arg);
	public void getTrafficLoadDone(IDMContext context, IResourcesInformation loads, Object arg);

	/**
	 * Invoked when the load timer triggers
	 */
	public void updateLoads();
}
