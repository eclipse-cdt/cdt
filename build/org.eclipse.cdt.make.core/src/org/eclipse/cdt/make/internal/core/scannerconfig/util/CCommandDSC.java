/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Gerhard Schaber (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.core.resources.ResourceLookup;
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
    protected final static String SINGLE_SPACE = " "; //$NON-NLS-1$
    protected final static String CMD_DESCRIPTION_ELEM = "commandDescription"; //$NON-NLS-1$
    protected final static String CMD_SI_ELEM = "commandScannerInfo"; //$NON-NLS-1$
    protected final static String OPTION_ELEM = "option"; //$NON-NLS-1$
    protected final static String SI_ITEM_ELEM = "siItem"; //$NON-NLS-1$
    protected final static String KEY_ATTR = "key"; //$NON-NLS-1$
    protected final static String VALUE_ATTR = "value"; //$NON-NLS-1$
    protected final static String QUOTE_INCLUDE_ATTR = "quote"; //$NON-NLS-1$
    protected final static String KIND_ATTR = "kind"; //$NON-NLS-1$
    
	protected int commandId;
	protected List<KVStringPair> compilerCommand;	// members are KVStringPair objects
	protected boolean discovered;
	protected boolean cppFileType;	// C or C++ file type
	protected IProject project;

    protected List<String> symbols;
    protected List<String> includes;
    protected List<String> quoteIncludes;

	public CCommandDSC(boolean cppFileType) {
		this(cppFileType, null);
	}
	
	public CCommandDSC(boolean cppFileType, IProject project) {
		compilerCommand = new ArrayList<KVStringPair>();
		discovered = false;
		this.cppFileType = cppFileType;
        
        symbols = new ArrayList<String>();
        includes = new ArrayList<String>();
        quoteIncludes = new ArrayList<String>();
        this.project = project;
	}

    public boolean appliesToCPPFileType() {
        return cppFileType;
    }
    
	public void addSCOption(KVStringPair option) {
		if (project != null &&
			(option.getKey().equals(SCDOptionsEnum.INCLUDE_FILE.toString()) ||
			 option.getKey().equals(SCDOptionsEnum.INCLUDE.toString()) ||
			 option.getKey().equals(SCDOptionsEnum.ISYSTEM.toString()) ||
			 option.getKey().equals(SCDOptionsEnum.IMACROS_FILE.toString()) ||
			 option.getKey().equals(SCDOptionsEnum.IQUOTE.toString())))
		{
			String value = option.getValue();
			value = CygpathTranslator.translateIncludePaths(project, Collections.singletonList(value)).get(0);
			value = makeRelative(project, new Path(value)).toOSString();
			option = new KVStringPair(option.getKey(), value);
		}
		compilerCommand.add(option);
	}
	
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
    
	@Override
	public String toString() {
		String commandAsString = new String();
		for (Iterator<KVStringPair> i = compilerCommand.iterator(); i.hasNext(); ) {
			KVStringPair optionPair = i.next();
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
	 * @param quoteIncludePaths whether or not paths for includes must be put inside double quotes.
	 * @return the command line to run the scanner discovery.
	 */
	public String getSCDRunnableCommand(boolean quoteIncludePaths, boolean quoteDefines) {
		String commandAsString = new String();
		for (Iterator<KVStringPair> i = compilerCommand.iterator(); i.hasNext(); ) {
			KVStringPair optionPair = i.next();
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
    				optionPair.getKey().equals(SCDOptionsEnum.ISYSTEM.toString()) || 
    				optionPair.getKey().equals(SCDOptionsEnum.IQUOTE.toString())) {
    				value = makeAbsolute(project, value);
    				if (quoteIncludePaths) {
    					value= "\"" + value + "\""; //$NON-NLS-1$ //$NON-NLS-2$
    				}
    			}
    			else if (quoteDefines && optionPair.getKey().equals(SCDOptionsEnum.DEFINE.toString())) {
    				if (value.indexOf('\'') == -1) {
    					value= "'" + value + "'";  //$NON-NLS-1$//$NON-NLS-2$
    				}
    				else {
    					value= value.replaceAll("\"", "\\\\\"");  //$NON-NLS-1$//$NON-NLS-2$
    					value= "\"" + value + "\""; //$NON-NLS-1$ //$NON-NLS-2$
    				}
    			}
    			commandAsString += optionPair.getKey() + SINGLE_SPACE + value + SINGLE_SPACE;
            }
		}
		return commandAsString.trim();
	}
	
	/**
	 * @return the compiler command
	 */
	public String getCompilerName() {
		String compiler = new String();
		for (Iterator<KVStringPair> i = compilerCommand.iterator(); i.hasNext(); ) {
			KVStringPair optionPair = i.next();
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
	public List<String> getImacrosFile() {
		List<String> imacrosFiles = new ArrayList<String>();
		for (Iterator<KVStringPair> i = compilerCommand.iterator(); i.hasNext(); ) {
			KVStringPair optionPair = i.next();
			if (optionPair.getKey().equals(SCDOptionsEnum.IMACROS_FILE.toString())) {
				imacrosFiles.add(makeAbsolute(project,optionPair.getValue()));
			}
		}
		return imacrosFiles;
	}
	
	/**
	 * @return list of strings
	 */
	public List<String> getIncludeFile() {
		List<String> includeFiles = new ArrayList<String>();
		for (Iterator<KVStringPair> i = compilerCommand.iterator(); i.hasNext(); ) {
			KVStringPair optionPair = i.next();
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
	@Override
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
	@Override
	public int hashCode() {
		return compilerCommand.hashCode();
	}

    /**
     * @return Returns the includes as strings.
     */
    public List<String> getIncludes() {
        return makeAbsolute(project, includes);
    }
    /**
     * @param includes The includes to set.
     */
    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }
    /**
     * @return Returns the quote include paths as strings (for #include "...")
     */
    public List<String> getQuoteIncludes() {
        return makeAbsolute(project, quoteIncludes);
    }
    /**
     * @param includes - quote include paths (for #include "...")
     */
    public void setQuoteIncludes(List<String> includes) {
        quoteIncludes = includes;
    }
    /**
     * @return Returns the symbols.
     */
    public List<String> getSymbols() {
        return symbols;
    }
    /**
     * @param symbols The symbols to set.
     */
    public void setSymbols(List<String> symbols) {
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

    public void serialize(Element cmdElem) {
        Document doc = cmdElem.getOwnerDocument();
        // serialize the command
        Element cmdDescElem = doc.createElement(CMD_DESCRIPTION_ELEM);
        for (Iterator<KVStringPair> i = compilerCommand.iterator(); i.hasNext(); ) {
            Element optionElem = doc.createElement(OPTION_ELEM); 
            KVStringPair option = i.next();
            optionElem.setAttribute(KEY_ATTR, option.getKey()); 
            optionElem.setAttribute(VALUE_ATTR, option.getValue()); 
            cmdDescElem.appendChild(optionElem);
        }
        cmdElem.appendChild(cmdDescElem);
        // serialize includes and symbols
        Element siElem = doc.createElement(CMD_SI_ELEM);
        for (Iterator<String> j = quoteIncludes.iterator(); j.hasNext(); ) {
            Element siItem = doc.createElement(SI_ITEM_ELEM); 
            siItem.setAttribute(KIND_ATTR, "INCLUDE_PATH");  //$NON-NLS-1$
            siItem.setAttribute(VALUE_ATTR, j.next());
            siItem.setAttribute(QUOTE_INCLUDE_ATTR, "true"); //$NON-NLS-1$
            siElem.appendChild(siItem);
        }
        for (Iterator<String> j = includes.iterator(); j.hasNext(); ) {
            Element siItem = doc.createElement(SI_ITEM_ELEM); 
            siItem.setAttribute(KIND_ATTR, "INCLUDE_PATH");  //$NON-NLS-1$
            siItem.setAttribute(VALUE_ATTR, j.next());
            siElem.appendChild(siItem);
        }
        for (Iterator<String> j = symbols.iterator(); j.hasNext(); ) {
            Element siItem = doc.createElement(SI_ITEM_ELEM);
            siItem.setAttribute(KIND_ATTR, "SYMBOL_DEFINITION"); //$NON-NLS-1$
            siItem.setAttribute(VALUE_ATTR, j.next()); 
            siElem.appendChild(siItem);
        }
        cmdElem.appendChild(siElem);
    }

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
    	if (!isDiscovered()) {
    		// that's wrong for sure, options cannot be resolved fron the optionPairs??
    		ArrayList<String> symbols = new ArrayList<String>();
    		ArrayList<String> includes = new ArrayList<String>();
    		ArrayList<String> quoteincludes = new ArrayList<String>();
    		for (Iterator<KVStringPair> options = compilerCommand.iterator(); options.hasNext(); ) {
    			KVStringPair optionPair = options.next();
    			String key = optionPair.getKey();
    			String value = optionPair.getValue();
    			if (key.equals(SCDOptionsEnum.INCLUDE.toString()) || key.equals(SCDOptionsEnum.ISYSTEM.toString())) {
    				includes.add(value);
    			}
    			else if (key.equals(SCDOptionsEnum.IQUOTE.toString())) {
    				quoteincludes.add(value);
    			}
    			else if (key.equals(SCDOptionsEnum.DEFINE.toString())) {
    				symbols.add(value);
    			}
    		}
    		setIncludes(includes);
    		setQuoteIncludes(quoteincludes);		                
    		setSymbols(symbols);
    	}
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

	protected static IResource findResource(IProject project, IPath path) {
		IResource resource = project.findMember(path, false);
		if (resource == null) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			resource = root.findMember(path, false);
			if (resource == null) {
				resource= ResourceLookup.selectFileForLocation(path, project);
			}
		}
		return resource;
	}

	public static List<String> makeRelative(IProject project, List<String> paths) {
		List<String> list = new ArrayList<String>(paths.size());
		for (Iterator<String> iter=paths.iterator(); iter.hasNext(); ) {
			String path = iter.next();
			path = makeRelative(project, new Path(path)).toOSString();
			list.add(path);
		}
		return list;
	}


	public static final String makeAbsolute(IProject project, String path) {
		IPath ppath = new Path(path);
		if (project != null && !ppath.isAbsolute()) {
			IResource res = project.findMember(ppath);
			if (res != null) {
				ppath = res.getLocation();
				if (ppath != null) {
					path = ppath.toOSString();
				}
			}
//			path = new File(project.getLocation().toOSString(), path).getAbsolutePath();
		}
		return path;
	}

	public static List<String> makeAbsolute(IProject project, List<String> paths) {
		List<String> list = new ArrayList<String>(paths.size());
		for (Iterator<String> iter=paths.iterator(); iter.hasNext(); ) {
			String path = iter.next();
			path = makeAbsolute(project, path);
			list.add(path);
		}
		return list;
	}
}
