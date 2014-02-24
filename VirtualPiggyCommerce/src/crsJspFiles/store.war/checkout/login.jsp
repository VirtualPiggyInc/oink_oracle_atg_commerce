<%--
  This page checks, if the user is already logged in. If this is the case, it displays whether Shipping or Confirmation page,
  depending on the 'express' parameter passed. Otherwise, this page will display the Login form.

  Required parameters:
    None.

  Optional parameters:
    express
      This flag specifies, if express checkout option has been selected. If true, the user will be redirected to the Confirmation page;
      otherwise the user will be redirected to the Shipping page.
--%>

<dsp:page>
  <dsp:importbean bean="/atg/store/profile/CheckoutProfileFormHandler"/>
  <dsp:importbean bean="/atg/userprofiling/ProfileFormHandler"/>
  
  <crs:pageContainer index="false" follow="false" bodyClass="atg_store_pageLogin atg_store_checkout">
    
    <jsp:attribute name="formErrorsRenderer">
      <%-- Show form errors above accessibility navigation. --%>
      <dsp:include page="/myaccount/gadgets/myAccountErrorMessage.jsp">
        <dsp:param name="formHandler" bean="CheckoutProfileFormHandler"/>
        <dsp:param name="errorMessageClass" value="errorMessage"/>
      </dsp:include>
    </jsp:attribute>
    
    <jsp:body>
      <fmt:message key="checkout_title.checkout" var="title"/>
      <crs:checkoutContainer currentStage="login" title="${title}">
        <jsp:body>          
          <dsp:importbean bean="/atg/userprofiling/Profile"/>
          
          <%-- show form errors --%>
          <dsp:include page="/myaccount/gadgets/myAccountErrorMessage.jsp">
            <dsp:param name="formHandler" bean="CheckoutProfileFormHandler"/>
            <dsp:param name="errorMessageClass" value="errorMessage"/>
          </dsp:include>

          <div id="atg_store_checkoutlogin">
            <%-- Exsisting customer tab --%>
            <div class="atg_store_checkoutLogin atg_store_loginMethod" id="atg_store_returningCustomerLogin">
              <h2>
                <span><fmt:message key="login.returningCustomer"/></span>
              </h2>
              <div class="atg_store_register">
                <dsp:form id="atg_store_checkoutLoginForm" formid="checkoutloginregistered" action="${pageContext.request.requestURI}" method="post">
                  <%-- Returning customers should be redirected to Shipping or Confirmation page, depending on the 'express' parameter. --%>
                  <dsp:input bean="CheckoutProfileFormHandler.loginSuccessURL" type="hidden"
                             value="${param['express'] ? 'confirm.jsp?expressCheckout=true' : 'shipping.jsp'}"/>
                  <dsp:input bean="CheckoutProfileFormHandler.loginErrorURL" type="hidden" value="${pageContext.request.requestURI}"/>
                  <fieldset class="enter_info atg_store_havePassword">
                    <div class="hid">
                      <ul class="atg_store_basicForm">
                        <%-- E-mail field. --%>
                        <li>
                          <label for="atg_store_emailInput">
                            <fmt:message key="common.email"/>
                            <span class="required">*</span>
                          </label>
                          <dsp:getvalueof var="anonymousStatus" vartype="java.lang.Integer" bean="/atg/userprofiling/PropertyManager.securityStatusAnonymous"/>
                          <dsp:getvalueof var="currentStatus" vartype="java.lang.Integer" bean="Profile.securityStatus"/>
                          <dsp:getvalueof var="currentEmail" vartype="java.lang.String" bean="Profile.email"/>
                          <%-- Prepopulate the e-mail field for auto-logged in users only. --%>
                          <dsp:input type="text" bean="CheckoutProfileFormHandler.emailAddress" name="email" value="${currentStatus > anonymousStatus ? currentEmail : ''}" id="atg_store_emailInput"/>
                        </li>
                        <%-- Password Field. --%>
                        <li>
                          <label for="atg_store_passwordInput">
                            <fmt:message key="common.loginPassword"/>
                            <span class="required">*</span>
                          </label>
                          <dsp:input bean="CheckoutProfileFormHandler.value.password" type="password" name="password" id="atg_store_passwordInput" value="" />
                          
                          <fmt:message var="forgotPasswordTitle" key="checkout_checkoutLogin.forgotPasswordTitle"/>
                          <dsp:a page="/myaccount/passwordReset.jsp" title="${forgotPasswordTitle}" iclass="info_link atg_store_forgetPassword">
                            <fmt:message key="common.button.passwordResetText"/>
                          </dsp:a>

                        </li>
                      </ul>
                    </div>
                    <%-- Login button. --%>
                    <div class="atg_store_formFooter">
                      <div class="atg_store_formActions">
                        <span class="atg_store_basicButton">
                          <fmt:message key="myaccount_login.submit" var="loginCaption"/>
                          <dsp:input bean="CheckoutProfileFormHandler.returningCustomer" id="atg_store_checkoutLoginButton" type="submit" value="${loginCaption}"/>
                        </span>
                      </div>
                    </div>
                  </fieldset>
                </dsp:form>
              </div>
            </div>

            <%-- New customer tab --%>
            <div class="atg_store_checkoutLogin atg_store_loginMethod" id="atg_store_newCustomerLogin">
              <h2>
                <span class="open"><fmt:message key="login.newCustomer"/></span>
              </h2>
              <div class="atg_store_register">
                <dsp:form id="atg_store_checkoutLoginForm" formid="checkoutloginnewuser" action="${pageContext.request.requestURI}" method="post">
                  <!--[if IE]><input type="text" style="display: none;" disabled="disabled" size="1" /><![endif]-->
                  <%-- New customers should be redirected to the Registration page. They will be redirected to Shipping later. --%>
                  <dsp:input bean="CheckoutProfileFormHandler.loginSuccessURL" type="hidden" value="registration.jsp"/>
                  <dsp:input bean="CheckoutProfileFormHandler.loginErrorURL" type="hidden" value="${pageContext.request.requestURI}"/>
                  <fieldset class="enter_info atg_store_noPassword">
                    <div class="hid">
                      <ul class="atg_store_basicForm">
                        <%-- E-mail field. --%>
                        <li>
                          <label for="atg_store_emailInputRegister">
                            <fmt:message key="common.email"/>
                            <span class="required">*</span>
                          </label>
                          <dsp:input type="text" bean="CheckoutProfileFormHandler.emailAddress" id="atg_store_emailInputRegister" />

                          <a class="info_link" href="javascript:void(0)" onclick="atg.store.util.openwindow('../company/privacyPolicyPopup.jsp', 'sizeChart', 500, 500)">
                            <fmt:message key="common.button.privacyPolicyText"/>
                          </a>
                        </li>
                      </ul>
                    </div>
                  </fieldset>
                  <%-- Register button. --%>
                  <div class="atg_store_formFooter">
                    <div class="atg_store_formActions">
                      <span class="atg_store_basicButton">
                        <fmt:message key="myaccount_registration.submit" var="createAccountCaption"/>
                        <dsp:input bean="CheckoutProfileFormHandler.newCustomer" id="atg_store_createMyAccountButton" type="submit" value="${createAccountCaption}"/>
                      </span>
                    </div>
                  </div>
                </dsp:form>
              </div>
            </div>

            <%-- Anonymous customer tab --%>
            <div class="atg_store_checkoutLogin atg_store_loginMethod" id="atg_store_anonCustomerLogin">
              <h2>
                <span class="open"><fmt:message key="checkout_login.button.anonymous"/></span>
              </h2>
              <div class="atg_store_register">
                <dsp:form id="atg_store_checkoutLoginForm" formid="checkoutloginanonymous" action="${pageContext.request.requestURI}" method="post">
                  <%-- Anonymous customers should be redirected directly to the Shipping page. --%>
                  <dsp:input bean="CheckoutProfileFormHandler.loginSuccessURL" type="hidden" value="shipping.jsp"/>
                  <dsp:input bean="CheckoutProfileFormHandler.loginErrorURL" type="hidden" value="${pageContext.request.requestURI}"/>
                  <fieldset>
                    <ul class="atg_store_basicForm">
                      <li>
                        <p><fmt:message key="checkout_login.description.anonymous"/></p>
                      </li>
                    </ul>
                  </fieldset>
                  <%-- Anonymous button. --%>
                  <div class="atg_store_formFooter">
                    <div class="atg_store_formActions">
                      <span class="atg_store_basicButton">
                        <fmt:message key="checkout_login.button.anonymous" var="anonymousCaption"/>
                        <dsp:input bean="CheckoutProfileFormHandler.anonymousCustomer" type="submit" value="${anonymousCaption}"/>
                      </span>
                    </div>
                  </div>
                </dsp:form>
              </div>
            </div>
            
			<DIV><H2>&nbsp;</H2></DIV>
                <%-- Virtual Piggy Login Option --%>
	<dsp:getvalueof var="vpUsed" vartype="java.lang.String" bean="/atg/userprofiling/Profile.isVirtualPiggyCheckout"/>
	<dsp:getvalueof var="vpUserName" vartype="java.lang.String" bean="ProfileFormHandler.value.vpUserName"/><%--/atg/userprofiling/Profile.virtualPiggyUsername --%>
	<dsp:getvalueof var="vpToken" vartype="java.lang.String" bean="/atg/userprofiling/Profile.virtualPiggyUserToken"/>
	<dsp:getvalueof var="vpUserType" vartype="java.lang.String" bean="/atg/userprofiling/Profile.virtualPiggyUserType"/>
	<%-- show form errors --%>
    <dsp:getvalueof var="formExceptions" vartype="java.lang.Object" bean="ProfileFormHandler.formExceptions"/>
	<c:if test="${not empty formExceptions}">
		<c:forEach var="formException" items="${formExceptions}">
			<div style="text-align:left; font-weight:bold; font-size:11px;">
				${formException.message}
			</div>
		</c:forEach>
	</c:if>	
	<%-- c:choose>
		<c:when test='${vpUsed == "true" && vpToken != ""}'>
		  <div style="text-align:left; font-weight:bold; font-size:12px;color:black;">
		  Currently authenticated as : ${vpUserName}&nbsp;|&nbsp;
		  <c:choose>
			<c:when test='${vpUserType == "Parent"}'>
		      <a href="selectVirtualPiggyChild.jsp">Continue</a>
			</c:when>
			<c:otherwise>
		      <a href="shipping.jsp">Continue</a>
			</c:otherwise>
		  </c:choose>
		  </div>
		</c:when>
	</c:choose--%>
  <div class="atg_store_checkoutLogin atg_store_loginMethod" id="atg_store_vpCustomerLogin">
    <h2>
      <img src="/crsdocroot/content/images/storefront/PaymentSolution-SM.png"/><br>
      <span class="open">Virtual Piggy Checkout</span>
    </h2>
    <div class="atg_store_register">
      <dsp:form id="atg_store_checkoutLoginForm" formid="vpcheckoutlogin" action="${originatingRequest.requestURI}" method="post">
        <dsp:input bean="ProfileFormHandler.parentLoginSuccessURL" type="hidden" value="selectVirtualPiggyChild.jsp"/>
        <dsp:input bean="ProfileFormHandler.loginSuccessURL" type="hidden" value="shipping.jsp"/>
        <dsp:input bean="ProfileFormHandler.logoutSuccessURL" type="hidden" value="shipping.jsp"/>
        <dsp:input bean="ProfileFormHandler.loginErrorURL" type="hidden" beanvalue="/OriginatingRequest.requestURI"/>
        <dsp:input bean="ProfileFormHandler.checkoutLoginOption" type="hidden" value="continueanonymous"/>
        
		<fieldset class="enter_info atg_store_havePassword">
          <div class="hid">
            <ul class="atg_store_basicForm">
              <li>
                <label>
                  User
                  <span class="required">*</span>
                </label>
				<dsp:input type="text" bean="ProfileFormHandler.value.vpUserName" name="vpUserName"
                    value="${vpUserName}"/>

              </li>
              <li>
                <label for="atg_store_passwordInput">
                  Password
                  <span class="required">*</span>
                </label>
                <dsp:input bean="ProfileFormHandler.value.vpPassword" type="password" name="vpPassword" id="atg_store_passwordInput" value="" />
              </li>
            </ul>
          </div>
          <div class="atg_store_formFooter">
            <div class="atg_store_formActions">
                <dsp:input src="/crsdocroot/content/images/storefront/Button_vpiggy_SM3.png" bean="ProfileFormHandler.VPLoginDuringCheckout" type="image" value=""/>
            </div>
          </div>
        </fieldset>
      </dsp:form>
    </div>
  </div>
          </div>
        </jsp:body>
      </crs:checkoutContainer>
    </jsp:body>
  </crs:pageContainer>
</dsp:page>
<%-- @version $Id: //hosting-blueprint/B2CBlueprint/version/10.1.2/Storefront/j2ee/store.war/checkout/login.jsp#1 $$Change: 713790 $--%>