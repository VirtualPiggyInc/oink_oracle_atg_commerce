package com.virtualpiggy.integration.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.virtualpiggy.integration.services.VirtualPiggyIntegrationService;
import com.virtualpiggy.integration.services.beans.VPAddressResponseBean;
import com.virtualpiggy.integration.services.beans.VPAuthenticationResponseBean;
import com.virtualpiggy.integration.services.beans.VPChildrenResponseBean;
import com.virtualpiggy.integration.services.beans.VirtualPiggyAccountInfo;
import com.virtualpiggy.integration.services.beans.VirtualPiggyPaymentAccountInfoBean;

import atg.beans.DynamicBeans;
import atg.beans.PropertyNotFoundException;
import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.repository.MutableRepository;
import atg.repository.MutableRepositoryItem;
import atg.repository.Repository;
import atg.repository.RepositoryException;
import atg.repository.RepositoryItem;
import atg.userprofiling.Profile;
import atg.userprofiling.ProfileTools;

public class VPProfileTools extends GenericService {
	
	private VirtualPiggyIntegrationService mSvc;
	private String mContactInfoItemDescName;
	private Map<String,String> mContactInfoToVPAddressMap;
	
	/**
	 * Authenticates user credentials with Virtual Piggy and returns the response.
	 * 
	 * @param pUser
	 * @param pPassword
	 * @return
	 */
	public VPAuthenticationResponseBean authenticateVPUser(String pUser, String pPassword){
		return getService().authenticateUser(pUser, pPassword);
	}
	
	/**
	 * Updates profile with user authentication data. Data that identifies the user - token, usertype etc.
	 * 
	 * @param pProfile
	 * @param pAuthBean
	 */
	public void updateProfileWithAuthenticationData(Profile pProfile, VPAuthenticationResponseBean pAuthBean){
		VPProfileUtil.setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_CHECKOUT, Boolean.valueOf(true));
		VPProfileUtil.setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_TOKEN, pAuthBean.getVpToken());
		VPProfileUtil.setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_TYPE, pAuthBean.getUserType());
		if(isLoggingDebug()){
			logDebug("updateProfileWithAuthenticationData: Virtual Piggy Data Updated in Profile:\n");
			logDebug(VPProfileConstants.PROPERTY_NAME_VP_CHECKOUT + "="+ VPProfileUtil.getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_CHECKOUT));
			logDebug(VPProfileConstants.PROPERTY_NAME_VP_USER_NAME + "="+ VPProfileUtil.getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_NAME));
			logDebug(VPProfileConstants.PROPERTY_NAME_VP_USER_TOKEN + "="+ VPProfileUtil.getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_TOKEN));
			logDebug(VPProfileConstants.PROPERTY_NAME_VP_USER_TYPE + "="+ VPProfileUtil.getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_TYPE));
		}
	}
	
	/**
	 * Checks profile repository to locate the user with passed in information.
	 * If located, profile is set to use the located profile repository item.
	 * 
	 * @param pLogin
	 * @param pProfile
	 * @param pProfTools
	 */
	public void loadProfileIfExisting(String pLogin, Profile pProfile, ProfileTools pProfTools){
		RepositoryItem prof = pProfTools.getItem(pLogin, null, pProfTools.getDefaultProfileType());
		if(prof != null){
			pProfile.setDataSource(prof);
		}
	}
	
	/**
	 * Persists the transient profile to profile repository and sets the required properties with VP username value.
	 * 
	 * @param pLogin
	 * @param pProfile
	 */
	public void persistTransientProfile(String pLogin, Profile pProfile){
		if(pProfile == null) return;
		//persist profile if transient
		if(pProfile.isTransient()){
			MutableRepositoryItem mutProfItem = null;
			RepositoryItem profItem = pProfile.getDataSource();
			Repository profRepository = pProfile.getRepository();
			if(profRepository instanceof MutableRepository && profItem instanceof MutableRepositoryItem){
				mutProfItem = (MutableRepositoryItem)profItem;
				try {
					//set required properties
					mutProfItem.setPropertyValue("email", VPProfileUtil.getVPUserName(pProfile));
					mutProfItem.setPropertyValue("password", VPProfileUtil.getVPUserName(pProfile));
					mutProfItem.setPropertyValue("lastName", VPProfileUtil.getVPUserName(pProfile));
					mutProfItem.setPropertyValue("login", pLogin);
					mutProfItem.setPropertyValue("firstName", VPProfileUtil.getVPUserName(pProfile));
					mutProfItem.setPropertyValue("autoLogin", false);
					mutProfItem.setPropertyValue("receiveEmail", "no");
					
					((MutableRepository)profRepository).addItem(mutProfItem);
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * Calls Virtual Piggy api to get all children for authenticated parent profile. 
	 * Returned Children information is set in the profile.
	 * 
	 * @param pProfile
	 */
	public void getAllChildrenForParentAndUpdateProfile(Profile pProfile){
		if(isLoggingDebug()){
			logDebug("Start getAllChildrenForParentAndUpdateProfile\n");
		}
		VirtualPiggyAccountInfo accInfo = new VirtualPiggyAccountInfo();
		accInfo.setVpToken(VPProfileUtil.getVPUserToken(pProfile));
		
		VPChildrenResponseBean[] children = getService().getAllChildrenForAParent(accInfo);
		if(children != null){
			if(isLoggingDebug()){
				logDebug("getAllChildrenForParentAndUpdateProfile...# of children = " + children.length);
			}
			List childrenList = (List)VPProfileUtil.getChildren(pProfile);
			for(int i =0; i < children.length; i++){
				if(children[i] != null){
					MutableRepositoryItem child= updateProfileWithChildDetails(pProfile, children[i]);
					childrenList.add(child);
					if(isLoggingDebug()){
						logDebug("getAllChildrenForParentAndUpdateProfile...child added = " + children[i]);
					}
				}
			}
		}
		if(isLoggingDebug()){
			logDebug("End getAllChildrenForParentAndUpdateProfile\n");
		}
	}
	
	/**
	 * Creates a child repository item and updates the child information from VPChildrenResponseBean.
	 * 
	 * @param pProfile
	 * @param pChild
	 * @return
	 */
	private MutableRepositoryItem updateProfileWithChildDetails(Profile pProfile, VPChildrenResponseBean pChild){
		MutableRepository profRep = (MutableRepository) pProfile.getRepository();
		try {
			//transient
			MutableRepositoryItem childItem = profRep.createItem(VPProfileConstants.ITEMS_DESC_VP_CHILD);
			childItem.setPropertyValue(VPProfileConstants.PROPERTY_NAME_VP_CHILD_ID, pChild.getIdentifier());
			childItem.setPropertyValue(VPProfileConstants.PROPERTY_NAME_VP_CHILD_NAME, pChild.getName());
			childItem.setPropertyValue(VPProfileConstants.PROPERTY_NAME_VP_CHILD_TYPE, pChild.getType());
			
			return childItem;
		} catch (RepositoryException e) {
			if(isLoggingError()){
				logError(e.getMessage());
			}
		}
		return null;
	}
	
	/**
	 * Clears all VP transient information from the passed in profile.
	 * 
	 * @param pProfile
	 */
	public void clearVirtualPiggyProfileSettings(Profile pProfile){
		if(pProfile != null){
			VPProfileUtil.setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_CHECKOUT, Boolean.valueOf(false));
			VPProfileUtil.setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_TOKEN, "");
			VPProfileUtil.setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_TYPE, "");
			VPProfileUtil.setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_TYPE, "");
			Map addresses = (Map)VPProfileUtil.getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_SHIP_ADDRESSES);
			if(addresses != null){
				addresses.clear();
			}
			VPProfileUtil.setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_SHIP_ADDRESS, null);
			VPProfileUtil.setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_CHILD_SELECTED, null);
			VPProfileUtil.setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_PAYMENT_SELECTED, null);
			List children = (List)VPProfileUtil.getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_CHILDREN);
			if(children != null){
				children.clear();
			}
			List payments = (List)VPProfileUtil.getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_PAYMENTS);
			if(payments != null){
				payments.clear();
			}
		}
	}
	
	/**
	 * Updates profile property to set selected child id.
	 * 
	 * @param pProfile
	 * @param pSelChildId
	 * @return
	 */
	public boolean updateProfileForSelectedChild(Profile pProfile, String pSelChildId){
		if(pProfile != null){
			VPProfileUtil.setSelectedChildToProfile(pProfile, pSelChildId);
			return true;
		}
		return false;
	}
	
	/**
	 * Updates profile property to set selected child id.
	 * 
	 * @param pProfile
	 * @param pSelChildId
	 * @return
	 */
	public boolean updateProfileForParentShipAddressSelection(Profile pProfile, String isParentShipAddressSelected){
		if(pProfile != null){
			VPProfileUtil.setParentShipAddressSelected(pProfile, isParentShipAddressSelected);
			return true;
		}
		return false;
	}
	

	/**
	 * Creates a new contact info item and updates the address information retrieved from Virtual Piggy for a User.
	 * Address is mapped to a child-id.
	 * 
	 * @param pProfile user profile.
	 * @param pUserIdentifier child id to map the address with.
	 * @param pInBean address response bean from Virtual Piggy service call.
	 * @return contact info repository item.
	 */
	public MutableRepositoryItem addShipAddressToProfile(Profile pProfile, String pUserIdentifier, VPAddressResponseBean pInBean){
		MutableRepository profRep = (MutableRepository) pProfile.getRepository();
		try {
			//transient
			MutableRepositoryItem addressItem = profRep.createItem(getContactInfoItemDescriptorName());
			copyShipAddress(pInBean, addressItem);
			
			//set to profile
			//VPProfileUtil.setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_SHIP_ADDRESS, addressItem);
			Map addMap = (Map)VPProfileUtil.getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_SHIP_ADDRESSES);
			if(addMap == null){
				addMap = new HashMap();
				VPProfileUtil.setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_SHIP_ADDRESSES, addMap);
			}
			
			addMap.put(pUserIdentifier,addressItem);
			
			return addressItem;
		} catch (RepositoryException e) {
			if(isLoggingError()){
				logError(e.getMessage());
			}
		}
		return null;
	}
	
	/**
	 * Creates a new contact info item and updates the address information retrieved from Virtual Piggy for a User.
	 * Address is mapped to a child-id.
	 * 
	 * @param pProfile user profile.
	 * @param pUserIdentifier child id to map the address with.
	 * @param pInBean address response bean from Virtual Piggy service call.
	 * @return contact info repository item.
	 */
	public MutableRepositoryItem addEmailToProfile(Profile pProfile, String pUserIdentifier, VPAddressResponseBean pInBean){
		MutableRepository profRep = (MutableRepository) pProfile.getRepository();
		try {
			//transient
			MutableRepositoryItem userItem = profRep.getItemForUpdate(pProfile.getRepositoryId(),VPProfileConstants.ITEM_NAME_USER);
			userItem.setPropertyValue(VPProfileConstants.PROPERTY_NAME_EMAIL, pInBean.getParentEmail());
			return userItem;
		} catch (RepositoryException e) {
			if(isLoggingError()){
				logError(e.getMessage());
			}
		}
		return null;
	}
	
	/**
	 * Creates a Virtual Piggy parent payment information repository item from the VirtualPiggyPaymentAccountInfoBean.
	 * 
	 * @param pProfile user profile.
	 * @param pPayBean VirtualPiggyPaymentAccountInfoBean object as a payment a/c info of a parent.
	 * @return Virtual Piggy Parent Payment repository item.
	 */
	public MutableRepositoryItem createPaymentAccountItem(Profile pProfile, VirtualPiggyPaymentAccountInfoBean pPayBean){
		MutableRepository profRep = (MutableRepository) pProfile.getRepository();
		try {
			//transient
			MutableRepositoryItem payItem = profRep.createItem(VPProfileConstants.ITEMS_DESC_VP_PAYMENT);
			if(payItem != null){
				payItem.setPropertyValue(VPProfileConstants.PROPERTY_NAME_VP_PAYMENT_ID, pPayBean.getIdentifier());
				payItem.setPropertyValue(VPProfileConstants.PROPERTY_NAME_VP_PAYMENT_NAME, pPayBean.getName());
				payItem.setPropertyValue(VPProfileConstants.PROPERTY_NAME_VP_PAYMENT_TYPE, pPayBean.getType());
				payItem.setPropertyValue(VPProfileConstants.PROPERTY_NAME_VP_PAYMENT_URL, pPayBean.getUrl());
			}
			return payItem;
		} catch (RepositoryException e) {
			if(isLoggingError()){
				logError(e.getMessage());
			}
		}
		return null;
	}

	/**
	 * Adds parent payment a/c repository item to profile's parent payment information list.
	 * 
	 * @param pProfile
	 * @param pPayItem
	 * @return
	 */
	public List<MutableRepositoryItem> addPaymentAccountItemToProfile(Profile pProfile, MutableRepositoryItem pPayItem){
		//transient
		List<MutableRepositoryItem> addList = (List<MutableRepositoryItem>)VPProfileUtil.getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_PAYMENTS);
		if(addList == null){
			addList = new ArrayList<MutableRepositoryItem>();
			VPProfileUtil.setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_PAYMENTS, addList);
		}
		addList.add(pPayItem);
		
		return addList;
	
	}

	/**
	 * copy shipping address values from VPAddressResponseBean to mutable repository item.
	 * 
	 * @param pFromBean VPAddressResponseBean object.
	 * @param pToItem mutable repository item to copy values to.
	 */
	private void copyShipAddress(VPAddressResponseBean pFromBean, MutableRepositoryItem pToItem){
		
		Map<String,String> propMap = getContactInfoToVPAddressMap();
		if(propMap == null || propMap.isEmpty()){
			if(isLoggingError()) logError("copyShipAddress-> Nothing to copy. Configured map is null.");
			return;
		}
		Set<String> itemProps = propMap.keySet();
		for (String itemProp : itemProps) {		
			
			String beanProp = propMap.get(itemProp);
			
			if(!StringUtils.isEmpty(beanProp)){
				try {
					pToItem.setPropertyValue(itemProp, DynamicBeans.getPropertyValue(pFromBean, beanProp));
				} catch (PropertyNotFoundException e) {
					if(isLoggingError()) logError(e.getMessage());
					//skip
				}
			}
		} 
	}
	
	public String getContactInfoItemDescriptorName(){
		return mContactInfoItemDescName;
	}
	
	public void setContactInfoItemDescriptorName(String pContactInfoItemDescName){
		mContactInfoItemDescName = pContactInfoItemDescName;
	}
	
	public Map<String,String> getContactInfoToVPAddressMap() {
		return mContactInfoToVPAddressMap;
	}

	public void setContactInfoToVPAddressMap(Map<String,String> pContactInfoToVPAddressMap) {
		this.mContactInfoToVPAddressMap = pContactInfoToVPAddressMap;
	}

	public void setService(VirtualPiggyIntegrationService pSvc){
		mSvc = pSvc;
	}

	public VirtualPiggyIntegrationService getService(){
		return mSvc;
	}

}
