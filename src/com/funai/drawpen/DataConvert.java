package com.funai.drawpen;

//! @brief データ変換処理群
public class DataConvert {
	DataConvertEventHandler hd;
	public DataConvert(DataConvertEventHandler handler)
	{
		hd = handler;
	}

	public ReceiveData oldData = new ReceiveData();
	public ReceiveData DataConvert_DrawDataConvert(ReceiveData data) {
		ReceiveData retData = new ReceiveData();

		// 変化量算出
		retData.x = data.x - oldData.x;
		retData.y = data.y - oldData.y;
		retData.z = data.z - oldData.z;
		// クリック状態
		retData.push = data.push;
		// 状態保存
		oldData = new ReceiveData(data);
		// 変換結果
		return (retData);
	}
}
