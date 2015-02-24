/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - initial API and implementation (bug 460737)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui;

/**
 * @since 1.3
 */
public interface IVisualizer2 extends IVisualizer {
	
	/**
	 * Invoked when the visualizer is created, to permit any initialization.
	 * This version provides the visualizer with the secondary Part id of 
	 * its Visualizer View. This is a string containing an instance number for 
	 * cloned views, or null for the first instance. This lets each visualizer 
	 * know which instance it is of itself. Intended to be overridden. Default 
	 * implementation does nothing.
	 * 
	 * @since 1.3
	 */
	public void initializeVisualizer(String secondaryPartId);

}
