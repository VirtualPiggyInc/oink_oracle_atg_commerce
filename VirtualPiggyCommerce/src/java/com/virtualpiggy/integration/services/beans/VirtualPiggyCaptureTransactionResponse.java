package com.virtualpiggy.integration.services.beans;

import java.io.Serializable;

public class VirtualPiggyCaptureTransactionResponse implements Serializable{

	private static final long serialVersionUID = 9077596350018492324L;
	
	private String mErrorMessage;
	private boolean mStatus;
	private String mTransactionStatus;

	public VirtualPiggyCaptureTransactionResponse(){
		
	}
	
	public void setErrorMessage(String pErrorMessage){
		mErrorMessage = pErrorMessage;
	}
	
	public String getErrorMessage(){
		return mErrorMessage;
	}
	
	public void setStatus(boolean pStatus){
		mStatus = pStatus;
	}
	
	public boolean getStatus(){
		return mStatus;
	}
	
	public void setTransactionStatus(String pTransactionStatus){
		mTransactionStatus = pTransactionStatus;
	}
	
	public String getTransactionStatus(){
		return mTransactionStatus;
	}
	
}
