/*
 */
package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 */
public interface IMakefileDocumentProvider extends IDocumentProvider {

	/**
	 * Shuts down this provider.
	 */
	void shutdown();

	/**
	 * Returns the working copy for the given element.
	 *
	 * @param element the element
	 * @return the working copy for the given element
	 */
	IMakefile getWorkingCopy(Object element);

}
