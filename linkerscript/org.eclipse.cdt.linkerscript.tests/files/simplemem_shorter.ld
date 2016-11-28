MEMORY {
	RAM : ORIGIN = 0x0, LENGTH = 0x2000
	ROM : ORIGIN = 0x80000, LENGTH = 0x10000
}

SECTIONS {
	.text : { *(.text) *(.text.*) } > ROM
	.rodata : { *(.rodata) *(.rodata.*) } > ROM
	.data :	{ *(.data) *(.data.*) } > RAM
	.bss : { _bss = .; *(.bss) *(.bss.*) *(COMMON) _ebss = .; _end = .; } > RAM
}
