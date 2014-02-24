package com.virtualpiggy.integration.profile;

import java.util.List;
import java.util.Map;

import atg.core.util.StringUtils;
import atg.repository.MutableRepositoryItem;
import atg.userprofiling.Profile;

public class VPProfileUtil {
	
	/**
	 * Utility method to set input profile property with input value.
	 * 
	 */
	public static void setProfileProperty(Profile pProfile, String pProperty, Object pValue){
		if(pProfile != null){
			pProfile.setPropertyValue(pProperty, pValue);
		}
	}
	
	/**
	 * Utility method to get value for input profile property pProperty.
	 * 
	 * @param pProfile
	 * @param pProperty
	 * @return
	 */
	public static Object getProfileProperty(Profile pProfile, String pProperty){
		if(pProfile != null && pProperty != null){
			return pProfile.getPropertyValue(pProperty);
		}
		return null;
	}
	
//	/**
//	 * Utility method to get a child's shipping address from profile, based on child id - pChildId.
//	 * 
//	 * @param pProfile user profile.
//	 * @param pChildId child-id
//	 * @return shipping address repository item.
//	 */
//	public static MutableRepositoryItem getChildShipAddress(Profile pProfile, String pChildId){
//		Map addMap = (Map)getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_SHIP_ADDRESSES);
//		MutableRepositoryItem address = null;
//		if(addMap != null){
//			address = (MutableRepositoryItem)addMap.get(pChildId);
//		}
//		//(MutableRepositoryItem)getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_SHIP_ADDRESS);
//		return address;
//	}
	
	
	
	/**
	 * Utility method to get a User's shipping address from profile, based on User id - pUserId.
	 * 
	 * @param pProfile user profile.
	 * @param pUserId User -id
	 * @return shipping address repository item.
	 */
	public static MutableRepositoryItem getShipAddress(Profile pProfile, String pUserId){
		Map addMap = (Map)getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_SHIP_ADDRESSES);
		MutableRepositoryItem address = null;
		if(addMap != null){
			address = (MutableRepositoryItem)addMap.get(pUserId);
		}
		//(MutableRepositoryItem)getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_SHIP_ADDRESS);
		return address;
	}
	
	/**
	 * Utility method to get a list of parent payment a/c infos from profile.
	 * 
	 * @param pProfile user profile.
	 * @return List of repository items representing parent payment info items.
	 */
	public static List<MutableRepositoryItem> getParentPaymentsInfoFromProfile(Profile pProfile){
		return (List<MutableRepositoryItem>)getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_PAYMENTS);
	}

	/**
	 * Returns the currently selected parent payment id used for order payment.
	 * 
	 * @param pProfile
	 * @return payment id.
	 */
	public static String getCurrentlySelectedParentPaymentIdFromProfile(Profile pProfile){
		return (String)getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_PAYMENT_SELECTED);
	}

	/**
	 * Sets payment id as currently selected one to be used for order payment.
	 * 
	 * @param pProfile
	 * @param pPayId
	 */
	public static void setCurrentlySelectedParentPaymentIdToProfile(Profile pProfile, String pPayId){
		setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_PAYMENT_SELECTED, pPayId);
	}

	/**
	 * Retrieve VP user token from profile.
	 * @param pProfile
	 * @return
	 */
	public static String getVPUserToken(Profile pProfile){
		return (String)getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_TOKEN);
	}
	
	/**
	 * Retrieve VP user type (Parent or Child) from profile.
	 * @param pProfile
	 * @return
	 */
	public static String getVPUserType(Profile pProfile){
		return (String)getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_TYPE);
	}

	/**
	 * Retrieve VP checkout property value from profile.
	 * @param pProfile
	 * @return true, if VP checkout is used, false otherwise.
	 */
	public static Boolean getVPCheckout(Profile pProfile){
		return (Boolean)getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_CHECKOUT);
	}

	/**
	 * Retrieve VP user name from profile.
	 * @param pProfile
	 * @return
	 */
	public static String getVPUserName(Profile pProfile){
		return (String)getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_USER_NAME);
	}

	/**
	 * Checks if VP checkout is used or not.
	 * @param pProfile
	 * @return true, if VP checkout is used, false otherwise.
	 */
	public static boolean isVirtualPiggyCheckout(Profile pProfile){
		if(pProfile != null){
			Boolean bChkout = getVPCheckout(pProfile);
			String sToken = getVPUserToken(pProfile);
			if(bChkout.booleanValue() && !StringUtils.isEmpty(sToken)) return true;
		}
		return false;
	}
	
	/**
	 * Get the List of children from profile.
	 * 
	 * @param pProfile
	 * @return List of children.
	 */
	public static List getChildren(Profile pProfile){
		if(pProfile != null){
			return (List)getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_CHILDREN);
		}
		return null;

	}
	
	/**
	 * Checks if the authenticated user is Child or not.
	 * @param pProfile
	 * @return true, if child. False, otherwise.
	 */
	public static boolean isVirtualPiggyChildLogin(Profile pProfile){
		return doesUserTypeMatch(pProfile,VPProfileConstants.PROPERTY_VALUE_VP_USER_TYPE_CHILD);
	}

	/**
	 * Checks if the authenticated user is Parent or not.
	 * @param pProfile
	 * @return true, if parent. False, otherwise.
	 */
	public static boolean isVirtualPiggyParentLogin(Profile pProfile){
		return doesUserTypeMatch(pProfile, VPProfileConstants.PROPERTY_VALUE_VP_USER_TYPE_PARENT);
	}
	
	/**
	 * Set profile property for selected child-id by the parent.
	 * 
	 */
	public static void setSelectedChildToProfile(Profile pProfile, String pSelChildId){
		if(pProfile != null){
			setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_CHILD_SELECTED, pSelChildId);
		}
	}
	
	/**
	 * Get profile property for selected child-id by the parent.
	 * 
	 */
	public static String getSelectedChildFromProfile(Profile pProfile){
		if(pProfile != null){
			return (String)getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_CHILD_SELECTED);
		}
		return null;
	}
	
	/**
	 * Set profile property for Parent's Ship Address Selection 
	 * 
	 */
	public static void setParentShipAddressSelected(Profile pProfile, String isParentShipAddressSelected){
		if(pProfile != null){
			setProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_PARENT_SHIP_ADDRESS_SELECTED, isParentShipAddressSelected);
		}
	}
	
	/**
	 * Get profile property for Parent's Ship Address Selection 
	 * 
	 */
	public static String getParentShipAddressSelected(Profile pProfile){
		if(pProfile != null){
			return (String) getProfileProperty(pProfile, VPProfileConstants.PROPERTY_NAME_VP_PARENT_SHIP_ADDRESS_SELECTED);
		}
		return null;
	}

	private static boolean doesUserTypeMatch(Profile pProfile, String pUserType){
		if(pProfile != null && !StringUtils.isEmpty(pUserType)){
			String sUsrType = getVPUserType(pProfile);
			if(pUserType.equalsIgnoreCase(sUsrType)){
				return true;
			}
		}
		return false;
	}

}
