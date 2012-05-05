/*******************************************************************************
 * Copyright (c) 2011, 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * Interface for accessing information about OS resources.
 *
 * <strong>EXPERIMENTAL</strong>.  This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 *
 * @since 4.2
 */
public interface IGDBHardwareAndOS2 extends IGDBHardwareAndOS {
	
	/** Returns true if information returned by the other methods is available.
	 * If false is returned, it means that session initialization is in progress,
	 * and further request should be made after DataModelInitializedEvent is fired.
	 */
	public boolean isAvailable();

	/** Information about OS resource class. */
	public interface IResourceClass
	{
		/** The id of this resource class, used in GDB requests. */
		public String getId();
		/** Human-friendly description of this class, suitable for direct display in UI. */
		public String getHumanDescription();
	}

    /**
     * Return a list of OS resource classes GDB knows about
     */
    public void getResourceClasses(IDMContext dmc, DataRequestMonitor<IResourceClass[]> rm);

    /** Information about OS resources of specific resource class
     * This is conceptually a table. GDB provides column headers, and
     * data rows, but does not provide any additional information about
     * the meaning
     */
    public interface IResourcesInformation
    {
    	/** Return the names of the columns in resource table. */
    	public String[] getColumnNames();
    	/** Returns rows of the resource table. Each element is an array
    	 * of the size equal to getColumnNames().length
    	 */
    	public String[][] getContent();
    }

    /**
     * Return table describing resources of specified class.
     */
    void getResourcesInformation(IDMContext dmc, String resourceClassId, DataRequestMonitor<IResourcesInformation> rm);
}
