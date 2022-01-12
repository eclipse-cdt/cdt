/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.template.c;

/**
 * A context type for documentation comments.
 *
 * @since 5.1
 */
public class DocCommentContextType extends CommentContextType {

	@SuppressWarnings("hiding")
	public static final String ID = "org.eclipse.cdt.ui.text.templates.doccomment"; //$NON-NLS-1$

	public DocCommentContextType() {
	}

}
