## Adding elf files tests

1- generate an elf file

e.g.
`gcc simple.c -g -o simple-my_arch.elf

where gcc is the appropriate compiler
examples of gcc in linux would be:
- aarch64-linux-gnu-gcc
- power-linux-gnu-gcc
...

2- read certain fields: 

`readelf simple-my_arch.elf -s
or
`readelf simple-my_arch.elf -a


3- update ElfTest.java by adding the new architecture into the method "elfArchitectures"

That is it.