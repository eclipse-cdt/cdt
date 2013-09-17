/*******************************************************************************
 * Copyright (c) 2013 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Mentor Graphics - Initial API and implementation
 * 		John Dallaway - Add methods to get the endianness and address size (Bug 225609) 
 * 		Philippe Gil (AdaCore) - Switch to c language when getting sizeof(void *) when required (Bug 421541)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.MIMemory;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIShowEndianInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBShowLanguageInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
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
	private Boolean fIsBigEndian;

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

	@Override
	public void initializeMemoryData(final IMemoryDMContext memContext, RequestMonitor rm) {

		ImmediateExecutor.getInstance().execute(new Sequence(getExecutor(), rm) {

			private String originalLanguage = MIGDBShowLanguageInfo.AUTO;

			// Need a global here as getSteps() can be called more than once.
			private Step[] steps = null;
			
			private void determineSteps()
			{
				ArrayList<Step> stepsList = new ArrayList<Step>();

				if (fAddressSizes.get(memContext) == null) {
					stepsList.add(
						new Step() {
							// store original language
							@Override
							public void execute(final RequestMonitor requestMonitor) {
								fCommandControl.queueCommand(
									fCommandControl.getCommandFactory().createMIGDBShowLanguage(memContext),
									new ImmediateDataRequestMonitor<MIGDBShowLanguageInfo>(requestMonitor) {
										@Override
										@ConfinedToDsfExecutor("fExecutor")
										protected void handleSuccess() {
											originalLanguage = getData().getLanguage();
											requestMonitor.done();
										}
									});
							}
						});
					stepsList.add(
						new Step() {
							// switch to c language
							@Override
							public void execute(final RequestMonitor requestMonitor) {
								fCommandControl.queueCommand(
									fCommandControl.getCommandFactory().createMIGDBSetLanguage(memContext, MIGDBShowLanguageInfo.C),
									new ImmediateDataRequestMonitor<MIInfo>(requestMonitor));
							}
						});

					stepsList.add(
						new Step() {
							// read address size
							@Override
							public void execute(final RequestMonitor requestMonitor) {
								readAddressSize(
									memContext, 
									new ImmediateDataRequestMonitor<Integer>(requestMonitor) {
										@Override
										@ConfinedToDsfExecutor("fExecutor")
										protected void handleSuccess() {
											fAddressSizes.put(memContext, getData());
											requestMonitor.done();
										}
									});
							}
						});

					stepsList.add(
							new Step() {
								// restore original language
								@Override
								public void execute(final RequestMonitor requestMonitor) {
									fCommandControl.queueCommand(
										fCommandControl.getCommandFactory().createMIGDBSetLanguage(memContext, originalLanguage),
										new ImmediateDataRequestMonitor<MIInfo>(requestMonitor));
								}
							});

					}

				if (fIsBigEndian == null) {
					stepsList.add(
						new Step() {
							// read endianness
							@Override
							public void execute(final RequestMonitor requestMonitor) {
								readEndianness(
										memContext, 
										new ImmediateDataRequestMonitor<Boolean>(requestMonitor) {
											@Override
											@ConfinedToDsfExecutor("fExecutor")
											protected void handleSuccess() {
												fIsBigEndian = getData();
												requestMonitor.done();
											}
										});
								}
							});
				}
				
				steps = stepsList.toArray(new Step[stepsList.size()]);
			}

			@Override
			public Step[] getSteps() {
				if (steps == null) {
					determineSteps();
				}
				
				return steps;
			}
		});
	}

	@DsfServiceEventHandler
	public void eventDispatched(IExitedDMEvent event) {
		if (event.getDMContext() instanceof IContainerDMContext) {
			IMemoryDMContext context = DMContexts.getAncestorOfType(event.getDMContext(), IMemoryDMContext.class);
			if (context != null) {
				fAddressSizes.remove(context);
			}
		}
	}

	@Override
	public int getAddressSize(IMemoryDMContext context) {
		Integer addressSize = fAddressSizes.get(context);
		return (addressSize != null) ? addressSize.intValue() : 8;
	}

	@Override
	public boolean isBigEndian(IMemoryDMContext context) {
		assert fIsBigEndian != null;
		if (fIsBigEndian == null) {
    		GdbPlugin.log(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Endianness was never initialized!")); //$NON-NLS-1$
			return false;
		}
		return fIsBigEndian;
	}

	protected void readAddressSize(IMemoryDMContext memContext, final DataRequestMonitor<Integer> drm) {
		IExpressions exprService = getServicesTracker().getService(IExpressions.class);
		IExpressionDMContext exprContext = exprService.createExpression(memContext, "sizeof (void*)"); //$NON-NLS-1$
		CommandFactory commandFactory = fCommandControl.getCommandFactory();
		fCommandControl.queueCommand(
			commandFactory.createMIDataEvaluateExpression(exprContext),
			new DataRequestMonitor<MIDataEvaluateExpressionInfo>(ImmediateExecutor.getInstance(), drm) {
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

	protected void readEndianness(IMemoryDMContext memContext, final DataRequestMonitor<Boolean> drm) {
		CommandFactory commandFactory = fCommandControl.getCommandFactory();
		fCommandControl.queueCommand(
			commandFactory.createCLIShowEndian(memContext),
			new DataRequestMonitor<CLIShowEndianInfo>(ImmediateExecutor.getInstance(), drm) {
				@Override
				@ConfinedToDsfExecutor("fExecutor")
				protected void handleSuccess() {
					drm.setData(Boolean.valueOf(getData().isBigEndian()));
					drm.done();
				}
			});
	}
}
