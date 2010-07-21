/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.xlc.core.scannerconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredScannerInfoSerializable;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector3;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.PathInfo;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredPathManager;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CygpathTranslator;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig2.PerFileSICollector;
import org.eclipse.cdt.make.xlc.core.activator.Activator;
import org.eclipse.cdt.make.xlc.core.messages.Messages;
import org.eclipse.cdt.make.xlc.core.scannerconfig.util.XLCCommandDSC;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author crecoskie
 *
 */
public class PerFileXLCScannerInfoCollector implements IScannerInfoCollector3, IManagedScannerInfoCollector {

	protected class ScannerConfigUpdateJob extends Job {
		
		private InfoContext fContext;
		private IDiscoveredPathInfo fPathInfo;
		private boolean fIsDefaultContext;
		private List<IResource> fChangedResources;
		
		public ScannerConfigUpdateJob(InfoContext context, IDiscoveredPathInfo pathInfo, boolean isDefaultContext, List<IResource> changedResources) {
			super(Messages.getString("PerFileXLCScannerInfoCollector.0")); //$NON-NLS-1$);
			fContext = context;
			fPathInfo = pathInfo;
			fIsDefaultContext = isDefaultContext;
			fChangedResources = changedResources;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			 try {
				 
				 // get the scanner info profile ID
				 
				IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
				IConfiguration config = info.getDefaultConfiguration();
				
				String profileID = config.getToolChain().getScannerConfigDiscoveryProfileId();
				IDiscoveredPathManager manager = MakeCorePlugin.getDefault().getDiscoveryManager();
				
				if(manager instanceof DiscoveredPathManager) {
					((DiscoveredPathManager)manager).updateDiscoveredInfo(fContext, fPathInfo, fIsDefaultContext, fChangedResources, profileID);
				}
				
				// reload project description to hopefully get the data to take
				ICProjectDescriptionManager descriptionManager = CoreModel.getDefault().getProjectDescriptionManager();
				ICProjectDescription cProjectDescription = descriptionManager.getProjectDescription(project, true /* writable */);
				ICConfigurationDescription configDes = cProjectDescription.getActiveConfiguration();
								
				IToolChain toolchain = config.getToolChain();
				for(ITool tool : toolchain.getTools()) {
					for(IInputType inputType : tool.getInputTypes()) {
						IContentType contentType = inputType.getSourceContentType();
						if(contentType != null) {
							for(IResource resource : fChangedResources) {
								// get language settings for the resource
								ICLanguageSetting langSetting = configDes.getLanguageSettingForFile(resource.getProjectRelativePath(), false);
								
								if(langSetting == null) {
									continue;
								}
								
								// get content type IDs for the setting
								String[] contentTypeIDs = langSetting.getSourceContentTypeIds();
								
								// if the setting doesn't handle our content type ID, then go to the next resource
								boolean found = false;
								for(String id : contentTypeIDs) {
									if(id.equals(contentType.getId())) {
										found = true;
										break;
									}
								}
								
								if(!found) {
									continue;
								}
								
								// update all the scanner config entries on the setting
								updateIncludeSettings(langSetting);
								updateMacroSettings(langSetting);
						
							}
						}
						
					}
				}
				
			descriptionManager.setProjectDescription(project, cProjectDescription, true /* force */, monitor);
				
			} catch (CoreException e) {
				Activator.log(e);
				return Activator.createStatus(Messages.getString("PerFileXLCScannerInfoCollector.1")); //$NON-NLS-1$
			}
			 return Status.OK_STATUS;
		}

		private boolean updateMacroSettings(ICLanguageSetting langSetting) {
			ICLanguageSettingEntry[] entries = langSetting.getSettingEntries(ICSettingEntry.MACRO);
			List<ICLanguageSettingEntry> newEntries = new LinkedList<ICLanguageSettingEntry>();
			for(ICLanguageSettingEntry entry : entries) {
				newEntries.add(entry);
			}
			
			
			boolean entriesChanged = false;
														
			// look for settings corresponding to each path we discovered
			Map<String, String> discSymbols = fPathInfo.getSymbols();
			for (String symbol : discSymbols.keySet()) {
				boolean symbolFound = false;
				
				for (ICLanguageSettingEntry entry : entries) {
					if (((CMacroEntry) entry).getName().equals(symbol)) {
						symbolFound = true; // it's already there, so don't set it
						break;
					}
				}
				
				// if we didn't find the path, add it
				if(!symbolFound) {
					entriesChanged = true;
					CMacroEntry newEntry = new CMacroEntry(symbol, discSymbols.get(symbol), ICSettingEntry.BUILTIN | ICSettingEntry.READONLY | ICSettingEntry.RESOLVED);
					newEntries.add(newEntry);
				}
			}
				
			// if we changed the entries, then set the new ones
			if(entriesChanged) {
				langSetting.setSettingEntries(ICSettingEntry.MACRO, newEntries.toArray(new ICLanguageSettingEntry[0]));
			}
			
			return entriesChanged;		
		}

		private boolean updateIncludeSettings(ICLanguageSetting langSetting) {
			ICLanguageSettingEntry[] entries = langSetting.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
			List<ICLanguageSettingEntry> newEntries = new LinkedList<ICLanguageSettingEntry>();
			for(ICLanguageSettingEntry entry : entries) {
				newEntries.add(entry);
			}
			
			
			boolean entriesChanged = false;
														
			// look for settings corresponding to each path we discovered
			IPath[] discPaths = fPathInfo.getIncludePaths();
			for (IPath path : discPaths) {
				boolean pathFound = false;
				
				for (ICLanguageSettingEntry entry : entries) {
					if (((CIncludePathEntry) entry).getLocation().equals(path)) {
						pathFound = true; // it's already there, so don't set it
						break;
					}
				}
				
				// if we didn't find the path, add it
				if(!pathFound) {
					entriesChanged = true;
					CIncludePathEntry newEntry = new CIncludePathEntry(path, ICSettingEntry.BUILTIN | ICSettingEntry.READONLY | ICSettingEntry.RESOLVED);
					newEntries.add(newEntry);
				}
			}
				
			// if we changed the entries, then set the new ones
			if(entriesChanged) {
				langSetting.setSettingEntries(ICSettingEntry.INCLUDE_PATH, newEntries.toArray(new ICLanguageSettingEntry[0]));
			}
			
			return entriesChanged;
		}
	}
	
	protected class MergedPerFileDiscoveredPathInfo implements IPerFileDiscoveredPathInfo2 {
		private IDiscoveredPathInfo fInfo1;
		private IPerFileDiscoveredPathInfo2 fInfo2;
		
		public MergedPerFileDiscoveredPathInfo(IDiscoveredPathInfo info1, IPerFileDiscoveredPathInfo2 info2) {
			fInfo1 = info1;
			fInfo2 = info2;
		}

		private IPerFileDiscoveredPathInfo2 getPerFileInfo1() {
			if(fInfo1 instanceof IPerFileDiscoveredPathInfo2) {
				return (IPerFileDiscoveredPathInfo2) fInfo1;
			}
			
			else {
				return null;
			}
		}
		
		public Map<IResource, PathInfo> getPathInfoMap() {
			synchronized (fLock) {
				IPerFileDiscoveredPathInfo2 info1 = getPerFileInfo1();
				if (info1 != null) {
					Map<IResource, PathInfo> map = new HashMap<IResource, PathInfo>();
					map.putAll(info1.getPathInfoMap());
					map.putAll(fInfo2.getPathInfoMap());
					return map;
				}

				else {
					return fInfo2.getPathInfoMap();
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo#getIncludeFiles(org.eclipse.core.runtime.IPath)
		 */
		public IPath[] getIncludeFiles(IPath path) {
			synchronized (fLock) {
				IPerFileDiscoveredPathInfo2 info1 = getPerFileInfo1();
				if (info1 != null) {
					List<IPath> list = new LinkedList<IPath>();
					for (IPath path1 : info1.getIncludeFiles(path)) {
						list.add(path1);
					}

					for (IPath path1 : fInfo2.getIncludeFiles(path)) {
						list.add(path1);
					}
					return list.toArray(new IPath[0]);
				}

				else {
					return fInfo2.getIncludeFiles(path);
				}
			}
		}

		public IPath[] getIncludePaths(IPath path) {
			synchronized (fLock) {

				Set<IPath> pathSet = new HashSet<IPath>();

				// add project level settings if other info is per project
				if (fInfo1 instanceof DiscoveredPathInfo) {
					for (IPath path1 : fInfo1.getIncludePaths()) {
						pathSet.add(path1);
					}
				}

				else {
					IPerFileDiscoveredPathInfo2 info1 = getPerFileInfo1();
					if (info1 != null) {
						// add file level settings
						for (IPath path1 : info1.getIncludePaths(path)) {
							pathSet.add(path1);
						}
					}
				}

				// add file level settings
				for (IPath path2 : fInfo2.getIncludePaths(path)) {
					pathSet.add(path2);
				}

				return pathSet.toArray(new IPath[0]);
			}
		}

		public IPath[] getMacroFiles(IPath path) {
			synchronized (fLock) {
				Set<IPath> pathSet = new HashSet<IPath>();

				IPerFileDiscoveredPathInfo2 info1 = getPerFileInfo1();
				if (info1 != null) {
					// add file level settings
					for (IPath path1 : info1.getMacroFiles(path)) {
						pathSet.add(path1);
					}
				}

				// add file level settings
				for (IPath path2 : fInfo2.getMacroFiles(path)) {
					pathSet.add(path2);
				}

				return pathSet.toArray(new IPath[0]);
			}
		}

		public IPath[] getQuoteIncludePaths(IPath path) {
			synchronized (fLock) {

				Set<IPath> pathSet = new HashSet<IPath>();

				IPerFileDiscoveredPathInfo2 info1 = getPerFileInfo1();
				if (info1 != null) {
					// add file level settings
					for (IPath path1 : info1.getQuoteIncludePaths(path)) {
						pathSet.add(path1);
					}
				}

				// add file level settings
				for (IPath path2 : fInfo2.getQuoteIncludePaths(path)) {
					pathSet.add(path2);
				}

				return pathSet.toArray(new IPath[0]);
			}
		}

		public Map<String, String> getSymbols(IPath path) {
			synchronized (fLock) {

				Map<String, String> symbols = new HashMap<String, String>();

				// add project level settings
				Map<String, String> projectSymbols = (Map<String, String>) fInfo1.getSymbols();
				for (String symbol : projectSymbols.keySet()) {
					symbols.put(symbol, projectSymbols.get(symbol));
				}

				IPerFileDiscoveredPathInfo2 info1 = getPerFileInfo1();
				if (info1 != null) {
					// add file level settings
					symbols.putAll(info1.getSymbols(path));
				}

				// add file level settings
				symbols.putAll(fInfo2.getSymbols(path));

				return symbols;
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo#isEmpty(org.eclipse.core.runtime.IPath)
		 */
		public boolean isEmpty(IPath path) {
			synchronized (fLock) {
				boolean info1empty = false;

				IPerFileDiscoveredPathInfo2 info1 = getPerFileInfo1();
				if (info1 != null) {
					info1empty = info1.isEmpty(path);
				} else {
					info1empty = fInfo1.getIncludePaths().length == 0 && fInfo1.getSymbols().size() == 0;
				}

				return fInfo2.isEmpty(path) && info1empty;
			}
		}

		public IPath[] getIncludePaths() {
			synchronized (fLock) {
				return fInfo1.getIncludePaths();
			}
		}

		public IProject getProject() {
			return fInfo1.getProject();
		}

		public IDiscoveredScannerInfoSerializable getSerializable() {
			return fInfo2.getSerializable();
		}

		public Map<String, String> getSymbols() {
			synchronized (fLock) {
				return fInfo1.getSymbols();
			}
		}
		
	}
	
	/**
     * Per file DPI object
     * 
     * @author vhirsl
     */
    protected class PerFileDiscoveredPathInfo implements IPerFileDiscoveredPathInfo2 {
        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getIncludeFiles(org.eclipse.core.runtime.IPath)
         */
        public IPath[] getIncludeFiles(IPath path) {
        	synchronized (fLock) {
        		
        		Set<IPath> pathSet = new LinkedHashSet<IPath>();
	            // get the command
	            CCommandDSC cmd = getCommand(path);
	            if (cmd != null) {
	            	IPath[] paths = stringListToPathArray(cmd.getIncludeFile());
	                pathSet.addAll(Arrays.asList(paths));
	            }
	            // use project scope scanner info
	            if (psi == null) {
	            	generateProjectScannerInfo();
	            }

	            for(IPath path2 : psi.includeFiles) {
	            	pathSet.add(path2);
	            }
	            
	            return pathSet.toArray(new IPath[0]);
        	}
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getIncludePaths()
         */
        public IPath[] getIncludePaths() {
        	final IPath[] includepaths;
        	final IPath[] quotepaths;
        	synchronized (fLock) {
//      		return new IPath[0];
	        	includepaths = getAllIncludePaths(INCLUDE_PATH);
	        	quotepaths = getAllIncludePaths(QUOTE_INCLUDE_PATH);
        	}
        	if (quotepaths == null || quotepaths.length == 0) {
        		return includepaths;
        	}
        	if (includepaths == null || includepaths.length == 0) {
        		return quotepaths;
        	}
        	ArrayList<IPath> result = new ArrayList<IPath>(includepaths.length + quotepaths.length);
        	result.addAll(Arrays.asList(includepaths));
        	result.addAll(Arrays.asList(quotepaths));
            return result.toArray(new IPath[result.size()]);
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getIncludePaths(org.eclipse.core.runtime.IPath)
         */
        public IPath[] getIncludePaths(IPath path) {
        	synchronized (fLock) {
        		Set<IPath> pathSet = new LinkedHashSet<IPath>();
	            // get the command
	            CCommandDSC cmd = getCommand(path);
	            if (cmd != null) {
	            	IPath[] paths = stringListToPathArray(cmd.getIncludes());
	                pathSet.addAll(Arrays.asList(paths));
	            }
	            // use project scope scanner info
	            if (psi == null) {
	            	generateProjectScannerInfo();
	            }

	            for(IPath path2 : psi.includePaths) {
	            	pathSet.add(path2);
	            }
	            
	            return pathSet.toArray(new IPath[0]);
        	}
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo#getMacroFiles(org.eclipse.core.runtime.IPath)
         */
        public IPath[] getMacroFiles(IPath path) {
        	synchronized (fLock) {
        		Set<IPath> pathSet = new LinkedHashSet<IPath>();
	            // get the command
	            CCommandDSC cmd = getCommand(path);
	            if (cmd != null) {
	                IPath[] paths = stringListToPathArray(cmd.getImacrosFile());
	                pathSet.addAll(Arrays.asList(paths));
	            }
	            // use project scope scanner info
	            if (psi == null) {
	            	generateProjectScannerInfo();
	            }

	            for(IPath path2 : psi.macrosFiles) {
	            	pathSet.add(path2);
	            }
	            
	            return pathSet.toArray(new IPath[0]);
        	}
        }

        public Map<IResource, PathInfo> getPathInfoMap() {
        	synchronized (fLock) {
				//TODO: do we need to cache this?
				return calculatePathInfoMap();
        	}
		}

		/* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getProject()
         */
        public IProject getProject() {
            return project;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo#getQuoteIncludePaths(org.eclipse.core.runtime.IPath)
         */
        public IPath[] getQuoteIncludePaths(IPath path) {
        	synchronized (fLock) {
        		Set<IPath> pathSet = new LinkedHashSet<IPath>();
	            // get the command
	            CCommandDSC cmd = getCommand(path);
	            if (cmd != null) {
	            	IPath[] paths = stringListToPathArray(cmd.getQuoteIncludes());
	                pathSet.addAll(Arrays.asList(paths));
	            }
	            // use project scope scanner info
	            if (psi == null) {
	            	generateProjectScannerInfo();
	            }

	            for(IPath path2 : psi.quoteIncludePaths) {
	            	pathSet.add(path2);
	            }
	            
	            return pathSet.toArray(new IPath[0]);
        	}
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo#getSerializable()
         */
        public IDiscoveredScannerInfoSerializable getSerializable() {
        	synchronized (fLock) {
        		return sid;
        	}
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getSymbols()
         */
        public Map<String, String> getSymbols() {
//            return new HashMap();
        	synchronized (fLock) {
        		return getAllSymbols();
        	}
        }

		/*
		 * (non-Javadoc)
		 * 
		 * @seeorg.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.
		 * IDiscoveredPathInfo#getSymbols(org.eclipse.core.runtime.IPath)
		 */
		public Map<String, String> getSymbols(IPath path) {
			synchronized (fLock) {
				Map<String, String> definedSymbols = new HashMap<String, String>();

				// put project data in first so file level data can override it
				// use project scope scanner info
				if (psi == null) {
					generateProjectScannerInfo();
				}
				definedSymbols.putAll(psi.definedSymbols);

				// get the command
				CCommandDSC cmd = getCommand(path);
				if (cmd != null && cmd.isDiscovered()) {
					List<String> symbols = cmd.getSymbols();
					for (String symbol : symbols) {
						String key = ScannerConfigUtil.getSymbolKey(symbol);
						String value = ScannerConfigUtil.getSymbolValue(symbol);
						definedSymbols.put(key, value);
					}

				}
				// use project scope scanner info
				if (psi == null) {
					generateProjectScannerInfo();
				}
				definedSymbols.putAll(psi.definedSymbols);
				return definedSymbols;
			}

		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo#isEmpty(org.eclipse.core.runtime.IPath)
		 */
		public boolean isEmpty(IPath path) {
			synchronized (fLock) {
				boolean rc = true;
				IResource resource = project.getWorkspace().getRoot().findMember(path);
				if (resource != null) {
					if (resource instanceof IFile) {
						rc = (getCommand((IFile) resource) == null);
					} else if (resource instanceof IProject) {
						synchronized (fLock) {
							rc = (psi == null || psi.isEmpty());
						}
					}
				}
				return rc;
			}
		}

    }

	public static class ProjectScannerInfo {
    	public Map<String, String> definedSymbols;
    	public IPath[] includeFiles;
    	public IPath[] includePaths;
    	public IPath[] macrosFiles;
    	public IPath[] quoteIncludePaths;
		public boolean isEmpty() {
			return (includePaths.length == 0 &&
					quoteIncludePaths.length == 0 &&
					includeFiles.length == 0 &&
					macrosFiles.length == 0 &&
					definedSymbols.size() == 0);
		}
    }

	public class ScannerInfoData implements IDiscoveredScannerInfoSerializable {
        public static final String DEFINED_SYMBOL = "definedSymbol"; //$NON-NLS-1$
        public static final String ID_ATTR = "id"; //$NON-NLS-1$
        public static final String INCLUDE_PATH = "includePath"; //$NON-NLS-1$

        private static final String NAME = "name"; //$NON-NLS-1$
    	   	
    	public static final String PATH = "path"; //$NON-NLS-1$
    	private static final String PROJECT = "project"; //$NON-NLS-1$
    	public static final String REMOVED = "removed"; //$NON-NLS-1$
    	public static final String SYMBOL = "symbol"; //$NON-NLS-1$
    	public final Map<Integer, CCommandDSC> commandIdCommandMap; // map of all commands
		public final Map<Integer, Set<IFile>> commandIdToFilesMap; // command id and set of files it applies to
		public final Map<IFile, Integer> fileToCommandIdMap;  // maps each file to the corresponding command id
        
        public ScannerInfoData() {
            commandIdCommandMap = new LinkedHashMap<Integer, CCommandDSC>();  // [commandId, command]
            fileToCommandIdMap = new HashMap<IFile, Integer>();         // [file, commandId]
            commandIdToFilesMap = new HashMap<Integer, Set<IFile>>();        // [commandId, set of files]
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore.IDiscoveredScannerInfoSerializable#deserialize(org.w3c.dom.Element)
         */
        public void deserialize(Element collectorElem) {
        	synchronized (fLock) {
        		
        		for (Node child = collectorElem.getFirstChild(); child != null; child = child.getNextSibling()) {
	            	if(child.getNodeName().equals(PROJECT)) {
	            		Element projectElement = (Element) child;
	            		String projectName = projectElement.getAttribute(NAME);
	            		
	            		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	            		
	            		Map<ScannerInfoTypes, List<String>> scannerInfo = new HashMap<ScannerInfoTypes, List<String>>();
	            		
	            		List<String> includes = new LinkedList<String>();
	            		List<String> symbols = new LinkedList<String>();
	            		
	            		// iterate over children
	            		for(Node projectChild = projectElement.getFirstChild(); projectChild != null; projectChild = projectChild.getNextSibling()) {
	            			if(projectChild.getNodeName().equals(INCLUDE_PATH)) {
	            				Element childElem = (Element) projectChild;
	            				String path = childElem.getAttribute(PATH);
								if(path != null) {
									includes.add(path);
								}
	            			}
	            			else if(projectChild.getNodeName().equals(DEFINED_SYMBOL)) {
	            				Element childElem = (Element) projectChild;
	            				String symbol = childElem.getAttribute(SYMBOL);
	            				
								if(symbol != null) {
									symbols.add(symbol);
								}
	            			}
	            		}
	            		
	            		// add loaded scanner info to project settings for this collector
	            		scannerInfo.put(ScannerInfoTypes.INCLUDE_PATHS, includes);
	            		scannerInfo.put(ScannerInfoTypes.SYMBOL_DEFINITIONS, symbols);
	            		fProjectSettingsMap.put(project, scannerInfo);
	            	}
	            	
	            	
        			else if (child.getNodeName().equals(CC_ELEM)) { 
	                    Element cmdElem = (Element) child;
	                    boolean cppFileType = cmdElem.getAttribute(FILE_TYPE_ATTR).equals("c++"); //$NON-NLS-1$
	                    XLCCommandDSC command = new XLCCommandDSC(cppFileType, project);
	                    command.setCommandId(Integer.parseInt(cmdElem.getAttribute(ID_ATTR)));
	                    // deserialize command
	                    command.deserialize(cmdElem);
	                    // get set of files the command applies to
	                    NodeList appliesList = cmdElem.getElementsByTagName(APPLIES_TO_ATTR);
	                    if (appliesList.getLength() > 0) {
	                        Element appliesElem = (Element) appliesList.item(0);
	                        NodeList fileList = appliesElem.getElementsByTagName(FILE_ELEM);
	                        for (int i = 0; i < fileList.getLength(); ++i) {
	                            Element fileElem = (Element) fileList.item(i);
	                            String fileName = fileElem.getAttribute(PATH_ATTR);
	                            IFile file = project.getFile(fileName);
	                            addCompilerCommand(file, command);
	                        }
							applyFileDeltas();
	                    }
	                }
	            }
        	}
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore.IDiscoveredScannerInfoSerializable#getCollectorId()
         */
        public String getCollectorId() {
            return COLLECTOR_ID;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore.IDiscoveredScannerInfoSerializable#serialize(org.w3c.dom.Element)
         */
        public void serialize(Element collectorElem) {
        	try {
        	synchronized (fLock) {
	            Document doc = collectorElem.getOwnerDocument();
	            
	            // serialize project level info
				for (IProject project : fProjectSettingsMap.keySet()) {
					// create a project node
					Element projectElement = doc.createElement(PROJECT);
					projectElement.setAttribute(NAME, project.getName());
					
					Map<ScannerInfoTypes, List<String>> scannerInfo = (Map<ScannerInfoTypes, List<String>>) fProjectSettingsMap.get(project);
					
					List<String> includes = scannerInfo.get(ScannerInfoTypes.INCLUDE_PATHS); 
					for(String include : includes) {
						Element pathElement = doc.createElement(INCLUDE_PATH);
						pathElement.setAttribute(PATH, include);
						//Boolean removed = (Boolean) includes.contains(include);
						//if (removed != null && removed.booleanValue() == true) {
						//	pathElement.setAttribute(REMOVED, "true"); //$NON-NLS-1$
						//}
						pathElement.setAttribute(REMOVED, "false"); //$NON-NLS-1$
						projectElement.appendChild(pathElement);
					}
					
					// Now do the same for the symbols
					List<String> symbols = scannerInfo.get(ScannerInfoTypes.SYMBOL_DEFINITIONS);
					
					for(String symbol : symbols) {
							Element symbolElement = doc.createElement(DEFINED_SYMBOL);
							symbolElement.setAttribute(SYMBOL, symbol);
							projectElement.appendChild(symbolElement);
					}
					collectorElem.appendChild(projectElement);
				}
	            
				// serialize file level info
	            List<Integer> commandIds = new ArrayList<Integer>(commandIdCommandMap.keySet());
	            Collections.sort(commandIds);
	            for (Iterator<Integer> i = commandIds.iterator(); i.hasNext(); ) {
	                Integer commandId = i.next();
	                CCommandDSC command = commandIdCommandMap.get(commandId);
	                
	                Element cmdElem = doc.createElement(CC_ELEM); 
	                collectorElem.appendChild(cmdElem);
	                cmdElem.setAttribute(ID_ATTR, commandId.toString()); 
	                cmdElem.setAttribute(FILE_TYPE_ATTR, command.appliesToCPPFileType() ? "c++" : "c"); //$NON-NLS-1$ //$NON-NLS-2$
	                // write command and scanner info
	                command.serialize(cmdElem);
	                // write files command applies to
	                Element filesElem = doc.createElement(APPLIES_TO_ATTR); 
	                cmdElem.appendChild(filesElem);
	                Set<IFile> files = commandIdToFilesMap.get(commandId);
	                if (files != null) {
	                    for (Iterator<IFile> j = files.iterator(); j.hasNext(); ) {
	                        Element fileElem = doc.createElement(FILE_ELEM); 
	                        IFile file = j.next();
	                        IPath path = file.getProjectRelativePath();
	                        fileElem.setAttribute(PATH_ATTR, path.toString()); 
	                        filesElem.appendChild(fileElem);
	                    }
	                }
	            }
        	}
        	
        	}
        	catch(Throwable e) {
        		e.printStackTrace();
        	}
        }

    }
	
	protected static final String APPLIES_TO_ATTR = "appliesToFiles"; //$NON-NLS-1$

	protected static final String CC_ELEM = "compilerCommand"; //$NON-NLS-1$

	public static final String COLLECTOR_ID = Activator.PLUGIN_ID + ".PerFileXLCScannerInfoCollector"; //$NON-NLS-1$

	protected static final String FILE_ELEM = "file"; //$NON-NLS-1$

	protected static final String FILE_TYPE_ATTR = "fileType"; //$NON-NLS-1$

	protected static final String ID_ATTR = "id"; //$NON-NLS-1$

	protected static final int INCLUDE_FILE		= 3;

	protected static final int INCLUDE_PATH 		= 1;
	
	

	protected static final int MACROS_FILE		= 4;

	protected static final String PATH_ATTR = "path"; //$NON-NLS-1$

	protected static final int QUOTE_INCLUDE_PATH = 2;
	
	protected static PathInfo createFilePathInfo(CCommandDSC cmd){
    	IPath[] includes = stringListToPathArray(cmd.getIncludes());
    	IPath[] quotedIncludes = stringListToPathArray(cmd.getQuoteIncludes());
    	IPath[] incFiles = stringListToPathArray(cmd.getIncludeFile());
    	IPath[] macroFiles = stringListToPathArray(cmd.getImacrosFile());
        List symbols = cmd.getSymbols();
        Map<String, String> definedSymbols = new HashMap<String, String>(symbols.size());
        for (Iterator i = symbols.iterator(); i.hasNext(); ) {
            String symbol = (String) i.next();
            String key = ScannerConfigUtil.getSymbolKey(symbol);
            String value = ScannerConfigUtil.getSymbolValue(symbol);
            definedSymbols.put(key, value);
        }
        
        return new PathInfo(includes, quotedIncludes, definedSymbols, incFiles, macroFiles);
    }
	/**
	 * @param discovered
	 * @param allIncludes
	 * @return
	 */
	protected static IPath[] stringListToPathArray(List<String> discovered) {
		List<Path> allIncludes = new ArrayList<Path>(discovered.size());
		for (Iterator<String> j = discovered.iterator(); j.hasNext(); ) {
		    String include = j.next();
		    if (!allIncludes.contains(include)) {
		        allIncludes.add(new Path(include));
		    }
		}
		return allIncludes.toArray(new IPath[allIncludes.size()]);
	}
	protected int commandIdCounter = 0;
	protected InfoContext context;
	
    /** monitor for data access */
    protected final Object fLock = new Object();
    
    private Map<IProject, Map<?, ?>> fProjectSettingsMap = new HashMap<IProject, Map<?, ?>>();
    
    protected final SortedSet<Integer> freeCommandIdPool;   // sorted set of free command ids
	protected IProject project;
	protected ProjectScannerInfo psi = null;	// sum of all scanner info
	protected final List<Integer> siChangedForCommandIdList;	// list of command ids for which scanner info has changed
	//    protected List siChangedForFileList; 		// list of files for which scanner info has changed
	protected final Map<IResource, Integer> siChangedForFileMap;		// (file, comandId) map for deltas
	protected ScannerInfoData sid; // scanner info data
	
	/**
     * 
     */
    public PerFileXLCScannerInfoCollector() {
        sid = new ScannerInfoData();
        
//        siChangedForFileList = new ArrayList();
		siChangedForFileMap = new HashMap<IResource, Integer>();
		siChangedForCommandIdList = new ArrayList<Integer>();
		
        freeCommandIdPool = new TreeSet<Integer>();
    }
	
    /**
     * @param file 
     * @param object
     */
    protected void addCompilerCommand(IFile file, CCommandDSC cmd) {
		synchronized (fLock) {
			List<CCommandDSC> existingCommands = new ArrayList<CCommandDSC>(sid.commandIdCommandMap.values());
			int index = existingCommands.indexOf(cmd);
			if (index != -1) {
				cmd = existingCommands.get(index);
			} else {
				int commandId = -1;
				if (!freeCommandIdPool.isEmpty()) {
					Integer freeCommandId = freeCommandIdPool.first();
					freeCommandIdPool.remove(freeCommandId);
					commandId = freeCommandId.intValue();
				} else {
					commandId = ++commandIdCounter;
				}
				cmd.setCommandId(commandId);
				sid.commandIdCommandMap.put(cmd.getCommandIdAsInteger(), cmd);
			}

			generateFileDelta(file, cmd);
		}
	}
    
    /**
     * @param commandId
     * @param scannerInfo
     */
    protected void addScannerInfo(Integer commandId, Map scannerInfo) {
		synchronized (fLock) {
			CCommandDSC cmd = sid.commandIdCommandMap.get(commandId);
			if (cmd != null) {
				List<String> siItem = (List<String>) scannerInfo.get(ScannerInfoTypes.SYMBOL_DEFINITIONS);
				cmd.setSymbols(siItem);
				siItem = (List<String>) scannerInfo.get(ScannerInfoTypes.INCLUDE_PATHS);
				siItem = CygpathTranslator.translateIncludePaths(project, siItem);
				siItem = CCommandDSC.makeRelative(project, siItem);
				cmd.setIncludes(siItem);
				siItem = (List<String>) scannerInfo.get(ScannerInfoTypes.QUOTE_INCLUDE_PATHS);
				siItem = CygpathTranslator.translateIncludePaths(project, siItem);
				siItem = CCommandDSC.makeRelative(project, siItem);
				cmd.setQuoteIncludes(siItem);

				cmd.setDiscovered(true);
			}
		}
	}
    
    /**
     * @param type
     * @param object
     */
    protected void addScannerInfo(ScannerInfoTypes type, List delta) {
        // TODO Auto-generated method stub
        
    }
    /**
	 * @param file
	 * @param cmd
	 */
	protected void applyFileDeltas() {
		synchronized (fLock) {
			for (Iterator<IResource> i = siChangedForFileMap.keySet().iterator(); i.hasNext();) {
				IFile file = (IFile) i.next();
				Integer commandId = siChangedForFileMap.get(file);
				if (commandId != null) {

					// update sid.commandIdToFilesMap
					Set<IFile> fileSet = sid.commandIdToFilesMap.get(commandId);
					if (fileSet == null) {
						fileSet = new HashSet<IFile>();
						sid.commandIdToFilesMap.put(commandId, fileSet);
						CCommandDSC cmd = sid.commandIdCommandMap.get(commandId);
						if (cmd != null) {
							cmd.resolveOptions(project);
						}
					}
					if (fileSet.add(file)) {
						// update fileToCommandIdsMap
						boolean change = true;
						Integer oldCommandId = sid.fileToCommandIdMap.get(file);
						if (oldCommandId != null) {
							if (oldCommandId.equals(commandId)) {
								change = false;
							} else {
								Set oldFileSet = sid.commandIdToFilesMap.get(oldCommandId);
								if (oldFileSet != null) {
									oldFileSet.remove(file);
								}
							}
						}
						if (change) {
							sid.fileToCommandIdMap.put(file, commandId);
							// TODO generate change event for this resource
							// IPath path = file.getFullPath();
							// if (!siChangedForFileList.contains(path)) {
							// siChangedForFileList.add(path);
							// }
						}
					}
				}
			}
			generateProjectScannerInfo();
		}
	}
    

	protected Map<IResource, PathInfo> calculatePathInfoMap() {
		synchronized (fLock) {
			Map<IResource, PathInfo> map = new HashMap<IResource, PathInfo>(sid.fileToCommandIdMap.size() + 1);
			Map.Entry entry;
			IFile file;
			CCommandDSC cmd;
			PathInfo fpi;
			for (Iterator iter = sid.fileToCommandIdMap.entrySet().iterator(); iter.hasNext();) {
				entry = (Map.Entry) iter.next();
				file = (IFile) entry.getKey();
				if (file != null) {
					cmd = sid.commandIdCommandMap.get(entry.getValue());
					if (cmd != null) {
						fpi = createFilePathInfo(cmd);
						map.put(file, fpi);
					}
				}
			}

			if (project != null) {
				if (psi == null) {
					generateProjectScannerInfo();
				}

				fpi = new PathInfo(psi.includePaths, psi.quoteIncludePaths, psi.definedSymbols, psi.includeFiles,
						psi.macrosFiles);
				map.put(project, fpi);
			}

			return map;
		}
	}
	
	public void contributeToScannerConfig(Object resource, Map scannerInfo) {
        // check the resource
        String errorMessage = null;
        if (resource == null) {
            errorMessage = "resource is null";//$NON-NLS-1$
        }
        else if (resource instanceof Integer) {
        	synchronized (fLock) {
                addScannerInfo(((Integer)resource), scannerInfo);
			}
            return;
        }
        
       if ((resource instanceof IFile)) {

			if (((IFile) resource).getProject() == null) {
				errorMessage = "project is null";//$NON-NLS-1$
			} else if (!((IFile) resource).getProject().equals(project)) {
				errorMessage = "wrong project";//$NON-NLS-1$
			}
			if (errorMessage != null) {
				TraceUtil.outputError("PerFileSICollector.contributeToScannerConfig : ", errorMessage); //$NON-NLS-1$
				return;
			}

			IFile file = (IFile) resource;

			synchronized (fLock) {
				for (Iterator i = scannerInfo.keySet().iterator(); i.hasNext();) {
					ScannerInfoTypes type = (ScannerInfoTypes) i.next();
					if (type.equals(ScannerInfoTypes.COMPILER_COMMAND)) {
						List commands = (List) scannerInfo.get(type);
						for (Iterator j = commands.iterator(); j.hasNext();) {
							addCompilerCommand(file, (CCommandDSC) j.next());
						}
					} else {
						addScannerInfo(type, (List) scannerInfo.get(type));
					}
				}
			}
		}
       
       else if(resource instanceof IProject) {
    	   // save to project level settings
    	   synchronized (fLock) {
    		   fProjectSettingsMap.put(((IProject) resource), scannerInfo);
    	   }
       }
       
       else { // error
    	   TraceUtil.outputError("PerFileSICollector.contributeToScannerConfig : ", "Not a project or file."); //$NON-NLS-1$ //$NON-NLS-2$
			return;
       }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#createPathInfoObject()
     */
    public IDiscoveredPathInfo createPathInfoObject() {
        return new PerFileDiscoveredPathInfo();
    }
    /* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig2.PerFileSICollector#deleteAll(org.eclipse.core.resources.IResource)
	 */
	public void deleteAll(IResource resource) {
		synchronized (fLock) {
			if (resource instanceof IProject) {
				fProjectSettingsMap.remove(((IProject) resource));
			}
		}
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorCleaner#deleteAll(org.eclipse.core.resources.IResource)
     */
    public void deleteAll1(IResource resource) {
        if (resource.equals(project)) {
        	synchronized (fLock) {
//            	siChangedForFileList = new ArrayList();
	            siChangedForFileMap.clear();
	            Set<IFile> changedFiles = sid.fileToCommandIdMap.keySet();
	            for (Iterator<IFile> i = changedFiles.iterator(); i.hasNext(); ) {
	                IFile file = i.next();
//	                IPath path = file.getFullPath();
//	                siChangedForFileList.add(path);
	                siChangedForFileMap.put(file, null);
	            }
	
	            sid = new ScannerInfoData();
	            psi = null;
	            
	            commandIdCounter = 0;
				freeCommandIdPool.clear();
        	}
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.make.internal.core.scannerconfig2.PerFileSICollector#
	 * deleteAllPaths(org.eclipse.core.resources.IResource)
	 */
	public void deleteAllPaths(IResource resource) {
		synchronized (fLock) {
			if (resource instanceof IProject && fProjectSettingsMap != null) {
				fProjectSettingsMap.remove(((IProject) resource));
			}
		}
	}

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.make.internal.core.scannerconfig2.PerFileSICollector#
	 * deleteAllSymbols(org.eclipse.core.resources.IResource)
	 */
	public void deleteAllSymbols(IResource resource) {
		synchronized (fLock) {
			if (resource instanceof IProject && fProjectSettingsMap != null) {
				fProjectSettingsMap.remove(((IProject) resource));
			}
		}
	}

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.make.internal.core.scannerconfig2.PerFileSICollector#
	 * deletePath(org.eclipse.core.resources.IResource, java.lang.String)
	 */
	public void deletePath(IResource resource, String path) {
		synchronized (fLock) {
			if (resource instanceof IProject && fProjectSettingsMap != null) {
				fProjectSettingsMap.remove(((IProject) resource));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.make.internal.core.scannerconfig2.PerFileSICollector#
	 * deleteSymbol(org.eclipse.core.resources.IResource, java.lang.String)
	 */
	public void deleteSymbol(IResource resource, String symbol) {
		synchronized (fLock) {
			if (resource instanceof IProject && fProjectSettingsMap != null) {
				fProjectSettingsMap.remove(((IProject) resource));
			}
		}
	}

    /**
	 * @param file
	 * @param cmd
	 */
	protected void generateFileDelta(IFile file, CCommandDSC cmd) {
		synchronized (fLock) {
			Integer commandId = cmd.getCommandIdAsInteger();
			Integer oldCommandId = sid.fileToCommandIdMap.get(file);

			if (oldCommandId != null && oldCommandId.equals(commandId)) {
				// already exists; remove form delta
				siChangedForFileMap.remove(file);
			} else {
				// new (file, commandId) pair
				siChangedForFileMap.put(file, commandId);
			}
		}
	}

    protected void generateProjectScannerInfo() {
		synchronized (fLock) {
			psi = new ProjectScannerInfo();
			psi.includePaths = getAllIncludePaths(INCLUDE_PATH);
			psi.quoteIncludePaths = getAllIncludePaths(QUOTE_INCLUDE_PATH);
			psi.includeFiles = getAllIncludePaths(INCLUDE_FILE);
			psi.macrosFiles = getAllIncludePaths(MACROS_FILE);
			psi.definedSymbols = getAllSymbols();
		}
	}

    /* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig2.PerFileSICollector#getAllIncludePaths(int)
	 */
	protected IPath[] getAllIncludePaths(int type) {
		synchronized (fLock) {
			IProject project = this.getInfoContext().getProject();

			Map projectScannerInfo = fProjectSettingsMap.get(project);
			List<String> includes = null;

			if (projectScannerInfo != null) {
				includes = (List<String>) projectScannerInfo.get(ScannerInfoTypes.INCLUDE_PATHS);
			}

			List<IPath> pathList = new LinkedList<IPath>();

			if (includes != null) {
				for (String include : includes) {
					pathList.add(new Path(include));
				}
			}

			IPath[] fileIncludes = getAllIncludePaths1(type);

			for (IPath include : fileIncludes) {
				pathList.add(include);
			}

			return pathList.toArray(new IPath[0]);
		}
	}

	/**
     * @param type can be one of the following:
     * <li><code>INCLUDE_PATH</code>
     * <li><code>QUOTE_INCLUDE_PATH</code>
     * <li><code>INCLUDE_FILE</code>
     * <li><code>MACROS_FILE</code>
     * 
     * @return list of IPath(s).
     */
    protected IPath[] getAllIncludePaths1(int type) {
		synchronized (fLock) {
			List<String> allIncludes = new ArrayList<String>();
			for (Iterator<Integer> i = sid.commandIdCommandMap.keySet().iterator(); i.hasNext();) {
				Integer cmdId = i.next();
				CCommandDSC cmd = sid.commandIdCommandMap.get(cmdId);
				if (cmd.isDiscovered()) {
					List<String> discovered = null;
					switch (type) {
					case INCLUDE_PATH:
						discovered = cmd.getIncludes();
						break;
					case QUOTE_INCLUDE_PATH:
						discovered = cmd.getQuoteIncludes();
						break;
					case INCLUDE_FILE:
						discovered = cmd.getIncludeFile();
						break;
					case MACROS_FILE:
						discovered = cmd.getImacrosFile();
						break;
					}
					for (Iterator<String> j = discovered.iterator(); j.hasNext();) {
						String include = j.next();
						// the following line degrades perfomance
						// see
						// https://bugs.eclipse.org/bugs/show_bug.cgi?id=189127
						// it is not necessary for renaming projects anyway
						// include = CCommandDSC.makeRelative(project, new
						// Path(include)).toPortableString();
						if (!allIncludes.contains(include)) {
							allIncludes.add(include);
						}
					}
				}
			}
			return stringListToPathArray(allIncludes);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.make.internal.core.scannerconfig2.PerFileSICollector#
	 * getAllSymbols()
	 */
	protected Map<String, String> getAllSymbols() {
		synchronized (fLock) {
			IProject project = this.getInfoContext().getProject();

			Map projectScannerInfo = fProjectSettingsMap.get(project);

			Map<String, String> symbols = new HashMap<String, String>();

			if (projectScannerInfo != null) {
				List<String> projectSymbols = (List<String>) projectScannerInfo
						.get(ScannerInfoTypes.SYMBOL_DEFINITIONS);

				if (projectSymbols != null) {

					for (String symbol : projectSymbols) {
						symbols.put(symbol, "1"); //$NON-NLS-1$
					}
				}
			}

			Map<String, String> fileSymbols = getAllSymbols1();

			symbols.putAll(fileSymbols);

			return symbols;
		}
	}
    
    /**
     * @return
     */
    protected Map<String, String> getAllSymbols1() {
		synchronized (fLock) {
			Map<String, String> symbols = new HashMap<String, String>();
			for (Iterator<Integer> i = sid.commandIdCommandMap.keySet().iterator(); i.hasNext();) {
				Integer cmdId = i.next();
				CCommandDSC cmd = sid.commandIdCommandMap.get(cmdId);
				if (cmd.isDiscovered()) {
					List discovered = cmd.getSymbols();
					for (Iterator j = discovered.iterator(); j.hasNext();) {
						String symbol = (String) j.next();
						String key = ScannerConfigUtil.getSymbolKey(symbol);
						String value = ScannerConfigUtil.getSymbolValue(symbol);
						symbols.put(key, value);
					}
				}
			}
			return symbols;
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#getCollectedScannerInfo(java.lang.Object, org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes)
     */
    public List<CCommandDSC> getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
    	
        List<CCommandDSC> rv = new ArrayList<CCommandDSC>();
        // check the resource
        String errorMessage = null;
        if (resource == null) {
            errorMessage = "resource is null";//$NON-NLS-1$
        } 
        else if (!(resource instanceof IResource)) {
            errorMessage = "resource is not an IResource";//$NON-NLS-1$
        }
        else if (((IResource) resource).getProject() == null) {
            errorMessage = "project is null";//$NON-NLS-1$
        }
        else if (((IResource) resource).getProject() != project) {
            errorMessage = "wrong project";//$NON-NLS-1$
        }
        
        if (errorMessage != null) {
            TraceUtil.outputError("PerProjectSICollector.getCollectedScannerInfo : ", errorMessage); //$NON-NLS-1$
            return rv;
        }
        if (project.equals(((IResource)resource).getProject())) {
        	if (type.equals(ScannerInfoTypes.COMPILER_COMMAND)) {
        		synchronized (fLock) {
        			for (Iterator<Integer> i = sid.commandIdCommandMap.keySet().iterator(); i.hasNext(); ) {
        				Integer cmdId = i.next();
        				Set<IFile> fileSet = sid.commandIdToFilesMap.get(cmdId);
        				if (fileSet != null && !fileSet.isEmpty()) {
        					rv.add(sid.commandIdCommandMap.get(cmdId));
        				}
        			}
        		}
        	}
        	else if (type.equals(ScannerInfoTypes.UNDISCOVERED_COMPILER_COMMAND)) {
//      		if (!siChangedForFileList.isEmpty()) {
    			synchronized (fLock) {
    				if (scannerInfoChanged()) {
    					if (siChangedForCommandIdList.isEmpty()) {
//  						for (Iterator i = siChangedForFileList.iterator(); i.hasNext(); ) {
    						for (Iterator<IResource> i = siChangedForFileMap.keySet().iterator(); i.hasNext(); ) {
//  							IPath path = (IPath) i.next();
    							IFile file = (IFile) i.next();
    							Integer cmdId = siChangedForFileMap.get(file);
    							if (cmdId != null) {
    								if (!siChangedForCommandIdList.contains(cmdId)) {
    									siChangedForCommandIdList.add(cmdId);
    								}
    							}
    						}
    					}
    					Collections.sort(siChangedForCommandIdList);
    					for (Iterator<Integer> i = siChangedForCommandIdList.iterator(); i.hasNext(); ) {
    						Integer cmdId = i.next();
    						CCommandDSC command = sid.commandIdCommandMap.get(cmdId);
    						rv.add(command);
    					}
    				}
    			}
            }
		}
        return rv;
    }

    protected CCommandDSC getCommand(IFile file) {
		synchronized (fLock) {
			CCommandDSC cmd = null;
			if (file != null) {
				Integer cmdId = sid.fileToCommandIdMap.get(file);
				if (cmdId != null) {
					// get the command
					cmd = sid.commandIdCommandMap.get(cmdId);
				}
			}
			return cmd;
		}
	}

     /**
     * @param path
     * @return
     */
    protected CCommandDSC getCommand(IPath path) {
		synchronized (fLock) {
			try {
				IFile file = project.getWorkspace().getRoot().getFile(path);
				return getCommand(file);
			} catch (Exception e) {
				return null;
			}
		}
	}

    /* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector#getDefinedSymbols()
	 */
	public Map getDefinedSymbols() {
		synchronized (fLock) {
			return getAllSymbols();
		}
	}
    
    /* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector#getIncludePaths()
	 */
	public List<String> getIncludePaths() {
		synchronized (fLock) {
			List<String> pathStrings = new LinkedList<String>();

			List<IPath> paths = Arrays.asList(getAllIncludePaths(INCLUDE_PATH));
			paths.addAll(Arrays.asList(getAllIncludePaths(QUOTE_INCLUDE_PATH)));

			for (IPath path : paths) {
				pathStrings.add(path.toString());
			}

			return pathStrings;
		}
	}
    
    protected InfoContext getInfoContext() {
		return context;
	}

    protected void removeUnusedCommands() {
		synchronized (fLock) {
			for (Iterator i = sid.commandIdToFilesMap.entrySet().iterator(); i.hasNext();) {
				Entry entry = (Entry) i.next();
				Integer cmdId = (Integer) entry.getKey();
				Set fileSet = (Set) entry.getValue();
				if (fileSet.isEmpty()) {
					// return cmdId to the free command id pool
					freeCommandIdPool.add(cmdId);
				}
			}
			for (Iterator<Integer> i = freeCommandIdPool.iterator(); i.hasNext();) {
				Integer cmdId = i.next();
				// the command does not have any files associated; remove
				sid.commandIdCommandMap.remove(cmdId);
				sid.commandIdToFilesMap.remove(cmdId);
			}
			while (!freeCommandIdPool.isEmpty()) {
				Integer last = freeCommandIdPool.last();
				if (last.intValue() == commandIdCounter) {
					freeCommandIdPool.remove(last);
					--commandIdCounter;
				} else
					break;
			}
		}
	}

    protected boolean scannerInfoChanged() {
    	synchronized (fLock) {
    		return (!fProjectSettingsMap.isEmpty()) || !siChangedForFileMap.isEmpty();
    	}
	}

    public void setInfoContext(InfoContext context) {
		synchronized (fLock) {
			this.project = context.getProject();
			this.context = context;

			try {
				// deserialize from SI store
				DiscoveredScannerInfoStore.getInstance().loadDiscoveredScannerInfoFromState(project, context, sid);
			} catch (CoreException e) {
				MakeCorePlugin.log(e);
			}
		}
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#setProject(org.eclipse.core.resources.IProject)
     */
    public void setProject(IProject project) {
    	synchronized (fLock) {
			setInfoContext(new InfoContext(project));
		}
    }

    public void updateScannerConfiguration(IProgressMonitor monitor) throws CoreException {
	       if (monitor == null) {
	            monitor = new NullProgressMonitor();
	        }
	        monitor.beginTask(Messages.getString("ScannerInfoCollector.Processing"), 100); //$NON-NLS-1$
	        monitor.subTask(Messages.getString("ScannerInfoCollector.Processing")); //$NON-NLS-1$
	        ArrayList<IResource> changedResources = new ArrayList<IResource>();
	        synchronized (fLock) {
	        	if (scannerInfoChanged()) {
	        		applyFileDeltas();
	        		removeUnusedCommands();
	        		changedResources.addAll(siChangedForFileMap.keySet());
	        		siChangedForFileMap.clear();
	        	}
	        	siChangedForCommandIdList.clear();
	        	
		        // add in any projects that got project level info (from the specs provider)
		        changedResources.addAll(fProjectSettingsMap.keySet());
		        
			    monitor.worked(50);
		        if (!changedResources.isEmpty()) {
			        // update outside monitor scope
			        try {
			        	// update scanner configuration
			        	monitor.subTask(Messages.getString("ScannerInfoCollector.Updating") + project.getName()); //$NON-NLS-1$
			        	IDiscoveredPathInfo pathInfo = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(project, context);
			        	//IDiscoveredPathInfo pathInfo = new PerFileDiscoveredPathInfo();
			        	if (!(pathInfo instanceof IPerFileDiscoveredPathInfo)) {
			        		pathInfo = createPathInfoObject();
			        	}
			        	else {
			        		PerFileDiscoveredPathInfo perFilePathInfo = new PerFileDiscoveredPathInfo();
			        		
			        		// merge them
			        		if (!(pathInfo instanceof IPerFileDiscoveredPathInfo)) {
			        			pathInfo = new MergedPerFileDiscoveredPathInfo(pathInfo, perFilePathInfo);
			        		}
			        		else {
			        			pathInfo = perFilePathInfo;
			        		}
			        	}
			        	
       	
			        	Job job = new ScannerConfigUpdateJob(context, pathInfo, context.isDefaultContext(), changedResources);
			        	ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRoot();
			        	job.setRule(rule);
			        	job.schedule();

							
			        	   
//			        	} finally {
//			        	    manager.endRule(rule);
//			        	}
			        	
			        } catch (CoreException e) {
			        	MakeCorePlugin.log(e);
			        }
			        
			        catch (Throwable e) {
			        	e.printStackTrace();
			        }
			    }
	        }
	        

		    monitor.worked(50);
			monitor.done();
	}
	
}