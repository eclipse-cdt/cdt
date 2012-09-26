/********************************************************************************
 * Copyright (c) 2012 MontaVista Software, LLC.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova (MontaVista)      - initial API and implementation
 ********************************************************************************/
package org.eclipse.cdt.launch.remote.te.controls;

import org.eclipse.cdt.launch.internal.remote.te.Messages;
import org.eclipse.cdt.launch.remote.te.Activator;
import org.eclipse.cdt.launch.remote.te.utils.TEHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.model.Model;

public class TCFPeerSelector {

	protected Combo combo;
	protected Label label;
	protected IPeerModel[] peers = null;

	private Composite composite;

	public TCFPeerSelector(Composite parent, int style, int numberOfColumns) {
		composite = new Composite(parent, style);
		GridLayout layout = new GridLayout(numberOfColumns, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		label = new Label(composite, SWT.NONE);
		label.setText(Messages.TCFPeerSelector_0);
		label.setLayoutData(new GridData(SWT.BEGINNING));

		combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		refreshCombo();

	}

	public void setEnabled(boolean enabled) {
		composite.setEnabled(enabled);
		label.setEnabled(enabled);
		combo.setEnabled(enabled);
	}

	public boolean isDisposed() {
		return composite == null || composite.isDisposed();
	}

	public void setLayoutData(Object layoutData) {
		if (composite != null) {
			composite.setLayoutData(layoutData);
		}
	}

	public void addSelectionListener(SelectionListener listener) {
		combo.addSelectionListener(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		combo.removeSelectionListener(listener);
	}

	public void addModifyListener(ModifyListener listener) {
		combo.addModifyListener(listener);
	}

	public void removeModifyListener(ModifyListener listener) {
		combo.removeModifyListener(listener);
	}

	/**
	 * Get the selected peer.
	 */
	public IPeerModel getPeer() {
		final String peerId = getPeerId();
		return TEHelper.getPeer(peerId);
	}

	/**
	 * Get the selected peerId.
	 */
	public String getPeerId() {
		if (peers != null && !combo.isDisposed()) {
			int selectionIndex = combo.getSelectionIndex();
			if (selectionIndex >= 0 && selectionIndex < peers.length) {
				return combo.getItem(selectionIndex);
			}
		}
		return null;
	}

	public void updateSelectionFrom(IPeerModel peer) {
		int newSelectedIndex = -1;
		String[] peerIds = combo.getItems();
		for (int i = 0; i < peerIds.length; i++) {
			if (peerIds[i].equals(peer.getPeerId())) {
				newSelectedIndex = i;
				break;
			}
		}
		if (newSelectedIndex >= 0) {
			combo.select(newSelectedIndex);
		}
	}

	public void updateSelectionFrom(String peerId) {
		int newSelectedIndex = -1;
		String[] peerIds = combo.getItems();
		for (int i = 0; i < peerIds.length; i++) {
			if (peerIds[i].equals(peerId)) {
				newSelectedIndex = i;
				break;
			}
		}
		if (newSelectedIndex >= 0) {
			combo.select(newSelectedIndex);
		}
	}

	protected void refreshCombo() {
		Activator.getDefault().initializeTE();
		peers = Model.getModel().getPeers();
		int newSelectedIndex = 0;
		for (int i = 0; i < peers.length; i++) {
			combo.add(peers[i].getPeerId());
		}
		combo.select(newSelectedIndex);
	}

}
