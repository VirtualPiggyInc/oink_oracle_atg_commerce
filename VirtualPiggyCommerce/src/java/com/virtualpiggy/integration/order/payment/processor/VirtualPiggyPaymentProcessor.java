package com.virtualpiggy.integration.order.payment.processor;

import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentInfo;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentStatus;

public interface VirtualPiggyPaymentProcessor {

	/**
	 * Authorizes the amount for Virtual Piggy Account
	 * 
	 * @param pVPPaymentInfo VirtualPiggyPaymentInfo that contains the authorization data.
	 * @return VirtualPiggyPaymentStatus object containing the result of the authorization.
	 */
	public VirtualPiggyPaymentStatus authorize(VirtualPiggyPaymentInfo pVPPaymentInfo);
	
	/**
	 * Debit the amount from Virtual Piggy Payment Account, if required, in two step capture.
	 * 
	 * @param pVPPaymentInfo VirtualPiggyPaymentInfo object that contains debit data.
	 * @param pVPPaymentStatus VirtualPiggyPaymentStatus object which contains information about the transaction(from authorize call).
	 * @return VirtualPiggyPaymentStatus object containing the response of debit.
	 */
	public VirtualPiggyPaymentStatus debit(VirtualPiggyPaymentInfo pVPPaymentInfo, VirtualPiggyPaymentStatus pVPPaymentStatus);
	
	/**
	 * Credit the amount in Virtual Piggy Payment account after it has been debited.
	 * 
	 * @param pVPPaymentInfo VirtualPiggyPaymentInfo object that contains credit data.
	 * @param pVPPaymentStatus VirtualPiggyPaymentStatus object which contains information about the transaction(from debit call).
	 * @return VirtualPiggyPaymentStatus object containing the response of credit.
	 */
	public VirtualPiggyPaymentStatus credit(VirtualPiggyPaymentInfo pVPPaymentInfo, VirtualPiggyPaymentStatus pVPPaymentStatus);
	
	/**
	 * Credit the amount in Virtual Piggy Payment account outside the context of an order.
	 * 
	 * @param pVPPaymentInfo VirtualPiggyPaymentInfo object that contains credit data.
	 * @param pVPPaymentStatus VirtualPiggyPaymentStatus object which contains information about the transaction(from debit call).
	 * @return VirtualPiggyPaymentStatus object containing the response of credit.
	 */
	public VirtualPiggyPaymentStatus credit(VirtualPiggyPaymentInfo pVPPaymentInfo);

}
