<%-- 
  This page displays all necessary elements to make proper billing for the cart the user is checking out.
  First, this page applies all available store credits to the current order. If there are no enough store credits to pay for the whole order,
  this page would include gadgets to render a form with necessary input fields to select or create a credit card.
  Or it will just display 'Move to Confirm' button otherwise.

  Required parameters:
    None.

  Optional parameters:
    None.
--%>

<dsp:page>
  <dsp:importbean bean="/atg/commerce/ShoppingCart" />
  <dsp:importbean bean="/atg/store/order/purchase/BillingFormHandler" />
  <dsp:importbean bean="/atg/store/order/purchase/CouponFormHandler"/>
  <dsp:importbean bean="/atg/commerce/order/purchase/RepriceOrderDroplet"/>
  <dsp:importbean bean="/atg/commerce/ShoppingCart"/>
  <dsp:importbean bean="/atg/userprofiling/B2CProfileFormHandler"/>
  <dsp:importbean bean="/com/virtualpiggy/integration/purchase/VirtualPiggyPaymentGroupFormHandler"/>
  <dsp:importbean bean="/com/virtualpiggy/integration/purchase/VirtualPiggyPaymentInfoDroplet" />
  <dsp:importbean bean="/atg/userprofiling/Profile"/>
  <dsp:importbean bean="/atg/store/states/CheckoutProgressStates" var="checkoutProgressStates"/>  
  
  <c:set var="stage" value="billing"/>
  
   <%-- Set title for submit button --%>
  <fmt:message var="submitFieldText" key="common.button.continueText"/>

  <crs:pageContainer divId="atg_store_cart" index="false" follow="false" levelNeeded="BILLING" redirectURL="../cart/cart.jsp"
                     bodyClass="atg_store_pageBilling atg_store_checkout atg_store_rightCol">
                     
    <jsp:attribute name="formErrorsRenderer">
     <%-- 
       Display error messages from Billing and Coupon form handlers 
       above the accessibility navigation  
     --%>     
     <dsp:include page="/checkout/gadgets/checkoutErrorMessages.jsp">
       <dsp:param name="formHandler" bean="BillingFormHandler"/>
       <dsp:param name="submitFieldText" value="${submitFieldText}"/>
     </dsp:include>
     <dsp:include page="/checkout/gadgets/checkoutErrorMessages.jsp">
       <dsp:param name="formHandler" bean="CouponFormHandler"/>
       <dsp:param name="submitFieldText" value="${submitFieldText}"/>
     </dsp:include>  
     <dsp:include page="/checkout/gadgets/checkoutErrorMessages.jsp">
       <dsp:param name="formHandler" bean="B2CProfileFormHandler"/>
     </dsp:include> 
   </jsp:attribute>
          
    <jsp:body>
	  <dsp:getvalueof var="isVPChkout" bean="Profile.isVirtualPiggyCheckout"/>
	  <c:choose>
        <c:when test='${isVPChkout}'>
	        <dsp:droplet name="RepriceOrderDroplet">
	          <dsp:param name="pricingOp" value="ORDER_TOTAL"/>
	        </dsp:droplet>
		  <fmt:message key="checkout_title.checkout" var="title"/>
		  <crs:checkoutContainer currentStage="billing"
								 title="${title}">
		  <div class="atg_store_main">
			<%-- show form errors --%>
			<dsp:getvalueof var="formExceptions" vartype="java.lang.Object" bean="VirtualPiggyPaymentGroupFormHandler.formExceptions"/>
			<c:if test="${not empty formExceptions}">
				<c:forEach var="formException" items="${formExceptions}">
					<div style="text-align:center; font-weight:bold; font-size:11px;background-color:#ff9900;color:#ffffff;">
						${formException.message}
						<br>
						Internal Error associating Virtual Piggy Payment Group with Order. 
					</div>
				</c:forEach>
			</c:if>
			<div class="atg_store_generalMessage">
			
			<dsp:form id="vp_checkoutBilling" formid="vp_checkoutBilling"
					  action="${pageContext.request.requestURI}" method="post">

			<dsp:droplet name="VirtualPiggyPaymentInfoDroplet">
			  <dsp:oparam name="default">
					<c:set var="showForm" scope="request" value="true"/>
					<h3>
					  Payment Account associated with your Virtual Piggy Profile will be used to pay this order.
					</h3>
			  </dsp:oparam>
			  <dsp:oparam name="output">
					<c:set var="showForm" scope="request" value="true"/>
					<h3>
					  Select a payment account:
					</h3>
					<%-- loop over the payment list--%>
					<dsp:select bean="VirtualPiggyPaymentGroupFormHandler.value.virtualPiggyPaymentSelect" nodefault="true">
						<dsp:option value="">Select a payment account.</dsp:option>
					<%-- paymentd select option --%>
					<dsp:droplet name="/atg/dynamo/droplet/ForEach">
						<dsp:param name="array" param="paymentInfoList"/>
						<dsp:oparam name="output">
						  <dsp:getvalueof var="payid" param="element.paymentId"/>
						  <c:choose>
						   <c:when test="${selPaymentId==payid}">
							<dsp:option value='${payid}' selected="true"><dsp:valueof param="element.name"/>(<dsp:valueof param="element.type"/>)</dsp:option>
						   </c:when>
						   <c:otherwise>
							 <dsp:option value='${payid}' selected="false"><dsp:valueof param="element.name"/>(<dsp:valueof param="element.type"/>)</dsp:option>
						   </c:otherwise>
						  </c:choose>
						 </dsp:oparam>
					</dsp:droplet>
					</dsp:select>
			  </dsp:oparam>
			  <dsp:oparam name="error">
				<c:set var="showForm" scope="request" value="true"/>
			    <h3>${errorMsg}</h3>
			  </dsp:oparam>
			</dsp:droplet>
			<c:choose>
				<c:when test="${showForm == 'true'}">
					<div class="atg_store_formFooter">
					<div class="atg_store_formActions">
					  <span class="atg_store_basicButton">
					    <dsp:input bean="VirtualPiggyPaymentGroupFormHandler.applyPaymentGroupsSuccessURL" type="hidden" value="confirm.jsp"/>
					    <dsp:input bean="VirtualPiggyPaymentGroupFormHandler.applyPaymentGroupsErrorURL" type="hidden" beanvalue="/OriginatingRequest.requestURI"/>
					    <dsp:input bean="VirtualPiggyPaymentGroupFormHandler.applyPaymentGroups" id="atg_store_billingButton" type="submit" value="Continue"/>
					  </span>
					</div>
					</div>
					  <dsp:setvalue bean="CheckoutProgressStates.currentLevel" value="CONFIRM" />
				</c:when>
			</c:choose>
			</dsp:form>
			</div>
		  </div>
		  </crs:checkoutContainer>
			<%-- Order Summary --%>
			<dsp:include page="/checkout/gadgets/checkoutOrderSummary.jsp">
			  <dsp:param name="order" bean="/atg/commerce/ShoppingCart.current"/>
			  <dsp:param name="currentStage" value="billing"/>
			</dsp:include>		  
		</c:when>
		<c:otherwise>
    
      <%-- Apply available store credits to the order  --%>
      <dsp:setvalue bean="BillingFormHandler.applyStoreCreditsToOrder" value=""/>

      <%-- Reprice the order, we should now its exact cost to render the checkout summary area. --%>
      <dsp:getvalueof var="formError" vartype="java.lang.String" bean="BillingFormHandler.formError"/>
      <%-- Check, if there was an error in the component specified. --%>
      <c:if test='${formError == "true"}'>
        <%--
          This droplet invocates a 'repriceAndUpdateOrder' pipeline chain. It also constructs proper parameters object for this chain.
    
          Input parameters:
            pricingOp
              Pricing operation to be performed on the current order.
          --%>
        <dsp:droplet name="RepriceOrderDroplet">
          <dsp:param name="pricingOp" value="ORDER_TOTAL"/>
        </dsp:droplet>
      </c:if>

      <dsp:form id="atg_store_checkoutBilling" formid="atg_store_checkoutBilling"
                action="${pageContext.request.requestURI}" method="post">
        <dsp:param name="skipCouponFormDeclaration" value="true"/>
        <fmt:message key="checkout_title.checkout" var="title"/>
        <crs:checkoutContainer currentStage="${stage}" title="${title}">
          <jsp:attribute name="formErrorsRenderer">
            <%-- Display error messages from Billing and Coupon form handlers. --%>            
            <dsp:include page="/checkout/gadgets/checkoutErrorMessages.jsp">
              <dsp:param name="formHandler" bean="BillingFormHandler"/>
              <dsp:param name="submitFieldText" value="${submitFieldText}"/>
            </dsp:include>
            <dsp:include page="/checkout/gadgets/checkoutErrorMessages.jsp">
              <dsp:param name="formHandler" bean="CouponFormHandler"/>
              <dsp:param name="submitFieldText" value="${submitFieldText}"/>
            </dsp:include>   
            <dsp:include page="/checkout/gadgets/checkoutErrorMessages.jsp">
              <dsp:param name="formHandler" bean="B2CProfileFormHandler"/>
            </dsp:include>        
          </jsp:attribute>
          <jsp:body>
            <div id="atg_store_checkout" class="atg_store_main">
              <dsp:getvalueof var="order" vartype="atg.commerce.Order" bean="ShoppingCart.current"/>
              <%-- Don't offer enter credit card's info if order's total is covered by store credits. --%>
              <c:choose>
                <c:when test="${order.priceInfo.total > order.storeCreditsAppliedTotal}">
                  <%-- Store credits are not enough, render billing form. --%>
                  <dsp:include page="gadgets/billingForm.jsp"/>
                </c:when>
                <c:otherwise>
                  <%-- Order is paid by store credits, display its total and 'Continue' button. --%>
                  <crs:messageContainer id="atg_store_zeroBalance">                     
                    <h3>
                      <%-- Display 'Order's total is 0' message. --%>
                      <fmt:message key="checkout_billing.yourOrderTotal"/>
                      <dsp:include page="/global/gadgets/formattedPrice.jsp">
                        <dsp:param name="price" value="0"/>
                      </dsp:include>
                    </h3>
                    <p><fmt:message key="checkout_billing.yourOrderTotalMessage"/></p>
                    <dsp:input bean="BillingFormHandler.moveToConfirmSuccessURL" type="hidden" value="confirm.jsp"/>
                    <%-- Go to Confirm button. --%>
                    <span class="atg_store_basicButton">
                      <fmt:message var="caption" key="common.button.continueText"/>
                      <dsp:input bean="BillingFormHandler.billingWithStoreCredit" type="submit" value="${caption}"/>
                    </span>                    
                  </crs:messageContainer>     
                </c:otherwise>
              </c:choose>
            </div>
          </jsp:body>
        </crs:checkoutContainer>
        
        <%-- Order Summary --%>
        <dsp:include page="/checkout/gadgets/checkoutOrderSummary.jsp">
          <dsp:param name="order" bean="ShoppingCart.current"/>
          <dsp:param name="currentStage" value="${stage}"/>
        </dsp:include>
        
      </dsp:form>
	</c:otherwise>
	</c:choose>      
    </jsp:body>
  </crs:pageContainer>
</dsp:page>
<%-- @version $Id: //hosting-blueprint/B2CBlueprint/version/10.1.2/Storefront/j2ee/store.war/checkout/billing.jsp#1 $$Change: 713790 $--%>
