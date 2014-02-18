/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.pullup.ui;

import org.eclipse.cdt.internal.ui.refactoring.pullup.InheritanceLevel;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpHelper;

public class SubClassTreeEntry {

	private final PullUpMemberTableEntry mte;
	private final InheritanceLevel parent;

	public SubClassTreeEntry(PullUpMemberTableEntry mte, InheritanceLevel parent) {
		this.mte = mte;
		this.parent = parent;
	}

	public InheritanceLevel getParent() {
		return this.parent;
	}

	public PullUpMemberTableEntry getMember() {
		return this.mte;
	}

	@Override
	public String toString() {
		return PullUpHelper.getMemberString(this.mte.getMember());
	}
}
