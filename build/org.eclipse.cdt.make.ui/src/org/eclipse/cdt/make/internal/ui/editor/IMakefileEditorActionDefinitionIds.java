/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 */
public interface IMakefileEditorActionDefinitionIds extends ITextEditorActionDefinitionIds {

	final String UNCOMMENT = "org.eclipse.cdt.make.ui.edit.text.makefile.uncomment"; //$NON-NLS-1$

	final String COMMENT = "org.eclipse.cdt.make.ui.edit.text.makefile.comment"; //$NON-NLS-1$

	final String OPEN_DECLARATION = "org.eclipse.cdt.make.ui.edit.text.makefile.opendcl"; //$NON-NLS-1$

}
