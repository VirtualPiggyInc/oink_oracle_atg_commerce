package com.virtualpiggy.integration.services.beans;

public class VirtualPiggyProcessTransactionRequest extends VirtualPiggyAccountInfo{

	private static final long serialVersionUID = 4143630006266181313L;
	
	private String mVpTransDesc;
	private String mCheckoutData;

	public VirtualPiggyProcessTransactionRequest(){
		super();
	}
	
	public void setVpTransDesc(String pVPTransDesc){
		mVpTransDesc = pVPTransDesc;
	}
	
	public String getVpTransDesc(){
		return mVpTransDesc;
	}

	public void setCheckoutData(String pCheckoutData){
		mCheckoutData = pCheckoutData;
	}
	
	public String getCheckoutData(){
		return mCheckoutData;
	}

}
