package com.virtualpiggy.integration.services;

import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.nucleus.ServiceException;

/** This class maintains configuration required to connect to 
 * VirtualPiggy web services.
 * 
 * @author Tarun
 *
 */
public class VirtualPiggyServiceConfiguration extends GenericService {

	private String mMerchantUser;
	private String mMerchantPass;
	private String mMerchantId;
	private String mAPIkey;
	private String mTrSrvcServerURL;
	private boolean mOneStepPaymentMerchant = false;
	private boolean mMerchantCapturesPayment = false;
	
	
	public boolean getMerchantCapturesPayment() {
		return mMerchantCapturesPayment;
	}
	
	public void setMerchantCapturesPayment(boolean pMerchantCapturesPayment) {
		this.mMerchantCapturesPayment = pMerchantCapturesPayment;
	}
	
	public boolean isOneStepPaymentMerchant() {
		return mOneStepPaymentMerchant;
	}
	
	public void setOneStepPaymentMerchant(boolean pOneStepPaymentMerchant) {
		this.mOneStepPaymentMerchant = pOneStepPaymentMerchant;
	}
	
	public String getMerchantUser() {
		return mMerchantUser;
	}
	
	public void setMerchantUser(String pMerchantUser) {
		this.mMerchantUser = pMerchantUser;
	}
	
	public String getMerchantPass() {
		return mMerchantPass;
	}
	
	public void setMerchantPass(String pMerchantPass) {
		this.mMerchantPass = pMerchantPass;
	}
	
	public String getMerchantId() {
		return mMerchantId;
	}
	
	public void setMerchantId(String pMerchantId) {
		this.mMerchantId = pMerchantId;
	}
	
	public String getAPIkey() {
		return mAPIkey;
	}
	
	public void setAPIkey(String pAPIkey) {
		this.mAPIkey = pAPIkey;
	}
	
	public String getTrSrvcServerURL() {
		return mTrSrvcServerURL;
	}
	
	public void setTrSrvcServerURL(String pTrSrvcServerURL) {
		this.mTrSrvcServerURL = pTrSrvcServerURL;
	}
	
	@Override
	public void doStartService() throws ServiceException {
		super.doStartService();
		if (StringUtils.isEmpty(getMerchantUser())
				|| StringUtils.isEmpty(getMerchantPass())
				|| StringUtils.isEmpty(getMerchantId())
				|| StringUtils.isEmpty(getAPIkey())
				|| StringUtils.isEmpty(getTrSrvcServerURL())) {
			throw new ServiceException(
					"Please configure properties of com.virtualpiggy.integration.services.VirtualPiggyServiceConfiguration component before using it.");
		}
	}
}
