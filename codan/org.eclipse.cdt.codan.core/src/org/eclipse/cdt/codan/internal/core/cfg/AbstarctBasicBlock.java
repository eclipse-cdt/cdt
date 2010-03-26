package org.eclipse.cdt.codan.internal.core.cfg;

import java.util.Iterator;

import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;

public abstract class AbstarctBasicBlock implements IBasicBlock {
	static class OneElementIterator<T> implements Iterator<T> {
		private T o;

		public OneElementIterator(T o) {
			this.o = o;
		}

		public boolean hasNext() {
			return o != null;
		}

		public T next() {
			T x = o;
			o = null;
			return x;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
