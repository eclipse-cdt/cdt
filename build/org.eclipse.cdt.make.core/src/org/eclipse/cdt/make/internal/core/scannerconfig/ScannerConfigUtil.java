/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.SymbolEntry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;

/**
 * Utility class that handles some Scanner Config specifig collection conversions
 * 
 * @author vhirsl
 */
public final class ScannerConfigUtil {
	private static Random sRandom = new Random();
	private static final QualifiedName discoveredScannerConfigFileNameProperty = new 
			QualifiedName(MakeCorePlugin.getUniqueIdentifier(), "discoveredScannerConfigFileName"); //$NON-NLS-1$
	/**
	 * Adds all new discovered symbols/values to the existing ones.
	 *  
	 * @param sumSymbols - a map of [String, Map] where Map is a SymbolEntry
	 * @param symbols
	 * @return boolean
	 */
	public static boolean scAddSymbolsList2SymbolEntryMap(Map sumSymbols, List symbols, boolean active) {
		boolean rc = false;
		for (Iterator i = symbols.iterator(); i.hasNext(); ) {
			String symbol = (String) i.next();
			String key;
			String value = null;
			int index = symbol.indexOf("="); //$NON-NLS-1$
			if (index != -1) {
				key = symbol.substring(0, index).trim();
				value = symbol.substring(index + 1).trim();
			} else {
				key = symbol.trim();
			}
			SymbolEntry sEntry = (SymbolEntry) sumSymbols.get(key);
			if (sEntry == null) {
				// make only the first one to be active
				sEntry = new SymbolEntry(key, value, true);
				rc = true;
			}
			else {
				rc |= sEntry.add(value, active);
			}
			sumSymbols.put(key, sEntry);
		}
		return rc;
	}

	/**
	 * Gets all discovered symbols with either active or removed values
	 * @param sumSymbols
	 * @param active - false = removed
	 * @return
	 */
	public static List scSymbolsSymbolEntryMap2List(Map sumSymbols, boolean active) {
		Set symbols = sumSymbols.entrySet();
		List rv = new ArrayList(symbols.size());
		for (Iterator i = symbols.iterator(); i.hasNext(); ) {
			SymbolEntry sEntry = (SymbolEntry) ((Map.Entry) i.next()).getValue();
			if (active) {
				rv.addAll(sEntry.getActiveRaw());
			}
			else {
				rv.addAll(sEntry.getRemovedRaw());
			}
		}
		return rv;
	}
	
	/**
	 * MapsSymbolEntryMap to a plain Map
	 * 
	 * @param sumSymbols (in) - discovered symbols in SymbolEntryMap
	 * @return - active symbols as a plain Map
	 */
	public static Map scSymbolEntryMap2Map(Map sumSymbols) {
		Map rv = new HashMap();
		for (Iterator i = sumSymbols.keySet().iterator(); i.hasNext(); ) {
			String key = (String) i.next();
			SymbolEntry values = (SymbolEntry) sumSymbols.get(key);
			for (Iterator j = values.getValuesOnly(true).iterator(); j.hasNext(); ) {
				String value = (String) j.next();
				rv.put(key, value); // multiple active values will be condensed to one !!!
			}
		}
		return rv;
	}

	/**
	 * Adds a single symbol definition string ("DEBUG_LEVEL=4") to the SymbolEntryMap
	 * 
	 * @param symbols
	 * @param symbol
	 * @param active
	 */
	public static boolean scAddSymbolString2SymbolEntryMap(Map symbols, String symbol, boolean active) {
		boolean rc = false;
		String key;
		String value = null;
		int index = symbol.indexOf("="); //$NON-NLS-1$
		if (index != -1) {
			key = getSymbolKey(symbol);
			value = getSymbolValue(symbol);
		} else {
			key = symbol.trim();
		}
		SymbolEntry sEntry = (SymbolEntry) symbols.get(key);
		if (sEntry == null) {
			// make only the first one to be active
			sEntry = new SymbolEntry(key, value, active);
			rc = true;
		}
		else {
			rc |= sEntry.add(value, active);
		}
		symbols.put(key, sEntry);
		return rc;
	}

	/**
	 * @param result (out)
	 * @param addend (in)
	 * @return
	 */
	public static boolean scAddSymbolEntryMap2SymbolEntryMap(Map result, Map addend) {
		boolean rc = false;
		for (Iterator i = addend.keySet().iterator(); i.hasNext(); ) {
			String key = (String) i.next();
			if (result.keySet().contains(key)) {
				SymbolEntry rSE = (SymbolEntry) result.get(key);
				SymbolEntry aSE = (SymbolEntry) addend.get(key);
				List activeValues = rSE.getActiveRaw();
				for (Iterator j = aSE.getActiveRaw().iterator(); j.hasNext(); ) {
					String aValue = (String) j.next();
					if (!activeValues.contains(aValue)) {
						// result does not contain addend's value; add it
						rSE.add(getSymbolValue(aValue), true);
						rc |= true;
					}
				}
				List removedValues = rSE.getRemovedRaw();
				for (Iterator j = aSE.getRemovedRaw().iterator(); j.hasNext(); ) {
					String aValue = (String) j.next();
					if (!removedValues.contains(aValue)) {
						// result does not contain addend's value; add it
						rSE.add(getSymbolValue(aValue), false);
						rc |= true;
					}
				}
			}
			else {
				// result does not contain the symbol; add it
				// shallow copy
				SymbolEntry aSymbolEntry = (SymbolEntry) addend.get(key);
				result.put(key, aSymbolEntry);
				rc |= true;
			}
		}
		return rc;
	}

	/**
	 * Returns a symbol key (i.e. for DEF=1 returns DEF)
	 * 
	 * @param symbol - in
	 * @param key - out
	 */
	public static String getSymbolKey(String symbol) {
		int index = symbol.indexOf('=');
		if (index != -1) {
			return symbol.substring(0, index).trim();
		}
		return symbol;
	}
	
	/**
	 * Returns a symbol value (i.e. for DEF=1 returns 1)
	 * 
	 * @param symbol - in
	 * @param key - out (may be null)
	 */
	public static String getSymbolValue(String symbol) {
		int index = symbol.indexOf('=');
		if (index != -1) {
			return symbol.substring(index+1).trim();
		}
		return null;
	}

	/**
	 * Removes a symbol value from the symbol entry. If it was an only value than
	 * it symbol entry will be removed alltogether.
	 * 
	 * @param symbol
	 * @param symbolEntryMap map of [symbol's key, symbolEntry]
	 */
	public static void removeSymbolEntryValue(String symbol, Map symbolEntryMap) {
		String key = getSymbolKey(symbol);
		String value = getSymbolValue(symbol);
		// find it in the discoveredSymbols Map of SymbolEntries
		SymbolEntry se = (SymbolEntry) symbolEntryMap.get(key);
		if (se != null) {
			se.remove(value);
			if (se.numberOfValues() == 0) {
				symbolEntryMap.remove(key);
			}
		}
	}
	
	/**
	 * Swaps two include paths in the include paths Map.
	 * Used by Up/Down discovered paths
	 *  
	 * @param sumPaths
	 * @param index1
	 * @param index2
	 * @return new map of include paths
	 */
	public static LinkedHashMap swapIncludePaths(LinkedHashMap sumPaths, int index1, int index2) {
		int size = sumPaths.size();
		if (index1 == index2 ||
			!(index1 >= 0 && index1 < size && 
			  index2 >= 0 && index2 < size)) {
			return sumPaths;
		}
		ArrayList pathKeyList = new ArrayList(sumPaths.keySet());
		String temp1 = (String) pathKeyList.get(index1);
		String temp2 = (String) pathKeyList.get(index2);
		pathKeyList.set(index1, temp2);
		pathKeyList.set(index2, temp1);
		
		LinkedHashMap newSumPaths = new LinkedHashMap(sumPaths.size());
		for (Iterator i = pathKeyList.iterator(); i.hasNext(); ) {
			String key = (String) i.next();
			newSumPaths.put(key, sumPaths.get(key));
		}
		return newSumPaths;
	}
	
	/**
	 * Tokenizes string with quuotes
	 * 
	 * @param String
	 * @return String[] 
	 */
	public static String[] tokenizeStringWithQuotes(String line, String quoteStyle) {
		ArrayList allTokens = new ArrayList();
		String[] tokens = line.split(quoteStyle);
		for (int i = 0; i < tokens.length; ++i) {
			if (i % 2 == 0) { // even tokens need further tokenization
				String[] sTokens = tokens[i].split("\\s+"); //$NON-NLS-1$
				for (int j = 0; j < sTokens.length; allTokens.add(sTokens[j++]));
			}
			else {
				allTokens.add(tokens[i]);
			}
		}
		return (String[]) allTokens.toArray(new String[allTokens.size()]);
	}

	/**
	 * Converts array of IPath-s to array of String-s
	 * 
	 * @param paths
	 * @return
	 */
	public static String[] iPathArray2StringArray(IPath[] paths) {
		String[] rv = new String[paths.length];
		for (int i = 0; i < paths.length; ++i) {
			rv[i] = paths[i].toString(); 
		}
		return rv;
	}
	
	public static IPath getDiscoveredScannerConfigStore(IProject project, boolean delete) {
        String fileName = project.getName() + ".sc"; //$NON-NLS-1$
		String storedFileName = null;
		try {
            storedFileName = project.getPersistentProperty(discoveredScannerConfigFileNameProperty);
		} catch (CoreException e) {
			MakeCorePlugin.log(e.getStatus());
		}
        if (storedFileName != null && !storedFileName.equals(fileName)) {
            // try to move 2.x file name format to 3.x file name format
            movePluginStateFile(storedFileName, fileName);
        }
        try {
            project.setPersistentProperty(discoveredScannerConfigFileNameProperty, fileName);
        } catch (CoreException e) {
            MakeCorePlugin.log(e.getStatus());
        }

		if (delete) {
			deletePluginStateFile(fileName);
		}
		return MakeCorePlugin.getWorkingDirectory().append(fileName);
	}

    /**
     * @param delta
     */
    public static void updateScannerConfigStore(IResourceDelta delta) {
        try {
            delta.accept(new IResourceDeltaVisitor() {

                public boolean visit(IResourceDelta delta) throws CoreException {
                    IResource resource = delta.getResource();
                    if (resource instanceof IProject) {
                        IProject project = (IProject) resource;
                        int kind = delta.getKind();
                        switch (kind) {
                        case IResourceDelta.REMOVED:
                            if ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
                                // project renamed
                                IPath newPath = delta.getMovedToPath();
                                IProject newProject = delta.getResource().getWorkspace().
                                        getRoot().getProject(newPath.toString());
                                scProjectRenamed(project, newProject);
                            }
                            else {
                                // project deleted
                                scProjectDeleted(project);
                            }
                        }
                        return false;
                    }
                    return true;
                }

            });
        }
        catch (CoreException e) {
            MakeCorePlugin.log(e);
        }
    }

    private static void scProjectDeleted(IProject project) {
        String scFileName = project.getName() + ".sc"; //$NON-NLS-1$
        deletePluginStateFile(scFileName);
    }

    /**
     * @param scFileName
     */
    private static void deletePluginStateFile(String scFileName) {
        IPath path = MakeCorePlugin.getWorkingDirectory().append(scFileName);
        File file = path.toFile();
        if (file.exists()) {
            file.delete();
        }
    }

    private static void scProjectRenamed(IProject project, IProject newProject) {
        String scOldFileName = project.getName() + ".sc"; //$NON-NLS-1$
        String scNewFileName = newProject.getName() + ".sc"; //$NON-NLS-1$
        movePluginStateFile(scOldFileName, scNewFileName);
        try {
            newProject.setPersistentProperty(discoveredScannerConfigFileNameProperty, scNewFileName);
        }
        catch (CoreException e) {
            MakeCorePlugin.log(e);
        }
    }

    /**
     * @param oldFileName
     * @param newFileName
     */
    private static void movePluginStateFile(String oldFileName, String newFileName) {
        IPath oldPath = MakeCorePlugin.getWorkingDirectory().append(oldFileName);
        IPath newPath = MakeCorePlugin.getWorkingDirectory().append(newFileName);
        File oldFile = oldPath.toFile();
        File newFile = newPath.toFile();
        if (oldFile.exists()) {
            oldFile.renameTo(newFile);
        }
    }

}
