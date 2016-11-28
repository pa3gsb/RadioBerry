/* 
	Radioberry protocol
	  
	 Working with radioberry.
	  
	 2016 Johan PA3GSB
*/

#define BUFFER_SIZE 1024

void radioberry_protocol_init();
void radioberry_protocol_stop();
void *radioberry_protocol_process_local_mic(unsigned char *buffer,int le);

void setVolume(double volume);
void setRX_Dither(int dither);
void setRX_Random(int random);
void setRX_Attenuation(int attenuation);
void setRX_Frequency(int freq);

