/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.TextFileChange;

import org.eclipse.cdt.ui.refactoring.CTextFileChange;

import org.eclipse.cdt.internal.core.dom.rewrite.ICTextFileChangeFactory;

/**
 * Factory provided to the core plugin to create appropriate text file changes.
 * @since 5.0
 */
public class CTextFileChangeFactory implements ICTextFileChangeFactory {

	@Override
	public TextFileChange createCTextFileChange(IFile file) {
		return new CTextFileChange(file.getName(), file);
	}
}
