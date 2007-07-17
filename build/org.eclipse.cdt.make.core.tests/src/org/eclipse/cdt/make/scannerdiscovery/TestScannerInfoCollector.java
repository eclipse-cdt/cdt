/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.make.scannerdiscovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;

final class TestScannerInfoCollector implements IScannerInfoCollector {
	private HashMap fInfoMap = new HashMap();

	public void contributeToScannerConfig(Object resource, Map scannerInfo) {
		for (Iterator iterator = scannerInfo.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			ScannerInfoTypes key = (ScannerInfoTypes) entry.getKey();
			List value = (List) entry.getValue();
			addTo(key, value);
			if (ScannerInfoTypes.COMPILER_COMMAND.equals(key)) {
				for (Iterator iterator2 = value.iterator(); iterator2.hasNext();) {
					CCommandDSC cdsc= (CCommandDSC) iterator2.next();
					cdsc.resolveOptions(null);
					addTo(ScannerInfoTypes.INCLUDE_PATHS, cdsc.getIncludes());
					addTo(ScannerInfoTypes.QUOTE_INCLUDE_PATHS, cdsc.getQuoteIncludes());
					addTo(ScannerInfoTypes.SYMBOL_DEFINITIONS, cdsc.getSymbols());
				}
			}
		}
	}

	private void addTo(ScannerInfoTypes type, List col) {
		Collection target= (Collection) fInfoMap.get(type);
		if (target == null) {
			target= new ArrayList();
			fInfoMap.put(type, target);
		}
		target.addAll(col);
	}

	public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
		List result= (List) fInfoMap.get(type);
		return result == null ? Collections.EMPTY_LIST : result;
	}
}