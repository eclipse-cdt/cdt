/*
 * Created on Sep 21, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.cdt.make.core.makefile;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author alain
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface IMakefileValidator {
	public abstract void setMarkerGenerator(IMarkerGenerator errorHandler);
	public abstract void checkFile(IFile file, IProgressMonitor monitor);
}