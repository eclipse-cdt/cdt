/*******************************************************************************
 * Copyright (c) 2011, 2013 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *     Marc Dumais (Ericsson) - Add CPU/core load information to the multicore visualizer (Bug 396268)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import java.util.Map;

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
	public interface IResourceClass {
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
	public interface IResourcesInformation {
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

	/**
	 * Information about the CPU/core load for one given CPU or core
	 */
	public interface ILoadInfo {
		/**
		 * A string representing the current load (between "0" and "100")
		 */
		public String getLoad();

		/**
		 * Used to give more details about a CPU's/core's load.  For instance
		 * the breakdown of the different load types and their proportion: system,
		 * user, I/O, interrupts, etc.
		 */
		public Map<String, String> getDetailedLoad();
	}

	/**
	 * Computes CPU/core load information according to context and
	 * asynchronously returns the result in a ILoadInfo object
	 */
	void getLoadInfo(IDMContext dmc, DataRequestMonitor<ILoadInfo> rm);
}
