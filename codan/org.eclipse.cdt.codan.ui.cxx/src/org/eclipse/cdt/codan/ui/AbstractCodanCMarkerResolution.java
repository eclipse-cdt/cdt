/*******************************************************************************
 * Copyright (c) 2009, 2016 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia   - initial API and implementation
 *    Tomasz Wesolowski - extension
 *******************************************************************************/
package org.eclipse.cdt.codan.ui;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Generic class for codan marker resolution (for quick fix). Use as a base
 * class for codanMarkerResolution extension. To add specific icon and
 * description client class should additionally implement
 * {@link IMarkerResolution2}
 *
 * @since 2.0
 */
public abstract class AbstractCodanCMarkerResolution implements ICodanMarkerResolution {
	private boolean codanProblem;

	/**
	 * Get position offset from marker. If CHAR_START attribute is not set for
	 * marker, line and document would be used.
	 *
	 * @param marker
	 * @param doc
	 * @return
	 */
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
	 * Check if the given marker is associated with a Codan problem
	 * @since 3.4
	 */
	public boolean isCodanProblem(IMarker marker) {
		return getProblemId(marker) != null;
	}

	/**
	 * @deprecated Does not work as expected when called
	 *             in {@link #isApplicable(IMarker)}, use
	 *             {@link #isCodanProblem(IMarker)} instead
	 */
	@Deprecated
	public boolean isCodanProblem() {
		return codanProblem;
	}

	public String getProblemArgument(IMarker marker, int index) {
		return CodanProblemMarker.getProblemArgument(marker, index);
	}

	/**
	 * Runs this resolution.
	 *
	 * @param marker
	 *        the marker to resolve
	 */
	@Override
	public void run(IMarker marker) {
		IDocument doc = openDocument(marker);
		if (doc != null) {
			codanProblem = getProblemId(marker) != null;
			apply(marker, doc);
		}
	}

	/**
	 * Apply marker resolution for given marker in given open document.
	 *
	 * @param marker
	 * @param document
	 */
	public abstract void apply(IMarker marker, IDocument document);

	/**
	 * Override is extra checks is required to determine appicablity of marker
	 * resolution
	 *
	 * @param marker
	 * @return
	 */
	@Override
	public boolean isApplicable(IMarker marker) {
		return true;
	}

	/**
	 * Opens an editor with the document corresponding to the given problem and
	 * returns the corresponding IEditorPart. Please note that is code analysis
	 * is setup to run on reconsile this action would trigger checkers run, and
	 * original marker may be removed as a result.
	 *
	 * @param marker
	 *        the problem marker
	 * @return the opened document
	 */
	protected IEditorPart openEditor(IMarker marker) {
		IEditorPart editorPart;
		try {
			editorPart = CodanEditorUtility.openInEditor(marker);
		} catch (PartInitException e) {
			CodanUIActivator.log(e);
			return null;
		}
		return editorPart;
	}

	/**
	 * Opens the editor and returns the document corresponding to a given
	 * marker.
	 *
	 * @param marker
	 *        the marker to find the editor
	 * @return the corresponding document
	 */
	protected IDocument openDocument(IMarker marker) {
		return openDocument(openEditor(marker));
	}

	/**
	 * Returns the document corresponding to a given editor part.
	 *
	 * @param editorPart
	 *        an editor part
	 * @return the document of that part
	 */
	protected IDocument openDocument(IEditorPart editorPart) {
		if (editorPart instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) editorPart;
			IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			return doc;
		}
		return null;
	}

	/**
	 * Receives a translation unit from a given marker. Opens the editor.
	 *
	 * @param marker
	 *        A marker in an editor to get the translation unit
	 * @return The translation unit
	 */
	protected ITranslationUnit getTranslationUnitViaEditor(IMarker marker) {
		ITranslationUnit tu = (ITranslationUnit) CDTUITools.getEditorInputCElement(openEditor(marker).getEditorInput());
		return tu;
	}

	/**
	 * Receives a translation unit from a given marker using the marker's path.
	 *
	 * @param marker
	 *        A marker in a translation unit
	 * @return The translation unit
	 */
	protected ITranslationUnit getTranslationUnitViaWorkspace(IMarker marker) {
		IPath path = marker.getResource().getFullPath();
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		ITranslationUnit tu = (ITranslationUnit) CoreModel.getDefault().create(file);
		return tu;
	}

	/**
	 * Receives an ASTName enclosing a given IMarker
	 *
	 * @param marker
	 *        The marker enclosing an ASTName
	 * @param ast
	 *        The AST to check
	 * @return The enclosing ASTName or null
	 */
	protected IASTName getASTNameFromMarker(IMarker marker, IASTTranslationUnit ast) {
		final int charStart = marker.getAttribute(IMarker.CHAR_START, -1);
		final int length = marker.getAttribute(IMarker.CHAR_END, -1) - charStart;
		return getASTNameFromPositions(ast, charStart, length);
	}

	/**
	 * @param ast
	 * @param charStart
	 * @param length
	 * @return
	 */
	protected IASTName getASTNameFromPositions(IASTTranslationUnit ast, final int charStart, final int length) {
		IASTName name = ast.getNodeSelector(null).findEnclosingName(charStart, length);
		return name;
	}

	/**
	 * Returns the smallest element within translation unit that
	 * includes the given source position (that is, a method, field, etc.), or
	 * {@code null} if there is no element other than the translation
	 * unit itself at the given position, or if the given position is not
	 * within the source range of this translation unit.
	 *
	 * @param marker that contains a position inside the translation unit
	 * @return the innermost C element enclosing a given source position or
	 *         {@code null}
	 *         if none found (excluding the translation unit).
	 * @since 3.3
	 */
	protected ICElement getCElementFromMarker(IMarker marker) {
		ITranslationUnit tu = getTranslationUnitViaEditor(marker);
		ICElement element = null;
		try {
			int charStart = marker.getAttribute(IMarker.CHAR_START, -1);
			if (charStart > 0) {
				element = tu.getElementAtOffset(charStart);
			} else {
				int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, -1);
				element = tu.getElementAtLine(lineNumber);
			}
		} catch (CModelException e) {
			CodanUIActivator.log(e);
		}
		return element;
	}

	/**
	 * Receives an {@link IIndex} corresponding to the given {@link IMarker}'s
	 * resource.
	 *
	 * @param marker
	 *        the marker to use
	 * @return the received index
	 * @throws CoreException
	 */
	protected IIndex getIndexFromMarker(final IMarker marker) throws CoreException {
		IProject project = marker.getResource().getProject();
		ICProject cProject = CoreModel.getDefault().create(project);
		IIndex index = CCorePlugin.getIndexManager().getIndex(cProject);
		return index;
	}

	/**
	 * @param marker
	 * @return
	 */
	public String getProblemId(IMarker marker) {
		return CodanProblemMarker.getProblemId(marker);
	}

	public IProblem getProblem(IMarker marker) {
		IResource resource = marker.getResource();
		if (resource == null) {
			// IMarker.getResource() is not supposed to return null,
			// but it looks like it sometimes does (bug 529383).
			return null;
		}
		IProblemProfile profile = CodanRuntime.getInstance().getCheckersRegistry().getResourceProfile(resource);
		String id = getProblemId(marker);
		return profile.findProblem(id);
	}

	/**
	 * @param marker
	 * @return
	 */
	public String getProblemMessage(IMarker marker) {
		return CodanProblemMarker.getMessage(marker);
	}

	/**
	 * Get the enclosing node using the input position
	 * @param ast The AST
	 * @param charStart Start position
	 * @param length Length
	 * @return The node or null
	 * @since 3.5
	 */
	protected IASTNode getASTNodeFromPosition(IASTTranslationUnit ast, final int charStart, final int length) {
		IASTNode node = ast.getNodeSelector(null).findEnclosingNode(charStart, length);
		return node;
	}

	/**
	 * Get the enclosing node using the input marker
	 * @param marker The marker
	 * @param ast The AST
	 * @return The node or null
	 * @since 3.5
	 */
	protected IASTNode getASTNodeFromMarker(IMarker marker, IASTTranslationUnit ast) {
		final int charStart = marker.getAttribute(IMarker.CHAR_START, -1);
		final int length = marker.getAttribute(IMarker.CHAR_END, -1) - charStart;
		return getASTNodeFromPosition(ast, charStart, length);
	}
}
