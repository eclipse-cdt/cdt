/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
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
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson				- Modified for handling of frame contexts
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarSetFormatInfo;

/**
 *
 *    -var-set-format NAME FORMAT-SPEC
 *
 *  Sets the output format for the value of the object NAME to be
 * FORMAT-SPEC.
 *
 *  The syntax for the FORMAT-SPEC is as follows:
 *
 *     FORMAT-SPEC ==>
 *     {binary | decimal | hexadecimal | octal | natural}
 *
 */
public class MIVarSetFormat extends MICommand<MIVarSetFormatInfo> {
	/**
	 * @since 1.1
	 */
	public MIVarSetFormat(ICommandControlDMContext ctx, String name, String fmt) {
		super(ctx, "-var-set-format"); //$NON-NLS-1$
		setParameters(new String[] { name, getFormat(fmt) });
	}

	private String getFormat(String fmt) {
		String format = "natural"; //$NON-NLS-1$

		if (IFormattedValues.HEX_FORMAT.equals(fmt)) {
			format = "hexadecimal"; //$NON-NLS-1$
		} else if (IFormattedValues.BINARY_FORMAT.equals(fmt)) {
			format = "binary"; //$NON-NLS-1$
		} else if (IFormattedValues.OCTAL_FORMAT.equals(fmt)) {
			format = "octal"; //$NON-NLS-1$
		} else if (IFormattedValues.NATURAL_FORMAT.equals(fmt)) {
			format = "natural"; //$NON-NLS-1$
		} else if (IFormattedValues.DECIMAL_FORMAT.equals(fmt)) {
			format = "decimal"; //$NON-NLS-1$
		}
		return format;
	}

	@Override
	public MIVarSetFormatInfo getResult(MIOutput out) {
		return new MIVarSetFormatInfo(out);
	}
}
