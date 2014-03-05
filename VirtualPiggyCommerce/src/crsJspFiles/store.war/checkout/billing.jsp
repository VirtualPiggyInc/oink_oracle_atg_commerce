<%--
  This page includes the gadgets for the billing page during the checkout process
  --%>

    <dsp:page>
    <cbp:pageContainer divId="atg_store_cart"
    index="false"
    follow="false"
    bodyClass="atg_store_pageBilling">
    <jsp:body>
        <div id="atg_store_checkout">
        <cbp:checkoutContainer currentStage="billing"
        titleKey="checkout_step.billing"
        showOrderSummary="false">
        <jsp:body>
            <dsp:include page="gadgets/billingForm.jsp" flush="true"/>
        </jsp:body>
        </cbp:checkoutContainer>
        </div>
    </jsp:body>
    </cbp:pageContainer>
    </dsp:page>
    <%-- @version $Id: //hosting-blueprint/B2CBlueprint/version/9.4/Storefront/j2ee/store.war/checkout/billing.jsp#1 $$Change: 652444 $--%>
