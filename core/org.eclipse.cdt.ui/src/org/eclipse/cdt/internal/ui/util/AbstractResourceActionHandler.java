/*******************************************************************************
 * Copyright (c) 2014, 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.internal.core.AdapterUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * Base class for command handlers operating on resources.
 */
public abstract class AbstractResourceActionHandler extends AbstractHandler {
	private IEvaluationContext evaluationContext;
	private IStructuredSelection selection;

	public void setSelection(ISelection selection) {
		this.selection = convertSelection(null, selection);
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		this.evaluationContext = (IEvaluationContext) evaluationContext;
		selection = convertSelection(this.evaluationContext, null);
	}

	protected IStructuredSelection getSelection() {
		if (selection == null) {
			selection = convertSelection(evaluationContext, null);
		}
		return selection;
	}

	protected static IStructuredSelection getSelection(ExecutionEvent event) throws ExecutionException {
		Object selection = HandlerUtil.getActiveMenuSelection(event);
		if (selection == null) {
			selection = HandlerUtil.getCurrentSelectionChecked(event);
		}
		if (selection instanceof ITextSelection) {
			IEditorInput editorInput = HandlerUtil.getActiveEditorInputChecked(event);
			IResource resource = ResourceUtil.getResource(editorInput);
			if (resource != null) {
				return new StructuredSelection(resource);
			}

			resource = ResourceUtil.getFile(editorInput);
			if (resource != null) {
				return new StructuredSelection(resource);
			}
		}
		if (selection instanceof IStructuredSelection) {
			return (IStructuredSelection) selection;
		}
		return StructuredSelection.EMPTY;
	}

	private static IStructuredSelection convertSelection(IEvaluationContext context, Object selection) {
		if (selection == null) {
			if (context == null) {
				return StructuredSelection.EMPTY;
			}
			selection = context.getVariable(ISources.ACTIVE_MENU_SELECTION_NAME);
			if (!(selection instanceof ISelection)) {
				selection = context.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
			}
		}
		if (selection instanceof ITextSelection) {
			if (context == null) {
				context = getEvaluationContext();
			}
			IResource resource = ResourceUtil.getResource(context.getVariable(ISources.ACTIVE_EDITOR_INPUT_NAME));
			if (resource != null) {
				return new StructuredSelection(resource);
			}
		}
		if (selection instanceof IStructuredSelection) {
			return (IStructuredSelection) selection;
		}
		return StructuredSelection.EMPTY;
	}

	private static IEvaluationContext getEvaluationContext() {
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWindow == null) {
			return null;
		}
		IHandlerService service = activeWindow.getService(IHandlerService.class);
		return service.getCurrentState();
	}

	/**
	 * Returns the selected resources.
	 */
	protected Collection<IResource> getSelectedResources() {
		return getSelectedResources(getSelection());
	}

	/**
	 * Returns the selected resources.
	 */
	protected static Collection<IResource> getSelectedResources(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = getSelection(event);
		return getSelectedResources(selection);
	}

	private static Collection<IResource> getSelectedResources(IStructuredSelection selection) {
		Set<IResource> result = new LinkedHashSet<>();
		for (Object obj : selection.toList()) {
			IResource resource = AdapterUtil.adapt(obj, IResource.class);
			if (resource != null) {
				result.add(resource);
			} else {
				result.addAll(extractResourcesFromMapping(obj));
			}
		}
		return result;
	}

	/**
	 * Extracts resources associated with a {@link ResourceMapping}.
	 *
	 * @param obj an object adaptable to {@link ResourceMapping}
	 * @return a list of resources associated with the mapping
	 */
	private static List<IResource> extractResourcesFromMapping(Object obj) {
		ResourceMapping mapping = AdapterUtil.adapt(obj, ResourceMapping.class);
		if (mapping != null) {
			try {
				ResourceTraversal[] traversals = mapping.getTraversals(null, null);
				for (ResourceTraversal traversal : traversals) {
					return Arrays.asList(traversal.getResources());
				}
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}
		return Collections.emptyList();
	}
}
