
package com.virtualpiggy.integration.order.processor;

import java.util.HashMap;

import atg.commerce.fulfillment.OrderFulfiller;
import atg.commerce.fulfillment.PipelineConstants;
import atg.commerce.fulfillment.processor.ProcSettleOrder;
import atg.commerce.order.Order;
import atg.commerce.states.PaymentGroupStates;
import atg.service.pipeline.PipelineResult;

import com.virtualpiggy.integration.order.payment.VirtualPiggyPayment;
import com.virtualpiggy.integration.purchase.VirtualPiggyPurchaseProcessHelper;
import com.virtualpiggy.integration.services.VirtualPiggyServiceConfiguration;

public class ProcVirtualPiggySettleOrder extends ProcSettleOrder {
  
  private final int SUCCESS = 1;
  
  //-----------------------------------------------
  public ProcVirtualPiggySettleOrder() {
  }

  //-----------------------------------------------
  /**
   * Returns the valid return codes
   * 1 - The processor completed
   * @return an integer array of the valid return codes.
   */
  public int[] getRetCodes()
  {
    int[] ret = {SUCCESS};
    return ret;
  } 
  
  private VirtualPiggyServiceConfiguration VPConfiguration;
  
  public VirtualPiggyServiceConfiguration getVPConfiguration() {
		return VPConfiguration;
	}

	public void setVPConfiguration(VirtualPiggyServiceConfiguration vPConfiguration) {
		VPConfiguration = vPConfiguration;
	}


  /**
   * If One Step Merchant and VP Payment Group is already Settled, Skip OOTB Settlement Process.
   */
  public int runProcess(Object pParam, PipelineResult pResult) throws Exception
  {
    HashMap map = (HashMap) pParam;
    OrderFulfiller of = (OrderFulfiller) map.get(PipelineConstants.ORDERFULFILLER);
    Order pOrder = (Order) map.get(PipelineConstants.ORDER);
    PaymentGroupStates pgs = of.getPaymentGroupStates();    
    
    VirtualPiggyPayment payGrp = VirtualPiggyPurchaseProcessHelper.getVirtualPiggyPaymentGroupFromOrder(pOrder);    
    if (getVPConfiguration().isOneStepPaymentMerchant() && payGrp != null && payGrp.getState() == pgs.getStateValue(PaymentGroupStates.SETTLED)) {
		return SUCCESS;
	}
    return super.runProcess(pParam, pResult);
  }

}
    
