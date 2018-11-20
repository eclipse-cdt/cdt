/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.cxx;

import org.eclipse.cdt.codan.core.model.ICodanProblemMarker;
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
 * properly for non hardcoded errors.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * </p>
 */
public abstract class AbstractCodanCQuickFixProcessor implements IQuickFixProcessor {
	@Override
	public boolean hasCorrections(ITranslationUnit unit, int problemId) {
		return problemId == 42;
	}

	@Override
	public ICCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations)
			throws CoreException {
		if (locations == null || locations.length == 0)
			return null;
		IProblemLocation loc = locations[0];
		IPath location = context.getTranslationUnit().getLocation();
		IFile astFile = ResourceLookup.selectFileForLocation(location,
				context.getTranslationUnit().getCProject().getProject());
		IMarker[] markers = astFile.findMarkers(loc.getMarkerType(), false, 1);
		for (int i = 0; i < markers.length; i++) {
			IMarker m = markers[i];
			int start = m.getAttribute(IMarker.CHAR_START, -1);
			if (start == loc.getOffset()) {
				String id = m.getAttribute(ICodanProblemMarker.ID, ""); //$NON-NLS-1$
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

	public abstract ICCompletionProposal[] getCorrections(IInvocationContext context, String problemId, IMarker marker);
}