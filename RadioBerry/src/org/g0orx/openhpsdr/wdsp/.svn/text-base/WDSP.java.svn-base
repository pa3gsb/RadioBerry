package org.g0orx.openhpsdr.wdsp;

import java.io.File;
import org.g0orx.openhpsdr.Log;

public class WDSP {

    public static WDSP getInstance() {
        if (instance == null) {
            Log.i("WDSP","new");
            instance = new WDSP();
        }
        Log.i("WDSP","return instance");
        return instance;
    }

    private WDSP() {
        //Log.i("WDSP","WDSP constructor");
    }

    //public void setActivity(WisdomActivity activity) {
    //    this.activity=activity;
    //}
    
    public void updateStatus(String status) {
        //if (activity != null) {
        //    activity.updateStatus(status);
        //}
    }

    public native void OpenChannel(int channel, int in_size, int dsp_size, int input_samplerate,
            int dsp_rate, int output_samplerate, int type, int state, double tdelayup,
            double tslewup, double tdelaydown, double tslewdown, int bfo);

    public native void CloseChannel(int channel);

    public native void fexchange0(int channel, double[] in, double[] out, int[] error);

    public native void fexchange2(int channel, float[] Iin, float[] Qin, float[] Iout, float[] Qout,
            int[] error);

    public native void SetType(int channel, int type);

    public native void SetInputBuffsize(int channel, int in_size);

    public native void SetDSPBuffsize(int channel, int dsp_size);

    public native void SetInputSamplerate(int channel, int in_rate);

    public native void SetDSPSamplerate(int channel, int dsp_rate);

    public native void SetOutputSamplerate(int channel, int out_rate);

    public native void SetAllRates(int channel, int in_rate, int dsp_rate, int out_rate);

    public native void SetChannelState(int channel, int state, int dmode);

    public native void SetChannelTDelayUp(int channel, double time);

    public native void SetChannelTSlewUp(int channel, double time);

    public native void SetChannelTDelayDown(int channel, double time);

    public native void SetChannelTSlewDown(int channel, double time);

    public native void SetRXAMode(int channel, int mode);

    public static final int LSB = 0;
    public static final int USB = 1;
    public static final int DSB = 2;
    public static final int CWL = 3;
    public static final int CWU = 4;
    public static final int FM = 5;
    public static final int AM = 6;
    public static final int DIGU = 7;
    public static final int SPEC = 8;
    public static final int DIGL = 9;
    public static final int SAM = 10;
    public static final int DRM = 11;

    public native void SetRXAShiftRun(int channel, int run); // INACTIVE/ACTIVE (default ACTIVE)

    public static final int INACTIVE = 0;
    public static final int ACTIVE = 1;

    public native void SetRXAShiftFreq(int channel, double fshift); // default 0.0

    public native void SetRXABandpassRun(int channel, int run);// INACTIVE/ACTIVE (default ACTIVE)

    public native void SetRXABandpassFreqs(int channel, double f_low, double f_high);

        // 'f_low' and 'f_high' are in Hertz and may be either positive or negative, relative to the center frequency.
    // Note that f_low must always be numerically less than f_high.  (default f_low = ‐4150.0; default f_high = ‐
    // 150.0)
    public native void SetRXAAMSQRun(int channel, int run); // default INACTIVE

    public native void SetRXAAMSQThreshold(int channel, double threshold); // dB (expected to be negative)

    public native void SetRXAAMDSBMode(int channel, int sbmode);

        // If sbmode is 0, both sidebands are received; 1 is only LSB; 2 is only USB.  Note that this is NOT a filtering
    // operation; therefore, the received station does not have to be exactly centered on frequency for the
    // sideband selection to function.  This is often useful when QRM exists on one side or the other of an AM
    // QSO.  (default = 0)
    public native void SetRXAAMDFadeLevel(int channel, int levelfade);

        // If levelfade is 0, no action; if 1, the AM carrier is stripped out and replaced with a stable carrier.  This is
    // useful when the carrier is experiencing QSB.  (default = 1)
    public native void SetRXAFMDeviation(int channel, double deviation);

        // Deviation is in Hertz.  Internal defaults exist for PLL settings and de‐emphasis; these have not currently
    // been made externally available ‐‐ they could be.
    public native void SetRXAFMSQRun(int channel, int run);

    public native void SetRXAFMSQThreshold(int channel, double threshold);

    public native void SetRXAEQRun(int channel, int run);
    // 'run' is 0 or 1, to turn the Equalizer OFF and ON, respectively.

    public native void SetRXAEQProfile(int channel, int nfreqs, double[] F, double[] G);
        // 'nfreqs' is the number of frequency/gain pairs to be specified.
    // 'F' is a pointer to an array of frequencies.  NOTE:  The 0th element of this vector is not used.  So, if nfreqs
    // is 5, the elements of F will be 0 through 5.
    // 'G' is a pointer to a corresponding array of gain values.  The 0th element specifies the preamp gain.
    // Values are in dB.

    public native void SetRXAGrphEQ(int channel, int[] rxeq);
        // For convenience, a three band equalizer is provided with *F = {0.0, 150.0, 400.0, 1500.0, 6000.0}.
    // 'rxeq is a pointer to an array containing gain values, in dB, for each of the frequencies.  The 0th element
    // is the preamp gain value.

    public native void SetRXAGrphEQ10(int channel, int[] rxeq);
        // For convenience, a ten band equalizer is provided with *F = {0.0, 32.0, 63.0, 125.0, 250.0, 500.0, 1000.0,
    // 2000.0, 4000.0, 8000.0, 16000.0}.
    // 'rxeq' is a pointer to an array containing gain values, in dB, for each of the frequencies.  The 0th element
    // is the preamp gain value.

    public native void SetRXAANFRun(int channel, int run);

    // 'run' is 0 or 1, to turn the Equalizer OFF and ON, respectively.

    public native void SetRXAANFVals(int channel, int taps, int delay, double gain, double leakage);

    public native void SetRXAANFTaps(int channel, int taps);

    public native void SetRXAANFDelay(int channel, int delay);

    public native void SetRXAANFGain(int channel, double gain);

    public native void SetRXAANFLeakage(int channel, double leakage);

    public native void SetRXAANFPosition(int channel, int position);

    public native void SetRXAANRRun(int channel, int run);

    // 'run' is 0 or 1, to turn the Equalizer OFF and ON, respectively.

    public native void SetRXAANRVals(int channel, int taps, int delay, double gain, double leakage);

    public native void SetRXAANRTaps(int channel, int taps);

    public native void SetRXAANRDelay(int channel, int delay);

    public native void SetRXAANRGain(int channel, double gain);

    public native void SetRXAANRLeakage(int channel, double leakage);

    public native void SetRXAANRPosition(int channel, int position);

    public native void SetRXAAGCMode(int channel, int mode);

    public static final int OFF = 0;
    public static final int LONG = 1; // (hangtime = 2000ms, τ_decay = 2000ms)
    public static final int SLOW = 2; // (hangtime = 1000ms, τ_decay = 500ms)
    public static final int MED = 3; //  (No Hang, τ_decay = 250ms)
    public static final int FAST = 4; //  (No Hang, τ_decay = 50ms)
    public static final int CUSTOM = 5; // Settings

    public native void SetRXAAGCAttack(int channel, int attack);

    // 'attack' is the attack time constant in ms.  (default = 2ms)

    public native void SetRXAAGCDecay(int channel, int decay);

    // 'decay' is the decay time constant in ms.  (default = 250ms)

    public native void SetRXAAGCHang(int channel, int hang);

    // 'hang' is the hang time in ms.  (default = 250ms)

    public native void GetRXAAGCHangLevel(int channel, double[] hangLevel);

    // Get position data for 'Hang' line on the bandscope.

    public native void SetRXAAGCHangLevel(int channel, double hangLevel);

    // 'hangLevel' is position data needed when the 'Hang' line on the bandscope is moved.

    public native void GetRXAAGCHangThreshold(int channel, int[] hangthreshold);

    // Get position data for Hang Threshold slider.

    public native void SetRXAAGCHangThreshold(int channel, int hangthreshold);

    // 'hangthreshold' is position data needed when the Hang Threshold slider is moved.

    public native void GetRXAAGCThresh(int channel, double[] thresh, double size, double rate);

    // Get position data for the 'AGC‐T' line on the bandscope.

    public native void SetRXAAGCThresh(int channel, double thresh, double size, double rate);

    // 'thresh' is position data needed when the 'AGC‐T' line on the bandscope is moved.

    public native void GetRXAAGCTop(int channel, double[] max_agc);

    // Get data for the AGC Maximum Gain Control.

    public native void SetRXAAGCTop(int channel, double max_agc);

    // Set data from the AGC Maximum Gain Control.

    public native void SetRXAAGCSlope(int channel, int slope);

    // Set AGC Slope.

    public native void SetRXAAGCFixed(int channel, double fixed_agc);
    // Set fixed AGC gain

    public native void SetRXACBLRun(int channel, int setit);
    // 'setit' is 0 to turn‐off the carrier block and is '1' to turn it on.

    public native void SetRXAPanelPan(int channel, double pan);

    // 'pan' ranges from 0.0 to 1.0.

    public native void SetRXAPanelBinaural(int channel, int bin);
        // 'bin' is 0 or 1 to turn binaural mode OFF or ON, respectively.

    public native double GetRXAMeter(int channel, int mt);
        // 'mt' specifies which meter's data is being requested.
    // The return value is in dB.

    public static final int S_PK = 0;
    public static final int S_AV = 1;
    public static final int ADC_PK = 2;
    public static final int ADC_AV = 3;
    public static final int AGC_GAIN = 4;
    public static final int AGC_PK = 5;
    public static final int AGC_AV = 6;

    public native void RXAGetaSipF(int channel, float[] out, int size);
        // '*out' is a vector of raw I samples (no Q) which can be used for a scope display.
    // 'size' samples are returned.

    public native void RXAGetaSipF1(int channel, float[] out, int size);

        // Transmitter
    public native void SetTXAMode(int channel, int mode);

    public native void SetTXABandpassRun(int channel, int run);// INACTIVE/ACTIVE (default ACTIVE)

    public native void SetTXABandpassFreqs(int channel, double f_low, double f_high);

        // Analyzer
    public native void XCreateAnalyzer(int disp,
            int[] success, //writes '0' to success if all went well, <0 if mem alloc failed
            int m_size, //maximum fft size to be used
            int m_LO, //maximum number of LO positions per subspan
            int m_stitch, //maximum number of subspans to be concatenated
            String app_data_path
    );

    public native void DestroyAnalyzer(int disp);

    public static final int REAL = 0;
    public static final int COMPLEX = 1;

    public static final int RECTANGULAR = 0;
    public static final int BLACKMAN_HARRIS = 1;
    public static final int HANN = 2;
    public static final int FLAT_TOP = 3;
    public static final int HAMMING = 4;
    public static final int KAISER = 5;

    public static final int PEAK_DETECT = -1;
    public static final int NO_AVERAGING = 0;
    public static final int TIME_WEIGHTED_LINEAR = 1;
    public static final int TIME_WEIGHTED_LOG = 2;
    public static final int WINDOW_LINEAR = 3;
    public static final int WINDOW_LOG = 4;
    public static final int WEIGHTED_LINEAR_LOW_NOISE = 5;
    public static final int WEIGHTED_LOG_LOW_NOISE = 6;

    public native void SetAnalyzer(int disp,
            int n_fft, //number of LO frequencies = number of ffts used in elimination
            int typ, //0 for real input data (I only); 1 for complex input data (I & Q)
            int[] flp, //vector with one elt for each LO frequency, 1 if high-side LO, 0 otherwise
            int sz, //size of the fft, i.e., number of input samples
            int bf_sz, //number of samples transferred for each OpenBuffer()/CloseBuffer()
            int win_type, //integer specifying which window function to use
            double pi, //PiAlpha parameter for Kaiser window
            int ovrlp, //number of samples each fft (other than the first) is to re-use from the previous
            int clp, //number of fft output bins to be clipped from EACH side of each sub-span
            int fscLin, //number of bins to clip from low end of entire span
            int fscHin, //number of bins to clip from high end of entire span
            int n_pix, //number of pixel values to return.  may be either <= or > number of bins
            int n_stch, //number of sub-spans to concatenate to form a complete span
            int av_m, //averaging mode
            int n_av, //number of spans to (moving) average for pixel result
            double av_b, //back multiplier for weighted averaging
            int calset, //identifier of which set of calibration data to use
            double fmin, //frequency at first pixel value
            double fmax, //frequency at last pixel value
            int max_w //max samples to hold in input ring buffers
    );

    public native void Spectrum(int display, int subspan, int LO, float[] i, float[] q);

    public native void Spectrum2(int display, int subspan, int LO, float[] pbuff);

    public native void GetPixels(int disp,
            float[] pix, //if new pixel values avail, copies to pix and sets flag = 1
            int[] flag //else, returns 0 (try again later)
    );

    public native void GetNAPixels(int disp,
            float[] pix, //if new non-averaged pixel values avail, copies to pix and sets flag = 1
            int[] flag //else, returns 0 (try again later)
    );

    public native void WDSPwisdom(String dir);

    // new for r3385
    public native void SetRXAEMNRRun(int channel, int setit);

    public native void SetRXAEMNRgainMethod(int channel, int method);

    public native void SetRXAEMNRnpeMethod(int channel, int method);

    public native void SetRXAEMNRaeRun(int channel, int run);

    public native void SetRXAEMNRPosition(int channel, int position);

    private static WDSP instance = null;

    //private WisdomActivity activity;
    static {
        // must load fftw3 as wdsp is dependent on it
        if(System.getProperty("os.name").startsWith("Windows")) {
            String libraryPath=System.getProperty("user.dir")+File.separator+"lib"+File.separator+"windows";
            Log.i("WDSP","libraryPath: "+libraryPath);
            Log.i("WDSP","load libfftw3-3.dll");
            System.load(libraryPath+File.separator+"libfftw3-3.dll");
            Log.i("WDSP","load wdsp.dll");
            System.load(libraryPath+File.separator+"wdsp.dll");
        } else if(System.getProperty("os.name").startsWith("Linux")) {
            String libraryPath=System.getProperty("user.dir")+File.separator+"lib"+File.separator+"linux";
            Log.i("WDSP","libraryPath: "+libraryPath);
            Log.i("WDSP","load libfftw3.so");
            System.load(libraryPath+File.separator+"libfftw3.so");
            Log.i("WDSP","load libwdsp.so");
            System.load(libraryPath+File.separator+"libwdsp.so");
            Log.i("WDSP","load libwdspj.so");
            System.load(libraryPath+File.separator+"libwdspj.so");
        } else if(System.getProperty("os.name").startsWith("Mac")) {
            String libraryPath=System.getProperty("user.dir")+File.separator+"lib"+File.separator+"mac";
            Log.i("WDSP","libraryPath: "+libraryPath);
            Log.i("WDSP","load libfftw3.3.dylib");
            System.load(libraryPath+File.separator+"libfftw3.3.dylib");
            Log.i("WDSP","load libwdsp.dylib");
            System.load(libraryPath+File.separator+"libwdsp.dylib");
        }
        Log.i("WDSP","shared libraries loaded");
    }
}
