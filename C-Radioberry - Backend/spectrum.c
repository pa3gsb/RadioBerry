/*
	Spectrum....

*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <memory.h>
#include <json/json.h>

#include "spectrum.h"
#include "channel.h"
#include "radioberry-backend.h"

static pthread_t spectrum_thread_id;
//static void start_radioberry_thread();
static void *spectrum_thread(void* arg);

static float *samples;
//int display_width = 800; //1280-> only 800 valid samples...???

static void *spectrum_thread(void* arg) {
	
	while (1) {
		usleep(100000); //10 times a second.... 
		//measure time for handling the json object.. depends also display width.
		//make it dynamic? by calc and time measurements....
		int result;
		GetPixels(CHANNEL_RX,0,samples,&result);
		
		//create json string for transferring to browser.
		json_object * jobj = json_object_new_object();
		json_object *jarray = json_object_new_array();
		int i;
		for (i = 0; i < SPECTRUM_WIDTH; i++) { 
		  json_object *jdouble = json_object_new_double(samples[i]);
		  json_object_array_add(jarray,jdouble);
		}
		json_object_object_add(jobj,"spectrum", jarray);
		broadcast_spectrum_data(json_object_to_json_string(jobj));
		json_object_put(jobj);
	}
}

void initSpectrum() {
	int rc;	
	samples=malloc(SPECTRUM_WIDTH*sizeof(float)*2);
	rc=pthread_create(&spectrum_thread_id,NULL,spectrum_thread,NULL);
	if(rc != 0) {
		fprintf(stderr,"failed to create spectrum thread: rc=%d\n", rc);
		exit(-1);
	}
}