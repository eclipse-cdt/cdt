/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems and others.
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
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format (Bug 202556)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.actions;

import org.eclipse.cdt.dsf.ui.viewmodel.IVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Static utility methods for use with View Model related
 * commands and handlers.
 *
 * @since 1.1
 */
public class VMHandlerUtils {

	/**
	 * Retrieves the active VM provider based on the currently active
	 * selection and part.
	 * @param serviceLocator Service locator for access to active selection
	 * and part.
	 *
	 * @return The active VM provder.
	 */
	static public IVMProvider getActiveVMProvider(IServiceLocator serviceLocator) {
		ISelection selection = null;

		ISelectionService selectionService = serviceLocator.getService(ISelectionService.class);
		if (selectionService != null) {
			selection = selectionService.getSelection();
		}

		if (selection != null && !selection.isEmpty()) {
			return getVMProviderForSelection(selection);
		} else {
			IWorkbenchPart part = null;
			IPartService partService = serviceLocator.getService(IPartService.class);
			if (partService != null) {
				part = partService.getActivePart();
				return getVMProviderForPart(part);
			}
			return null;
		}
	}

	/**
	 * Retrieves the active VM provider based on the given execution event.
	 * @param event The execution event which is usually given as an argument
	 * to the command handler execution call.
	 *
	 * @return The active VM provder.
	 */
	static public IVMProvider getActiveVMProvider(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection != null && !selection.isEmpty()) {
			return getVMProviderForSelection(selection);
		} else {
			IWorkbenchPart part = HandlerUtil.getActivePart(event);
			return getVMProviderForPart(part);
		}
	}

	/**
	 * Retrieves the selection from a given service locator's selection service.
	 * @param serviceLocator Service locator for access to active selection
	 * and part.
	 *
	 * @return The selection if available; return null otherwise.
	 * @since 2.2
	 */
	static public ISelection getSelection(IServiceLocator serviceLocator) {
		ISelectionService selectionService = serviceLocator.getService(ISelectionService.class);
		if (selectionService != null) {
			return selectionService.getSelection();
		}
		return null;
	}

	public static IVMProvider getVMProviderForPart(IWorkbenchPart part) {
		IDebugContextService contextService = DebugUITools.getDebugContextManager()
				.getContextService(part.getSite().getWorkbenchWindow());

		ISelection debugContext = contextService.getActiveContext(getPartId(part));
		if (debugContext == null) {
			debugContext = contextService.getActiveContext();
		}

		Object input = null;
		if (debugContext instanceof IStructuredSelection) {
			input = ((IStructuredSelection) debugContext).getFirstElement();
		}

		if (part instanceof IDebugView) {
			Viewer viewer = ((IDebugView) part).getViewer();
			if (input instanceof IAdaptable && viewer instanceof TreeModelViewer) {
				IPresentationContext presContext = ((TreeModelViewer) viewer).getPresentationContext();
				IVMAdapter vmAdapter = ((IAdaptable) input).getAdapter(IVMAdapter.class);
				if (vmAdapter != null) {
					return vmAdapter.getVMProvider(presContext);
				}
			}
		}
		return null;
	}

	private static String getPartId(IWorkbenchPart part) {
		if (part instanceof IViewPart) {
			IViewSite site = (IViewSite) part.getSite();
			return site.getId() + (site.getSecondaryId() != null ? (":" + site.getSecondaryId()) : ""); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return part.getSite().getId();
		}
	}

	public static IVMProvider getVMProviderForSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IVMContext) {
				return ((IVMContext) element).getVMNode().getVMProvider();
			}
		}
		return null;
	}

	/**
	 * Returns the viewer input element for the viewer in the given
	 * presentation context.  The input element is retrieved only if the
	 * presentation context's view is based on the {@link AbstractDebugView}.
	 * Returns <code>null</code> if not found.
	 * @since 2.5
	 */
	public static Object getViewerInput(IPresentationContext context) {
		if (context.getPart() instanceof AbstractDebugView) {
			Viewer viewer = ((AbstractDebugView) context.getPart()).getViewer();
			if (viewer != null) {
				return viewer.getInput();
			}
		}
		return null;
	}

	/**
	 * Returns the {@link IVMNode} associated with the element in the
	 * given path.  Returns <code>null</code> if not found.
	 *
	 * @since 2.5
	 */
	public static IVMNode getVMNode(Object viewerInput, TreePath path) {
		if (path.getSegmentCount() != 0) {
			return (IVMNode) DebugPlugin.getAdapter(path.getLastSegment(), IVMNode.class);
		} else {
			return (IVMNode) DebugPlugin.getAdapter(viewerInput, IVMNode.class);
		}
	}
}
