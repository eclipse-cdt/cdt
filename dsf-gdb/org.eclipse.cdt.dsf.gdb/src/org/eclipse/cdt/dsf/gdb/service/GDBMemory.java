package org.eclipse.cdt.dsf.gdb.service;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.MIMemory;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIShowEndianInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.MemoryByte;

/**
 * @since 4.2
 */
public class GDBMemory extends MIMemory implements IGDBMemory {

	private IGDBControl fCommandControl;

	/**
	 * Cache of the address sizes for each memory context. 
	 */
	private Map<IMemoryDMContext, Integer> fAddressSizes = new HashMap<IMemoryDMContext, Integer>();

	/**
	 * We assume the endianness is the same for all processes because GDB supports only one target.
	 */
	private boolean fIsBigEndian = false;

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
		getSession().addServiceEventListener(this, null);
		register(
			new String[] { 
				IMemory.class.getName(),
				MIMemory.class.getName(),
				IGDBMemory.class.getName(),
				GDBMemory.class.getName(),
			},
			new Hashtable<String, String>());
		requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();
    	getSession().removeServiceEventListener(this);
		fAddressSizes.clear();
		super.shutdown(requestMonitor);
	}

	@Override
	protected void readMemoryBlock(final IDMContext dmc, IAddress address, 
		long offset, int word_size, int count, final DataRequestMonitor<MemoryByte[]> drm) {
		super.readMemoryBlock(
			dmc, 
			address, 
			offset, 
			word_size, 
			count, 
			new DataRequestMonitor<MemoryByte[]>(ImmediateExecutor.getInstance(), drm) {
				@Override
				@ConfinedToDsfExecutor("fExecutor")
				protected void handleSuccess() {
					IMemoryDMContext memDmc = DMContexts.getAncestorOfType(dmc, IMemoryDMContext.class);
					if (memDmc != null) {
						boolean bigEndian = isBigEndian(memDmc);
						for (MemoryByte b : getData()) {
							b.setBigEndian(bigEndian);
							b.setEndianessKnown(true);
						}
					}
					drm.setData(getData());
					drm.done();
				}
			});
	}

	@DsfServiceEventHandler
	public void eventDispatched(IStartedDMEvent event) {
		final IMemoryDMContext memContext = DMContexts.getAncestorOfType(event.getDMContext(), IMemoryDMContext.class);
		final IMIExecutionDMContext execContext = DMContexts.getAncestorOfType(event.getDMContext(), IMIExecutionDMContext.class);
		if (memContext != null && execContext != null) {
			getExecutor().execute(new DsfRunnable() {				
				@Override
				public void run() {
					readAddressSize(memContext, execContext);
					readEndianness(memContext, execContext);
				}
			});
		}
	}

	@DsfServiceEventHandler
	public void eventDispatched(IExitedDMEvent event) {
		IMemoryDMContext context = DMContexts.getAncestorOfType(event.getDMContext(), IMemoryDMContext.class);
		if (context != null) {
			fAddressSizes.remove(context);
		}
	}

	@Override
	public int getAddressSize(IMemoryDMContext context) {
		Integer addressSize = fAddressSizes.get(context);
		return (addressSize != null) ? addressSize.intValue() : 8;
	}

	@Override
	public boolean isBigEndian(IMemoryDMContext context) {
		return fIsBigEndian;
	}

	/**
	 * Retrieves the address size for given memory and execution contexts. 
	 */
	private void readAddressSize(final IMemoryDMContext memContext, IMIExecutionDMContext execContext) {
		Integer addrSize = fAddressSizes.get(memContext);
		if (addrSize == null) {
			doReadAddressSize(
				memContext, 
				execContext, 
				new DataRequestMonitor<Integer>(getExecutor(), null) {
					@Override
					@ConfinedToDsfExecutor("fExecutor")
					protected void handleSuccess() {
						fAddressSizes.put(memContext, getData());
					}
				});
		}
	}

	/**
	 * Retrieves the endianness for given memory and execution contexts. 
	 */
	private void readEndianness(IMemoryDMContext memContext, IMIExecutionDMContext execContext) {
		doReadEndianness(
			memContext, 
			execContext,
			new DataRequestMonitor<Boolean>(getExecutor(), null) {
				@Override
				@ConfinedToDsfExecutor("fExecutor")
				protected void handleSuccess() {
					fIsBigEndian = getData();
				}
			});
	}

	protected void doReadAddressSize(IMemoryDMContext memContext, IMIExecutionDMContext execContext, final DataRequestMonitor<Integer> drm) {
		CommandFactory commandFactory = fCommandControl.getCommandFactory();
		fCommandControl.queueCommand(
			commandFactory.createMIDataEvaluateExpression(
				execContext, 
				"sizeof (void*)"),  //$NON-NLS-1$
			new DataRequestMonitor<MIDataEvaluateExpressionInfo>(getExecutor(), drm) {
				@Override
				@ConfinedToDsfExecutor("fExecutor")
				protected void handleSuccess() {
					try {
						drm.setData(Integer.decode(getData().getValue()));
					}
					catch(NumberFormatException e) {
						drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, String.format("Invalid address size: %s", getData().getValue()))); //$NON-NLS-1$
					}
					drm.done();
				}
			});
	}

	protected void doReadEndianness(IMemoryDMContext memContext, IMIExecutionDMContext execContext, final DataRequestMonitor<Boolean> drm) {
		CommandFactory commandFactory = fCommandControl.getCommandFactory();
		fCommandControl.queueCommand(
			commandFactory.createCLIShowEndian(fCommandControl.getContext()),
			new DataRequestMonitor<CLIShowEndianInfo>(getExecutor(), drm) {
				@Override
				@ConfinedToDsfExecutor("fExecutor")
				protected void handleSuccess() {
					drm.setData(Boolean.valueOf(getData().isBigEndian()));
					drm.done();
				}
			});
	}
}
