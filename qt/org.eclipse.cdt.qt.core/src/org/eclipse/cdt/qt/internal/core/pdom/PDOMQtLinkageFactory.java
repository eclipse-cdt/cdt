package org.eclipse.cdt.qt.internal.core.pdom;

import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.qt.core.QtPlugin;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public class PDOMQtLinkageFactory implements IPDOMLinkageFactory {

	@Override
	public PDOMLinkage getLinkage(PDOM pdom, long record) {
		try {
			return new QtPDOMLinkage(pdom, record);
		} catch(CoreException e) {
			QtPlugin.log(e);
		}
		return null;
	}

	@Override
	public PDOMLinkage createLinkage(PDOM pdom) throws CoreException {
		return new QtPDOMLinkage(pdom);
	}
}
