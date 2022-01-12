/*****************************************************************
 * Copyright (c) 2011, 2014 Texas Instruments and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format (Bug 202556)
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.update;

import java.util.Set;

/**
 * An event that indicates the format of certain elements has changed.
 * Even when a viewer is configured to be in a manual update mode, there is a need to
 * update the labels/states of elements.
 *
 * @since 2.2
 */
public class ElementFormatEvent {
	protected Set<Object> elements;
	protected int applyDepth;

	/**
	 * Constructor
	 * @param elems The set of elements that have their formats changed
	 * @param depth The depth to which the change of format applies, with respect to the
	 *              affected elements and their children:
	 *        -1 - recursively applies to the element and its children to an infinite depth;
	 *         0 - does not apply to the element itself or its children elements;
	 *         1 - applies to the element itself only;
	 *         2 - apply to the element, its direct children and grand-children;
	 *         and so on for other positive numbers.
	 */
	public ElementFormatEvent(Set<Object> elems, int depth) {
		elements = elems;
		applyDepth = depth;
	}

	/**
	 * Get the elements for which the format has changed.
	 *
	 * @return the elements
	 */
	public Set<Object> getElements() {
		return elements;
	}

	/**
	 * Get the depth to which the change of format applies, with respect to the
	 * affected elements and their children.
	 *
	 * @return the apply depth.
	 */
	public int getApplyDepth() {
		return applyDepth;
	}
}