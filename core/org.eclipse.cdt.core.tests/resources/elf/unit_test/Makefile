#just an example
# gcc simple.c -g -o simple.elf

simple-dwarf4.elf:
	gcc simple.c -g -gdwarf-4 -o simple-dwarf4.elf

# This needs to run on a GCC that fully supports DWARF v5, e.g. Ubuntu 22.04
# e.g. docker run --rm -it -v $PWD:/work -w /work ubuntu:22.04 /bin/bash
# apt-get update && apt-get install -y --no-install-recommends build-essential
simple-dwarf5.elf:
	gcc simple.c -g -gdwarf-5 -o simple-dwarf5.elf
