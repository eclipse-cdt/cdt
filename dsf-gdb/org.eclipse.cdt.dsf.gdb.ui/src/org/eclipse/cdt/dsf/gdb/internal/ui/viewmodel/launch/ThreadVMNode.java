/*******************************************************************************
 * Copyright (c) 2006, 2016 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for multi threaded functionality
 *     Patrick Chuong (Texas Instruments) - Add support for icon overlay in the debug view (Bug 334566)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.internal.ui.pinclone.PinCloneUtils;
import org.eclipse.cdt.debug.ui.IPinProvider.IPinElementColorDescriptor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.AbstractThreadVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ExecutionContextLabelText;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ILaunchVMConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.service.IGDBFocusSynchronizer.IGDBFocusChangedEvent;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbPinProvider;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerCountingRequestMonitor;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelAttribute;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelColumnInfo;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelImage;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.PropertiesBasedLabelProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.VMDelegatingPropertiesUpdate;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;

public class ThreadVMNode extends AbstractThreadVMNode implements IElementMementoProvider {
	/** Indicator that we should not display running threads */
	private boolean fHideRunningThreadsProperty = false;

	/** PropertyChangeListener to keep track of the PREF_HIDE_RUNNING_THREADS preference */
	private IPropertyChangeListener fPropertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(IGdbDebugPreferenceConstants.PREF_HIDE_RUNNING_THREADS)) {
				fHideRunningThreadsProperty = (Boolean) event.getNewValue();
				// Refresh the debug view to take in consideration this change
				getDMVMProvider().refresh();
			}
		}
	};

	public ThreadVMNode(AbstractDMVMProvider provider, DsfSession session) {
		super(provider, session);

		IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(fPropertyChangeListener);
		fHideRunningThreadsProperty = store.getBoolean(IGdbDebugPreferenceConstants.PREF_HIDE_RUNNING_THREADS);
	}

	@Override
	public void dispose() {
		GdbUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPropertyChangeListener);
		super.dispose();
	}

	@Override
	public String toString() {
		return "ThreadVMNode(" + getSession().getId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected IElementLabelProvider createLabelProvider() {
		PropertiesBasedLabelProvider provider = new PropertiesBasedLabelProvider();

		provider.setColumnInfo(PropertiesBasedLabelProvider.ID_COLUMN_NO_COLUMNS,
				new LabelColumnInfo(new LabelAttribute[] {
						// Text is made of the thread name followed by its state and state change reason.
						new GdbExecutionContextLabelText(MessagesForGdbLaunchVM.ThreadVMNode_No_columns__text_format,
								new String[] { ExecutionContextLabelText.PROP_NAME_KNOWN, PROP_NAME,
										ExecutionContextLabelText.PROP_ID_KNOWN, ILaunchVMConstants.PROP_ID,
										IGdbLaunchVMConstants.PROP_OS_ID_KNOWN, IGdbLaunchVMConstants.PROP_OS_ID,
										IGdbLaunchVMConstants.PROP_CORES_ID_KNOWN, IGdbLaunchVMConstants.PROP_CORES_ID,
										ILaunchVMConstants.PROP_IS_SUSPENDED,
										ExecutionContextLabelText.PROP_STATE_CHANGE_REASON_KNOWN,
										ILaunchVMConstants.PROP_STATE_CHANGE_REASON,
										ExecutionContextLabelText.PROP_STATE_CHANGE_DETAILS_KNOWN,
										ILaunchVMConstants.PROP_STATE_CHANGE_DETAILS }),
						new LabelText(MessagesForGdbLaunchVM.ThreadVMNode_No_columns__Error__label, new String[0]),
						/* RUNNING THREAD - RED PIN */
						new LabelImage(
								CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_RUNNING_R_PINNED)) {
							{
								setPropertyNames(new String[] { ILaunchVMConstants.PROP_IS_SUSPENDED,
										IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
										IGdbLaunchVMConstants.PROP_PIN_COLOR });
							}

							@Override
							public boolean isEnabled(IStatus status, Map<String, Object> properties) {
								Boolean prop = (Boolean) properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED);
								Boolean pin_prop = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT);
								Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR);
								return (prop != null && pin_prop != null && pin_color_prop != null)
										? !prop.booleanValue() && pin_prop.booleanValue()
												&& pin_color_prop.equals(IPinElementColorDescriptor.RED)
										: false;
							}
						},
						/* RUNNING THREAD - GREEN PIN */
						new LabelImage(
								CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_RUNNING_G_PINNED)) {
							{
								setPropertyNames(new String[] { ILaunchVMConstants.PROP_IS_SUSPENDED,
										IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
										IGdbLaunchVMConstants.PROP_PIN_COLOR });
							}

							@Override
							public boolean isEnabled(IStatus status, Map<String, Object> properties) {
								Boolean prop = (Boolean) properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED);
								Boolean pin_prop = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT);
								Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR);
								return (prop != null && pin_prop != null && pin_color_prop != null)
										? !prop.booleanValue() && pin_prop.booleanValue()
												&& pin_color_prop.equals(IPinElementColorDescriptor.GREEN)
										: false;
							}
						},
						/* RUNNING THREAD - BLUE PIN */
						new LabelImage(
								CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_RUNNING_B_PINNED)) {
							{
								setPropertyNames(new String[] { ILaunchVMConstants.PROP_IS_SUSPENDED,
										IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
										IGdbLaunchVMConstants.PROP_PIN_COLOR });
							}

							@Override
							public boolean isEnabled(IStatus status, Map<String, Object> properties) {
								Boolean prop = (Boolean) properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED);
								Boolean pin_prop = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT);
								Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR);
								return (prop != null && pin_prop != null && pin_color_prop != null)
										? !prop.booleanValue() && pin_prop.booleanValue()
												&& pin_color_prop.equals(IPinElementColorDescriptor.BLUE)
										: false;
							}
						},
						/* RUNNING THREAD - NO PIN */
						new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING)) {
							{
								setPropertyNames(new String[] { ILaunchVMConstants.PROP_IS_SUSPENDED });
							}

							@Override
							public boolean isEnabled(IStatus status, java.util.Map<String, Object> properties) {
								// prop has been seen to be null during session shutdown [313823]
								Boolean prop = (Boolean) properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED);
								return (prop != null) ? !prop.booleanValue() : false;
							}
						},
						/* SUSPENDED THREAD - RED PIN */
						new LabelImage(
								CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_SUSPENDED_R_PINNED)) {
							{
								setPropertyNames(new String[] { IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
										IGdbLaunchVMConstants.PROP_PIN_COLOR });
							}

							@Override
							public boolean isEnabled(IStatus status, Map<String, Object> properties) {
								Boolean pin_prop = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT);
								Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR);
								return (pin_prop != null && pin_color_prop != null) ? pin_prop.booleanValue()
										&& pin_color_prop.equals(IPinElementColorDescriptor.RED) : false;
							}
						},
						/* SUSPENDED THREAD - GREEN PIN */
						new LabelImage(
								CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_SUSPENDED_G_PINNED)) {
							{
								setPropertyNames(new String[] { IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
										IGdbLaunchVMConstants.PROP_PIN_COLOR });
							}

							@Override
							public boolean isEnabled(IStatus status, Map<String, Object> properties) {
								Boolean pin_prop = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT);
								Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR);
								return (pin_prop != null && pin_color_prop != null) ? pin_prop.booleanValue()
										&& pin_color_prop.equals(IPinElementColorDescriptor.GREEN) : false;
							}
						},
						/* SUSPENDED THREAD - BLUE PIN */
						new LabelImage(
								CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_SUSPENDED_B_PINNED)) {
							{
								setPropertyNames(new String[] { IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
										IGdbLaunchVMConstants.PROP_PIN_COLOR });
							}

							@Override
							public boolean isEnabled(IStatus status, Map<String, Object> properties) {
								Boolean pin_prop = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT);
								Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR);
								return (pin_prop != null && pin_color_prop != null) ? pin_prop.booleanValue()
										&& pin_color_prop.equals(IPinElementColorDescriptor.BLUE) : false;
							}
						},
						/* SUSPENDED THREAD - NO PIN */
						new LabelImage(
								DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED)), }));
		return provider;
	}

	@Override
	protected void updateElementsInSessionThread(final IChildrenUpdate update) {
		IProcesses procService = getServicesTracker().getService(IProcesses.class);
		final IContainerDMContext contDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(),
				IContainerDMContext.class);
		if (procService == null || contDmc == null) {
			handleFailedUpdate(update);
			return;
		}

		procService.getProcessesBeingDebugged(contDmc,
				new ViewerDataRequestMonitor<IDMContext[]>(getSession().getExecutor(), update) {
					@Override
					public void handleCompleted() {
						if (!isSuccess() || !(getData() instanceof IExecutionDMContext[])) {
							handleFailedUpdate(update);
							return;
						}

						IExecutionDMContext[] execDmcs = (IExecutionDMContext[]) getData();
						if (fHideRunningThreadsProperty) {
							// Remove running threads from the list
							IRunControl runControl = getServicesTracker().getService(IRunControl.class);
							if (runControl == null) {
								handleFailedUpdate(update);
								return;
							}

							List<IExecutionDMContext> execDmcsNotRunning = new ArrayList<>();
							for (IExecutionDMContext execDmc : execDmcs) {
								// Keep suspended or stepping threads
								if (runControl.isSuspended(execDmc) || runControl.isStepping(execDmc)) {
									execDmcsNotRunning.add(execDmc);
								}
							}
							execDmcs = execDmcsNotRunning.toArray(new IExecutionDMContext[execDmcsNotRunning.size()]);
						}

						fillUpdateWithVMCs(update, execDmcs);
						update.done();
					}
				});
	}

	@Override
	protected void updatePropertiesInSessionThread(IPropertiesUpdate[] updates) {
		IPropertiesUpdate[] parentUpdates = new IPropertiesUpdate[updates.length];

		for (int i = 0; i < updates.length; i++) {
			final IPropertiesUpdate update = updates[i];

			final ViewerCountingRequestMonitor countringRm = new ViewerCountingRequestMonitor(
					ImmediateExecutor.getInstance(), updates[i]);
			int count = 0;

			// Create a delegating update which will let the super-class fill in the
			// standard container properties.
			parentUpdates[i] = new VMDelegatingPropertiesUpdate(updates[i], countringRm);
			count++;

			IMIExecutionDMContext execDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(),
					IMIExecutionDMContext.class);
			if (execDmc != null) {
				update.setProperty(ILaunchVMConstants.PROP_ID, execDmc.getThreadId());

				// set pin properties
				IPinElementColorDescriptor colorDesc = PinCloneUtils
						.getPinElementColorDescriptor(GdbPinProvider.getPinnedHandles(), execDmc);
				updates[i].setProperty(IGdbLaunchVMConstants.PROP_PIN_COLOR,
						colorDesc != null ? colorDesc.getOverlayColor() : null);
				updates[i].setProperty(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
						PinCloneUtils.isPinnedTo(GdbPinProvider.getPinnedHandles(), execDmc));
			}

			if (update.getProperties().contains(PROP_NAME)
					|| update.getProperties().contains(IGdbLaunchVMConstants.PROP_OS_ID)
					|| update.getProperties().contains(IGdbLaunchVMConstants.PROP_CORES_ID)) {
				IProcesses processService = getServicesTracker().getService(IProcesses.class);
				final IThreadDMContext threadDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(),
						IThreadDMContext.class);

				if (processService == null || threadDmc == null) {
					update.setStatus(
							new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, "Service or handle invalid", null)); //$NON-NLS-1$
				} else {
					processService.getExecutionData(threadDmc,
							new ViewerDataRequestMonitor<IThreadDMData>(getExecutor(), update) {
								@Override
								public void handleCompleted() {
									if (isSuccess()) {
										fillThreadDataProperties(update, getData());
									}
									update.setStatus(getStatus());
									countringRm.done();
								}
							});
					count++;
				}
			}

			countringRm.setDoneCount(count);
		}
		super.updatePropertiesInSessionThread(parentUpdates);
	}

	protected void fillThreadDataProperties(IPropertiesUpdate update, IThreadDMData data) {
		if (data.getName() != null && data.getName().length() > 0) {
			update.setProperty(PROP_NAME, data.getName());
		}
		update.setProperty(IGdbLaunchVMConstants.PROP_OS_ID, data.getId());

		if (data instanceof IGdbThreadDMData) {
			String[] cores = ((IGdbThreadDMData) data).getCores();
			if (cores != null) {
				StringBuilder str = new StringBuilder();
				for (String core : cores) {
					str.append(core).append(',');
				}
				if (str.length() > 0) {
					String coresStr = str.substring(0, str.length() - 1);
					update.setProperty(IGdbLaunchVMConstants.PROP_CORES_ID, coresStr);
				}
			}
		}
	}

	private String produceThreadElementName(String viewName, IMIExecutionDMContext execCtx) {
		return "Thread." + execCtx.getThreadId(); //$NON-NLS-1$
	}

	@Override
	public int getDeltaFlags(Object e) {
		if (fHideRunningThreadsProperty && e instanceof IResumedDMEvent) {
			// Special handling in the case of hiding the running threads to
			// cause a proper refresh when a thread is resumed.
			// We don't need to worry about the ISuspendedDMEvent in this case
			// because a proper refresh will be triggered anyway by the stack frame
			// being displayed.
			return IModelDelta.CONTENT;
		} else if (e instanceof IGDBFocusChangedEvent) {
			return IModelDelta.SELECT;
		}
		return super.getDeltaFlags(e);
	}

	@Override
	public void buildDelta(Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor rm) {
		if (fHideRunningThreadsProperty && e instanceof IResumedDMEvent) {
			// Special handling in the case of hiding the running threads to
			// cause a proper refresh when a thread is resumed.
			// We don't need to worry about the ISuspendedDMEvent in this case
			// because a proper refresh will be triggered anyway by the stack frame
			// being displayed.
			//
			// - If not stepping, update the content of the parent, to allow for
			//   this thread to become hidden.
			// - If stepping, do nothing to avoid too many updates.  If a
			//   time-out is reached before the step completes, the
			//   ISteppingTimedOutEvent will trigger a refresh.
			if (((IResumedDMEvent) e).getReason() != IRunControl.StateChangeReason.STEP) {
				VMDelta ancestorDelta = parentDelta.getParentDelta();
				ancestorDelta.setFlags(ancestorDelta.getFlags() | IModelDelta.CONTENT);
			}
			rm.done();
		} else if (e instanceof IGDBFocusChangedEvent) {
			buildDeltaForFocusChangedEvent((IGDBFocusChangedEvent) e, parentDelta, nodeOffset, rm);
		} else {
			super.buildDelta(e, parentDelta, nodeOffset, rm);
		}
	}

	private void buildDeltaForFocusChangedEvent(IGDBFocusChangedEvent event, VMDelta parentDelta, int nodeOffset,
			RequestMonitor rm) {
		getSession().getExecutor().execute(new DsfRunnable() {
			@Override
			public void run() {
				// can we find a thread context in the hierarchy of the IGDBFocusChangedEvent's context?
				IDMContext thread = DMContexts.getAncestorOfType(event.getDMContext(), IMIExecutionDMContext.class);
				final IDMContext newThreadFocus = thread;
				if (newThreadFocus != null) {
					// we need to find the VMC index for the thread that switched, so we can
					// select it correctly.
					getVMCIndexForDmc(ThreadVMNode.this, newThreadFocus, parentDelta,
							new DataRequestMonitor<Integer>(getExecutor(), rm) {
								@Override
								protected void handleSuccess() {
									final int threadOffset = getData();
									// Create a delta for the thread node - Select it whether it's running or not
									// this way the thread will be visible in the DV even if we end-up selecting one
									// of its frame. Using the FORCE flag to override the sticky selection
									// policy.
									parentDelta.addNode(createVMContext(newThreadFocus), nodeOffset + threadOffset,
											IModelDelta.SELECT | IModelDelta.FORCE);
									rm.done();
								}
							});
				} else {
					// context not a thread - nothing to do here
					rm.done();
				}
			}
		});
	}

	private static final String MEMENTO_NAME = "THREAD_MEMENTO_NAME"; //$NON-NLS-1$

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#compareElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest[])
	 */
	@Override
	public void compareElements(IElementCompareRequest[] requests) {

		for (IElementCompareRequest request : requests) {

			Object element = request.getElement();
			IMemento memento = request.getMemento();
			String mementoName = memento.getString(MEMENTO_NAME);

			if (mementoName != null) {
				if (element instanceof IDMVMContext) {

					IDMContext dmc = ((IDMVMContext) element).getDMContext();

					if (dmc instanceof IMIExecutionDMContext) {

						String elementName = produceThreadElementName(request.getPresentationContext().getId(),
								(IMIExecutionDMContext) dmc);
						request.setEqual(elementName.equals(mementoName));
					}
				}
			}
			request.done();
		}
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#encodeElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest[])
	 */
	@Override
	public void encodeElements(IElementMementoRequest[] requests) {

		for (IElementMementoRequest request : requests) {

			Object element = request.getElement();
			IMemento memento = request.getMemento();

			if (element instanceof IDMVMContext) {

				IDMContext dmc = ((IDMVMContext) element).getDMContext();

				if (dmc instanceof IMIExecutionDMContext) {

					String elementName = produceThreadElementName(request.getPresentationContext().getId(),
							(IMIExecutionDMContext) dmc);
					memento.putString(MEMENTO_NAME, elementName);
				}
			}
			request.done();
		}
	}

}
