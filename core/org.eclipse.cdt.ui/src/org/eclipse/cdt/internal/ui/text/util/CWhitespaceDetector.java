/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.cdt.internal.ui.text.util;

import org.eclipse.jface.text.rules.IWhitespaceDetector;


/**
 * A simple white space detector.
 */
public class CWhitespaceDetector implements IWhitespaceDetector {

	/*
	 * @see IWhitespaceDetector#isWhitespace(char)
	 */
	@Override
	public boolean isWhitespace(char c) {
		switch (c) {
		case ' ':
		case '\t':
		case '\r':
		case '\n':
			return true;
		default:
			return false;
		}
	}
}
