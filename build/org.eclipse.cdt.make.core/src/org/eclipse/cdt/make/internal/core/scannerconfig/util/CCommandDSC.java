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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Class that represents a compiler command and related scanner configuration 
 * 
 * @author vhirsl
 */
public class CCommandDSC {
    private final static String SINGLE_SPACE = " "; //$NON-NLS-1$
    
	private int commandId;
	private List compilerCommand;	// members are KVPair objects
	private boolean discovered;
	private boolean cppFileType;	// C or C++ file type

    private List symbols;
    private List includes;
    
    /**
	 * @param cppFileType2 
	 */
	public CCommandDSC(boolean cppFileType) {
		compilerCommand = new ArrayList();
		discovered = false;
//		files = null;
		this.cppFileType = cppFileType;
        
        symbols = new ArrayList();
        includes = new ArrayList();
	}

    public boolean appliesToCPPFileType() {
        return cppFileType;
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
            if (optionPair.getKey().equals(SCDOptionsEnum.COMMAND)) {
                commandAsString += optionPair.getValue() + SINGLE_SPACE;
            }
            else {
//    			if (optionPair.getKey().equals(SCDOptionsEnum.IMACROS_FILE) ||
//    					optionPair.getKey().equals(SCDOptionsEnum.INCLUDE_FILE))
//    				continue;
    			commandAsString += optionPair.getKey().toString() + SINGLE_SPACE + 
                                   optionPair.getValue() + SINGLE_SPACE;
            }
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
            CCommandDSC other = (CCommandDSC)arg0;
			return (compilerCommand.equals(other.compilerCommand) &&
                cppFileType == other.cppFileType);
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return compilerCommand.hashCode();
	}

    /**
     * @return Returns the includes.
     */
    public List getIncludes() {
        return includes;
    }
    /**
     * @param includes The includes to set.
     */
    public void setIncludes(List includes) {
        this.includes = includes;
    }
    /**
     * @return Returns the symbols.
     */
    public List getSymbols() {
        return symbols;
    }
    /**
     * @param symbols The symbols to set.
     */
    public void setSymbols(List symbols) {
        this.symbols = symbols;
    }
    /**
     * @return Returns the discovered.
     */
    public boolean isDiscovered() {
        return discovered;
    }
    /**
     * @param discovered The discovered to set.
     */
    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }

    /**
     * @param cmdElem
     */
    public void serialize(Element cmdElem) {
        Document doc = cmdElem.getOwnerDocument();
        // serialize the command
        Element cmdDescElem = doc.createElement("commandDescription"); //$NON-NLS-1$
        for (Iterator i = compilerCommand.iterator(); i.hasNext(); ) {
            Element optionElem = doc.createElement("option"); //$NON-NLS-1$
            KVPair option = (KVPair) i.next();
            optionElem.setAttribute("key", option.getKey().toString()); //$NON-NLS-1$
            optionElem.setAttribute("value", option.getValue()); //$NON-NLS-1$
            cmdDescElem.appendChild(optionElem);
        }
        cmdElem.appendChild(cmdDescElem);
        // serialize includes and symbols
        Element siElem = doc.createElement("commandScannerInfo"); //$NON-NLS-1$
        for (Iterator j = includes.iterator(); j.hasNext(); ) {
            Element siItem = doc.createElement("siItem"); //$NON-NLS-1$
            siItem.setAttribute("kind", "INCLUDE_PATH"); //$NON-NLS-1$ //$NON-NLS-2$
            siItem.setAttribute("value", (String) j.next()); //$NON-NLS-1$
            siElem.appendChild(siItem);
        }
        for (Iterator j = symbols.iterator(); j.hasNext(); ) {
            Element siItem = doc.createElement("siItem"); //$NON-NLS-1$
            siItem.setAttribute("kind", "SYMBOL_DEFINITION"); //$NON-NLS-1$ //$NON-NLS-2$
            siItem.setAttribute("value", (String) j.next()); //$NON-NLS-1$
            siElem.appendChild(siItem);
        }
        cmdElem.appendChild(siElem);
    }

    /**
     * @param cmdElem
     */
    public void deserialize(Element cmdElem) {
        // read command options
        NodeList descList = cmdElem.getElementsByTagName("commandDescription");
        if (descList.getLength() > 0) {
            Element descElem = (Element) descList.item(0);
            NodeList optionList = descElem.getElementsByTagName("option");
            for (int i = 0; i < optionList.getLength(); ++i) {
                Element optionElem = (Element) optionList.item(i);
                String key = optionElem.getAttribute("key");
                SCDOptionsEnum eKey = SCDOptionsEnum.getSCDOptionsEnum(key);
                String value = optionElem.getAttribute("value");
                KVPair option = new KVPair(eKey, value);
                addSCOption(option);
            }
        }
        // read associated scanner info
        NodeList siList = cmdElem.getElementsByTagName("commandScannerInfo");
        if (siList.getLength() > 0) {
            Element siElem = (Element) siList.item(0);
            NodeList siItemList = siElem.getElementsByTagName("siItem");
            for (int i = 0; i < siItemList.getLength(); ++i) {
                Element siItemElem = (Element) siItemList.item(i);
                String kind = siItemElem.getAttribute("kind");
                String value = siItemElem.getAttribute("value");
                if (kind.equals("INCLUDE_PATH")) {
                    includes.add(value);
                }
                else if (kind.equals("SYMBOL_DEFINITION")) {
                    symbols.add(value);
                }
            }
            setDiscovered(true);
        }
    }
}
