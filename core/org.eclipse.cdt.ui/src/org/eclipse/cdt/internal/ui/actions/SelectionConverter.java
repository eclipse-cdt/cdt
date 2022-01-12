/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class SelectionConverter {
	/**
	 * Converts the selection provided by the given part into a structured selection.
	 * The following conversion rules are used:
	 * <ul>
	 *	<li><code>part instanceof CEditor</code>: returns a structured selection
	 * 	using code resolve to convert the editor's text selection.</li>
	 * <li><code>part instanceof IWorkbenchPart</code>: returns the part's selection
	 * 	if it is a structured selection.</li>
	 * <li><code>default</code>: returns an empty structured selection.</li>
	 * </ul>
	 */
	public static IStructuredSelection getStructuredSelection(IWorkbenchPart part) throws CModelException {
		if (part instanceof CEditor)
			return new StructuredSelection(codeResolve((CEditor) part));
		ISelectionProvider provider = part.getSite().getSelectionProvider();
		if (provider != null) {
			ISelection selection = provider.getSelection();
			if (selection instanceof IStructuredSelection)
				return (IStructuredSelection) selection;
		}
		return StructuredSelection.EMPTY;
	}

	/**
	 * Converts objects of a structured selection to c elements if possible.
	 * This is a convenience method, fully equivalent to
	 * <code>convertSelectionToCElements(s, false)</code>.
	 * @param s The structured selection
	 * @return The converted selection
	 */
	public static IStructuredSelection convertSelectionToCElements(ISelection s) {
		return convertSelectionToCElements(s, false);
	}

	/**
	 * Converts objects of a structured selection to c elements if possible.
	 * @param s The structured selection
	 * @param keepNonCElements Whether to keep objects in selection if they cannot be converted
	 * @return The converted selection
	 */
	public static IStructuredSelection convertSelectionToCElements(ISelection s, boolean keepNonCElements) {
		List<Object> converted = new ArrayList<>();
		if (s instanceof IStructuredSelection) {
			Object[] elements = ((IStructuredSelection) s).toArray();
			for (int i = 0; i < elements.length; i++) {
				Object e = elements[i];
				if (e instanceof ICElement) {
					converted.add(e);
				} else if (e instanceof IAdaptable) {
					ICElement c = ((IAdaptable) e).getAdapter(ICElement.class);
					if (c != null) {
						converted.add(c);
					} else if (keepNonCElements)
						converted.add(e);
				} else if (keepNonCElements)
					converted.add(e);
			}
		}
		return new StructuredSelection(converted.toArray());
	}

	public static IStructuredSelection convertSelectionToResources(ISelection s) {
		List<Object> converted = new ArrayList<>();
		if (s instanceof StructuredSelection) {
			Object[] elements = ((StructuredSelection) s).toArray();
			for (int i = 0; i < elements.length; i++) {
				Object e = elements[i];
				if (e instanceof IResource) {
					converted.add(e);
				} else if (e instanceof IAdaptable) {
					IResource r = ((IAdaptable) e).getAdapter(IResource.class);
					if (r != null) {
						converted.add(r);
					}
				}
			}
		}
		return new StructuredSelection(converted.toArray());
	}

	public static boolean allResourcesAreOfType(IStructuredSelection selection, int resourceMask) {
		Iterator<?> resources = selection.iterator();
		while (resources.hasNext()) {
			Object next = resources.next();
			if (next instanceof IAdaptable) {
				IAdaptable element = (IAdaptable) next;
				IResource resource = element.getAdapter(IResource.class);

				if (resource == null) {
					return false;
				}
				if (!resourceIsType(resource, resourceMask)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns the selection adapted to IResource. Returns null if any of the
	 * entries are not adaptable.
	 *
	 * @param selection
	 *            the selection
	 * @param resourceMask
	 *            resource mask formed by bitwise OR of resource type constants
	 *            (defined on <code>IResource</code>)
	 * @return IStructuredSelection or null if any of the entries are not
	 *         adaptable.
	 * @see IResource#getType()
	 */
	public static IStructuredSelection allResources(IStructuredSelection selection, int resourceMask) {
		Iterator<?> adaptables = selection.iterator();
		List<IResource> result = new ArrayList<>();
		while (adaptables.hasNext()) {
			Object next = adaptables.next();
			if (next instanceof IAdaptable) {
				IResource resource = ((IAdaptable) next).getAdapter(IResource.class);
				if (resource == null) {
					return null;
				} else if (resourceIsType(resource, resourceMask)) {
					result.add(resource);
				}
			} else {
				return null;
			}
		}
		return new StructuredSelection(result);

	}

	public static ICElement getElementAtOffset(ITextEditor editor) throws CModelException {
		return getElementAtOffset(getInput(editor), (ITextSelection) editor.getSelectionProvider().getSelection());
	}

	public static ICElement[] getElementsAtOffset(ITextEditor editor) throws CModelException {
		return getElementsAtOffset(getInput(editor), (ITextSelection) editor.getSelectionProvider().getSelection());
	}

	public static ICElement getElementAtOffset(ICElement input, ITextSelection selection) throws CModelException {
		if (input instanceof ITranslationUnit) {
			ITranslationUnit tunit = (ITranslationUnit) input;
			if (tunit.isWorkingCopy() && tunit.isOpen()) {
				synchronized (tunit) {
					if (tunit instanceof IWorkingCopy) {
						((IWorkingCopy) tunit).reconcile();
					}
				}
			}
			ICElement ref = tunit.getElementAtOffset(selection.getOffset());
			if (ref == null)
				return input;
			return ref;
		}
		return null;
	}

	public static ICElement[] getElementsAtOffset(ICElement input, ITextSelection selection) throws CModelException {
		if (input instanceof ITranslationUnit) {
			ITranslationUnit tunit = (ITranslationUnit) input;
			if (tunit.isWorkingCopy() && tunit.isOpen()) {
				synchronized (tunit) {
					if (tunit instanceof IWorkingCopy) {
						((IWorkingCopy) tunit).reconcile();
					}
				}
			}
			ICElement[] refs = tunit.getElementsAtOffset(selection.getOffset());
			if (refs == null)
				return new ICElement[] { input };
			return refs;
		}
		return null;
	}

	public static ICElement getInput(ITextEditor editor) {
		if (editor == null)
			return null;
		IEditorInput input = editor.getEditorInput();
		IWorkingCopyManager manager = CUIPlugin.getDefault().getWorkingCopyManager();
		return manager.getWorkingCopy(input);
	}

	/**
	 * Returns whether the type of the given resource is among the specified
	 * resource types.
	 *
	 * @param resource
	 *            the resource
	 * @param resourceMask
	 *            resource mask formed by bitwise OR of resource type constants
	 *            (defined on <code>IResource</code>)
	 * @return <code>true</code> if the resources has a matching type, and
	 *         <code>false</code> otherwise
	 * @see IResource#getType()
	 */
	public static boolean resourceIsType(IResource resource, int resourceMask) {
		return (resource.getType() & resourceMask) != 0;
	}

	public static ICElement[] codeResolve(CEditor editor) throws CModelException {
		return codeResolve(getInput(editor), (ITextSelection) editor.getSelectionProvider().getSelection());
	}

	public static ICElement[] codeResolve(ICElement input, ITextSelection selection) throws CModelException {
		if (input instanceof ITranslationUnit) {
			ITranslationUnit tunit = (ITranslationUnit) input;
			if (tunit.isWorkingCopy() && tunit.isOpen()) {
				synchronized (tunit) {
					if (tunit instanceof IWorkingCopy) {
						((IWorkingCopy) tunit).reconcile();
					}
				}
			}
			ICElement[] elems = tunit.getElementsAtOffset(selection.getOffset());
			if (elems != null) {
				for (int i = 0; i < elems.length; ++i) {
					ICElement elem = elems[i];
					if (elem instanceof ISourceReference) {
						String text = selection.getText();
						if (text.equals(elem.getElementName())) {
							return new ICElement[] { elem };
						}
					}
				}
				for (int i = 0; i < elems.length; ++i) {
					ICElement elem = elems[i];
					if (elem instanceof ISourceReference) {
						ISourceRange range = ((ISourceReference) elem).getSourceRange();
						if (range.getStartPos() == selection.getOffset()) {
							return new ICElement[] { elem };
						}
					}
				}
			}
		}
		return ICElement.EMPTY_ARRAY;
	}

	/**
	 * Converts the text selection provided by the given editor a Java element by
	 * asking the user if code resolve returned more than one result. If the selection
	 * doesn't cover a Java element <code>null</code> is returned.
	 */
	public static ICElement codeResolve(CEditor editor, Shell shell, String title, String message)
			throws CModelException {
		ICElement[] elements = codeResolve(editor);
		if (elements == null || elements.length == 0)
			return null;
		ICElement candidate = elements[0];
		if (elements.length > 1) {
			candidate = OpenActionUtil.selectCElement(elements, shell, title, message);
		}
		return candidate;
	}

	/**
	 * Converts the text selection provided by the given editor into an array of
	 * C elements. If the selection doesn't cover a C element and the selection's
	 * length is greater than 0 the method returns the editor's input element.
	 */
	public static ICElement[] codeResolveOrInput(CEditor editor) throws CModelException {
		ICElement input = getInput(editor);
		ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		ICElement[] result = codeResolve(input, selection);
		if (result.length == 0) {
			result = new ICElement[] { input };
		}
		return result;
	}

	public static ICElement[] codeResolveOrInputHandled(CEditor editor, Shell shell, String title) {
		try {
			return codeResolveOrInput(editor);
		} catch (CModelException e) {
			ExceptionHandler.handle(e, shell, title, ActionMessages.SelectionConverter_codeResolve_failed);
		}
		return null;
	}

	public static boolean canOperateOn(CEditor editor) {
		if (editor == null)
			return false;
		return getInput(editor) != null;
	}
}
