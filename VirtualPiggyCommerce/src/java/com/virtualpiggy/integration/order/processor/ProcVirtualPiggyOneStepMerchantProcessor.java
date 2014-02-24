package com.virtualpiggy.integration.order.processor;

import java.util.HashMap;
import java.util.List;


import com.virtualpiggy.integration.order.payment.VirtualPiggyPayment;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentStatus;
import com.virtualpiggy.integration.purchase.VirtualPiggyPurchaseProcessHelper;
import com.virtualpiggy.integration.services.VirtualPiggyServiceConfiguration;

import atg.commerce.order.InvalidParameterException;
import atg.commerce.order.Order;
import atg.commerce.order.OrderManager;
import atg.commerce.states.PaymentGroupStates;
import atg.commerce.states.StateDefinitions;
import atg.nucleus.GenericService;
import atg.service.pipeline.PipelineProcessor;
import atg.service.pipeline.PipelineResult;

public class ProcVirtualPiggyOneStepMerchantProcessor extends GenericService implements
PipelineProcessor {

	private VirtualPiggyServiceConfiguration mVPConfiguration;

	/** Return value for this processor. **/
	public static final int SUCCESS = 1;

	@Override
	public int[] getRetCodes() {
		int retCodes[] = {SUCCESS};
		return retCodes;
	}

	/**
	 * After successful callback (VP payment approval). this processor updates the debit status on 
	 * the virtual piggy payment group if the merchant is setup
	 * as one step (auth and settle performed in one transaction).
	 */
	@Override
	public int runProcess(Object pParam, PipelineResult pResult) throws Exception {
		if (isLoggingDebug())
			logDebug("ProcVirtualPiggyOneStepMerchantProcessor pipeline processor invoked..." );
		
		HashMap map = (HashMap)pParam;
		Order order = (Order)map.get("Order");
		OrderManager orderMgr = (OrderManager)map.get("OrderManager");
		
		if (order == null || orderMgr == null) {
            throw new InvalidParameterException("ProcVirtualPiggyOneStepMerchantProcessor-> null order or orderMgr parameter");
		}
		
		addDebitStatusIfOneStepMerchant(order);
		
		orderMgr.updateOrder(order);
		
		return SUCCESS;
	}
	
	private void addDebitStatusIfOneStepMerchant(Order pOrder){
		//if one step, add debit status and mark the status of payment group to settled.
		if(getVPConfiguration().isOneStepPaymentMerchant()){
			VirtualPiggyPayment payGrp = VirtualPiggyPurchaseProcessHelper.getVirtualPiggyPaymentGroupFromOrder(pOrder);
			if(payGrp != null){
				addDebitStatusToPaymentGroup(payGrp);
			}
		}
	}
	/**
	 * This method copies the auth status to a new status object and adds it as debit status to the payment group.
	 * Since the details are not different for debit status, values are copied over from payment group's auth status object.
	 * Finally the status of the payment group is set to Settled.
	 * 
	 * @param pPayGroup
	 */
	private void addDebitStatusToPaymentGroup(VirtualPiggyPayment pPayGroup){
		//add debit status and mark the payment group state as Settled.
		VirtualPiggyPaymentStatus vpDebitStatus = new VirtualPiggyPaymentStatus();
		List authStatuses = pPayGroup.getAuthorizationStatus();
		VirtualPiggyPaymentStatus vpAuthStatus = null;
		if(authStatuses != null && authStatuses.size() > 0){
			vpAuthStatus = (VirtualPiggyPaymentStatus)authStatuses.get(authStatuses.size() - 1);
			VirtualPiggyPurchaseProcessHelper.copyStatusValues(vpAuthStatus, vpDebitStatus, vpAuthStatus.getVpTransactionStatus());
		}
		pPayGroup.addDebitStatus(vpDebitStatus);
		pPayGroup.setAmountDebited(pPayGroup.getAmountDebited() + vpDebitStatus.getAmount());
		pPayGroup.setState(StateDefinitions.PAYMENTGROUPSTATES.getStateValue(PaymentGroupStates.SETTLED));
	}
	
	public VirtualPiggyServiceConfiguration getVPConfiguration() {
		return mVPConfiguration;
	}

	public void setVPConfiguration(
			VirtualPiggyServiceConfiguration pVPConfiguration) {
		this.mVPConfiguration = pVPConfiguration;
	}

}
