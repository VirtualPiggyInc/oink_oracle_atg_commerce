    <%-- This page includes a gadget that will check if the user is already logged in.
         If so, it redirects to the shipping page --%>

        <dsp:page>
            <dsp:importbean bean="/atg/dynamo/droplet/Compare"/>
            <dsp:importbean bean="/atg/userprofiling/Profile"/>
            <dsp:importbean bean="/atg/userprofiling/PropertyManager"/>

            <dsp:droplet name="Compare">
                <dsp:param bean="Profile.securityStatus" name="obj1"/>
                <dsp:param bean="PropertyManager.securityStatusLogin" name="obj2"/>
                <dsp:oparam name="equal">
                    <dsp:include page="shipping.jsp"/>
                </dsp:oparam>
                <dsp:oparam name="default">
                    <dsp:include page="loginPage.jsp"/>
                </dsp:oparam>
            </dsp:droplet>
        </dsp:page>
        <%-- @version $Id: //hosting-blueprint/B2CBlueprint/version/9.4/Storefront/j2ee/store.war/checkout/login.jsp#1 $$Change: 652444 $--%>
						   