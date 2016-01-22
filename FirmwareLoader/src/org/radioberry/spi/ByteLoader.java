package org.radioberry.spi;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ByteLoader {

	    public static void main(String[] args) throws IOException {
	 
	        try (
	        // Declaring and initializing both the Streams inside try so that there
	        // is no need to close them. You can do this with many other classes but
	        // make sure that they all implements the Closable Interface
	                InputStream inputStream = new FileInputStream("radioberry.rbf");
	                OutputStream outputStream = new FileOutputStream("test.rbf")
	                ) {
	            Integer c;
	             
	            Integer count = 0;
	            //continue reading till the end of the file
	            while ((c = inputStream.read()) != -1) {
	             
	            	count++;
	                //writes to the output Stream
	                outputStream.write(c);
	            }
	            
	            System.out.println(count);
	        }
	    }
	}
	
	
