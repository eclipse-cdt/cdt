package org.eclipse.cdt.dsf.gdb.service;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.MIMemory;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 4.2
 */
public class GDBMemory extends MIMemory implements IGDBMemory {

	private IGDBControl fCommandControl;

	/**
	 * Cache address sizes for each memory context. 
	 */
	private Map<IMemoryDMContext, Integer> fAddressSizes = new HashMap<IMemoryDMContext, Integer>();

	public GDBMemory(DsfSession session) {
		super(session);
	}

	@Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize(new ImmediateRequestMonitor(requestMonitor) {
            @Override
            protected void handleSuccess() {
                doInitialize(requestMonitor);
            }
        });
    }

    private void doInitialize(final RequestMonitor requestMonitor) {
    	fCommandControl = getServicesTracker().getService(IGDBControl.class);
    	requestMonitor.done();
    }

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		fAddressSizes.clear();
		super.shutdown(requestMonitor);
	}

	@Override
	public void getAddressSize(final IMemoryDMContext context, final DataRequestMonitor<Integer> rm) {
		Integer addrSize = fAddressSizes.get(context);
		if (addrSize != null) {
			rm.setData(addrSize);
			rm.done();
			return;
		}
		
		CommandFactory commandFactory = fCommandControl.getCommandFactory();
		fCommandControl.queueCommand(
			commandFactory.createMIDataEvaluateExpression(
				fCommandControl.getContext(), 
				"sizeof (void*)"),  //$NON-NLS-1$
			new DataRequestMonitor<MIDataEvaluateExpressionInfo>(getExecutor(), rm) {
				@Override
				@ConfinedToDsfExecutor("fExecutor")
				protected void handleSuccess() {
					try {
						rm.setData(Integer.decode(getData().getValue()));
						fAddressSizes.put(context, rm.getData());
					}
					catch(NumberFormatException e) {
						rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, String.format("Invalid address size: %s", getData().getValue()))); //$NON-NLS-1$
					}
					rm.done();
				}
			});
	}
}
