package cps.mobilemaestro.library;

public class MMMeasureResult
{
	private MMPeakData[] peaks;

	public MMMeasureResult()
	{ 			
		peaks = new MMPeakData[MMCtrlParam.numDevice];
	}
	
	public void setPeak(int cnt, int idx, int value)
	{
		peaks[cnt] = new MMPeakData(idx, value);
	}
	
	public MMPeakData getPeak(int cnt)
	{
		return peaks[cnt];
	}

	public class MMPeakData
	{
		private int idx;
		private int value;

		public MMPeakData(int idx, int value)
		{
			this.idx = idx;
			this.value = value;
		}

		public int getIdx() {
			return idx;
		}

		public void setIdx(int idx) {
			this.idx = idx;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}
	}
}


	