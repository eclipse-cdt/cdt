/*******************************************************************************
 * Copyright (c) 2008, 2009 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.internal.ui.text.doctools.NullDocCommentOwner;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * Manages the population and selection of the doc-comment {@link Combo} box
 * <em>This class is not intended for use outside of CDT</em>
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DocCommentOwnerCombo extends Composite {
	protected Combo fCombo;
	protected IDocCommentOwner fOwners[], fInitialOwner;

	public DocCommentOwnerCombo(Composite parent, int style, IDocCommentOwner initialOwner) {
		super(parent, style);
		setLayout(new GridLayout());
		fInitialOwner = initialOwner;
		fOwners = getNontestOwners();
		createControl(this);
	}

	public IDocCommentOwner getSelectedDocCommentOwner() {
		int index = fCombo.getSelectionIndex();
		return index == 0 ? NullDocCommentOwner.INSTANCE : fOwners[index - 1];
	}

	/**
	 * @return the list of registered doc-comment owners, filtering out those from the
	 * test plug-in.
	 */
	private static IDocCommentOwner[] getNontestOwners() {
		List<IDocCommentOwner> result = new ArrayList<>();
		for (IDocCommentOwner owner : DocCommentOwnerManager.getInstance().getRegisteredOwners()) {
			if (owner.getID().indexOf(".test.") == -1) //$NON-NLS-1$
				result.add(owner);
		}
		return result.toArray(new IDocCommentOwner[result.size()]);
	}

	public void createControl(Composite parent) {
		String[] items = new String[fOwners.length + 1];
		items[0] = DialogsMessages.DocCommentOwnerCombo_None;
		for (int i = 0; i < fOwners.length; i++) {
			items[i + 1] = fOwners[i].getName();
		}
		fCombo = ControlFactory.createSelectCombo(parent, items, DialogsMessages.DocCommentOwnerCombo_None);
		selectInCombo(fInitialOwner);
	}

	public void selectInCombo(IDocCommentOwner owner) {
		for (int i = 0; i < fOwners.length; i++) {
			if (fOwners[i].getID().equals(owner.getID())) {
				fCombo.select(i + 1);
				return;
			}
		}
		fCombo.select(0);
	}

	@Override
	public void setEnabled(boolean enabled) {
		fCombo.setEnabled(enabled);
	}
}
