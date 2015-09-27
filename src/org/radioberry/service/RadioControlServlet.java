package org.radioberry.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.radioberry.radio.Radio;
import org.radioberry.utility.Log;

/**
 * This class handles all radio control request.
 * 
 * 	controlling the SDR hardware; freq rx, tx, sample rate, drive level
 * 
 * @author PA3GSB
 *
 */
public class RadioControlServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String requestStr = IOUtils.toString(req.getInputStream());
		
		JSONObject jso;
		try {
			jso = new JSONObject(requestStr);
			int freq = (Integer)jso.get("frequency");
			Log.info("Frequency ", "" + freq);
			
			Radio.getInstance().setFrequency(freq);
			
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
	}

	
	
	
	
}
