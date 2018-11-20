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
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;

public class AutomakefilePresentationReconciler extends PresentationReconciler {

	public AutomakefilePresentationReconciler() {
		AutomakefileCodeScanner scanner = new AutomakefileCodeScanner();
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, MakefilePartitionScanner.MAKEFILE_COMMENT_PARTITION);
		setRepairer(dr, MakefilePartitionScanner.MAKEFILE_COMMENT_PARTITION);

		dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, MakefilePartitionScanner.MAKEFILE_MACRO_ASSIGNEMENT_PARTITION);
		setRepairer(dr, MakefilePartitionScanner.MAKEFILE_MACRO_ASSIGNEMENT_PARTITION);

		dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, MakefilePartitionScanner.MAKEFILE_INCLUDE_BLOCK_PARTITION);
		setRepairer(dr, MakefilePartitionScanner.MAKEFILE_INCLUDE_BLOCK_PARTITION);

		dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, MakefilePartitionScanner.MAKEFILE_IF_BLOCK_PARTITION);
		setRepairer(dr, MakefilePartitionScanner.MAKEFILE_IF_BLOCK_PARTITION);

		dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, MakefilePartitionScanner.MAKEFILE_DEF_BLOCK_PARTITION);
		setRepairer(dr, MakefilePartitionScanner.MAKEFILE_DEF_BLOCK_PARTITION);

		dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, MakefilePartitionScanner.MAKEFILE_OTHER_PARTITION);
		setRepairer(dr, MakefilePartitionScanner.MAKEFILE_OTHER_PARTITION);
	}

}
