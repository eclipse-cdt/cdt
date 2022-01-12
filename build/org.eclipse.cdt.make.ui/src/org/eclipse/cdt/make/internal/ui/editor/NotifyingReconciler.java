/*******************************************************************************
 * Copyright (c) 2002, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.editor;

import java.util.ArrayList;

import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;

/**
 * NotifyingReconciler
 */
public class NotifyingReconciler extends MonoReconciler {
	private ArrayList<IReconcilingParticipant> fReconcilingParticipants = new ArrayList<>();

	/**
	 * Constructor for NotifyingReconciler.
	 */
	public NotifyingReconciler(IReconcilingStrategy strategy, boolean isIncremental) {
		super(strategy, isIncremental);
	}

	@Override
	protected void process(DirtyRegion dirtyRegion) {
		super.process(dirtyRegion);
		notifyReconcilingParticipants();
	}

	public void addReconcilingParticipant(IReconcilingParticipant participant) {
		fReconcilingParticipants.add(participant);
	}

	public void removeReconcilingParticipant(IReconcilingParticipant participant) {
		fReconcilingParticipants.remove(participant);
	}

	protected void notifyReconcilingParticipants() {
		for (IReconcilingParticipant participant : fReconcilingParticipants) {
			participant.reconciled();
		}
	}

	@Override
	protected void initialProcess() {
		super.initialProcess();
		notifyReconcilingParticipants();
	}
}
