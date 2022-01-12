/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

//----------------------------------------------------------------------------
// SelectionUtils
//----------------------------------------------------------------------------

/** Eclipse ISelection munging utilities. */
public class SelectionUtils {
	// --- constants ---

	/** Special value for an "empty" selection,
	 *  since selection cannot be null.
	 */
	public static final ISelection EMPTY_SELECTION = new EmptySelection();

	/** "Empty" or undefined selection. */
	public static class EmptySelection implements IStructuredSelection {
		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public Iterator<Object> iterator() {
			return new ArrayList<>().iterator();
		}

		@Override
		public List<Object> toList() {
			return new ArrayList<>();
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public Object getFirstElement() {
			return null;
		}

		@Override
		public Object[] toArray() {
			return new Object[0];
		}
	}

	// --- selection utilities ---

	/**
	 * Returns workbench selection, if any.
	 */
	public static ISelection getWorkbenchSelection() {
		ISelection result = null;
		IWorkbenchPage page = getWorkbenchPage();
		if (page != null) {
			result = page.getSelection();
		}
		return result;
	}

	/** Creates an ISelection from a collection of objects */
	public static ISelection toSelection(Collection<?> objects) {
		return (objects == null) ? null : new StructuredSelection(toList(objects));
	}

	/** Creates an ISelection from a list of objects */
	public static ISelection toSelection(List<?> objects) {
		return (objects == null) ? null : new StructuredSelection(objects);
	}

	/** Creates an ISelection from the specified object(s) */
	public static <T> ISelection toSelection(T... objects) {
		return (objects == null) ? null : new StructuredSelection(objects);
	}

	/**
	 * Gets number of top-level object(s) from an Eclipse ISelection.
	 */
	public static int getSelectionSize(ISelection selection) {
		int result = 0;
		if (selection == EMPTY_SELECTION) {
			// nothing to do
		} else if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			result = structuredSelection.size();
		} else if (selection instanceof ISelection) {
			result = selection.isEmpty() ? 0 : 1;
		}
		// collections are not ISelections, this just makes the method a little more generic
		else if (selection instanceof Collection) {
			Collection<?> collection = (Collection<?>) selection;
			result = collection.size();
		} else if (selection != null) {
			result = 1;
		}
		return result;
	}

	/**
	 * Gets selected object(s) from an Eclipse ISelection as a List.
	 * If selection is a multiple selection (an IStructuredSelection or Collection),
	 * the list contains the top-level elements of the selection.
	 * Otherwise the list contains the ISelection itself.
	 */
	public static List<Object> getSelectedObjects(ISelection selection) {
		List<Object> result = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			result = new ArrayList<Object>(structuredSelection.toList());
		} else if (selection instanceof Collection) {
			Collection<?> collection = (Collection<?>) selection;
			result = new ArrayList<>(collection);
		} else {
			List<Object> list = new ArrayList<>();
			list.add(selection);
			result = list;
		}
		return result;
	}

	/**
	 * Gets single selected object from an Eclipse ISelection.
	 * If selection is a single selection, returns it.
	 * If selection is multiple selection, returns first selected item.
	 */
	public static Object getSelectedObject(ISelection selection) {
		Object result = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			result = structuredSelection.getFirstElement();
		} else if (selection instanceof Collection) {
			Collection<?> collection = (Collection<?>) selection;
			Iterator<?> i = collection.iterator();
			if (i.hasNext())
				result = i.next();
		} else {
			result = selection;
		}
		return result;
	}

	/**
	 * Creates Eclipse ISelection from a list.
	 */
	public static ISelection toISelection(List<?> items) {
		return new StructuredSelection(items);
	}

	/**
	 * Creates Eclipse ISelection from one or more items or an array of items.
	 */
	public static <T> ISelection toISelection(T... items) {
		return new StructuredSelection(items);
	}

	/**
	 * Gets iterator for an ISelection.
	 * Note: returns null if ISelection is not an IStructuredSelection,
	 * which is the only interface that currently defines an Iterator.
	 */
	public static Iterator<?> getSelectionIterator(ISelection iselection) {
		Iterator<?> result = null;
		if (iselection instanceof IStructuredSelection) {
			result = ((IStructuredSelection) iselection).iterator();
		}
		return result;
	}

	// --- debugging tools ---

	/** Converts selection to string, for debug display. */
	public static String toString(ISelection selection) {
		String result = null;
		// convert selection to text string
		if (selection == null) {
			result = "No selection";
		} else if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			List<?> elements = structuredSelection.toList();
			int size = elements.size();
			if (size == 0) {
				result = "Empty selection";
			} else {
				result = "Selection: ";
				for (int i = 0; i < size; i++) {
					if (i > 0)
						result += "\n";
					Object o = elements.get(i);
					String type = o.getClass().getName();
					String value = o.toString();
					result += "[" + i + "]: type= + " + type + ", value='" + value + "'";
				}
			}
		} else {
			String type = selection.getClass().getName();
			String value = selection.toString();
			result = "Selection: (type = " + type + ") " + value;
		}
		return result;
	}

	// --- utilities ---

	/** Creates list from array/set of elements */
	public static List<Object> toList(Collection<?> collection) {
		int size = (collection == null) ? 0 : collection.size();
		List<Object> result = new ArrayList<>(size);
		if (collection != null)
			result.addAll(collection);
		return result;
	}

	/** Gets current Eclipse workbench */
	public static IWorkbench getWorkbench() {
		IWorkbench result = null;
		try {
			result = PlatformUI.getWorkbench();
		} catch (IllegalStateException e) {
			// Workbench is not defined for some reason. Oh well.
		}
		return result;
	}

	/** Gets current Eclipse workbench window.
	 *  Returns null if workbench does not exist.
	 */
	public static IWorkbenchWindow getWorkbenchWindow() {
		IWorkbenchWindow result = null;
		IWorkbench workbench = getWorkbench();
		if (workbench != null) {
			result = workbench.getActiveWorkbenchWindow();
			if (result == null) {
				if (workbench.getWorkbenchWindowCount() > 0) {
					result = workbench.getWorkbenchWindows()[0];
				}
			}
		}
		return result;
	}

	/** Gets current Eclipse workbench page */
	public static IWorkbenchPage getWorkbenchPage() {
		IWorkbenchWindow window = getWorkbenchWindow();
		return (window == null) ? null : window.getActivePage();
	}
}
