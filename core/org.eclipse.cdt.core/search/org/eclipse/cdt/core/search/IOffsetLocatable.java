package org.eclipse.cdt.core.search;

public interface IOffsetLocatable extends IMatchLocatable {
	 int getNameStartOffset();
	 int getNameEndOffset();
	 
	 int getElementStartOffset();
	 int getElementEndOffset();
}
