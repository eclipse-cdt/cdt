package org.eclipse.cdt.core;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


import org.eclipse.cdt.core.resources.IBuildInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.QualifiedName;

public class BuildInfoFactory {
	public static final String LOCATION = "buildLocation";
	public static final String FULL_ARGUMENTS = "buildFullArguments";
	public static final String INCREMENTAL_ARGUMENTS = "buildIncrementalArguments";
	public static final String STOP_ON_ERROR = "stopOnError";
//	public static final String CLEAR_CONSOLE = "clearConsole";
	public static final String DEFAULT_BUILD_CMD = "useDefaultBuildCmd";
	
	public static abstract class Store implements IBuildInfo {
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
    	
        public String getFullBuildArguments() {
        	return getString(FULL_ARGUMENTS);
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
	    
        public void setFullBuildArguments(String arguments) {
        	putValue(FULL_ARGUMENTS, arguments);
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

	    public String getString(String property) {
	    	return null;
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
    
    public static IBuildInfo create() {
    	return new BuildInfoFactory.Preference();
    }
    
    public static IBuildInfo create(IProject project) {
    	return new BuildInfoFactory.Property(project);
    }
}

