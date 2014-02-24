package com.virtualpiggy.integration.order.payment.processor;

import java.util.List;

import atg.commerce.CommerceException;
import atg.commerce.order.Order;
import atg.commerce.order.OrderManager;
import atg.commerce.payment.PaymentException;
import atg.commerce.payment.PaymentManagerPipelineArgs;
import atg.commerce.payment.processor.ProcProcessPaymentGroup;
import atg.commerce.states.OrderStates;
import atg.payment.PaymentStatus;

import com.virtualpiggy.integration.order.VirtualPiggyOrderConstants;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPayment;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentConstants;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentInfo;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentStatus;
import com.virtualpiggy.integration.services.VirtualPiggyServiceConfiguration;

public class ProcProcessVirtualPiggyPayment extends ProcProcessPaymentGroup {

	private VirtualPiggyPaymentProcessor mVpPaymentProcessor;
	private OrderStates mOrderStates;
	private OrderManager mOrderManager;
	
	private VirtualPiggyServiceConfiguration VPConfiguration;

	private static String MSG_VPIGGY_PAYMENT_TR_NOT_AUTHORIZED = "vpiggyPaymentTrNotAuthorized";
	
	/**
	 * Get VirtualPiggyPaymentInfo object from the pParams parameter and invoke the
	 * VirtualPiggyPaymentProcessor.authorize method.
	 *
	 * @param pParams PaymentManagerPipelineArgs object which contains the VirtualPiggyPaymentInfo object.
	 * @return a PaymentStatus object that will detail the authorize details.
	 * @exception CommerceException if an error occurs
	 */
	@Override
	public PaymentStatus authorizePaymentGroup(PaymentManagerPipelineArgs pParams)
			throws CommerceException {
		VirtualPiggyPaymentInfo vpi = null;
		try{
			vpi = (VirtualPiggyPaymentInfo)pParams.getPaymentInfo();

		}catch(ClassCastException ex){
			if (isLoggingError())
				logError("Expecting class of type VirtualPiggyPaymentInfo but got: " +	pParams.getPaymentInfo().getClass().getName());
			throw ex;	
		}
		PaymentStatus status = getVpPaymentProcessor().authorize(vpi);
		updateOrderState(status, pParams);
		return status;

	}


	/**
	 * This method updates the order's state based on the Virtual Piggy Payment Status response. 
	 * 
	 * @param pStatus - PaymentStatus object which is result of VP Authorize Process.
	 * @param pParams PaymentManagerPipelineArgs object which contains the Order object.
	 */
	private void updateOrderState(PaymentStatus pStatus,
			PaymentManagerPipelineArgs pParams) {
		Order order = pParams.getOrder();
		if (pStatus != null && pStatus.getTransactionSuccess()) {
			if (isLoggingDebug())
				logDebug("authorizePaymentGroup-VirtualPiggy success...setting order state.");
			if (VirtualPiggyPaymentConstants.PAYMENT_TRANSACTION_STATUS_APPROVAL_PENDING
					.equalsIgnoreCase(((VirtualPiggyPaymentStatus) pStatus)
							.getVpTransactionStatus())) {

				if (order != null && getOrderStates() != null) {
					order.setState(getOrderStates().getStateValue(VirtualPiggyOrderConstants.ORDER_STATE_PENDING_PARENT_APPROVAL));
					if (isLoggingDebug())
						logDebug("authorizePaymentGroup-VirtualPiggy success...order state set to:"	+ getOrderStates().getStateValue(VirtualPiggyOrderConstants.ORDER_STATE_PENDING_PARENT_APPROVAL));
					try {
						getOrderManager().updateOrder(order);
					} catch (CommerceException e) {
						if (isLoggingError()) {
							logError("Error Updating Order with Virtual Piggy Order state for order "
									+ order.getId() + ". " + e.getMessage());
							e.printStackTrace();
						}
					}
				}
				else if (VirtualPiggyPaymentConstants.PAYMENT_TRANSACTION_STATUS_PROCESSED
						.equalsIgnoreCase(((VirtualPiggyPaymentStatus) pStatus)
								.getVpTransactionStatus())) {					
					if (order != null && getOrderStates() != null) {
						order.setState(getOrderStates().getStateValue(OrderStates.PENDING_APPROVAL));
						if (isLoggingDebug())
							logDebug("authorizePaymentGroup-VirtualPiggy success...order state set to:" + getOrderStates().getStateValue(VirtualPiggyOrderConstants.ORDER_STATE_PENDING_PARENT_APPROVAL));
						try {
							getOrderManager().updateOrder(order);
						} catch (CommerceException e) {
							if (isLoggingError()) {
								logError("Error Updating Order with Virtual Piggy Order state for order "
										+ order.getId() + ". " + e.getMessage());
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	@Override
	public PaymentStatus creditPaymentGroup(PaymentManagerPipelineArgs arg0)
			throws CommerceException {
		return null;
	}
	
	/**
	 * Get VirtualPiggyPaymentInfo object and VirtualPiggyPayment Object from the pParams parameter and invoke the
	 * VirtualPiggyPaymentProcessor.debit method.
	 *
	 * @param pParams PaymentManagerPipelineArgs object which contains the VirtualPiggyPaymentInfo & VirtualPiggyPayment object.
	 * @return a PaymentStatus object that will detail the debit details.
	 * @exception CommerceException if an error occurs
	 */

	@Override
	public PaymentStatus debitPaymentGroup(PaymentManagerPipelineArgs pParams)
			throws CommerceException {

		VirtualPiggyPayment vpPayment = (VirtualPiggyPayment)pParams.getPaymentGroup();
		VirtualPiggyPaymentInfo vpi = (VirtualPiggyPaymentInfo)pParams.getPaymentInfo();
		//if payment merchant is one step then this method should not be called from fulfillment. Funds are already settled. Return the debit status.
		if(getVPConfiguration().isOneStepPaymentMerchant()){
			//throw new PaymentException("Merchant is configured for one-step payment.");
			List debitStatusList = vpPayment.getDebitStatus();
			if (debitStatusList != null && debitStatusList.size() > 0) {
				return (VirtualPiggyPaymentStatus)debitStatusList.get(debitStatusList.size() - 1);
			}
			throw new PaymentException("Merchant is configured for one-step payment, but no debit status found.");
		}		
		List authStatuses = vpPayment.getAuthorizationStatus();
		//call VP api to inform about settlement.
		VirtualPiggyPaymentStatus debitStatus = getVpPaymentProcessor().debit(vpi, (VirtualPiggyPaymentStatus)authStatuses.get(authStatuses.size() - 1));
		debitStatus.setAmount(vpPayment.getAmount());
		return debitStatus;

	}

	public VirtualPiggyPaymentProcessor getVpPaymentProcessor() {
		return mVpPaymentProcessor;
	}

	public void setVpPaymentProcessor(
			VirtualPiggyPaymentProcessor pVpPaymentProcessor) {
		this.mVpPaymentProcessor = pVpPaymentProcessor;
	}

	public OrderStates getOrderStates() {
		return mOrderStates;
	}

	public void setOrderStates(OrderStates pOrderStates) {
		this.mOrderStates = pOrderStates;
	}

	public OrderManager getOrderManager() {
		return mOrderManager;
	}

	public void setOrderManager(OrderManager pOrderManager) {
		this.mOrderManager = pOrderManager;
	}
	
	public VirtualPiggyServiceConfiguration getVPConfiguration() {
		return VPConfiguration;
	}


	public void setVPConfiguration(VirtualPiggyServiceConfiguration VPConfiguration) {
		this.VPConfiguration = VPConfiguration;
	}

}
