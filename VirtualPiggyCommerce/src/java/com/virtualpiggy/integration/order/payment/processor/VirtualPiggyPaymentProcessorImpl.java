package com.virtualpiggy.integration.order.payment.processor;

import java.util.Date;

import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentInfo;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentStatus;
import com.virtualpiggy.integration.services.VirtualPiggyIntegrationService;
import com.virtualpiggy.integration.services.beans.VirtualPiggyCaptureTransactionResponse;
import com.virtualpiggy.integration.services.beans.VirtualPiggyProcessTransactionRequest;
import com.virtualpiggy.integration.services.beans.VirtualPiggyProcessTransactionResponse;

import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.nucleus.ServiceException;

public class VirtualPiggyPaymentProcessorImpl extends GenericService implements
		VirtualPiggyPaymentProcessor {
	
	private VirtualPiggyIntegrationService mSvc;
	private String[] mSuccessTrStatuses;

	/**
	 * Based on VP payment info, check if the payment is being submitted by a Parent or a child.
	 * 
	 * @param pVPPaymentInfo Payment information
	 * @return true, if Parent. False, if child.
	 */
	private boolean invokeParentTransaction(VirtualPiggyPaymentInfo pVPPaymentInfo){
		if(!StringUtils.isEmpty(pVPPaymentInfo.getVpChildId()) && !StringUtils.isEmpty(pVPPaymentInfo.getVpPaymentAccId())){
			return true;
		}
		return false;
	}
	
	/**
	 * This method submits order payment request to processTransaction/processParentTransaction VP api,
	 * processes VP response and returns the response details as VirtualPiggyPaymentStatus object.
	 * 
	 */
	@Override
	public VirtualPiggyPaymentStatus authorize(
			VirtualPiggyPaymentInfo pVPPaymentInfo) {
		if(isLoggingDebug()){
			logDebug("VirtualPiggyPaymentProcessor.authorize called...");
			logDebug("VirtualPiggyPaymentProcessor.authorize input value::" + pVPPaymentInfo);
		}
		if(pVPPaymentInfo != null){
			VirtualPiggyProcessTransactionResponse response = null;
			VirtualPiggyProcessTransactionRequest requestObj = generateProcessTransactionRequest(pVPPaymentInfo);
			if(invokeParentTransaction(pVPPaymentInfo)){//check if Parent is submitting the order.
				if(isLoggingDebug()){
					logDebug("VirtualPiggyPaymentProcessor.authorize..calling virtual piggy processParentTransaction...");
				}
				//if parent
				response = getService().processParentTransaction(requestObj);
				
			}else{//invoke child transaction
				if(isLoggingDebug()){
					logDebug("VirtualPiggyPaymentProcessor.authorize..calling virtual piggy processTransaction...");
				}
				response = getService().processTransaction(requestObj);
			}
			if(isLoggingDebug()){
				logDebug("VirtualPiggyPaymentProcessor.authorize..virtual piggy authorize response: " + response);
			}
			return convertProcessTransactionResponse(response, pVPPaymentInfo);
		}
		return null;
	}

	/**
	 * This method submits order payment settlement request to Virtual Piggy,
	 * processes VP response and returns the response details as VirtualPiggyPaymentStatus object.
	 * Based on the configuration settings, if merchant is set to capture payments then merchantCaptureTransactionByIdentifier
	 * api is invoked. If not, captureTransactionByIdentifier api is invoked.
	 * 
	 */
	@Override
	public VirtualPiggyPaymentStatus debit(
			VirtualPiggyPaymentInfo pVPPaymentInfo,
			VirtualPiggyPaymentStatus pVPPaymentStatus) {
		VirtualPiggyCaptureTransactionResponse response = null;
		if(getService().getConnectionSvc().getConfiguration().getMerchantCapturesPayment()){
			//if merchant is set to capture payments
			response = getService().merchantCaptureTransactionByIdentifier(pVPPaymentStatus.getTransactionId(), true);
		}else{
			//if virtual piggy is set to capture payments
			response = getService().captureTransactionByIdentifier(pVPPaymentStatus.getTransactionId(), true);
		}
		if(response == null) return null;
		VirtualPiggyPaymentStatus status = new VirtualPiggyPaymentStatus();
		status.setTransactionId(pVPPaymentStatus.getTransactionId());
		status.setTransactionTimestamp(new Date());
		status.setErrorMessage(response.getErrorMessage());
		status.setAmount(pVPPaymentStatus.getAmount());
		status.setVpTransactionStatus(response.getTransactionStatus());
		
		if(response.getStatus() && isTrSuccess(response.getTransactionStatus())){
			status.setTransactionSuccess(true);
		}else{
			status.setTransactionSuccess(false);
		}
		return status;
	}

	@Override
	public VirtualPiggyPaymentStatus credit(
			VirtualPiggyPaymentInfo pVPPaymentInfo,
			VirtualPiggyPaymentStatus pVPPaymentStatus) {
		return null;
	}

	@Override
	public VirtualPiggyPaymentStatus credit(
			VirtualPiggyPaymentInfo pVPPaymentInfo) {
		return null;
	}
	
	/**
	 * Generate Virtual Piggy ProcessTransaction Request from input VirtualPiggyPaymentInfo.
	 * 
	 * @param pPaymentInfo
	 * @return
	 */
	private VirtualPiggyProcessTransactionRequest generateProcessTransactionRequest(VirtualPiggyPaymentInfo pPaymentInfo){
		VirtualPiggyProcessTransactionRequest rqstObj = new VirtualPiggyProcessTransactionRequest();
		rqstObj.setCheckoutData(pPaymentInfo.getCheckoutData());
		rqstObj.setVpToken(pPaymentInfo.getVpToken());
		rqstObj.setVpTransDesc(pPaymentInfo.getVpTransDesc());
		rqstObj.setVpChildId(pPaymentInfo.getVpChildId());
		rqstObj.setVpPaymentAccId(pPaymentInfo.getVpPaymentAccId());
		
		return rqstObj;
	}
	
	/**
	 * Converts Virtual Piggy Transaction Response to VirtualPiggyPaymentStatus object.
	 * Note: Successful Transaction status values are configured in the component and can be changed
	 * if requirements/values change in future.
	 * 
	 * @param pResponse
	 * @param pPaymentInfo
	 * @return
	 */
	private VirtualPiggyPaymentStatus convertProcessTransactionResponse(VirtualPiggyProcessTransactionResponse pResponse, VirtualPiggyPaymentInfo pPaymentInfo){
		if(pResponse == null) return null;
		
		VirtualPiggyPaymentStatus retStatus = new VirtualPiggyPaymentStatus();
		String trStatus = pResponse.getTransactionStatus();
		boolean svcSuccess = pResponse.getBooleanStatus();//call was successful or not
		
		retStatus.setTransactionTimestamp(new Date());
		retStatus.setTransactionId(pResponse.getTransactionIdentifier());
		retStatus.setVpTransactionStatus(trStatus);
		retStatus.setAmount(pPaymentInfo.getAmount());
		
		if(!svcSuccess || !isTrSuccess(trStatus)){
			retStatus.setErrorMessage(pResponse.getErrorMessage());
			retStatus.setTransactionSuccess(false);
			return retStatus;
		}
		//pResponse.getDataXml();Ignoring unless we have to extract return info from the xml.
		retStatus.setTransactionSuccess(true);
		return retStatus;
	}
	
	@Override
	public void doStartService() throws ServiceException {
		super.doStartService();
		if(mSuccessTrStatuses == null)
			throw new ServiceException("Configuration Error: check 'SuccessTrStatuses' property. Values should be configured.");
	}
	
	private boolean isTrSuccess(String pTrStatus){
		if(mSuccessTrStatuses != null){
			for(int i=0; i< mSuccessTrStatuses.length; i++){
				if(mSuccessTrStatuses[i].equalsIgnoreCase(pTrStatus)){
					return true;
				}
			}
		}
		return false;
	}
	
	public void setService(VirtualPiggyIntegrationService pSvc){
		mSvc = pSvc;
	}

	public VirtualPiggyIntegrationService getService(){
		return mSvc;
	}
	
	public String[] getSuccessTrStatuses() {
		return mSuccessTrStatuses;
	}

	public void setSuccessTrStatuses(String[] pSuccessTrStatuses) {
		this.mSuccessTrStatuses = pSuccessTrStatuses;
	}

}
