/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyPosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.LabelPosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyDocument;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;

/**
 * A text hover to evaluate registers and variables under the cursor.
 */
@SuppressWarnings("restriction")
public class DisassemblyTextHover implements ITextHover {

	private final DisassemblyPart fDisassemblyPart;

	/**
	 * Create a new disassembly text hover.
	 */
	public DisassemblyTextHover(DisassemblyPart part) {
		fDisassemblyPart= part;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
	 */
    @Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		IDocument doc = textViewer.getDocument();
		return CWordFinder.findWord(doc, offset);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		DisassemblyDocument doc = (DisassemblyDocument)textViewer.getDocument();
		int offset = hoverRegion.getOffset();
		AddressRangePosition pos;
		try {
			String ident = doc.get(offset, hoverRegion.getLength());
			String value = null;
			pos = doc.getModelPosition(offset);
			
			value = fDisassemblyPart.getHoverInfoData(pos, ident);

			// If returns null (or empty string), not implemented or something went wrong.		
			if (value == null || value.length() == 0) {
				if (pos instanceof SourcePosition) {
					value = evaluateExpression(ident);
				} else if (pos instanceof LabelPosition) {
					value = evaluateExpression(ident);
				} else if (pos instanceof DisassemblyPosition) {
					// first, try to evaluate as register
					value = evaluateRegister(ident);
					if (value == null) {
						// if this fails, try expression
						value = evaluateExpression(ident);
					}
				}
				if (value != null) {
					return ident + " = " + value; //$NON-NLS-1$
				}
			}
			else
				return value;
		} catch (BadLocationException e) {
			if (DsfUIPlugin.getDefault().isDebugging()) {
				DsfUIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, "Internal Error", e)); //$NON-NLS-1$
			}
		}
		return null;
	}

	/**
	 * Evaluate the given register.
	 * @param register
	 * @return register value or <code>null</code>
	 */
	private String evaluateRegister(String register) {
        return fDisassemblyPart.evaluateRegister(register);
	}

	/**
	 * Evaluate the given expression.
	 * @param expr
	 * @return expression value or <code>null</code>
	 */
	private String evaluateExpression(final String expr) {
		return fDisassemblyPart.evaluateExpression(expr);
	}

}
