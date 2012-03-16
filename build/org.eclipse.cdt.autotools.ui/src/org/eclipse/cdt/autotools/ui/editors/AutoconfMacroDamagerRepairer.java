/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;

public class AutoconfMacroDamagerRepairer extends DefaultDamagerRepairer {
	
	public final static String UNMATCHED_RIGHT_PARENTHESIS = "UnmatchedRightParenthesis"; //$NON-NLS-1$
	public final static String UNMATCHED_LEFT_PARENTHESIS = "UnmatchedLeftParenthesis"; //$NON-NLS-1$
	public final static String UNMATCHED_RIGHT_QUOTE = "UnmatchedRightQuote"; //$NON-NLS-1$
	public final static String UNMATCHED_LEFT_QUOTE = "UnmatchedLeftQuote"; //$NON-NLS-1$
	
	/**
	 * Creates a damager/repairer that uses the given scanner. The scanner may not be <code>null</code>
	 * and is assumed to return only token that carry text attributes.
	 *
	 * @param scanner the token scanner to be used, may not be <code>null</code>
	 */
	public AutoconfMacroDamagerRepairer(ITokenScanner scanner) {
		super(scanner);
	}

	/*
	 * @see IPresentationDamager#getDamageRegion(ITypedRegion, DocumentEvent, boolean)
	 */
	public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e, boolean documentPartitioningChanged) {
		// In the case of a partition with multiline rules, we will punt to
		// reparse the entire partition because we don't know if the line being
		// edited is in the middle of an area covered by a multiline rule.  In
		// such a case, we need to back up and find the start sequence of the
		// rule.  It is easiest to just reparse the whole partition.
		return partition;
	}
	
	/*
	 * @see IPresentationRepairer#createPresentation(TextPresentation, ITypedRegion)
	 */
	public void createPresentation(TextPresentation presentation, ITypedRegion region) {

//		int offset = region.getOffset();
//		int length = region.getLength();
		
		super.createPresentation(presentation, region);
	}
}
