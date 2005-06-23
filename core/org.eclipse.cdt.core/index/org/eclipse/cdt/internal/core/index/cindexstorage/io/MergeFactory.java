/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.cindexstorage.io;

import java.io.IOException;
import java.util.Map;

import org.eclipse.cdt.internal.core.index.cindexstorage.IncludeEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.Util;
import org.eclipse.cdt.internal.core.index.cindexstorage.WordEntry;
import org.eclipse.cdt.internal.core.index.impl.Int;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.processing.JobManager;

/**
 * A mergeFactory is used to merge 2 indexes into one. One of the indexes 
 * (oldIndex) is on the disk and the other(addsIndex) is in memory.
 * The merge respects the following rules: <br>
 *   - The files are sorted in alphabetical order;<br>
 *   - if a file is in oldIndex and addsIndex, the one which is added 
 * is the one in the addsIndex.<br>
 */
public class MergeFactory {
	/**
	 * Input on the addsIndex.
	 */
	protected IndexInput addsInput;
	/**
	 * Input on the oldIndex. 
	 */
	protected IndexInput oldInput;
	/**
	 * Output to write the result of the merge in.
	 */
	protected BlocksIndexOutput mergeOutput;
	/**
	 * Files removed from oldIndex. 
	 */
	protected Map removedInOld;
	/**
	 * Files removed from addsIndex. 
	 */
	protected Map removedInAdds;
	protected int[] mappingOld;
	protected int[] mappingAdds;
	public static final int ADDS_INDEX= 0;
	public static final int OLD_INDEX= 1;
	/**
	 * MergeFactory constructor comment.
	 * @param directory java.io.File
	 */
	public MergeFactory(IndexInput oldIndexInput, IndexInput addsIndexInput, BlocksIndexOutput mergeIndexOutput, Map removedInOld, Map removedInAdds) {
		oldInput= oldIndexInput;
		addsInput= addsIndexInput;
		mergeOutput= mergeIndexOutput;
		this.removedInOld= removedInOld;
		this.removedInAdds= removedInAdds;
	}
	/**
	 * Initialise the merge.
	 */
	protected void init() {
		mappingOld= new int[oldInput.getNumFiles() + 1];
		mappingAdds= new int[addsInput.getNumFiles() + 1];

	}
	/**
	 * Merges the 2 indexes into a new one on the disk.
	 */
	public void merge() throws IOException {
		long startTime = 0;
		if (IndexManager.VERBOSE){
			JobManager.verbose("-> starting merge"); //$NON-NLS-1$
			startTime = System.currentTimeMillis();
		}
		try {
			//init
			addsInput.open();
			oldInput.open();
			mergeOutput.open();
			init();
			//merge
			//findChanges();
			mergeFiles();
			mergeReferences();
			mergeIncludes();
			mergeOutput.flush();
		} 
		catch ( Exception ex ){
			if (ex instanceof IOException)
			  throw (IOException) ex;
			else {
			  if (IndexManager.VERBOSE) {
				   JobManager.verbose("-> got the following exception during merge:"); //$NON-NLS-1$
				   ex.printStackTrace();
			  }
			}
		}
		catch ( VirtualMachineError er ) {
			if (IndexManager.VERBOSE) {
			  JobManager.verbose("-> got the following exception during merge:"); //$NON-NLS-1$
			  er.printStackTrace();
			} 
		}
		finally {
			//closes everything
			oldInput.close();
			addsInput.close();
			mergeOutput.close();
			
			if (IndexManager.VERBOSE){
				long elapsedTime = System.currentTimeMillis() - startTime;
				JobManager.verbose("-> merge complete: " + (elapsedTime > 0 ? elapsedTime : 0) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	/**
	 * Merges the files of the 2 indexes in the new index, removes the files
	 * to be removed, and records the changes made to propagate them to the 
	 * word references.
	 */

	protected void mergeFiles() throws IOException {
		int positionInMerge= 1;
		int compare;

		while (oldInput.hasMoreFiles() || addsInput.hasMoreFiles()) {
			IndexedFileEntry file1= oldInput.getCurrentFile();
			IndexedFileEntry file2= addsInput.getCurrentFile();

			//if the file has been removed we don't take it into account
			while (file1 != null && wasRemoved(file1, OLD_INDEX)) {
				oldInput.moveToNextFile();
				file1= oldInput.getCurrentFile();
			}
			while (file2 != null && wasRemoved(file2, ADDS_INDEX)) {
				addsInput.moveToNextFile();
				file2= addsInput.getCurrentFile();
			}

			//the addsIndex was empty, we just removed files from the oldIndex
			if (file1 == null && file2 == null)
				break;

			//test if we reached the end of one the 2 index
			if (file1 == null)
				compare= 1;
			else if (file2 == null)
				compare= -1;
			else
				compare= file1.getPath().compareTo(file2.getPath());

			//records the changes to Make
			if (compare == 0) {
				//the file has been modified: 
				//we remove it from the oldIndex and add it to the addsIndex
				removeFile(file1, OLD_INDEX);
				mappingAdds[file2.getFileID()]= positionInMerge;
				file1.setFileNumber(positionInMerge);
				mergeOutput.addFile(file1);
				oldInput.moveToNextFile();
				addsInput.moveToNextFile();
			} else if (compare < 0) {
				mappingOld[file1.getFileID()]= positionInMerge;
				file1.setFileNumber(positionInMerge);
				mergeOutput.addFile(file1);
				oldInput.moveToNextFile();
			} else {
				mappingAdds[file2.getFileID()]= positionInMerge;
				file2.setFileNumber(positionInMerge);
				mergeOutput.addFile(file2);
				addsInput.moveToNextFile();
			}
			positionInMerge++;
		}
		mergeOutput.flushFiles();		
	}
	/**
	 * Merges the files of the 2 indexes in the new index, according to the changes
	 * recorded during mergeFiles().
	 */
	protected void mergeReferences() throws IOException {
		int compare;
		while (oldInput.hasMoreWords() || addsInput.hasMoreWords()) {
			WordEntry word1= oldInput.getCurrentWordEntry();
			WordEntry word2= addsInput.getCurrentWordEntry();

			if (word1 == null && word2 == null)
				break;
			
			if (word1 == null)
				compare= 1;
			else if (word2 == null)
				compare= -1;
			else
				compare= Util.compare(word1.getWord(), word2.getWord());
			if (compare < 0) {
				word1.mapRefs(mappingOld);
				mergeOutput.addWord(word1);
				oldInput.moveToNextWordEntry();
			} else if (compare > 0) {
				word2.mapRefs(mappingAdds);
				mergeOutput.addWord(word2);
				addsInput.moveToNextWordEntry();
			} else {
				word1.mapRefs(mappingOld);
				word2.mapRefs(mappingAdds);
				word1.addWordInfo(word2.getRefs(), word2.getOffsets(),word2.getOffsetLengths(), word2.getOffsetCount(), word2.getModifiers());
				mergeOutput.addWord(word1);
				addsInput.moveToNextWordEntry();
				oldInput.moveToNextWordEntry();
			}
		}
		mergeOutput.flushWords();
	}
	/**
	 * Merges the files of the 2 indexes in the new index, according to the changes
	 * recorded during mergeFiles().
	 */
	protected void mergeIncludes() throws IOException {
		int compare;

		while (oldInput.hasMoreIncludes() || addsInput.hasMoreIncludes()) {
			IncludeEntry inc1= oldInput.getCurrentIncludeEntry();
			IncludeEntry inc2= addsInput.getCurrentIncludeEntry();

			if (inc1 == null && inc2 == null)
				break;
			
			if (inc1 == null)
				compare= 1;
			else if (inc2 == null)
				compare= -1;
			else
				compare= Util.compare(inc1.getFile(), inc2.getFile());
			if (compare < 0) {
				inc1.mapRefs(mappingOld);
				mergeOutput.addInclude(inc1);
				oldInput.moveToNextIncludeEntry();
			} else if (compare > 0) {
				inc2.mapRefs(mappingAdds);
				mergeOutput.addInclude(inc2);
				addsInput.moveToNextIncludeEntry();
			} else {
				inc1.mapRefs(mappingOld);
				inc2.mapRefs(mappingAdds);
				inc1.addRefs(inc2.getRefs());
				mergeOutput.addInclude(inc1);
				addsInput.moveToNextIncludeEntry();
				oldInput.moveToNextIncludeEntry();
			}
		}
		mergeOutput.flushIncludes();
	}
	/**
	 * Records the deletion of one file.
	 */
	protected void removeFile(IndexedFileEntry file, int index) {
		if (index == OLD_INDEX)
			mappingOld[file.getFileID()]= -1;
		else
			mappingAdds[file.getFileID()]= -1;
	}
	/**
	 * Returns whether the given file has to be removed from the given index
	 * (ADDS_INDEX or OLD_INDEX). If it has to be removed, the mergeFactory 
	 * deletes it and records the changes. 
	 */

	protected boolean wasRemoved(IndexedFileEntry indexedFile, int index) {
		String path= indexedFile.getPath();
		if (index == OLD_INDEX) {
			if (removedInOld.remove(path) != null) {
				mappingOld[indexedFile.getFileID()]= -1;
				return true;
			}
		} else if (index == ADDS_INDEX) {
			Int lastRemoved= (Int) removedInAdds.get(path);
			if (lastRemoved != null) {
				int fileNum= indexedFile.getFileID();
				if (lastRemoved.value >= fileNum) {
					mappingAdds[fileNum]= -1;
					//if (lastRemoved.value == fileNum) // ONLY if files in sorted order for names AND fileNums
					//removedInAdds.remove(path);
					return true;
				}
			}
		}
		return false;
	}
}

