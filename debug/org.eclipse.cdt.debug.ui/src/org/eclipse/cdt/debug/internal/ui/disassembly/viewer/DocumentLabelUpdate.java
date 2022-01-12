/*******************************************************************************
 * Copyright (c) 2008, 2012 ARM Limited and others.
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

package org.eclipse.cdt.debug.internal.ui.disassembly.viewer;

import java.util.Properties;

import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementLabelUpdate;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentPresentation;

public class DocumentLabelUpdate extends DocumentUpdate implements IDocumentElementLabelUpdate {
	private DocumentLabelProvider fLabelProvider;
	private int fIndex = 0;
	private Properties fLabels;

	public DocumentLabelUpdate(DocumentLabelProvider labelProvider, IDocumentPresentation presentationContext,
			Object root, Object base, Object element, int index) {
		super(presentationContext, root, base, element);
		fLabelProvider = labelProvider;
		fIndex = index;
		fLabels = new Properties();
	}

	/* (non-Javadoc)
	 * @see com.arm.eclipse.rvd.ui.disassembly.IDocumentElementLabelUpdate#setLabel(java.lang.String, java.lang.String)
	 */
	@Override
	public void setLabel(String attribute, String text) {
		fLabels.put(attribute, text);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.commands.Request#done()
	 */
	@Override
	public void done() {
		super.done();
		getLabelProvider().completed(this);
	}

	public int getIndex() {
		return fIndex;
	}

	protected DocumentLabelProvider getLabelProvider() {
		return fLabelProvider;
	}

	protected Properties getLabels() {
		return fLabels;
	}

	/* (non-Javadoc)
	 * @see com.arm.eclipse.rvd.internal.ui.disassembly.DocumentUpdate#startRequest()
	 */
	@Override
	void startRequest() {
	}
}
