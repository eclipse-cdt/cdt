package org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.view.EpiphanyVisualizer;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DSFDebugModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DSFSessionState;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.IHardwareTargetDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2.IResourcesInformation;
import org.eclipse.cdt.dsf.service.DsfSession;

@SuppressWarnings("restriction")
public class DSFDebugModelEpiphany extends DSFDebugModel{

	/** Request load information for all eCores of an Epiphany chip */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public static void getCoreLoad(DSFSessionState sessionState,
			final DSFDebugModelListenerEpiphany listener,
			final Object arg)
	{
		IGDBHardwareAndOS2 hwService = sessionState.getService(IGDBHardwareAndOS2.class);
		
		if (hwService == null) {
			listener.getLoadDone(null, null, arg);
			return;
		}
		ICommandControlService controlService = sessionState.getService(ICommandControlService.class);
		final IHardwareTargetDMContext contextToUse = DMContexts.getAncestorOfType(controlService.getContext(),
                IHardwareTargetDMContext.class);
		
		final DsfExecutor executor = DsfSession.getSession(sessionState.getSessionID()).getExecutor();
		final String resourceClass = "load";
		
		hwService.getResourcesInformation(contextToUse, resourceClass, new DataRequestMonitor<IResourcesInformation>(executor, null) {

			@Override
			@ConfinedToDsfExecutor("fExecutor")
			protected void handleCompleted() {
				IResourcesInformation resourceInfo = isSuccess() ? getData() : null;
				listener.getCoreLoadDone(contextToUse, resourceInfo, arg);
			}
		});

	}

	public static void getTrafficLoad(DSFSessionState sessionState,
			final DSFDebugModelListenerEpiphany listener,
			final Object arg)
	{
		IGDBHardwareAndOS2 hwService = sessionState.getService(IGDBHardwareAndOS2.class);
		
		if (hwService == null) {
			listener.getLoadDone(null, null, arg);
			return;
		}
		ICommandControlService controlService = sessionState.getService(ICommandControlService.class);
		final IHardwareTargetDMContext contextToUse = DMContexts.getAncestorOfType(controlService.getContext(),
                IHardwareTargetDMContext.class);
		
		final DsfExecutor executor = DsfSession.getSession(sessionState.getSessionID()).getExecutor();
		final String resourceClass = "traffic";
		
		hwService.getResourcesInformation(contextToUse, resourceClass, new DataRequestMonitor<IResourcesInformation>(executor, null) {

			@Override
			@ConfinedToDsfExecutor("fExecutor")
			protected void handleCompleted() {
				IResourcesInformation resourceInfo = isSuccess() ? getData() : null;
				listener.getTrafficLoadDone(contextToUse, resourceInfo, arg);
			}
		});
	}
}
