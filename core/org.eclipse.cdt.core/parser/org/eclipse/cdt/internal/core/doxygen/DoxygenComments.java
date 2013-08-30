package org.eclipse.cdt.internal.core.doxygen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDoxygenComment;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class DoxygenComments implements Iterable<IASTDoxygenComment> {
	List<IASTDoxygenComment> doxygenComments = new LinkedList<IASTDoxygenComment>();
	List<IASTDoxygenComment> toRemove = new LinkedList<IASTDoxygenComment>();

	public DoxygenComments(IASTTranslationUnit tu) {
		IASTComment [] comments = tu.getComments();
		for (int i=0; i<comments.length; i++) {
			if (comments[i] instanceof IASTDoxygenComment) {
				doxygenComments.add((IASTDoxygenComment)comments[i]);
			}
		}
	}

	public void prepareForRemoval(IASTDoxygenComment comment) {
		toRemove.add(comment);
	}

	public void remove() {
		doxygenComments.removeAll(toRemove);
	}

	@Override
	public Iterator<IASTDoxygenComment> iterator() {
		return doxygenComments.iterator();
	}
}
