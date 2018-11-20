/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence.Step;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpointsExtension;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.ITracepointAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.TracepointActionManager;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.CLITraceInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Breakpoint service for GDB 7.0.
 * It also supports tracepoints

 * @since 3.0
 */
public class GDBBreakpoints_7_0 extends MIBreakpoints {
	private ICommandControl fConnection;
	private IMIRunControl fRunControl;
	private CommandFactory fCommandFactory;

	public GDBBreakpoints_7_0(DsfSession session) {
		super(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#initialize(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new ImmediateRequestMonitor(rm) {
			@Override
			protected void handleSuccess() {
				doInitialize(rm);
			}
		});
	}

	private void doInitialize(final RequestMonitor rm) {
		// Get the services references
		fConnection = getServicesTracker().getService(ICommandControl.class);
		fRunControl = getServicesTracker().getService(IMIRunControl.class);
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		// Register this service
		register(
				new String[] { IBreakpoints.class.getName(), IBreakpointsExtension.class.getName(),
						MIBreakpoints.class.getName(), GDBBreakpoints_7_0.class.getName() },
				new Hashtable<String, String>());

		rm.done();
	}

	@Override
	protected String adjustWatchPointExpression(final Map<String, Object> attributes, String origExpression) {
		String adjustedExpression = origExpression;
		if (!origExpression.isEmpty()) {
			// Resolve the address range
			String sRange = (String) getProperty(attributes, RANGE, NULL_STRING);
			int addressRange = 0;
			if (!sRange.equals(NULL_STRING)) {
				try {
					addressRange = Integer.valueOf(sRange);
				} catch (NumberFormatException e) {
					// No expression adjustment for an unexpected format
				}
			}

			if (addressRange > 1 && Character.isDigit(origExpression.charAt(0))) {
				// Monitoring a range of addresses,
				// The following line formats the string to a valid GDB expression
				// i.e. casting to an array of char with the size given by range
				// Taking the size of the character type to resolve the addressable size
				adjustedExpression = String.format("*((char (*)[ %d ]) %s)", addressRange, origExpression); //$NON-NLS-1$
			} else if (Character.isDigit(origExpression.charAt(0))) {
				// If expression is a single address, we need the '*' prefix.
				adjustedExpression = "*" + origExpression; //$NON-NLS-1$
			}
		}
		return adjustedExpression;
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();
		super.shutdown(requestMonitor);
	}

	/**
	 * Add a breakpoint of type BREAKPOINT.
	 * With GDB 7.0, we can create a breakpoint that is disabled.  This avoids having to disable it as
	 * a separate command.  It is also much better because in non-stop, we don't risk habing a thread
	 * hitting the breakpoint between creation and disablement.
	 *
	 * @param context
	 * @param breakpoint
	 * @param finalRm
	 */
	@Override
	protected void addBreakpoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes,
			final DataRequestMonitor<IBreakpointDMContext> finalRm) {
		// Select the context breakpoints map
		final Map<String, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
			finalRm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
			finalRm.done();
			return;
		}

		// Extract the relevant parameters (providing default values to avoid potential NPEs)
		final String location = formatLocation(attributes);
		if (location.equals(NULL_STRING)) {
			finalRm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
			finalRm.done();
			return;
		}

		final Boolean enabled = (Boolean) getProperty(attributes, MIBreakpoints.IS_ENABLED, true);
		final Boolean isTemporary = (Boolean) getProperty(attributes, MIBreakpointDMData.IS_TEMPORARY, false);
		final Boolean isHardware = (Boolean) getProperty(attributes, MIBreakpointDMData.IS_HARDWARE, false);
		final String condition = (String) getProperty(attributes, MIBreakpoints.CONDITION, NULL_STRING);
		final Integer ignoreCount = (Integer) getProperty(attributes, MIBreakpoints.IGNORE_COUNT, 0);
		String threadId = (String) getProperty(attributes, MIBreakpointDMData.THREAD_ID, "0"); //$NON-NLS-1$

		final Step insertBreakpointStep = new Step() {
			@Override
			public void execute(final RequestMonitor rm) {
				// Execute the command
				fConnection.queueCommand(
						fCommandFactory.createMIBreakInsert(context, isTemporary, isHardware, condition, ignoreCount,
								location, threadId, !enabled, false),
						new DataRequestMonitor<MIBreakInsertInfo>(getExecutor(), rm) {
							@Override
							protected void handleSuccess() {

								// With MI, an invalid location won't generate an error
								if (getData().getMIBreakpoints().length == 0) {
									rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED,
											BREAKPOINT_INSERTION_FAILURE, null));
									rm.done();
									return;
								}

								// Create a breakpoint object and store it in the map
								final MIBreakpointDMData newBreakpoint = createMIBreakpointDMData(
										getData().getMIBreakpoints()[0]);
								String reference = newBreakpoint.getNumber();
								if (reference.isEmpty()) {
									rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED,
											BREAKPOINT_INSERTION_FAILURE, null));
									rm.done();
									return;
								}
								contextBreakpoints.put(reference, newBreakpoint);

								// Format the return value
								MIBreakpointDMContext dmc = new MIBreakpointDMContext(GDBBreakpoints_7_0.this,
										new IDMContext[] { context }, reference);
								finalRm.setData(dmc);

								// Flag the event
								getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());

								rm.done();
							}

							@Override
							protected void handleError() {
								rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED,
										BREAKPOINT_INSERTION_FAILURE, getStatus().getException()));
								rm.done();
							}
						});
			}
		};

		fRunControl.executeWithTargetAvailable(context, new Step[] { insertBreakpointStep }, finalRm);
	}

	/**
	 * Add a tracepoint
	 *
	 * @param context
	 * @param breakpoint
	 * @param drm
	 */
	@Override
	protected void addTracepoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes,
			final DataRequestMonitor<IBreakpointDMContext> drm) {
		// Select the context breakpoints map
		final Map<String, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
			drm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
			drm.done();
			return;
		}

		// Extract the relevant parameters (providing default values to avoid potential NPEs)
		final String location = formatLocation(attributes);
		if (location.equals(NULL_STRING)) {
			drm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
			drm.done();
			return;
		}

		final String condition = (String) getProperty(attributes, MIBreakpoints.CONDITION, NULL_STRING);

		fConnection.queueCommand(fCommandFactory.createCLITrace(context, location, condition),
				new DataRequestMonitor<CLITraceInfo>(getExecutor(), drm) {
					@Override
					protected void handleSuccess() {
						final String tpReference = getData().getTraceReference();
						if (tpReference == null) {
							drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED,
									BREAKPOINT_INSERTION_FAILURE, null));
							drm.done();
							return;
						}

						// The simplest way to convert from the CLITraceInfo to a MIBreakInsertInfo
						// is to list the breakpoints and take the proper output
						fConnection.queueCommand(fCommandFactory.createMIBreakList(context),
								new DataRequestMonitor<MIBreakListInfo>(getExecutor(), drm) {
									@Override
									protected void handleSuccess() {
										MIBreakpoint[] breakpoints = getData().getMIBreakpoints();
										for (MIBreakpoint bp : breakpoints) {
											if (bp.getNumber().equals(tpReference)) {

												// Create a breakpoint object and store it in the map
												final MIBreakpointDMData newBreakpoint = createMIBreakpointDMData(bp);
												String reference = newBreakpoint.getNumber();
												contextBreakpoints.put(reference, newBreakpoint);

												// Format the return value
												MIBreakpointDMContext dmc = new MIBreakpointDMContext(
														GDBBreakpoints_7_0.this, new IDMContext[] { context },
														reference);
												drm.setData(dmc);

												// Flag the event
												getSession().dispatchEvent(new BreakpointAddedEvent(dmc),
														getProperties());

												// By default the tracepoint is enabled at creation
												// If it wasn't supposed to be, then disable it right away
												// Also, tracepoints are created with no passcount.
												// We have to set the passcount manually now.
												// Same for commands.
												Map<String, Object> delta = new HashMap<>();
												delta.put(MIBreakpoints.IS_ENABLED,
														getProperty(attributes, MIBreakpoints.IS_ENABLED, true));
												delta.put(MIBreakpoints.PASS_COUNT,
														getProperty(attributes, MIBreakpoints.PASS_COUNT, 0));
												delta.put(MIBreakpoints.COMMANDS,
														getProperty(attributes, MIBreakpoints.COMMANDS, "")); //$NON-NLS-1$
												modifyBreakpoint(dmc, delta, drm, false);
												return;
											}
										}
										drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED,
												BREAKPOINT_INSERTION_FAILURE, null));
										drm.done();
									}
								});
					}
				});
	}

	/**
	 * @param dmc
	 * @param properties
	 * @param rm
	 * @param generateUpdateEvent
	 */
	@Override
	protected void modifyBreakpoint(final IBreakpointDMContext dmc, final Map<String, Object> attributes,
			final RequestMonitor rm, final boolean generateUpdateEvent) {
		// Retrieve the breakpoint parameters
		// At this point, we know their are OK so there is no need to re-validate
		MIBreakpointDMContext breakpointCtx = (MIBreakpointDMContext) dmc;
		IBreakpointsTargetDMContext context = DMContexts.getAncestorOfType(breakpointCtx,
				IBreakpointsTargetDMContext.class);
		final Map<String, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		final String reference = breakpointCtx.getReference();
		MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);

		// Track the number of change requests
		int numberOfChanges = 0;
		final CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm) {
			@Override
			protected void handleCompleted() {
				GDBBreakpoints_7_0.super.modifyBreakpoint(dmc, attributes, rm, generateUpdateEvent);
			}
		};

		// Determine if the tracepoint pass count changed
		String passCountAttribute = MIBreakpoints.PASS_COUNT;
		if (attributes.containsKey(passCountAttribute)) {
			Integer oldValue = breakpoint.getPassCount();
			Integer newValue = (Integer) attributes.get(passCountAttribute);
			if (newValue == null)
				newValue = 0;
			if (!oldValue.equals(newValue)) {
				changePassCount(context, reference, newValue, countingRm);
				numberOfChanges++;
			}
			attributes.remove(passCountAttribute);
		}

		// Determine if tracepoint commands changed
		// Note that breakpoint commands (actions) are not handled by the backend
		// which is why we don't check for changes here
		String commandsAttribute = MIBreakpoints.COMMANDS;
		if (attributes.containsKey(commandsAttribute)
				&& breakpoint.getBreakpointType().equals(MIBreakpoints.TRACEPOINT)) {
			String oldValue = breakpoint.getCommands();
			String newValue = (String) attributes.get(commandsAttribute);
			if (newValue == null)
				newValue = NULL_STRING;
			if (!oldValue.equals(newValue)) {
				ITracepointAction[] actions = generateGdbCommands(newValue);
				numberOfChanges++;
				changeActions(context, reference, newValue, actions, countingRm);
			}
			attributes.remove(commandsAttribute);
		}

		// Set the number of completions required
		countingRm.setDoneCount(numberOfChanges);
	}

	private ITracepointAction[] generateGdbCommands(String actionStr) {
		String[] actionNames = actionStr.split(TracepointActionManager.TRACEPOINT_ACTION_DELIMITER);
		ITracepointAction[] actions = new ITracepointAction[actionNames.length];

		TracepointActionManager actionManager = TracepointActionManager.getInstance();
		for (int i = 0; i < actionNames.length; i++) {
			actions[i] = actionManager.findAction(actionNames[i]);
		}
		return actions;
	}

	private void changeActions(final IBreakpointsTargetDMContext context, final String reference,
			final String actionNames, final ITracepointAction[] actions, final RequestMonitor rm) {
		// Pick the context breakpoints map
		final Map<String, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
			rm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
			rm.done();
			return;
		}

		ArrayList<String> actionStrings = new ArrayList<>();
		for (ITracepointAction action : actions) {
			if (action != null) {
				actionStrings.add(action.getSummary());
			}
		}

		fConnection.queueCommand(
				fCommandFactory.createMIBreakCommands(context, reference, actionStrings.toArray(new String[0])),
				new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);
						if (breakpoint == null) {
							rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED,
									UNKNOWN_BREAKPOINT, null));
							rm.done();
							return;
						}
						breakpoint.setCommands(actionNames);
						rm.done();
					}
				});
	}

	/**
	 * Update the breakpoint ignoreCount.
	 * IgnoreCount is not supported by tracepoints
	 */
	@Override
	protected void changeIgnoreCount(IBreakpointsTargetDMContext context, final String reference, final int ignoreCount,
			final RequestMonitor rm) {
		// Pick the context breakpoints map
		final Map<String, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
			rm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
			rm.done();
			return;
		}

		final MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);
		if (breakpoint == null || breakpoint.getBreakpointType().equals(MIBreakpoints.TRACEPOINT)) {
			// Ignorecount is not supported for tracepoints
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, INVALID_BREAKPOINT_TYPE, null));
			rm.done();
			return;
		}

		super.changeIgnoreCount(context, reference, ignoreCount, rm);
	}

	/**
	 * Update the tracepoint passCount
	 *
	 * @param context
	 * @param reference
	 * @param ignoreCount
	 * @param rm
	 *
	 * @since 5.0
	 */
	protected void changePassCount(IBreakpointsTargetDMContext context, final String reference, final int ignoreCount,
			final RequestMonitor rm) {
		// Pick the context breakpoints map
		final Map<String, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
			rm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
			rm.done();
			return;
		}

		final MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);
		if (breakpoint == null || breakpoint.getBreakpointType().equals(MIBreakpoints.TRACEPOINT) == false) {
			// Passcount is just for tracepoints
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, INVALID_BREAKPOINT_TYPE, null));
			rm.done();
			return;
		}

		// Queue the command
		fConnection.queueCommand(fCommandFactory.createCLIPasscount(context, reference, ignoreCount),
				new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						breakpoint.setPassCount(ignoreCount);
						rm.done();
					}
				});
	}
}
