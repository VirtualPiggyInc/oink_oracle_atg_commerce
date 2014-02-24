package com.virtualpiggy.integration.services;

import java.rmi.RemoteException;

import com.virtualpiggy.integration.services.beans.VPAddressResponseBean;
import com.virtualpiggy.integration.services.beans.VPAuthenticationResponseBean;
import com.virtualpiggy.integration.services.beans.VPChildrenResponseBean;
import com.virtualpiggy.integration.services.beans.VirtualPiggyAccountInfo;
import com.virtualpiggy.integration.services.beans.VirtualPiggyCaptureTransactionResponse;
import com.virtualpiggy.integration.services.beans.VirtualPiggyPaymentAccountInfoBean;
import com.virtualpiggy.integration.services.beans.VirtualPiggyProcessTransactionRequest;
import com.virtualpiggy.integration.services.beans.VirtualPiggyProcessTransactionResponse;
import com.virtualpiggy.stub.AddressResultObject;
import com.virtualpiggy.stub.EntityResult;
import com.virtualpiggy.stub.ITransactionService;
import com.virtualpiggy.stub.LoginResultObject;
import com.virtualpiggy.stub.PaymentAccountResult;
import com.virtualpiggy.stub.ResultObject;
import com.virtualpiggy.stub.TransactionResultObject;

import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.nucleus.ServiceException;

public class VirtualPiggyIntegrationService extends GenericService {

	private VirtualPiggyServiceConnection mConnSvc;
	private static final String ERR_MSG_VP_SERVICE_GET_TR_SERVICE_STUB = "Error retrieving Transaction Service Reference from Connection Class.";
	private static final String ERR_MSG_VP_SERVICE_AUTHENTICATE_USER = "Error while making virtual piggy user authentication call.";
	private static final String ERR_MSG_VP_SERVICE_AUTHENTICATE_USER_NULL_RESPONSE = "Null response received for virtual piggy authentication call.";
	private static final String ERR_MSG_VP_SERVICE_GET_CHILD_ADDRESS = "Error while making virtual piggy getChildAddress call.";
	private static final String ERR_MSG_VP_SERVICE_GET_CHILD_ADDRESS_NULL_RESPONSE = "Null response received for virtual piggy getChildAddress call.";
	private static final String ERR_MSG_VP_SERVICE_GET_PARENT_CHILD_ADDRESS = "Error while making virtual piggy getParentChildAddress call.";
	private static final String ERR_MSG_VP_SERVICE_GET_PARENT_ADDRESS = "Error while making virtual piggy getParentAddress call.";
	private static final String ERR_MSG_VP_SERVICE_PROCESS_TRANSACTION = "Error while making virtual piggy processTransaction call.";
	private static final String ERR_MSG_VP_SERVICE_PROCESS_TRANSACTION_NULL_RESPONSE = "Null response received for virtual piggy processTransaction call.";
	private static final String ERR_MSG_VP_NULL_REQUEST = "Request object is null for : ";
	private static final String ERR_MSG_VP_SERVICE_GET_ALL_CHILDREN = "Error while making virtual piggy getAllChildren call.";
	private static final String ERR_MSG_VP_SERVICE_GET_ALL_CHILDREN_NULL_RESPONSE = "Null response received for virtual piggy getAllChildren call.";
	private static final String ERR_MSG_VP_SERVICE_GET_PAYMENT_ACCOUNTS = "Error while making virtual piggy getPaymentAccounts call.";
	private static final String ERR_MSG_VP_SERVICE_GET_PAYMENT_ACCOUNTS_NULL_RESPONSE = "Null response received for virtual piggy getPaymentAccounts call.";
	private static final String ERR_MSG_VP_SERVICE_CAPTURE_TRANSACTION = "Error while making virtual piggy capture transaction call.";
	private static final String ERR_MSG_VP_SERVICE_CAPTURE_TRANSACTION_NULL_RESPONSE = "Null response received for virtual piggy capture transaction call.";
	
	/**
	 * This method interfaces with VP web service to authenticate passed in user credentials.
	 * 
	 * @param pUser
	 * @param pPass
	 * @return result of authentication.
	 */
	public VPAuthenticationResponseBean authenticateUser(String pUser, String pPass){
		if(isLoggingDebug()){
			logDebug("Start virtual piggy authenticateUser for user " + pUser);
		}
		ITransactionService trSvc = getConnectionSvc().getVPTransactionServiceClient();
		if(trSvc == null){
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_GET_TR_SERVICE_STUB);
			}
			return null;
		}
		try {
			LoginResultObject resObj = trSvc.authenticateUser(pUser, pPass);
			return converAuthenticationResponse(resObj);
		} catch (RemoteException e) {
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_AUTHENTICATE_USER + "\n");
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * This method interfaces with VP web service to get child address for the passed in token.
	 * 
	 * @param pAccInfo
	 * @return VPAddressResponseBean
	 */
	public VPAddressResponseBean getChildAddress(VirtualPiggyAccountInfo pAccInfo){
		if(isLoggingDebug()){
			logDebug("Start virtual piggy getChildAddress for child  " + pAccInfo.getVpToken());
		}
		ITransactionService trSvc = getConnectionSvc().getVPTransactionServiceClient();
		if(trSvc == null){
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_GET_TR_SERVICE_STUB);
			}
			return null;
		}
		try {
			AddressResultObject resObj = trSvc.getChildAddress(pAccInfo.getVpToken());
			return convertChildAddressResponse(resObj);
		} catch (RemoteException e) {
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_GET_CHILD_ADDRESS + " for child=" + pAccInfo.getVpToken() + "\n");
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * This method interfaces with VP web service to notify VP system that merchant has captured/settled
	 * a payment transaction identified by pTransId.
	 * 
	 * @param pTransId
	 * @param pCapture
	 * @return VirtualPiggyCaptureTransactionResponse
	 */
	public VirtualPiggyCaptureTransactionResponse merchantCaptureTransactionByIdentifier(String pTransId, boolean pCapture){
		if(isLoggingDebug()){
			logDebug("merchantCaptureTransactionByIdentifier.");
		}
		ITransactionService trSvc = getConnectionSvc().getVPTransactionServiceClient();
		if(trSvc == null){
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_GET_TR_SERVICE_STUB);
			}
			return null;
		}
		try {
			ResultObject resObj = trSvc.merchantCaptureTransactionByIdentifier(pTransId, pCapture, null, null);
			return convertCaptureTransactionResponse(resObj);
		} catch (RemoteException e) {
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_CAPTURE_TRANSACTION + " for transactionid=" + pTransId + "\n");
				e.printStackTrace();
			}
		}
		return null;
		
	}
	
	/**
	 * This method interfaces with VP web service to notify VP system that VP has to capture/settle
	 * a payment transaction identified by pTransId.
	 *
	 * @param pTransId
	 * @param pCapture
	 * @return
	 */
	public VirtualPiggyCaptureTransactionResponse captureTransactionByIdentifier(String pTransId, boolean pCapture){
		if(isLoggingDebug()){
			logDebug("captureTransactionByIdentifier.");
		}
		ITransactionService trSvc = getConnectionSvc().getVPTransactionServiceClient();
		if(trSvc == null){
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_GET_TR_SERVICE_STUB);
			}
			return null;
		}
		try {
			ResultObject resObj = trSvc.captureTransactionByIdentifier(pTransId, pCapture);
			return convertCaptureTransactionResponse(resObj);
		} catch (RemoteException e) {
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_CAPTURE_TRANSACTION + " for transactionid=" + pTransId + "\n");
				e.printStackTrace();
			}
		}
		return null;
		
	}

	private VirtualPiggyCaptureTransactionResponse convertCaptureTransactionResponse(ResultObject pResObj){
		if (pResObj == null) {
			if (isLoggingError()) {
				logError(ERR_MSG_VP_SERVICE_CAPTURE_TRANSACTION_NULL_RESPONSE);
			}
			return null;
		}
		VirtualPiggyCaptureTransactionResponse retBean = new VirtualPiggyCaptureTransactionResponse();
		retBean.setStatus(pResObj.getStatus());
		retBean.setTransactionStatus(pResObj.getTransactionStatus());
		retBean.setErrorMessage(pResObj.getErrorMessage());
		return retBean;
	}
	
	/**
	 * This method interfaces with VP web service to retrieve parent payment accounts
	 * for the passed in user token.
	 * 
	 * @param pAccInfo
	 * @return VirtualPiggyPaymentAccountInfoBean[]
	 */
	public VirtualPiggyPaymentAccountInfoBean[] getParentPaymentAccounts(VirtualPiggyAccountInfo pAccInfo){
		if(isLoggingDebug()){
			logDebug("Start virtual piggy getParentPaymentAccounts for parent  " + pAccInfo.getVpToken());
		}
		ITransactionService trSvc = getConnectionSvc().getVPTransactionServiceClient();
		if(trSvc == null){
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_GET_TR_SERVICE_STUB);
			}
			return null;
		}
		try {
			PaymentAccountResult[] resObj = trSvc.getPaymentAccounts(pAccInfo.getVpToken());
			return convertParentPaymentAccountsResponse(resObj);
		} catch (RemoteException e) {
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_GET_PAYMENT_ACCOUNTS + " for parent =" + pAccInfo.getVpToken() + "\n");
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private VirtualPiggyPaymentAccountInfoBean[] convertParentPaymentAccountsResponse(
			PaymentAccountResult[] pResObj) {
		if (pResObj == null || pResObj.length == 0) {
			if (isLoggingError()) {
				logError(ERR_MSG_VP_SERVICE_GET_PAYMENT_ACCOUNTS_NULL_RESPONSE);
			}
			return null;
		}
		VirtualPiggyPaymentAccountInfoBean[] retBean = new VirtualPiggyPaymentAccountInfoBean[pResObj.length];
		for (int i = 0; i < pResObj.length; i++) {
			VirtualPiggyPaymentAccountInfoBean accInfoBean = new VirtualPiggyPaymentAccountInfoBean(
					pResObj[i].getIdentifier(), pResObj[i].getName(),pResObj[i].getType(),pResObj[i].getUrl());

			retBean[i] = accInfoBean;
		}
		return retBean;
	}
	
	/**
	 * This method interfaces with VP web service to submit processTransaction request for a child.
	 * 
	 * @param pTransRqst
	 * @return VirtualPiggyProcessTransactionResponse
	 */
	public VirtualPiggyProcessTransactionResponse processTransaction(VirtualPiggyProcessTransactionRequest pTransRqst){
		if(isLoggingDebug()){
			logDebug("Start virtual piggy processTransaction.");
		}
		if(pTransRqst == null){
			if(isLoggingError()){
				logError(ERR_MSG_VP_NULL_REQUEST + "VirtualPiggyProcessTransactionRequest");
			}
			return null;
		}
		ITransactionService trSvc = getConnectionSvc().getVPTransactionServiceClient();
		if(trSvc == null){
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_GET_TR_SERVICE_STUB);
			}
			return null;
		}
		try {
			TransactionResultObject resObj = trSvc.processTransaction(pTransRqst.getCheckoutData(), pTransRqst.getVpToken(), pTransRqst.getVpTransDesc());
			return convertProcessTransactionResponse(resObj);
		} catch (RemoteException e) {
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_PROCESS_TRANSACTION + " for child token =" + pTransRqst.getVpToken() + "\n");
				e.printStackTrace();
			}
		}
		return null;
		
	}
	
	/**
	 * This method interfaces with VP web service to submit processParentTransaction request for a parent.
	 * @param pTransRqst
	 * @return
	 */
	public VirtualPiggyProcessTransactionResponse processParentTransaction(VirtualPiggyProcessTransactionRequest pTransRqst){
		if(isLoggingDebug()){
			logDebug("Start virtual piggy processParentTransaction.");
		}
		if(pTransRqst == null){
			if(isLoggingError()){
				logError(ERR_MSG_VP_NULL_REQUEST + ": VirtualPiggyProcessTransactionRequest");
			}
			return null;
		}
		ITransactionService trSvc = getConnectionSvc().getVPTransactionServiceClient();
		if(trSvc == null){
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_GET_TR_SERVICE_STUB);
			}
			return null;
		}
		try {
			TransactionResultObject resObj = trSvc.processParentTransaction(
					pTransRqst.getVpToken(), pTransRqst.getCheckoutData(),
					pTransRqst.getVpTransDesc(), pTransRqst.getVpChildId(), pTransRqst.getVpPaymentAccId());
			return convertProcessTransactionResponse(resObj);
		} catch (RemoteException e) {
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_PROCESS_TRANSACTION + " for child token =" + pTransRqst.getVpToken() + "\n");
				e.printStackTrace();
			}
		}
		return null;
		
	}


	/**
	 * Gets all the children for a parent. Assumption is that the token populated in the input bean represents a parent and that the
	 * validation has been performed by the calling class.
	 * 
	 * @param pVPAccInfo VirtualPiggyAccountInfo object with token field set.
	 * 
	 * @return Array of VPChildrenResponseBean
	 */
	public VPChildrenResponseBean[] getAllChildrenForAParent(VirtualPiggyAccountInfo pVPAccInfo){
		if(isLoggingDebug()){
			logDebug("Start virtual piggy processTransaction.");
		}
		if(pVPAccInfo == null || StringUtils.isEmpty(pVPAccInfo.getVpToken())){
			if(isLoggingError()){
				logError(ERR_MSG_VP_NULL_REQUEST + " for getAllChildrenForAParent call");
			}
			return null;
		}
		ITransactionService trSvc = getConnectionSvc().getVPTransactionServiceClient();
		if(trSvc == null){
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_GET_TR_SERVICE_STUB);
			}
			return null;
		}
		try {
			EntityResult[] resObj = trSvc.getAllChildren(pVPAccInfo.getVpToken());
			return convertGetAllChildrenResponse(resObj);
		} catch (RemoteException e) {
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_GET_ALL_CHILDREN + " for token =" + pVPAccInfo.getVpToken() + "\n");
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Gets address of a child from Virtual Piggy web service, when parent is logged in. 
	 * 
	 * @param pVPAccInfo VirtualPiggyAccountInfo object having 'token' and 'childID' fields populated 
	 * 
	 * @return VPAddressResponseBean object representing child address.
	 */
	public VPAddressResponseBean getParentChildAddress(VirtualPiggyAccountInfo pVPAccInfo){
		if(isLoggingDebug()){
			logDebug("Start virtual piggy getParentChildAddress...");
		}
		ITransactionService trSvc = getConnectionSvc().getVPTransactionServiceClient();
		if(trSvc == null){
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_GET_TR_SERVICE_STUB);
			}
			return null;
		}
		try {
			AddressResultObject resObj = trSvc.getParentChildAddress(pVPAccInfo.getVpToken(), pVPAccInfo.getVpChildId());
			if(isLoggingDebug()){
				logDebug("getParentChildAddress: response received = " + resObj);
			}
			return convertChildAddressResponse(resObj);
		} catch (RemoteException e) {
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_GET_PARENT_CHILD_ADDRESS + " for parent =" + pVPAccInfo.getVpToken() + " and child-id = " + pVPAccInfo.getVpChildId());
				e.printStackTrace();
			}
		}
		return null;
		
	}
	
	/**
	 * Gets address of a parent from Virtual Piggy web service, when parent is logged in. 
	 * 
	 * @param pVPAccInfo VirtualPiggyAccountInfo object having 'token'
	 * 
	 * @return VPAddressResponseBean object representing parent address.
	 */
	public VPAddressResponseBean getParentAddress(VirtualPiggyAccountInfo pVPAccInfo){
		if(isLoggingDebug()){
			logDebug("Start virtual piggy getParentAddress...");
		}
		ITransactionService trSvc = getConnectionSvc().getVPTransactionServiceClient();
		if(trSvc == null){
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_GET_TR_SERVICE_STUB);
			}
			return null;
		}
		try {
			AddressResultObject resObj = trSvc.getParentAddress(pVPAccInfo.getVpToken());
			if(isLoggingDebug()){
				logDebug("getParentAddress: response received = " + resObj);
			}
			return convertChildAddressResponse(resObj);
		} catch (RemoteException e) {
			if(isLoggingError()){
				logError(ERR_MSG_VP_SERVICE_GET_PARENT_ADDRESS + " for parent =" + pVPAccInfo.getVpToken());
				e.printStackTrace();
			}
		}
		return null;
		
	}
	
	private VPChildrenResponseBean[] convertGetAllChildrenResponse(EntityResult[] respObjArray){
		if(respObjArray == null || respObjArray.length == 0){
			if (isLoggingError()) {
				logError(ERR_MSG_VP_SERVICE_GET_ALL_CHILDREN_NULL_RESPONSE);
			}
			return null;
		}
		VPChildrenResponseBean[] retBean = new VPChildrenResponseBean[respObjArray.length];
		for(int i=0; i<respObjArray.length; i++){
			VPChildrenResponseBean childBean = new VPChildrenResponseBean(respObjArray[i].getIdentifier(), respObjArray[i].getName(), respObjArray[i].getType());
			retBean[i] = childBean;
		}
		return retBean;
	}

	private VPAuthenticationResponseBean converAuthenticationResponse(
			LoginResultObject pResObj) {
		if (pResObj == null) {
			if (isLoggingError()) {
				logError(ERR_MSG_VP_SERVICE_AUTHENTICATE_USER_NULL_RESPONSE);
			}
			return null;
		}
		VPAuthenticationResponseBean bean = new VPAuthenticationResponseBean(
				pResObj.getErrorMessage(), pResObj.getStatus(),
				pResObj.getToken(), pResObj.getUserType());
		return bean;
	}

	private VPAddressResponseBean convertChildAddressResponse(
			AddressResultObject pResObj) {
		if (pResObj == null) {
			if (isLoggingError()) {
				logError(ERR_MSG_VP_SERVICE_GET_CHILD_ADDRESS_NULL_RESPONSE);
			}
			return null;
		}
		VPAddressResponseBean bean = new VPAddressResponseBean(
				pResObj.getAddress(), pResObj.getAttentionOf(),
				pResObj.getCity(), pResObj.getCountry(),
				pResObj.getErrorMessage(), pResObj.getName(),
				pResObj.getFirstName(),pResObj.getLastName(),
				pResObj.getParentEmail(), pResObj.getParentPhone(),
				pResObj.getState(), pResObj.getStatus(), pResObj.getZip());
		
		return bean;
	}
	
	private VirtualPiggyProcessTransactionResponse convertProcessTransactionResponse(TransactionResultObject pTransResponse){
		if(pTransResponse == null){
			if (isLoggingError()) {
				logError(ERR_MSG_VP_SERVICE_PROCESS_TRANSACTION_NULL_RESPONSE);
			}
			return null;
		}
		VirtualPiggyProcessTransactionResponse retResponse = new VirtualPiggyProcessTransactionResponse();
		retResponse.setDataXml(pTransResponse.getDataXml());
		retResponse.setErrorMessage(pTransResponse.getErrorMessage());
		retResponse.setBooleanStatus(pTransResponse.getStatus());
		retResponse.setTransactionIdentifier(pTransResponse.getTransactionIdentifier());
		retResponse.setTransactionStatus(pTransResponse.getTransactionStatus());
		
		return retResponse;
	}
	
	public VirtualPiggyServiceConnection getConnectionSvc(){
		return mConnSvc;
	}
	
	public void setConnectionSvc(VirtualPiggyServiceConnection pConnSvc){
		mConnSvc = pConnSvc;
	}
	
	@Override
	public void doStartService() throws ServiceException {
		super.doStartService();
		if(mConnSvc == null){
			throw new ServiceException("VirtualPiggyServiceConnection component reference is not configured.");
		}
	}
}
