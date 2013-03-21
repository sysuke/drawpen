package com.funai.drawpen;

public class ReceiveData {
	public int x;
	public int y;
	public int z;
	public boolean push;

	public ReceiveData() {
		dataClear();
	}
	public ReceiveData(int x, int y, int z, boolean push){
		this.x = x;
		this.y = y;
		this.z = z;
		this.push = push;
	}
	public ReceiveData(ReceiveData data){
		this.x = data.x;
		this.y = data.y;
		this.z = data.z;
		this.push = data.push;
	}

	public void dataClear() {
		x = 0;
		y = 0;
		z = 0;
		push = false;
	}
}
