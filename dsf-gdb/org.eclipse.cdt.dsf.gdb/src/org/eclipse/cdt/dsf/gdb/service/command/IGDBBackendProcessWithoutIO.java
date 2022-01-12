/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

/**
 * Interface used by a process representing the GDB process but for which there
 * is no IO.
 *
 * When using the full GDB console, this marker can be used for the class that
 * will represent the GDB process in the launch since the IO should not be
 * handled by the launch and the console it normally created, but is handled by
 * the full GDB console itself.
 *
 * @since 5.2
 */
public interface IGDBBackendProcessWithoutIO {
}