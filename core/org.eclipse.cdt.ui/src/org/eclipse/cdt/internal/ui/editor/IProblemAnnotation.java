/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

/**
 * Interface of annotations representing problems.
 */
public interface IProblemAnnotation {

	String getMessage();

	int getId();

	String[] getArguments();

	boolean isTemporaryProblem();

	boolean isWarning();

	boolean isError();

	boolean isProblem();
}
