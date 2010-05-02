/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.cxx;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.IInvocationContext;
import org.eclipse.cdt.ui.text.IProblemLocation;
import org.eclipse.cdt.ui.text.IQuickFixProcessor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * Abstract class IQuickFixProcessor - not used right now because it does not work
 * properly for non hardcoded errors
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * </p>
 */
public abstract class AbstractCodanCQuickFixProcessor implements IQuickFixProcessor {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.ui.text.IQuickFixProcessor#hasCorrections(org.eclipse
	 * .cdt.ui.text.ITranslationUnit, int)
	 */
	public boolean hasCorrections(ITranslationUnit unit, int problemId) {
		return problemId == 42;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.ui.text.IQuickFixProcessor#getCorrections(org.eclipse
	 * .cdt.ui.text.IInvocationContext,
	 * org.eclipse.cdt.ui.text.IProblemLocation[])
	 */
	public ICCompletionProposal[] getCorrections(IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {
		if (locations==null || locations.length==0) return null;
		IProblemLocation loc = locations[0];
		IPath location= context.getTranslationUnit().getLocation();
		IFile astFile = ResourceLookup.selectFileForLocation(location, context.getTranslationUnit().getCProject().getProject());
		IMarker[] markers = astFile.findMarkers(loc.getMarkerType(), false, 1);
		for (int i = 0; i < markers.length; i++) {
			IMarker m = markers[i];
			int start = m.getAttribute(IMarker.CHAR_START, -1);
			if (start==loc.getOffset()) {
				String id = m.getAttribute(IMarker.PROBLEM,"");
				return getCorrections(context, id, m);
			}
		}
		
		return null;
	}

	public int getOffset(IMarker marker, IDocument doc) {
		int charStart = marker.getAttribute(IMarker.CHAR_START, -1);
		int position;
		if (charStart > 0) {
			position = charStart;
		} else {
			int line = marker.getAttribute(IMarker.LINE_NUMBER, -1) - 1;
			try {
				position = doc.getLineOffset(line);
			} catch (BadLocationException e) {
				return -1;
			}
		}
		return position;
	}
	/**
	 * @param context
	 * @param loc
	 * @param marker
	 * @return
	 */
	public abstract ICCompletionProposal[] getCorrections(IInvocationContext context,
			String problemId, IMarker marker);
}