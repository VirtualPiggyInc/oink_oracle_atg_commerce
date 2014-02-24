package com.virtualpiggy.integration.order.processor;

import java.util.HashMap;

import atg.commerce.order.InvalidParameterException;
import atg.commerce.order.Order;
import atg.commerce.states.OrderStates;
import atg.nucleus.GenericService;
import atg.service.pipeline.PipelineProcessor;
import atg.service.pipeline.PipelineResult;

import com.virtualpiggy.integration.order.VirtualPiggyOrderConstants;

public class ProcCheckVirtualPiggyOrderState extends GenericService implements
		PipelineProcessor {

	private OrderStates orderStates;
	
	/** Return value for this processor. **/
	public static final int SUCCESS = 1;
	public static final int SKIP_SEND_TO_FULFILLMENT = 2;
	
	@Override
	public int[] getRetCodes() {
		int retCodes[] = {SUCCESS, SKIP_SEND_TO_FULFILLMENT};
		return retCodes;
	}
	
	public OrderStates getOrderStates() {
		return orderStates;
	}

	public void setOrderStates(OrderStates orderStates) {
		this.orderStates = orderStates;
	}

	
	
	/* 
	 * If order state is pending for approval, then this process will skip sending the fulfillment message.
	 * 
	 */
	@Override
	public int runProcess(Object pParam, PipelineResult pResult) throws Exception {
		if (isLoggingDebug())
			logDebug("ProcCheckVirtualPiggyOrderState pipeline processor invoked..." );
		
		HashMap map = (HashMap)pParam;
		Order order = (Order)map.get("Order");
		if (order == null) {
            throw new InvalidParameterException("ProcCheckVirtualPiggyOrderState-> null order parameter");
		}
		if(order.getState() ==  getOrderStates().getStateValue(VirtualPiggyOrderConstants.ORDER_STATE_PENDING_PARENT_APPROVAL)) {
			return SKIP_SEND_TO_FULFILLMENT;
		}
		else {
			return SUCCESS;
		}
		
	}


}
