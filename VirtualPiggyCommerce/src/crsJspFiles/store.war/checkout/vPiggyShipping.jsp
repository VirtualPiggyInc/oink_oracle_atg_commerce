    <%--
      This page includes the gadgets for the shipping page for a single shipping group.
      (That is, all items will be shipped to the same shipping address)
    --%>

        <dsp:page>
        <cbp:pageContainer divId="atg_store_cart"
        index="false"
        follow="false">
        <jsp:attribute name="bodyClass">atg_store_pageShipping</jsp:attribute>

        <jsp:body>
            <dsp:importbean bean="/atg/userprofiling/Profile"/>
            <div id="atg_store_checkout">
            <%-- show form errors --%>
            <dsp:getvalueof var="formExceptions" vartype="java.lang.Object"
            bean="/atg/commerce/order/purchase/ShippingGroupFormHandler.formExceptions"/>
            <c:if test="${not empty formExceptions}">
            <c:forEach var="formException" items="${formExceptions}">
            <div style="text-align:center; font-weight:bold; font-size:11px;background-color:#ff9900;color:#ffffff;">
            <%--${formException.message}--%>
            <br>
            Internal Error associating Virtual Piggy Shipping Group with Order.
            </div>
            </c:forEach>
            </c:if>

            <div class="atg_store_generalMessage">

            <dsp:droplet name="/com/virtualpiggy/integration/purchase/VirtualPiggyShippingInfoDroplet">
            <dsp:param name="clearShippingInfos" value="true"/>
            <dsp:param name="clearShippingGroups" value="true"/>
            <dsp:param name="shippingGroupTypes" value="hardgoodShippingGroup"/>
            <dsp:param name="initShippingGroups" value="true"/>
            <dsp:param name="initShippingInfos" value="true"/>

            <dsp:oparam name="output">
            <h3>
            Your items will be shipped to address below:
            </h3>
            <ul align="left" style="width:100%; margin-top:10px; padding-left:3%;">

            <dsp:getvalueof var="defShippingGroupName"
            bean="/atg/commerce/order/purchase/ShippingGroupContainerService.defaultShippingGroupName"/>
            <dsp:droplet name="/atg/commerce/util/MapToArrayDefaultFirst">
            <dsp:param name="map" param="shippingGroups"/>
            <dsp:param name="defaultKey" value="${defShippingGroupName}"/>
            <dsp:oparam name="output">
            <dsp:getvalueof var="sortedArray" vartype="java.lang.Object" param="sortedArray"/>
            <c:forEach var="shippingAddressVar" items="${sortedArray}">
            <dsp:getvalueof var="shippingGroup" vartype="java.lang.Object" value="${shippingAddressVar}"/>
            <dsp:getvalueof var="shippingGroupValue" value="${shippingGroup.value.shippingAddress}"/>
            <li>
            <label> Name : <dsp:valueof value="${shippingGroupValue.firstName}"/> <dsp:valueof
            value="${shippingGroupValue.lastName}"/></label>
            </li>
            <li>&nbsp;</li>
            <li>
            <label> <dsp:valueof value="${shippingGroupValue.address1}"/> </label>
            </li>
            <li>
            <label> <dsp:valueof value="${shippingGroupValue.address2}"/> </label>
            </li>
            <li>
            <label> <dsp:valueof value="${shippingGroupValue.city}"/>, <label> <dsp:valueof
            value="${shippingGroupValue.state}"/> <dsp:valueof value="${shippingGroupValue.postalCode}"/></label>
            </li>
            <li>
            <dsp:valueof value="${shippingGroupValue.country}"/>
            </li>
            <li>&nbsp;</li>
            <li>
            <label> Phone : <dsp:valueof value="${shippingGroupValue.phoneNumber}"/> </label>
            </li>
            <li>&nbsp;</li>
            <li>
            <label> Email : <dsp:valueof bean="Profile.email"/> </label>
            </li>
            </c:forEach>
            </dsp:oparam>
            </dsp:droplet>
            </ul>
            <br>
            <dsp:form id="atg_store_checkoutShippingAddress" iclass="atg_store_checkoutOption"
            formid="atg_store_checkoutShippingAddress"
            action="${pageContext.request.requestURI}" method="post">
            <c:choose>
            <c:when test='${vpParentChild == "Parent"}'>
            <dsp:input bean="/atg/commerce/order/purchase/ShippingGroupFormHandler.applyShippingGroupsSuccessURL"
            type="hidden" value="shippingMethod.jsp"/>
            </c:when>
            <c:otherwise>
            <dsp:input bean="/atg/commerce/order/purchase/ShippingGroupFormHandler.applyShippingGroupsSuccessURL"
            type="hidden" value="billing.jsp"/>
            </c:otherwise>
            </c:choose>

            <dsp:input bean="/atg/commerce/order/purchase/ShippingGroupFormHandler.applyShippingGroupsErrorURL"
            type="hidden" beanvalue="/OriginatingRequest.requestURI"/>
            <div class="atg_store_formFooter">
            <div class="atg_store_formActions">
            <span class="atg_store_basicButton">
            <dsp:input bean="/atg/commerce/order/purchase/ShippingGroupFormHandler.applyShippingGroups"
            id="atg_store_shippingButton" type="submit" value="Continue"/>
            </span>
            </div>
            </div>
            </dsp:form>

            <%-- <dsp:setvalue bean="CheckoutProgressStates.currentLevel" value="BILLING" /> --%>

            </dsp:oparam>
            <dsp:oparam name="error">
            <h3>
            Error Associating Shipping information for Virtual Piggy Profile. Please <a href="shipping.jsp">click
            here</a> to try again.
            </h3>
            </dsp:oparam>
            </dsp:droplet>
            </div>
            </div>
            <%-- Order Summary --%>
            <dsp:include page="/checkout/gadgets/checkoutOrderSummary.jsp">
            <dsp:param name="order" bean="/atg/commerce/ShoppingCart.current"/>
            <dsp:param name="currentStage" value="shipping"/>
            </dsp:include>
        </jsp:body>
        </cbp:pageContainer>
        </dsp:page>
        <%-- @version $Id: //hosting-blueprint/B2CBlueprint/version/9.4/Storefront/j2ee/store.war/checkout/shippingSingle.jsp#1 $$Change: 652444 $--%>
