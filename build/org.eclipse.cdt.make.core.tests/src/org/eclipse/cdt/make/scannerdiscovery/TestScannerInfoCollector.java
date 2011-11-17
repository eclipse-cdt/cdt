/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.scannerdiscovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;

@SuppressWarnings({"rawtypes", "unchecked"})
final class TestScannerInfoCollector implements IScannerInfoCollector {
	private HashMap<ScannerInfoTypes, List> fInfoMap = new HashMap<ScannerInfoTypes, List>();
	private HashMap<Object, Map<ScannerInfoTypes, List>> fResourceToInfoMap = new HashMap<Object, Map<ScannerInfoTypes, List>>();

	@Override
	public void contributeToScannerConfig(Object resource, Map scannerInfo0) {
		Map<ScannerInfoTypes, List> scannerInfo = scannerInfo0;
		Set<Entry<ScannerInfoTypes, List>> entrySet = scannerInfo.entrySet();
		for (Entry<ScannerInfoTypes, List> entry : entrySet) {
			ScannerInfoTypes key = entry.getKey();
			List value = entry.getValue();
			addTo(key, value);
			if (ScannerInfoTypes.COMPILER_COMMAND.equals(key)) {
				List<CCommandDSC> cdscs = value;
				for (CCommandDSC cdsc : cdscs) {
					cdsc.resolveOptions(null);
					addTo(ScannerInfoTypes.INCLUDE_PATHS, cdsc.getIncludes());
					addTo(ScannerInfoTypes.QUOTE_INCLUDE_PATHS, cdsc.getQuoteIncludes());
					addTo(ScannerInfoTypes.SYMBOL_DEFINITIONS, cdsc.getSymbols());
				}
			}
		}
		if (resource != null) {
			fResourceToInfoMap.put(resource, scannerInfo);
		}
	}

	private void addTo(ScannerInfoTypes type, List<String> col) {
		List<String> target = fInfoMap.get(type);
		if (target == null) {
			target= new ArrayList<String>();
			fInfoMap.put(type, target);
		}
		target.addAll(col);
	}

	@Override
	public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
		if (resource == null) {
			List result= fInfoMap.get(type);
			return result == null ? Collections.EMPTY_LIST : result;
		}
		Map<ScannerInfoTypes, List> scannerInfo= fResourceToInfoMap.get(resource);
		if (scannerInfo != null) {
			List result= scannerInfo.get(type);
			return result == null ? Collections.EMPTY_LIST : result;
		}
		return Collections.EMPTY_LIST;
	}
}
