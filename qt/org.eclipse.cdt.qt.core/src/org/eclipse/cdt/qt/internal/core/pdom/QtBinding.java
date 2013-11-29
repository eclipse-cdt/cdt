package org.eclipse.cdt.qt.internal.core.pdom;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.Linkage;

@SuppressWarnings("restriction")
public class QtBinding implements IBinding {

	private final QtPDOMNodeType type;
	private final QtBinding owner;
	private final IASTName qtName;
	private final IASTName cppName;

	private QtPDOMBinding pdomBinding;

	public QtBinding(QtPDOMNodeType type, IASTName qtName, IASTName cppName) {
		this(type, null, qtName, cppName);
	}

	public QtBinding(QtPDOMNodeType type, QtBinding owner, IASTName qtName, IASTName cppName) {
		this.type = type;
		this.owner = owner;
		this.qtName = qtName;
		this.cppName = cppName;
	}

	public QtPDOMNodeType getType() {
		return type;
	}

	public IASTName getQtName() {
		return qtName;
	}

	public IASTName getCppName() {
		return cppName;
	}

	public void setPDOMBinding(QtPDOMBinding pdomBinding) {
		this.pdomBinding = pdomBinding;
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (getClass().isAssignableFrom(adapter))
			return this;
		if (QtPDOMBinding.class.isAssignableFrom(adapter))
			return pdomBinding;

		return null;
	}

	@Override
	public String getName() {
		return String.valueOf(getNameCharArray());
	}

	@Override
	public char[] getNameCharArray() {
		return qtName.getSimpleID();
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.QT_LINKAGE;
	}

	@Override
	public IBinding getOwner() {
		return owner;
	}

	@Override
	public IScope getScope() throws DOMException {
		return null;
	}
}
