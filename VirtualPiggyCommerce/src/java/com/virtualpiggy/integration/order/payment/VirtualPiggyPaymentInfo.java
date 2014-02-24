package com.virtualpiggy.integration.order.payment;

/**
 * Virtual Piggy Payment Info Class used by Virtual Piggy payment processors.
 * 
 * @author tarun
 *
 */
public class VirtualPiggyPaymentInfo {

	private String mVpToken;
	private String mVpChildId;
	private String mVpTransDesc;
	private String mVpPaymentAccId;
	private String mCheckoutData;
	private double mAmount;
	private String mTransactionId;
	
	/**
	 * Gets Virtual Piggy Transaction id from authorize call.
	 * @return
	 */
	public String getTransactionId() {
		return mTransactionId;
	}

	/**
	 * Sets Virtual Piggy Transaction id from authorize call.
	 * @param pTransactionId
	 */
	public void setTransactionId(String pTransactionId) {
		this.mTransactionId = pTransactionId;
	}

	/**
	 * Gets amount for auth/charge.
	 * @return 
	 */
	public double getAmount() {
		return mAmount;
	}

	/**
	 * Sets amount for auth/charge.
	 * 
	 * @param pAmount
	 */
	public void setAmount(double pAmount) {
		this.mAmount = pAmount;
	}

	public VirtualPiggyPaymentInfo(){
		
	}
	
	/**
	 * Sets Virtual Piggy Token of the child/parent.
	 * @param pToken
	 */
	public void setVpToken(String pToken){
		mVpToken = pToken;
	}
	
	/**
	 * Gets Virtual Piggy Token of the child/parent.
	 * @return token.
	 */
	public String getVpToken(){
		return mVpToken;
	}
	
	/**
	 * Sets the ChildId of the child for which the order is being placed.
	 * 
	 * @param pVPChildId
	 */
	public void setVpChildId(String pVPChildId){
		mVpChildId = pVPChildId;
	}
	
	/**
	 * Gets the ChildId of the child for which the order is being placed.
	 * @return child-id.
	 */
	public String getVpChildId(){
		return mVpChildId;
	}
	
	/**
	 * Sets transaction description to send to VP system.
	 * 
	 * @param pVPTransDesc
	 */
	public void setVpTransDesc(String pVPTransDesc){
		mVpTransDesc = pVPTransDesc;
	}
	
	/**
	 * Gets transaction description.
	 * 
	 * @return transaction description.
	 */
	public String getVpTransDesc(){
		return mVpTransDesc;
	}

	/**
	 * Sets payment account id used for payment.
	 * 
	 * @param pVPPaymentAccId
	 */
	public void setVpPaymentAccId(String pVPPaymentAccId){
		mVpPaymentAccId = pVPPaymentAccId;
	}
	
	/**
	 * Gets payment account id used for payment.
	 * 
	 * @return payment account id.
	 */
	public String getVpPaymentAccId(){
		return mVpPaymentAccId;
	}

	/**
	 * Sets cart xml as checkout data.
	 * @param pCheckoutData
	 */
	public void setCheckoutData(String pCheckoutData){
		mCheckoutData = pCheckoutData;
	}
	
	/**
	 *  Gets cart xml.
	 *  
	 * @return cart xml.
	 */
	public String getCheckoutData(){
		return mCheckoutData;
	}

}
