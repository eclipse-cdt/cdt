/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional;

import java.math.BigInteger;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.LabelPosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyDocument;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.AnnotationModel;

public class DisassemblyAnnotationModel extends AnnotationModel {

	public DisassemblyAnnotationModel() {
		super();
	}

	private DisassemblyDocument getDisassemblyDocument() {
		return (DisassemblyDocument) fDocument;
	}

	protected Position createPositionFromSourceLine(String fileName, int lineNumber) {
		if (fileName != null) {
			return getDisassemblyDocument().getSourcePosition(fileName, lineNumber);
		}
		return null;
	}

	protected Position createPositionFromSourceLine(IFile file, int lineNumber) {
		if (file != null) {
			return getDisassemblyDocument().getSourcePosition(file, lineNumber);
		}
		return null;
	}

	protected Position createPositionFromAddress(BigInteger address) {
		if (address != null) {
			AddressRangePosition p= getDisassemblyDocument().getDisassemblyPosition(address);
			if (p != null && p.fValid) {
				return new Position(p.offset, p.length);
			}
		}
		return null;
	}

	protected Position createPositionFromLabel(BigInteger address) {
		if (address != null) {
			LabelPosition p = getDisassemblyDocument().getLabelPosition(address);
			if (p != null && p.fValid) {
				return new Position(p.offset, p.length);
			}
		}
		return null;
	}

	protected Position createPositionFromLabel(String label) {
		if (label != null) {
			try {
				Position[] labelPositions = getDisassemblyDocument().getPositions(DisassemblyDocument.CATEGORY_LABELS);
				int labelLen = label.length();
				for (Position position : labelPositions) {
					if (position instanceof LabelPosition) {
						String candidate = ((LabelPosition) position).fLabel;
						if (candidate != null && candidate.startsWith(label)) {
							// exact match or followed by ()
							if (candidate.length() == labelLen || candidate.charAt(labelLen) == '(') {
								return position;
							}
						}
					}
				}
			} catch (BadPositionCategoryException exc) {
				return null;
			}
		}
		return null;
	}

}