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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Utility class that handles some Scanner Config specifig collection conversions
 * 
 * @author vhirsl
 */
public final class ScannerConfigUtil {
	/**
	 * Converts a map of symbols to a set
	 * 
	 * @param symbolsMap
	 * @return
	 */
	public static Set scSymbolsMap2Set(Map symbolsMap) {
		Set retSymbols = new HashSet();
		Set keys = symbolsMap.keySet();
		for (Iterator i = keys.iterator(); i.hasNext(); ) {
			String symbol;
			String key = (String) i.next();
			String value = (String) symbolsMap.get(key);
			if (value == null || value.length() == 0) {
				symbol = key;
			}
			else {
				symbol = key + "=" + value;	//$NON-NLS-1
			}
			retSymbols.add(symbol);
		}
		return retSymbols;
	}

	/**
	 * Adds all new discovered symbols/values to the existing ones.
	 *  
	 * @param sumSymbols - a map of [String, Set] where Set is a SymbolEntry
	 * @param symbols
	 * @return boolean
	 */
	public static boolean scAddSymbolsSet2SymbolEntryMap(Map sumSymbols, Set symbols, boolean preferred) {
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
				sEntry = new SymbolEntry(key, value, true);
				rc = true;
			}
			else {
				rc |= sEntry.add(value, preferred);
			}
			sumSymbols.put(key, sEntry);
		}
		return rc;
	}

	/**
	 * Gets all discovered symbols with preferred values
	 * @param sumSymbols
	 * @return
	 */
	public static Set scSymbolsSymbolEntryMap2Set(Map sumSymbols) {
		Set symbols = (Set) sumSymbols.entrySet();
		Set rv = new HashSet(symbols.size());
		for (Iterator i = symbols.iterator(); i.hasNext(); ) {
			SymbolEntry sEntry = (SymbolEntry) ((Map.Entry) i.next()).getValue();
			rv.add(sEntry.getPreferedRaw());
		}
		return rv;
	}
}
