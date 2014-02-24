package com.virtualpiggy.integration.purchase;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import com.virtualpiggy.integration.order.payment.VirtualPiggyPaymentConstants;
import com.virtualpiggy.integration.profile.VPProfileTools;
import com.virtualpiggy.integration.profile.VPProfileUtil;
import com.virtualpiggy.integration.services.beans.VirtualPiggyAccountInfo;

import atg.nucleus.ServiceException;
import atg.repository.MutableRepositoryItem;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.servlet.DynamoServlet;
import atg.userprofiling.Profile;

public class VirtualPiggyPaymentInfoDroplet extends DynamoServlet {
	private VPProfileTools mvpTools;
	private Profile mProfile;
	private VirtualPiggyPurchaseProcessHelper mVirtualPiggyPurchaseProcessHelper;

	private static final String OPARAM_OUTPUT = "output";
	private static final String OPARAM_ERROR = "error";
	private static final String OPARAM_DEFAULT = "default";
	private static final String PARAM_PAYMENT_INFO_LIST = "paymentInfoList";
	private static final String PARAM_SELECTED_PAYMENT_ID = "selPaymentId";
	private static final String PARAM_ERROR_MSG = "errorMsg";
	

	private static final String ERR_MSG_NOT_A_VIRTUAL_PIGGY_CHECKOUT = "notVpiggyCheckout";
	private static final String ERR_MSG_INTERNAL_ERROR = "internalErrorGettingPaymentAccounts";

	/**
	 * This droplet is used on the payment page to show payment options (for parent user) or use 
	 * VP account associated default payment (for child user).
	 * For parent user, payment list is retrieved from VP parent account and set on the profile. Call 
	 * is made to VP system if the information is not already retrieved. Otherwise it is retrieved from
	 * the profile.
	 * Sets output oparam for parent user.
	 * Sets defaut oparam for child user.
	 * Sets error oparam for any errors while processing the request.
	 */
	@Override
	public void service(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException,
			IOException {

		//if virtual piggy checkout is not used.
		if(!VPProfileUtil.isVirtualPiggyCheckout(getProfile())){
			pRequest.setParameter(PARAM_ERROR_MSG, VirtualPiggyPaymentConstants.getMsgString(ERR_MSG_NOT_A_VIRTUAL_PIGGY_CHECKOUT, pRequest.getLocale()));
			pRequest.serviceLocalParameter(OPARAM_ERROR, pRequest, pResponse);
			return;
		}
		//if child return
		if(VPProfileUtil.isVirtualPiggyParentLogin(getProfile())){
			//if parent, check profile for existing payment info list of RepositoryItems.
			List<MutableRepositoryItem> paymentInfoList = VPProfileUtil.getParentPaymentsInfoFromProfile(getProfile());
			String currSelected = VPProfileUtil.getCurrentlySelectedParentPaymentIdFromProfile(getProfile());

			if(paymentInfoList == null || paymentInfoList.size() == 0){
				//if not, make a web service call to get parent's payment info, set it in the profile and return it as out parameter.
				VirtualPiggyAccountInfo accInfo = new VirtualPiggyAccountInfo();
				accInfo.setVpToken(VPProfileUtil.getVPUserToken(getProfile()));
				paymentInfoList = getVirtualPiggyPurchaseProcessHelper().getParentPaymentsAndUpdateProfile(accInfo, getProfile());
				if(paymentInfoList == null || paymentInfoList.size() == 0){
					pRequest.setParameter(PARAM_ERROR_MSG, VirtualPiggyPaymentConstants.getMsgString(ERR_MSG_INTERNAL_ERROR, pRequest.getLocale()));
					pRequest.serviceLocalParameter(OPARAM_ERROR, pRequest, pResponse);
					return;
				}
			}
			pRequest.setParameter(PARAM_PAYMENT_INFO_LIST, paymentInfoList);
			pRequest.setParameter(PARAM_SELECTED_PAYMENT_ID, currSelected);
			pRequest.serviceLocalParameter(OPARAM_OUTPUT, pRequest, pResponse);
			
		} else if(VPProfileUtil.isVirtualPiggyChildLogin(getProfile())){
			//user default set with child's profile in virtual piggy system.
			pRequest.serviceLocalParameter(OPARAM_DEFAULT, pRequest, pResponse);
		}else{
			//error
			pRequest.setParameter(PARAM_ERROR_MSG, VirtualPiggyPaymentConstants.getMsgString(ERR_MSG_NOT_A_VIRTUAL_PIGGY_CHECKOUT, pRequest.getLocale()));
			pRequest.serviceLocalParameter(OPARAM_ERROR, pRequest, pResponse);
			return;
		}
		
	}

	@Override
	public void doStartService() throws ServiceException {
		super.doStartService();
	}

	public VirtualPiggyPurchaseProcessHelper getVirtualPiggyPurchaseProcessHelper() {
		return mVirtualPiggyPurchaseProcessHelper;
	}

	public void setVirtualPiggyPurchaseProcessHelper(
			VirtualPiggyPurchaseProcessHelper pVirtualPiggyPurchaseProcessHelper) {
		this.mVirtualPiggyPurchaseProcessHelper = pVirtualPiggyPurchaseProcessHelper;
	}

	public void setProfile(Profile pProfile) {
		mProfile = pProfile;
	}

	public Profile getProfile() {
		return mProfile;
	}

	public VPProfileTools getVpTools() {
		return mvpTools;
	}

	public void setVpTools(VPProfileTools vpTools) {
		this.mvpTools = vpTools;
	}

}
