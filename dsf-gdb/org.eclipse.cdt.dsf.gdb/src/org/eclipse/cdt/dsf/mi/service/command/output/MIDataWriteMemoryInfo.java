/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson Communication - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * -data-write-memory result
 *
 * (gdb)
 * nn-data-write-memory [command parameters]
 * nn^done
 *
 */
public class MIDataWriteMemoryInfo extends MIInfo {

	/**
	 * Constructor
	 *
	 * @param output
	 */
	public MIDataWriteMemoryInfo(MIOutput output) {

		super(output);
	}

}
