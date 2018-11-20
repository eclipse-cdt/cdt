/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.autoconf;

import org.eclipse.cdt.autotools.ui.editors.AutoconfCodeScanner;
import org.eclipse.cdt.autotools.ui.editors.AutoconfMacroCodeScanner;
import org.eclipse.cdt.autotools.ui.editors.AutoconfMacroDamagerRepairer;
import org.eclipse.cdt.autotools.ui.editors.AutoconfPartitionScanner;
import org.eclipse.cdt.autotools.ui.editors.MultilineRuleDamagerRepairer;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;

public class AutoconfPresentationReconciler extends PresentationReconciler {

	public AutoconfPresentationReconciler() {
		DefaultDamagerRepairer dr = new AutoconfMacroDamagerRepairer(new AutoconfMacroCodeScanner());
		setDamager(dr, AutoconfPartitionScanner.AUTOCONF_MACRO);
		setRepairer(dr, AutoconfPartitionScanner.AUTOCONF_MACRO);

		dr = new DefaultDamagerRepairer(new AutoconfCodeScanner());
		setDamager(dr, AutoconfPartitionScanner.AUTOCONF_COMMENT);
		setRepairer(dr, AutoconfPartitionScanner.AUTOCONF_COMMENT);

		dr = new MultilineRuleDamagerRepairer(new AutoconfCodeScanner());
		setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
	}

}
