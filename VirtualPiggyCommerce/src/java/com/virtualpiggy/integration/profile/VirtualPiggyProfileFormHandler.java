package com.virtualpiggy.integration.profile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;

import com.virtualpiggy.integration.services.beans.VPAuthenticationResponseBean;

import atg.commerce.profile.CommerceProfileFormHandler;
import atg.core.i18n.LayeredResourceBundle;
import atg.core.util.StringUtils;
import atg.droplet.DropletException;
import atg.droplet.DropletFormException;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;

/**
 * This class is Virtual Piggy's extension of ATG Commerce CommerceProfileFormhandler.
 * 
 * @author Tarun
 *
 */
public class VirtualPiggyProfileFormHandler extends CommerceProfileFormHandler {

	private VPProfileTools mvpTools;

	private String mVPUserNameParam;
	private String mVPPassParam;
	private String mParentLoginSuccessURL;
	private String mSelectVirtualPiggyChildSuccessURL;
	private String mSelectVirtualPiggyChildErrorURL;

	//String constants
	//private static final String VIRTUAL_PIGGY_USER_LOGIN_SUFFIX = "@virtualpiggy";
	private static final String PARAM_NAME_VP_SELECTED_CHILD = "virtualPiggyChildSelect";
	private static final String PARAM_NAME_VP_PARENTADDRESSSELECTION = "virtualPiggyParentShipAddressSelection";
	private static final String ERR_MSG_VP_USERNAME_NOT_ENTERED = "vpiggyUsernameNotEntered";
	private static final String ERR_MSG_VP_PASSWORD_NOT_ENTERED = "vpiggyPwdNotEntered";
	private static final String ERR_MSG_VP_UNABLE_TO_AUTHENTICATE_USER = "vpiggyUnableToAuthenticateUser";
	private static final String ERR_MSG_VP_USER_AUTHENTICATION_FAILED = "vpiggyUserAuthenticationFailed";
	private static final String ERR_MSG_VP_MISSING_CHILDREN_INFO = "vpiggyMissingChildrenInfo";
	private static final String ERR_MSG_VP_SELECT_CHILD = "vpiggySelectChild";
	private static final String ERR_MSG_VP_UPDATE_CHILD_TO_PROFILE = "vpiggyUpdateChildToProfile";
	private static final String ERR_MSG_VP_UPDATE_PARENT_SHIP_ADDRESS_SELECTION_TO_PROFILE = "vpiggyUpdateParentShipAddressSelectionToProfile";

	//payment resource bundle name.
	public static final String PROFILE_RESOURCE_BUNDLE_NAME = "com.virtualpiggy.integration.profile.ProfileResources";

	public static String getMsgString(String pMsgKey, Locale pLocale){
		ResourceBundle bundle = LayeredResourceBundle.getBundle(PROFILE_RESOURCE_BUNDLE_NAME, pLocale);
		return bundle.getString(pMsgKey);
	}
	
	/**
	 * This method is used to handle VirtualPiggy Child Login during checkout.
	 * It validates the input username and password with Virtual Piggy Web Service.
	 * if authenticated, it persists the return information with Profile. If not, it displays a message to the user.
	 * 
	 * @param pRequest {@link DynamoHttpServletRequest}
	 * @param pResponse {@link DynamoHttpServletResponse}
	 * @return true - if login validated, false - otherwise.
	 * @throws ServletException
	 * @throws IOException
	 */
	public boolean handleVPLoginDuringCheckout(
			DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException,
			IOException {
		preVPLoginDuringCheckout(pRequest, pResponse);

		//If any form errors found, redirect to error URL:
		if (! checkFormRedirect(null, getLoginErrorURL(), pRequest, pResponse))
			return false;

		VPAuthenticationResponseBean authBean = getVpTools().authenticateVPUser(getVPUserName(), getVPPassword());

		checkIfUserAuthenticated(authBean, pRequest, pResponse);

		if (! checkFormRedirect(null, getLoginErrorURL(), pRequest, pResponse))
			return false;

		getVpTools().clearVirtualPiggyProfileSettings(getProfile());

		VPProfileUtil.setProfileProperty(getProfile(), VPProfileConstants.PROPERTY_NAME_VP_USER_NAME, getVPUserName());

		getVpTools().updateProfileWithAuthenticationData(getProfile(), authBean);

		if(VPProfileUtil.isVirtualPiggyParentLogin(getProfile())){
			handleParentLogin(pRequest, pResponse);
            return checkFormRedirect(getParentLoginSuccessURL(), getLoginErrorURL(), pRequest, pResponse);
		}
		
		return checkFormRedirect(getLoginSuccessURL(), getLoginErrorURL(), pRequest, pResponse);
	}

	/**
	 * This method is used to select VirtualPiggy Child during checkout.
	 * 
	 * @param pRequest {@link DynamoHttpServletRequest}
	 * @param pResponse {@link DynamoHttpServletResponse}
	 * @return true - if login validated, false - otherwise.
	 * @throws ServletException
	 * @throws IOException
	 */

	/**
	 * This method handles form submit request to set selected Virtual Piggy Child to profile.
	 * It validates the input parameters before setting it to profile.
	 * 
	 * @param pRequest
	 * @param pResponse
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public boolean handleSelectVirtualPiggyChild(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException,
			IOException {
		//get selected child id
		String selectedChildId = (String)getValue().get(PARAM_NAME_VP_SELECTED_CHILD) ;
		if(isLoggingDebug()){
			logDebug("handleSelectVirtualPiggyChild: selected child id = " + selectedChildId);
		}
		validateChildSelection(selectedChildId, pRequest, pResponse);
		
		String parentShipAddressSelected = (String)getValue().get(PARAM_NAME_VP_PARENTADDRESSSELECTION) ;
		if(isLoggingDebug()){
			logDebug("handleSelectVirtualPiggyChild: Is Parent Ship Address Selected? = " + parentShipAddressSelected);
		}

		//If any form errors found, redirect to error URL:
		if (! checkFormRedirect(null, getSelectVirtualPiggyChildErrorURL(), pRequest, pResponse))
			return false;

		//set it to profile.
		boolean updated = getVpTools().updateProfileForSelectedChild(getProfile(), selectedChildId);
		if(isLoggingDebug()){
			logDebug("handleSelectVirtualPiggyChild: selected child set to profile = " + updated);
		}
		if(!updated){
			String msg = getMsgString(ERR_MSG_VP_UPDATE_CHILD_TO_PROFILE, getUserLocale(pRequest, pResponse));
			addFormException(new DropletException(msg, ERR_MSG_VP_UPDATE_CHILD_TO_PROFILE));
		}
		
		updated = getVpTools().updateProfileForParentShipAddressSelection(getProfile(), parentShipAddressSelected);
		if(isLoggingDebug()){
			logDebug("handleSelectVirtualPiggyChild: Parent Ship Address Selection Set to profile = " + updated);
		}
		if(!updated){
			String msg = getMsgString(ERR_MSG_VP_UPDATE_PARENT_SHIP_ADDRESS_SELECTION_TO_PROFILE, getUserLocale(pRequest, pResponse));
			addFormException(new DropletException(msg, ERR_MSG_VP_UPDATE_PARENT_SHIP_ADDRESS_SELECTION_TO_PROFILE));
		}
		return checkFormRedirect(getSelectVirtualPiggyChildSuccessURL(), getSelectVirtualPiggyChildErrorURL(), pRequest, pResponse);
	}

	/**
	 * This method is used to validate child selection during checkout.
	 * 
	 * @param pRequest {@link DynamoHttpServletRequest}
	 * @param pResponse {@link DynamoHttpServletResponse}
	 * @throws ServletException
	 * @throws IOException
	 */
	private void validateChildSelection(String pSelectedChildId, DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws IOException,
			ServletException {
		if(StringUtils.isEmpty(pSelectedChildId)){
			String msg = getMsgString(ERR_MSG_VP_SELECT_CHILD, getUserLocale(pRequest, pResponse));
			addFormException(new DropletException(msg, ERR_MSG_VP_SELECT_CHILD));
		}
	}

	/**
	 * Validates to check if a valid value is selected for the child id.
	 * 
	 * @param pAuthBean
	 * @param pRequest
	 * @param pResponse
	 * @throws IOException
	 * @throws ServletException
	 */
	private void checkIfUserAuthenticated(VPAuthenticationResponseBean pAuthBean, DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws IOException,
			ServletException {
		if (pAuthBean == null) {
			String msg = getMsgString(ERR_MSG_VP_UNABLE_TO_AUTHENTICATE_USER, getUserLocale(pRequest, pResponse));
			addFormException(new DropletException(msg, ERR_MSG_VP_UNABLE_TO_AUTHENTICATE_USER));
			return;
		}
		Boolean bSuccess = pAuthBean.getStatus();
		if (!bSuccess.booleanValue()) {
			String msg = getMsgString(ERR_MSG_VP_USER_AUTHENTICATION_FAILED, getUserLocale(pRequest, pResponse));
			addFormException(new DropletException(msg, ERR_MSG_VP_USER_AUTHENTICATION_FAILED));
		}
	}

	/**
	 * This method implements the additional logic when the authenticated user is identified as Parent.
	 * Additional logic is to get all children for the authenticated parent account and set it in profile.
	 * 
	 * @param pRequest
	 * @param pResponse
	 * @throws IOException
	 * @throws ServletException
	 */
	private void handleParentLogin(DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse) throws IOException,
	ServletException {
		getVpTools().getAllChildrenForParentAndUpdateProfile(getProfile());
		List childrenList = (List)VPProfileUtil.getChildren(getProfile());
		if(childrenList == null || childrenList.size() == 0){
			String msg = getMsgString(ERR_MSG_VP_MISSING_CHILDREN_INFO, getUserLocale(pRequest, pResponse));
			addFormException(new DropletException(msg, ERR_MSG_VP_MISSING_CHILDREN_INFO));
		}	
	}

	/**
	 * This method validates input parameters and sets form exceptions if validation fails.
	 * This method is called before authenticating the user with Virtual Piggy.
	 * 
	 * @param pRequest
	 * @param pResponse
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void preVPLoginDuringCheckout(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException,
			IOException {
		if(StringUtils.isEmpty(getVPUserName()) || StringUtils.isEmpty(getVPUserName().trim())){
			String msg = getMsgString(ERR_MSG_VP_USERNAME_NOT_ENTERED, getUserLocale(pRequest, pResponse));
			addFormException(new DropletFormException(msg, ERR_MSG_VP_USERNAME_NOT_ENTERED));
		}
		if(StringUtils.isEmpty(getVPPassword()) || StringUtils.isEmpty(getVPPassword().trim())){
			String msg = getMsgString(ERR_MSG_VP_PASSWORD_NOT_ENTERED, getUserLocale(pRequest, pResponse));
			addFormException(new DropletFormException(msg, ERR_MSG_VP_PASSWORD_NOT_ENTERED));
		}
	}

	private String getVPUserName(){
		return (String)getValue().get(getVirtualPiggyUserNameParam());
	}

	private String getVPPassword(){
		return (String)getValue().get(getVirtualPiggyPasswordParam());
	}

	public String getVirtualPiggyUserNameParam(){
		return mVPUserNameParam;
	}

	public void setVirtualPiggyUserNameParam(String pVPUserNameParam){
		mVPUserNameParam = pVPUserNameParam;
	}

	public String getVirtualPiggyPasswordParam(){
		return mVPPassParam;
	}

	public void setVirtualPiggyPasswordParam(String pVPPassParam){
		mVPPassParam = pVPPassParam;
	}

	public VPProfileTools getVpTools() {
		return mvpTools;
	}

	public void setVpTools(VPProfileTools vpTools) {
		this.mvpTools = vpTools;
	}

	public String getParentLoginSuccessURL() {
		return mParentLoginSuccessURL;
	}

	public void setParentLoginSuccessURL(String pParentLoginSuccessURL) {
		this.mParentLoginSuccessURL = pParentLoginSuccessURL;
	}

	public String getSelectVirtualPiggyChildSuccessURL() {
		return mSelectVirtualPiggyChildSuccessURL;
	}

	public void setSelectVirtualPiggyChildSuccessURL(
			String pSelectVirtualPiggyChildSuccessURL) {
		this.mSelectVirtualPiggyChildSuccessURL = pSelectVirtualPiggyChildSuccessURL;
	}

	public String getSelectVirtualPiggyChildErrorURL() {
		return mSelectVirtualPiggyChildErrorURL;
	}

	public void setSelectVirtualPiggyChildErrorURL(
			String pSelectVirtualPiggyChildErrorURL) {
		this.mSelectVirtualPiggyChildErrorURL = pSelectVirtualPiggyChildErrorURL;
	}

}
