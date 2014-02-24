package com.virtualpiggy.integration.purchase;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import com.virtualpiggy.integration.order.VirtualPiggyOrderConstants;
import com.virtualpiggy.integration.profile.VPProfileConstants;
import com.virtualpiggy.integration.profile.VPProfileTools;
import com.virtualpiggy.integration.profile.VPProfileUtil;
import com.virtualpiggy.integration.services.beans.VirtualPiggyAccountInfo;

import atg.beans.PropertyNotFoundException;
import atg.commerce.CommerceException;
import atg.commerce.order.HardgoodShippingGroup;
import atg.commerce.order.OrderTools;
import atg.commerce.order.ShippingGroup;
import atg.commerce.order.ShippingGroupManager;
import atg.commerce.order.purchase.CommerceItemShippingInfo;
import atg.commerce.order.purchase.CommerceItemShippingInfoContainer;
import atg.commerce.order.purchase.PurchaseProcessConfiguration;
import atg.commerce.order.purchase.ShippingGroupDroplet;
import atg.commerce.order.purchase.ShippingGroupMapContainer;
import atg.commerce.pricing.PricingTools;
import atg.core.util.StringUtils;
import atg.nucleus.ServiceException;
import atg.repository.MutableRepositoryItem;
import atg.repository.RepositoryException;
import atg.repository.RepositoryItem;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.userprofiling.Profile;

/**
 * Extends OOTB ShippingGroupDroplet to reuse Shipping group creation/initialization logic.
 * 
 * @author tarun
 *
 */
public class VirtualPiggyShippingInfoDroplet extends ShippingGroupDroplet {
	private VPProfileTools mvpTools;
	private PricingTools mPricingTools;

	private String mHardgoodShippingGroupType;
	private String mDefaultShippingMethod;

	// private static final String OUTPUT = "output";
	private static final String ERROR = "error";
	private static final String ERROR_MSG = "errorMsg";
	private String mDefaultShippingGroupNameProp;

	private static final String ERR_MSG_ADDRESS_NOT_RETURNED = "errRetrievingShippingAddress";
	private static final String ERR_MSG_ADDRESS_NOT_IN_PROFILE = "invalidShipAddressInProfile";
	private static final String TRUE= "true";

	/**
	 * This droplet is used on the shipping page to show shipping address to be used with the order.
	 * The shipping address for the user (Parent or Child)  is retrieved from VP user account. If the
	 * shipping address is already retrieved and associated with the user profile then the profile address
	 * is used and the call is not made again.
	 * Finally, configured shipping method is associated as the default shipping method for the VP shipping
	 * group and commerce item-shipping group relationships.
	 * 
	 */
	@Override
	public void service(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException,
			IOException {

		// call ShippingGroupDroplet from jsp to clear.

		MutableRepositoryItem addressItem = null;
		// if VP checkout, get address for child - make a web service call-
		// check token not null
		if (VPProfileUtil.isVirtualPiggyCheckout(getProfile())) {
			// if profile has shipping address already associated then avoid the
			// call and reuse.
			boolean isParentLogin = VPProfileUtil
					.isVirtualPiggyParentLogin(getProfile());
			String userId = VPProfileUtil.getVPUserToken(getProfile());
			VirtualPiggyAccountInfo accInfo = new VirtualPiggyAccountInfo();
			accInfo.setVpToken(userId);
			accInfo.setUserType(isParentLogin ? VPProfileConstants.PROPERTY_VALUE_VP_USER_TYPE_PARENT
					: VPProfileConstants.PROPERTY_VALUE_VP_USER_TYPE_CHILD);
			if (isParentLogin ) {					
				String isParentShipAddressSelected = VPProfileUtil.getParentShipAddressSelected(getProfile());				
				if(!TRUE.equalsIgnoreCase(isParentShipAddressSelected)) {
					userId = VPProfileUtil
							.getSelectedChildFromProfile(getProfile());
					accInfo.setVpChildId(userId);
					
				}	
			} 
			addressItem = VPProfileUtil.getShipAddress(getProfile(),
					userId);
			if (addressItem == null) {// get ship address
				String keyToShippingAddress = getVirtualPiggyPurchaseProcessHelper()
						.getShippingAddressAndUpdateProfile(accInfo,
								getProfile());
				if (StringUtils.isEmpty(keyToShippingAddress)) {
					pRequest.setParameter(ERROR_MSG,
							VirtualPiggyOrderConstants.getMsgString(ERR_MSG_ADDRESS_NOT_RETURNED, pRequest.getLocale()));
					pRequest.serviceLocalParameter(ERROR, pRequest, pResponse);
					return;
				}
				addressItem = VPProfileUtil.getShipAddress(getProfile(),
						keyToShippingAddress);
				if (addressItem == null) {
					pRequest.setParameter(ERROR_MSG,
							VirtualPiggyOrderConstants.getMsgString(ERR_MSG_ADDRESS_NOT_IN_PROFILE, pRequest.getLocale()));
					pRequest.serviceLocalParameter(ERROR, pRequest, pResponse);
					return;
				}
				// SET to VPProfileConstants.PROPERTY_NAME_VP_USER_SHIP_ADDRESS
				// for it to work as default address for shipping group.
				VPProfileUtil.setProfileProperty(getProfile(),
						VPProfileConstants.PROPERTY_NAME_VP_USER_SHIP_ADDRESS,
						addressItem);
			}
		}
		super.service(pRequest, pResponse);
		
		//if shipping group was successfully created, add default shipping method
		Map shipGrps = (Map)pRequest.getObjectParameter("shippingGroups");
		Map shipInfos = (Map)pRequest.getObjectParameter("shippingInfos");
		
		if(shipGrps != null && !shipGrps.isEmpty()){
			//Only one shipping group for virtual piggy. set default shipping method to that shipping group
			ShippingGroup shipgrp = (ShippingGroup)shipGrps.get(getShippingGroupMapContainer().getDefaultShippingGroupName());
			if(shipgrp != null){
				shipgrp.setShippingMethod(getDefaultShippingMethod());
				
				if(isLoggingDebug()){
					logDebug("Added default shipping method " + getDefaultShippingMethod() + " to default VP shipping grp " + getShippingGroupMapContainer().getDefaultShippingGroupName());
				}
				if(shipInfos != null && !shipInfos.isEmpty()){
					Collection values = shipInfos.values();
					for (Iterator iterator = values.iterator(); iterator.hasNext();) {
						List cisiList = (List) iterator.next();
						if(cisiList != null){
							for (Object element : cisiList) {
								CommerceItemShippingInfo cisi = (CommerceItemShippingInfo)element;
								cisi.setShippingMethod(getDefaultShippingMethod());
							}
						}
					}
					
				}
			}
		}

	}

	/**
	 * createHardgoodShippingGroup creates a HardgoodShippingGroup from a
	 * Profile and a RepositoryItem that represents an address.
	 * 
	 * @param pAddressItem
	 *            a <code>RepositoryItem</code> value
	 * @param pProfile
	 *            a <code>Profile</code> value
	 * @return a <code>ShippingGroup</code> value
	 * @exception CommerceException
	 *                if an error occurs
	 * @exception PropertyNotFoundException
	 *                if an error occurs
	 * @exception RepositoryException
	 *                if an error occurs
	 */
	protected ShippingGroup createHardgoodShippingGroup(
			RepositoryItem pAddressItem, Profile pProfile) {
		HardgoodShippingGroup shippingGroup = null;
		try {
			shippingGroup = (HardgoodShippingGroup) getShippingGroupManager()
					.createShippingGroup(getHardgoodShippingGroupType());
			OrderTools.copyAddress(pAddressItem,
					shippingGroup.getShippingAddress());
		} catch (Exception ex) {
			if (isLoggingError())
				logError("Error Creating Hardgood Shipping Group from Virtual piggy address. "
						+ ex.getMessage());
		}
		return shippingGroup;
	}

	@Override
	public void doStartService() throws ServiceException {
		super.doStartService();
		copyConfiguration();
	}

	private Profile mProfile;
	private VirtualPiggyPurchaseProcessHelper mVirtualPiggyPurchaseProcessHelper;

	private ShippingGroupMapContainer mShippingGroupMapContainer;
	private PurchaseProcessConfiguration mConfiguration;
	private CommerceItemShippingInfoContainer mCommerceItemShippingInfoContainer;
	private ShippingGroupManager mSGManager;

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

	public void setShippingGroupMapContainer(
			ShippingGroupMapContainer pShippingGroupMapContainer) {
		mShippingGroupMapContainer = pShippingGroupMapContainer;
	}

	public ShippingGroupMapContainer getShippingGroupMapContainer() {
		return mShippingGroupMapContainer;
	}

	protected void copyConfiguration() {
		if (mConfiguration != null) {
			if (getCommerceItemShippingInfoContainer() == null) {
				setCommerceItemShippingInfoContainer(mConfiguration
						.getCommerceItemShippingInfoContainer());
			}
			if (getShippingGroupMapContainer() == null) {
				setShippingGroupMapContainer(mConfiguration
						.getShippingGroupMapContainer());
			}
			if (getShippingGroupManager() == null) {
				setShippingGroupManager(mConfiguration
						.getShippingGroupManager());
			}
		}
	}

	public void setConfiguration(PurchaseProcessConfiguration pConfiguration) {
		mConfiguration = pConfiguration;
	}

	public PurchaseProcessConfiguration getConfiguration() {
		return mConfiguration;
	}

	public void setCommerceItemShippingInfoContainer(
			CommerceItemShippingInfoContainer pCommerceItemShippingInfoContainer) {
		mCommerceItemShippingInfoContainer = pCommerceItemShippingInfoContainer;
	}

	public CommerceItemShippingInfoContainer getCommerceItemShippingInfoContainer() {
		return mCommerceItemShippingInfoContainer;
	}

	public void setShippingGroupManager(ShippingGroupManager pSGManager) {
		mSGManager = pSGManager;
	}

	public ShippingGroupManager getShippingGroupManager() {
		return mSGManager;
	}

	public String getHardgoodShippingGroupType() {
		return mHardgoodShippingGroupType;
	}

	public void setHardgoodShippingGroupType(String pHardgoodShippingGroupType) {
		this.mHardgoodShippingGroupType = pHardgoodShippingGroupType;
	}

	public String getDefaultShippingGroupNameProp() {
		return mDefaultShippingGroupNameProp;
	}

	public void setDefaultShippingGroupNameProp(
			String pDefaultShippingGroupNameProp) {
		this.mDefaultShippingGroupNameProp = pDefaultShippingGroupNameProp;
	}

	public VPProfileTools getVpTools() {
		return mvpTools;
	}

	public void setVpTools(VPProfileTools vpTools) {
		this.mvpTools = vpTools;
	}


	public String getDefaultShippingMethod() {
		return mDefaultShippingMethod;
	}

	public void setDefaultShippingMethod(String pDefaultShippingMethod) {
		this.mDefaultShippingMethod = pDefaultShippingMethod;
	}

	public PricingTools getPricingTools() {
		return mPricingTools;
	}

	public void setPricingTools(PricingTools pPricingTools) {
		this.mPricingTools = pPricingTools;
	}

}
