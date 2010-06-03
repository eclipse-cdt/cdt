/*******************************************************************************
 * Copyright (c) 2007, 2009 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.internal.core.CharOperation;

public class TodoTaskParser {
	private static final Task[] EMPTY_TASK_ARRAY = new Task[0];

	private final char[][] tags;
	private final int[] priorities;
	private final boolean isTaskCaseSensitive;
	private final int[] order;

	public TodoTaskParser(char[][] taskTags, int[] taskPriorities, boolean isTaskCaseSensitive) {
		assert taskPriorities.length == taskTags.length;
		this.tags = taskTags;
		this.priorities = taskPriorities;
		this.isTaskCaseSensitive = isTaskCaseSensitive;

		// Calculate task checking order that gives preference to the longest matching tag.
		this.order = new int[taskTags.length];
		for (int i = 0; i < order.length; i++) {
			order[i] = i;
		}
		// Sort order array in reverse order of tag lengths.
		// Shell sort algorithm from http://en.wikipedia.org/wiki/Shell_sort
		for (int inc = order.length / 2; inc > 0; inc /= 2) {
			for (int i = inc; i < order.length; i++) {
				for (int j = i;
						j >= inc && taskTags[order[j - inc]].length < taskTags[order[j]].length;
						j -= inc) {
					int temp = order[j];
					order[j] = order[j - inc];
					order[j - inc] = temp;
				}
			}
		}
	}

	public Task[] parse(IASTComment[] comments) {
		HashSet<String> locKeys= new HashSet<String>();
		List<Task> tasks = new ArrayList<Task>();
		for (IASTComment comment : comments) {
			IASTFileLocation location = comment.getFileLocation();
			if (location != null) { // be defensive, bug 213307
				final String fileName = location.getFileName();
				final int nodeOffset = location.getNodeOffset();
				final String key= fileName + ':' + nodeOffset;
				// full indexer can yield duplicate comments, make sure to handle each comment only once (bug 287181)
				if (locKeys.add(key)) {
					parse(comment.getComment(), fileName, nodeOffset,
							location.getStartingLineNumber(), tasks);
				}
			}
		}
		if (tasks.isEmpty()) {
			return EMPTY_TASK_ARRAY;
		}
		return tasks.toArray(new Task[tasks.size()]);
	}
	
    private void parse(char[] comment, String filename, int offset, int lineNumber,
    		List<Task> tasks) {
        int commentLength = comment.length;

    	int foundTaskIndex = tasks.size();
    	char previous = comment[1]; // Should be '*' or '/'
    	for (int i = 2; i < commentLength; i++) {
    		char[] tag = null;
			nextTag : for (int j = 0; j < order.length; j++) {
				int itag = order[j];
				tag = tags[itag];
				int tagLength = tag.length;
				if (tagLength == 0 || i + tagLength > commentLength)
					continue nextTag;
	
				// Ensure tag is not leaded by a letter if the tag starts with a letter.
				if (isIdentifierStart(tag[0]) && isIdentifierPart(previous)) {
					continue nextTag;
				}
	
				for (int t = 0; t < tagLength; t++) {
					int x = i + t;
					if (x >= commentLength)
						continue nextTag;
					char sc = comment[x];
					char tc = tag[t];
					if (sc != tc) { 											 // case sensitive check
						if (isTaskCaseSensitive || Character.toLowerCase(sc) != Character.toLowerCase(tc)) { // case insensitive check
							continue nextTag;
						}
					}
				}
				// Ensure tag is not followed by a letter if the tag ends with a letter.
				if (i + tagLength < commentLength && isIdentifierPart(comment[i + tagLength - 1]) &&
						isIdentifierPart(comment[i + tagLength])) {
					continue nextTag;
				}
				
				Task task = new Task(filename, i, i + tagLength, lineNumber,
						String.valueOf(tag), "", priorities[itag]); //$NON-NLS-1$
				tasks.add(task);
				i += tagLength - 1; // Will be incremented when looping
				break nextTag;
			}
    		previous = comment[i];
    	}

    	boolean containsEmptyTask = false;
    	for (int i = foundTaskIndex; i < tasks.size(); i++) {
    		Task task = tasks.get(i);
    		// Retrieve message start and end positions
    		int msgStart = task.start + task.tag.length();
    		int maxValue = i + 1 < tasks.size() ? tasks.get(i + 1).start : commentLength;
    		// At most beginning of next task
    		if (maxValue < msgStart) {
    			maxValue = msgStart; // Would only occur if tag is before EOF.
    		}
    		int end = -1;
    		char c;
    		for (int j = msgStart; j < maxValue; j++) {
    			if ((c = comment[j]) == '\n' || c == '\r') {
    				end = j;
    				break;
    			}
    		}
    		if (end == -1) {
    			for (int j = maxValue; --j >= msgStart;) {
    				if ((c = comment[j]) == '*') {
    					end = j;
    					break;
    				}
    			}
    			if (end == -1) {
    				end = maxValue;
    			}
    		}
    		// Trim the message
    		while (msgStart < end && CharOperation.isWhitespace(comment[end - 1])) {
    			end--;
    		}
    		while (msgStart < end && (CharOperation.isWhitespace(comment[msgStart]) || comment[msgStart] == ':')) {
    			msgStart++;
    		}
    		if (msgStart == end) {
    			// If the description is empty, we might want to see if two tags
    			// are not sharing the same message.
    			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=110797
    			containsEmptyTask = true;
    			continue;
    		}
    		// Update the end position of the task
    		task.end = end;
    		// Get the message source
    		int messageLength = end - msgStart;
    		task.message = String.valueOf(comment, msgStart, messageLength);
    	}

    	if (containsEmptyTask) {
    		for (int i = foundTaskIndex; i < tasks.size(); i++) {
    			Task task1 = tasks.get(i);
    			if (task1.message.length() == 0) {
    				for (int j = i + 1; j < tasks.size(); j++) {
    	    			Task task2 = tasks.get(j);
    					if (task2.message.length() != 0) {
    						task1.message = task2.message;
    						task1.end = task2.end;
    						break;
    					}
    				}
    			}
    		}
    	}
    	
    	// Add comment offset.
		for (int i = foundTaskIndex; i < tasks.size(); i++) {
			Task task = tasks.get(i);
			task.lineNumber += getLineOffset(comment, task.start);
			task.start += offset;
			task.end += offset;
		}
    }
	
    /**
     * Returns zero-based line number for a given character position.
     */
	private static int getLineOffset(char[] buffer, int pos) {
		int count = 0;
		for (int i = 0; i < pos && i < buffer.length; i++) {
			if (buffer[i] == '\n') {
				count++;
			}
		}
		return count;
	}

	private static boolean isIdentifierStart(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_'
				|| Character.isUnicodeIdentifierPart(c);
	}
	
	private static boolean isIdentifierPart(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_'
		        || c >= '0' && c <= '9'
		        || Character.isUnicodeIdentifierPart(c);
	}


	public static class Task {
		private String fileLocation;
		private int start;
		private int end;
		private int lineNumber;
		private String tag;
		private String message;
		private int priority;
		
		Task(String fileLocation, int start, int end, int lineNumber,
				String tag, String message, int priority) {
			this.fileLocation = fileLocation;
			this.start = start;
			this.end = end;
			this.lineNumber = lineNumber;
			this.tag = tag;
			this.message = message;
			this.priority = priority;
		}

		public String getFileLocation() {
			return fileLocation;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}

		public int getLineNumber() {
			return lineNumber;
		}

		public String getTag() {
			return tag;
		}

		public String getMessage() {
			return message;
		}

		public int getPriority() {
			return priority;
		}
	}
}
