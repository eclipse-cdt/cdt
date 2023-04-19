package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTName;

public interface ITemplateIdStrategy {
	boolean shallParseAsTemplateID(IASTName name);
}
