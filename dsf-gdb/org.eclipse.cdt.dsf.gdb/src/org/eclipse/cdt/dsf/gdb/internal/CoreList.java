/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Vector;

import org.eclipse.cdt.internal.core.ICoreInfo;

/**
 */
public class CoreList {

	private ICoreInfo[] fCoreList;
	private String fCoreFileName;
	
	public CoreList() {
		fCoreFileName = "/proc/cpuinfo"; //$NON-NLS-1$
	}
	
	public CoreList(String fileName) {
		fCoreFileName = fileName;
	}
	
	/**
	 * Returns the list of cores as shown in /proc/cpuinfo
	 * This method will only parse /proc/cpuinfo once and cache
	 * the result.  To force a re-parse, one must create a new
	 * CoreList object. 
	 */
	public ICoreInfo[] getCoreList()  {
		if (fCoreList != null) {
			return fCoreList;
		}
		
		File cpuInfo = new File(fCoreFileName);

		Vector<ICoreInfo> coreInfo = new Vector<ICoreInfo>();
        BufferedReader reader = null;
        try {
        	String processorId = null;
        	String physicalId = null;
        	
            Reader r = new InputStreamReader(new FileInputStream(cpuInfo));
            reader = new BufferedReader(r);
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("processor")) { //$NON-NLS-1$
                	if (processorId != null) {
                		// We are already at the next 'processor' entry, without
                		// having found the 'physical id' entry.  This means
                		// there is a single physical CPU.
                		physicalId = "0";  //$NON-NLS-1$

                		coreInfo.add(new CoreInfo(processorId, physicalId));
                		processorId = null;
                	}
                	// Found the processor id of this core, so store it temporarily
                	processorId = line.split(":")[1].trim();  //$NON-NLS-1$
                } else if (line.startsWith("physical id")) { //$NON-NLS-1$
                	// Found the physical id of this core, so store it temporarily
                	
                	assert physicalId == null;
                	physicalId = line.split(":")[1].trim();  //$NON-NLS-1$
                	
	            	coreInfo.add(new CoreInfo(processorId, physicalId));
	            	
	            	// Get ready to look for the next core.
	            	processorId = null;
	            	physicalId = null;
	            }
            }
            if (processorId != null) {
            	// This will happen when there is no 'physical id' field
        		coreInfo.add(new CoreInfo(processorId, "0"));  //$NON-NLS-1$
            }
		} catch (IOException e) {
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {/* Don't care */}
			reader = null;
		}
        
		fCoreList = coreInfo.toArray(new ICoreInfo[coreInfo.size()]);
		return fCoreList;
	}
}
