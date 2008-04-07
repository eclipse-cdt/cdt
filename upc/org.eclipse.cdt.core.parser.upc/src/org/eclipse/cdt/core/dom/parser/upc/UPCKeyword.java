package org.eclipse.cdt.core.dom.parser.upc;

import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.parser.util.CharArrayMap;

/**
 * Enumeration of keywords that UPC adds to C99.
 * 
 * @author Mike Kucera
 */
public enum UPCKeyword {

	MYTHREAD(TK_MYTHREAD),
	THREADS(TK_THREADS),
	UPC_MAX_BLOCKSIZE(TK_UPC_MAX_BLOCKSIZE),
	relaxed(TK_relaxed),
	shared(TK_shared),
	strict(TK_strict),
	upc_barrier(TK_upc_barrier),
	upc_localsizeof(TK_upc_localsizeof),
	upc_blocksizeof(TK_upc_blocksizeof), 
	upc_elemsizeof(TK_upc_elemsizeof), 
	upc_notify(TK_upc_notify),
	upc_fence(TK_upc_fence), 
	upc_wait(TK_upc_wait),
	upc_forall(TK_upc_forall);
	
	
	private final int tokenKind;
	
	private static List<String> names = new ArrayList<String>();
	private static final CharArrayMap<Integer> tokenMap = new CharArrayMap<Integer>();
	
	UPCKeyword(int tokenKind) {
		this.tokenKind = tokenKind;
	}
	
	static { // cannot refer to static fields from constructor
		for(UPCKeyword keyword : values()) { 
			String name = keyword.name();
			names.add(name);
			tokenMap.put(name.toCharArray(), keyword.tokenKind);
		}
	}
	
	public int getTokenKind() {
		return tokenKind;
	}
	
	public static String[] getUPCOnlyKeywords() {
		return names.toArray(new String[names.size()]);
	}
	
	public static String[] getAllKeywords() {
		List<String> allKeywords = new ArrayList<String>(names);
		allKeywords.addAll(Arrays.asList(C99Language.getDefault().getKeywords()));
		return allKeywords.toArray(new String[allKeywords.size()]);
	}
	
	public static Integer getTokenKind(char[] image) {
		return tokenMap.get(image);
	}
}
