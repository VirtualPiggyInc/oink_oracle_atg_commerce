package com.virtualpiggy.integration.purchase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import com.virtualpiggy.integration.order.payment.VirtualPiggyPayment;
import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentConstants;
import com.virtualpiggy.integration.profile.VPProfileUtil;

import atg.commerce.CommerceException;
import atg.commerce.order.purchase.PaymentGroupFormHandler;
import atg.commerce.order.purchase.PaymentGroupMapContainer;
import atg.core.util.StringUtils;
import atg.droplet.DropletException;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.userprofiling.Profile;

public class VirtualPiggyPaymentGroupFormHandler extends
		PaymentGroupFormHandler {

	private Profile mProfile;
	
	private Map<String,String> mValue = new HashMap<String, String>(2);
	
	private static final String ERR_MSG_VP_USER_NOT_AUTHENTICATED = "vpUserNotAuthenticated";
	private static final String ERR_MSG_VP_PAYMENT_ACCOUNT_ID_MISSING_IN_REQUEST = "vpPaymentIdMissing";
	private static final String ERR_MSG_VP_PAYMENT_GROUP_CREATION = "vpPaymentGrpCreateErr";
	
	/**
	 * This method overrides OOTB method called before applying payment group to the order.
	 * It validates user selected VP payment information, creates Virtual Piggy Payment group and
	 * adds it to the payment group container - making it available for the handler method to apply
	 * to the order.
	 * 
	 */
	@Override
	public void preApplyPaymentGroups(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException,
			IOException {
		//create virtual piggy payment group
		if(isLoggingDebug()) logDebug("Creating Virtual Piggy Payment Group...");
		if(getProfile() == null){
			mProfile= (Profile)pRequest.resolveName("/atg/userprofiling/Profile");
		}

		validateVirtualPiggyPaymentData(pRequest, pResponse);
		if(getFormError()){
			return;
		}
		
		//pRequest.setParameter(VirtualPiggyPaymentConstants.PARAMETER_NAME_VP_TOKEN, VPProfileUtil.getVPUserToken(getProfile()));
		
		String transDesc = pRequest.getParameter(VirtualPiggyPaymentConstants.PARAMETER_NAME_TRANS_DESC);
		if(StringUtils.isEmpty(transDesc)){
			transDesc = addDefaultTransactionDescription();
			pRequest.setParameter(VirtualPiggyPaymentConstants.PARAMETER_NAME_TRANS_DESC, transDesc);
		}
		createVirtualPiggyPaymentAndAddToContainers(pRequest, pResponse);
		//update profile for payment id used
		String payID = getValue().get(VirtualPiggyPaymentConstants.PARAMETER_NAME_PAYMENT_ACCOUNT_ID);
		if(payID != null){
			VPProfileUtil.setCurrentlySelectedParentPaymentIdToProfile(getProfile(), payID);
		}
		
		super.preApplyPaymentGroups(pRequest, pResponse);
	}
	
	/**
	 * This method creates Virtual Piggy Payment group, sets the VP payment info and adds
	 * it to the payment group container. It also marks the newly created payment group
	 * as the default one.
	 * 
	 */
	private void createVirtualPiggyPaymentAndAddToContainers(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws IOException, ServletException{
        if (isLoggingDebug()) {
            logDebug("Entering createVirtualPiggyPaymentAndAddToContainers...");
        }
        try{
	        //Create a payment group of type VirtualPiggyPayment
	        VirtualPiggyPayment vpPayGrp = (VirtualPiggyPayment)getPaymentGroupManager().
	                                		createPaymentGroup(VirtualPiggyPaymentConstants.ITEM_DESC_NAME_VIRTUAL_PIGGY_PAYMENT);
	        if (isLoggingDebug()) {
	            logDebug("createVirtualPiggyPaymentAndAddToContainers-> payment group instance created.");
	        }
	        //Set the amount to the payment group.
	        vpPayGrp.setAmount(getOrder().getPriceInfo().getTotal());
	        vpPayGrp.setVpToken(VPProfileUtil.getVPUserToken(getProfile()));
	        vpPayGrp.setVpTransDesc(pRequest.getParameter(VirtualPiggyPaymentConstants.PARAMETER_NAME_TRANS_DESC));
	        //parent props
	        vpPayGrp.setVpChildId(VPProfileUtil.getSelectedChildFromProfile(getProfile()));
	        vpPayGrp.setVpPaymentAccId(getValue().get(VirtualPiggyPaymentConstants.PARAMETER_NAME_PAYMENT_ACCOUNT_ID));
	        if (isLoggingDebug()) {
	            logDebug("createVirtualPiggyPaymentAndAddToContainers->properties set.");
	        }
	        PaymentGroupMapContainer container = getPaymentGroupMapContainer();
	        container.removeAllPaymentGroups();
	        //Add the payment group to the container.
	        container.addPaymentGroup(VirtualPiggyPaymentConstants.ITEM_DESC_NAME_VIRTUAL_PIGGY_PAYMENT, vpPayGrp);
	        container.setDefaultPaymentGroupName(VirtualPiggyPaymentConstants.ITEM_DESC_NAME_VIRTUAL_PIGGY_PAYMENT);
	        if (isLoggingDebug()) {
	            logDebug("createVirtualPiggyPaymentAndAddToContainers->payment group added to container and set as default.");
	        }
        }catch(CommerceException ex){
        	if(isLoggingError())logError("Error Creating Virtual Piggy Payment Group." + ex.getMessage());
        	String msg = VirtualPiggyPaymentConstants.getMsgString(ERR_MSG_VP_PAYMENT_GROUP_CREATION, getUserLocale(pRequest, pResponse));
			addFormException(new DropletException(msg, ERR_MSG_VP_PAYMENT_GROUP_CREATION));
        }catch(Exception ex){
        	if(isLoggingError())logError("Error Creating Virtual Piggy Payment Group." + ex.getMessage());
        	String msg = VirtualPiggyPaymentConstants.getMsgString(ERR_MSG_VP_PAYMENT_GROUP_CREATION, getUserLocale(pRequest, pResponse));
			addFormException(new DropletException(msg, ERR_MSG_VP_PAYMENT_GROUP_CREATION));
        }
        if (isLoggingDebug()) {
            logDebug("Existing createVirtualPiggyPaymentAndAddToContainers.");
        }
	}
	
	private void validateVirtualPiggyPaymentData(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws IOException, ServletException{
		if(StringUtils.isEmpty(VPProfileUtil.getVPUserToken(getProfile()))){
			String msg = VirtualPiggyPaymentConstants.getMsgString(ERR_MSG_VP_USER_NOT_AUTHENTICATED, getUserLocale(pRequest, pResponse));
			addFormException(new DropletException(msg, ERR_MSG_VP_USER_NOT_AUTHENTICATED));
		}
		if(VPProfileUtil.isVirtualPiggyParentLogin(getProfile())){//if parent flow.
			//paymentid expected as input parameter.
			if(StringUtils.isEmpty(getValue().get(VirtualPiggyPaymentConstants.PARAMETER_NAME_PAYMENT_ACCOUNT_ID))){
				String msg = VirtualPiggyPaymentConstants.getMsgString(ERR_MSG_VP_PAYMENT_ACCOUNT_ID_MISSING_IN_REQUEST, getUserLocale(pRequest, pResponse));
				addFormException(new DropletException(msg, ERR_MSG_VP_PAYMENT_ACCOUNT_ID_MISSING_IN_REQUEST));
			}
		}
		
	}
	
	private String addDefaultTransactionDescription(){
		return VPProfileUtil.getVPUserName(getProfile()) + "'s "+ " Order with id:" + getOrder().getId();
	}
	
	public Profile getProfile() {
		return mProfile;
	}

	public void setProfile(Profile pProfile) {
		this.mProfile = pProfile;
	}

	public Map<String, String> getValue() {
		return mValue;
	}

	public void setValue(Map<String, String> pValue) {
		this.mValue = pValue;
	}
}
