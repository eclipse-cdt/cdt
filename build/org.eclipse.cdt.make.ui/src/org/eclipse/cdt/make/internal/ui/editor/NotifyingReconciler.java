/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	private ArrayList<IReconcilingParticipant> fReconcilingParticipants= new ArrayList<IReconcilingParticipant>();
	
	/**
	 * Constructor for NotifyingReconciler.
	 */
	public NotifyingReconciler(IReconcilingStrategy strategy, boolean isIncremental) {
		super(strategy, isIncremental);
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.AbstractReconciler#process(org.eclipse.jface.text.reconciler.DirtyRegion)
	 */
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
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.AbstractReconciler#initialProcess()
	 */
	@Override
	protected void initialProcess() {
		super.initialProcess();
		notifyReconcilingParticipants();
	}
}
