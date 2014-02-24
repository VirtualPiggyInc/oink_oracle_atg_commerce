package com.virtualpiggy.integration.purchase;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import com.virtualpiggy.integration.order.payment.VirtualPiggyPayment;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentConstants;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentInfo;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentStatus;
import com.virtualpiggy.integration.profile.VPProfileConstants;
import com.virtualpiggy.integration.profile.VPProfileTools;
import com.virtualpiggy.integration.profile.VPProfileUtil;
import com.virtualpiggy.integration.services.VirtualPiggyIntegrationService;
import com.virtualpiggy.integration.services.beans.VPAddressResponseBean;
import com.virtualpiggy.integration.services.beans.VirtualPiggyAccountInfo;
import com.virtualpiggy.integration.services.beans.VirtualPiggyPaymentAccountInfoBean;
import com.virtualpiggy.integration.services.beans.VirtualPiggyProcessTransactionRequest;
import com.virtualpiggy.integration.services.beans.VirtualPiggyProcessTransactionResponse;

import atg.commerce.order.CommerceItem;
import atg.commerce.order.Order;
import atg.commerce.order.PaymentGroupImpl;
import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.repository.MutableRepositoryItem;
import atg.repository.RepositoryItem;
import atg.userprofiling.Profile;

public class VirtualPiggyPurchaseProcessHelper extends GenericService {

	private String[] mSuccessTrStatuses;

	private VPProfileTools mvpTools;
	private VirtualPiggyIntegrationService mService;
	
	/**
	 * This method retrieves VP payment group from the order.
	 * 
	 * @param pOrder
	 * @return VP payment group.
	 */
	public static VirtualPiggyPayment getVirtualPiggyPaymentGroupFromOrder(Order pOrder) {
		if (pOrder == null)
			return null;
		List payGroups = pOrder.getPaymentGroups();
		if (payGroups == null || payGroups.isEmpty()) {
			return null;
		}
		for (int i = 0; i < payGroups.size(); i++) {
			PaymentGroupImpl payGrp = (PaymentGroupImpl) payGroups.get(i);
			String payType = (String) payGrp
					.getPropertyValue(VirtualPiggyPaymentConstants.PROPERTY_NAME_PAYMENT_TYPE);
			if (VirtualPiggyPaymentConstants.ITEM_DESC_NAME_VIRTUAL_PIGGY_PAYMENT
							.equalsIgnoreCase(payType)) {
				return (VirtualPiggyPayment)payGrp;
			}
		}
		return null;
	}
	
	/**
	 * This method submits the checkout/payment request to VP system. Different api call is made to VP
	 * system based on the user type (Parent or Child) checking out. But the rresponse processing is
	 * the same and the response is converted to VirtualPiggyPaymentStatus object and returned.
	 * 
	 * @param pVPPaymentInfo
	 * @return
	 */
	public VirtualPiggyPaymentStatus approveVirtualPiggyPayment(VirtualPiggyPaymentInfo pVPPaymentInfo){
		if(pVPPaymentInfo != null){
			VirtualPiggyProcessTransactionResponse response = null;
			VirtualPiggyProcessTransactionRequest requestObj = generateProcessTransactionRequest(pVPPaymentInfo);
			if(invokeParentTransaction(pVPPaymentInfo)){
				if(isLoggingDebug()){
					logDebug("approveVirtualPiggyPayment->calling virtual piggy processParentTransaction...");
				}
				response = getService().processParentTransaction(requestObj);
				
			}else{//invoke child transaction
				if(isLoggingDebug()){
					logDebug("approveVirtualPiggyPayment->calling virtual piggy processTransaction...");
				}
				response = getService().processTransaction(requestObj);
			}
			if(isLoggingDebug()){
				logDebug("approveVirtualPiggyPayment->virtual piggy response: " + response);
			}
			return convertProcessTransactionResponse(response, pVPPaymentInfo);
		}
		return null;
	}
	
	/**
	 * Converts VirtualPiggyProcessTransactionResponse object to VirtualPiggyPaymentStatus object.
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
		
		if(svcSuccess && isTrSuccess(trStatus)){
			retStatus.setTransactionSuccess(true);
		}else{
			retStatus.setErrorMessage(pResponse.getErrorMessage());
			retStatus.setTransactionSuccess(false);
		}
		//pResponse.getDataXml();Ignoring unless we have to extract return info from the xml.
		return retStatus;
	}
	
	/**
	 * Returns true if input status is mapped to one of the list of success transaction status values maintained
	 * in the component. False, otherwise.
	 * 
	 * @param pTrStatus
	 * @return
	 */
	private boolean isTrSuccess(String pTrStatus){
		if(mSuccessTrStatuses != null){
			for(int i=0; i< mSuccessTrStatuses.length; i++){
				if(!StringUtils.isEmpty(mSuccessTrStatuses[i]) && mSuccessTrStatuses[i].equalsIgnoreCase(pTrStatus)){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Generates request object for process transaction api call from input VirtualPiggyPaymentInfo object.
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
	 * Determines if parent transaction is required to be invoked or not.
	 * 
	 * @param pVPPaymentInfo
	 * @return true, if parent transaction should be invoked, false otherwise.
	 */
	private boolean invokeParentTransaction(VirtualPiggyPaymentInfo pVPPaymentInfo){
		if(!StringUtils.isEmpty(pVPPaymentInfo.getVpChildId()) && !StringUtils.isEmpty(pVPPaymentInfo.getVpPaymentAccId())){
			return true;
		}
		return false;
	}
	
//	/**
//	 * This method retrieves child address from VP system and updates the profile with the shipping address.
//	 * Address is retrieved using a different api call based on the userType (Parent or Child) of the authenticated user.
//	 * Shipping address is keyed in with the child-id.
//	 * 
//	 * @param pAccInfo
//	 * @param pProfile
//	 * @return child-id for which the address is retrieved.
//	 */
//	public String getChildShippingAddressAndUpdateProfile(VirtualPiggyAccountInfo pAccInfo, Profile pProfile){
//		if(pAccInfo == null || pProfile == null){
//			if(isLoggingError()){
//				logError("getChildShippingAddressAndUpdateProfile: Null input parameter for VirtualPiggyAccountInfo or Profile.");
//				return null;
//			}
//		}
//		VPAddressResponseBean addressBean = null;
//		String childKeyToShippingAddress = null;
//		if(VPProfileConstants.PROPERTY_VALUE_VP_USER_TYPE_PARENT.equalsIgnoreCase(pAccInfo.getUserType())){
//			addressBean = getService().getParentChildAddress(pAccInfo);
//			childKeyToShippingAddress = pAccInfo.getVpChildId();
//		}else{
//			addressBean = getService().getChildAddress(pAccInfo);
//			childKeyToShippingAddress = pAccInfo.getVpToken();
//		}
//		if(addressBean == null || StringUtils.isEmpty(addressBean.getAddress())){//ignoring status and checking actual address value instead.
//			return null;
//		}
//		MutableRepositoryItem addressItem = getVpTools().addShipAddressToProfile(pProfile, childKeyToShippingAddress, addressBean);
//		if(addressItem == null) return null;
//		
//		return childKeyToShippingAddress;
//		//return getService().getChildAddress(pAccInfo);
//	}
	
	/**
	 * This method retrieves user's address from VP system and updates the profile with the shipping address.
	 * Address is retrieved using a different api call based on the userType (Parent or Child) of the authenticated user.
	 * Shipping address is keyed in with the user-id.
	 * 
	 * @param pAccInfo
	 * @param pProfile
	 * @return userId for which the address is retrieved.
	 */
	public String getShippingAddressAndUpdateProfile(VirtualPiggyAccountInfo pAccInfo, Profile pProfile){
		if(pAccInfo == null || pProfile == null){
			if(isLoggingError()){
				logError("getChildShippingAddressAndUpdateProfile: Null input parameter for VirtualPiggyAccountInfo or Profile.");
				return null;
			}
		}
		VPAddressResponseBean addressBean = null;
		String keyToShippingAddress = null;
		if(VPProfileConstants.PROPERTY_VALUE_VP_USER_TYPE_PARENT.equalsIgnoreCase(pAccInfo.getUserType())){
			if(VPProfileConstants.PROPERTY_TRUE.equalsIgnoreCase(VPProfileUtil.getParentShipAddressSelected(pProfile))) {
				addressBean = getService().getParentAddress(pAccInfo);
				keyToShippingAddress = pAccInfo.getVpToken();
			}else{
				addressBean = getService().getParentChildAddress(pAccInfo);
				keyToShippingAddress = pAccInfo.getVpChildId();
			}
			
		}else{
			addressBean = getService().getChildAddress(pAccInfo);
			keyToShippingAddress = pAccInfo.getVpToken();
		}
		if(isLoggingDebug()) {
			logDebug("Shipping Address of : "+ pAccInfo.getUserType() + ": " + addressBean.getAddress() + addressBean.getCity() + addressBean.getState() );
		}
		if(addressBean == null || StringUtils.isEmpty(addressBean.getAddress())){//ignoring status and checking actual address value instead.
			
			return null;
		}
		
		MutableRepositoryItem userItem = getVpTools().addEmailToProfile(pProfile, keyToShippingAddress, addressBean);
		MutableRepositoryItem addressItem = getVpTools().addShipAddressToProfile(pProfile, keyToShippingAddress, addressBean);
		if(userItem == null) return null;
		if(addressItem == null) return null;
		
		
		return keyToShippingAddress;
		//return getService().getChildAddress(pAccInfo);
	}
	
	
	
	/**
	 * This method gets parent payment accounts from VP system for the parent account and sets the profile
	 * with the list of payment infos returned.
	 * 
	 * @param pAccInfo
	 * @param pProfile
	 * @return List of parent payment accounts.
	 */
	public List<MutableRepositoryItem> getParentPaymentsAndUpdateProfile(VirtualPiggyAccountInfo pAccInfo, Profile pProfile){
		if(pAccInfo == null || pProfile == null){
			if(isLoggingError()){
				logError("getParentPaymentsAndUpdateProfile: Null input parameter for VirtualPiggyAccountInfo or Profile.");
				return null;
			}
		}
		VirtualPiggyPaymentAccountInfoBean[] payBeanArray = getService().getParentPaymentAccounts(pAccInfo);
		if(payBeanArray == null || payBeanArray.length == 0){
			if(isLoggingError()){
				logError("getParentPaymentsAndUpdateProfile: Null response.");
				return null;
			}
		}
		return updateProfileWithPaymentAccounts(payBeanArray, pProfile);
	}

	private List<MutableRepositoryItem> updateProfileWithPaymentAccounts(VirtualPiggyPaymentAccountInfoBean[] pPayBeanArray, Profile pProfile){
		List<MutableRepositoryItem> payList = null;
		for(int i = 0; i< pPayBeanArray.length; i++){
			MutableRepositoryItem accItem = getVpTools().createPaymentAccountItem(pProfile, pPayBeanArray[i]);
			payList = getVpTools().addPaymentAccountItemToProfile(pProfile, accItem);
		}
		return payList;
	}
	
	/**
	 * Converts Order details to VP specific cart xml.
	 * 
	 */
	public String convertOrderToVirtualPiggyXML(Order pOrder){
		StringBuffer sb = new StringBuffer("<?xml version='1.0' encoding='utf-8' ?>");
		sb.append("<cart currency=\"" + pOrder.getPriceInfo().getCurrencyCode() + "\" total=\"" + pOrder.getPriceInfo().getTotal() + "\">");
		sb.append("<cart-shipment total=\"" + pOrder.getPriceInfo().getTotal() + "\" shipment-reference=\""+ pOrder.getId() +"\">");
		sb.append("<shipment-tax total=\"" + pOrder.getPriceInfo().getTax() + "\"/>");
		sb.append("<shipment-cost total=\"" +pOrder.getPriceInfo().getShipping()+ "\"/>");
		//sb.append("<shipment-discount total=\"" +"0\"/>");//optional
		sb.append("<items>");
		
		List commItems = pOrder.getCommerceItems();
		if(commItems != null){
			for(int i=0; i<commItems.size(); i++){
				CommerceItem ci = (CommerceItem)commItems.get(i);
				if(ci != null){
					sb.append("<item total=\"" + ci.getPriceInfo().getAmount() + "\">");
					
					RepositoryItem prod = (RepositoryItem)ci.getAuxiliaryData().getProductRef();
					if(prod != null){
						sb.append("<item-name><![CDATA[" + prod.getPropertyValue("displayName") + "]]></item-name>");
						sb.append("<item-description><![CDATA[" + prod.getPropertyValue("description") + "]]></item-description>");
					}
					sb.append("<item-price><![CDATA[" + ci.getPriceInfo().getListPrice() + "]]></item-price>");
					sb.append("<item-quantity><![CDATA[" + ci.getQuantity() + "]]></item-quantity>");

					sb.append("</item>");
				}
			}
		}
		
		sb.append("</items>");
		sb.append("</cart-shipment>");
		sb.append("</cart>");
		
		if(isLoggingDebug())
			logDebug("Virtual Piggy Cart XML: " + sb.toString());
		//StringEscapeUtils.escapeXml(sb.toString())
		String encStr=sb.toString();
		try {
			encStr = URLEncoder.encode(sb.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return encStr;
//			<cart currency="USD" total="10">
//			<cart-shipment total="0">
//			<shipment-tax total="0"/>
//			<shipment-cost total="0"/>
//			<shipment-discount total="0"/>
//			<items>
//			<item total="10">
//			<item-name><![CDATA[Mock item]]></item-name>
//			<item-description><![CDATA[Mock description]]></item-description>
//			<item-price><![CDATA[10]]></item-price>
//			<item-quantity><![CDATA[1]]></item-quantity>
//			</item>
//			</items>
//			</cart-shipment>
//			</cart>
	}
	
	/**
	 * Copies payment status values from pFromStatus to pToStatus with new status value
	 * identified by pNewStatus.
	 * 
	 * @param pFromStatus
	 * @param pToStatus
	 * @param pNewStatus
	 */
	public static void copyStatusValues(VirtualPiggyPaymentStatus pFromStatus, VirtualPiggyPaymentStatus pToStatus, String pNewStatus){
		pToStatus.setTransactionSuccess(pFromStatus.getTransactionSuccess());
		pToStatus.setAmount(pFromStatus.getAmount());
		pToStatus.setTransactionId(pFromStatus.getTransactionId());
		pToStatus.setErrorMessage(pFromStatus.getErrorMessage());
		pToStatus.setTransactionTimestamp(pFromStatus.getTransactionTimestamp());
		pToStatus.setVpTransactionStatus(pNewStatus);
	}


	public VirtualPiggyIntegrationService getService() {
		return mService;
	}

	public void setService(VirtualPiggyIntegrationService pService) {
		this.mService = pService;
	}

	public VPProfileTools getVpTools() {
		return mvpTools;
	}

	public void setVpTools(VPProfileTools vpTools) {
		this.mvpTools = vpTools;
	}
	
	public String[] getSuccessTrStatuses() {
		return mSuccessTrStatuses;
	}

	public void setSuccessTrStatuses(String[] pSuccessTrStatuses) {
		this.mSuccessTrStatuses = pSuccessTrStatuses;
	}

}
