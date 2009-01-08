/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.AddressRangePosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyDocument;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyPosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.LabelPosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.SourcePosition;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
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
    public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		IDocument doc = textViewer.getDocument();
		return CWordFinder.findWord(doc, offset);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		DisassemblyDocument doc = (DisassemblyDocument)textViewer.getDocument();
		int offset = hoverRegion.getOffset();
		AddressRangePosition pos;
		try {
			String ident = doc.get(offset, hoverRegion.getLength());
			String value = null;
			pos = doc.getModelPosition(offset);
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
		// TLETODO [disassembly] evaluate register
        return null;
	}

	/**
	 * Evaluate the given expression.
	 * @param expr
	 * @return expression value or <code>null</code>
	 */
	private String evaluateExpression(String expr) {
		final IExpressions expressions= fDisassemblyPart.getService(IExpressions.class);
		if (expressions == null) {
			return null;
		}
		final IFrameDMContext frameDmc= fDisassemblyPart.getTargetFrameContext();
		if (frameDmc == null || !fDisassemblyPart.isSuspended()) {
			return null;
		}
		IExpressionDMContext exprDmc= expressions.createExpression(frameDmc, expr);
		final FormattedValueDMContext valueDmc= expressions.getFormattedValueContext(exprDmc, IFormattedValues.NATURAL_FORMAT);
		final DsfExecutor executor= fDisassemblyPart.getSession().getExecutor();
		Query<FormattedValueDMData> query= new Query<FormattedValueDMData>() {
			@Override
			protected void execute(final DataRequestMonitor<FormattedValueDMData> rm) {
				expressions.getFormattedExpressionValue(valueDmc, new DataRequestMonitor<FormattedValueDMData>(executor, rm) {
					@Override
					protected void handleSuccess() {
						FormattedValueDMData data= getData();
						rm.setData(data);
						rm.done();
					}
				});
			}};
		
		executor.execute(query);
		FormattedValueDMData data= null;
		try {
			data= query.get();
		} catch (InterruptedException exc) {
		} catch (ExecutionException exc) {
		}
		if (data != null) {
			return data.getFormattedValue();
		}
		return null;
	}

}
