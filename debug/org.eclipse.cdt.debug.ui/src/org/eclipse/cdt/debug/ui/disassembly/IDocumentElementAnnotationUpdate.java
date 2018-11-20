/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.disassembly;

import org.eclipse.jface.text.source.Annotation;

/**
 * Request to provide annotations for the given element and presentation context.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 *
 * This interface is experimental
 */
public interface IDocumentElementAnnotationUpdate extends IDocumentUpdate {

	/**
	 * Adds an annotation to this request.
	 *
	 * @param annotation the annotation to add
	 */
	public void addAnnotation(Annotation annotation);
}
