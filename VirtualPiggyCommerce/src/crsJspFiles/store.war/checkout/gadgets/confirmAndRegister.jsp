<%--
  This gadget displays a 'Thank you' message at the end of the checkout progress.
  It also renders a form that allows the user to register.

  Required parameters:
    None.

  Optional parameters:
    None.
--%>

<dsp:page>
  <dsp:importbean bean="/atg/commerce/ShoppingCart"/>
  <dsp:importbean bean="/atg/store/order/purchase/BillingFormHandler" />
  <dsp:importbean bean="/atg/store/order/purchase/CouponFormHandler"/>
  <dsp:importbean var="originatingRequest" bean="/OriginatingRequest"/>
  <dsp:importbean bean="/atg/userprofiling/Profile"/>

  <dsp:getvalueof var="contextPath" vartype="java.lang.String" bean="/OriginatingRequest.contextPath"/>

  <%-- Retrieve the submit button text here in case we need to use it in an error message --%>
  <fmt:message key="common.button.continueText" var="submitText"/>

  <div id="atg_store_confirmAndRegister"> 
    <%-- 
      The submit button text is used in some error messages, so ensure 
      it is set as a param to be utilized in the error jsp. 
    --%>
    <dsp:param name="submitFieldText" value="${submitText}" />
    
    <%-- Show page errors. We're using Billing and Coupon form handlers here. --%>
    <dsp:include page="checkoutErrorMessages.jsp" >
      <dsp:param name="formHandler" bean="BillingFormHandler"/>
    </dsp:include>
    <dsp:include page="/checkout/gadgets/checkoutErrorMessages.jsp">
      <dsp:param name="formHandler" bean="CouponFormHandler"/>
    </dsp:include>

    <%-- 'Thank you' message area. --%>
    <div id="atg_store_confirmResponse">
      <h3><fmt:message key="checkout_confirmResponse.successTitle"/></h3>
      <%-- If Virtual Piggy Payment then check if status is pending approval else show store message --%>
      <%-- get order payment groups(0).type  --%>
      <dsp:getvalueof var="payGrpType" bean="/atg/commerce/ShoppingCart.last.paymentGroups[0].paymentMethod"/>
      <c:choose>
        <c:when test='${payGrpType == "virtualPiggyPayment"}'>
          <dsp:getvalueof var="vpTransStatus" bean="/atg/commerce/ShoppingCart.last.paymentGroups[0].authorizationStatus[0].vpTransactionStatus"/>
        </c:when>
        <c:otherwise>
          <dsp:getvalueof var="vpTransStatus" value=""/>
	    </c:otherwise>
      </c:choose>
      <c:choose>
        <c:when test='${vpTransStatus == "ApprovalPending"}'>
	      <p>
            <span>Your order <dsp:valueof bean="ShoppingCart.last.id"/> is saved and is pending parent approval. It will be processed when approved.</span>
	      </p>
        </c:when>
        <c:otherwise>
		  <p>
			<fmt:message key="checkout_confirmResponse.omsOrderId">
			  <fmt:param>
				<span><dsp:valueof bean="ShoppingCart.last.omsOrderId"/></span>
			  </fmt:param>
			</fmt:message>
		  </p>
	    </c:otherwise>
      </c:choose>

      <dsp:getvalueof var="confirmationEmail" bean="Profile.email"/>
      <c:if test="${not empty confirmationEmail}">
        <p>
          <fmt:message key="checkout_confirmResponse.emailText"/>
          <span><dsp:valueof value="${confirmationEmail}"/></span>
        </p>
      </c:if>

      <%-- Display link to just placed order. --%>
      <p>
        <fmt:message key="checkout_confirmResponse.printOrderText"/>
        <dsp:a page="/myaccount/orderDetail.jsp">
          <dsp:param name="orderId" bean="ShoppingCart.last.id"/>
          <fmt:message key="common.here"/>
        </dsp:a>
      </p>
    </div>

    <%-- Registration area. --%>
    <dsp:form method="post" action="${originatingRequest.requestURI}"
              id="atg_store_registration" formid="atg_store_registerForm">
      <h3><fmt:message key="checkout_confirmResponse.registerTitle"/></h3>

      <%-- Registration form --%>
      <dsp:include page="/myaccount/gadgets/registrationForm.jsp">
        <dsp:param name="formHandler" value="BillingFormHandler"/>
        <dsp:param name="email" value="${confirmationEmail}"/>
      </dsp:include>

      <%-- If registration succeeds, go to my profile page and welcome new customer there --%>
      <dsp:input bean="BillingFormHandler.registerAccountSuccessURL" type="hidden" value="${contextPath}/myaccount/profile.jsp"/>
      <%-- If registration fails, redisplay this page with errors shown --%>
      <dsp:input bean="BillingFormHandler.registerAccountErrorURL" type="hidden" value="${originatingRequest.requestURI}"/>

      <div class="atg_store_formActions">
        <span class="atg_store_basicButton">
          <dsp:input bean="BillingFormHandler.registerUser" type="submit" alt="${submitText}" value="${submitText}" />
        </span>
      </div>
    </dsp:form>
  </div>
</dsp:page>
<%-- @version $Id: //hosting-blueprint/B2CBlueprint/version/10.1.2/Storefront/j2ee/store.war/checkout/gadgets/confirmAndRegister.jsp#1 $$Change: 713790 $--%>