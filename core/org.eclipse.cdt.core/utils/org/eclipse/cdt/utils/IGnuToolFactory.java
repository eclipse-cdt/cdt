/*
 * Created on Jul 5, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.utils;

import org.eclipse.core.runtime.IPath;

/**
 * @author DInglis
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IGnuToolFactory {

	public abstract Addr2line getAddr2line(IPath path);

	public abstract CPPFilt getCPPFilt();

	public abstract Objdump getObjdump(IPath path);
}
