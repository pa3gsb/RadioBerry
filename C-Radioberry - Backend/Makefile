CC=gcc
LINK=gcc

OPTIONS=-g 

PROGRAM=radioberry-backend

AUDIO_LIBS=-lasound

LIBS=-lrt -lm -lwdsp -lbcm2835 -lpthread  $(AUDIO_LIBS)

COMPILE=$(CC) $(OPTIONS) $(INCLUDES)

SOURCES= \
radio.c \
main.c \
frozen.c \
mongoose.c \
radioberry-backend.c \
audio.c \
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
audio.h

OBJS= \
radio.o \
main.o \
frozen.o \
mongoose.o \
audio.o \
radioberry-backend.o \
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

