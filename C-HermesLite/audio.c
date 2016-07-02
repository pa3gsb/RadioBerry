/* Copyright (C)
* 2016 - John Melton, G0ORX/N6LYT
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*
*	Changed for using by Radioberry Johan PA3GSB
*/

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <pulse/simple.h>
#include <pulse/error.h>

#include "audio.h"

int audio_buffer_size = 1024; // samples (both left and right)

#define AUDIO_BUFFER_SIZE (audio_buffer_size)

static pa_simple *stream;

void audio_init() {

    static const pa_sample_spec spec= {
        .format = PA_SAMPLE_S16RE,
        .rate =  48000,
        .channels = 2
    };

    int error;

	fprintf(stderr,"audio_init audio_buffer_size=%d\n",audio_buffer_size);

    if (!(stream = pa_simple_new(NULL, "radioberry", PA_STREAM_PLAYBACK, NULL, "playback", &spec, NULL, NULL, &error))) {
        fprintf(stderr, __FILE__": pa_simple_new() failed: %s\n", pa_strerror(error));
        _exit(1);
    }
	fprintf(stderr,"audio_init finished\n");
}

void audio_write(unsigned char* buffer,int samples) {
    int error;
	if (pa_simple_write(stream, buffer, (size_t)AUDIO_BUFFER_SIZE, &error) < 0) {
		fprintf(stderr, __FILE__": pa_simple_write() failed: %s\n", pa_strerror(error));
		_exit(1);
	}    
}
