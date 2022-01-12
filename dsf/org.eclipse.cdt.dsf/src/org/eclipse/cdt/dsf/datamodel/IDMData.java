/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
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
package org.eclipse.cdt.dsf.datamodel;

import org.eclipse.cdt.dsf.concurrent.Immutable;

/**
 * Marker interface for data corresponding to IDMContext, retrieved from a
 * service.  These data objects are meant to be processed by clients on
 * different threads, therefore they should be immutable.
 *
 * @since 1.0
 */
@Immutable
public interface IDMData {
}
