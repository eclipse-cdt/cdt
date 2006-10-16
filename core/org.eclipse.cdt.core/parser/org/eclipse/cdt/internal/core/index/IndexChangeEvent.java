/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.index.IIndexChangeEvent;
import org.eclipse.cdt.core.model.ICProject;

public class IndexChangeEvent implements IIndexChangeEvent {

	private ICProject fAffectedProject;

	public IndexChangeEvent(ICProject projectChanged) {
		fAffectedProject= projectChanged;
	}

	public IndexChangeEvent() {
		fAffectedProject= null;
	}

	public ICProject getAffectedProject() {
		return fAffectedProject;
	}
	
	public void setAffectedProject(ICProject project) {
		fAffectedProject= project;
	}
}
