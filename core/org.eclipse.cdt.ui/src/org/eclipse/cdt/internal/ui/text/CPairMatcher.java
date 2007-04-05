/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;

import org.eclipse.cdt.ui.text.ICPartitions;

/**
 * Helper class for match pairs of characters.
 */
public class CPairMatcher extends DefaultCharacterPairMatcher {

	public CPairMatcher(char[] pairs) {
		super(pairs, ICPartitions.C_PARTITIONING);
	}

}
