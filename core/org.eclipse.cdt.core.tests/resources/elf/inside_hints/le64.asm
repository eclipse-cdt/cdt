; ELF Header
db 0x7F, 'E', 'L', 'F'; Magic start
db 0x02; EI_CLASS: 1: 32 bit, 2: 64 bit
db 0x01; EI_DATA: 1: LE, 2: BE
db 0x01; EI_VERSION: 1: Original ELF
db 0x00; EI_OSABI: 00: System-V, 03: Linux, mostly 0x00 regardless
db 0x00; EI_ABIVERSION: mostly unused therefore 0x00
db 0x00; EI_PAD
dw 0x0000; EI_PAD
dd 0x00000000; EI_PAD
dw 0x0003; e_type: ET_DYN
dw 0x003E; e_machine x86-64
dd 0x00000001; e_version
dq 0x0000000000000110; e_entry
dq 0x0000000000000040; e_phoff
dq 0x0000000000000000; e_shoff
dd 0x00000000; e_flags
dw 0x0040; e_ehsize
dw 0x0038; e_phentsize
dw 0x0003; e_phnum
dw 0x0040; e_shentsize
dw 0x0000; e_shnum
dw 0x0000; e_shstrndx

; Program Header 1
dd 0x00000006; p_type: PT_PHDR
dd 0x00000004; p_flags, x86-64 only
dq 0x0000000000000040; p_offset
dq 0x0000000000000040; p_vaddr
dq 0x0000000000000040; p_paddr
dq 0x00000000000000B0; p_filesz
dq 0x00000000000000B0; p_memsz
dq 0x0000000000000008; p_align

; Program Header 2
dd 0x00000003; p_type: PT_INTERP
dd 0x00000004; p_flags, x86-64 only
dq 0x00000000000000E8; p_offset
dq 0x00000000000000E8; p_vaddr
dq 0x00000000000000E8; p_paddr
dq 0x000000000000001C; p_filesz
dq 0x000000000000001C; p_memsz
dq 0x0000000000000001; p_align

; Program Header 3
dd 0x00000001; p_type: PT_LOAD
dd 0x00000004; p_flags, x86-64 only
dq 0x0000000000000000; p_offset
dq 0x0000000000000000; p_vaddr
dq 0x0000000000000000; p_paddr
dq 0x00000000000000F0; p_filesz
dq 0x00000000000000F0; p_memsz
dq 0x0000000000000100; p_align

db '/', 'l', 'i', 'b', '6', '4', '/', 'l'
db 'd', '-', 'l', 'i', 'n', 'u', 'x', '-'
db 'x', '8', '6', '-', '6', '4', '.', 's'
db 'o', '.', '2', 0x00
dd 0x00; pad
dq 0x00; pad