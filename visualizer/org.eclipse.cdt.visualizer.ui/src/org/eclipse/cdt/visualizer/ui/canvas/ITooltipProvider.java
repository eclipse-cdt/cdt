/*******************************************************************************
 * Copyright (c) 2014 Ericsson.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - initial API and implementation
 *     Xavier Raynaud (Kalray) - Bug 430804
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.canvas;

/**
 * Interface that can be implemented by objects that want to provide tooltips.
 * @since 1.1
 */
public interface ITooltipProvider {
	/**
	 * Return the tooltip to display when mouse stays on this object.
	 * It may return <code>null</code> if there is nothing to display.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the tooltip to display on this object.
	 */
	public String getTooltip(int x, int y);
}
