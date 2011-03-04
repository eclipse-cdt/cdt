/*****************************************************************
 * Copyright (c) 2011 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format (Bug 202556)
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.update;

import java.util.Set;

/**
 * An event that indicates element format is changed. Even when a viewer is
 * configured to be in a manual update mode, there is a need to update of the
 * labels/states of the element.
 * 
 * @since 2.2
 */
public class ElementFormatEvent {
	protected Set<Object> elements;
	protected int applyDepth;

	/**
	 * Constructor
	 * @param elements the elements that have their formats changed
	 * @param applyDepth how deep each of the elements apply to itself and their child elements.
	 *        -1 - recursively apply child elements to an infinite depth;
	 *         0 - does not apply to the element itself and its child elements;
	 *         1 - apply to the element itself only;
	 *         2 - apply to the element, its direct children and grand-children;
	 *         and so on for other positive numbers.
	 */
	public ElementFormatEvent(Set<Object> elements, int applyDepth) {
		this.elements = elements;
		this.applyDepth = applyDepth;
	}

	/**
	 * Get the elements that has formats changed.
	 * 
	 * @return the elements
	 */
	public Set<Object> getElements() {
		return elements;
	}

	/**
	 * Get the depth that how each of the elements apply to itself and their
	 * child elements.
	 * 
	 * @return the apply depth.
	 */
	public int getApplyDepth() {
		return applyDepth;
	}
}