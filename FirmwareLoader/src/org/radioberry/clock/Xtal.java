package org.radioberry.clock;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Xtal implements Serializable {

	private double xtal;

	public void setXtal(double xtal) {
		this.xtal = xtal;
	}

	public double getXtal() {
		return xtal;
	}

	public void writeSerializedObject() {
		try {
			FileOutputStream fileOut = new FileOutputStream("si570-fxtal.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
			System.out.printf("Fixed Xtal-freq saved in si570.ser file");
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public Xtal readSerializedObject() {
		Xtal xtal = null;
		try {
			FileInputStream fileIn = new FileInputStream("si570-fxtal.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			xtal = (Xtal) in.readObject();
			in.close();
			fileIn.close();
			
			System.out.println("file found and fxtal si570 = " + xtal.xtal);
			
		} catch (IOException i) {
			System.out.println("no si570-fxtal.ser file found");
		} catch (ClassNotFoundException c ) {
			System.out.println("no si570-fxtal.ser file found");
		}
		
		
		
		
		return xtal;
	}

}
