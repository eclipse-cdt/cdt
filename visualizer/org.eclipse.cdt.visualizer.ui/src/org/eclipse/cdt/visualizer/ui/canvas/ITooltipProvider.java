/*******************************************************************************
 * Copyright (c) 2014 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
