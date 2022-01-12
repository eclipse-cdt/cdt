; ELF Header
db 0x7F, 'E', 'L', 'F'; Magic start
db 0x02; EI_CLASS: 1: 32 bit, 2: 64 bit
db 0x02; EI_DATA: 1: LE, 2: BE
db 0x01; EI_VERSION: 1: Original ELF
db 0x00; EI_OSABI: 00: System-V, 03: Linux, mostly 0x00 regardless
db 0x00; EI_ABIVERSION: mostly unused therefore 0x00
db 0x00; EI_PAD
dw 0x0000; EI_PAD
dd 0x00000000; EI_PAD
dw 0x0300; e_type: ET_DYN
dw 0x3E00; e_machine x86-64
dd 0x01000000; e_version
dq 0x1001000000000000; e_entry
dq 0x4000000000000000; e_phoff
dq 0x0000000000000000; e_shoff
dd 0x00000000; e_flags
dw 0x4000; e_ehsize
dw 0x3800; e_phentsize
dw 0x0300; e_phnum
dw 0x4000; e_shentsize
dw 0x0000; e_shnum
dw 0x0000; e_shstrndx

; Program Header 1
dd 0x06000000; p_type: PT_PHDR
dd 0x04000000; p_flags, x86-64 only
dq 0x4000000000000000; p_offset
dq 0x4000000000000000; p_vaddr
dq 0x4000000000000000; p_paddr
dq 0xB000000000000000; p_filesz
dq 0xB000000000000000; p_memsz
dq 0x0800000000000000; p_align

; Program Header 2
dd 0x03000000; p_type: PT_INTERP
dd 0x04000000; p_flags, x86-64 only
dq 0xE800000000000000; p_offset
dq 0xE800000000000000; p_vaddr
dq 0xE800000000000000; p_paddr
dq 0x1C00000000000000; p_filesz
dq 0x1C00000000000000; p_memsz
dq 0x0100000000000000; p_align

; Program Header 3
dd 0x01000000; p_type: PT_LOAD
dd 0x04000000; p_flags, x86-64 only
dq 0x0000000000000000; p_offset
dq 0x0000000000000000; p_vaddr
dq 0x0000000000000000; p_paddr
dq 0xF000000000000000; p_filesz
dq 0xF000000000000000; p_memsz
dq 0x0001000000000000; p_align

db '/', 'l', 'i', 'b', '6', '4', '/', 'l'
db 'd', '-', 'l', 'i', 'n', 'u', 'x', '-'
db 'x', '8', '6', '-', '6', '4', '.', 's'
db 'o', '.', '2', 0x00
dd 0x00; pad
dq 0x00; pad