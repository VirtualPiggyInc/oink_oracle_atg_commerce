package com.virtualpiggy.integration.order.payment;

import java.util.List;

import atg.commerce.CommerceException;
import atg.commerce.order.PaymentGroup;
import atg.commerce.payment.PaymentManager;
import atg.commerce.states.PaymentGroupStates;
import atg.payment.PaymentStatus;

import com.virtualpiggy.integration.purchase.VirtualPiggyPurchaseProcessHelper;
import com.virtualpiggy.integration.services.VirtualPiggyServiceConfiguration;

public class VirtualPiggyPaymentManager extends PaymentManager {

	private VirtualPiggyServiceConfiguration VPConfiguration;

	private PaymentGroupStates paymentGroupStates;

	/**
	 * If One Step Merchant and there are no approvals required, then Settle The Payment. 
	 */
	public void postProcessAuthorize(PaymentGroup pPaymentGroup,
			PaymentStatus pStatus, double pAmount) throws CommerceException {

		vlogDebug("Entering Post ProcessAuthorize Method");
		super.postProcessAuthorize(pPaymentGroup, pStatus, pAmount);

		if (pPaymentGroup instanceof VirtualPiggyPayment) {
			VirtualPiggyPaymentStatus pgStatus = (VirtualPiggyPaymentStatus) pStatus;
			vlogDebug("One Step Merchant ??: " + getVPConfiguration().isOneStepPaymentMerchant());
			if (getVPConfiguration().isOneStepPaymentMerchant()) {	
				vlogDebug("One Step Merchant");
				vlogDebug("VP Transaction Status : "+ pgStatus.getVpTransactionStatus());
				if (! VirtualPiggyPaymentConstants.PAYMENT_TRANSACTION_STATUS_APPROVAL_PENDING
						.equalsIgnoreCase(pgStatus.getVpTransactionStatus())) {
					vlogDebug("No Approval Pending.. So Debit Status..");
					VirtualPiggyPayment pg = (VirtualPiggyPayment) pPaymentGroup;

					// VirtualPiggyPayment pg = (VirtualPiggyPayment) pPaymentGroup;
					addDebitStatusToPaymentGroup(pg);
				}

			}
		}

	}

	/**
	 * This method adds the debit status to Payment Group and mark Payment Group as SETTLED
	 * 
	 * @param pPayGroup
	 */
	private void addDebitStatusToPaymentGroup(VirtualPiggyPayment pPayGroup) {
		// add debit status and mark the payment group state as Settled.
		VirtualPiggyPaymentStatus vpDebitStatus = new VirtualPiggyPaymentStatus();
		List authStatuses = pPayGroup.getAuthorizationStatus();
		VirtualPiggyPaymentStatus vpAuthStatus = null;
		if (authStatuses != null && authStatuses.size() > 0) {
			vpAuthStatus = (VirtualPiggyPaymentStatus) authStatuses
					.get(authStatuses.size() - 1);
			VirtualPiggyPurchaseProcessHelper.copyStatusValues(vpAuthStatus,
					vpDebitStatus, vpAuthStatus.getVpTransactionStatus());
		}
		pPayGroup.addDebitStatus(vpDebitStatus);
		pPayGroup.setAmountDebited(pPayGroup.getAmount());
		pPayGroup.setState(getPaymentGroupStates().getStateValue(
				PaymentGroupStates.SETTLED));
	}

	public VirtualPiggyServiceConfiguration getVPConfiguration() {
		return VPConfiguration;
	}

	public void setVPConfiguration(
			VirtualPiggyServiceConfiguration VPConfiguration) {
		this.VPConfiguration = VPConfiguration;
	}

	public PaymentGroupStates getPaymentGroupStates() {
		return paymentGroupStates;
	}

	public void setPaymentGroupStates(PaymentGroupStates paymentGroupStates) {
		this.paymentGroupStates = paymentGroupStates;
	}

}
