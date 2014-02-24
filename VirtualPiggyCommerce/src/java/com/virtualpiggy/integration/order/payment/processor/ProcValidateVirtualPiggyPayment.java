package com.virtualpiggy.integration.order.payment.processor;

import com.virtualpiggy.integration.order.VirtualPiggyOrderConstants;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPayment;

import atg.commerce.order.PaymentGroup;
import atg.commerce.order.processor.ValidatePaymentGroupPipelineArgs;
import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.service.pipeline.PipelineProcessor;
import atg.service.pipeline.PipelineResult;

public class ProcValidateVirtualPiggyPayment extends GenericService implements
		PipelineProcessor {

	private static int SUCCESS = 1;
	private static int[] RETURN_CODES = { SUCCESS };
	
	@Override
	public int[] getRetCodes() {
		return RETURN_CODES;
	}

	/**
	 * Perform validation for VirtualPiggyPayment payment group.
	 * 
	 */
	@Override
	public int runProcess(Object pParam, PipelineResult pResult) throws Exception {
		ValidatePaymentGroupPipelineArgs args = (ValidatePaymentGroupPipelineArgs)pParam;
		PaymentGroup pg = args.getPaymentGroup();
		try {
			VirtualPiggyPayment vpp = (VirtualPiggyPayment)pg;
			if(StringUtils.isEmpty(vpp.getVpToken())){
				pResult.addError("vpTokenMissing", VirtualPiggyOrderConstants.getMsgString("vpTokenMissing"));
			}
			if(!StringUtils.isEmpty(vpp.getVpChildId())){//if parent selected child
				if(StringUtils.isEmpty(vpp.getVpPaymentAccId())){
					pResult.addError("vpPaymentAccIdMissing", VirtualPiggyOrderConstants.getMsgString("vpPaymentAccIdMissing"));
				}
			}
		}catch (ClassCastException cce){
			pResult.addError("ClassNotRecognized", "Expected a VirtualPiggyPayment payment group, but got "	+ pg.getClass().getName() + " instead.");
		}
		return SUCCESS;
	}

}
