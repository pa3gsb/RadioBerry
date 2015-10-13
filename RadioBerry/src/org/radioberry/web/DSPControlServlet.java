package org.radioberry.web;

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
 * This class handles all dsp control request.
 * 
 * 	controlling the DSP engine; mode, filters, agc mode, agc gain, bandwith
 * 
 * @author PA3GSB
 *
 */
public class DSPControlServlet extends HttpServlet{

	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String requestStr = IOUtils.toString(req.getInputStream());
		
		JSONObject jso;
		try {
			jso = new JSONObject(requestStr);
			int mode = (Integer)jso.get("mode");
			Log.info("Mode ", "" + mode);
			Radio.getInstance().setMode(mode);
			
			int low = (Integer)jso.get("low");
			int high = (Integer)jso.get("high");
			Log.info("Low = ", "" + low + " High= " + high);
			Radio.getInstance().setFilterLowAndHigh(low, high);
			
			int agc = (Integer)jso.get("agc");
			Log.info("AGC Mode ", "" +agc);
			Radio.getInstance().setAGCMode(agc);
			double agcGain = (Integer)(jso.get("agc_gain")) * 1.0;
			Log.info("AGC gain", ((Double)agcGain).toString());
			Radio.getInstance().setAGCgain(agcGain);
			
			double shift = 0.0;
			if (jso.get("shift") instanceof Integer){
				shift = (Integer)(jso.get("shift")) * 1.0;
			} else {
				shift = (Double)(jso.get("shift"));
			}
			
			Log.info("Shift  ", "" +shift);
			Radio.getInstance().setShift(shift);
			
			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	
}
