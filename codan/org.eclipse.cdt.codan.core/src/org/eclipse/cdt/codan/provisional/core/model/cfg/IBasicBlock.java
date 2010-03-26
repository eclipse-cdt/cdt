package org.eclipse.cdt.codan.provisional.core.model.cfg;

import java.util.Iterator;

public interface IBasicBlock {
	Iterator<IBasicBlock> getIncomingIterator();

	Iterator<IBasicBlock> getOutgoingIterator();

	int getIncomingSize();

	int getOutgoingSize();

}
