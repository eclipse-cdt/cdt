/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Static methods used by rename and move participants.
 */
public class RenameParticipantHelper {
	/**
	 * Consolidates the change produced by a refactoring participant with the one produced by
	 * the refactoring processor. If the two changes are be affecting the same files, the part of
	 * the refactoring participant's change that affects the files also modified by the refactoring
	 * processor is merged into the processor's change. The remaining part, if any, is returned from
	 * the method.
	 *
	 * @param change the change produced by a refactoring participant
	 * @param participant the refactoring participant
	 * @return the resulting change, or {@code null}
	 */
	public static Change postprocessParticipantChange(Change change, RefactoringParticipant participant) {
		if (change == null)
			return null;
		CompositeChange compositeChange = change instanceof CompositeChange ? (CompositeChange) change : null;
		Change[] changes = compositeChange != null ? compositeChange.getChildren() : new Change[] { change };
		for (Change ch : changes) {
			if (ch instanceof TextChange) {
				TextChange textChange = (TextChange) ch;
				Object element = textChange.getModifiedElement();
				TextChange primaryChange = participant.getTextChange(element);
				if (primaryChange != null) {
					TextEdit edit = textChange.getEdit();
					addExplodedTextEdit(edit, primaryChange);
					if (compositeChange != null) {
						compositeChange.remove(ch);
					} else {
						change = null;
					}
				}
			}
		}
		if (compositeChange != null && compositeChange.getChildren().length == 0)
			change = null;
		return change;
	}

	private static void addExplodedTextEdit(TextEdit textEdit, TextChange primaryChange) {
		if (textEdit instanceof MultiTextEdit) {
			TextEdit[] children = ((MultiTextEdit) textEdit).removeChildren();
			for (TextEdit edit : children) {
				addExplodedTextEdit(edit, primaryChange);
			}
		} else {
			primaryChange.addEdit(textEdit);
		}
	}

	private RenameParticipantHelper() {
	}
}
