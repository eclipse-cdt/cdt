package org.eclipse.cdt.core;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.resources.IStandardBuildInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.QualifiedName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class BuildInfoFactory {
	public static final String LOCATION = "buildLocation";
	public static final String FULL_ARGUMENTS = "buildFullArguments";
	public static final String INCREMENTAL_ARGUMENTS = "buildIncrementalArguments";
	public static final String STOP_ON_ERROR = "stopOnError";
//	public static final String CLEAR_CONSOLE = "clearConsole";
	public static final String DEFAULT_BUILD_CMD = "useDefaultBuildCmd";
	public static final String PROJECT_NAME = "projectName";
	public static final String INCLUDE_PATH = "includePath";
	public static final String PATH = "path";
	public static final String DEFINED_SYMBOL = "definedSymbol";
	public static final String SYMBOL = "symbol";
	
	public static abstract class Store implements IStandardBuildInfo, IScannerInfo {
		// List of include paths
		protected List pathList;
		protected List symbolList;
		
    	public String getBuildLocation() {
    		if ( isDefaultBuildCmd() ) {
				Plugin plugin = CCorePlugin.getDefault();
				if (plugin != null) {
					IExtensionPoint extension = plugin.getDescriptor().getExtensionPoint("CBuildCommand");
					if (extension != null) {
						IExtension[] extensions =  extension.getExtensions();
						for(int i = 0; i < extensions.length; i++){
							IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
							for(int j = 0; j < configElements.length; j++){
								String command = configElements[j].getAttribute("command"); //$NON-NLS-1$
								if (command != null)
									return command;
							}
						}
					}		
				}
				return "make";
			}
			return getString(LOCATION);
    	}
    	
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.build.managed.IScannerInfo#getIncludePaths()
		 */
    	public Map getDefinedSymbols() {
			// Return the defined symbols for the default configuration
			HashMap symbols = new HashMap();
			String[] symbolList = getPreprocessorSymbols();
			for (int i = 0; i < symbolList.length; ++i) {
				String symbol = symbolList[i];
				if (symbol.length() == 0) {
					continue;
				}
				String key = new String();
				String value = new String();
				int index = symbol.indexOf("=");
				if (index != -1) {
					key = symbol.substring(0, index).trim();
					value = symbol.substring(index + 1).trim();
				} else {
					key = symbol.trim();
				}
				symbols.put(key, value);
			}
			return symbols; 
    	}
    	
        public String getFullBuildArguments() {
        	return getString(FULL_ARGUMENTS);
        }
        
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.build.managed.IScannerInfo#getIncludePaths()
		 */
        public String[] getIncludePaths() {
        	return (String[]) getPathList().toArray(new String[getPathList().size()]);
        }
        
        public String getIncrementalBuildArguments() {
        	return getString(INCREMENTAL_ARGUMENTS);
        }
        
		public boolean isStopOnError() {
        	return getBoolean(STOP_ON_ERROR);
		}

	    public void setBuildLocation(String location) {
	    	putValue(LOCATION, location);
	    }
	    
	    public void setPreprocessorSymbols(String[] symbols) {
	    	// Clear out any existing symbols and add the new stuff
	    	getSymbolList().clear();
	    	getSymbolList().addAll(Arrays.asList(symbols));
	    }
	    
        public void setFullBuildArguments(String arguments) {
        	putValue(FULL_ARGUMENTS, arguments);
        }
        
		public void setIncludePaths(String[] paths) {
			// Clear the existing list and add the paths
			getPathList().clear();
			getPathList().addAll(Arrays.asList(paths));
		}
		
        public void setIncrementalBuildArguments(String arguments) {
        	putValue(INCREMENTAL_ARGUMENTS, arguments);
        }

		public void setStopOnError(boolean on) {
        	putValue(STOP_ON_ERROR, new Boolean(on).toString());
		}

		public boolean isDefaultBuildCmd() {
			if ( getString(DEFAULT_BUILD_CMD) == null ) { // if no property then default to true
				return true;
			}
			return getBoolean(DEFAULT_BUILD_CMD);
		}
		
		public void setUseDefaultBuildCmd(boolean on) {
        	putValue(DEFAULT_BUILD_CMD, new Boolean(on).toString());
		}

//        public boolean isClearBuildConsole() {
//        	return getBoolean(CLEAR_CONSOLE);
//        }
	
	    public boolean getBoolean(String property) {
	    	return Boolean.valueOf(getString(property)).booleanValue();
	    }
	    
	    public void putValue(String name, String value) {
	    }

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.resources.IBuildInfo#serialize(org.w3c.dom.Document, org.w3c.dom.Element)
		 */
		public void serialize(Document doc, Element rootElement) {
			// Serialize the include paths
			ListIterator iter = getPathList().listIterator();
			while (iter.hasNext()){
				Element pathElement = doc.createElement(INCLUDE_PATH);
				pathElement.setAttribute(PATH, (String)iter.next());
				rootElement.appendChild(pathElement);
			}
			// Now do the same for the symbols
			iter = getSymbolList().listIterator();
			while (iter.hasNext()) {
				Element symbolElement = doc.createElement(DEFINED_SYMBOL);
				symbolElement.setAttribute(SYMBOL, (String)iter.next());
				rootElement.appendChild(symbolElement);
			}
		}

		protected List getPathList() {
			if (pathList == null) {
				pathList = new ArrayList();
			}
			return pathList;
		}

	    public String getString(String property) {
	    	return null;
	    }

		public String[] getPreprocessorSymbols() {
			return (String[]) getSymbolList().toArray(new String[getSymbolList().size()]);
		}
    	
		protected List getSymbolList() {
			if (symbolList == null) {
				symbolList = new ArrayList();
			}
			return symbolList;
		}

	}
	
	public static class Preference extends Store {
		Preferences prefs;
		
		public Preference() {
			prefs = CCorePlugin.getDefault().getPluginPreferences();
		}
		
		public void putValue(String name, String value) {
			prefs.setValue(name, value);
		}
		
		public String getString(String property) {
			return prefs.getString(property);
		}
		
		public void setDefault(String name, String def) {
			prefs.setDefault(name, def);
		}
	}
	
	public static class Property extends Store {
		private IResource resource;
		
		public Property(IResource resource) {
			this.resource = resource;
		}
		
		public void putValue(String name, String value) {
			QualifiedName qName = new QualifiedName(CCorePlugin.PLUGIN_ID, name);
			try {
                resource.setPersistentProperty(qName, value);
			} catch (CoreException e) {
            }
		}
		
		public String getString(String property) {
			QualifiedName qName = new QualifiedName(CCorePlugin.PLUGIN_ID, property);
			try {
                return resource.getPersistentProperty(qName);
			} catch (CoreException e) {
            }
            return null;
		}
		
		public void setDefault(String name, String def) {
		}


//		public boolean isClearBuildConsole() {
//			return (new Preference()).isClearBuildConsole();
//		}
	}
    
    public static IStandardBuildInfo create() {
    	return new BuildInfoFactory.Preference();
    }
    
    public static IStandardBuildInfo create(IProject project) {
    	return new BuildInfoFactory.Property(project);
    }

	/**
	 * @param project
	 * @param element
	 * @return
	 */
	public static IStandardBuildInfo create(IProject project, Element element) {
		// Create a new info property object
		Property buildProperties = new Property(project);
		Node child = element.getFirstChild();
		while (child != null) {
			if (child.getNodeName().equals(INCLUDE_PATH)) {
				// Add the path to the property list
				buildProperties.getPathList().add(((Element)child).getAttribute(PATH));
			} else if (child.getNodeName().equals(DEFINED_SYMBOL)) {
				// Add the symbol to the symbol list
				buildProperties.getSymbolList().add(((Element)child).getAttribute(SYMBOL));
			}
			child = child.getNextSibling();
		}
		return (IStandardBuildInfo)buildProperties;
	}
}

