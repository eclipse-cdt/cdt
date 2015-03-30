/*******************************************************************************
 * Copyright (c) 2010-2015 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Wind River Systems - initial API and implementation in FindReplaceDialog.java
 *		Alvaro Sanchez-Leon (Ericsson) - Find / Replace for 16 bits addressable size systems (Bug 462073)
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.memory.search;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Properties;

import org.eclipse.cdt.debug.ui.memory.search.FindReplaceDialog.BigIntegerSearchPhrase;
import org.eclipse.cdt.debug.ui.memory.search.FindReplaceDialog.IMemorySearchQuery;
import org.eclipse.cdt.debug.ui.memory.search.FindReplaceDialog.SearchPhrase;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IRepositionableMemoryRendering;
import org.eclipse.jface.action.IAction;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.swt.widgets.Display;

public class MemorySearch {
	private final IMemoryBlockExtension fMemoryBlock;
	private final IMemoryRenderingSite fMemoryView;
	private final Properties fProperties;
	private final IAction fFindAction;
	private int fWordSize;

	final static int preFetchSize = 20 * 1024;

	public MemorySearch(IMemoryBlockExtension memoryBlock, IMemoryRenderingSite memoryView, Properties properties, IAction findAction) throws DebugException {
		fMemoryBlock = memoryBlock;
		fMemoryView = memoryView;
		fProperties = properties;
		fFindAction = findAction;
		fWordSize = memoryBlock.getAddressableSize();
		if (fWordSize < 1) {
			throw new DebugException(new Status(Status.ERROR, MemorySearchPlugin.getUniqueIdentifier(), "Invalid Argument: Addressable Size"));
		}
	}
	
	class FindReplaceMemoryCache
	{
		BigInteger memoryCacheStartAddress = BigInteger.ZERO;
		MemoryByte memoryCacheData[] = new MemoryByte[0];
	}
	
	public IMemorySearchQuery createSearchQuery(final BigInteger start, final BigInteger end, final SearchPhrase searchPhrase, 
			final boolean searchForward, final byte[] replaceData, final boolean all, final boolean replaceThenFind)
		{		
			final IMemorySearchQuery query = new IMemorySearchQuery() 
			{
		
				private ISearchResult searchResult = null;
				
				public boolean canRerun() {
					return false;
				}

				public boolean canRunInBackground() {
					return true;
				}

				public String getLabel() {
					return Messages.getString("FindReplaceDialog.SearchingMemoryFor") + searchPhrase; //$NON-NLS-1$
				}

				public ISearchResult getSearchResult() {
					if(searchResult == null)
						searchResult = new MemorySearchResult(this, Messages.getString("FindReplaceDialog.SearchingMemoryFor") + searchPhrase);	 //$NON-NLS-1$
					return searchResult;
				}

				public IStatus run(IProgressMonitor monitor)
						throws OperationCanceledException {

					// Resolve the length in octets, make sure it is divisible by the number of words
					// So we can traverse the search by incrementing addresses
					long phraseOctets = searchPhrase.getByteLength();
					phraseOctets = (phraseOctets % fWordSize) == 0 ? phraseOctets : phraseOctets + fWordSize - (phraseOctets % fWordSize);
					
					// TODO: Searching for numerical phrases with lengths not multiple of word size may not succeed
					// the user should be warned.
					assert (phraseOctets % fWordSize) == 0;
					long phraseWordSize = phraseOctets/fWordSize;
					
					// Phrase size in octets
					final BigInteger searchPhraseOctetsLength = BigInteger.valueOf(phraseOctets);
					
					// Phrase size in words
					final BigInteger searchPhraseWordsLength = BigInteger.valueOf(phraseWordSize);

					BigInteger range = end.subtract(start).add(BigInteger.ONE);
					BigInteger currentPosition = searchForward ? start : end.subtract(searchPhraseWordsLength).add(BigInteger.ONE);

					if ( searchPhraseWordsLength.compareTo(range) >= 0 ) {
						return Status.OK_STATUS;
					}
					
					boolean isReplace = replaceData != null;
					
					BigInteger jobs = range;
					BigInteger factor = BigInteger.ONE;
					if(jobs.compareTo(BigInteger.valueOf(0x07FFFFFF)) > 0)
					{
						factor = jobs.divide(BigInteger.valueOf(0x07FFFFFF));
						jobs = jobs.divide(factor);
					}
					
					BigInteger jobCount = BigInteger.ZERO;
					
					BigInteger replaceCount = BigInteger.ZERO;
					
					FindReplaceMemoryCache cache = new FindReplaceMemoryCache();
					
					monitor.beginTask(Messages.getString("FindReplaceDialog.SearchingMemoryFor") + searchPhrase, jobs.intValue()); //$NON-NLS-1$
			
					boolean matched = false;
					// decrement by one to include start and end addresses within the search
					while(((searchForward && currentPosition.compareTo(end.subtract(BigInteger.valueOf(searchPhraseWordsLength.longValue() -1))) < 1) 
						|| (!searchForward && currentPosition.compareTo(start) >= 0)) && !monitor.isCanceled()) 
					{
						try
						{
							MemoryByte bytes[] = getSearchableBytes(start, end, searchForward, currentPosition, searchPhraseOctetsLength.intValue(), cache);
							matched = searchPhrase.isMatch(bytes);
							if(matched)
							{
								if(all && !isReplace)
									((MemorySearchResult) getSearchResult()).addMatch(new MemoryMatch(currentPosition, searchPhraseOctetsLength));
							
								if(isReplace)
								{
									try
									{
										if ((searchPhrase instanceof BigIntegerSearchPhrase) && (bytes.length > 0) && bytes[0].isEndianessKnown() && !bytes[0].isBigEndian())
										{
											// swap the bytes when replacing an integer on little-endian targets
											fMemoryBlock.setValue(currentPosition.subtract(fMemoryBlock.getBigBaseAddress()), swapBytes(replaceData));
										}
										else
										{
											fMemoryBlock.setValue(currentPosition.subtract(fMemoryBlock.getBigBaseAddress()), replaceData);
										}
									}
									catch(DebugException de)
									{
										MemorySearchPlugin.logError(Messages.getString("FindReplaceDialog.MemoryReadFailed"), de); //$NON-NLS-1$
									}

									replaceCount = replaceCount.add(BigInteger.ONE);
								}
								
								if(isReplace && replaceThenFind && replaceCount.compareTo(BigInteger.ONE) == 0)
								{
									isReplace = false;
									matched = false;
								}
								
								if(matched && !all)
								{
									final BigInteger finalCurrentPosition = currentPosition;
									final BigInteger finalStart = start ;
									final BigInteger finalEnd = end;
									Display.getDefault().asyncExec(new Runnable(){

										public void run() {
											IMemoryRenderingContainer containers[] = getMemoryView().getMemoryRenderingContainers();
											for(int i = 0; i < containers.length; i++)
											{
												IMemoryRendering rendering = containers[i].getActiveRendering();
												if(rendering instanceof IRepositionableMemoryRendering)
												{
													try {
														((IRepositionableMemoryRendering) rendering).goToAddress(finalCurrentPosition);
													} catch (DebugException e) {
														MemorySearchPlugin.logError(Messages.getString("FindReplaceDialog.RepositioningMemoryViewFailed"), e); //$NON-NLS-1$
													}
												}
												
												if(rendering != null)
												{
													// Temporary, until platform accepts/adds new interface for setting the selection
													try {
														Method m = rendering.getClass().getMethod("setSelection", new Class[] { BigInteger.class, BigInteger.class } ); //$NON-NLS-1$
														if(m != null)
															m.invoke(rendering, finalCurrentPosition, finalCurrentPosition.add(searchPhraseWordsLength));
													} catch (Exception e) {
														// do nothing
													}
												}
											}
										}
										
									});
									
									fProperties.setProperty(FindReplaceDialog.SEARCH_ENABLE_FIND_NEXT, Boolean.TRUE.toString());
									if ( searchForward ) {
										BigInteger newFinalStart = finalCurrentPosition.add(BigInteger.ONE);
										fProperties.setProperty(FindReplaceDialog.SEARCH_LAST_START, "0x" + newFinalStart.toString(16)); //$NON-NLS-1$
										fProperties.setProperty(FindReplaceDialog.SEARCH_LAST_END, "0x" + finalEnd.toString(16)); //$NON-NLS-1$
									}
									else {
										BigInteger newFinalEnd = finalCurrentPosition.subtract(BigInteger.ONE);
										fProperties.setProperty(FindReplaceDialog.SEARCH_LAST_START, "0x" + finalStart.toString(16)); //$NON-NLS-1$
										fProperties.setProperty(FindReplaceDialog.SEARCH_LAST_END, "0x" + newFinalEnd.toString(16)); //$NON-NLS-1$
									}
									if ( fFindAction != null ) {
										fFindAction.setEnabled(true);
									}
									return Status.OK_STATUS;
								}
							}
							
							matched = false;
							
							if(searchForward)
								currentPosition = currentPosition.add(BigInteger.ONE);
							else
								currentPosition = currentPosition.subtract(BigInteger.ONE);
							
						}
						catch(DebugException e)
						{
							MemorySearchPlugin.logError(Messages.getString("FindReplaceDialog.MemorySearchFailure"), e); //$NON-NLS-1$
							return Status.CANCEL_STATUS;
						}
						
						jobCount = jobCount.add(BigInteger.ONE);
						if(jobCount.compareTo(factor) == 0)
						{
							jobCount = BigInteger.ZERO;
							monitor.worked(1);
						}	
					}
					
					if(monitor.isCanceled())
						return Status.CANCEL_STATUS;
					
					return Status.OK_STATUS;
				}

				public IMemoryRenderingSite getMemoryView() {
					return fMemoryView;
				}
			};
			
			return query;
		}
	
	/**
	 * Function : getSearchableBytes
	 * 
	 * This function returns to the user an array of memory 
	 * @param start Address ( inclusive ) of the beginning byte of the memory region to be searched
	 * @param end Address ( inclusive ) of the ending 
	 * @param forwardSearch direction of the search ( true == searching forward , false = searching backwards
	 * @param address Address ( inclusive ) of the byte set being requested/returned
	 * @param length Number of bytes of data to be returned
	 * @param cache Cached memory byte data ( this routine fetches additional bytes of memory to try and reduce interaction with the debug engine )
	 * @return MemoryByte[] array which contains the requested bytes
	 * @throws DebugException
	 */
	private MemoryByte[] getSearchableBytes(BigInteger start, BigInteger end, boolean forwardSearch, BigInteger address, int length, FindReplaceMemoryCache cache) throws DebugException
	{
		assert (length % fWordSize) == 0;
		int words_length = length / fWordSize;
		
		BigInteger endCacheAddress = cache.memoryCacheStartAddress.add(BigInteger.valueOf(cache.memoryCacheData.length/fWordSize));

		/*
		 * Determine if the requested data is already within the cache.
		 */
		if( ! ( ( address.compareTo(cache.memoryCacheStartAddress) >= 0                  ) &&
				( address.add(BigInteger.valueOf(words_length)).compareTo(endCacheAddress) < 0 )    ) )
		{
			// Data is outside the cache
			BigInteger prefetchSize = BigInteger.valueOf(preFetchSize/fWordSize);
			BigInteger phrase_words_len          = BigInteger.valueOf(words_length);
			BigInteger fetchAddress = address;
			BigInteger wordsToFetch;

			/*
			 *  Determine which way we are searching. Whichever way we are searching we need to make sure
			 *  we capture the minimum requested amount of data in the forward direction.
			 */

			if ( forwardSearch ) {
				/*
				 *  Legend : "#" == minimum requested data , "*" == additional data we want to prefetch/cache
				 *
				 *  This is the best case where everything cleanly fits within the starting/ending ranges
				 *  to be searched.  What we cannot do is to fetch data outside of these ranges. The user 
				 *  has specified them, if they are in error that is OK, but we must respect the boundaries 
				 *  they specified.
				 *
				 *  +-- address
				 *  |
				 *  +--length--+--prefetch--+------------------------------------+
				 *  |##########|************|                                    |
				 *  |##########|************|                                    |
				 *  |##########|************|                                    |
				 *  +----------+------------+------------------------------------+
				 *  |                                                            |
				 *  +-- start                                              end --+
				 *
				 *  This is the worst case scenario. We cannot even get the requested minimum ( no matter
				 *  the desired prefetch ) before we run out of the specified range.
				 *
				 *                                                       +-- address
				 *                                                       |
				 *  +----------------------------------------------------+--length--+--prefetch--+
				 *  |                                                    |##########|************|
				 *  |                                                    |##########|************|
				 *  |                                                    |##########|************|
				 *  +----------------------------------------------------+-------+--+------------+
				 *  |                                                            |
				 *  +-- start                                              end --+
				 *
				 *  See if the desired size ( minimum length + desired prefetch ) fits in to the current range.
				 *  If so there is nothing to adjust.
				 */

				if ( prefetchSize.compareTo(phrase_words_len) >= 0 ) {
					wordsToFetch = prefetchSize;
				}
				else {
					wordsToFetch = phrase_words_len;
				}
				
				if ( address.add( BigInteger.valueOf(wordsToFetch.longValue()) ).compareTo(end) > 0 ) {
					/*
					 *  It does not all fit. Get as much as we can ( end - current ) + 1.
					 */
					wordsToFetch = end.subtract(address).add(BigInteger.ONE);

					/*
					 *  If the amount of data we can get does not even meet the minimum request. In this case
					 *  we have to readjust how much we copy to match what we can actually read. If we do not
					 *  do this then we will run past the actual data fetched and generate an exception.
					 */
					if ( wordsToFetch.compareTo(phrase_words_len) < 0 ) {
						length = wordsToFetch.intValue();
					}
				}

				/*
				 *  The fetch address just starts at the current requested location since we are searching in
				 *  the forward direction and thus prefetching in the forward direction.
				 */
				fetchAddress = address;
			}
			else {

				/*
				 *  Legend : "#" == minimum requested data , "*" == additional data we want to prefetch/cache
				 *
				 *  This is the best case where everything cleanly fits within the starting/ending ranges
				 *  to be searched.  What we cannot do is to fetch data outside of these ranges. The user 
				 *  has specified them, if they are in error that is OK, but we must respect the boundaries 
				 *  they specified.
				 *
				 *               +-- address
				 *               |
				 *  +--prefetch--+--length--+------------------------------------+
				 *  |************|##########|                                    |
				 *  |************|##########|                                    |
				 *  |************|##########|                                    |
				 *  +------------+----------+------------------------------------+
				 *  |                                                            |
				 *  +-- start                                              end --+
				 *
				 *  This is the second worst case scenario. We cannot even get the requested minimum ( no matter
				 *  the desired prefetch ) before we run out of the specified range.
				 *
				 *                                                            +-- address
				 *                                                            |
				 *  +--------------------------------------------+--prefetch--+--length--+
				 *  |                                            |************|##########|
				 *  |                                            |************|##########|
				 *  |                                            |************|##########|
				 *  +--------------------------------------------+------------+--+-------+
				 *  |                                                            |
				 *  +-- start                                              end --+
				 *
				 *  This is the worst case scenario. The minimum length moves us off the end of the high range
				 *  end and the prefetch before this minimum data request ( remember we are fetching backwards
				 *  since we are searching backwards ) runs us off the start of the data.
				 *
				 *                                                                +-- address
				 *                                                                |
				 *  +---+-----------------------------------------------prefetch--+--length--+
				 *  |*************************************************************|##########|
				 *  |*************************************************************|##########|
				 *  |*************************************************************|##########|
				 *  +---+---------------------------------------------------------+--+-------+
				 *      |                                                            |
				 *      +-- start                                              end --+
				 *
				 *  See if the desired size ( minimum length + desired prefetch ) fits in to the current range.
				 *  Without running off the end. 
				 */
				if ( address.add(phrase_words_len).compareTo(end) > 0 ) {
					/*
					 *  We need to reduce the amount we can ask for to what is left. Also make sure to reduce the
					 *  amount to copy, otherwise we will overrun the buffer and generate an exception.
					 */
					phrase_words_len    = end.subtract(address).add(BigInteger.ONE);
					length = phrase_words_len.intValue() * fWordSize;
				}

				/*
				 *  Now determine  if the prefetch is going to run backwards past the "start" of where we are allowed
				 *  to access the memory. We will normalize the prefetch size so it takes in to account the amount of
				 *  data being gathered as part of the length requested portion.  This should insure that  in the end
				 *  we will request the prefetch amount of data unless there is not enough to service this request.
				 */
				if ( phrase_words_len.compareTo(prefetchSize) > 0 ) {
					prefetchSize = BigInteger.ZERO;
				}
				else {
					prefetchSize = prefetchSize.subtract(phrase_words_len);
				}
				
				if ( address.subtract(prefetchSize).compareTo(start) < 0) {
					/*
					 *  Just get what we can from the beginning up to the current required address.
					 */
					prefetchSize = address.subtract(start);
					fetchAddress = start;
				}
				else {
					/*
					 *  It fits so just start reading from the calculated position prior to the requested point.
					 */
					fetchAddress = address.subtract(prefetchSize);
				}

				wordsToFetch = phrase_words_len.add(prefetchSize);
			}

			/*
			 *  OK, we have determined where to start reading the data and how much. Just get the data
			 *  and store it in the cache.
			 */
			MemoryByte bytes[] = fMemoryBlock.getBytesFromAddress(fetchAddress, wordsToFetch.longValue());

			cache.memoryCacheStartAddress = fetchAddress;
			cache.memoryCacheData = bytes;
		}

		/*
		 * Either it was already cached or just has been, either way we have the data so copy what we can
		 * back to the user buffer.
		 */

		MemoryByte[] bytes = new MemoryByte[length];
		int readingIndex = address.subtract(cache.memoryCacheStartAddress).intValue() * fWordSize;
		System.arraycopy(cache.memoryCacheData, readingIndex, bytes, 0, length);
		
		byte[] rawBytes = new byte[bytes.length];
		for (int i=0; i< bytes.length; i++) {
			rawBytes[i] = bytes[i].getValue();
		}

		return bytes;
	}
	
	private byte[] swapBytes(byte[] bytes)
	{
		byte[] processedBytes = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++)
			processedBytes[i] = bytes[bytes.length - i - 1];
		return processedBytes;
	}
	
}
