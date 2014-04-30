/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model;

import org.eclipse.ui.services.IDisposable;


/** Interface that defines a visualizer model data source */
public interface IVisualizerModelDataSource extends IDisposable {
       /** Unique identifier for this model data source */
       public String getId();

       @Override
       public void dispose();

       /** Adds an event listener. */
       public void addServiceEventListener(Object listener);

       /** Removes an event listener. */
       public void removeServiceEventListener(Object listener);

       /** Removes all event listeners. */
       public void removeAllServiceEventListeners();

       /** Returns whether the data source is ready to answer queries */
       public boolean isAvailable();
}
