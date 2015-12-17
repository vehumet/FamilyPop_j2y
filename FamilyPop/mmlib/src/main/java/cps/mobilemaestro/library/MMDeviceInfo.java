package cps.mobilemaestro.library;

public class MMDeviceInfo 
{
	private String id = null;
	private String ipAddr = null;
	
	public MMDeviceInfo(String id, String ipAddr)
	{
		this.id = id;
		this.ipAddr = ipAddr;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}
	
	
}

	