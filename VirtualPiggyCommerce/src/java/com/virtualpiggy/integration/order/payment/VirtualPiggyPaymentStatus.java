package com.virtualpiggy.integration.order.payment;

import atg.payment.PaymentStatusImpl;

/**
 * Class that represents Virtual Piggy Payment Status details returned by VirtualPiggy system.
 * It extends OOTB PaymentStatusImpl.
 * 
 * @author tarun
 *
 */
public class VirtualPiggyPaymentStatus extends PaymentStatusImpl {

	private static final long serialVersionUID = -1904164790094507646L;
	
	public VirtualPiggyPaymentStatus(){
		super();
	}
	
	private String mVpTransactionStatus;

	/**
	 * Gets Virtual Piggy payment transaction status.
	 * 
	 * @return
	 */
	public String getVpTransactionStatus() {
		return mVpTransactionStatus;
	}

	/**
	 * Sets Virtual Piggy payment transaction status.
	 * @param pVpTransactionStatus
	 */
	public void setVpTransactionStatus(String pVpTransactionStatus) {
		this.mVpTransactionStatus = pVpTransactionStatus;
	}

}
