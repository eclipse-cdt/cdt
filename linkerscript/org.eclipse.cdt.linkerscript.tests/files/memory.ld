MEMORY {
/* lots of different ways to write memories */ 

	/* all kinds of ways to id things */
	plain : ORIGIN = 0, LENGTH = 1 
	s /*single char*/ : ORIGIN = 0, LENGTH = 1 
	UPPERandlowercase : ORIGIN = 0, LENGTH = 1 
	.dots.everywhere. : ORIGIN = 0, LENGTH = 1 
	/slashes/everywhere/ : ORIGIN = 0, LENGTH = 1
	\backslash\everywhere\ : ORIGIN = 0, LENGTH = 1
	$dollars$everywhere$ : ORIGIN = 0, LENGTH = 1
	_underscores_everywhere_ : ORIGIN = 0, LENGTH = 1
	~tildes~everywhere~ : ORIGIN = 0, LENGTH = 1
	numbers0everywhere1except2begin3 : ORIGIN = 0, LENGTH = 1
	dashes-everywhere-except-begin- : ORIGIN = 0, LENGTH = 1
	pluses+everywhere+except+begin+ : ORIGIN = 0, LENGTH = 1
	colons:everywhere:except:begin: : ORIGIN = 0, LENGTH = 1
	lsquare[everywhere[except[begin[ : ORIGIN = 0, LENGTH = 1
	rsquare]everywhere]except]begin] : ORIGIN = 0, LENGTH = 1
	"Quoted to cover every other case, except no escaping of quotes" : ORIGIN = 0, LENGTH = 1
	/* functions are actually keywords, e.g. ALIGN need to be quoted */
	"ALIGN" : ORIGIN = 0, LENGTH = 1
	/* but other keywords, such as o, org, MEMORY, etc don't need quotes */
	o : ORIGIN = 0, LENGTH = 1
	org : ORIGIN = 0, LENGTH = 1
	l : ORIGIN = 0, LENGTH = 1
	len : ORIGIN = 0, LENGTH = 1
	MEMORY : ORIGIN = 0, LENGTH = 1
	
	


	/* all kinds of ways to write numbers, k and M are legal suffixes */
	RAM1_0 : ORIGIN = 0, LENGTH = 131072 
	RAM1_1a : ORIGIN = 0, LENGTH = 128k 
	RAM1_2a : ORIGIN = 0, LENGTH = 128K
	RAM1_1b : ORIGIN = 0, LENGTH = 128m 
	RAM1_2b : ORIGIN = 0, LENGTH = 128M
	RAM1_3 : ORIGIN = 0, LENGTH = 0x128
	RAM1_4 : ORIGIN = 0, LENGTH = $128
	RAM1_5 : ORIGIN = 0, LENGTH = 100o
	RAM1_6 : ORIGIN = 0, LENGTH = 100O
	RAM1_7a : ORIGIN = 0, LENGTH = A1ABx  /* tricky: this looks like identifier, but gnu ld disallows A1ABx as identifier without quotes */
	RAM1_8a : ORIGIN = 0, LENGTH = A1ABX /* tricky: this looks like identifier, but gnu ld disallows A1ABX as identifier without quotes */
	RAM1_7b : ORIGIN = 0, LENGTH = 1ABh
	RAM1_8b : ORIGIN = 0, LENGTH = 1ABH
	RAM1_7c : ORIGIN = 0, LENGTH = 0h
	RAM1_8c : ORIGIN = 0, LENGTH = 0H
	RAM1_9 : ORIGIN = 0, LENGTH = 123d
	RAM1_10 : ORIGIN = 0, LENGTH = 123D
	RAM1_11 : ORIGIN = 0, LENGTH = 1010b
	RAM1_12 : ORIGIN = 0, LENGTH = 1010B
	RAM1_13 : ORIGIN = 0, LENGTH = 0x1010k
	RAM1_14 : ORIGIN = 0, LENGTH = 0x1010m

/*	things which don't generate warning/error in gnu ld, but probably should
	RAM1_b1 : ORIGIN = 0, LENGTH = 012b
	RAM1_b2 : ORIGIN = 0, LENGTH = 01ab
	RAM1_b3 : ORIGIN = 0, LENGTH = 01ao
	RAM1_b4 : ORIGIN = 0, LENGTH = 01ad
*/

	/* origin and length can be written 3 ways each */
	RAM2_0 : ORIGIN = 0x0, LENGTH = 131072 
	RAM2_1 : ORIGIN = 0x0, LENGTH = 131072 
	RAM2_2 : org = 0x0, LENGTH = 131072 
	RAM2_3 : o = 0x0, LENGTH = 131072 
	RAM2_4 : ORIGIN = 0x0, LENGTH = 131072 
	RAM2_5 : ORIGIN = 0x0, len = 131072 
	RAM2_6 : ORIGIN = 0x0, l = 131072 

	/* origin and length can be expressions, including
	   referencing other memories and using functions */
	RAM3 : ORIGIN = 0x0, LENGTH = 131072
	RAM3_0 : ORIGIN = 0x0, LENGTH = (1+2)*3
	RAM3_1 : ORIGIN = 0x0, LENGTH = LENGTH(RAM3) 
	RAM3_2_1 : ORIGIN = 0x0, LENGTH = ALIGN(LENGTH(RAM3)) 
	RAM3_2_2 : ORIGIN = 0x0, LENGTH = ALIGN((1+2)*3, LENGTH(RAM3)) 
	RAM3_3 : ORIGIN = 1+2, LENGTH = 0
	RAM3_4 : ORIGIN = LENGTH(RAM3), LENGTH = 131072
	RAM3_5 : ORIGIN = ALIGN(LENGTH(RAM3)), LENGTH = 131072
	RAM3_6 : ORIGIN = 0x0, LENGTH = 1k+2m+0x3+0x4k+1b 
	RAM3_7 : ORIGIN = 0x0, LENGTH = A1ABx +3 /* the space is required before the + */

	/* names can contain spaces, be quoted */
	"RAM name with space" : ORIGIN = 0x0, LENGTH = 131072 
	"RAM6" : ORIGIN = 0x0, LENGTH = 131072 

	/* memories can be on same line */
	SAMELINE1 : ORIGIN = 0x0, LENGTH = 131072 SAMELINE2 : ORIGIN = 0x0, LENGTH = 131072
	
	RAM_ATTR_1 (rx) : ORIGIN = 0x10000, LENGTH = 131072 
	RAM_ATTR_2 (!rx) : ORIGIN = 0x20000, LENGTH = 131072 
	RAM_ATTR_3 (RX) : ORIGIN = 0x30000, LENGTH = 131072 
	RAM_ATTR_4 (!RX) : ORIGIN = 0x40000, LENGTH = 131072 
	RAM_ATTR_4_1 (!RX!RX) : ORIGIN = 0x40000, LENGTH = 131072 
	X : ORIGIN = 0x50000, LENGTH = 131072 
	RAM_ATTR_5 (R!X) : ORIGIN = 0x50000, LENGTH = 131072 
	RAM_ATTR_6 (rwxail) : ORIGIN = 0x60000, LENGTH = 131072 
	RAM_ATTR_7 (RWXAIL) : ORIGIN = 0x70000, LENGTH = 131072 
	RAM_ATTR_8 (R X) : ORIGIN = 0x30000, LENGTH = 131072 
	RAM_ATTR_9 (R /* */ X) : ORIGIN = 0x30000, LENGTH = 131072 
	RAM_ATTR_10 (R ! X ! R) : ORIGIN = 0x30000, LENGTH = 131072 

	"RAM
	with
	newline and trailing space here -> 
	(can't believe this is really allowed, but it is)" : ORIGIN = 0x0, LENGTH = 131072 
	"Using silly ram name in length expression" : ORIGIN = 0x0, LENGTH = LENGTH("RAM
	with
	newline and trailing space here -> 
	(can't believe this is really allowed, but it is)") 
}

		 
