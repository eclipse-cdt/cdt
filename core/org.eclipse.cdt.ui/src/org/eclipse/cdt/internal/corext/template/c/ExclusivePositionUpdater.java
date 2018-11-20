/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin, Google
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.template.c;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Position;

/**
 * Position updater that takes any changes at the borders of a position to not belong to the position.
 */
final class ExclusivePositionUpdater implements IPositionUpdater {

	/** The position category. */
	private final String fCategory;

	/**
	 * Creates a new updater for the given <code>category</code>.
	 *
	 * @param category the new category.
	 */
	public ExclusivePositionUpdater(String category) {
		fCategory = category;
	}

	/*
	 * @see org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text.DocumentEvent)
	 */
	@Override
	public void update(DocumentEvent event) {

		int eventOffset = event.getOffset();
		int eventOldLength = event.getLength();
		int eventNewLength = event.getText() == null ? 0 : event.getText().length();
		int deltaLength = eventNewLength - eventOldLength;

		try {
			Position[] positions = event.getDocument().getPositions(fCategory);

			for (int i = 0; i != positions.length; i++) {

				Position position = positions[i];

				if (position.isDeleted())
					continue;

				int offset = position.getOffset();
				int length = position.getLength();
				int end = offset + length;

				if (offset >= eventOffset + eventOldLength)
					// position comes
					// after change - shift
					position.setOffset(offset + deltaLength);
				else if (end <= eventOffset) {
					// position comes way before change -
					// leave alone
				} else if (offset <= eventOffset && end >= eventOffset + eventOldLength) {
					// event completely internal to the position - adjust length
					position.setLength(length + deltaLength);
				} else if (offset < eventOffset) {
					// event extends over end of position - adjust length
					int newEnd = eventOffset;
					position.setLength(newEnd - offset);
				} else if (end > eventOffset + eventOldLength) {
					// event extends from before position into it - adjust offset
					// and length
					// offset becomes end of event, length adjusted accordingly
					int newOffset = eventOffset + eventNewLength;
					position.setOffset(newOffset);
					position.setLength(end - newOffset + deltaLength);
				} else {
					// event consumes the position - delete it
					position.delete();
				}
			}
		} catch (BadPositionCategoryException e) {
			// ignore and return
		}
	}

	/**
	 * Returns the position category.
	 *
	 * @return the position category
	 */
	public String getCategory() {
		return fCategory;
	}
}
