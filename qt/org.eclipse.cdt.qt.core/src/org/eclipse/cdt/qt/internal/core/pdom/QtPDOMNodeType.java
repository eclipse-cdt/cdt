package org.eclipse.cdt.qt.internal.core.pdom;

import org.eclipse.cdt.internal.core.index.IIndexBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public enum QtPDOMNodeType {

	QObject;

	public final int Type = IIndexBindingConstants.LAST_CONSTANT + 1 + ordinal();

	/**
	 * The current version of the QtPDOMLinkage.  This can be used to make sure the persisted
	 * data matches what is expected by the implementation.  Care should be taken to make changes
	 * backward compatible when possible.
	 * <p>
	 * The version is needed because ordinals for these enumerators are written to the file.
	 */
	public static final int VERSION = 1;

	public static QtPDOMNodeType forType(int version, int type) {
		// Nothing has been deleted or replaced yet, so the version is ignored.

		for(QtPDOMNodeType node : values())
			if (node.Type == type)
				return node;
		return null;
	}

	// This needs to return PDOMNode so that it can be either QtPDOMNode or QtPDOMBinding.
	public static PDOMNode load(QtPDOMLinkage linkage, int nodeType, long record) throws CoreException {
		QtPDOMNodeType node = QtPDOMNodeType.forType(linkage.getVersion(), nodeType);
		if (node == null)
			return null;

		switch(node) {
		case QObject:
			return new QtPDOMQObject(linkage, record);
		}

		return null;
	}
}
