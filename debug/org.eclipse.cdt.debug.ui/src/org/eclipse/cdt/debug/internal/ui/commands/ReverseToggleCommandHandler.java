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
 *     Ericsson - initial API and implementation
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.commands;

import java.net.URL;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler;
import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseDebugMethod;
import org.eclipse.cdt.debug.core.model.IReverseToggleHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.DebugCommandHandler;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.services.IEvaluationService;
import org.osgi.framework.Bundle;

/**
 * Command handler to toggle reverse debugging mode
 *
 * @since 7.0
 */
public class ReverseToggleCommandHandler extends DebugCommandHandler implements IDebugContextListener, IElementUpdater {

	private static final ImageDescriptor REVERSE_TOGGLE_DEFAULT_IMAGE = getImageDescriptor(
			"icons/obj16/reverse_toggle.gif"); //$NON-NLS-1$
	private static final ImageDescriptor REVERSE_TOGGLE_SOFTWARE_ON_IMAGE = getImageDescriptor(
			"icons/obj16/reverse_toggle_sw_on.png"); //$NON-NLS-1$
	private static final ImageDescriptor REVERSE_TOGGLE_SOFTWARE_OFF_IMAGE = getImageDescriptor(
			"icons/obj16/reverse_toggle_sw_off.png"); //$NON-NLS-1$
	private static final ImageDescriptor REVERSE_TOGGLE_HARDWARE_ON_IMAGE = getImageDescriptor(
			"icons/obj16/reverse_toggle_hw_on.png"); //$NON-NLS-1$
	private static final ImageDescriptor REVERSE_TOGGLE_HARDWARE_OFF_IMAGE = getImageDescriptor(
			"icons/obj16/reverse_toggle_hw_off.png"); //$NON-NLS-1$

	@Override
	protected Class<?> getCommandType() {
		return IReverseToggleHandler.class;
	}

	//
	// The below logic allows us to keep the checked state of the toggle button
	// properly set.  This is because in some case, the checked state may change
	// without the user actually pressing the button.  For instance, if we restart
	// the inferior, the toggle may automatically turn off.
	// To figure this out, whenever a debug context changes, we make sure we are
	// showing the proper checked state.
	//

	// We must hard-code the command id so as to know it from the very start (bug 290699)
	private static final String REVERSE_TOGGLE_COMMAND_ID = "org.eclipse.cdt.debug.ui.command.reverseToggle"; //$NON-NLS-1$

	private Object fActiveContext;
	private IReverseToggleHandler fTargetAdapter;
	private IDebugContextService fContextService;

	private static ImageDescriptor getImageDescriptor(String path) {
		Bundle bundle = Platform.getBundle("org.eclipse.cdt.debug.ui"); //$NON-NLS-1$
		URL url = null;
		if (bundle != null) {
			url = FileLocator.find(bundle, new Path(path), null);
			if (url != null) {
				return ImageDescriptor.createFromURL(url);
			}
		}
		return null;
	}

	public ReverseToggleCommandHandler() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			fContextService = DebugUITools.getDebugContextManager().getContextService(window);
			if (fContextService != null) {
				fContextService.addPostDebugContextListener(this);

				// This constructor might be called after the launch, so we must refresh here too.
				// This can happen if we activate the action set after the launch.
				refresh(fContextService.getActiveContext());
			}
		}
	}

	@Override
	public void dispose() {
		// Must use the stored context service.  If we try to fetch the service
		// again with the workbenchWindow, it may fail if the window is
		// already closed.
		if (fContextService != null) {
			fContextService.removePostDebugContextListener(this);
		}
		fTargetAdapter = null;
		fActiveContext = null;
		super.dispose();
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		refresh(event.getContext());
	}

	private void refresh(ISelection selection) {
		fTargetAdapter = null;
		fActiveContext = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			if (!ss.isEmpty()) {
				fActiveContext = ss.getFirstElement();
				if (fActiveContext instanceof IAdaptable) {
					fTargetAdapter = getAdapter((IAdaptable) fActiveContext);
				}
			}
		}

		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		if (commandService != null) {
			commandService.refreshElements(REVERSE_TOGGLE_COMMAND_ID, null);
		}
	}

	private IReverseToggleHandler getAdapter(IAdaptable adaptable) {
		IReverseToggleHandler adapter = adaptable.getAdapter(IReverseToggleHandler.class);
		if (adapter == null) {
			IAdapterManager adapterManager = Platform.getAdapterManager();
			if (adapterManager.hasAdapter(adaptable, getCommandType().getName())) {
				adapter = (IReverseToggleHandler) adapterManager.loadAdapter(adaptable,
						IReverseToggleHandler.class.getName());
			}
		}
		if (adapter instanceof IChangeReverseMethodHandler) {
			return adapter;
		} else {
			return null;
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ReverseDebugMethod newMethod;
		try {
			if (HandlerUtil.matchesRadioState(event)) {
				return null;
			}

			String radioState = event.getParameter(RadioState.PARAMETER_ID);

			if (radioState.equals("UseSoftTrace")) { //$NON-NLS-1$
				newMethod = ReverseDebugMethod.SOFTWARE;
			} else if (radioState.equals("TraceOff")) { //$NON-NLS-1$
				newMethod = ReverseDebugMethod.OFF;
			} else if (radioState.equals("UseHardTrace")) { //$NON-NLS-1$
				newMethod = ReverseDebugMethod.HARDWARE;
			} else {
				// undefined trace method
				throw new ExecutionException(Messages.ReverseDebugging_UndefinedTraceMethod);
			}

			// store the parameter in the gdb command handler class
			if (fTargetAdapter != null && fTargetAdapter instanceof IChangeReverseMethodHandler) {
				ReverseDebugMethod currMethod = ((IChangeReverseMethodHandler) fTargetAdapter)
						.getReverseDebugMethod(fActiveContext);
				if (currMethod == newMethod) {
					return null;
				}
				((IChangeReverseMethodHandler) fTargetAdapter).setReverseDebugMethod(newMethod);
			}

			// execute the event
			super.execute(event);

			// and finally update the radio current state
			HandlerUtil.updateRadioState(event.getCommand(), radioState);

			return null;
		} catch (NullPointerException | ExecutionException e) {
			// Disable tracing
			if (fTargetAdapter != null && fTargetAdapter instanceof IChangeReverseMethodHandler) {
				if (fTargetAdapter.toggleNeedsUpdating()) {
					ReverseDebugMethod currMethod = ((IChangeReverseMethodHandler) fTargetAdapter)
							.getReverseDebugMethod(fActiveContext);
					if (currMethod == ReverseDebugMethod.OFF) {
						ReverseDebugMethod prevMethod = ((IChangeReverseMethodHandler) fTargetAdapter)
								.getPreviousReverseDebugMethod(fActiveContext);
						if (prevMethod == ReverseDebugMethod.HARDWARE) {
							newMethod = ReverseDebugMethod.HARDWARE;
						} else {
							newMethod = ReverseDebugMethod.SOFTWARE;
						}
					} else {
						newMethod = ReverseDebugMethod.OFF;
					}
					((IChangeReverseMethodHandler) fTargetAdapter).setReverseDebugMethod(newMethod);
				}
			}
			super.execute(event);
			return null;
		}
	}

	@Override
	protected void postExecute(final IRequest request, Object[] targets) {
		super.postExecute(request, targets);
		new WorkbenchJob("") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (request.getStatus() != null && request.getStatus().getCode() != 0) {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					Shell activeShell = null;
					if (window != null) {
						activeShell = window.getShell();
					} else {
						activeShell = new Shell(PlatformUI.getWorkbench().getDisplay());
					}
					MessageDialog dialogbox = new MessageDialog(activeShell, Messages.ReverseDebugging_Error, null,
							request.getStatus().getMessage(), MessageDialog.ERROR,
							new String[] { IDialogConstants.OK_LABEL }, 0);
					dialogbox.open();
				}
				// Request re-evaluation of property "org.eclipse.cdt.debug.ui.isReverseDebuggingEnabled" to update
				// visibility of reverse stepping commands.
				IEvaluationService exprService = PlatformUI.getWorkbench().getService(IEvaluationService.class);
				if (exprService != null) {
					exprService.requestEvaluation("org.eclipse.cdt.debug.ui.isReverseDebuggingEnabled"); //$NON-NLS-1$
				}
				// Refresh reverse toggle commands with the new state of reverse enabled.
				// This is in order to keep multiple toggle actions in UI in sync.
				ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
				if (commandService != null) {
					commandService.refreshElements(REVERSE_TOGGLE_COMMAND_ID, null);
				}

				return Status.OK_STATUS;
			}
		}.schedule();
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		if (fTargetAdapter != null && fTargetAdapter instanceof IChangeReverseMethodHandler) {
			ReverseDebugMethod currMethod = ((IChangeReverseMethodHandler) fTargetAdapter)
					.getReverseDebugMethod(fActiveContext);
			ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
			try {
				if (currMethod == ReverseDebugMethod.HARDWARE) {
					HandlerUtil.updateRadioState(commandService.getCommand(REVERSE_TOGGLE_COMMAND_ID), "UseHardTrace"); //$NON-NLS-1$
					element.setTooltip(Messages.ReverseDebugging_ToggleHardwareTrace);
					element.setIcon(REVERSE_TOGGLE_HARDWARE_ON_IMAGE);
				} else if (currMethod == ReverseDebugMethod.SOFTWARE) {
					HandlerUtil.updateRadioState(commandService.getCommand(REVERSE_TOGGLE_COMMAND_ID), "UseSoftTrace"); //$NON-NLS-1$
					element.setTooltip(Messages.ReverseDebugging_ToggleSoftwareTrace);
					element.setIcon(REVERSE_TOGGLE_SOFTWARE_ON_IMAGE);
				} else {
					HandlerUtil.updateRadioState(commandService.getCommand(REVERSE_TOGGLE_COMMAND_ID), "TraceOff"); //$NON-NLS-1$
					element.setTooltip(Messages.ReverseDebugging_ToggleReverseDebugging);
					ReverseDebugMethod prevMethod = ((IChangeReverseMethodHandler) fTargetAdapter)
							.getPreviousReverseDebugMethod(fActiveContext);
					if (prevMethod == ReverseDebugMethod.HARDWARE) {
						element.setIcon(REVERSE_TOGGLE_HARDWARE_OFF_IMAGE);
					} else if (prevMethod == ReverseDebugMethod.SOFTWARE) {
						element.setIcon(REVERSE_TOGGLE_SOFTWARE_OFF_IMAGE);
					} else {
						element.setIcon(REVERSE_TOGGLE_DEFAULT_IMAGE);
					}
				}
			} catch (ExecutionException e) {
				// Do nothing
			}
		}
	}
}
