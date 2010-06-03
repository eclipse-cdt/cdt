/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.IAsmLabel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;

/**
 * A label in assembly code.
 *
 * @since 5.0
 */
public class AsmLabel extends SourceManipulation implements IAsmLabel, IParent {

	private final int fIndex;
	private final boolean fIsGlobal;

	/**
	 * Create a new assembly label.
	 * 
	 * @param parent  the parent element (must be ITranslationUnit)
	 * @param name  the name of the label
	 * @param global  if <code>true</code>, the label is declared global (visible to the linker)
	 * @param index  numbering of labels with the same name
	 */
	public AsmLabel(ICElement parent, String name, boolean global, int index) {
		super(parent, name, ICElement.ASM_LABEL);
		fIsGlobal= global;
		fIndex= index;
	}

	/*
	 * @see org.eclipse.cdt.core.model.IAsmLabel#isGlobal()
	 */
	public final boolean isGlobal() {
		return fIsGlobal;
	}

	/*
	 * @see org.eclipse.cdt.internal.core.model.CElement#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof AsmLabel) {
			return equals(this, (AsmLabel) o);
		}
		return false;
	}
	
	public static boolean equals(AsmLabel lhs, AsmLabel rhs) {
		if (CElement.equals(lhs, rhs)) {
			return lhs.fIndex == rhs.fIndex && lhs.fIsGlobal == rhs.fIsGlobal;
		}
		return false;
	}
}
