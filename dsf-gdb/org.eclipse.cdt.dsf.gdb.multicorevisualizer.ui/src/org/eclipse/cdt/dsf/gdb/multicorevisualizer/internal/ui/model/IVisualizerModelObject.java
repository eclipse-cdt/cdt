/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation (Bug 405390)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model;

/**
 * Common interface for visualizer model objects
 */
public interface IVisualizerModelObject {
	/** Get the ID of this model object */	
	public int getID();
	
	/** Get the parent of this model object*/
	public IVisualizerModelObject getParent();	
	
	/** Compare two IVisualizerModelObject */
	public int compareTo(IVisualizerModelObject o);
}
