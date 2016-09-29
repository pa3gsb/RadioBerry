/* 
	Radioberry protocol
	  
	 Working with pihpsdr.
	  
	 2016 Johan PA3GSB
*/

#define BUFFER_SIZE 1024
void radioberry_protocol_stop();
void radioberry_protocol_init(int rx,int pixels);
void *radioberry_protocol_process_local_mic(unsigned char *buffer,int le);
