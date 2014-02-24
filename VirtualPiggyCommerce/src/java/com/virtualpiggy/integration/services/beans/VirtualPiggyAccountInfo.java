package com.virtualpiggy.integration.services.beans;

import java.io.Serializable;

public class VirtualPiggyAccountInfo implements Serializable{

	private static final long serialVersionUID = 6297399971048686837L;
	
	private String mVpToken;
	private String mVpChildId;
	private String mVpPaymentAccId;
	private String userType;

	public VirtualPiggyAccountInfo(){
		
	}
	
	public void setVpToken(String pToken){
		mVpToken = pToken;
	}
	
	public String getVpToken(){
		return mVpToken;
	}
	
	public void setVpChildId(String pVPChildId){
		mVpChildId = pVPChildId;
	}
	
	public String getVpChildId(){
		return mVpChildId;
	}
	
	public void setVpPaymentAccId(String pVPPaymentAccId){
		mVpPaymentAccId = pVPPaymentAccId;
	}
	
	public String getVpPaymentAccId(){
		return mVpPaymentAccId;
	}
	
    /**
     * Gets the userType value.
     * 
     * @return userType
     */
    public String getUserType() {
        return userType;
    }


    /**
     * Sets the userType value.
     * 
     * @param userType
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

}
