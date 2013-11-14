/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;

public class CallsToResult {
	private Map<CElementSet, List<IIndexName>> fElementSetsToReferences= new HashMap<CElementSet, List<IIndexName>>();

	public CElementSet[] getElementSets() {
		Set<CElementSet> elementSets = fElementSetsToReferences.keySet();
		return elementSets.toArray(new CElementSet[elementSets.size()]);
	}

	public IIndexName[] getReferences(CElementSet elementSet) {
		List<IIndexName> references= fElementSetsToReferences.get(elementSet);
		return references.toArray(new IIndexName[references.size()]);
	}

	public void add(ICElement[] elems, IIndexName ref) {
		CElementSet key= new CElementSet(elems);
		List<IIndexName> list= fElementSetsToReferences.get(key);
		if (list == null) {
			list= new ArrayList<IIndexName>();
			fElementSetsToReferences.put(key, list);
		}
		list.add(ref);
	}
}
