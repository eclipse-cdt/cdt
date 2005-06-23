/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
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
    private final static String CMD_DESCRIPTION_ELEM = "commandDescription"; //$NON-NLS-1$
    private final static String CMD_SI_ELEM = "commandScannerInfo"; //$NON-NLS-1$
    private final static String OPTION_ELEM = "option"; //$NON-NLS-1$
    private final static String SI_ITEM_ELEM = "siItem"; //$NON-NLS-1$
    private final static String KEY_ATTR = "key"; //$NON-NLS-1$
    private final static String VALUE_ATTR = "value"; //$NON-NLS-1$
    private final static String QUOTE_INCLUDE_ATTR = "quote"; //$NON-NLS-1$
    private final static String KIND_ATTR = "kind"; //$NON-NLS-1$
    
	private int commandId;
	private List compilerCommand;	// members are KVStringPair objects
	private boolean discovered;
	private boolean cppFileType;	// C or C++ file type

    private List symbols;
    private List includes;
    private List quoteIncludes;
    
    /**
	 * @param cppFileType2 
	 */
	public CCommandDSC(boolean cppFileType) {
		compilerCommand = new ArrayList();
		discovered = false;
		this.cppFileType = cppFileType;
        
        symbols = new ArrayList();
        includes = new ArrayList();
        quoteIncludes = new ArrayList();
	}

    public boolean appliesToCPPFileType() {
        return cppFileType;
    }
    
	public void addSCOption(KVStringPair option) {
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
    
	public String toString() {
		String commandAsString = new String();
		for (Iterator i = compilerCommand.iterator(); i.hasNext(); ) {
			KVStringPair optionPair = (KVStringPair)i.next();
			commandAsString += optionPair.getKey() + SINGLE_SPACE + 
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
			KVStringPair optionPair = (KVStringPair)i.next();
            if (optionPair.getKey().equals(SCDOptionsEnum.COMMAND.toString())) {
                commandAsString += optionPair.getValue() + SINGLE_SPACE;
            }
            else {
                // skip -include and -imacros options
    			if (optionPair.getKey().equals(SCDOptionsEnum.IMACROS_FILE.toString()) ||
    					optionPair.getKey().equals(SCDOptionsEnum.INCLUDE_FILE.toString()))
    				continue;
    			commandAsString += optionPair.getKey() + SINGLE_SPACE + 
                                   optionPair.getValue() + SINGLE_SPACE;
            }
		}
		return commandAsString.trim();
	}
	
	/**
	 * @return list of strings
	 */
	public List getImacrosFile() {
		List imacrosFiles = new ArrayList();
		for (Iterator i = compilerCommand.iterator(); i.hasNext(); ) {
			KVStringPair optionPair = (KVStringPair)i.next();
			if (optionPair.getKey().equals(SCDOptionsEnum.IMACROS_FILE.toString())) {
				imacrosFiles.add(optionPair.getValue());
			}
		}
		return imacrosFiles;
	}
	
	/**
	 * @return list of strings
	 */
	public List getIncludeFile() {
		List includeFiles = new ArrayList();
		for (Iterator i = compilerCommand.iterator(); i.hasNext(); ) {
			KVStringPair optionPair = (KVStringPair)i.next();
			if (optionPair.getKey().equals(SCDOptionsEnum.INCLUDE_FILE.toString())) {
				includeFiles.add(optionPair.getValue());
			}
		}
		return includeFiles;
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
     * @return Returns the includes as strings.
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
     * @return Returns the quote include paths as strings (for #include "...")
     */
    public List getQuoteIncludes() {
        return quoteIncludes;
    }
    /**
     * @param includes. Quote include paths (for #include "...")
     */
    public void setQuoteIncludes(List includes) {
        quoteIncludes = includes;
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
        Element cmdDescElem = doc.createElement(CMD_DESCRIPTION_ELEM);
        for (Iterator i = compilerCommand.iterator(); i.hasNext(); ) {
            Element optionElem = doc.createElement(OPTION_ELEM); 
            KVStringPair option = (KVStringPair) i.next();
            optionElem.setAttribute(KEY_ATTR, option.getKey()); 
            optionElem.setAttribute(VALUE_ATTR, option.getValue()); 
            cmdDescElem.appendChild(optionElem);
        }
        cmdElem.appendChild(cmdDescElem);
        // serialize includes and symbols
        Element siElem = doc.createElement(CMD_SI_ELEM);
        for (Iterator j = quoteIncludes.iterator(); j.hasNext(); ) {
            Element siItem = doc.createElement(SI_ITEM_ELEM); 
            siItem.setAttribute(KIND_ATTR, "INCLUDE_PATH");  //$NON-NLS-1$
            siItem.setAttribute(VALUE_ATTR, (String) j.next());
            siItem.setAttribute(QUOTE_INCLUDE_ATTR, "true"); //$NON-NLS-1$
            siElem.appendChild(siItem);
        }
        for (Iterator j = includes.iterator(); j.hasNext(); ) {
            Element siItem = doc.createElement(SI_ITEM_ELEM); 
            siItem.setAttribute(KIND_ATTR, "INCLUDE_PATH");  //$NON-NLS-1$
            siItem.setAttribute(VALUE_ATTR, (String) j.next());
            siElem.appendChild(siItem);
        }
        for (Iterator j = symbols.iterator(); j.hasNext(); ) {
            Element siItem = doc.createElement(SI_ITEM_ELEM);
            siItem.setAttribute(KIND_ATTR, "SYMBOL_DEFINITION"); //$NON-NLS-1$
            siItem.setAttribute(VALUE_ATTR, (String) j.next()); 
            siElem.appendChild(siItem);
        }
        cmdElem.appendChild(siElem);
    }

    /**
     * @param cmdElem
     */
    public void deserialize(Element cmdElem) {
        // read command options
        NodeList descList = cmdElem.getElementsByTagName(CMD_DESCRIPTION_ELEM);
        if (descList.getLength() > 0) {
            Element descElem = (Element) descList.item(0);
            NodeList optionList = descElem.getElementsByTagName(OPTION_ELEM);
            for (int i = 0; i < optionList.getLength(); ++i) {
                Element optionElem = (Element) optionList.item(i);
                String key = optionElem.getAttribute(KEY_ATTR);
                String value = optionElem.getAttribute(VALUE_ATTR);
                KVStringPair option = new KVStringPair(key, value);
                addSCOption(option);
            }
        }
        // read associated scanner info
        NodeList siList = cmdElem.getElementsByTagName(CMD_SI_ELEM);
        if (siList.getLength() > 0) {
            Element siElem = (Element) siList.item(0);
            NodeList siItemList = siElem.getElementsByTagName(SI_ITEM_ELEM);
            for (int i = 0; i < siItemList.getLength(); ++i) {
                Element siItemElem = (Element) siItemList.item(i);
                String kind = siItemElem.getAttribute(KIND_ATTR);
                String value = siItemElem.getAttribute(VALUE_ATTR);
                String quote = siItemElem.getAttribute(QUOTE_INCLUDE_ATTR);
                if (kind.equals("INCLUDE_PATH")) { //$NON-NLS-1$
                    if (quote.equals("true")) { //$NON-NLS-1$
                        quoteIncludes.add(value);
                    }
                    else {
                        includes.add(value);
                    }
                }
                else if (kind.equals("SYMBOL_DEFINITION")) { //$NON-NLS-1$
                    symbols.add(value);
                }
            }
            setDiscovered(true);
        }
    }

}
