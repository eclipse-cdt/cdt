/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class that represents a compiler command and related scanner configuration 
 * 
 * @author vhirsl
 */
public class CCommandDSC {
    private final static String SINGLE_SPACE = " "; //$NON-NLS-1$
    
	private static int ids = 0;
	private int commandId;
	private List compilerCommand;	// members are KVPair objects
	private boolean discovered;
//	private List files;				// list of files this command applies to
	private boolean cppFileType;	// C or C++ file type
	// TODO add discovered scanner config
	/**
	 * 
	 */
	public CCommandDSC() {
		compilerCommand = new ArrayList();
		discovered = false;
//		files = null;
		cppFileType = false;	// assume C file type
		commandId = ++ids;
	}

	public void addSCOption(KVPair option) {
		compilerCommand.add(option);
	}
	
    /**
     * @return
     */
    public Integer getCommandIdAsInteger() {
        return new Integer(getCommandId());
    }
    /**
     * @return Returns the commandId.
     */
    public int getCommandId() {
        return commandId;
    }
    /**
     * @param commandId The commandId to set.
     */
    public void setCommandId(int commandId) {
        this.commandId = commandId;
    }
    
//	public void addFile(String fileName) {
//		if (files == null) {
//			files = new ArrayList();
//		}
//		if (!files.contains(fileName)) {
//			files.add(fileName);
//			if (!cppFileType && !fileName.endsWith(".c")) { //$NON-NLS-1$
//				cppFileType = true;
//			}
//		}
//	}
	
//	public int getNumberOfFiles() {
//		if (files == null) return 0;
//		return files.size();
//	}
	
	public String toString() {
		String commandAsString = new String();
		for (Iterator i = compilerCommand.iterator(); i.hasNext(); ) {
			KVPair optionPair = (KVPair)i.next();
			commandAsString += optionPair.getKey().toString() + SINGLE_SPACE + 
                               optionPair.getValue() + SINGLE_SPACE;
		}
		return commandAsString.trim();
	}
	
	public int getId() {
		return commandId;
	}
	
	/**
	 * Returns a command where -imacros and -include options have been removed
	 * @return
	 */
	public String getSCDRunnableCommand() {
		String commandAsString = new String();
		for (Iterator i = compilerCommand.iterator(); i.hasNext(); ) {
			KVPair optionPair = (KVPair)i.next();
			if (optionPair.getKey().equals(SCDOptionsEnum.IMACROS_FILE) ||
					optionPair.getKey().equals(SCDOptionsEnum.INCLUDE_FILE))
				continue;
			commandAsString += optionPair.getKey().toString() + SINGLE_SPACE + 
                               optionPair.getValue() + SINGLE_SPACE;
		}
		return commandAsString.trim();
	}
	
	public String[] getImacrosFile() {
		List imacrosFiles = new ArrayList();
		for (Iterator i = compilerCommand.iterator(); i.hasNext(); ) {
			KVPair optionPair = (KVPair)i.next();
			if (optionPair.getKey().equals(SCDOptionsEnum.IMACROS_FILE)) {
				imacrosFiles.add(optionPair.getValue());
			}
		}
		return (String[]) imacrosFiles.toArray(new String[imacrosFiles.size()]);
	}
	
	public String[] getIncludeFile() {
		List includeFiles = new ArrayList();
		for (Iterator i = compilerCommand.iterator(); i.hasNext(); ) {
			KVPair optionPair = (KVPair)i.next();
			if (optionPair.getKey().equals(SCDOptionsEnum.INCLUDE_FILE)) {
				includeFiles.add(optionPair.getValue());
			}
		}
		return (String[]) includeFiles.toArray(new String[includeFiles.size()]);
	}
	
//	public List getFilesList() {
//		return files;
//	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(this.getClass())) {
			return compilerCommand.equals(((CCommandDSC)arg0).compilerCommand);
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return compilerCommand.hashCode();
	}

}
