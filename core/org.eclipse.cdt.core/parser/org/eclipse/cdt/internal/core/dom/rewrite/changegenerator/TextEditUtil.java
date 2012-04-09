/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.changegenerator;

import org.eclipse.text.edits.CopyTargetEdit;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MoveSourceEdit;
import org.eclipse.text.edits.MoveTargetEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

public class TextEditUtil {
	// Do not instantiate. All methods are static.
	private TextEditUtil() {
	}
	
	/**
	 * Degenerates the given edit tree into a list.<br>
	 * All nodes of the result are leafs.<br>
	 * <strong>The given edit is modified and can no longer be used.</strong>
	 *
	 * @param edit the edit tree to flatten
	 * @return a MultiTextEdit containing the list of edits
	 */
	public static MultiTextEdit flatten(TextEdit edit) {
		MultiTextEdit result= new MultiTextEdit();
		flatten(edit, result);
		return result;
	}

	private static void flatten(TextEdit edit, MultiTextEdit result) {
		if (edit.hasChildren()) {
			TextEdit[] children= edit.getChildren();
			for (int i= 0; i < children.length; i++) {
				TextEdit child= children[i];
				child.getParent().removeChild(0);
				flatten(child, result);
			}
		} else if (!(edit instanceof MultiTextEdit)) {
			result.addChild(edit);
		}
	}

	/**
	 * Create an edit which contains <code>edit1</code> and <code>edit2</code>
	 * <p><strong>The given edits are modified and they can no longer be used.</strong></p>
	 *
	 * @param edit1 the edit to merge with edit2
	 * @param edit2 the edit to merge with edit1
	 * @return the merged tree
	 * @throws MalformedTreeException if the two edits ovelap
	 */
	public static TextEdit merge(TextEdit edit1, TextEdit edit2) {
		if (edit1 instanceof MultiTextEdit && !edit1.hasChildren()) {
			return edit2;
		}

		if (edit2 instanceof MultiTextEdit && !edit2.hasChildren()) {
			return edit1;
		}

		MultiTextEdit result= new MultiTextEdit();
		merge(edit1, edit2, result);
		return result;
	}

	private static void merge(TextEdit edit1, TextEdit edit2, MultiTextEdit result) {
		if (edit1 instanceof MultiTextEdit && edit2 instanceof MultiTextEdit) {
			MultiTextEdit multiTextEdit1= (MultiTextEdit) edit1;
			if (!multiTextEdit1.hasChildren()) {
				result.addChild(edit2);
				return;
			}

			MultiTextEdit multiTextEdit2= (MultiTextEdit) edit2;
			if (!multiTextEdit2.hasChildren()) {
				result.addChild(edit1);
				return;
			}

			TextEdit[] children1= multiTextEdit1.getChildren();
			TextEdit[] children2= multiTextEdit2.getChildren();

			int i1= 0;
			int i2= 0;
			while (i1 < children1.length && i2 < children2.length) {
				while (i1 < children1.length && children1[i1].getExclusiveEnd() < children2[i2].getOffset()) {
					edit1.removeChild(0);
					result.addChild(children1[i1]);
					i1++;
				}
				if (i1 >= children1.length)
					break;

				while (i2 < children2.length && children2[i2].getExclusiveEnd() < children1[i1].getOffset()) {
					edit2.removeChild(0);
					result.addChild(children2[i2]);
					i2++;
				}
				if (i2 >= children2.length)
					break;

				if (children1[i1].getExclusiveEnd() < children2[i2].getOffset())
					continue;

				edit1.removeChild(0);
				edit2.removeChild(0);
				merge(children1[i1], children2[i2], result);

				i1++;
				i2++;
			}

			while (i1 < children1.length) {
				edit1.removeChild(0);
				result.addChild(children1[i1]);
				i1++;
			}

			while (i2 < children2.length) {
				edit2.removeChild(0);
				result.addChild(children2[i2]);
				i2++;
			}
		} else if (edit1 instanceof MultiTextEdit) {
			TextEdit[] children= edit1.getChildren();

			int i= 0;
			while (children[i].getExclusiveEnd() < edit2.getOffset()) {
				edit1.removeChild(0);
				result.addChild(children[i]);
				i++;
				if (i >= children.length) {
					result.addChild(edit2);
					return;
				}
			}
			edit1.removeChild(0);
			merge(children[i], edit2, result);
			i++;
			while (i < children.length) {
				edit1.removeChild(0);
				result.addChild(children[i]);
				i++;
			}
		} else if (edit2 instanceof MultiTextEdit) {
			TextEdit[] children= edit2.getChildren();

			int i= 0;
			while (children[i].getExclusiveEnd() < edit1.getOffset()) {
				edit2.removeChild(0);
				result.addChild(children[i]);
				i++;
				if (i >= children.length) {
					result.addChild(edit1);
					return;
				}
			}
			edit2.removeChild(0);
			merge(edit1, children[i], result);
			i++;
			while (i < children.length) {
				edit2.removeChild(0);
				result.addChild(children[i]);
				i++;
			}
		} else {
			if (edit1.getExclusiveEnd() < edit2.getOffset()) {
				result.addChild(edit1);
				result.addChild(edit2);
			} else {
				result.addChild(edit2);
				result.addChild(edit1);
			}
		}
	}

	/**
	 * Returns the difference in the document length caused by the edit. {@code InsertEdit}s have
	 * positive delta, {@code DeleteEdit}s have negative one.
	 * @param edit the edit to determine delta for.
	 * @return the delta
	 */
	public static int delta(TextEdit edit) {
		int delta = 0;
		for (TextEdit child : edit.getChildren()) {
			delta += delta(child);
		}
		delta += ownDelta(edit);
		return delta;
	}

	private static int ownDelta(TextEdit edit) {
		if (edit instanceof DeleteEdit || edit instanceof MoveSourceEdit) {
			return -edit.getLength();
		} else if (edit instanceof InsertEdit) {
			return ((InsertEdit) edit).getText().length();
		} else if (edit instanceof ReplaceEdit) {
			return ((ReplaceEdit) edit).getText().length() - edit.getLength();
		} else if (edit instanceof CopyTargetEdit) {
			return ((CopyTargetEdit) edit).getSourceEdit().getLength();
		} else if (edit instanceof MoveTargetEdit) {
			return ((MoveTargetEdit) edit).getSourceEdit().getLength();
		}
		return 0;
	}
}
