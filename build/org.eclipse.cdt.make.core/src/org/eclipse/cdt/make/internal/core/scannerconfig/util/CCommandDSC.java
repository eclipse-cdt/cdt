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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
	private IProject project;

    private List symbols;
    private List includes;
    private List quoteIncludes;
    
    /**
	 * @param cppFileType2 
	 */
	public CCommandDSC(boolean cppFileType) {
		this(cppFileType, null);
	}
	
	public CCommandDSC(boolean cppFileType, IProject project) {
		compilerCommand = new ArrayList();
		discovered = false;
		this.cppFileType = cppFileType;
        
        symbols = new ArrayList();
        includes = new ArrayList();
        quoteIncludes = new ArrayList();
        this.project = project;
	}

    public boolean appliesToCPPFileType() {
        return cppFileType;
    }
    
	public void addSCOption(KVStringPair option) {
		if (project != null &&
			(option.getKey().equals(SCDOptionsEnum.INCLUDE_FILE.toString()) ||
			 option.getKey().equals(SCDOptionsEnum.INCLUDE.toString()) ||
			 option.getKey().equals(SCDOptionsEnum.IDASH.toString()) ||
			 option.getKey().equals(SCDOptionsEnum.IMACROS_FILE.toString())))
		{
			String value = option.getValue();
			value = (String)CygpathTranslator.translateIncludePaths(project, Collections.singletonList(value)).get(0);
			value = makeRelative(project, new Path(value)).toOSString();
			option = new KVStringPair(option.getKey(), value);
		}
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
			String value = optionPair.getValue();
			commandAsString += optionPair.getKey() + SINGLE_SPACE + 
                               value + SINGLE_SPACE;
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
	public String getSCDRunnableCommand(boolean quoteIncludePaths) {
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
    			String value = optionPair.getValue();
    			if (optionPair.getKey().equals(SCDOptionsEnum.INCLUDE.toString()) ||
    				optionPair.getKey().equals(SCDOptionsEnum.IDASH.toString())) {
    				value = makeAbsolute(project, value);
    			}
    			if (quoteIncludePaths) {
    				if (optionPair.getKey().equals(SCDOptionsEnum.INCLUDE.toString()) ||
    					optionPair.getKey().equals(SCDOptionsEnum.IDASH.toString())) {
    					commandAsString += optionPair.getKey() + SINGLE_SPACE + 
    							"\"" + value + "\"" + SINGLE_SPACE;  //$NON-NLS-1$//$NON-NLS-2$
    				}
    			}
    			else if (optionPair.getKey().equals(SCDOptionsEnum.INCLUDE.toString())) {
	    			commandAsString += optionPair.getKey() + SINGLE_SPACE + 
	    					value + SINGLE_SPACE;
    			}
    			else {
	    			commandAsString += optionPair.getKey() + SINGLE_SPACE + 
	    					value + SINGLE_SPACE;
    			}
            }
		}
		return commandAsString.trim();
	}
	
	/**
	 * Returns the compiler command
	 * @return
	 */
	public String getCompilerName() {
		String compiler = new String();
		for (Iterator i = compilerCommand.iterator(); i.hasNext(); ) {
			KVStringPair optionPair = (KVStringPair)i.next();
            if (optionPair.getKey().equals(SCDOptionsEnum.COMMAND.toString())) {
            	compiler = optionPair.getValue();
            	break;
            }
		}
		return compiler.trim();
	}
	
	/**
	 * @return list of strings
	 */
	public List getImacrosFile() {
		List imacrosFiles = new ArrayList();
		for (Iterator i = compilerCommand.iterator(); i.hasNext(); ) {
			KVStringPair optionPair = (KVStringPair)i.next();
			if (optionPair.getKey().equals(SCDOptionsEnum.IMACROS_FILE.toString())) {
				imacrosFiles.add(makeAbsolute(project,optionPair.getValue()));
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
				includeFiles.add(makeAbsolute(project,optionPair.getValue()));
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
        return makeAbsolute(project, includes);
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
        return makeAbsolute(project, quoteIncludes);
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
    
    public void resolveOptions(IProject project) {
    	ArrayList symbols = new ArrayList();
    	ArrayList includes = new ArrayList();
    	ArrayList quoteincludes = new ArrayList();
		for (Iterator options = compilerCommand.iterator(); options.hasNext(); ) {
			KVStringPair optionPair = (KVStringPair)options.next();
			String key = optionPair.getKey();
			String value = optionPair.getValue();
			if (key.equals(SCDOptionsEnum.INCLUDE.toString())) {
				includes.add(value);
			}
			else if (key.equals(SCDOptionsEnum.IDASH.toString())) {
				quoteincludes.add(value);
			}
			else if (key.equals(SCDOptionsEnum.DEFINE.toString())) {
				symbols.add(value);
			}
		}
		setIncludes(includes);
		setQuoteIncludes(quoteincludes);		                
		setSymbols(symbols);
        
		setDiscovered(true);    	
    }
    
	public static IPath makeRelative(IProject project, IPath path) {
		IResource resource = findResource(project, path);
		if (resource != null) {
			if (resource.getProject() == project) {
				path = resource.getProjectRelativePath();
			}
//			else {
//				path = resource.getFullPath();
//			}
		}
		return path;
	}

	private static IResource findResource(IProject project, IPath path) {
		IResource resource = project.findMember(path, true);
		if (resource == null) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			resource = root.findMember(path, true);
			if (resource == null) {
				IResource[] resources = root.findFilesForLocation(path);
				if (resources != null) {
					for (int i = 0; i < resources.length; i++) {
						if (resources[i].getProject() == project) {
							resource = resources[i];
							break;
						}
					}
					// make a relative path to another project (better than an absolute path)
					if (resource == null && resources.length > 0) {
						resource = resources[0];
					}
				}
			}
		}
		return resource;
	}

	public static List makeRelative(IProject project, List paths) {
		List list = new ArrayList(paths.size());
		for (Iterator iter=paths.iterator(); iter.hasNext(); ) {
			String path = (String)iter.next();
			path = makeRelative(project, new Path(path)).toOSString();
			list.add(path);
		}
		return list;
	}


	public static final String makeAbsolute(IProject project, String path) {
		if (project != null && !new Path(path).isAbsolute()) {
			path = new File(project.getLocation().toOSString(), path).getAbsolutePath();
		}
		return path;
	}

	public static List makeAbsolute(IProject project, List paths) {
		List list = new ArrayList(paths.size());
		for (Iterator iter=paths.iterator(); iter.hasNext(); ) {
			String path = (String)iter.next();
			path = makeAbsolute(project, path);
			list.add(path);
		}
		return list;
	}
}
