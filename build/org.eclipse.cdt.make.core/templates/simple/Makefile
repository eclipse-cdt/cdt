OBJS = ${projectName}.o

all:	${projectName}

${projectName}:	$(OBJS)
	$(CC) -o $@ $^

%.o:	../../%.cpp
	$(CC) -c $(CFLAGS) $(CPPFLAGS) -o $@ $<

clean:
	rm -fr ${projectName} $(OBJS)
