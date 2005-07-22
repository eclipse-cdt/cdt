/*******************************************************************************
 * Copyright (c) 2000 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Provides utilities for checking the validity of selections.
 * <p>
 * This class provides static methods only; it is not intended to be instantiated
 * or subclassed.
 * </p>
 */

public class SelectionUtil {

    /**
     * Returns the first element of the given selection.
     * Returns null if the selection is empty or if
     * the given selection is not of type <code>IStructuredSelection</code>.
     *
     * @param selection the selection
     * @return the selected elements
     *
     */
    public static Object getFirstElement (ISelection selection) {
	if (!(selection instanceof IStructuredSelection)) {
	    return null;
	}
	return ((IStructuredSelection)selection).getFirstElement ();
    }

    public static Object getSingleElement (ISelection s) {
	if (!(s instanceof IStructuredSelection))
	    return null;
	IStructuredSelection selection= (IStructuredSelection)s;
	if (selection.size () != 1)
	    return null;
	return selection.getFirstElement ();
    }

    /**
     * Returns the elements of the given selection.
     * Returns an empty array if the selection is empty or if
     * the given selection is not of type <code>IStructuredSelection</code>.
     *
     * @param selection the selection
     * @return the selected elements
     *
     */

    public static Object[] toArray(ISelection selection) {
	if (!(selection instanceof IStructuredSelection)) {
	    return new Object[0];
	}
	return ((IStructuredSelection)selection).toArray();
    }

    public static List toList(ISelection selection) {
	if (selection instanceof IStructuredSelection) {
	    return ((IStructuredSelection)selection).toList();
	}
	return null;
    }

    /**
     * Returns whether the types of the resources in the given selection are among
     * the specified resource types.
     *
     * @param selection the selection
     * @param resourceMask resource mask formed by bitwise OR of resource type
     *   constants (defined on <code>IResource</code>)
     * @return <code>true</code> if all selected elements are resources of the right
     *  type, and <code>false</code> if at least one element is either a resource
     *  of some other type or a non-resource
     * @see IResource#getType
     */
    public static boolean allResourcesAreOfType(IStructuredSelection selection, int resourceMask) {
	Iterator resources = selection.iterator();
	while (resources.hasNext()) {
	    Object next = resources.next();
	    if (!(next instanceof IResource))
		return false;
	    if (!resourceIsType((IResource)next, resourceMask))
		return false;
	}
	return true;
    }

    /**
     * Returns whether the type of the given resource is among the specified
     * resource types.
     *
     * @param resource the resource
     * @param resourceMask resource mask formed by bitwise OR of resource type
     *   constants (defined on <code>IResource</code>)
     * @return <code>true</code> if the resources has a matching type, and
     *   <code>false</code> otherwise
     * @see IResource#getType
     */
    public static boolean resourceIsType(IResource resource, int resourceMask) {
	return ((resource != null) && ((resource.getType() & resourceMask) != 0));
    }

    /* (non-Javadoc)
     * Private constructor to block instantiation.
     */
    private SelectionUtil(){
    }
}
