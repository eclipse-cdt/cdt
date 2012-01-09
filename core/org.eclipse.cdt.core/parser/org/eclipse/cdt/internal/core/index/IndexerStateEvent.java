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

import org.eclipse.cdt.core.index.IIndexerStateEvent;

public class IndexerStateEvent implements IIndexerStateEvent {

	public static final int STATE_IDLE= 0;
	public static final int STATE_BUSY= 1;
	
	private int fState;

	public IndexerStateEvent() {
		this(STATE_IDLE);
	}
	
	public IndexerStateEvent(int state) {
		fState= state;
	}

	public void setState(int state) {
		fState= state;
	}

	@Override
	public boolean indexerIsIdle() {
		return fState == STATE_IDLE;
	}
}
