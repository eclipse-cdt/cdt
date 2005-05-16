package org.eclipse.cdt.core.search;

public class LineLocatable implements ILineLocatable {

	int startLine;
	int endLine;
	
	public LineLocatable(int startLine, int endLine){
		this.startLine = startLine;
		this.endLine = endLine;
	}
	
	public int getStartLine() {
		return startLine;
	}
	
	public int getEndLine() {
		return endLine;
	}
}
