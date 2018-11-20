/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *     Marc Dumais (Ericsson) - bug 464184
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Vector;

import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2.IResourcesInformation;
import org.eclipse.cdt.internal.core.ICoreInfo;

/**
 */
public class CoreList {

	private ICoreInfo[] fCoreList;
	private String fCoreFileName;
	private IResourcesInformation fResourcesInfo;

	private static final String PROCESSOR_ID_STR = "processor"; //$NON-NLS-1$
	private static final String PHYSICAL_ID_STR = "physical id"; //$NON-NLS-1$

	/** Default constructor - assumes the info comes from file /proc/cpuinfo */
	public CoreList() {
		fCoreFileName = "/proc/cpuinfo"; //$NON-NLS-1$
	}

	/** Alternate constructor - info comes from a file passed as parameter */
	public CoreList(String fileName) {
		fCoreFileName = fileName;
	}

	/**
	 * Alternate constructor - info comes from IResourcesInformation object,
	 *  that was obtained from GDB
	 */
	public CoreList(IResourcesInformation info) {
		fResourcesInfo = info;
	}

	/** Returns the list of cores. The core information will only
	 *  be parsed once and the result cached. To force a re-parse,
	 *  one must create a new CoreList object. */
	public ICoreInfo[] getCoreList() {

		// already parsed info?
		if (fCoreList != null) {
			return fCoreList;
		}
		if (fCoreFileName != null) {
			getCoreListFromFile();
		} else if (fResourcesInfo != null) {
			getCoreListFromResourceInfo();
		}
		return fCoreList;
	}

	/**
	 * This method will only parse /proc/cpuinfo once and cache
	 * the result.  To force a re-parse, one must create a new
	 * CoreList object.
	 */
	private void getCoreListFromFile() {
		File cpuInfo = new File(fCoreFileName);

		Vector<ICoreInfo> coreInfo = new Vector<>();
		BufferedReader reader = null;
		try {
			String processorId = null;
			String physicalId = null;

			Reader r = new InputStreamReader(new FileInputStream(cpuInfo));
			reader = new BufferedReader(r);
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith(PROCESSOR_ID_STR)) {
					if (processorId != null) {
						// We are already at the next 'processor' entry, without
						// having found the 'physical id' entry.  This means
						// there is a single physical CPU.
						physicalId = "0"; //$NON-NLS-1$

						coreInfo.add(new CoreInfo(processorId, physicalId));
						processorId = null;
					}
					// Found the processor id of this core, so store it temporarily
					processorId = line.split(":")[1].trim(); //$NON-NLS-1$
				} else if (line.startsWith(PHYSICAL_ID_STR)) {
					// Found the physical id of this core, so store it temporarily

					assert physicalId == null;
					physicalId = line.split(":")[1].trim(); //$NON-NLS-1$

					coreInfo.add(new CoreInfo(processorId, physicalId));

					// Get ready to look for the next core.
					processorId = null;
					physicalId = null;
				}
			}
			if (processorId != null) {
				// This will happen when there is no 'physical id' field
				coreInfo.add(new CoreInfo(processorId, "0")); //$NON-NLS-1$
			}
		} catch (IOException e) {
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				/* Don't care */}
			reader = null;
		}

		fCoreList = coreInfo.toArray(new ICoreInfo[coreInfo.size()]);
	}

	private void getCoreListFromResourceInfo() {
		Vector<ICoreInfo> coreInfo = new Vector<>();

		int processorIdIndex = -1;
		int physicalIdIndex = -1;
		String processorId = null;
		String physicalId = null;

		int column = 0;
		// find the indexes of the columns that we need
		for (String col : fResourcesInfo.getColumnNames()) {
			if (col.equalsIgnoreCase(PROCESSOR_ID_STR)) {
				processorIdIndex = column;
			} else if (col.equalsIgnoreCase(PHYSICAL_ID_STR)) {
				physicalIdIndex = column;
			}
			column++;
		}

		if (processorIdIndex >= 0) {
			// for each entry
			for (String[] line : fResourcesInfo.getContent()) {
				processorId = line[processorIdIndex];

				if (physicalIdIndex >= 0) {
					physicalId = line[physicalIdIndex];
				}
				// This will happen when there is no 'physical id' field. This means
				// there is a single physical CPU.
				else {
					physicalId = "0"; //$NON-NLS-1$
				}
				coreInfo.add(new CoreInfo(processorId, physicalId));

				// Get ready to look for the next core.
				processorId = null;
				physicalId = null;
			}
		}

		fCoreList = coreInfo.toArray(new ICoreInfo[coreInfo.size()]);
	}
}
