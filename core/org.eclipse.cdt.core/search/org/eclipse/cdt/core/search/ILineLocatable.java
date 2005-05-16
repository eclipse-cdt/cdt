package org.eclipse.cdt.core.search;

public interface ILineLocatable extends IMatchLocatable {
	 int getStartLine();
	 int getEndLine();
}
