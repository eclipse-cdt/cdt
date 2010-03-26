package org.eclipse.cdt.codan.provisional.core.model.cfg;

import java.util.Iterator;

public interface IStartNode extends IBasicBlock, ISingleOutgoing {
	Iterator<IBasicBlock> getExitNodeIterator();

	int getExitNodeSize();
}
