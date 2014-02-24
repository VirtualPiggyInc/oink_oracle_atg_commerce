package com.virtualpiggy.integration.order.payment;

import atg.commerce.order.PaymentGroupImpl;

/**
 * This class represents Virtual Piggy Payment Group.
 * 
 * @author tarun
 *
 */
public class VirtualPiggyPayment extends PaymentGroupImpl {
	
	private static final long serialVersionUID = 4427155494873142646L;
	
	public VirtualPiggyPayment(){
		super();
	}
	
	/**
	 * Sets Virtual Piggy Token of the child/parent.
	 * @param pToken
	 */
	public void setVpToken(String pToken){
		setPropertyValue(VirtualPiggyPaymentConstants.PROPERTY_NAME_VP_TOKEN, pToken);
	}

	/**
	 * Gets Virtual Piggy Token of the child/parent.
	 * @return token.
	 */
	public String getVpToken(){
		return (String)getPropertyValue(VirtualPiggyPaymentConstants.PROPERTY_NAME_VP_TOKEN);
	}
	
	/**
	 * Sets the ChildId of the child for which the order is being placed.
	 * 
	 * @param pVPChildId
	 */
	public void setVpChildId(String pVPChildId){
		setPropertyValue(VirtualPiggyPaymentConstants.PROPERTY_NAME_VP_CHILD_ID, pVPChildId);
	}
	
	/**
	 * Gets the ChildId of the child for which the order is being placed.
	 * @return child-id.
	 */
	public String getVpChildId(){
		return (String)getPropertyValue(VirtualPiggyPaymentConstants.PROPERTY_NAME_VP_CHILD_ID);
	}
	
	/**
	 * Sets transaction description to send to VP system.
	 * 
	 * @param pVPTransDesc
	 */
	public void setVpTransDesc(String pVPTransDesc){
		setPropertyValue(VirtualPiggyPaymentConstants.PROPERTY_NAME_TRANS_DESC, pVPTransDesc);
	}
	
	/**
	 * Gets transaction description.
	 * 
	 * @return transaction description.
	 */
	public String getVpTransDesc(){
		return (String)getPropertyValue(VirtualPiggyPaymentConstants.PROPERTY_NAME_TRANS_DESC);
	}

	/**
	 * Sets payment account id used for payment.
	 * 
	 * @param pVPPaymentAccId
	 */
	public void setVpPaymentAccId(String pVPPaymentAccId){
		setPropertyValue(VirtualPiggyPaymentConstants.PROPERTY_NAME_PAYMENT_ACCOUNT_ID, pVPPaymentAccId);
	}
	
	/**
	 * Gets payment account id used for payment.
	 * 
	 * @return payment account id.
	 */
	public String getVpPaymentAccId(){
		return (String)getPropertyValue(VirtualPiggyPaymentConstants.PROPERTY_NAME_PAYMENT_ACCOUNT_ID);
	}

}
