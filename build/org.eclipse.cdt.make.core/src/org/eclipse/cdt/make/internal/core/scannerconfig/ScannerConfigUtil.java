/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.make.internal.core.scannerconfig.util.SymbolEntry;
import org.eclipse.core.runtime.IPath;

/**
 * Utility class that handles some Scanner Config specifig collection conversions
 * 
 * @author vhirsl
 */
public final class ScannerConfigUtil {
    /**
	 * Adds all new discovered symbols/values to the existing ones.
	 *  
	 * @param sumSymbols - a map of [String, Map] where Map is a SymbolEntry
	 */
	public static boolean scAddSymbolsList2SymbolEntryMap(Map<String, SymbolEntry> sumSymbols, List<String> symbols, boolean active) {
		boolean rc = false;
		for (String symbol : symbols) {
			String key;
			String value = null;
			int index = symbol.indexOf("="); //$NON-NLS-1$
			if (index != -1) {
				key = symbol.substring(0, index).trim();
				value = symbol.substring(index + 1).trim();
			} else {
				key = symbol.trim();
			}
			SymbolEntry sEntry = sumSymbols.get(key);
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
	 */
	public static List<String> scSymbolsSymbolEntryMap2List(Map<String, SymbolEntry> sumSymbols, boolean active) {
		Set<Entry<String, SymbolEntry>> symbols = sumSymbols.entrySet();
		List<String> rv = new ArrayList<String>(symbols.size());
		for (Entry<String, SymbolEntry> symbol : symbols) {
			SymbolEntry sEntry = symbol.getValue();
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
	public static Map<String, String> scSymbolEntryMap2Map(Map<String, SymbolEntry> sumSymbols) {
		Map<String, String> rv = new HashMap<String, String>();
		Set<String> keys = sumSymbols.keySet();
		for (String key : keys) {
			SymbolEntry entries = sumSymbols.get(key);
			List<String> values = entries.getValuesOnly(true);
			for (String value : values) {
				rv.put(key, value); // multiple active values will be condensed to one !!!
			}
		}
		return rv;
	}

	/**
	 * Adds a single symbol definition string ("DEBUG_LEVEL=4") to the SymbolEntryMap
	 */
	public static boolean scAddSymbolString2SymbolEntryMap(Map<String, SymbolEntry> symbols, String symbol, boolean active) {
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
		SymbolEntry sEntry = symbols.get(key);
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
	 */
	public static boolean scAddSymbolEntryMap2SymbolEntryMap(Map<String, SymbolEntry> result, Map<String, SymbolEntry> addend) {
		boolean rc = false;
		Set<String> keySet = addend.keySet();
		for (String key : keySet) {
			if (result.keySet().contains(key)) {
				SymbolEntry rSE = result.get(key);
				SymbolEntry aSE = addend.get(key);
				List<String> activeValues = rSE.getActiveRaw();
				for (String aValue : aSE.getActiveRaw()) {
					if (!activeValues.contains(aValue)) {
						// result does not contain addend's value; add it
						rSE.add(getSymbolValue(aValue), true);
						rc |= true;
					}
				}
				List<String> removedValues = rSE.getRemovedRaw();
				for (String aValue : aSE.getRemovedRaw()) {
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
				SymbolEntry aSymbolEntry = addend.get(key);
				result.put(key, aSymbolEntry);
				rc |= true;
			}
		}
		return rc;
	}

	/**
	 * Returns a symbol key (i.e. for DEF=1 returns DEF)
	 */
	public static String getSymbolKey(String symbol) {
		int index = symbol.indexOf('=');
		if (index != -1) {
			return symbol.substring(0, index).trim();
		}
		return symbol;
	}
	
	/**
	 * Returns a symbol value (i.e. for DEF=1 returns 1),  may be null
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
	 * @param symbolEntryMap map of [symbol's key, symbolEntry]
	 */
	public static void removeSymbolEntryValue(String symbol, Map<String, SymbolEntry> symbolEntryMap) {
		String key = getSymbolKey(symbol);
		String value = getSymbolValue(symbol);
		// find it in the discoveredSymbols Map of SymbolEntries
		SymbolEntry se = symbolEntryMap.get(key);
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
	 * @return new map of include paths
	 */
	public static LinkedHashMap<String, SymbolEntry> swapIncludePaths(LinkedHashMap<String, SymbolEntry> sumPaths, int index1, int index2) {
		int size = sumPaths.size();
		if (index1 == index2 ||
			!(index1 >= 0 && index1 < size && 
			  index2 >= 0 && index2 < size)) {
			return sumPaths;
		}
		ArrayList<String> pathKeyList = new ArrayList<String>(sumPaths.keySet());
		String temp1 = pathKeyList.get(index1);
		String temp2 = pathKeyList.get(index2);
		pathKeyList.set(index1, temp2);
		pathKeyList.set(index2, temp1);
		
		LinkedHashMap<String, SymbolEntry> newSumPaths = new LinkedHashMap<String, SymbolEntry>(sumPaths.size());
		for (String key : pathKeyList) {
			newSumPaths.put(key, sumPaths.get(key));
		}
		return newSumPaths;
	}
	
	/**
	 * Tokenizes string with quotes
	 */
	public static String[] tokenizeStringWithQuotes(String line, String quoteStyle) {
		ArrayList<String> allTokens = new ArrayList<String>();
		String[] tokens = line.split(quoteStyle);
		for (int i = 0; i < tokens.length; ++i) {
			if (i % 2 == 0) { // even tokens need further tokenization
				String[] sTokens = tokens[i].split("\\s+"); //$NON-NLS-1$
				for (int j = 0; j < sTokens.length; allTokens.add(sTokens[j++])) {}
			}
			else {
				allTokens.add(tokens[i]);
			}
		}
		return allTokens.toArray(new String[allTokens.size()]);
	}

	/**
	 * Converts array of IPath-s to array of String-s
	 */
	public static String[] iPathArray2StringArray(IPath[] paths) {
		String[] rv = new String[paths.length];
		for (int i = 0; i < paths.length; ++i) {
			rv[i] = paths[i].toString(); 
		}
		return rv;
	}
	
}
