    <%--
      This gadget renders the login form during the checkout process
        Page consists of 3 tabs:
            - Returning Customer
            - New Customer
                - Continue without an Account
            --%>

        <dsp:page>

        <dsp:importbean bean="/atg/dynamo/droplet/Compare" />
        <dsp:importbean bean="/atg/userprofiling/B2CProfileFormHandler" />
        <dsp:importbean bean="/atg/userprofiling/Profile" />
        <dsp:importbean bean="/atg/userprofiling/PropertyManager" />
        <dsp:importbean bean="/OriginatingRequest" var="originatingRequest" />

        <dsp:form id="atg_store_checkoutLoginForm" formid="checkoutloginform"
        action="${originatingRequest.requestURI}" method="post">
        <div class="three_col_wrap">
        <%-- show form errors --%>
        <dsp:include page="/myaccount/gadgets/myAccountErrorMessage.jsp">
        <dsp:param name="formHandler" bean="B2CProfileFormHandler" />
        <dsp:param name="errorMessageClass" value="errorMessage"/>
        </dsp:include>
        <div id="atg_store_checkoutLogin">

        <%-- Exsisting customer tab --%>
        <div class="atg_store_checkoutLogin" id="atg_store_returningCustomerLogin">
        <h2>
        <span><fmt:message key="login.returningCustomer" /> </span>
        </h2>
        <div class="atg_store_register">
        <fieldset class="enter_info atg_store_havePassword">
        <div class="select_login_type">
        <dsp:droplet name="Compare">
        <dsp:param bean="Profile.securityStatus" name="obj1" />
        <dsp:param bean="PropertyManager.securityStatusCookie"
        name="obj2" />
        <dsp:oparam name="equal">
        <dsp:input type="radio" bean="B2CProfileFormHandler.checkoutLoginOption"
        checked="true" name="returning" id="atg_store_checkoutLoginTypeInput2"
        value="continueexistinguser" />
        </dsp:oparam>
        <dsp:oparam name="lessthan">
        <dsp:input type="radio" bean="B2CProfileFormHandler.checkoutLoginOption"
        checked="false" name="returning" id="atg_store_checkoutLoginTypeInput2"
        value="continueexistinguser" />
        </dsp:oparam>
        </dsp:droplet>

        <label for="atg_store_checkoutLoginTypeInput2">
        <fmt:message key="myaccount_login.existingUserLogin" />
        </label>
        </div>
        <div class="hid">
        <ul class="atg_store_basicForm">
        <li>

        <label>
        <fmt:message key="common.loginEmailAddress" />
        <span class="required">*</span>
        </label>
        <dsp:input type="text" bean="B2CProfileFormHandler.emailAddress" name="email"
        beanvalue="Profile.login" id="atg_store_emailInput" />
        </li>
        <li>
        <label for="atg_store_passwordInput">
        <fmt:message key="common.loginPassword" />
        <span class="required">*</span>
        </label>
        <dsp:input bean="B2CProfileFormHandler.value.password"
        type="password" name="password" id="atg_store_passwordInput" value="" />

        <p class="info_link atg_store_forgetPassword">
        <dsp:a page="/myaccount/passwordReset.jsp" title="${forgotPasswordTitle}">
        <fmt:message key="common.button.passwordResetText" />
        </dsp:a>
        </p>

        <fmt:message var="forgotPasswordTitle" key="checkout_checkoutLogin.forgotPasswordTitle" />
        </li>
        </ul>
        <div class="atg_store_formFooter">
        <div class="atg_store_formKey">
        <p class="required">
        <fmt:message key="login.requiredField" />
        </p>
        </div>
        </div>
        </div>
        </fieldset>

        <div class="atg_store_loginOptionSeparator ">
        <fmt:message key="login.or" />
        &gt;
        </div>
        </div>
        </div>

        <%-- New customer tab --%>
        <div class="atg_store_checkoutLogin" id="atg_store_newCustomerLogin">
        <h2>
        <span class="open"><fmt:message key="login.newCustomer" /> </span>
        </h2>
        <div class="atg_store_register">
        <fieldset class="enter_info atg_store_noPassword">
        <div class="select_login_type">
        <dsp:droplet name="Compare">
        <dsp:param bean="Profile.securityStatus" name="obj1" />
        <dsp:param bean="PropertyManager.securityStatusCookie" name="obj2" />
        <dsp:oparam name="equal">
        <dsp:getvalueof var="checkoutoption" param="checkoutoption"/>
        <dsp:getvalueof var="isCreateNewUserChecked" value="false"/>
        <c:if test="${checkoutoption == 'createnewuser'}">
        <dsp:getvalueof var="isCreateNewUserChecked" value="true"/>
        </c:if>
        <dsp:input type="radio" bean="B2CProfileFormHandler.checkoutLoginOption"
        name="returning" checked="${isCreateNewUserChecked}" id="atg_store_checkoutLoginTypeInput1"
        value="createnewuser" />
        </dsp:oparam>
        <dsp:oparam name="lessthan">
        <dsp:input type="radio" bean="B2CProfileFormHandler.checkoutLoginOption"
        name="returning" checked="true" id="atg_store_checkoutLoginTypeInput1"
        value="createnewuser" />
        </dsp:oparam>
        </dsp:droplet>

        <label for="atg_store_checkoutLoginTypeInput1">
        <fmt:message key="checkout_billingLogin.registration" />
        </label>

        </div>
        <div class="hid">
        <ul class="atg_store_basicForm">
        <li>
        <label>
        <fmt:message key="common.email" />
        <span class="required">*</span>
        </label>

        <dsp:input type="text" bean="B2CProfileFormHandler.newCustomerEmailAddress"
        id="atg_store_emailInput" />

        <p class="info_link atg_store_forgetPassword">
        <a href="#"> <fmt:message key="checkout_checkoutLogin.userWithNoPassword" /> </a>
        </p>
        </li>
        </ul>

        <div class="atg_store_formFooter">
        <div class="atg_store_formKey">
        <p class="required">
        <fmt:message key="login.requiredField" />
        </p>
        </div>
        </div>
        </div>
        </fieldset>
        <div class="atg_store_loginOptionSeparator">
        <fmt:message key="login.or" />
        &gt;
        </div>

        </div>
        </div>

        <%-- Anonymous customer tab --%>
        <div class="atg_store_checkoutLogin" id="atg_store_anonCustomerLogin">
        <h2>
        <span class="open"><fmt:message key="login.continueWithoutanAccount" /> </span>
        </h2>
        <div class="atg_store_register">
        <fieldset class="enter_info atg_store_refusePassword">
        <div class="select_login_type">
        <dsp:input type="radio" bean="B2CProfileFormHandler.checkoutLoginOption"
        name="returning" checked="false" id="atg_store_checkoutLoginTypeInput3"
        value="continueanonymous" />
        <label for="atg_store_checkoutLoginTypeInput3">
        <fmt:message key="checkout_checkoutLogin.userWithNoAccount" />
        </label>
        </div>

        </fieldset>
        </div>
        </div>

        <div><h2>&nbsp;</h2></div>
        <%-- Virtual Piggy Login Option --%>
        <dsp:getvalueof var="vpUsed" vartype="java.lang.String"
        bean="/atg/userprofiling/Profile.isVirtualPiggyCheckout"/>
        <dsp:getvalueof var="vpUserName" vartype="java.lang.String"
        bean="/atg/userprofiling/ProfileFormHandler.value.vpUserName"/><%--/atg/userprofiling/Profile.virtualPiggyUsername --%>
        <dsp:getvalueof var="vpToken" vartype="java.lang.String"
        bean="/atg/userprofiling/Profile.virtualPiggyUserToken"/>
        <dsp:getvalueof var="vpUserType" vartype="java.lang.String"
        bean="/atg/userprofiling/Profile.virtualPiggyUserType"/>
        <%-- show form errors --%>
        <dsp:getvalueof var="formExceptions" vartype="java.lang.Object"
        bean="/atg/userprofiling/ProfileFormHandler.formExceptions"/>
        <c:if test="${not empty formExceptions}">
        <c:forEach var="formException" items="${formExceptions}">
        <div style="text-align:left; font-weight:bold; font-size:11px;">
        ${formException.message}
        </div>
        </c:forEach>
        </c:if>
        <div class="atg_store_checkoutLogin atg_store_loginMethod" id="atg_store_vpCustomerLogin" style="background:
        black;">
        <h2>
        <img src="/images/storefront/PaymentSolution-SM.png"/><br>
        <span class="open">Oink Checkout</span>
        </h2>
        <div class="atg_store_register">
        <%-- <dsp:form id="atg_store_checkoutLoginForm" formid="vpcheckoutlogin" action="${originatingRequest.requestURI}" method="post"> --%>
        <dsp:input bean="/atg/userprofiling/ProfileFormHandler.parentLoginSuccessURL" type="hidden"
        value="selectVirtualPiggyChild.jsp"/>
        <dsp:input bean="/atg/userprofiling/ProfileFormHandler.loginSuccessURL" type="hidden" value="shipping.jsp"/>
        <dsp:input bean="/atg/userprofiling/ProfileFormHandler.logoutSuccessURL" type="hidden" value="shipping.jsp"/>
        <dsp:input bean="/atg/userprofiling/ProfileFormHandler.loginErrorURL" type="hidden"
        beanvalue="/OriginatingRequest.requestURI"/>
        <dsp:input bean="/atg/userprofiling/ProfileFormHandler.checkoutLoginOption" type="hidden"
        value="continueanonymous"/>

        <fieldset class="enter_info atg_store_havePassword">
        <div>
        <ul class="atg_store_basicForm">
        <li>
        <label>
        User
        <span class="required">*</span>
        </label>
        <dsp:input type="text" bean="/atg/userprofiling/ProfileFormHandler.value.vpUserName" name="vpUserName"
        value="${vpUserName}"/>
        </li>
        <li>
        <label for="atg_store_passwordInput">
        Password
        <span class="required">*</span>
        </label>
        <dsp:input bean="/atg/userprofiling/ProfileFormHandler.value.vpPassword" type="password" name="vpPassword"
        id="atg_store_passwordInput" value="" />
        </li>
        </ul>
        </div>
        <div class="atg_store_formFooter">
        <div class="atg_store_formActions">
        <dsp:input src="/images/storefront/Button_vpiggy_SM3.png"
        bean="/atg/userprofiling/ProfileFormHandler.VPLoginDuringCheckout" type="image" value=""/>
        </div>
        </div>
        </fieldset>
        </div>
        </div>
        </div>
        </div>

        <div class="atg_store_formFooter atg_store_checkoutFormFooter">
        <div class="atg_store_formActions">
        <dsp:input bean="B2CProfileFormHandler.loginSuccessURL" type="hidden" value="shipping.jsp" />
        <dsp:input bean="B2CProfileFormHandler.logoutSuccessURL" type="hidden" value="shipping.jsp"/>
        <dsp:input bean="B2CProfileFormHandler.loginErrorURL" type="hidden" beanvalue="/OriginatingRequest.requestURI"
        />

        <fmt:message var="continueButtonText" key="common.button.continueText" />
        <span class="atg_store_basicButton">
        <dsp:input bean="B2CProfileFormHandler.loginDuringCheckout" type="submit" alt="Login"
        value="${continueButtonText}"/>
        </span>

        </div>
        </div>
        </dsp:form>

        </dsp:page>
        <%-- @version $Id: //hosting-blueprint/B2CBlueprint/version/9.4/Storefront/j2ee/store.war/checkout/gadgets/checkoutLogin.jsp#1 $$Change: 652444 $--%>
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																											      