/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 */
public interface IMakefileEditorActionDefinitionIds extends ITextEditorActionDefinitionIds {

	String UNCOMMENT = "org.eclipse.cdt.make.ui.edit.text.makefile.comment";

	String COMMENT = "org.eclipse.cdt.make.ui.edit.text.makefile.uncomment";

}
