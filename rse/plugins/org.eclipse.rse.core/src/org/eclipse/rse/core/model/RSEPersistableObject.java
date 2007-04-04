package org.eclipse.rse.core.model;

public abstract class RSEPersistableObject implements IRSEPersistableContainer {

	private boolean _isDirty = false;
	private boolean _wasRestored = false;
	private boolean _isTainted = false;
	private boolean _restoring = false;

	public RSEPersistableObject() {
		super();
	}

	public final boolean isDirty() {
		return _isDirty;
	}

	public final void setDirty(boolean flag) {
		if (!_restoring) {
			_isDirty = flag;
			if (flag) {
				setTainted(true);
			}
		}
	}

	public final void beginRestore() {
		_restoring = true;
	}

	public final void endRestore() {
		_restoring = false;
		setWasRestored(true);
	}

	public final boolean wasRestored() {
		return _wasRestored;
	}

	public final void setWasRestored(boolean flag) {
		_wasRestored = flag;
	}

	public final boolean isTainted() {
		return _isTainted;
	}

	public final void setTainted(boolean flag) {
		if (!_restoring) {
			_isTainted = flag;
			if (_isTainted) {
				IRSEPersistableContainer parent = getPersistableParent();
				if (parent != null) {
					parent.setTainted(true);
				}
			} else {
				IRSEPersistableContainer[] children = getPersistableChildren();
				for (int i = 0; i < children.length; i++) {
					IRSEPersistableContainer child = children[i];
					child.setTainted(false);
				}
			}
		}
	}

}