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
package org.eclipse.cdt.make.internal.core.scannerconfig.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			key = symbol.substring(0, index).trim();
			value = symbol.substring(index + 1).trim();
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
						rSE.add(aValue, true);
						rc |= true;
					}
				}
				List removedValues = rSE.getRemovedRaw();
				for (Iterator j = aSE.getRemovedRaw().iterator(); j.hasNext(); ) {
					String aValue = (String) j.next();
					if (!removedValues.contains(aValue)) {
						// result does not contain addend's value; add it
						rSE.add(aValue, false);
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
}
