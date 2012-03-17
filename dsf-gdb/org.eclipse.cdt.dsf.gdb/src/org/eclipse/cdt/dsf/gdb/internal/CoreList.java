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
        	String physicalId = null;
        	String coreId = null;
        	String cpuCores = null;
        	
            Reader r = new InputStreamReader(new FileInputStream(cpuInfo));
            reader = new BufferedReader(r);
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("physical id")) { //$NON-NLS-1$
                	// Found the physical id of this core, so store it temporarily
                	physicalId = line.split(":")[1].trim();  //$NON-NLS-1$
                } else if (line.startsWith("core id")) { //$NON-NLS-1$
                	// Found core id of this core which come after the entry
                	// for physical id, so we have both now.
                	coreId = line.split(":")[1].trim(); //$NON-NLS-1$
	            } else if (line.startsWith("cpu cores")) { //$NON-NLS-1$
	            	// Found CPU core count which comes after the entry
	            	// for core id, so we have all three by now.
	            	cpuCores = line.split(":")[1].trim(); //$NON-NLS-1$
	            	
	            	int cid = Integer.parseInt(coreId);
	            	int pid = Integer.parseInt(physicalId);
	            	int cores_per_pid = Integer.parseInt(cpuCores);
	            	String absoluteCoreID = Integer.toString(cid + pid * cores_per_pid);
	            	
	            	coreInfo.add(new CoreInfo(absoluteCoreID, physicalId));
	            	
	            	// Get ready to look for the next core.
	            	physicalId = null;
	            	coreId = null;
	            	cpuCores = null;
	            }
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
