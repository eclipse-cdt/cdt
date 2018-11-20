/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import org.eclipse.jface.wizard.Wizard;

/**
 * @deprecated as of CDT 4.0. This class does not seem to be used,
 * probably remnant of 3.X style projects or earlier.
 */
@Deprecated
public class CPathFilterPathPage extends Wizard {

	@Override
	public boolean performFinish() {
		return true;
	}

}
