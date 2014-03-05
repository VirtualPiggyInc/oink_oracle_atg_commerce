    <%--
        This page renders the billing form
        --%>

        <dsp:page>
        <dsp:importbean bean="/atg/dynamo/droplet/Compare"/>
        <dsp:importbean bean="/atg/store/StoreConfiguration" />
        <dsp:importbean bean="/atg/store/order/purchase/BillingFormHandler" />
        <dsp:importbean bean="/atg/store/droplet/EnsureCreditCard"/>
        <dsp:importbean bean="/atg/commerce/order/purchase/ShippingGroupDroplet"/>
        <dsp:importbean bean="/atg/commerce/ShoppingCart"/>
        <dsp:importbean bean="/atg/userprofiling/Profile"/>
        <dsp:importbean bean="/atg/userprofiling/PropertyManager"/>
        <dsp:importbean bean="/OriginatingRequest" var="originatingRequest"/>
        <dsp:importbean bean="/atg/store/order/purchase/CheckoutOptionSelections"/>

        <dsp:getvalueof var="isTransient" bean="Profile.transient"/>
        <dsp:getvalueof var="checkoutOption" vartype="java.lang.String" bean="CheckoutOptionSelections.checkoutOption"/>

        <dsp:droplet name="ShippingGroupDroplet">
        <dsp:param name="createOneInfoPerUnit" value="true"/>
        <dsp:param name="clearShippingInfos" value="true"/>
        <dsp:param name="shippingGroupTypes" value="hardgoodShippingGroup"/>
        <dsp:param name="initShippingGroups" value="true"/>
        <dsp:param name="initBasedOnOrder" value="true"/>
        <dsp:oparam name="output"/>
        </dsp:droplet>

        <dsp:droplet name="EnsureCreditCard">
        <dsp:param name="order" bean="ShoppingCart.current"/>
        </dsp:droplet>


        <fmt:message var="submitFieldText" key="common.button.reviewOrderText"/>


        <dsp:getvalueof var="isVPChkout" bean="Profile.isVirtualPiggyCheckout"/>
        <c:choose>
        <c:when test='${isVPChkout}'>
        <dsp:droplet name="/atg/commerce/order/purchase/RepriceOrderDroplet">
        <dsp:param name="pricingOp" value="ORDER_TOTAL"/>
        </dsp:droplet>
        <fmt:message key="checkout_title.checkout" var="title"/>
        <crs:checkoutContainer currentStage="billing"
        title="${title}">
        <div class="atg_store_main">
        <%-- show form errors --%>
        <dsp:getvalueof var="formExceptions" vartype="java.lang.Object"
        bean="/com/virtualpiggy/integration/purchase/VirtualPiggyPaymentGroupFormHandler.formExceptions"/>
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

        <dsp:droplet name="/com/virtualpiggy/integration/purchase/VirtualPiggyPaymentInfoDroplet">
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
        <dsp:select
        bean="/com/virtualpiggy/integration/purchase/VirtualPiggyPaymentGroupFormHandler.value.virtualPiggyPaymentSelect"
        nodefault="true">
        <dsp:option value="">Select a payment account.</dsp:option>
        <%-- paymentd select option --%>
        <dsp:droplet name="/atg/dynamo/droplet/ForEach">
        <dsp:param name="array" param="paymentInfoList"/>
        <dsp:oparam name="output">
        <dsp:getvalueof var="payid" param="element.paymentId"/>
        <c:choose>
        <c:when test="${selPaymentId==payid}">
        <dsp:option value='${payid}' selected="true"><dsp:valueof param="element.name"/>(<dsp:valueof
        param="element.type"/>)</dsp:option>
        </c:when>
        <c:otherwise>
        <dsp:option value='${payid}' selected="false"><dsp:valueof param="element.name"/>(<dsp:valueof
        param="element.type"/>)</dsp:option>
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
        <dsp:input
        bean="/com/virtualpiggy/integration/purchase/VirtualPiggyPaymentGroupFormHandler.applyPaymentGroupsSuccessURL"
        type="hidden" value="confirm.jsp"/>
        <dsp:input
        bean="/com/virtualpiggy/integration/purchase/VirtualPiggyPaymentGroupFormHandler.applyPaymentGroupsErrorURL"
        type="hidden" beanvalue="/OriginatingRequest.requestURI"/>
        <dsp:input bean="/com/virtualpiggy/integration/purchase/VirtualPiggyPaymentGroupFormHandler.applyPaymentGroups"
        id="atg_store_billingButton" type="submit" value="Continue"/>
        </span>
        </div>
        </div>
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
        <%-- Show Form Errors --%>
        <dsp:include page="checkoutErrorMessages.jsp">
        <dsp:param name="formhandler" bean="BillingFormHandler"/>
        <dsp:param name="submitFieldText" value="${submitFieldText}"/>
        </dsp:include>
        <dsp:form id="atg_store_checkoutBilling"
        formid="atg_store_checkoutBilling"
        action="${originatingRequest.contextPath}/checkout/billing.jsp"
        method="post">
        <div class="atg_store_checkoutOption" id="atg_store_checkoutOptionArea">
        <div class="title_area">
        <h2><fmt:message key="checkout_billing.billingInformation" /></h2>
        </div>

        <dsp:include page="onlineCredit.jsp" flush="true"/>

        <input id="cntFlag" type="hidden" value="select" />

        <%-- Retrieve saved credit cards --%>
        <dsp:getvalueof var="creditCards" vartype="java.lang.Object" bean="Profile.creditCards"/>

        <c:if test="${!empty creditCards}">
        <%-- Get the last user's choice whether profile's saved credit cards to use or
                 new credit card to create --%>
        <dsp:getvalueof var="usingProfileCreditCard" bean="BillingFormHandler.usingProfileCreditCard"/>

        <%-- Tab with a list of saved credit cards and addresses --%>
        <fieldset class="atg_store_savedCreditCard">
        <legend class="atg_store_savedCreditCardTabs">
        <label for="saved_credit_card_and_address">
        <dsp:input bean="BillingFormHandler.usingProfileCreditCard" name="returning" value="true"
        checked="${usingProfileCreditCard}" id="saved_credit_card_and_address" type="radio" iclass="radio" />
        <fmt:message key="checkout_billing.chooseSavedPaymentInfor" />
        </label>
        </legend>

        <dsp:include page="creditCardSelect.jsp" flush="true" />

        <dsp:include page="savedCreditCards.jsp" flush="true" />
        </fieldset>
        </c:if>

        <fieldset class="atg_store_newCreditCard">
        <legend class="atg_store_newCreditCardTabs">
        <label for="atg_store_newCreditCardSelect">
        <dsp:input id="atg_store_newCreditCardSelect" type="radio" bean="BillingFormHandler.usingProfileCreditCard"
        name="returning" value="false" checked="${!empty creditCards ? !usingProfileCreditCard : 'true'}"
        iclass="radio"/>
        <fmt:message
        key="${!empty creditCards ? 'checkout_billing.enterAnotherPayment' : 'checkout_billing.enterAnotherPayment'}"/>
        </label>
        </legend>

        <%-- New credit card form --%>
        <fieldset class="atg_store_creditCardForm">
        <dsp:include page="creditCardForm.jsp" flush="true">
        <c:if test="${isTransient && checkoutOption != 'createnewuser'}">
        <dsp:param value="${false}" name="showSaveCheckbox"/>
        </c:if>
        </dsp:include>
        </fieldset>

        <%-- Show this only if we have saved secondary addresses --%>
        <dsp:getvalueof var="secondaryAddresses" vartype="java.lang.Object" bean="Profile.secondaryAddresses"/>
        <dsp:getvalueof var="usingSavedAddress" bean="BillingFormHandler.usingSavedAddress"/>

        <c:if test="${not empty secondaryAddresses}">
        <fieldset class="atg_store_billingAddresses">
        <div>
        <label for="atg_store_savedAddress">
        <dsp:input bean="BillingFormHandler.usingSavedAddress" name="address" checked="${usingSavedAddress}"
        value="true" id="atg_store_savedAddress" type="radio" iclass="radio" />
        <fmt:message key="checkout_billing.useSavedAddresses"/>
        </label>
        </div>

        <dsp:include page="billingAddressSelect.jsp" flush="true"/>
        </fieldset>
        </c:if>

        <dsp:include page="billingAddressAdd.jsp" flush="true"/>
        </fieldset>

        <%-- Gift certificate input --%>
        <fieldset class="atg_store_giftCertificate">
        <dsp:include page="giftCertificate.jsp" flush="true" />
        </fieldset>

        </div>
        <%-- Action buttons --%>
        <fieldset class="atg_store_checkoutContinue">
        <dsp:include page="billingFormControls.jsp" flush="true" />
        </fieldset>

        <dsp:droplet name="Compare">
        <dsp:param bean="Profile.securityStatus" name="obj1" converter="number"/>
        <dsp:param bean="PropertyManager.securityStatusLogin" name="obj2" converter="number"/>
        <dsp:oparam name="lessthan">
        <%-- Registration during checkout input --%>
        <fieldset class="atg_store_checkoutRegistration">
        <dsp:include page="billingLogin.jsp" flush="true"/>
        </fieldset>
        </dsp:oparam>
        </dsp:droplet> <%-- Compare on security status --%>

        <dsp:include page="/checkout/gadgets/checkoutOrderSummary.jsp" flush="true"/>

        </dsp:form>
        </c:otherwise>
        </c:choose>
        </dsp:page>
        <%-- @version $Id: //hosting-blueprint/B2CBlueprint/version/9.4/Storefront/j2ee/store.war/checkout/gadgets/billingForm.jsp#1 $$Change: 652444 $--%>