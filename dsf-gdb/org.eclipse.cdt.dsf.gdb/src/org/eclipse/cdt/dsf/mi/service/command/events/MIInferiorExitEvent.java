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
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.events;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;

/**
 * *stopped,reason="exited-normally"
 * *stopped,reason="exited",exit-code="04"
 * ^done,reason="exited",exit-code="04"
 *
 */
@Immutable
public class MIInferiorExitEvent extends MIEvent<ICommandControlDMContext> {

	final private int code;

	/**
	 * @since 1.1
	 */
	public MIInferiorExitEvent(ICommandControlDMContext ctx, int token, MIResult[] results, int code) {
		super(ctx, token, results);
		this.code = code;
	}

	public int getExitCode() {
		return code;
	}

	/**
	 * @since 1.1
	 */
	public static MIInferiorExitEvent parse(ICommandControlDMContext ctx, int token, MIResult[] results) {
		int code = 0;
		if (results != null) {
			for (int i = 0; i < results.length; i++) {
				String var = results[i].getVariable();
				MIValue value = results[i].getMIValue();
				String str = ""; //$NON-NLS-1$
				if (value instanceof MIConst) {
					str = ((MIConst) value).getString();
				}

				if (var.equals("exit-code")) { //$NON-NLS-1$
					try {
						code = Integer.decode(str.trim()).intValue();
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		return new MIInferiorExitEvent(ctx, token, results, code);
	}

}
