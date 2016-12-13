CC=gcc
LINK=gcc

OPTIONS=-g 

PROGRAM=radioberry-backend

LIBS=-lrt -lm -lwdsp -lbcm2835 -lpthread  -lasound  -ljson

COMPILE=$(CC) $(OPTIONS)   

SOURCES= \
radio.c \
main.c \
frozen.c \
mongoose.c \
radioberry-backend.c \
audio.c \
spectrum.c \
radioberry.c 


HEADERS= \
agc.h \
channel.h \
mode.h \
radioberry-backend.h \
radio.h \
frozen.h \
mongoose.h \
radioberry.h \
audio.h \
spectrum.h

OBJS= \
radio.o \
main.o \
frozen.o \
mongoose.o \
audio.o \
radioberry-backend.o \
spectrum.o \
radioberry.o 


all: prebuild $(PROGRAM) $(HEADERS) $(SOURCES) 

prebuild:
	rm -f version.o

$(PROGRAM): $(OBJS) 
	$(LINK) -o $(PROGRAM) $(OBJS) $(LIBS)

.c.o:
	$(COMPILE) -c -o $@ $<


clean:
	-rm -f *.o
	-rm -f $(PROGRAM)

