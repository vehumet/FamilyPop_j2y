package cps.mobilemaestro.library;

import android.os.Handler;

import java.io.BufferedWriter;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class MMAnalyzer extends Thread
{
	private short[] output;
	private Handler topHandler;
	private MMMeasureResult result;

	private BufferedWriter or_out = null;
	private BufferedWriter or_code = null;
	private BufferedWriter or_calc = null;

	public MMAnalyzer(Handler topHandler, short[] output)
	{
		this.output = output;		
		this.topHandler = topHandler;
		result = new MMMeasureResult();
	}
	
    @Override 
    public void run() 
    {   	
		System.out.println("Start analyzing.");
		int splitLength = (int)((double)MMCtrlParam.playInterval / 1000.f * (double)MMCtrlParam.recordRate);

		for(int cnt = 0; cnt < MMCtrlParam.numDevice; cnt++)
		{
			result.setPeak(cnt, -1, -1);

			short[] tmpOutput = new short[splitLength];

			for(int cnt2 = 0; cnt2 < splitLength; cnt2++)
				tmpOutput[cnt2] = output[cnt2 + cnt * splitLength];

			MMMeasureResult tmpResult = crossCorrelationByFFT(tmpOutput, cnt);
			result.getPeak(cnt).setValue(tmpResult.getPeak(cnt).getValue());
			result.getPeak(cnt).setIdx(tmpResult.getPeak(cnt).getIdx());
		}

        output = null;    	
        
        MMUtils.sendMsg(topHandler, 3, result);
        
        System.out.println("Finish analyzing.");
    }
    
    private MMMeasureResult crossCorrelationByFFT(short[] output, int analyzeIdx)
    {
    	int fftLength = MMCtrlParam.codeLength + output.length;
    	DoubleFFT_1D fft = new DoubleFFT_1D(fftLength);
    	
    	double[] output_fft = new double[fftLength * 2];
    	double[] code_fft = new double[fftLength * 2];
    	double[] result_fft = new double[fftLength * 2];
    	
    	for(int cnt = 0; cnt < fftLength * 2; cnt++)
    	{
    		if(cnt < output.length)
    			output_fft[cnt] = output[cnt];
    		else
    			output_fft[cnt] = 0;
    	}

		for(int cnt = 0; cnt < fftLength * 2; cnt++)
		{
			if(cnt < MMCtrlParam.codeLength)
				//code_fft[cnt] = MMCtrlParam.code[analyzeIdx][MMCtrlParam.codeLength - 1 - cnt];
				code_fft[cnt] = MMCtrlParam.code[MMCtrlParam.codeLength - 1 - cnt];
			else
				code_fft[cnt] = 0;
		}

    	fft.realForwardFull(output_fft);
    	
    	double a, b, c, d;
		MMMeasureResult result = new MMMeasureResult();

    	fft.realForwardFull(code_fft);
    		
		for(int cnt = 0; cnt < fftLength * 2; cnt += 2)
		{
			a = output_fft[cnt];
			b = output_fft[cnt + 1];
			c = code_fft[cnt];
			d = code_fft[cnt + 1];

			result_fft[cnt] = (a * c) - (b * d);
			result_fft[cnt + 1] = (b * c) + (a * d);
		}
    		
		fft.complexInverse(result_fft, true);
    		
		double max = Double.MIN_VALUE;
		int idx = -1;
    		
		for(int cnt = 0; cnt < fftLength; cnt++)
		{
			if(max < Math.sqrt(result_fft[2 * cnt] * result_fft[2 * cnt] + result_fft[2 * cnt + 1] * result_fft[2 * cnt + 1]))
			{
				max = Math.sqrt(result_fft[2 * cnt] * result_fft[2 * cnt] + result_fft[2 * cnt + 1] * result_fft[2 * cnt + 1]);
				idx = cnt;
			}
		}
    		
		result.setPeak(analyzeIdx, idx, (int)(max / MMCtrlParam.codeLength));
    	
		return result;
    }
}

	